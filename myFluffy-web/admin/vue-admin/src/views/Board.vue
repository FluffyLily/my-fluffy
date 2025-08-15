<template>
  <div class="page-wrapper">
    <div class="page-container">
      <div class="page-content">
        <!-- 게시판 카테고리 필터 영역 -->
        <div class="category-filter-container">
          <h2>게시판 카테고리</h2>
          <button class="btn mb-2" @click="openCreateCategoryModal">
            <font-awesome-icon :icon="['fas', 'plus']" />
          </button>
          <div class="category-list">
            <button class="category-button" 
            :class="allCategory.colorClass" 
            @click="showAllBoards"
            >
              # 전체
            </button>
            <button 
              v-for="(category, index) in categories" 
              :key="index"
              :class="['category-button', category.colorClass]"
              @click="selectCategory(category.boardCategoryId)"
            >
              # {{ category.boardCategoryName }}
            </button>
          </div>
        </div>
        <!-- 게시판 목록 영역 -->
        <div class="menu-container">
          <h2>게시판 목록</h2>
          <div class="menu-actions mb-3">
            <button class="btn" @click="openCreateBoardModal">
              <font-awesome-icon :icon="['fas', 'plus']" style="color: var(--button-add-color)"/>
            </button>
          </div>
          <div class="menu-list">
            <div v-if="boardList.length === 0" class="empty-message">
              아직 게시판이 없습니다.
            </div>
            <div v-else>
              <ul class="board-list">
                <li v-for="board in boardList" :key="board.boardId || board.boardName" 
                    :class="{ active: board.isActive }">
                  <span @click="goToBoardDetail(board)" style="cursor: pointer;">{{ board.boardName }}</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
        <!-- 게시판 정보 영역 -->
        <div class="board-detail-container">
          <div v-if="selectedBoard?.boardId" >
            <h2>{{ selectedBoard.boardName }} 게시판 정보</h2>
            <div class="board-detail-content">
              <p><strong>카테고리 :</strong> {{ selectedBoard.categories?.[0]?.boardCategoryName || '없음' }}</p>
              <p><strong>등록한 관리자 :</strong> {{ selectedBoard.createdByLoginId }}</p>
              <p><strong>등록한 날 :</strong> {{ formatDate(selectedBoard.createdAt) }}</p>
              <p><strong>수정한 관리자 :</strong> {{ selectedBoard.updatedByLoginId }}</p>
              <p><strong>수정한 날 :</strong> {{ formatDate(selectedBoard.updatedAt) }}</p>
              <button class="btn btn-warning me-2" @click="editBoardMenu(selectedBoard)">수정</button>
              <button v-if="canDeleteBoard" class="btn btn-danger"
                @click="confirmDeleteBoardMenu(selectedBoard)">
                삭제
              </button>
            </div>
          </div>
          <div v-else>
            <h2>게시판 정보</h2>
            <div>
              <h5>게시판을 선택하세요.</h5>
            </div>
          </div>
          <div class="post-list-container">
            <button class="btn btn-next mb-3" @click="goToPostManagement">게시글 관리하기</button>
            <div class="post-list-content">
              <h3>최근 게시글들</h3>
              <div v-if="posts && posts.length === 0">게시글이 없습니다.</div>
              <ul v-else>
                <li v-for="post in posts" :key="post.postId" @click="goToPostManagementWithPostId(post.postId)">
                  <strong>{{ post.title }}</strong> - {{ post.createdByName }} | {{ formatDate(post.createdAt) }}
                </li>
              </ul>
            </div>
          </div>
        </div>
        <!-------------------------------------- Modal -------------------------------------->
        <!-- 카테고리 추가 모달 -->
        <div v-if="showCreateCategoryModal" class="modal fade show d-flex justify-content-center align-items-center category-modal" style="display: block;" tabindex="-1" role="dialog">
          <div class="modal-dialog modal-dialog-centered custom-modal-lg" role="document">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title category-title">게시판 카테고리 등록</h5>
                <button type="button" class="btn-close" @click="closeCreateCategoryModal"></button>
              </div>
              <div class="modal-body">
                <label class="form-label">카테고리 이름</label>
                <input v-model="newCategory.boardCategoryName" ref="newBoardCategoryInput" placeholder="새로운 카테고리 입력" class="form-control mb-2"/>
                <small v-if="categoryNameError" class="text-danger">{{ categoryNameError }}</small>
              </div>
              <div class="modal-footer">
                <button class="btn btn-primary" @click="createBoardCategory" :disabled="!isCategoryValid">등록하기</button>
                <button class="btn btn-secondary" @click="closeCreateCategoryModal">닫기</button>
              </div>
            </div>
          </div>
        </div>
        <!-- 게시판 추가 모달 -->
        <div v-if="showCreateBoardModal" class="modal fade show d-flex justify-content-center align-items-center board-modal" style="display: block;" tabindex="-1" role="dialog">
          <div class="modal-dialog modal-dialog-centered custom-modal-lg" role="document">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title board-title">게시판 등록</h5>
                <button type="button" class="btn-close" @click="closeCreateBoardModal"></button>
              </div>
              <div class="modal-body">
                <label class="form-label">게시판 이름</label>
                <input v-model="newBoard.boardName" ref="newBoardNameInput" placeholder="새로운 게시판 입력" class="form-control mb-2" />
                <label class="form-label">카테고리</label>
                <select v-model="newBoard.boardCategoryId" class="form-select mb-2">
                  <option disabled value="">카테고리 선택</option>
                  <option v-for="category in categories" :key="category.boardCategoryId" :value="category.boardCategoryId">
                    {{ category.boardCategoryName }}
                  </option>
                </select>
                <small v-if="boardNameError" class="text-danger">{{ boardNameError }}</small>
              </div>
              <div class="modal-footer">
                <button class="btn btn-primary" @click="createBoard" :disabled="!isBoardValid">등록하기</button>
                <button class="btn btn-secondary" @click="closeCreateBoardModal">닫기</button>
              </div>
            </div>
          </div>
        </div>
        <!-- 게시판 수정 모달 -->
        <div v-if="showEditBoardModal" class="modal fade show d-flex justify-content-center align-items-center edit-modal" style="display: block;" tabindex="-1" role="dialog">
          <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title edit-title">게시판 수정</h5>
                <button type="button" class="btn-close" @click="showEditBoardModal = false"></button>
              </div>
              <div class="modal-body">
                  <label class="form-label d-block">게시판 이름</label>
                  <input v-model="editBoard.boardName" placeholder="게시판 이름을 입력하세요" class="form-control mb-2"/>
                  <label class="form-label d-block">카테고리</label>
                  <select v-model="editBoard.boardCategoryId" class="form-select mb-2">
                    <option disabled value="">카테고리 선택</option>
                    <option v-for="category in categories" :key="category.boardCategoryId" :value="category.boardCategoryId">
                      {{ category.boardCategoryName }}
                    </option>
                  </select>
                  <small v-if="boardEditError" class="text-danger">{{ boardEditError }}</small>
              </div>
              <div class="modal-footer">
                <button class="btn btn-warning" @click="updateBoard">수정</button>
                <button class="btn btn-secondary" @click="showEditBoardModal = false">닫기</button>
              </div>
            </div>
          </div>
        </div>
        <!-- 게시판 삭제 모달 -->
        <div v-if="showDeleteBoardModal" class="modal fade show d-flex justify-content-center align-items-center delete-modal" style="display: block;" tabindex="-1" role="dialog">
          <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title delete-title">게시판 삭제</h5>
                <button type="button" class="btn-close" @click="closeDeleteModal"></button>
              </div>
              <div class="modal-body">
                <div class="mb-3">
                  <div class="confirm-text">
                    <strong>"{{ selectedBoard.boardName }}" 게시판을 정말 삭제하시겠습니까?</strong>
                    <div class="note">(게시글이 있으면 삭제할 수 없습니다.)</div>
                    <div class="sub">
                      <span class="emoji" aria-hidden="true">⚠️</span>
                      <span class="warn-text">이 작업은 되돌릴 수 없습니다.</span>
                    </div>
                  </div>
                  <label class="form-label d-block mt-3">관리자 비밀번호 확인</label>
                  <div class="password-input">
                    <input
                      type="password"
                      v-model="deletePassword"
                      ref="deletePasswordInput"
                      class="form-control"
                      @keyup.enter="deletePassword && deleteBoard()"
                    />
                  </div>
                  <div v-if="deleteError" class="text-danger mt-2">{{ deleteError }}</div>
                </div>
              </div>
              <div class="modal-footer">
                <button class="btn btn-danger" @click="deleteBoard">삭제</button>
                <button class="btn btn-secondary" @click="closeDeleteModal">닫기</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue';
