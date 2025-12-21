import.meta.env
import axios from "axios";
import { useAuthStore } from "../stores/auth";

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use((config) => {
    const authStore = useAuthStore();
    if (authStore.accessToken) {
        config.headers["Authorization"] = `Bearer ${authStore.accessToken}`;
    }
    return config;
  });

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const authStore = useAuthStore();
    const originalRequest = error.config;
    if (error.response) {
      const status = error.response.status;

      if (status === 401 && originalRequest.url === "/auth/login") {
        return Promise.reject(error);
      }
      
      if (status === 401) {
        try {
          const refreshResponse = await apiClient.post(
            "/auth/refresh",
            {}, 
            { withCredentials: true }
          );

          const newAccessToken = refreshResponse.data.accessToken;

          if (newAccessToken) {
            authStore.setAccessToken(newAccessToken);

            const newConfig = { ...error.config };
            newConfig.headers["Authorization"] = `Bearer ${newAccessToken}`;
            return apiClient(newConfig);
          } else {
            throw new Error("Access Token을 재발급할 수 없습니다.");
          }
        } catch (refreshError) {
          console.error("토큰 갱신 실패", refreshError);
          authStore.clearAccessToken();
          return Promise.reject(refreshError);
        }
      } 
    }

    return Promise.reject(error);
  }
);

export default apiClient;