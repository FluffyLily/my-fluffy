import.meta.env
import axios from "axios";
import { useAuthStore } from "../stores/auth";
import { jwtDecode } from 'jwt-decode';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청 인터셉터 추가
apiClient.interceptors.request.use(
  (config) => {
    try {
      const authStore = useAuthStore();

      if (authStore.accessToken) {
        try {
          const decodedToken = jwtDecode(authStore.accessToken);
          authStore.setUserId(decodedToken.userId);

          config.headers["Authorization"] = `Bearer ${authStore.accessToken}`;
        } catch (error) {
          console.error("JWT 디코딩 오류 :", error);
          authStore.clearAccessToken();
        }
      }
    } catch (e) {
      console.log('Pinia 스토어를 가져오는 중 오류 발생 : ', e);
    }
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const authStore = useAuthStore();
    const originalRequest = error.config;
    if (error.response) {
      const status = error.response.status;

      // 로그인 요청에서 401이 발생한 경우 refresh 요청을 보내지 않음
      if (status === 401 && originalRequest.url === "/auth/login") {
        console.log("로그인 실패 - 잘못된 아이디 또는 비밀번호");
        return Promise.reject(error); // 그대로 에러 반환
      }
      
      // 그 외 401 발생 시 (Access Token 만료), refresh 시도
      if (status === 401) {  // 인증 오류 발생 시 (Access Token 만료)
        try {
          const refreshResponse = await apiClient.post(
            "/auth/refresh",
            {}, 
            { withCredentials: true }
          );

          const newAccessToken = refreshResponse.data.accessToken;

          if (newAccessToken) {
            authStore.setAccessToken(newAccessToken);

            // 기존 요청을 다시 보내기
            const newConfig = { ...error.config };
            newConfig.headers["Authorization"] = `Bearer ${newAccessToken}`;
            return apiClient(newConfig);
          } else {
            throw new Error("Refresh Token으로 Access Token을 얻을 수 없습니다.");
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