import { format } from 'date-fns';
import apiClient from '../api/axios.js';
import { useAuthStore } from '../stores/auth.js';
import { hasAnyRole } from '../util/roleUtils.js';
import { useRouter } from 'vue-router';
import { useToast } from 'vue-toastification';

// 기본 셋업
const router = useRouter();
const authStore = useAuthStore();
const toast = useToast();
const userId = authStore.userId;

// 게시판 & 카테고리 목록
const boardList = ref([]);
const categories = ref([]);
const filteredBoards = ref([]);
const allCategory = reactive({
  name: '전체',
  colorClass: ''
});

// 게시글 목록
const posts = ref([]);
const searchCondition = reactive({
  boardId: null,
  postCategory: '',
  searchKeyword: '',
  searchType: null,
  sort: 'recent',
  offset: 0,
  limit: 5
})

// 게시판 & 카테고리 생성
const newCategory = ref({
  boardCategoryName: '',
  createdBy: userId,
  createdAt: new Date().toISOString(),
  updatedBy: userId,
  updatedAt: new Date().toISOString()
});
const newBoard = ref({
  boardName: '',
  boardCategoryId: '',
  createdBy: userId,
  createdAt: new Date().toISOString(),
  updatedBy: userId,
  updatedAt: new Date().toISOString()
});
const newBoardCategoryInput = ref(null);
const newBoardNameInput = ref(null);
const showCreateCategoryModal = ref(false);
const showCreateBoardModal = ref(false);
const categoryNameError = ref(null);
const boardNameError = ref(null);

