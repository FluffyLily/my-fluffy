<template>
    <article class="post-card" @click="goToDetail">
        <h3 class="title">{{ post.title }}</h3>
        <p class="summary">{{ post.summary }}</p>
        <div class="meta">
        <span class="tags">
            <span v-for="tag in post.tags" :key="tag" class="tag">#{{ tag }}</span>
        </span>
        <span class="separator">·</span>
        <span class="author">{{ post.author }}</span>
        <span class="separator">·</span>
        <span class="date">{{ formatDate(post.createdAt) }}</span>
        </div>
    </article>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
    post: {
        type: Object,
        required: true,
    },
})

const router = useRouter()
const goToDetail = () => {
    router.push(`/posts/${props.post.id}`)
}

const formatDate = (dateStr) => {
    const date = new Date(dateStr)
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
    })
}
</script>

<style scoped lang="scss">
.post-card {
    background-color: var(--peach-cream);
    border-radius: 12px;
    padding: 1.2rem 1.5rem;
    margin-bottom: 1.5rem;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    .title {
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--plum-purple);
        margin-bottom: 0.6rem;
    }

    .summary {
        font-size: 0.95rem;
        color: var(--gray-900);
        margin-bottom: 1rem;
        line-height: 1.6;
    }

    .meta {
        font-size: 0.85rem;
        color: var(--gray-900);
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.3rem;
        .tags {
            display: flex;
            flex-wrap: wrap;
            gap: 0.4rem;
        }

        .tag {
            background: var(--tag-bg-color);
            color: var(--tag-text-color);
            padding: 0.2rem 0.6rem;
            border-radius: 999px;
            font-size: 0.8rem;
        }

        .separator {
        margin: 0 0.3rem;
        color: var(--gray-400);
        }
    }
}
</style>
