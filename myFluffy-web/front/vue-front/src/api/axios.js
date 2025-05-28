import axios from "axios";
import { useAuthStore } from "../stores/auth";

const apiClient = axios.create({
    baseURL: "http://localhost:8082/api",
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    },
})

// 요청 인터셉터: accessToken 체크만, Authorization 헤더 설정 제거
apiClient.interceptors.request.use(
    (config) => {
        try {
            const authStore = useAuthStore()
            // accessToken 존재 여부만 체크, 아무 동작도 하지 않음
            if (!authStore.accessToken) {
                // do nothing
            }
        } catch (error) {
            console.log('Pinia 스토어를 가져오는 중 오류 발생 : ', error);
        }
        return config
    },
    (error) => Promise.reject(error)
);

// 응답 인터셉터: 토큰 갱신 로직 제거, 실패 시 그대로 reject
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        return Promise.reject(error)
    }
)

export default apiClient;