// 선택 상태 구분
const selectedBoard = reactive({
  boardId: null,
  boardName: '',
  createdAt: '',
  createdBy: null,
  createdByLoginId: '',
  updatedAt: '',
  updatedBy: null,
  updatedByLoginId: '',
  categories: []
});
const selectedCategory = ref(null);
const selectedBoardId = ref(null);

// 게시판 수정 관련
const editBoard = ref({
  boardId: null,
  boardName: '',
  boardCategoryId: null,
  boardCategoryName: '',
  updatedBy: authStore.userId,
  updatedAt: new Date().toISOString()
});
const boardEditError = ref(null);
const showEditBoardModal = ref(false);
const isBoardUpdated = ref(false);

// 게시판 삭제 관련
const deletePassword = ref('');
const deleteError = ref('');
const deletePasswordInput = ref(null);
const showDeleteBoardModal = ref(false);

const canDeleteBoard = computed(() => 
  hasAnyRole(authStore.roleId, ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN'])
);

// 유효성 검사
const isCategoryValid = computed(() => newCategory.value.boardCategoryName.trim().length > 0);
const isBoardValid = computed(() =>
  newBoard.value.boardName.trim().length > 0 &&
  newBoard.value.boardCategoryId && newBoard.value.boardCategoryId.toString().trim().length > 0
);

// 유틸 & 공통
const resetCreateForm = () => {
  newBoard.value = {
    boardName: '',
    boardCategoryId: '',
    createdBy: userId,
    createdAt: new Date().toISOString(),
    updatedBy: userId,
    updatedAt: new Date().toISOString()
  };
  newCategory.value = {
    boardCategoryName: '',
    createdBy: userId,
    createdAt: new Date().toISOString(),
    updatedBy: userId,
    updatedAt: new Date().toISOString()
  };
  categoryNameError.value = null;
  boardNameError.value = null;
};

const openCreateCategoryModal = async() => {
  showCreateCategoryModal.value = true;
  await nextTick();
  newBoardCategoryInput.value?.focus();
}
const closeCreateCategoryModal = () => {
  showCreateCategoryModal.value = false;
  resetCreateForm();
};
const openCreateBoardModal = async() => {
  showCreateBoardModal.value = true;
  await nextTick();
  newBoardNameInput.value?.focus();
};
const closeCreateBoardModal = () => {
  showCreateBoardModal.value = false;
  resetCreateForm();
};

const assignRandomColorClass = (category) => {
  const colorClasses = ['color-pink', 'color-avocado', 'color-violet', 'color-seafoam', 'color-honey'];
  category.colorClass = colorClasses[Math.floor(Math.random() * colorClasses.length)];
};
assignRandomColorClass(allCategory);

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  return format(new Date(dateString), 'yyyy-MM-dd HH:mm:ss');
};

