<template>
  <div class="page-wrapper">
    <div class="page-container">
      <div class="page-content">
        <h1 class="main-title">게시글 관리</h1>
        <div class="filter-section">
          <div class="filter-item">
            <select v-model="searchCondition.boardId" @change="fetchPosts" class="instant-filter">
              <option :value="null">전체 게시판</option>
              <option v-for="board in boards" :key="board.boardId" :value="board.boardId">
                {{ board.boardName }}
              </option>
            </select>
          </div>
      
          <div class="filter-item">
            <select v-model="searchCondition.sort" @change="fetchPosts" class="instant-filter">
              <option value="recent">최신순</option>
              <option value="old">오래된순</option>
            </select>
          </div>
      
          <div class="filter-item search-group">
            <select v-model="searchCondition.searchType">
              <option :value="null" disabled selected>검색 조건</option>
              <option value="titleContent">제목+내용</option>
              <option value="title">제목</option>
              <option value="content">내용</option>
              <option value="authorName">작성자</option>
              <option value="postCategory">태그</option>
            </select>
            <input
              type="text"
              v-model="searchCondition.searchKeyword"
              @keyup.enter="fetchPosts"
              placeholder="검색어 입력"
            />
            <div class="checkbox-wrapper">
              <input
                type="checkbox"
                v-model="searchCondition.isVisible"
              />
              <label>비노출 제외</label>
            </div>
            <button @click="fetchPosts">검색</button>
            <button class="reset-button" @click="resetSearch">초기화</button>
          </div>
        </div>

        <div class="write-button mb-3">
          <button @click="goToWritePost">글쓰기</button>
        </div>

        <div class="post-list-content">
          <div v-if="posts && posts.length === 0">게시글이 없습니다.</div>
          <table class="post-list-table" v-if="posts && posts.length > 0">
            <colgroup>
              <col style="width: 10%">
              <col style="width: 20%">
              <col style="width: 35%">
              <col style="width: 10%">
              <col style="width: 15%">
              <col style="width: 10%">
            </colgroup>
            <thead>
              <tr>
                <th>게시판</th>
                <th>태그</th>
                <th>제목</th>
                <th>작성자</th>
                <th>등록일</th>
                <th>노출 상태</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="post in posts"
                :key="post.postId"
                @click="goToPost(post)"
                :class="{ focused: focusedId === post.postId }"
              >
                <td><span class="board-tag" :class="getBoardColorClass(post.boardName)">{{ post.boardName }}</span></td>
                <td>
                  <template v-if="post.postCategoryString">
                    <span
                      v-for="(tag, index) in post.postCategoryString.split(',')"
                      :key="index"
                      class="category-tag"
                    >#{{ tag.trim() }}</span>
                  </template>
                </td>
                <td class="title-cell">{{ post.title }}</td>
                <td>{{ post.createdByName }}</td>
                <td>{{ formatDate(post.createdAt) }}</td>
                <td>{{ post.isVisible ? '노출' : '비노출' }}</td>
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
import { ref, onMounted, reactive, computed } from 'vue';
import { format } from 'date-fns';
import apiClient from '../api/axios.js';
import { useRouter } from 'vue-router';
import { useRoute } from 'vue-router';
import { onBeforeRouteUpdate } from 'vue-router';

const route = useRoute();
const router = useRouter();
const boards = ref([]);
const posts = ref([]);
const totalCount = ref(0);
const focusedId = ref(
  route.query.focusedPostId ? Number(route.query.focusedPostId) : null
);
// 게시글 필터 + 검색 조건
const searchCondition = reactive({
  boardId: route.query.boardId ? Number(route.query.boardId) : null,
  offset: route.query.offset ? Number(route.query.offset) : 0,
  searchKeyword: route.query.searchKeyword || '',
  searchType: route.query.searchType || null,
  sort: route.query.sort || 'recent',
  isVisible: route.query.isVisible === 'true',
  limit: 10
});
// 게시판 네임택 색상 동적 매핑
const boardColorMap = {};
const boardColors = [
  'board-tag--coral',
  'board-tag--mintgray',
  'board-tag--gold',
  'board-tag--salmon',
  'board-tag--caramel',
  'board-tag--violet',
  'board-tag--blush',
  'board-tag--honey',
  'board-tag--avocado',
  'board-tag--seafoam'
];
let boardColorIndex = 0;

