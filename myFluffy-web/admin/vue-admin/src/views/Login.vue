<template>
  <div class="login-container ">
    <div class="login-card p-4 rounded shadow-lg">
      <h2 class="text-center mb-4">관리자 로그인</h2>
      <form @submit.prevent="handleLogin">
        <div class="mb-3">
          <label for="username" class="form-label">관리자 아이디</label>
          <input v-model="username" type="text" id="username" ref="usernameInput" class="form-control" placeholder="아이디 입력" />
        </div>
        <div class="mb-3">
          <label for="password" class="form-label">비밀번호</label>
          <input v-model="password" type="password" id="password" class="form-control" placeholder="비밀번호 입력" />
        </div>
        <p v-if="errorMessage" class="text-danger text-center">{{ errorMessage }}</p>
        <button type="submit" class="btn btn-primary w-100" :disabled="isLoading">
          <span v-if="isLoading" class="spinner-border-custom" role="status" aria-hidden="true"></span>
          {{ isLoading ? '로딩중...' : '로그인' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from "../api/axios";
import { useAuthStore } from '../stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const usernameInput = ref(null);
const username = ref('');
const password = ref('');
const errorMessage = ref('');
const isLoading = ref(false);

const handleLogin = async () => {
  const success = await login();
  if (success) {
    await nextTick();
    router.push("/main");
  }
};

const login = async () => {
  isLoading.value = true;
  try {
    const response = await apiClient.post("/auth/login", {
      username: username.value,
      password: password.value,
    });

    const accessToken = response.data.accessToken;

    if (!accessToken) {
      throw new Error("Access Token이 제공되지 않았습니다.");
    }

    authStore.setAccessToken(accessToken);
    return true;

  } catch (error) {
    if (error.response?.status === 403) {
      errorMessage.value = "비활성화된 계정입니다. 다른 관리자에게 문의하세요.";
    } else if (error.response?.status === 401) {
      errorMessage.value = "아이디 또는 비밀번호가 일치하지 않습니다.";
    } else {
      errorMessage.value = "로그인 중 오류가 발생했습니다.";
    }

    authStore.clearAccessToken();
    return false;
    
  } finally {
    isLoading.value = false;
  }
};
onMounted(() => {
  usernameInput.value?.focus();
});

</script>

<style lang="scss" scoped>

.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, var(--background-color), var(--mystic-skyblue));
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: var(--card-bg);
  border-radius: 15px;
  box-shadow: 0px 8px 15px rgba(0, 0, 0, 0.1);
  padding: 30px;
  text-align: center;
  border: 3px solid var(--card-border-pink);

  h2 {
    color: var(--point-pink);
    font-weight: bold;
    margin-bottom: 20px;
  }

  .form-control {
    border: 2px solid var(--card-border-pink);
    border-radius: 8px;
    padding: 10px;
    font-size: 1rem;
    transition: 0.3s;

    &:focus {
      border-color: var(--point-pink);
      box-shadow: 0px 0px 8px rgba(255, 128, 161, 0.5);
      outline: none;
    }
  }
}

.spinner-border-custom {
  width: 20px;
  height: 20px;
  border: 4px solid transparent;
  border-top-color: var(--point-pink);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.btn-primary {
  background-color: var(--point-pink);
  border: none;
  padding: 12px;
  font-size: 1rem;
  font-weight: bold;
  border-radius: 8px;
  transition: 0.3s;
  display: flex;
  justify-content: center;
  align-items: center;

  &:hover {
    background-color: var(--highlight-hotpink);
  }
}

.text-danger {
  font-size: 0.9rem;
  font-weight: bold;
  color: var(--dangerous-crimson);
  margin-top: 10px;
}
</style>