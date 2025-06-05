<template>
    <header class="app-header">
        <div class="header-container">
            <router-link to="/" class="logo">My Fluffy</router-link>
            <div class="hamburger" @click="toggleMenu">☰</div>
            <nav class="nav" :class="{ open: menuOpen }">
                <router-link to="/about" class="nav-item">Our story</router-link>
                <a href="#" class="nav-item" @click.prevent="goToBlog">Blog</a>
                <button class="nav-item signin-btn" v-if="!userName" @click="showLoginModal = true">Sign in</button>
                <template v-else>
                    <div class="user-menu">
                        <span class="welcome-text" @click="toggleUserDropdown">Welcome! {{ userName }}</span>
                        <div v-if="toggleDropdown && deviceType === 'pc'" class="dropdown">
                            <button class="logout-btn" @click="logout">Logout</button>
                        </div>
                        <button v-if="menuOpen" class="logout-btn" @click="logout">Logout</button>
                    </div>
                </template>
                <button class="nav-item start-btn" v-if="!userName" @click="showJoinModal = true">Get started</button>
            </nav>
        </div>
        <div class="divider" />
    </header>
    <JoinModal 
        v-if="showJoinModal" 
        @close="showJoinModal = false"
        @switch-to-login="showJoinModal = false; showLoginModal = true"
    />
    <LoginModal
        v-if="showLoginModal"
        @close="showLoginModal = false"
        @switch-to-join="showLoginModal = false; showJoinModal = true"
    />
</template>

<script setup>
import { ref } from 'vue';
import JoinModal from '../components/JoinModal.vue'
import LoginModal from '../components/Login.vue'
import apiClient from '../api/axios'
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth'
import { computed } from 'vue'
import { onMounted, onUnmounted } from 'vue';

const authStore = useAuthStore()
const router = useRouter()
const userName = computed(() => authStore.userName)

const menuOpen = ref(false);
const showJoinModal = ref(false);
const showLoginModal = ref(false);

// 사용자 기기 감지 - PC/Mobile
const deviceType = ref(window.innerWidth <= 767 ? 'mobile' : 'pc');
const updateDeviceType = () => {
    deviceType.value = window.innerWidth <= 767 ? 'mobile' : 'pc';
};

onMounted(() => window.addEventListener('resize', updateDeviceType));
onUnmounted(() => window.removeEventListener('resize', updateDeviceType));

const isMobile = computed(() => deviceType.value === 'mobile');

// 모바일 - 메뉴 햄버거 토글
const toggleMenu = () => {
  if (deviceType.value === 'mobile') {
    menuOpen.value = !menuOpen.value;
  }
};

// PC - 로그아웃 토글 on/off
const toggleDropdown = ref(false);
const toggleUserDropdown = () => {
  if (deviceType.value === 'pc') {
    toggleDropdown.value = !toggleDropdown.value;
  }
};

const logout = async () => {
    try {
        await apiClient.post("/auth/oauth/logout");
        authStore.clearUserInfo();
        await router.push("/");
    } catch (error) {
        console.error("로그아웃 실패:", error);
    }
};

const goToBlog = () => {
    if (!authStore.userName) {
        showJoinModal.value = true;
    } else {
        router.push('/blog');
    }
};
</script>


<style scoped lang="scss">
.app-header {
    height: 64px;
    display: flex;
    align-items: center;
    position: sticky;
    top: 0;
    z-index: 1000;
    padding: 0;
    position: relative;
}

.header-container {
    max-width: 1400px;
    margin: 0 auto;
    flex: 1;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 6rem;
}

.logo {
    font-size: 1.8rem;
    font-weight: bold;
    color: var(--plum-purple);
    text-decoration: none;
    font-family: 'Noto Serif KR', Georgia, serif;
    letter-spacing: -0.04em;
    line-height: 1.4;
}

.nav {
    display: flex;
    align-items: center;
}

.nav-item {
    color: var(--plum-purple);
    text-decoration: none;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", "Open Sans", sans-serif;
    font-size: 1rem;
    font-weight: 500;
    letter-spacing: 0.01em;
    transition: color 0.2s ease;
    cursor: pointer;
    margin-left: 1.5rem;
    background: none;
    border: none;
    padding: 0;
}
.nav-item:hover {
    color: var(--rose-pink);
}

.start-btn {
    background-color: var(--plum-purple);
    color: #fff !important;
    padding: 0.4rem 1rem;
    border-radius: 20px;
    font-weight: 500;
    transition: background-color 0.2s ease;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", "Open Sans", sans-serif;
    font-size: 1rem;
    letter-spacing: 0.01em;
    line-height: 1.4;
    border: none;
    cursor: pointer;
}
.start-btn:hover {
    background-color: var(--rose-pink);
}

.divider {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    border-top: 1px solid var(--gray-900);
    margin-top: 0;
}

.hamburger {
    display: none;
}

.user-menu {
    display: flex;
    align-items: center;
    gap: 0.3rem;
    position: relative;
    color: var(--plum-purple);
    text-decoration: none;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", "Open Sans", sans-serif;
    font-size: 1rem;
    font-weight: 500;
    letter-spacing: 0.01em;
    transition: color 0.2s ease;
    cursor: pointer;
    margin-left: 1.5rem;
    background: none;
    border: none;
    padding: 0;
}

.welcome-text {
    font-size: 1rem;
    font-weight: 500;
    color: var(--plum-purple);
    white-space: nowrap;
    cursor: pointer;
    user-select: none;
}

.dropdown {
    position: absolute;
    top: 120%;
    right: 0;
    background: white;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 0.5rem;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    z-index: 1001;
}

.logout-btn {
    background: none;
    border: none;
    color: var(--dusty-mauve);
    font-size: 0.95rem;
    cursor: pointer;
    padding: 0.3rem 0.6rem;
}
.logout-btn:hover {
    color: var(--rose-pink);
}

@media (max-width: 767px) {
    .header-container {
        padding: 0 1.2rem;
    }
    .logo {
        font-size: 1.4rem;
    }
    .hamburger {
        display: block;
        font-size: 1.5rem;
        cursor: pointer;
    }
    .nav {
        display: none;
        flex-direction: column;
        position: absolute;
        top: 64px;
        right: 0;
        background: white;
        width: 100%;
        padding: 1rem 2rem;
        border-top: 1px solid #eee;
    }
    .nav.open {
        display: flex;
    }
    .nav-item {
        margin: 0.8rem 0;
        width: 100%;
        text-align: left;
        display: block;
        line-height: 1.4;
        padding: 0;
    }
    .signin-btn {
        display: block;
        width: 100%;
        text-align: left;
        line-height: 1.4;
        padding: 0;
    }
    .user-menu {
        width: 100%;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        gap: 0.3rem;
        margin-left: 0;
        padding: 0;
    }
    .welcome-text {
        flex: 1;
        width: auto;
        margin: 0.8rem 0;
    }
    .logout-btn {
        font-size: 0.8rem;
        text-align: right;
        white-space: nowrap;
    }
    .start-btn {
        display: none !important;
    }
}
</style>