// 게시판 목록 및 필터링
// 게시판 카테고리 목록 가져오기
const selectAllCategories = async () => {
  try {
    const response = await apiClient.get('/board/category');
    categories.value = response.data.map(category => {
      assignRandomColorClass(category);
      return category;
    });
  } catch (error) {
    console.error('게시판 카테고리 불러오기 실패:', error);
  }
};

// 게시판 목록 가져오기
const getBoardList = async () => {
  try {
    const response = await apiClient.get('/board/list');
    boardList.value = response.data;
    filteredBoards.value = boardList.value;
    updateActiveBoard();
  } catch (error) {
    console.error('게시판 목록 불러오기 실패:', error);
    boardList.value = [];
  }
};

// 카테고리 선택 시 게시판 목록 필터링
const selectCategory = async (boardCategoryId) => {
  selectedCategory.value = boardCategoryId;
  try {
    const response = await apiClient.get(`/board/list/${boardCategoryId}`);
    boardList.value = response.data;
    updateActiveBoard();
  } catch (error) {
    console.error('게시판 카테고리 필터링 실패:', error);
  }
};

const showAllBoards = async () => {
  selectedCategory.value = null;
  await getBoardList();
};

// 게시판 상세 및 선택
// 게시판 세부정보 가져오기
const goToBoardDetail = async (board) => {
  if (!board.boardId) return;
  if (selectedBoard.boardId === board.boardId) {
    Object.assign(selectedBoard, {
      boardId: null,
      boardName: '',
      createdAt: '',
      createdBy: null,
      createdByLoginId: '',
      updatedAt: '',
      updatedBy: null,
      updatedByLoginId: '',
      categories: []
    });

    selectedBoardId.value = null;
    searchCondition.boardId = null;
    await fetchPosts();
    updateActiveBoard();
    return;
  }
  try {
    const response = await apiClient.get(`/board/detail/${board.boardId}`);
    if (response.data) {
      selectedBoard.boardId = null;
      await nextTick();

      Object.assign(selectedBoard, response.data);
      selectedBoardId.value = board.boardId;

      searchCondition.boardId = board.boardId;
      await fetchPosts();
    }
    updateActiveBoard();
  } catch (error) {
    console.error('게시판 세부 정보 가져오기 실패:', error);
  }
};

// 선택한 게시판 활성화하기
const updateActiveBoard = () => {
  boardList.value.forEach(board => {
    board.isActive = selectedBoardId.value !== null && board.boardId === selectedBoardId.value;
  });
};

// 게시판 상세 정보 가져오기
const fetchBoardDetail = async (boardId) => {
  if (!boardId) {
    console.warn("boardId가 없습니다. URL을 확인하세요.");
    return;
  }
  try {
    const response = await apiClient.get(`/board/detail/${boardId}`);

    if (response.data) {
      Object.assign(selectedBoard, response.data);
    }
  } catch (error) {
    console.error('게시판 세부 정보 가져오기 실패:', error);
    deleteError.value = '게시판 정보를 불러오는 데 실패했습니다. 다시 시도해 주세요.';
  }
};

// 게시판 생성 및 수정
// 게시판 카테고리 추가
const createBoardCategory = async () => {
  if (!newCategory.value.boardCategoryName.trim()) {
    categoryNameError.value = '카테고리 이름을 입력하세요.';
    return;
  }
  if (confirm("새로운 게시판 카테고리를 생성하시겠습니까?")) {
    try {
      await apiClient.post('/board/category', newCategory.value);
      await selectAllCategories();

      newCategory.value.boardCategoryName = '';
      showCreateCategoryModal.value = false;
    } catch (error) {
      console.error('카테고리 추가 실패:', error);
    }
  }
};

// 게시판 추가하기
const createBoard = async () => {
  if (!newBoard.value.boardName.trim()) {
    boardNameError.value = '게시판 이름을 입력하세요.';
    return;
  }
  if (confirm("새로운 게시판을 생성하시겠습니까?")) {
    newBoard.value.createdAt = new Date().toISOString();
    newBoard.value.updatedAt = new Date().toISOString();
    try {
      await apiClient.post('/board/create', newBoard.value);
      await getBoardList();
      newBoard.value.boardName = '';
      newBoard.value.boardCategoryId = null;
      showCreateBoardModal.value = false;
      toast.success("새로운 게시판이 추가되었습니다.");
    } catch (error) {
      console.error('게시판 추가 실패:', error);
    }
  }
};

