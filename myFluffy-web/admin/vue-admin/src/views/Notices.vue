<template>
  <div class="page-wrapper">
    <div class="page-container">
      <div class="page-content">
        <h1 class="main-title">시스템 공지 관리</h1>
        <div class="filter-section">
          <div class="filter-item">
            <select v-model="searchCondition.sort" @change="fetchNotices" class="instant-filter">
              <option value="recent">최신순</option>
              <option value="old">오래된순</option>
            </select>
          </div>
          <div class="filter-item search-group">
            <input
              type="text"
              v-model="searchCondition.searchKeyword"
              @keyup.enter="fetchNotices"
              placeholder="제목이나 내용 입력"
            />
            <button class="search-button" @click="fetchNotices">검색</button>
            <button class="reset-button" @click="resetSearch">초기화</button>
          </div>
        </div>
    
        <div class="write-button mb-3">
          <button @click="openCreateNotice">공지 작성</button>
        </div>

        <!-- 공지 작성 / 수정 모달 -->
        <div v-if="showCreateNoticeModal" class="modal-overlay">
          <div class="modal-content">
            <div class="modal-header">
              <h2 class="modal-title">{{ newNotice.noticeId ? '공지 수정' : '공지 작성' }}</h2>
              <button class="btn-close" @click="closeCreateNoticeModal"></button>
            </div>
            <div class="modal-body">
              <div class="modal-field">
                <label for="noticeTitle">제목</label>
                <input v-model="newNotice.title" type="text" id="noticeTitle" placeholder="제목을 입력하세요" />
              </div>
              <div class="modal-field">
                <label for="noticeContent">내용</label>
                <textarea v-model="newNotice.content" id="noticeContent" placeholder="내용을 입력하세요"></textarea>
              </div>
              <div class="modal-field">
                <label>
                  <input type="checkbox" v-model="newNotice.isActive" />
                  활성화 여부
                </label>
              </div>
            </div>
            <div class="modal-footer">
              <button class="submit-btn" @click="newNotice.noticeId ? updateNotice() : createNotice()">
                {{ newNotice.noticeId ? '수정하기' : '작성하기' }}
              </button>
              <button class="cancel-btn" @click="closeCreateNoticeModal">
                {{ newNotice.noticeId ? '취소' : '닫기' }}
              </button>
            </div>
          </div>
        </div>
    
        <div class="notice-list-content">
          <div v-if="!notices || notices.length === 0">공지 사항이 없습니다.</div>
          <table class="notice-list-table" v-if="notices && notices.length > 0">
            <thead>
              <tr>
                <th>번호</th>
                <th>작성자</th>
                <th>제목</th>
                <th>등록일</th>
                <th>활성 상태</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(notice, index) in notices" 
              :key="notice.noticeId"
              @click="goToNotice(notice)">
                <td>{{ index + 1 }}</td>
                <td>{{ notice.createdByName }}</td>
                <td>{{ notice.title }}</td>
                <td>{{ formatDate(notice.createdAt) }}</td>
                <td>{{ notice.isActive ? '활성화' : '비활성화' }}</td>
                <td>
                  <button class="update-btn" @click.stop="editNotice(notice)">수정</button>
                  <button class="delete-btn" @click.stop="deleteNotice(notice)">삭제</button>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- 공지 내용 모달 -->
          <div v-if="showNoticeDetailsModal" class="modal-overlay">
            <div class="modal-content modal-content-details">
              <div class="modal-header">
                <h2 class="modal-title">공지 내용</h2>
                <button class="btn-close" @click="closeNoticeModal"></button>
              </div>
              <div class="modal-body details-view">
                <div class="modal-field">
                  <h3>{{ newNotice.title }}</h3>
                  <p class="notice-content">{{ newNotice.content }}</p>
                </div>
              </div>
              <div class="modal-footer">
                <button class="cancel-btn" @click="closeNoticeModal">닫기</button>
              </div>
            </div>
          </div>

          <div v-if="totalCount > 0" class="pagination-bar">
            <button :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">이전</button>
            
            <button 
              v-for="page in visiblePages" 
              :key="page" 
              :class="{ active: currentPage === page }" 
              @click="goToPage(page)">
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
import { ref, reactive, onMounted, computed } from 'vue';
import apiClient from '../api/axios.js';
import { useAuthStore } from '../stores/auth.js';
import { format } from 'date-fns';

