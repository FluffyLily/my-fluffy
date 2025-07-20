import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from "../stores/auth";
import Login from '../views/Login.vue'
import Users from '../views/Users.vue'
import Board from '../views/Board.vue'
import Posts from '../views/Posts.vue'
import PostDetail from '../views/PostDetail.vue'
import Editor from '../components/Editor.vue';
import Main from '../views/Main.vue'
import Notices from '../views/Notices.vue';
import Admin from '../views/Admin.vue'
import apiClient from "../api/axios.js";

const routes = [
  { 
    path: '/',
    name: 'Login',
    component: Login 
  },
  { 
    path: '/main', 
    name: 'Main',
    component: Main,
    meta: { requiresAuth: true }
  },
  {
    path: '/notice',
    name: 'Notice',
    component: Notices,
    meta: { requiresAuth: true }
  },
  { 
    path: '/admin', 
    name: 'Admin',
    component: Admin,
    meta: { requiresAuth: true }
  },
  { 
    path: '/user', 
    name: 'User',
    component: Users,
    meta: { requiresAuth: true } 
  },
  { 
    path: '/board',
    name: 'Board',
    component: Board,
    meta: { requiresAuth: true } 
  },
  {
    path: '/post/:boardId?',
    name: 'PostManagement',
    component: Posts,
    meta: { requiresAuth: true }
  },
  {
    path: '/post/write/:boardId?',
    name: 'WritePost',
    component: Editor,
    props: route => ({
      boardId: route.params.boardId ? Number(route.params.boardId) : null,
      boardName: route.query.boardName || null
    }),
    meta: { requiresAuth: true }
  },
  {
    path: '/post/update/:boardId?/:postId?',
    name: 'UpdatePost',
    component: Editor,
    props: route => ({
      boardId: route.params.boardId ? Number(route.params.boardId) : null,
      postId: route.params.postId ? Number(route.params.postId) : null,
      boardName: route.query.boardName || null
    }),
    meta: { requiresAuth: true }
  },
  {
    path: '/post/detail/:boardId?/:postId?',
    name: 'PostDetail',
    component: PostDetail,
    props: route => ({
      boardId: route.params.boardId ? Number(route.params.boardId) : null,
      postId: route.params.postId ? Number(route.params.postId) : null,
      boardName: route.query.boardName || null
    }),
    meta: { requiresAuth: true }
  }

]

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 라우트 가드 (로그인 상태 확인)
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  const accessToken = authStore.accessToken;
  const requiresAuth = to.meta.requiresAuth;

  // 토큰이 있으면 Authorization 헤더 설정
  if (accessToken) {
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
  }

  // 로그인 필수 페이지 접근 시
  if (requiresAuth && !accessToken) {
    authStore.clearAccessToken(); // 혹시라도 남아있을 수 있으니까 정리
    return next('/');
  }

  // 로그인 상태로 / 접근 시 /main으로 이동
  if (to.path === '/' && accessToken) {
    return next('/main');
  }
  next();
})

export default router