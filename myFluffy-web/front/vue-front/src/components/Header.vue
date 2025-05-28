<template>
    <header class="app-header">
        <div class="header-container">
            <router-link to="/" class="logo">My Fluffy</router-link>
            <div class="hamburger" @click="toggleMenu">☰</div>
            <nav class="nav" :class="{ open: menuOpen }">
                <router-link to="/about">Our story</router-link>
                <router-link to="/blog">Blog</router-link>
                <router-link to="/login" v-if="!userName">Sign in</router-link>
                <template v-else>
                    <span class="welcome-text">Welcome! {{ userName }} </span>
                </template>
                <button class="start-btn" v-if="!userName" @click="showJoinModal = true">Get started</button>
            </nav>
        </div>
        <div class="divider" />
    </header>
    <JoinModal v-if="showJoinModal" @close="showJoinModal = false" />
</template>

<script setup>
import { ref } from 'vue';
import JoinModal from '../components/JoinModal.vue'
import { useAuthStore } from '../stores/auth'
import { computed } from 'vue'

const authStore = useAuthStore()
const userName = computed(() => authStore.userName)

const menuOpen = ref(false);
const showJoinModal = ref(false);
const toggleMenu = () => {
    menuOpen.value = !menuOpen.value;
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

    .nav > a {
        color: var(--plum-purple);
        text-decoration: none;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", "Open Sans", sans-serif;
        font-size: 1rem;
        font-weight: 500;
        letter-spacing: 0.01em;
        margin-left: 1.5rem;
        transition: color 0.2s ease;

        &:hover {
            color: var(--rose-pink);
        }
    }

    .start-btn {
        background-color: var(--plum-purple);
        color: #fff !important;
        padding: 0.4rem 1rem;
        border-radius: 20px;
        font-weight: 500;
        margin-left: 1.5rem;
        transition: background-color 0.2s ease;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", "Open Sans", sans-serif;
        font-size: 1rem;
        letter-spacing: 0.01em;
        line-height: 1.4;
        border: none;
        cursor: pointer;

        &:hover {
            background-color: var(--rose-pink);
        }
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

    .welcome-text {
        margin-left: 1.5rem;
        font-size: 1rem;
        font-weight: 500;
        color: var(--plum-purple);
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", "Open Sans", sans-serif;
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

            &.open {
            display: flex;
            }

            a {
            margin: 0.8rem 0;
            }
        }

        .start-btn {
            display: none !important;
        }

        .welcome-text {
            margin-top: 0.8rem;
        }
    }
}
</style>