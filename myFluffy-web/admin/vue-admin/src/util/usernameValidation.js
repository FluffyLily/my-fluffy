import apiClient from '../api/axios';
import { useAuthStore } from '../stores/auth';

export const validateUsername = async (loginId) => {
    const authStore = useAuthStore();
    if (!authStore.accessToken) {
        console.error("인증 토큰이 없습니다.");
        return "로그인이 필요합니다.";
    }

    const minLength = 6;
    if (!loginId) {
        return "아이디를 입력해주세요.";
    } else if (loginId.length < minLength) {
        return "아이디는 최소 6자 이상이어야 합니다.";
    }
    
    try {
        const response = await apiClient.get("/admin/check-username", {
        params: { loginId }
        });

        return response.data ? "사용 가능한 아이디입니다." : "이미 존재하는 아이디입니다.";
    } catch (error) {
        console.error("아이디 중복 체크 실패:", error);
        return "아이디 중복 체크 중 오류가 발생했습니다.";
    }
};
