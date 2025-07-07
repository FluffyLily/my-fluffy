<template>
  <div class="login-container ">
    <div class="login-card p-4 rounded shadow-lg">
      <h2 class="text-center mb-4">관리자 로그인</h2>
      <form @submit.prevent="login">
        <div class="mb-3">
          <label for="username" class="form-label">관리자 아이디</label>
          <input v-model="username" type="text" id="username" class="form-control" placeholder="아이디 입력" />
        </div>
        <div class="mb-3">
          <label for="password" class="form-label">비밀번호</label>
          <input v-model="password" type="password" id="password" class="form-control" placeholder="비밀번호 입력" />
        </div>
        <p v-if="errorMessage" class="text-danger text-center">{{ errorMessage }}</p>
        <!-- 로딩 상태에 따라 버튼 텍스트나 로딩 표시 -->
        <button type="submit" class="btn btn-primary w-100" :disabled="isLoading">
          <span v-if="isLoading" class="spinner-border-custom" role="status" aria-hidden="true"></span>
          {{ isLoading ? '로딩중...' : '로그인' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from "../api/axios";
import { useAuthStore } from '../stores/auth';
import { jwtDecode } from 'jwt-decode';

const router = useRouter()
const username = ref('')
const password = ref('')
const errorMessage = ref('')
const isLoading = ref(false);

const authStore = useAuthStore();
watch(() => authStore.accessToken, (newToken) => {
});

const login = async () => {
  isLoading.value = true;
  try {
    const response = await apiClient.post("/auth/login", {
      username: username.value,
      password: password.value,
    });

    const accessToken = response.data.accessToken;

    if (accessToken) {
      authStore.setAccessToken(accessToken);
      apiClient.defaults.headers.common["Authorization"] = `Bearer ${accessToken}`;

      // 토큰에서 userId 추출 후 저장
      const decodedToken = jwtDecode(accessToken);
      const userId = decodedToken.userId; 
      const loginId = decodedToken.sub;
      const roleId = decodedToken.role;
      authStore.setUserId(userId);
      authStore.setLoginId(loginId);
      authStore.setRoleId(roleId);

      nextTick(() => {
        router.push("/main");
      });
      
    } else {
      throw new Error("Access Token이 제공되지 않았습니다.");
    }
  } catch (error) {
    console.error("로그인 요청 에러:", error);
    console.error("에러 응답 데이터:", error.response?.data); 
    
    if (error.response?.status === 403) {
      authStore.clearAccessToken();
      errorMessage.value = "비활성화된 계정입니다. 다른 관리자에게 문의하세요."

    } else if (error.response?.status === 401) {
      authStore.clearAccessToken();
      errorMessage.value = "아이디 또는 비밀번호가 일치하지 않습니다."
    }
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  const params = new URLSearchParams(window.location.search);
  const demo = params.get('demo');

  if (demo === 'admin') {
    username.value = 'demo-admin';
    password.value = '1234qwer!';
    login();
  } else if (demo === 'manager') {
    username.value = 'demo-manager';
    password.value = '1234qwer!';
    login();
  }
});
</script>

<style lang="scss" scoped>

.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #FFF7F7, #E6F7FF);
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: #ffffff;
  border-radius: 15px;
  box-shadow: 0px 8px 15px rgba(0, 0, 0, 0.1);
  padding: 30px;
  text-align: center;
  border: 3px solid #FFB6D4;
}

h2 {
  color: #FF80A1;
  font-weight: bold;
  margin-bottom: 20px;
}

.form-control {
  border: 2px solid #FFB6D4;
  border-radius: 8px;
  padding: 10px;
  font-size: 1rem;
  transition: 0.3s;
}

.form-control:focus {
  border-color: #FF80A1;
  box-shadow: 0px 0px 8px rgba(255, 128, 161, 0.5);
  outline: none;
}

.spinner-border-custom {
  width: 20px;
  height: 20px;
  border: 4px solid transparent;
  border-top-color: #FF80A1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.btn-primary {
  background-color: #FF80A1;
  border: none;
  padding: 12px;
  font-size: 1rem;
  font-weight: bold;
  border-radius: 8px;
  transition: 0.3s;
  display: flex;
  justify-content: center;
  align-items: center;
}

.btn-primary:hover {
  background-color: #FF66A1;
}

.text-danger {
  font-size: 0.9rem;
  font-weight: bold;
  color: #D9534F;
  margin-top: 10px;
}
</style>