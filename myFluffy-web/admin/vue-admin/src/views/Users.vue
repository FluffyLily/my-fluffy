<template>
  <div class="page-wrapper">
    <div class="page-container">
      <div class="page-content">
        <h1 class="main-title">회원 관리</h1>
        <div class="filter-section">
          <div class="filter-item">
            <select v-model="searchCondition.userTypeCode" @change="fetchUsers" class="instant-filter">
              <option :value="null">전체 회원</option>
              <option value="normal">통합 회원</option>
              <option value="google">구글 회원</option>
              <option value="kakao">카카오 회원</option>
              <option value="suspended">정지 회원</option>
            </select>
          </div>
          <div class="filter-item">
            <select v-model="searchCondition.sort" @change="fetchUsers" class="instant-filter">
              <option value="recent">최신순</option>
              <option value="old">오래된순</option>
            </select>
          </div>
          <div class="filter-item search-group">
            <select v-model="searchCondition.searchType">
              <option :value="null" disabled selected>검색 조건</option>
              <option value="userName">이름</option>
              <option value="loginId">아이디</option>
              <option value="email">이메일</option>
            </select>
            <input
              type="text"
              v-model="searchCondition.searchKeyword"
              @keyup.enter="fetchUsers"
              placeholder="검색어 입력"
            />
            <button @click="fetchUsers">검색</button>
            <button class="reset-button" @click="resetSearch">초기화</button>
          </div>
        </div>

        <div class="post-list-content">
          <div v-if="users.length === 0">회원이 없습니다.</div>
          <table class="post-list-table" v-if="users.length > 0">
            <thead>
              <tr>
                <th>번호</th>
                <th>회원 구분</th>
                <th>아이디</th>
                <th>이름</th>
                <th>이메일</th>
                <th>가입일</th>
                <th>최근 로그인</th>
                <th>로그인 횟수</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(user, index) in users" :key="user.userId">
                <td>{{ currentPage * searchCondition.limit - searchCondition.limit + index + 1 }}</td>
                <td><span class="user-tag" :class="`user-tag--${user.userTypeCode.toLowerCase()}`">{{ user.userTypeCode }}</span></td>
                <td>{{ user.loginId }}</td>
                <td>{{ user.userName }}</td>
                <td>{{ user.email }}</td>
                <td>{{ formatDate(user.createdAt) }}</td>
                <td>{{ formatDate(user.lastLoginAt) }}</td>
                <td>{{ user.loginCount }}</td>
              </tr>
            </tbody>
          </table>

          <div class="pagination">
            <button :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">이전</button>
            <button
              v-for="page in visiblePages"
              :key="page"
              :class="{ active: currentPage === page }"
              @click="goToPage(page)"
            >
              {{ page }}
            </button>
            <button :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)">다음</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { format } from 'date-fns';
import apiClient from '../api/axios';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
const users = ref([]);
const totalCount = ref(0);

const searchCondition = reactive({
  userTypeCode: null,
  sort: 'recent',
  searchType: null,
  searchKeyword: '',
  offset: 0,
  limit: 10
});

const fetchUsers = async () => {
  try {
    const response = await apiClient.post('/user/list', searchCondition, {
      headers: { Authorization: `Bearer ${authStore.accessToken}` }
    });
    users.value = response.data.users;
    totalCount.value = response.data.totalCount;

    // offset 유효성 검사 및 보정 후 페이지 이동
    const maxOffset = Math.max(0, (totalPages.value - 1)) * searchCondition.limit;
    if (searchCondition.offset > maxOffset) {
      searchCondition.offset = 0;
      goToPage(1);
      return;
    }
  } catch (e) {
    console.error('회원 목록 로딩 실패:', e);
    users.value = [];
  }
};

const resetSearch = () => {
  searchCondition.userTypeCode = null;
  searchCondition.sort = 'recent';
  searchCondition.searchType = null;
  searchCondition.searchKeyword = '';
  fetchUsers();
};

const formatDate = (date) => {
  return format(new Date(date), 'yyyy-MM-dd HH:mm');
};

