<template>
    <div class="post-detail-wrapper">
        <div class="board-header">
            <h2 class="board-name">{{ boardName }}</h2>
            <button class="btn-edit" v-if="canEdit" @click="goToEdit">수정하기</button>
        </div>
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="meta">
        <span>작성자: {{ post.createdByName }}</span>
        <span>등록일: {{ formatDate(post.createdAt) }}</span>
        <span>수정일: {{ formatDate(post.updatedAt) }}</span>
        <span>
            <span v-for="tag in postTags" :key="tag" class="tag">#{{ tag }}</span>
        </span>
        </div>
        <div class="content" v-html="post.content"></div>
        <div class="action-buttons">
            <button class="btn-back" @click="goToPostList">목록으로</button>
            <button class="btn-delete" v-if="canDelete" @click="confirmDeletePost(postId, post.title)">삭제하기</button>
        </div>
    </div>
    <!-- 게시글 삭제 모달 -->
    <div v-if="showDeletePostModal" class="modal fade show d-flex justify-content-center align-items-center" style="display: block;" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content">
            <div class="modal-header">
            <h5 class="modal-title">게시글 삭제</h5>
            <button type="button" class="btn-close" @click="closeDeleteModal"></button>
            </div>
            <div class="modal-body">
            <div class="mb-3">
                <label class="form-label d-block">관리자 비밀번호 확인</label>
                <input type="password" v-model="deletePassword" class="form-control mb-2" />
                <div v-if="deleteError" class="text-danger mb-3">{{ deleteError }}</div>
            </div>
            <div class="text-danger mb-3">
                <small>⚠️ 이 작업은 되돌릴 수 없습니다.</small>
            </div>
            </div>
            <div class="modal-footer">
            <button class="btn btn-delete" @click="deletePost">삭제</button>
            <button class="btn btn-secondary" @click="closeDeleteModal">닫기</button>
            </div>
        </div>
    </div>
    </div>
</template>