// 게시판 세부정보 수정 데이터, 모달 세팅
const editBoardMenu = (selectedBoard) => {
  editBoard.value = { 
    boardId: selectedBoard.boardId,
    boardName: selectedBoard.boardName,
    updatedBy: authStore.userId,
    boardCategoryId: selectedBoard.categories.length ? selectedBoard.categories[0].boardCategoryId : '',
    boardCategoryName: selectedBoard.categories.length ? selectedBoard.categories[0].boardCategoryName : ''
  };

  boardEditError.value = null;
  showEditBoardModal.value = true;

};

// 게시판 수정 사항 업데이트 요청
const updateBoard = async () => {
  if (!editBoard.value.boardName.trim()) {
    boardEditError.value = '게시판의 이름을 입력하세요.';
    return;
  }
  if (confirm("게시판을 수정하시겠습니까?")) {
    try {
      await apiClient.put(`/board/${editBoard.value.boardId}`, {
        boardName: editBoard.value.boardName,
        boardCategoryId: editBoard.value.boardCategoryId,
        updatedBy: editBoard.value.updatedBy
      });

      showEditBoardModal.value = false;
      await getBoardList();
      isBoardUpdated.value = true;
      
    } catch (error) {
      console.error('게시판 수정 실패:', error);
    }
  }
};

// 게시글 목록 가져오기
const fetchPosts = async () => {
  try {
    const response = await apiClient.post('/post/list', searchCondition);
    posts.value = response.data.posts;
  } catch (error) {
    console.error('게시글 목록 조회 실패:', error);
    posts.value = [];
  }
};

// 게시글 관리 페이지로 가기
const goToPostManagement = () => {
  if (selectedBoard?.boardId) {
    router.push({
      name: 'PostManagement',
      query: { boardId: selectedBoard?.boardId }
    });
  } else {
    router.push({ name: 'PostManagement' });
  }
}

// 게시글 선택 + 게시글 관리 페이지로 가기
const goToPostManagementWithPostId = (postId) => {
  router.push({
    name: 'PostManagement',
    query: {
      boardId: selectedBoard?.boardId,
      focusedPostId: postId
    }
  });
}

const confirmDeleteBoardMenu = async (board) => {
  selectedBoardId.value = board.boardId;
  showDeleteBoardModal.value = true;
  await nextTick();
  deletePasswordInput.value?.focus();
};
  
const closeDeleteModal = () => {
  showDeleteBoardModal.value = false;
  deletePassword.value = '';
  deleteError.value = '';
};

// 게시판 삭제하기
const deleteBoard = async () => {
  if (!selectedBoardId.value) {
    console.error('삭제할 게시판이 선택되지 않았습니다.');
    return;
  }
  if (!deletePassword.value.trim()) {
    deleteError.value = '관리자 비밀번호를 입력하세요.';
    return;
  }
  try {
    await apiClient.delete(`/board/${selectedBoardId.value}`, {
      data: { password: deletePassword.value }
    });

    showDeleteBoardModal.value = false;
    deletePassword.value = '';
    selectedBoardId.value = null;
    await getBoardList();
    Object.assign(selectedBoard, {
      boardId: null,
      boardName: '',
      createdAt: '',
      createdBy: null,
      createdByLoginId: '',
      updatedAt: '',
      updatedBy: null,
      updatedByLoginId: '',
      categories: []
    });
    searchCondition.boardId = null;
    updateActiveBoard();
    toast.success("게시판이 삭제되었습니다.");
    await fetchPosts();

  } catch (error) {
    const status = error?.response?.status;
    const msg = error?.response?.data?.message;

    if (status === 400) {
      if (msg && msg.includes('비밀번호')) {
        deletePassword.value = '';
        await nextTick();
        deleteError.value = '비밀번호가 일치하지 않습니다.';
        deletePasswordInput.value?.focus(); 
      } else {
        deleteError.value = msg || '요청이 올바르지 않습니다.';
      }
      return;
    }
    // 게시글이 있으면 게시판 삭제 불가
    if (status === 409) {
      deleteError.value = msg || '해당 게시판에 게시글이 남아 있어 삭제할 수 없습니다.';
      return;
    }

    if (status === 404) {
      deleteError.value = '게시판을 찾을 수 없습니다.';
    } else if ([401, 403].includes(status)) {
      deleteError.value = '권한이 없거나 인증이 만료되었습니다. 다시 로그인 해주세요.';
    } else {
      deleteError.value = '게시판 삭제 중 문제가 발생했습니다. 다시 시도해 주세요.';
    }

    console.error('게시판을 삭제하지 못함: ', error);
  }
};

