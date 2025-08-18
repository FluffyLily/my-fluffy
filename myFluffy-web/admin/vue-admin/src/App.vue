
<template>
  <Login v-if="!isLoggedIn" />
  <div v-else class="app-container">
    <Sidebar />
    <Header />
    <div class="main-content">
      <div class="content-area">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import Sidebar from './components/Sidebar.vue';
import Header from './components/Header.vue';
import Login from './views/Login.vue';
import { useAuthStore } from './stores/auth';

const authStore = useAuthStore();
const isLoggedIn = computed(() => !!authStore.userId);
</script>

<style lang="scss" scoped>
.app-container {
  display: flex;
  width: 100%;
  min-height: 100vh;
}

.main-content {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  margin-left: 250px;
  transition: margin-left 0.3s;
  padding-top: 70px;
}

.content-area {
  padding: 30px;
  background-color: white;
  border-radius: 12px;
  margin: 20px;
  box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.1);
}

@media (max-width: 992px) {
  .main-content {
    margin-left: 0;
  }
}
</style>