const currentPage = computed(() => Math.floor(searchCondition.offset / searchCondition.limit) + 1);
const totalPages = computed(() => Math.ceil(totalCount.value / searchCondition.limit));
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

const formatDate = (date) => {
  return format(new Date(date), 'yyyy-MM-dd HH:mm');
};

const fetchBoards = async () => {
  try {
    const response = await apiClient.get('/board/list');
    boards.value = response.data;
  } catch (e) {
    console.error('게시판 목록 불러오기 실패:', e);
  }
};

const fetchPosts = async () => {
  try {
    const response = await apiClient.post(
      '/post/list',
      {
        ...searchCondition,
        isVisible: searchCondition.isVisible ? true : null
      }
    );
    posts.value = response.data.posts.map(post => {
      const board = boards.value.find(b => b.boardId === post.boardId);
      return {
        ...post,
        boardName: board ? board.boardName : '알 수 없음'
      };
    });
    totalCount.value = response.data.totalCount;

    const maxOffset = Math.max(0, (totalPages.value - 1)) * searchCondition.limit;
    if (searchCondition.offset > maxOffset) {
      searchCondition.offset = 0;
      goToPage(1);
      return;
    }

    router.replace({
      query: {
        ...route.query,
        offset: searchCondition.offset,
        searchKeyword: searchCondition.searchKeyword || undefined,
        searchType: searchCondition.searchType || undefined,
        isVisible: searchCondition.isVisible ? 'true' : undefined,
        boardId: searchCondition.boardId || undefined,
        sort: searchCondition.sort || undefined,
      }
    });
  } catch (error) {
    console.error('게시글 목록 조회 실패:', error);
    posts.value = [];
  }
};

// 페이지 이동
const goToPage = (page) => {
  const safePage = Math.min(page, totalPages.value);
  if (safePage < 1) return;
  searchCondition.offset = (safePage - 1) * searchCondition.limit;
  fetchPosts();
};

// 게시글 상세 내용 보기
const goToPost = (post) => {
  const board = boards.value.find(b => b.boardId === post.boardId);
  const query = {
    offset: searchCondition.offset,
    searchKeyword: searchCondition.searchKeyword,
    searchType: searchCondition.searchType,
    isVisible: searchCondition.isVisible ? 'true' : 'false',
  };

  if (searchCondition.boardId) {
    query.boardId = searchCondition.boardId;
    query.boardName = board ? board.boardName : null;
    query.filteredByBoard = 'true';
  }

  router.push({
    name: 'PostDetail',
    params: {
      postId: post.postId
    },
    query
  });
};

// 게시글 필터 + 검색 입력 초기화
const resetSearch = () => {
  searchCondition.boardId = null;
  searchCondition.searchKeyword = '';
  searchCondition.searchType = null;
  searchCondition.isVisible = false;
  searchCondition.sort = 'recent';
  searchCondition.offset = 0;
  focusedId.value = null;
  fetchPosts();
};

// 게시글 작성하기
const goToWritePost = () => {
  if (searchCondition?.boardId) {
    const board = boards.value.find(b => b.boardId === searchCondition.boardId);
    router.push({
      name: 'WritePost',
      query: {
        boardId: searchCondition.boardId,
        boardName: board ? board.boardName : null,
        filteredByBoard: 'true'
      }
    });
  } else {
    router.push({ name: 'WritePost' });
  }
};

// 게시판 이름 색깔 입히기
const getBoardColorClass = (boardName) => {
  if (!boardColorMap[boardName]) {
    const color = boardColors[boardColorIndex % boardColors.length];
    boardColorMap[boardName] = color;
    boardColorIndex++;
  }
  return boardColorMap[boardName];
};

onMounted(async () => {
  await fetchBoards();
  if (route.query.filteredByBoard === 'true' && !route.query.boardId) {
    searchCondition.boardId = null;
  }
  await fetchPosts();
});