onMounted(() => {
  selectAllCategories();
  getBoardList();
  fetchPosts();
});

// 게시판 내용 업데이트 감지
watch(isBoardUpdated, async (newValue) => {
  if (newValue) {
    await fetchBoardDetail(editBoard.value.boardId);
    isBoardUpdated.value = false;
  }
});

// 사용자가 비밀번호 입력을 다시 시작하면 에러 메시지 제거
watch(deletePassword, () => {
    deleteError.value = '';
});
</script>

<style lang="scss" scoped>

.color-pink    { background-color: var(--blush-pink); }
.color-avocado { background-color: var(--avocado-frost); }
.color-violet  { background-color: var(--violet-deep); }
.color-seafoam { background-color: var(--seafoam-teal); }
.color-honey   { background-color: var(--sun-honey); }

.page-wrapper {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: var(--background-color);
  min-width: 1200px;

  .page-container {
    display: flex;
    flex-direction: row;
    flex: 1;
    padding: 20px;
    background-color: white;
    overflow-y: auto;
    scroll-behavior: smooth;

    .page-content {
      display: flex;
      flex: 1;
      justify-content: space-between;
      gap: 20px;
      align-items: stretch;
      min-height: 600px;

      .category-filter-container,
      .menu-container,
      .board-detail-container {
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
        padding: 20px;
        background-color: var(--card-bg);
        border-radius: 12px;
        box-shadow: 0 0 10px var(--shadow-color);
      }

      .menu-container {
        flex: 1.5;
        border: 2px solid var(--avocado-frost);

        h2 {
          font-size: 30px;
          font-weight: bold;
          color: var(--avocado-frost);
          margin-bottom: 15px;
        }

        .menu-actions {
          display: flex;
          justify-content: center;
          margin-bottom: 1rem;

          .btn {
            color: var(--button-add-color);
            transition: background-color 0.3s, border-color 0.3s;
          }
        }    

        .board-list {
          list-style-type: none;
          padding: 0;
          overflow-y: auto;
          max-height: 600px;
          li {
            padding: 15px;
            margin-bottom: 10px;
            background-color: var(--card-bg);
            border-radius: 8px;
            cursor: pointer;
            transition: background-color 0.3s, box-shadow 0.3s;
            box-shadow: 0 2px 5px var(--shadow-color);

            &:hover {
              background-color: var(--avocado-frost);
              box-shadow: 0 5px 10px var(--shadow-color);
            }

            &.active {
              background-color: var(--avocado-frost);
              color: white;
              box-shadow: 0 5px 10px var(--shadow-color);
            }
          }
        }

        .menu-list {
          .empty-message {
            text-align: center;
            padding: 2.5rem 0;
            font-style: italic;
            color: var(--black-pearl);
            background-color: var(--melon-icecream);
            border-radius: 8px;
            box-shadow: var(--card-shadow);
          }
        }
      }

      .category-filter-container {
        flex: 1;
        border: 2px solid var(--peach-sherbet);
        box-shadow: 0 4px 10px var(--shadow-color);

        h2 {
          font-size: 30px;
          font-weight: bold;
          color: var(--peach-sherbet);
          margin-bottom: 15px;
        }

        .btn {
          color: var(--button-add-color);
          transition: background-color 0.3s, border-color 0.3s;
        }

        .category-list {
          display: flex;
          flex-direction: column;
          gap: 10px;
          align-items: center;
          overflow-y: auto;
          max-height: 600px;

          .category-button {
            padding: 10px 15px;
            width: 120px;
            border: none;
            border-radius: 8px;
            font-size: 1rem;
            font-weight: bold;
            cursor: pointer;
            transition: background-color 0.3s;

            &:hover {
              opacity: 0.8;
            }
          }
        }
      }

      .board-detail-container {
        flex: 3;
        border: 2px solid var(--purple-moon);

        h2 {
          font-size: 30px;
          font-weight: bold;
          color: var(--purple-moon);
          margin-bottom: 15px;
        }

        .board-detail-content {
          flex: 2.5;
          padding: 20px;
          background-color: var(--card-bg);
          border-radius: 12px;
          box-shadow: 0 0 10px var(--shadow-color);
        }

        .post-list-container {
          margin-top: 1.5rem;
          padding: 15px 20px;
          background-color: var(--card-bg);
          border-radius: 10px;
          box-shadow: 0 0 6px var(--shadow-color);

          .post-list-content {
            h3 {
              font-size: 20px;
              font-weight: bold;
              color: var(--purple-moon);
              margin-bottom: 10px;
            }

            ul {
              list-style: none;
              padding: 0;
              overflow-y: auto;
              max-height: 400px;

              li {
                padding: 12px 15px;
                margin-bottom: 10px;
                background-color: white;
                border-radius: 8px;
                box-shadow: 0 1.5px 4px var(--shadow-color);
                transition: background-color 0.3s, box-shadow 0.3s;

                &:hover {
                  background-color: var(--highlight-yellow);
                  color: white;
                  box-shadow: 0 4px 8px var(--shadow-color);
                }
              }
            }
          }
        }
      }
    }
  }
}