const authStore = useAuthStore();
const notices = ref([]);
const totalCount = ref(0);

const searchCondition = reactive({
  sort: 'recent',
  offset: 0,
  limit: 5,
  searchKeyword: ''
});

const currentPage = computed(() => Math.floor(searchCondition.offset / searchCondition.limit) + 1);
const totalPages = computed(() => Math.ceil(totalCount.value / searchCondition.limit));

const goToPage = (page) => {
  const maxPage = totalPages.value;
  if (page < 1 || page > maxPage) return;

  const nextOffset = (page - 1) * searchCondition.limit;
  const maxOffset = Math.max(0, (totalPages.value - 1)) * searchCondition.limit;
  searchCondition.offset = nextOffset > maxOffset ? 0 : nextOffset;

  fetchNotices();
};

const visiblePages = computed(() => {
  const total = totalPages.value;
  const current = currentPage.value;
  const delta = 2;
  const pages = [];

  for (let i = Math.max(1, current - delta); i <= Math.min(total, current + delta); i++) {
    pages.push(i);
  }

  return pages;
});

// 공지 목록 조회
const fetchNotices = async () => {
  
    try {
      const response = await apiClient.post('/notice', searchCondition, {
        headers: { Authorization: `Bearer ${authStore.accessToken}` }
      });
      notices.value = response.data.notices;
      totalCount.value = response.data.totalCount;

      // offset 유효성 검사 및 보정 후 페이지 이동
      const maxOffset = Math.max(0, (totalPages.value - 1)) * searchCondition.limit;
      if (searchCondition.offset > maxOffset) {
        searchCondition.offset = 0;
        goToPage(1);
        return;
      }
    } catch (error) {
      console.error('공지 목록 조회 실패:', error);
      notices.value = [];
      totalCount.value = 0;
    }
};

const formatDate = (date) => {
  return format(new Date(date), 'yyyy-MM-dd HH:mm');
};

const resetSearch = () => {
  searchCondition.searchKeyword = '';
  searchCondition.sort = 'recent';
  fetchNotices();
};

const newNotice = reactive({
  noticeId: null,
  title: '',
  content: '',
  isActive: false,
  createdAt: '',
  createdBy: authStore.userId,
  createdByName: authStore.loginId,
  updatedAt: '',
  updatedBy: authStore.userId,
  updatedByName: authStore.loginId
});

// 공지 내용 조회하기
const showNoticeDetailsModal = ref(false);

const closeNoticeModal = () => {
  showNoticeDetailsModal.value = false;
};

const goToNotice = async (notice) => {
  try {
    const response = await apiClient.get(`/notice/detail/${notice.noticeId}`, {
      headers: { Authorization: `Bearer ${authStore.accessToken}` }
    });
    const noticeDetails = response.data;
    newNotice.title = noticeDetails.title;
    newNotice.content = noticeDetails.content;
    newNotice.isActive = noticeDetails.isActive;
    showNoticeDetailsModal.value = true;
  } catch (error) {
    console.error("공지 세부 내용 조회 실패:", error);
    alert("공지 세부 내용을 불러오는 데 실패했습니다.");
  }
};

// 공지 등록하기
const showCreateNoticeModal = ref(false);

const openCreateNotice = () => {
  newNotice.noticeId = null;
  newNotice.title = '';
  newNotice.content = '';
  newNotice.isActive = true;
  showCreateNoticeModal.value = true;
};

const closeCreateNoticeModal = () => {
  showCreateNoticeModal.value = false;
};

