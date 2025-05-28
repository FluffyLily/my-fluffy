<template>
  <div class="page-wrapper">
    <div class="page-container">
      <div class="page-content">
        <h1 class="main-title">회원 관리</h1>
        <p>회원 목록 조회, 활동 상태 확인, 불법 계정 사용 정지 및 탈퇴 처리</p>
        <div v-if="loading">로딩 중...</div>
        <table v-else class="user-table">
          <thead>
            <tr>
              <th>번호</th>
              <th>이름</th>
              <th>아이디</th>
              <th>이메일</th>
              <th>전화번호</th>
              <th>계정 구분</th>
              <th>등록일</th>
              <th>수정일</th>
              <th v-if="isAdmin">비밀번호 변경일</th>
              <th v-if="isAdmin">비밀번호 오류</th>
              <th v-if="isAdmin">로그인 횟수</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(user, index) in users" :key="user.userId">
              <td>{{ index + 1 }}</td>
              <td>{{ user.userName }}</td>
              <td>{{ user.loginId }}</td>
              <td>{{ user.email }}</td>
              <td>{{ user.phoneNumber }}</td>
              <td>{{ user.userTypeCode }}</td>
              <td>{{ formatDate(user.createdAt) }}</td>
              <td>{{ formatDate(user.updatedAt) }}</td>
              <td v-if="isAdmin">{{ formatDate(user.lastPasswordChangeAt) }}</td>
              <td v-if="isAdmin">{{ user.passwordErrorCount }}</td>
              <td v-if="isAdmin">{{ user.loginCount }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { format } from 'date-fns';
import apiClient from '../api/axios';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
const users = ref([]);
const loading = ref(true);

const isAdmin = computed(() =>
  authStore.roleId === 'ROLE_ADMIN' || authStore.roleId === 'ROLE_SUPER_ADMIN'
);

// const fetchUsers = async () => {
//   try {
//     const response = await apiClient.get('/user/list', {
//       headers: { Authorization: `Bearer ${authStore.accessToken}` }
//     });
//     users.value = response.data;
//   } catch (e) {
//     console.error('회원 목록 로딩 실패:', e);
//   } finally {
//     loading.value = false;
//   }
// };

const formatDate = (date) => {
  if (!date) return 'N/A';
  try {
    return format(new Date(date), 'yyyy-MM-dd HH:mm:ss');
  } catch {
    return 'Invalid';
  }
};

// onMounted(fetchUsers);
onMounted(() => {
  users.value = [
    {
      userId: 1,
      userName: '홍길동',
      loginId: 'hong123',
      email: 'hong@example.com',
      phoneNumber: '010-1234-5678',
      userTypeCode: '일반회원',
      createdAt: '2024-01-01T10:00:00',
      updatedAt: '2024-04-01T12:00:00',
      lastPasswordChangeAt: '2024-03-01T08:30:00',
      passwordErrorCount: 0,
      loginCount: 5,
    },
    {
      userId: 2,
      userName: '김영희',
      loginId: 'kimyh',
      email: 'kim@example.com',
      phoneNumber: '010-9876-5432',
      userTypeCode: '정지회원',
      createdAt: '2023-11-20T09:15:00',
      updatedAt: '2024-03-28T11:45:00',
      lastPasswordChangeAt: '2024-01-15T14:10:00',
      passwordErrorCount: 3,
      loginCount: 12,
    },
  ];
  loading.value = false;
});
</script>

<style scoped lang="scss">
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
  color: var(--avocado-frost);
  margin-top: 14px;
  margin-bottom: 30px;
}

.user-table {
  width: 100%;
  margin-top: 20px;
  border-collapse: separate;
  border-spacing: 0 8px;

  th {
    text-align: center;
    padding: 10px 14px;
    background-color: var(--card-bg-mint);
    font-size: 14px;
    font-weight: bold;
    color: #333;
  }

  td {
    background-color: white;
    padding: 12px 14px;
    font-size: 14px;
    text-align: center;
    border-radius: 8px;
    vertical-align: middle;
  }
}
</style>
