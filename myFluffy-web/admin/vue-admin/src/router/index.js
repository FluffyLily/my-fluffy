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
    path: '/post',
    name: 'PostManagement',
    component: Posts,
    props: route => ({
      boardId: route.query.boardId ? Number(route.query.boardId) : null,
      focusedPostId: route.query.focusedPostId ? Number(route.query.focusedPostId) : null
    }),
    meta: { requiresAuth: true }
  },
  {
    path: '/post/write',
    name: 'WritePost',
    component: Editor,
    props: route => ({
      boardId: route.query.boardId ? Number(route.query.boardId) : null,
      boardName: route.query.boardName || null
    }),
    meta: { requiresAuth: true }
  },
  {
    path: '/post/update/:postId?',
    name: 'UpdatePost',
    component: Editor,
    props: route => ({
      boardId: route.query.boardId ? Number(route.query.boardId) : null,
      postId: route.params.postId ? Number(route.params.postId) : null,
      boardName: route.query.boardName || null
    }),
    meta: { requiresAuth: true }
  },
  {
    path: '/post/detail/:postId?',
    name: 'PostDetail',
    component: PostDetail,
    props: route => ({
      boardId: route.query.boardId ? Number(route.query.boardId) : null,
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

function isLoggedIn() {
  return !!useAuthStore().accessToken;
}

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  if (to.meta.requiresAuth && !isLoggedIn()) {
    authStore.clearAccessToken();
    return next('/');
  }

  if (to.path === '/' && isLoggedIn()) {
    return next('/main');
  }
  next();
})

export default router