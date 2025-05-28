<template>
    <section class="home-intro">
        <div class="home-intro-container" @click="handleBackgroundClick">
        <h1 class="home-intro-title">
            Turning moments<br />
            with pets<br />
            into shared stories.
        </h1>
        <p class="home-intro-subtext">Read, write, and feel closer through everyday moments.</p>
        <button class="home-start-button" v-if="showStartButton" @click="openJoinModal">Start reading</button>
        </div>
    </section>
    <JoinModal v-if="showJoinModal" @close="closeJoinModal" />
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import JoinModal from '@/components/JoinModal.vue'

const router = useRouter();
const showJoinModal = ref(false)
const showStartButton = ref(window.innerWidth > 767)

const openJoinModal = () => {
    showJoinModal.value = true
}

const closeJoinModal = () => {
    showJoinModal.value = false
}

const handleBackgroundClick = () => {
    if (window.innerWidth <= 767) {
        openJoinModal()
    }
}

const handleResize = () => {
    showStartButton.value = window.innerWidth > 767
}

onMounted(() => {
    window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.home-intro {
    flex: 1;
    background-image: url('../assets/images/my-fluffy01.png');
    background-size: cover;
    background-position: center center;
    background-repeat: no-repeat;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 4rem;
}

.home-intro-container {
    text-align: left;
}

.home-intro-title {
    font-family: 'Noto Serif KR', Georgia, serif;
    font-weight: 600;
    font-size: 4.5rem;
    letter-spacing: -0.03em;
    line-height: 1.3;
    color: var(--plum-purple);
    margin-bottom: 1.5rem;
}

.home-intro-subtext {
    font-size: 1.2rem;
    font-weight: bold;
    color: var(--gray-700);
    margin-top: 1rem;
    margin-bottom: 2rem;
    line-height: 1.6;
}

.home-start-button {
    background-color: var(--plum-purple);
    color: #fff;
    padding: 0.6rem 1.6rem;
    border-radius: 24px;
    font-weight: 600;
    font-size: 1.1rem;
    letter-spacing: 0.01em;
    line-height: 1.4;
    border: none;
    cursor: pointer;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", "Open Sans", sans-serif;
    transition: background-color 0.2s ease;

    &:hover {
        background-color: var(--rose-pink);
    }
}

@media (max-width: 1024px) {
    .home-intro-title {
        font-size: 3rem;
    }

    .home-intro-subtext {
        font-size: 1.1rem;
    }
}

@media (max-width: 767px) {
    .home-intro-container {
        cursor: pointer;
    }
}

@media (max-width: 600px) {
    .home-intro {
        padding: 2rem;
    }

    .home-intro-title {
        font-size: 2.2rem;
        line-height: 1.4;
    }

    .home-intro-subtext {
        font-size: 1rem;
    }

    .home-start-button {
        font-size: 0.9rem;
        padding: 0.5rem 1.2rem;
    }
}
</style>
