<template>
  <div class="header">
    <div class="logo">관리자 시스템</div>
    <div class="user-info" v-if="authStore.userId">
      <button class="btn btn-user" @click="showProfileModal = true">{{ authStore.loginId }}</button>
      <button class="btn btn-logout" @click="logout">로그아웃</button>
    </div>
  </div>
  <!-- 프로필 수정 모달 -->
  <div v-if="showProfileModal" class="modal fade show d-flex justify-content-center align-items-center" style="display: block;">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">내 정보 수정</h5>
          <button type="button" class="btn-close" @click="showProfileModal = false"></button>
        </div>
        <div class="modal-body">
          <label class="form-label d-block text-start">아이디</label>
          <input :value="profileForm.loginId" class="form-control mb-2" readonly />

          <label class="form-label d-block text-start">현재 비밀번호</label>
          <input v-model="profileForm.currentPassword" type="password" class="form-control mb-2"/>
          <small v-if="currentPasswordError" class="text-danger">{{ currentPasswordError }}</small>

          <label class="form-label d-block text-start">새 비밀번호</label>
          <input v-model="profileForm.loginPassword" type="password" class="form-control mb-2"/>
          <small v-if="passwordError" class="text-danger">{{ passwordError }}</small>

          <label class="form-label d-block text-start">비밀번호 확인</label>
          <input v-model="confirmNewPassword" type="password" class="form-control mb-2"/>
          <small v-if="passwordMismatch" class="text-danger">비밀번호가 일치하지 않습니다.</small>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="updateProfile" :disabled="!isFormValid || passwordError || passwordMismatch">수정</button>
          <button class="btn btn-secondary" @click="showProfileModal = false">취소</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from "../api/axios";
import { useAuthStore } from '../stores/auth';
import { validatePassword } from '../util/passwordValidation';

const router = useRouter();
const authStore = useAuthStore();
const showProfileModal = ref(false);

const profileForm = ref({
  loginId: authStore.loginId,
  currentPassword: '',
  loginPassword: '',
  updatedBy: authStore.userId
});

const confirmNewPassword = ref('');
const passwordError = ref('');
const currentPasswordError = ref('');

const passwordMismatch = computed(() => 
  profileForm.value.loginPassword !== confirmNewPassword.value &&
  confirmNewPassword.value.length > 0
);

const isFormValid = computed(() => {
  return (
    profileForm.value.currentPassword.trim() !== '' &&
    profileForm.value.loginPassword.trim() !== '' &&
    confirmNewPassword.value.trim() !== ''
  );
});

// 현재 비밀번호 검증 함수
const validateCurrentPassword = async (currentPassword, loginId) => {
  try {
    const response = await apiClient.post('/admin/verify-password', {
      username: loginId,
      password: currentPassword
    });
    return response.data.success;
  } catch (error) {
    currentPasswordError.value = "현재 비밀번호가 일치하지 않습니다.";
    return false;
  }
};

// 프로필 업데이트 (비밀번호 변경)
const updateProfile = async () => {
  if (confirm("비밀번호를 변경하시겠습니까?")) {
    const isCurrentPasswordValid = await validateCurrentPassword(profileForm.value.currentPassword, profileForm.value.loginId);
    if (!isCurrentPasswordValid) {
      currentPasswordError.value = "현재 비밀번호가 일치하지 않습니다.";
      return;
    }
    try {
      await apiClient.put(`/admin/update-profile/${authStore.userId}`, profileForm.value);
      alert("비밀번호가 변경되었습니다.");
      showProfileModal.value = false;
    } catch (error) {
      console.error("비밀번호 변경 오류: ", error.response);
      if (error.response && error.response.data) {
        console.error("서버 응답: ", error.response.data);
      }
    }
  }
};

const logout = async () => {
  try {
    await apiClient.post("/auth/logout");
    delete apiClient.defaults.headers.common["Authorization"];
    authStore.clearAccessToken();
    authStore.clearRefreshToken();
    authStore.clearUserId();
    await router.push("/");
  } catch (error) {
    console.error("로그아웃 실패: ", error);
  }
};

watch(showProfileModal, (newVal) => {
  if (!newVal) {
    // 모달이 닫힐 때 입력값들을 초기화
    profileForm.value.currentPassword = '';  // 현재 비밀번호 초기화
    profileForm.value.loginPassword = '';    // 새 비밀번호 초기화
    confirmNewPassword.value = '';           // 비밀번호 확인 초기화
    currentPasswordError.value = '';         // 현재 비밀번호 오류 메시지 초기화
    passwordError.value = '';                // 새 비밀번호 오류 메시지 초기화
  } else {
    profileForm.value.loginId = authStore.loginId;
  }
});

watch(() => profileForm.value.loginPassword, async (newPassword) => {
  if (newPassword) {
    passwordError.value = await validatePassword(newPassword);
  } else {
    passwordError.value = "";
  }
});

watch(() => profileForm.value.currentPassword, () => {
  currentPasswordError.value = '';
});
</script>

<style lang="scss" scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--secondary-color);
  color: white;
  padding: 0 20px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 70px;
  box-shadow: 0px 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 1.6rem;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
}

.btn-user {
  padding: 8px 15px;
  background-color: var(--button-warning-color);
  color: white;
  border: none;
  border-radius: 8px;
  margin-right: 10px;
  transition: background-color 0.3s;
  cursor: pointer;
}

.btn-user:hover {
  background-color: var(--button-hover-warning);
}

.btn-logout {
  padding: 8px 15px;
  background-color: var(--button-danger-color);
  color: white;
  border: none;
  border-radius: 8px;
  transition: background-color 0.3s;
  cursor: pointer;
}

.btn-logout:hover {
  background-color: var(--button-hover-danger);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  width: 400px;
}
</style>