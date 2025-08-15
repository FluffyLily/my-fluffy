<template>
  <div class="page-wrapper">
    <div class="page-container">
      <div class="page-content">
        <h1 class="main-title">관리자 목록</h1>
        <div v-if="isAdmin" class="actions mb-3 text-start">
          <button class="btn register-btn" @click="registerNewAdmin">관리자 등록</button>
        </div>
        <div v-if="loading">로딩 중...</div>
        <table v-else class="admin-table">
          <thead>
            <tr>
              <th>번호</th>
              <th>로그인 ID</th>
              <th>권한</th>
              <th>초기화 여부</th>
              <th>등록일</th>
              <th>수정일</th>
              <th>활성 상태</th>
              <th>
                <template v-if="isAdmin">계정 관리</template>
                <template v-else>계정 관리</template>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(admin, index) in admins" :key="admin.userId">
              <td>{{ index + 1 }}</td>
              <td class="login-id">{{ admin.loginId }}</td>
              <td>
                <span class="role-tag" :class="getRoleColorClass(admin.roleId)">
                  {{ admin.roleName }}
                </span>
              </td>
              <td>{{ admin.isInitialized ? '초기화됨' : '초기화되지 않음' }}</td>
              <td>{{ formatDate(admin.createdAt) }}</td>
              <td>{{ formatDate(admin.updatedAt) }}</td>
              <td>{{ admin.isActive ? '사용 중' : '사용 안함' }}</td>
              <td>
                <template v-if="canEditOrDelete(admin)">
                  <div class="admin-action-buttons">
                    <button v-if="canEdit(admin)"
                      class="btn btn-warning btn-sm"
                      @click="openEditModal(admin)">
                      수정
                    </button>
                    <button v-if="canDelete(admin)"
                      class="btn btn-danger btn-sm"
                      @click="openDeleteModal(admin)">
                      삭제
                    </button>
                    <button v-else
                      class="btn btn-danger btn-sm forbidden-button"
                      disabled>
                      삭제
                    </button>
                  </div>
                </template>
                <template v-else><span class="emoji-text">🚫</span> 권한 없음</template>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 관리자 계정 등록 모달 -->
      <div v-if="showCreateModal" class="modal fade show d-flex justify-content-center align-items-center" style="display: block;" tabindex="-1" role="dialog">
        <div class="modal-dialog modal-dialog-centered" role="document">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">관리자 등록</h5>
              <button type="button" class="btn-close" aria-label="Close" @click="showCreateModal = false"></button>
            </div>
            <div class="modal-body">
              <label class="form-label d-block text-start">아이디</label>
              <input v-model="newAdmin.loginId" ref="newLoginIdInput" placeholder="계정 아이디" class="form-control mb-2"/>
              <small v-if="loginIdError" 
                :class="{
                  'text-danger': loginIdError !== '사용 가능한 아이디입니다.', 
                  'text-primary': loginIdError === '사용 가능한 아이디입니다.'
                }"
              >
                {{ loginIdError }}
              </small>

              <label class="form-label d-block text-start">비밀번호</label>
              <input v-model="newAdmin.loginPassword" placeholder="비밀번호" type="password" class="form-control mb-2"/>
              <small v-if="passwordError" class="text-danger">{{ passwordError }}</small>

              <label class="form-label d-block text-start">비밀번호 확인</label>
              <input v-model="confirmPassword" placeholder="비밀번호 확인" type="password" class="form-control mb-2"/>
              <small v-if="passwordMismatch" class="text-danger">비밀번호가 일치하지 않습니다.</small>

              <div class="mb-3">
                <label class="form-label d-block text-start">권한</label>
                <select v-model="newAdmin.roleId" class="form-select">
                  <option value="" disabled selected hidden>권한을 선택하세요</option>
                  <option v-for="role in availableRoles" :key="role.roleId" :value="role.roleId">{{ role.roleName }}</option>
                </select>
              </div>            
            </div>

            <div class="modal-footer">
              <button class="btn register-btn" @click="createAdmin" :disabled="!isFormValid || !isValidLoginId || passwordError || passwordMismatch">등록하기</button>
              <button class="btn btn-secondary" @click="showCreateModal = false">취소</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 관리자 계정 변경 모달 -->
      <div v-if="showEditModal" class="modal fade show d-flex justify-content-center align-items-center" style="display: block;" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">관리자 수정
                <span class="bg-light rounded px-2 text-warning fst-italic">({{ editForm.loginId }})</span>
              </h5>
              <button type="button" class="btn-close" aria-label="Close" @click="showEditModal = false"></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label d-block text-start">권한</label>
                <select v-model="editForm.roleId" class="form-select">
                  <option v-for="role in availableRoles" :key="role.roleId" :value="role.roleId">
                    {{ role.roleName }}
                  </option>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label d-block text-start">활성화 여부</label>
                <select v-model="editForm.isActive" class="form-select">
                  <option :value="true">사용함</option>
                  <option :value="false">사용 안함</option>
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-warning" @click="updateAdmin">변경하기</button>
              <button class="btn btn-secondary" @click="showEditModal = false">취소</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 관리자 삭제 모달 -->
      <div v-if="showDeleteModal" class="modal fade show d-flex justify-content-center align-items-center" style="display: block;" tabindex="-1" role="dialog">
        <div class="modal-dialog modal-dialog-centered" role="document">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">관리자 삭제 
                <span class="bg-light rounded px-2 text-danger fst-italic">({{ deleteLoginId }})</span>
              </h5>
              <button type="button" class="btn-close" aria-label="Close" @click="() => { showDeleteModal = false; deletePassword = ''; }"></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label d-block">관리자 비밀번호 확인:</label>
                <input type="password" v-model="deletePassword" ref="deletePasswordInput" class="form-control mb-2" />
                <div v-if="deleteError" class="text-danger mb-3">{{ deleteError }}</div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-danger" @click="deleteAdmin">확인</button>
              <button class="btn btn-secondary" @click="() => { showDeleteModal = false; deletePassword = ''; }">취소</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import apiClient from '../api/axios.js';