const createNotice = async () => {
  if (!newNotice.title.trim() || !newNotice.content.trim()) {
    alert("제목과 내용을 모두 입력해주세요.");
    return;
  }

  try {
    await apiClient.post('/notice/create', {
      title: newNotice.title,
      content: newNotice.content,
      isActive: newNotice.isActive,
      createdAt: new Date().toISOString(),
      createdBy: newNotice.createdBy,
      createdByName: newNotice.createdByName
    }, {
      headers: { Authorization: `Bearer ${authStore.accessToken}` }
    });

    alert('공지 작성이 완료되었습니다.');
    closeCreateNoticeModal();
    fetchNotices();
  } catch (error) {
    console.error('공지 작성 실패:', error);
    alert('공지 작성에 실패했습니다.');
  }
};

// 공지 수정 모달 열기
const editNotice = (notice) => {
  newNotice.noticeId = notice.noticeId;
  newNotice.title = notice.title;
  newNotice.content = notice.content;
  newNotice.isActive = notice.isActive;
  showCreateNoticeModal.value = true;
};

// 공지 수정하기
const updateNotice = async () => {
  if (!newNotice.title.trim() || !newNotice.content.trim()) {
    alert("제목과 내용을 모두 입력해주세요.");
    return;
  }

  try {
    await apiClient.put(`/notice/${newNotice.noticeId}`, {
      title: newNotice.title,
      content: newNotice.content,
      isActive: newNotice.isActive,
      updatedAt: new Date().toISOString(),
      updatedBy: newNotice.updatedBy,
      updatedByName: newNotice.updatedByName
    }, {
      headers: { Authorization: `Bearer ${authStore.accessToken}` }
    });

    alert("공지 수정이 완료되었습니다.");
    closeCreateNoticeModal();
    fetchNotices();
  } catch (error) {
    console.error("공지 수정 실패:", error);
    alert("공지 수정에 실패했습니다.");
  }
};

// 공지 삭제하기
const deleteNotice = async (notice) => {
  if (confirm(`공지 "${notice.title}"을(를) 삭제하시겠습니까?`)) {
    try {
      await apiClient.delete(`/notice/${notice.noticeId}`, {
        params: {
          deleterId: authStore.loginId,
          title: notice.title
        },
        headers: { Authorization: `Bearer ${authStore.accessToken}` }
      });
      await fetchNotices();
      alert('공지 삭제가 완료되었습니다.');
    } catch (error) {
      console.error('공지 삭제 실패:', error);
      alert('공지 삭제에 실패했습니다.');
    }
  }
};

onMounted(() => {
  fetchNotices();
});
</script>

<style scoped lang="scss">
.page-wrapper {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f8f9fa;
  min-width: 1200px;

  .page-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 20px;
    background-color: white;
    overflow-y: auto;
    scroll-behavior: smooth;

    .page-content {
      flex: 1;
      background-color: white;
      padding: 20px;
      border-radius: 10px;
      box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);

      .main-title {
        font-size: 2rem;
        font-weight: bold;
        color: var(--sun-honey);
        margin: 14px 0 30px 0;
      }
    }
  }
}

// 필터 섹션
.filter-section {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
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

    input[type="text"] {
      width: 400px;
      max-width: 100%;
      background-color: var(--almond-cream);
      outline: none;
      transition: border 0.2s;

      &:focus {
        border-color: var(--button-search-color);
      }
    }

    select.instant-filter {
      background-color: var(--melon-icecream);
      border: 1px solid var(--avocado-frost);
      background-image: url("data:image/svg+xml,%3Csvg fill='%23cbd690' height='18' viewBox='0 0 24 24' width='18' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M7 10l5 5 5-5z'/%3E%3Cpath d='M0 0h24v24H0z' fill='none'/%3E%3C/svg%3E");
      background-repeat: no-repeat;
      background-position: right 12px center;
      background-size: 16px 16px;
      appearance: none;
      padding-right: 28px; /* 오른쪽 여백 추가 */
      min-width: 80px; /* 좌우 길이 확장 */
    }
  }

  button {
    padding: 6px 12px;
    border: none;
    border-radius: 6px;
    color: white;
    font-weight: bold;
    cursor: pointer;
    transition: background-color 0.2s;

    &.search-button {
      background-color: var(--button-search-color);
    }

    &.reset-button {
      background-color: var(--sun-honey);
    }
  }
}

