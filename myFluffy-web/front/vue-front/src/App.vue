<template>
    <div class="app-container">
        <Header />
        <main class="main-content">
        <router-view />
        </main>
        <Footer />
    </div>
</template>

<script setup>
import Header from './components/Header.vue';
import Footer from './components/Footer.vue';
</script>

<style scoped lang="scss">
:deep(body),
:deep(html),
:deep(#app) {
    height: 100%;
    margin: 0;
    padding: 0;
    // overflow 제거
}

.app-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
    overflow: auto;
}

.main-content {
    flex: 1 0 auto;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: auto;
}

:deep(.app-header),
:deep(.app-footer) {
    flex-shrink: 0;
}
</style>