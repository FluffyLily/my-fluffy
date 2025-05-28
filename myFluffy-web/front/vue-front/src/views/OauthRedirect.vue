<template>
    <div>로그인 처리 중입니다...</div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import apiClient from "../api/axios";

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

onMounted(async () => {
    try {
        const { data } = await apiClient.get('/auth/oauth/me', {
            withCredentials: true
        });

        const { userId, loginId, userName, userType } = data;
        authStore.setUserInfo({ userId, loginId, userName, userType });

        router.replace('/');
    } catch (e) {
        console.error("소셜 로그인 처리 실패", e);
        authStore.clearAccessToken();
        router.replace('/login');
    }
});

</script>