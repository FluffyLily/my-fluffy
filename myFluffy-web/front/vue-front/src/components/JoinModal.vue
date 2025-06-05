<template>
    <div class="modal-overlay" @click.self="$emit('close')">
        <div class="modal-card">
        <button class="modal-close" @click="$emit('close')">×</button>
        <h2 class="modal-title">Join My fluffy.</h2>
        <button class="social-btn google" @click="loginWithGoogle">
            <img src="../assets/icons/google_rd_SU.svg" alt="Sign up with Google" class="google-btn-img" />
        </button>
        <button class="social-btn email" @click="$emit('signup-email')">
            <font-awesome-icon icon="envelope" class="email-icon" /> Sign up with Email
        </button>
        <div class="sub-text">
            Already have an account? <a href="#" @click.prevent="$emit('switch-to-login')">Sign in</a>
        </div>
        <div class="legal-text">
            Click "Sign up" to agree to My fluffy’s <a href="#">Terms of Service</a> and<br> 
            acknowledge that My fluffy’s <a href="#">Privacy Policy</a> applies to you.
        </div>
        </div>
    </div>
</template>

<script setup>
function loginWithGoogle() {
    window.location.href = 'http://localhost:8082/oauth2/authorization/google'
}

// function loginWithKakao() {
//     window.location.href = 'http://localhost:8082/oauth2/authorization/kakao'
// }
</script>

<style scoped lang="scss">
.modal-overlay {
    position: fixed;
    inset: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal-card {
    position: relative;
    background: white;
    padding: 2rem;
    border-radius: 8px;
    max-width: 420px;
    width: 100%;
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1);
    text-align: center;

    .modal-close {
        position: absolute;
        top: 1rem;
        right: 1rem;
        background: none;
        border: none;
        font-size: 2rem;
        color: #666;
        cursor: pointer;
    }

    .modal-title {
        color: var(--gray-900);
        font-size: 1.5rem;
        font-weight: 600;
        margin-bottom: 2rem;
    }
}

.social-btn {
    width: 100%;
    max-width: 300px;
    margin: 0 auto;
    padding: 0.75rem 1rem;
    margin-bottom: 1rem;
    border-radius: 9999px;
    border: 1px solid #dcdcdc;
    background: #fff;
    color: #333;
    font-weight: 500;
    font-size: 0.95rem;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    cursor: pointer;

    &.google {
        padding: 0;
        border: none;
        background: none;
        height: auto;
        width: auto;
        display: inline-block;
    }

    &.email {
        width: 179px;
        height: 40px;
        padding: 0;
        border: 1px solid var(--gray-700);
        background: #fff;
        color: var(--gray-800);
        display: flex;
        justify-content: center;
        align-items: center;
        font-weight: 600;
        font-size: 0.85rem;
        border-radius: 9999px;
    }
}

.google-btn-img {
    height: 40px;
    width: auto;
    max-width: 100%;
    display: block;
}

.email-icon {
    font-size: 18px;
    display: inline-block;
    vertical-align: middle;
}

.sub-text {
    font-size: 0.85rem;
    margin-top: 0.75rem;

    a {
        color: var(--misty-blue);
        font-weight: 600;
        text-decoration: none;
    }
}

.legal-text {
    margin-top: 1.5rem;
    font-size: 0.75rem;
    color: #888;

    a {
        color: #888;
        text-decoration: underline;
        margin: 0 0.25rem;
    }
}
</style>