// ===== 버튼 스타일 =====
.btn-warning {
  background-color: var(--button-warning-color);
  border-color: var(--button-warning-color);
  color: white;
  &:hover {
    background-color: var(--button-hover-warning);
    border-color: var(--button-hover-warning);
  }
}
.btn-danger {
  background-color: var(--button-danger-color);
  border-color: var(--button-danger-color);
  color: white;
  &:hover {
    background-color: var(--button-hover-danger);
    border-color: var(--button-hover-danger);
  }
}
.btn-next {
  background-color: var(--violet-deep);
  border-color: var(--violet-deep);
  color: white;
  &:hover {
    background-color: var(--lavender-soft);
    border-color: var(--lavender-soft);
  }
}

.table {
  margin-top: 20px;
}

h2 {
  margin-bottom: 20px;
}

p {
  margin: 5px 0;
}

// ===== Modal 공통 스타일 =====
.custom-modal-lg,
.modal-dialog {
  max-width: 500px !important;
  width: 100%;
}

.modal-header {
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 16px 20px;
  .modal-title {
    font-size: 1.6rem;
    font-weight: 600;
    text-align: center;
    flex: 1;
    margin: 0;
  }
  .category-title {
    color: var(--peach-sherbet);
  }
  .board-title {
    color: var(--avocado-frost);
  }
  .edit-title {
    color: var(--button-warning-color);
  }
  .delete-title {
    color: var(--button-danger-color);
  }
}

.modal-body {
  padding: 20px;
  text-align: center;
  label {
    font-size: 1rem;
    font-weight: 500;
    display: block;
    margin-bottom: 10px;
    text-align: center;
  }
}

.modal-footer {
  display: flex;
  .btn-primary {
    background-color: var(--button-add-color);
    border-color: var(--button-add-color);
    transition: background-color 0.3s, border-color 0.3s;
    &:disabled {
      background-color: var(--button-hover-add);
      cursor: not-allowed;
      filter: brightness(85%);
      pointer-events: none;
    }
    &:hover {
      background-color: var(--button-hover-add);
      border-color: var(--button-hover-add);
    }
  }
  .btn-secondary {
    background-color: var(--button-close-color);
    border-color: var(--button-close-color);
    transition: background-color 0.3s, border-color 0.3s;
    &:hover {
      background-color: var(--button-hover-close);
      border-color: var(--button-hover-close);
    }
  }
}

// ===== 삭제 모달 =====
.delete-modal {
  .confirm-text {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 12px 14px;
    border-radius: 10px;
    color: var(--black-pearl);
    background-color: var(--almond-cream, #fff8d6);
    border: 1px solid rgba(0,0,0,0.05);

    strong { font-size: 1.05rem; }
    .note {
      margin-top: 2px;
      font-size: 0.85rem;
      color: #6c757d;
    }
    .sub {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      font-size: 0.9rem;
      color: #a94442;

      .emoji {
        font-size: 1.1rem;
        line-height: 1;
      }

      .warn-text {
        display: inline-block;
      }
    }
  }

  .password-input {
    max-width: 320px;
    margin: 0 auto;

    .form-control { width: 100%; }
  }
}

</style>