import { format } from 'date-fns';
import { useAuthStore } from '../stores/auth';
import { useToast } from 'vue-toastification';
import { validatePassword } from '../util/passwordValidation.js';
import { validateUsername } from '../util/usernameValidation.js';
import { getRoleLevel, useCurrentUserRoleLevel, useAvailableRoles } from '../util/roleUtils.js';

const authStore = useAuthStore();
const toast = useToast();
const admins = ref([]);
const loading = ref(true);

const showCreateModal = ref(false);
const showEditModal = ref(false);
const showDeleteModal = ref(false);
const newLoginIdInput = ref(null);
const deletePasswordInput = ref(null);
const deletePassword = ref('');
const deleteUserId = ref(null);
const deleteLoginId = ref(null);
const deleteError = ref('');

const availableRoles = useAvailableRoles(authStore);
const currentUserRoleLevel = useCurrentUserRoleLevel(authStore);

const isAdmin = computed(() => 
  authStore.roleId === 'ROLE_ADMIN' || authStore.roleId === 'ROLE_SUPER_ADMIN'
);

// 역할별 고정 색상 네임택 클래스 매핑
const roleColorMap = {};
const roleColors = ['role-tag--yellow', 'role-tag--purple', 'role-tag--mint'];
let roleColorIndex = 0;

// 관리자 계정 등록 폼
const newAdmin = ref({
  loginId: '',
  loginPassword: '',
  roleId: '',
  isActive: true,
  createdBy: authStore.userId,
  createdAt: '',
  updatedBy: authStore.userId,
  updatedAt: '',
  isInitialized: false
});
// 관리자 계정 변경 폼
const editForm = ref({ 
  userId: null,
  loginId: '',
  roleId: '', 
  isActive: true, 
  updatedBy: authStore.userId,
  updatedAt: ''
});

// 아이디 중복 검사
const loginIdError = ref('');
const isValidLoginId = computed(() => 
  !loginIdError.value || loginIdError.value === "사용 가능한 아이디입니다."
);

// 비밀번호 확인
const confirmPassword = ref('');
const passwordError = ref('');
const passwordMismatch = computed(() =>
  newAdmin.value.loginPassword !== confirmPassword.value && confirmPassword.value.length > 0
);

// 폼 유효성 검사
const isFormValid = computed(() => {
  return (
    newAdmin.value.loginId.trim() !== '' &&
    newAdmin.value.loginPassword.trim() !== '' &&
    confirmPassword.value.trim() !== '' &&
    newAdmin.value.roleId !== ''
  );
});

// 함수 정의
const getRoleColorClass = (roleId) => {
  if (!roleColorMap[roleId]) {
    const color = roleColors[roleColorIndex % roleColors.length];
    roleColorMap[roleId] = color;
    roleColorIndex++;
  }
  return roleColorMap[roleId];
};

// 권한 비교 함수들
// 'demo-xxx' 계정 (테스트용)
const isDemoUser = authStore.loginId?.startsWith('demo-');

// 수정 가능 권한 함수
const canEdit = (targetAdmin) => {
  const targetLevel = getRoleLevel(targetAdmin.roleId);
  const currentLevel = currentUserRoleLevel.value;

  if (authStore.loginId === targetAdmin.loginId) return false;
  if (isDemoUser && !(targetAdmin.loginId.startsWith('demo-') || targetAdmin.loginId.startsWith('sample-'))) return false;

  return currentLevel >= targetLevel;
};