const currentPage = computed(() => Math.floor(searchCondition.offset / searchCondition.limit) + 1);
const totalPages = computed(() => Math.ceil(totalCount.value / searchCondition.limit));

const goToPage = (page) => {
  const safePage = Math.min(page, totalPages.value);
  if (safePage < 1) return;
  searchCondition.offset = (safePage - 1) * searchCondition.limit;
  fetchPosts();
};

const visiblePages = computed(() => {
  const total = totalPages.value;
  const current = Math.min(currentPage.value, total);
  const delta = 2;
  const pages = [];

  for (let i = Math.max(1, current - delta); i <= Math.min(total, current + delta); i++) {
    pages.push(i);
  }

  return pages;
});

onMounted(fetchUsers);
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
  color: var(--seafoam-teal);
  margin-top: 14px;
  margin-bottom: 30px;
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  

  .filter-item {
    display: flex;
    align-items: center;
    gap: 8px;

    select,
    input {
      padding: 6px 10px;
      border-radius: 6px;
      border: 1px solid #ccc;
      font-size: 14px;
    }

    select.instant-filter {
      background-color: var(--melon-icecream);
      border: 1px solid var(--avocado-frost);
      font-weight: 500;
      font-size: 13px;
      padding-right: 24px;
      background-image: url("data:image/svg+xml,%3Csvg fill='%23cbd690' height='18' viewBox='0 0 24 24' width='18' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M7 10l5 5 5-5z'/%3E%3Cpath d='M0 0h24v24H0z' fill='none'/%3E%3C/svg%3E");
      background-repeat: no-repeat;
      background-position: right 6px center;
      background-size: 16px 16px;
      appearance: none;
    }

    button {
      padding: 6px 12px;
      border: none;
      border-radius: 6px;
      background-color: var(--button-search-color);
      color: white;
      font-weight: bold;
      cursor: pointer;
      transition: background-color 0.2s;
    }

    button.reset-button {
      background-color: var(--sun-honey);
      color: white;
    }

    button.reset-button:hover {
      opacity: 0.9;
    }
  }

  .search-group {
    flex: 1;

    select,
    input {
      background-color: var(--almond-cream);
      border: 1px solid #d6cfc3;
      border-radius: 6px;
      padding: 6px 10px;
      font-size: 14px;
    }

    input {
      flex: 1;
      min-width: 200px;
    }

    select {
      padding-right: 24px;
      background-image: url("data:image/svg+xml,%3Csvg fill='%23d6cfc3' height='18' viewBox='0 0 24 24' width='18' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M7 10l5 5 5-5z'/%3E%3Cpath d='M0 0h24v24H0z' fill='none'/%3E%3C/svg%3E");
      background-repeat: no-repeat;
      background-position: right 6px center;
      background-size: 16px 16px;
      appearance: none;
    }
  }
}

.post-list-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0 10px;

  th, td {
    text-align: center;
    padding: 12px 14px;
    font-size: 14px;
    white-space: nowrap;
  }

  th {
    background-color: var(--card-bg-mint);
    color: #333;
    font-weight: bold;
  }

  td {
    background-color: white;
    border-radius: 8px;
    vertical-align: middle;
    transition: background-color 0.2s ease;
  }
}

.user-tag {
  display: inline-block;
  font-weight: 400;
  border-radius: 12px;
  padding: 2px 8px;
  font-size: 14px;
}
.user-tag--normal { background-color: var(--avocado-frost); color: black; }
.user-tag--google { background-color: var(--melon-icecream); color: black; }
.user-tag--kakao { background-color: #f9e000; color: black; }
.user-tag--suspended { background-color: #ccc; color: #555; }

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 10px;
}

.pagination button {
  padding: 6px 12px;
  border-radius: 6px;
  border: none;
  background-color: var(--avocado-frost);
  color: white;
  font-weight: bold;
  cursor: pointer;
}

.pagination button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.pagination button.active {
  background-color: var(--seafoam-teal);
  color: black;
  font-weight: bold;
}
</style>
