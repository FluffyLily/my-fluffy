<template>
  <div class="main-wrapper">
    <h1 class="main-title">관리자 시스템 대시보드</h1>
    <p class="main-subtitle">전체 시스템 현황을 한눈에 확인하세요</p>
    <!-- 시스템 공지 (최근 공지) -->
    <div class="card notice-card" @click="openNoticeModal">
      <h3>📢 시스템 공지</h3>
      <p v-if="recentNotice">📅 {{ formatDate(recentNotice.createdAt) }} | 📌 {{ recentNotice.title }}</p>
      <p v-else>📌 최근 공지가 없습니다.</p>
    </div>

    <NoticeModal 
      :show="showNoticeModal" 
      :notice="recentNotice" 
      @close="showNoticeModal = false" 
    />
    <div class="summary-cards">
      <div class="card stat-card pink-border">
        <h2>🔖 게시판 수</h2>
        <p>{{ boardCount }}개</p>
      </div>
      <div class="card stat-card mint-border">
        <h2>✏️ 게시글 수</h2>
        <p>{{ postSummary.totalCount }}건 / 오늘 {{ postSummary.todayCount }}건</p>
      </div>
      <div class="card stat-card yellow-border">
        <h2>👥 회원 수</h2>
        <p>2,148명 / 최근 7일 25명</p>
      </div>
      <div class="card stat-card violet-border">
        <h2>🔧 관리자 수</h2>
        <p>{{ adminCount }}명</p>
      </div>
    </div>

    <div class="card-section">
      <div class="card activity-card">
        <h3>📌 최근 게시글</h3>
        <ul>
          <li v-for="(post, index) in recentPosts" :key="index">
            📝 {{ post.title }} - {{ post.author }} | {{ formatDate(post.createdAt) }}
          </li>
        </ul>
      </div>

      <div class="card activity-card">
        <h3>🆕 최근 가입 회원</h3>
        <ul>
          <li>👤 홍길동 - 2025.04.28</li>
          <li>👤 김철수 - 2025.04.27</li>
          <li>👤 이영희 - 2025.04.27</li>
        </ul>
      </div>
    </div>

    <div class="card chart-card">
      <h3>📈 시스템 활동 추이 (최근 7일)</h3>
      <p>차트 영역 (게시글 등록 추이)</p>
      <WeeklyPostChart :labels="chartLabels" :counts="chartCounts" />
    </div>

    <div class="card quick-action-card">
      <h3>⚡ 빠른 작업</h3>
      <div class="quick-buttons">
        <button class="btn-add" @click="goToBoardCreate">게시판 생성</button>
        <button class="btn-post" @click="goToPostCreate">게시글 작성</button>
        <button class="btn-admin" @click="goToAdminCreate">관리자 추가</button>
        <button class="btn-search">회원 검색</button>
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
const boardCount = ref(0);
const postSummary = ref({ totalCount: 0, todayCount: 0 });
const adminCount = ref(0);
const formatDate = (date) => {
  return format(new Date(date), 'yyyy-MM-dd');
};
const weeklyPostData = ref({ labels: [], counts: [] })
const chartLabels = computed(() => weeklyPostData.value.labels)
const chartCounts = computed(() => weeklyPostData.value.counts)
const fetchWeeklyPostStats = async () => {
  try {
    const res = await apiClient.get('/post/weekly-count')
    weeklyPostData.value.labels = res.data.map(item => item.postDate)
    weeklyPostData.value.counts = res.data.map(item => item.count)
  } catch (e) {
    console.error('주간 게시글 통계 조회 실패:', e)
  }
}

const fetchDashboardStats = async () => {
  try {
    const [boardRes, postRes, adminRes] = await Promise.all([
      apiClient.get('/board/count'),
      apiClient.get('/post/count-summary'),
      apiClient.get('/admin/count')
    ])
    boardCount.value = boardRes.data.totalCount
    postSummary.value = postRes.data
    adminCount.value = adminRes.data.totalCount
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

const goToBoardCreate = () => {
  router.push({ name: 'Board' })
}

const goToPostCreate = () => {
  router.push({ name: 'WritePost' })
}

const goToAdminCreate = () => {
  router.push({ name: 'Admin' })
}

onMounted(() => {
  fetchDashboardStats();
  fetchRecentPosts();
  fetchWeeklyPostStats();
  fetchRecentNotice();
})
</script>

<style lang="scss" scoped>
.main-wrapper {
  padding: 2rem;
  background-color: var(--background-color);
  color: var(--text-color);
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

</style>