// 삭제 가능 권한 함수
const canDelete = (targetAdmin) => {
  const targetLevel = getRoleLevel(targetAdmin.roleId);
  const currentLevel = currentUserRoleLevel.value;

  if (authStore.loginId === targetAdmin.loginId) return false;
  if (authStore.roleId !== 'ROLE_SUPER_ADMIN') return false;

  return currentLevel > targetLevel;
};

const canEditOrDelete = (targetAdmin) => {
  return canEdit(targetAdmin) || canDelete(targetAdmin);
};

// 날짜 포맷팅
const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  try {
    return format(new Date(dateString), 'yyyy-MM-dd HH:mm:ss');
  } catch (error) {
    return 'Invalid Date';
  }
};

const resetNewAdminForm = () => {
  newAdmin.value = {
    loginId: '',
    loginPassword: '',
    roleId: '',
    isActive: true,
    createdBy: authStore.userId,
    createdAt: '',
    updatedBy: authStore.userId,
    updatedAt: '',
    isInitialized: false
  };
  confirmPassword.value = '';
  loginIdError.value = '';
  passwordError.value = '';
};

// 서버 통신 함수들
// 관리자 목록 불러오기
const fetchAdmins = async () => {
  try {
    const response = await apiClient.get('/admin/list', {
      headers: { Authorization: `Bearer ${authStore.accessToken}` }
    });
    admins.value = response.data;
  } catch (error) {
    console.error("Error response:", error.response);
  } finally {
    loading.value = false;
  }
};

// 관리자 등록 모달 열기
const registerNewAdmin = async () => {
  showCreateModal.value = true;
  await nextTick();
  newLoginIdInput.value?.focus();
}

// 관리자 새로 등록하기
const createAdmin = async () => {

  if (confirm("새로운 관리자 계정을 생성하시겠습니까?")) {
  newAdmin.value.createdAt = new Date().toISOString();;
  newAdmin.value.updatedAt = new Date().toISOString();;
  
  await apiClient.post('/admin/create', newAdmin.value, {
    headers: { Authorization: `Bearer ${authStore.accessToken}` }
  });
  showCreateModal.value = false;
  fetchAdmins();
  }
};

// 관리자 계정 변경 모달 열기
const openEditModal = (admin) => {
  editForm.value = { 
    userId: admin.userId,
    loginId: admin.loginId,
    roleId: admin.roleId,
    isActive: admin.isActive,
    updatedAt: new Date().toISOString(),
    updatedBy: authStore.userId
  };
  showEditModal.value = true;
};

// 관리자 계정 변경
const updateAdmin = async () => {
  if (confirm("수정사항을 반영하시겠습니까?")) {
    await apiClient.put(`/admin/${editForm.value.userId}`, editForm.value);
    showEditModal.value = false;
    toast.success("관리자 계정이 변경되었습니다.");
    fetchAdmins();
  }
};

// 관리자 계정 삭제 모달 열기
const openDeleteModal = async (admin) => {
  deleteUserId.value = admin.userId;
  deleteLoginId.value = admin.loginId;
  showDeleteModal.value = true;
  await nextTick();
  deletePasswordInput.value?.focus();
};

// 관리자 계정 삭제
const deleteAdmin = async () => {
  try {
    if (!deletePassword.value || deletePassword.value.trim().length === 0) {
      deleteError.value = '비밀번호를 입력하세요.';
      return;
    }
    await apiClient.delete(`/admin/${deleteUserId.value}`, {
      data: { password: deletePassword.value }
    });
    await fetchAdmins();
    toast.success("관리자 계정이 삭제되었습니다.");
    showDeleteModal.value = false;
    deletePassword.value = '';
    deleteError.value = '';
  } catch (error) {
    if (error?.response?.status === 401 || error?.response?.status === 403) {
      deleteError.value = '권한이 없거나 인증이 만료되었습니다. 다시 로그인 해주세요.';
    } else if (error?.response?.status === 400) {
      deleteError.value = error.response?.data?.message || '요청이 올바르지 않습니다.';
      deletePasswordInput.value?.focus();
    } else {
      deleteError.value = '삭제 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
    }
    console.error(error);
  }
};

onMounted(fetchAdmins);

// watchers
// 아이디 중복 확인
watch(() => newAdmin.value.loginId, async (newLoginId) => {
  if (newLoginId) {
    loginIdError.value = await validateUsername(newLoginId);
  } else {
    loginIdError.value = "";
  }
});

// 비밀번호 유효성 검사
watch(() => newAdmin.value.loginPassword, async (newPassword) => {
  if (newPassword) {
    passwordError.value = await validatePassword(newPassword);
  } else {
    passwordError.value = "";
  }
});

// 생성 모달 닫힐 때 초기화
watch(showCreateModal, (newVal) => {
  if (!newVal) {
    resetNewAdminForm();
  }
});

