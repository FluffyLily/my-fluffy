<template>
    <footer class="app-footer">
        <div class="divider" />
        <div class="footer-container">
        © 2025 My fluffy. All rights reserved.
        </div>
    </footer>
</template>

<style scoped lang="scss">
.app-footer {
    padding: 0;
    color: var(--gray-600);
    font-size: 0.9rem;
    text-align: center;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    flex-shrink: 0;
    margin-top: 0 !important;

    .footer-container {
        max-width: 1400px;
        margin: 0 auto;
        padding: 0 2rem;
    }

    .divider {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        border-top: 1px solid var(--gray-900);
    }
}

@media (max-width: 768px) {
    .app-footer {
        font-size: 0.75rem;

        .footer-container {
        padding: 0 1rem;
        }
    }
}
</style>