// 공지 작성 버튼
.write-button {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;

  button {
    padding: 6px 12px;
    background-color: var(--button-add-color);
    color: white;
    border: none;
    border-radius: 6px;
    font-weight: bold;
    cursor: pointer;
  }
}

// 공지 목록 테이블
.notice-list-table {
  width: 100%;
  table-layout: fixed;
  border-collapse: collapse;
  margin-top: 20px;

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
    font-size: 15px;
    font-weight: 500;
    color: #333;
  }

  // 번호 열
  th:nth-child(1),
  td:nth-child(1) {
    width: 10%;
  }

  // 작성자 열
  th:nth-child(2),
  td:nth-child(2) {
    width: 15%;
  }

  // 제목 열
  th:nth-child(3),
  td:nth-child(3) {
    width: 35%;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  // 등록일
  th:nth-child(4),
  td:nth-child(4) {
    width: 15%;
  }

  // 활성화 여부
  th:nth-child(5),
  td:nth-child(5) {
    width: 10%;
  }

  // 관리
  th:nth-child(6),
  td:nth-child(6) {
    width: 15%;
  }

  tr {
    cursor: pointer;
  }

  button {
    padding: 6px 10px;
    border: none;
    border-radius: 6px;
    color: white;
    font-weight: bold;
    cursor: pointer;
    margin-right: 6px;

    &.update-btn {
      background-color: var(--button-warning-color);
    }

    &.delete-btn {
      background-color: var(--button-danger-color);
    }
  }
}

// 모달 스타일
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 10px;
  width: 500px;
  max-width: 90%;
  padding: 20px;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.3);

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    border-bottom: 1px solid #ddd;

    .modal-title {
      font-size: 1.8rem;
      font-weight: 600;
      color: var(--sun-honey);
      flex: 1;
      text-align: center;
    }
  }

  .modal-body {
    padding: 16px 0;
    display: flex;
    flex-direction: column;
    gap: 12px;

    .modal-field {
      display: flex;
      flex-direction: column;
      gap: 6px;

      label {
        font-weight: 500;
        font-size: 14px;
      }

      input,
      textarea {
        padding: 10px;
        border: 1px solid #ddd;
        border-radius: 6px;
        font-size: 14px;
        background-color: white;
        outline: none;
        transition: border 0.2s;

        &:focus {
          border-color: var(--button-search-color);
        }
      }

      textarea {
        resize: vertical;
        min-height: 200px;
      }
    }
  }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding: 12px 20px;
    gap: 10px;
    border-top: 1px solid #ddd;

    button {
      padding: 8px 16px;
      border: none;
      border-radius: 6px;
      font-weight: bold;
      cursor: pointer;
      transition: background-color 0.2s;
    }

    .submit-btn {
      background-color: var(--button-add-color);
      color: white;
    }

    .cancel-btn {
      background-color: var(--button-close-color);
      color: white;
    }

    button:hover {
      opacity: 0.9;
    }
  }
}

.modal-content-details {
  max-width: 600px;
  padding: 20px;
}

.modal-body.details-view {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  gap: 12px;
  max-height: 70vh;
  overflow-y: auto;
  padding: 40px 24px; /* 상단, 하단, 좌우 여백 */
}

.modal-field h3 {
  font-size: 1.3rem;
  font-weight: bold;
  color: var(--text-color);
  margin-bottom: 10px;
  text-align: left;
}

.notice-content {
  font-size: 1rem;
  color: var(--text-color);
  white-space: pre-wrap;
  line-height: 1.5;
  max-height: 60vh;
  overflow-y: auto;
  text-align: left;
}

// 페이징 바
.pagination-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 20px;

  button {
    padding: 6px 12px;
    border: 1px solid #ddd;
    border-radius: 6px;
    background-color: var(--button-search-color);
    color: white;
    font-weight: bold;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: var(--button-add-color);
    }
    &.active {
      background-color: var(--button-add-color);
      cursor: default;
    }
    &:disabled {
      background-color: #ccc;
      cursor: not-allowed;
      opacity: 0.6;
    }
  }
}

</style>