// 삭제 모달 닫힐 때 에러 문구 초기화
watch(showDeleteModal, (newVal) => {
  if (!newVal) {
    deleteError.value = '';
  }
});

// 삭제 비밀번호 입력 시 에러 문구 제거
watch(deletePassword, () => {
  deleteError.value = '';
});
</script>

<style lang="scss" scoped>

.page-wrapper {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f8f9fa;
}
.page-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background-color: white;
  overflow-y: auto;
  scroll-behavior: smooth;
}
.page-content {
  flex: 1;
  background-color: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}
.main-title {
  font-size: 2rem;
  font-weight: bold;
  color: var(--violet-deep);
  margin: 14px 0 30px 0;
}

// 관리자 등록 버튼
.register-btn {
  background-color: var(--button-add-color);
  border-color: var(--button-add-color);
  color: white;
  font-weight: bold;
  padding: 4px 12px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s, border-color 0.3s;

  &:hover {
    background-color: var(--button-hover-add);
    border-color: var(--button-hover-add);
    color: white;
  }

  &:disabled {
    background-color: var(--button-hover-add);
    border-color: var(--button-hover-add);
    color: white;
    cursor: not-allowed;
    pointer-events: none;
  }
}

// 테이블 스타일
.admin-table {
  width: 100%;
  margin-top: 20px;
  border-collapse: separate;
  border-spacing: 0 8px;

  th, td {
    font-size: 14px;
    text-align: center;
    padding: 12px 14px;
    white-space: nowrap;
  }
  th {
    background-color: var(--card-bg-mint);
    font-weight: bold;
    color: #333;
  }
  td {
    background-color: white;
    border-radius: 8px;
    vertical-align: middle;
    transition: background-color 0.2s ease;
    &.login-id {
      font-weight: 600;
      color: #222;
    }
    .emoji-text {
      font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif;
    }
    
  }
  .role-tag {
    display: inline-block;
    padding: 2px 8px;
    font-size: 14px;
    border-radius: 12px;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 120px;
    &--yellow { background-color: var(--highlight-yellow); color: black; }
    &--purple { background-color: var(--highlight-purple); color: white; }
    &--mint   { background-color: var(--highlight-mint); color: black; }
  }

  .role-tag--yellow { background-color: var(--highlight-yellow); color: black; }
  .role-tag--purple { background-color: var(--highlight-purple); color: white; }
  .role-tag--mint   { background-color: var(--highlight-mint); color: black; }

  // 관리자 목록 - 수정, 삭제 버튼
  td {
    & .admin-action-buttons {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      flex-wrap: nowrap;
    }
  }
  // 관리자 목록 - 수정, 삭제 버튼 스타일
  button {
    padding: 4px 12px;
    font-size: 0.875rem;
    border-radius: 6px;
    height: 36px;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.3s, border-color 0.3s;

    &.btn-warning {
      background-color: var(--button-warning-color);
      border-color: var(--button-warning-color);
      color: white;
      &:hover {
        background-color: var(--button-hover-warning);
        border-color: var(--button-hover-warning);
      }
    }

    &.btn-danger {
      background-color: var(--button-danger-color);
      border-color: var(--button-danger-color);
      color: white;
      position: relative;
      overflow: hidden;
      &:hover {
        background-color: var(--button-hover-danger);
        border-color: var(--button-hover-danger);
      }
      &.forbidden-button {
        background-color: var(--button-danger-color);
        border-color: var(--button-danger-color);
        color: white;
        opacity: 0.6;
        pointer-events: none;
        display: flex;
        align-items: center;
        justify-content: center;
        height: 36px;
        position: relative;
        &::after {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 200%;
          height: 2px;
          background-color: #9d0d3b;
          transform: rotate(35deg);
          transform-origin: top left;
          pointer-events: none;
        }
      }
    }
  }
}

// 모달 오버레이
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

// 버튼 스타일 (등록, 수정, 삭제, 취소)
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 5px;
  button {
    padding: 4px 12px;
    font-size: 0.875rem;
    border-radius: 6px;
    height: 36px;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.3s, border-color 0.3s;
    &.btn-secondary {
      background-color: var(--button-close-color);
      border-color: var(--button-close-color);
      color: white;
      &:hover {
        background-color: var(--button-hover-close);
        border-color: var(--button-hover-close);
      }
    }
    &.btn-warning {
      background-color: var(--button-warning-color);
      border-color: var(--button-warning-color);
      color: white;
      &:hover {
        background-color: var(--button-hover-warning);
        border-color: var(--button-hover-warning);
      }
    }
    &.btn-danger {
      background-color: var(--button-danger-color);
      border-color: var(--button-danger-color);
      color: white;
      &:hover {
        background-color: var(--button-hover-danger);
        border-color: var(--button-hover-danger);
      }
    }
  }
}


</style>

