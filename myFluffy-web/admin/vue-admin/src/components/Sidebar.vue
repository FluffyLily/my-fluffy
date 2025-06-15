<template>
  <div class="sidebar">
    <div class="sidebar-header">메뉴</div>
    <ul>
      <li v-for="item in filteredMenuItems" :key="item.name">
        <router-link 
          :to="item.path"
          @click="reloadIfSameRoute(item.path)"
        >
          <font-awesome-icon :icon="item.icon" class="menu-icon" />
          {{ item.name }}
        </router-link>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from "../stores/auth";
import { hasAnyRole } from "../util/roleUtils";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 권한에 따른 메뉴 필터링
const filteredMenuItems = computed(() => {
  const userRole = authStore.roleId;
  const menu = [
    { name: '대시보드', path: '/main', icon: 'fa-home' },
    { name: '관리자 관리', path: '/admin', icon: 'fa-users-cog' },
    { name: '게시판 관리', path: '/board', icon: 'fa-th-list' },
    { name: '게시글 관리', path: '/post', icon: 'fa-file-alt' },
    { name: '회원 관리', path: '/user', icon: 'fa-user' },
    { name: '시스템 공지', path: '/notice', roles: ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN'], icon: 'fa-bell' }
  ];

  // 권한 체크하여 메뉴 필터링
  return menu.filter(item => {
    return !item.roles || hasAnyRole(userRole, item.roles);
  });
});

const reloadIfSameRoute = (path) => {
  if (route.path === path) {
    router.replace({ path: '/reload' }).then(() => {
      router.replace({ path });
    });
  }
};
</script>

<style lang="scss" scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 70px;
  bottom: 0;
  width: 250px;
  background-color: var(--sky-mist);
  color: white;
  padding: 20px;
  overflow-y: auto;
  transition: transform 0.3s;
  z-index: 10;

  &-header {
    font-size: 1.5rem;
    font-weight: bold;
    text-align: center;
    background-color: var(--mint-gray);
    color: white;
    padding: 12px;
    margin-bottom: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    letter-spacing: 1px;
    text-transform: uppercase;
  }

  ul {
    list-style-type: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }

  li {
    margin-bottom: 10px;

    a {
      text-decoration: none;
      color: white;
      font-size: 1.1rem;
      font-weight: 600;
      padding: 10px 15px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      transition: background-color 0.3s;

      .menu-icon {
        margin-right: 8px;
        font-size: 1rem;
      }

      &:hover {
        background-color: var(--mint-gray);
      }
    }
  }
}

@media (max-width: 992px) {
  .sidebar {
    transform: translateX(-100%);
  }
}
</style>