
import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue';
import OauthRedirect from '../views/OauthRedirect.vue';


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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router