<script setup>
import { onMounted, ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiClient from '../api/axios.js';
import { format } from 'date-fns';
import { useAuthStore } from '../stores/auth.js';
import { hasAnyRole } from '../util/roleUtils.js';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const { boardId, postId, boardName } = defineProps({
    boardId: {
        type: [String, Number],
        default: null
    },
    postId: {
        type: [String, Number],
        default: null
    },
    boardName: {
        type: String,
        default: ''
    }
});
const post = ref({});
const postTags = ref([]);

const canEdit = computed(() => !!postId);
const selectedPostTitle = ref('');
const selectedPostId = ref(null);

const deletePassword = ref('');
const deleteError = ref('');
const showDeletePostModal = ref(false);

// 관리자 삭제 모달 비밀번호 에러 메시지 초기화
watch(showDeletePostModal, (newVal) => {
    if (!newVal) deleteError.value = '';
});

// 사용자가 비밀번호 입력을 다시 시작하면 에러 메시지 제거
watch(deletePassword, () => {
    deleteError.value = '';
});

const confirmDeletePost = (postId, postTitle) => {
    selectedPostId.value = postId;
    selectedPostTitle.value = postTitle;
    showDeletePostModal.value = true;
};

const closeDeleteModal = () => {
    showDeletePostModal.value = false;
    deletePassword.value = '';
};

const canDelete = computed(() =>
    postId && (
        hasAnyRole(authStore.roleId, ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN']) ||
        authStore.userId === post.value.createdBy
    )
);

const fetchPost = async () => {
    try {
        const response = await apiClient.get(`/post/detail/${postId}`);
        post.value = response.data;
        if (response.data.postCategoryString) {
        postTags.value = response.data.postCategoryString.split(',').map(tag => tag.trim());
        }
    } catch (e) {
        console.error('게시글 조회 실패', e);
    }
};

// 보던 게시글 목록으로 돌아가기
const goToPostList = () => {
    router.push({
        name: 'PostManagement',
        params: { boardId: route.query.boardId },
        query: {
            offset: route.query.offset,
            boardId: route.query.boardId,
            boardName: route.query.boardName,
            searchKeyword: route.query.searchKeyword,
            searchType: route.query.searchType,
            sort: route.query.sort,
            isVisible: route.query.isVisible,
        }
    });
};

// 수정하기 에디터 이동
const goToEdit = () => {
    router.push({
        name: 'UpdatePost',
        params: {
            postId,
            boardId: route.params.boardId
        },
        query: {
            boardName: route.query.boardName || null
        }
    });
};

const deletePost = async () => {
    if (!deletePassword.value.trim()) {
        deleteError.value = '관리자 비밀번호를 입력하세요.';
        return;
    }

    try {
        const response = await apiClient.post('/admin/verify-password', {
            username: authStore.loginId,
            password: deletePassword.value
        });

        if (response.data.success) {
            await apiClient.delete(`/post/delete/${selectedPostId.value}`, {
                params: {
                    deleterId: authStore.loginId,
                    postTitle: selectedPostTitle.value
                }
            });
            showDeletePostModal.value = false;
            deletePassword.value = '';
            router.push({ name: 'PostManagement' });
        } else {
            deleteError.value = '비밀번호가 일치하지 않습니다.';
        }
    } catch (error) {
        console.error('게시글을 삭제하지 못함: ', error);
        deleteError.value = '게시글 삭제 중 문제가 발생했습니다. 다시 시도해 주세요.';
    }
};

const formatDate = (date) => {
    const d = new Date(date);
    if (isNaN(d.getTime())) return '-';
    return format(d, 'yyyy-MM-dd HH:mm');
};

onMounted(() => {
    if (postId) {
        fetchPost();
    } else {
        console.warn('postId가 없어 게시글 상세 조회를 할 수 없음.');
    }

    if (route.query.edited) {
        alert('게시글이 성공적으로 수정되었습니다.');
    }
});
</script>

<style scoped lang="scss">
.post-detail-wrapper {
    padding: 32px;
    background: #fdfcfa;
    border-radius: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    max-width: 800px;
    margin: 40px auto;
    text-align: left;

    .board-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .btn-edit {
            background: var(--button-next-color);
            color: white;
            padding: 6px 12px;
            border-radius: 6px;
            font-size: 14px;
            border: none;
            cursor: pointer;

            &:hover {
                background: var(--button-hover-next);
            }
        }
    }

    .board-name {
        font-size: 16px;
        font-weight: 500;
        color: var(--violet-deep);
        margin-bottom: 8px;
    }

    .post-title {
        font-size: 28px;
        font-weight: bold;
        margin-bottom: 14px;
        line-height: 1.4;
        color: #333;
    }

    .meta {
        font-size: 15px;
        color: #999;
        margin-bottom: 24px;
        line-height: 1.8;

        span {
            display: inline-block;
            margin-right: 14px;
        }

        .tag {
            background: var(--melon-icecream);
            padding: 2px 8px;
            margin-left: 4px;
            margin-right: 4px;
            color: black;
            border-radius: 14px;
        }
    }

    .content {
        padding: 16px 0;
        font-size: 16px;
        border-top: 1px solid #eee;
        border-bottom: 1px solid #eee;
        margin-bottom: 32px;
        line-height: 1.8;
        word-break: break-word;
    }

    ::v-deep(.content img) {
        width: 100%;
        max-width: 100%;
        max-height: 600px;
        height: auto;
        display: block;
        margin: 1rem auto;
        object-fit: contain;
    }

    .action-buttons {
        margin-top: 8px;
        display: flex;
        justify-content: center;
        gap: 12px;

        button {
            padding: 8px 14px;
            border-radius: 6px;
            border: none;
            cursor: pointer;
            font-size: 14px;
        }

        .btn-back {
            background: var(--button-close-color);
            color: white;
        }
        .btn-back:hover{
            background: var(--button-hover-close);
        }

        .btn-delete {
            background: var(--button-danger-color);
            color: white;
        }
        .btn-delete:hover {
            background: var(--button-hover-danger);
        }
    }
}

// 삭제 확인 모달
.modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background-color: rgba(0, 0, 0, 0.4);
    z-index: 1050;
    display: flex !important;
    align-items: center;
    justify-content: center;

    .modal-dialog {
        max-width: 500px;
        width: 100%;
        margin: 0 auto;

        .modal-content {
        background-color: #fff;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);

            .modal-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 1rem 1.5rem;
                border-bottom: 1px solid #dee2e6;

                .modal-title {
                    color: var(--button-danger-color);
                    font-size: 1.6rem;
                    font-weight: 600;
                    text-align: center;
                    flex: 1;
                }
            }

            .modal-body {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                padding: 1.25rem 1.5rem;
                gap: 0rem;

                label {
                    color: var(--text-color);
                    font-size: 1rem;
                    font-weight: 500;
                    display: block;
                    margin-bottom: 10px;
                    text-align: center;
                }
                
                input[type="password"] {
                    width: 100%;
                    max-width: 280px;
                }
            }

            .modal-footer {
                display: flex;
                justify-content: flex-end;
                padding: 1rem 1.5rem;
                border-top: 1px solid #dee2e6;

                .btn {
                    margin-top: 0;
                    margin-bottom: 0;
                    padding: 0.5rem 1rem;
                    font-size: 0.85rem;
                    color: #fff;
                    min-width: auto;
                    transition: background-color 0.3s ease, border-color 0.3s ease;

                    &.btn-delete {
                        background-color: var(--button-danger-color);
                        border-color: var(--button-danger-color);

                        &:hover {
                        background-color: var(--button-hover-danger);
                        border-color: var(--button-hover-danger);
                        }
                    }

                    &.btn-secondary {
                        background-color: var(--button-close-color);
                        border-color: var(--button-close-color);

                        &:hover {
                            background-color: var(--button-hover-close);
                            border-color: var(--button-hover-close);
                        }
                    }
                }
            }
        }
    }
}

</style>