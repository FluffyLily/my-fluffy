import.meta.env
import axios from "axios";
import { useAuthStore } from "../stores/auth";

let isRefreshing = false;
let refreshPromise = null
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

    if (!error.response) {
      return Promise.reject(error);
    }

    const status = error.response.status;

    // login / refresh 요청은 refresh 로직 제외
    if (
      status === 401 &&
      (
        originalRequest.url === "/auth/login" ||
        originalRequest.url === "/auth/refresh"
      )
    ) {
      authStore.clearAccessToken();
      return Promise.reject(error);
    }

    // 401 + 아직 재시도 안 한 요청만 refresh
    if (status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        if (!isRefreshing) {
          isRefreshing = true;

          refreshPromise = (async () => {
            try {
              const response = await axios.post(
                `${import.meta.env.VITE_API_BASE_URL}/auth/refresh`,
                {},
                { withCredentials: true }
              );

              const newToken = response.data.accessToken;
              authStore.setAccessToken(newToken);
              return newToken;

            } catch (e) {
              authStore.clearAccessToken();
              throw e;

            } finally {
              isRefreshing = false;
            }
          })();
        }

        const newAccessToken = await refreshPromise;

        originalRequest.headers["Authorization"] =
          `Bearer ${newAccessToken}`;

        return apiClient(originalRequest);

      } catch (refreshError) {
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;