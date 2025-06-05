<template>
    <section class="blog-page">
        <div class="blog-header">
        <h1 class="blog-title">My Fluffy Blog</h1>
        <p class="blog-subtitle">귀여움과 이야기가 함께하는 공간 🐾</p>
        </div>

        <div class="blog-list">
        <div class="blog-card" v-for="n in 3" :key="n">
            <h2 class="card-title">포스트 제목 {{ n }}</h2>
            <p class="card-snippet">
            이곳에는 게시글 미리보기가 들어갑니다. 짧은 소개글이나 요약 내용이 보여질 예정입니다.
            </p>
        </div>
        </div>
    </section>
</template>

<script setup>

</script>

<style scoped lang="scss">
.blog-page {
    padding: 3rem 1.5rem;
    background-color: #fffafc;

    .blog-header {
        text-align: center;
        margin-bottom: 2rem;

        .blog-title {
        font-size: 2.5rem;
        color: #655a7c;
        font-weight: bold;
        }

        .blog-subtitle {
        font-size: 1.2rem;
        color: #777;
        margin-top: 0.5rem;
        }
    }

    .blog-list {
        display: flex;
        flex-direction: column;
        gap: 1.5rem;
        max-width: 800px;
        margin: 0 auto;

        .blog-card {
        background-color: #fff;
        border: 1px solid #eee;
        border-radius: 8px;
        padding: 1.5rem;

        .card-title {
            font-size: 1.5rem;
            color: #333;
            margin-bottom: 0.5rem;
        }

        .card-snippet {
            font-size: 1rem;
            color: #555;
            line-height: 1.6;
        }
        }
    }
}
</style>