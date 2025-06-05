
import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue';
import About from '../views/About.vue';
import Blog from '../views/Blog.vue';
import OauthRedirect from '../components/OauthRedirect.vue';

const routes = [
  { 
    path: '/',
    name: 'Home',
    component: Home 
  },
  {
    path: '/oauth-redirect',
    name: 'OauthRedirect',
    component: OauthRedirect
  },
  {
    path: '/about',
    name: 'About',
    component: About
  },
  {
    path: '/blog',
    name: 'Blog',
    component: Blog
  }

]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router