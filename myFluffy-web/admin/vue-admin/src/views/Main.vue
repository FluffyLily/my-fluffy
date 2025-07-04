<template>
  <div class="main-wrapper">
    <h1 class="main-title">관리자 시스템 대시보드</h1>
    <p class="main-subtitle">전체 시스템 현황을 한눈에 확인하세요</p>
    <!-- 시스템 공지 (최근 공지) -->
    <div class="card notice-card" @click="openNoticeModal">
      <h3><span class="emoji-text">📢</span> 시스템 공지</h3>
      <p v-if="recentNotice"><span class="emoji-text">📅</span> {{ formatDate(recentNotice.createdAt) }} | <span class="emoji-text">📌</span> {{ recentNotice.title }}</p>
      <p v-else><span class="emoji-text">📌</span> 최근 공지가 없습니다.</p>
    </div>

    <NoticeModal 
      :show="showNoticeModal" 
      :notice="recentNotice" 
      @close="showNoticeModal = false" 
    />
    <div class="summary-cards">
      <div class="card stat-card pink-border">
        <h2><span class="emoji-text">🔖</span> 게시판 수</h2>
        <p>{{ boardCount }}개</p>
      </div>
      <div class="card stat-card mint-border">
        <h2><span class="emoji-text">✏️</span> 게시글 수</h2>
        <p>{{ postSummary.totalCount }}건 / 오늘 {{ postSummary.todayCount }}건</p>
      </div>
      <div class="card stat-card yellow-border">
        <h2><span class="emoji-text">👥</span> 회원 수</h2>
        <p>총 {{ userSummary.totalCount }}명 / 최근 7일 {{ userSummary.weeklyCount }}명</p>
      </div>
      <div class="card stat-card violet-border">
        <h2><span class="emoji-text">🔧</span> 관리자 수</h2>
        <p>{{ adminCount }}명</p>
      </div>
    </div>

    <div class="card-section">
      <div class="card activity-card">
        <h3><span class="emoji-text">📌</span> 최근 게시글</h3>
        <ul>
          <li v-for="(post, index) in recentPosts" :key="index">
            <span class="emoji-text">📝</span> {{ post.title }} - {{ post.author }} | {{ formatDate(post.createdAt) }}
          </li>
        </ul>
      </div>

      <div class="card activity-card">
        <h3><span class="emoji-text">🆕</span> 최근 가입 회원</h3>
        <ul>
          <li v-for="(user, index) in recentUsers" :key="index">
            <span class="emoji-text">👤</span> {{ user.loginId }} - {{ formatDate(user.createdAt) }}
          </li>
        </ul>
      </div>
    </div>

    <div class="card chart-card">
      <h3><span class="emoji-text">📈</span> 시스템 활동 추이 (최근 7일)</h3>
      <p>차트 영역 (게시글 등록 추이)</p>
      <WeeklyPostChart :labels="chartLabels" :counts="chartCounts" />
    </div>

    <div class="card quick-action-card">
      <h3><span class="emoji-text">⚡</span> 빠른 작업</h3>
      <div class="quick-buttons">
        <button class="btn-add" @click="goToBoard">게시판 생성</button>
        <button class="btn-post" @click="goToPost">게시글 작성</button>
        <button class="btn-admin" @click="goToAdmin">관리자 추가</button>
        <button class="btn-search" @click="goToUser">회원 검색</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth.js';
import { format } from 'date-fns';
import apiClient from '../api/axios';
import WeeklyPostChart from '../components/WeeklyPostChart.vue'
import NoticeModal from '../components/NoticeModal.vue';
// 시스템 공지 (최근 공지)
const recentNotice = ref(null);
const showNoticeModal = ref(false);

// 시스템 공지 (최근 공지) 가져오기
const fetchRecentNotice = async () => {
  try {
    const response = await apiClient.get('/notice/dashboard');
    recentNotice.value = response.data;
  } catch (e) {
    console.error('최근 시스템 공지 불러오기 실패:', e);
  }
};

// 공지 모달 열기
const openNoticeModal = () => {
  if (recentNotice.value) {
    showNoticeModal.value = true;
  }
};

const router = useRouter();
const authStore = useAuthStore();
const recentPosts = ref([]);
const recentUsers = ref([]);
const boardCount = ref(0);
const postSummary = ref({ totalCount: 0, todayCount: 0 });
const adminCount = ref(0);
const userSummary = ref({ totalCount: 0, weeklyCount: 0 });
const formatDate = (date) => {
  return format(new Date(date), 'yyyy-MM-dd');
};
const weeklyPostData = ref({ labels: [], counts: [] })
const chartLabels = computed(() => weeklyPostData.value.labels)
const chartCounts = computed(() => weeklyPostData.value.counts)
const fetchWeeklyPostStats = async () => {
  try {
    const response = await apiClient.get('/post/weekly-count')
    weeklyPostData.value.labels = response.data.map(item => item.postDate)
    weeklyPostData.value.counts = response.data.map(item => item.count)
  } catch (e) {
    console.error('주간 게시글 통계 조회 실패:', e)
  }
}

