<template>
    <section class="about-page">
        <div class="about-container">
        <h1 class="about-title">About My Fluffy</h1>
        <p class="about-description">
            My Fluffy는 따뜻한 일상과 이야기를 기록하고 공유할 수 있는 공간입니다.<br />
            소중한 순간, 반려동물과의 추억, 그리고 당신만의 이야기를 담아보세요.
        </p>
        </div>
    </section>
</template>

<script setup>

</script>

<style scoped lang="scss">
.about-page {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 4rem 1.5rem;
    background-color: #fdfafa;

    .about-container {
        max-width: 800px;
        text-align: center;

        .about-title {
            font-size: 2.5rem;
            font-weight: bold;
            margin-bottom: 1rem;
            color: #655a7c;
        }

        .about-description {
            font-size: 1.2rem;
            line-height: 1.8;
            color: #555;
        }
    }
}
</style>