// 쿼리에서 필터 + 검색 조건 가져오기
onBeforeRouteUpdate((to, from, next) => {
  if (to.query.offset !== undefined) {
    searchCondition.offset = Number(to.query.offset);
  }
  if (to.query.searchKeyword !== undefined) {
    searchCondition.searchKeyword = to.query.searchKeyword;
  }
  if (to.query.searchType !== undefined) {
    searchCondition.searchType = to.query.searchType;
  }
  if (to.query.isVisible !== undefined) {
    searchCondition.isVisible = to.query.isVisible === 'true';
  }
  if (to.query.boardId !== undefined) {
    searchCondition.boardId = to.query.boardId ? Number(to.query.boardId) : null;
  }
  if (to.query.sort !== undefined) {
    searchCondition.sort = to.query.sort;
  }
  next();
});

</script>
<style lang="scss" scoped>
.page-wrapper {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f8f9fa;

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
            white-space: nowrap;
          }

          .reset-button {
            background-color: var(--sun-honey);
            color: white;
            &:hover {
              opacity: 0.9;
            }
          }

          label {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 14px;
            font-weight: 500;
            color: #333;
          }

          input[type="checkbox"] {
            width: 16px;
            height: 16px;
            accent-color: var(--primary-color);
            cursor: pointer;
            border: 1px solid #ccc;
            border-radius: 4px;
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

      .checkbox-wrapper {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        width: auto;
        input[type="checkbox"] {
          width: 16px !important;
          height: 16px !important;
          accent-color: var(--primary-color) !important;
          cursor: pointer !important;
          border: 1px solid #ccc !important;
          border-radius: 4px !important;
          margin-right: 4px;
          flex-shrink: 0;
          min-width: auto;
        }
      }

      .write-button {
        display: flex;
        justify-content: flex-end;
        button {
          padding: 8px 14px;
          border: none;
          border-radius: 6px;
          background-color: var(--button-add-color);
          color: white;
          font-weight: bold;
          cursor: pointer;
        }
      }

      .post-list-content {
        ul {
          list-style: none;
          padding: 0;
        }
        .post-list-table {
          table-layout: fixed;
          width: 100%;
          border-collapse: separate;
          border-spacing: 0 10px;

          th,
          td {
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
            &.title-cell {
              font-size: 15px;
              font-weight: 500;
              color: #333;
              overflow: hidden;
              text-overflow: ellipsis;
              text-align: left;
              max-width: 200px;
              white-space: normal;
              word-break: break-word;
            }
          }

          td:last-child {
            max-width: 100px;
          }

          tr {
            cursor: pointer;
          }

          .category-tag,
          .board-tag {
            display: inline-block;
            font-weight: 400;
            border-radius: 12px;
            padding: 2px 8px;
            font-size: 14px;
            max-width: 100px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          .category-tag {
            background-color: var(--rose-dust);
            color: black;
            border-radius: 14px;
            margin-right: 6px;
          }

          tr.focused {
            outline: 2px solid var(--highlight-border-yellow);
            outline-offset: -2px;
          }
        }
      }

      .pagination {
        display: flex;
        justify-content: center;
        align-items: center;
        margin-top: 20px;
        gap: 10px;

        button {
          padding: 6px 12px;
          border-radius: 6px;
          border: none;
          background-color: var(--avocado-frost);
          color: white;
          font-weight: bold;
          cursor: pointer;
          &.active {
            background-color: var(--seafoam-teal);
            color: black;
            font-weight: bold;
          }
          &:disabled {
            background-color: #ccc;
            cursor: not-allowed;
          }
        }
      }
    }
  }
}

.board-tag--coral { background-color: var(--coral-red); color: white; }
.board-tag--mintgray { background-color: var(--mint-gray); color: black; }
.board-tag--gold { background-color: var(--golden-sand); color: black; }
.board-tag--salmon { background-color: var(--salmon-sunset); color: black; }
.board-tag--caramel { background-color: var(--soft-caramel); color: black; }
.board-tag--violet { background-color: var(--violet-deep); color: white; }
.board-tag--blush { background-color: var(--blush-pink); color: black; }
.board-tag--honey { background-color: var(--sun-honey); color: black; }
.board-tag--avocado { background-color: var(--avocado-frost); color: black; }
.board-tag--seafoam { background-color: var(--seafoam-teal); color: white; }
</style>