const fetchDashboardStats = async () => {
  try {
    const [boardRes, postRes, adminRes, userRes] = await Promise.all([
      apiClient.get('/board/count'),
      apiClient.get('/post/count-summary'),
      apiClient.get('/admin/count'),
      apiClient.get('/user/count-summary')
    ])
    boardCount.value = boardRes.data.totalCount
    postSummary.value = postRes.data
    adminCount.value = adminRes.data.totalCount
    userSummary.value = userRes.data
  } catch (e) {
    console.error('대시보드 통계 불러오기 실패:', e)
  }
}

const fetchRecentPosts = async () => {
  try {
    const response = await apiClient.post('/post/list', {
      offset: 0,
      limit: 3,
      sort: 'recent'
    }, {
      headers: { Authorization: `Bearer ${authStore.accessToken}` }
    });

    recentPosts.value = response.data.posts.map(post => ({
      title: post.title,
      author: post.createdByName,
      createdAt: post.createdAt
    }))
  } catch (error) {
    console.error('최근 게시글 불러오기 실패:', error)
  }
}

const fetchRecentUsers = async () => {
  try {
    const response = await apiClient.post('/user/list', {
      offset: 0,
      limit: 3,
      sort: 'recent'
    }, {
      headers: { Authorization: `Bearer ${authStore.accessToken}` }
    });

    recentUsers.value = response.data.users.map(user => ({
      loginId: user.loginId,
      createdAt: user.createdAt
    }))
  } catch (error) {
    console.error('최근 등록한 회원 불러오기 실패:', error)
  }
}

const goToBoard = () => {
  router.push({ name: 'Board' })
}

const goToPost = () => {
  router.push({ name: 'WritePost' })
}

const goToAdmin = () => {
  router.push({ name: 'Admin' })
}

const goToUser = () => {
  router.push({ name: 'User' })
}

onMounted(() => {
  fetchDashboardStats();
  fetchRecentPosts();
  fetchRecentUsers();
  fetchWeeklyPostStats();
  fetchRecentNotice();
})
</script>

<style lang="scss" scoped>
.main-wrapper {
  padding: 2rem;
  background-color: var(--background-color);
  color: var(--text-color);
  min-width: 1200px;
  position: relative;
  z-index: 0;
  overflow-x: auto;
}

.notice-card,
.summary-cards,
.card-section,
.chart-card,
.quick-action-card {
  position: relative;
  z-index: 0;
  min-width: 1200px;
}

.main-title {
  font-size: 2rem;
  font-weight: bold;
  color: var(--secondary-color);
}

.main-subtitle {
  margin-bottom: 2rem;
  color: var(--text-color);
}

.notice-card {
  background-color: var(--avocado-frost);
  border-left: 5px solid var(--salmon-sunset);
  padding: 1rem;
  margin-bottom: 2rem;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: var(--peach-sherbet);
  }
}

.summary-cards {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  flex: 1;
  text-align: center;
  padding: 1.5rem;
  font-size: 1.1rem;
  font-weight: bold;
}

.pink-border {
  border-color: var(--card-border-pink);
}

.mint-border {
  border-color: var(--card-border-mint);
}

.yellow-border {
  border-color: var(--highlight-border-yellow);
}

.violet-border {
  border-color: var(--card-border-purple);
}

.card-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.activity-card {
  flex: 1;
}

.activity-card ul {
  padding-left: 1rem;
  margin-top: 0.5rem;
}

.activity-card ul li {
  position: relative;
  padding-left: 1.2rem;
  list-style: none;
  padding-bottom: 2px;
  border-bottom: 2px dotted var(--accent-color);
  margin-bottom: 6px;
}

.activity-card ul li .dot {
  position: absolute;
  left: 0;
  top: 0.6em;
  width: 8px;
  height: 8px;
  background-color: var(--button-add-color);
  border-radius: 50%;
}

.chart-card {
  margin-bottom: 2rem;
}

.quick-action-card {
  margin-bottom: 2rem;
}

.quick-buttons {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  margin-top: 1rem;
}

.btn-add {
  background-color: var(--avocado-frost);
}

.btn-add:hover {
  background-color: var(--melon-icecream);
}

.btn-post {
  background-color: var(--seafoam-teal);
}

.btn-post:hover {
  background-color: var(--mint-gray);
}

.btn-admin {
  background-color: var(--violet-deep);
}

.btn-admin:hover {
  background-color: var(--lavender-soft);
}

.btn-search {
  background-color: var(--salmon-sunset);
}

.btn-search:hover {
  background-color: var(--peach-sherbet);
}

.btn-notice {
  background-color: var(--sun-honey);
}

.btn-notice:hover {
  background-color: var(--highlight-yellow);
}

.emoji-text {
  font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif;
}

</style>
