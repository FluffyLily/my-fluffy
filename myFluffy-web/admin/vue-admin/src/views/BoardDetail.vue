<template>
    <div class="board-detail-container">
        <div v-if="boardDetail?.boardId" >
            <h2>{{ boardDetail.boardName }} 게시판 정보</h2>
            <div class="board-detail-content">
                <p><strong>카테고리 :</strong> {{ boardDetail.categories.map(c => c.boardCategoryName).join(', ') }}</p>
                <p><strong>등록한 관리자 :</strong> {{ boardDetail.createdByLoginId }}</p>
                <p><strong>등록한 날 :</strong> {{ formatDate(boardDetail.createdAt) }}</p>
                <p><strong>수정한 관리자 :</strong> {{ boardDetail.updatedByLoginId }}</p>
                <p><strong>수정한 날 :</strong> {{ formatDate(boardDetail.updatedAt) }}</p>
                <button class="btn btn-warning me-2" @click="editBoardMenu(boardDetail)">수정</button>
                <button class="btn btn-danger" @click="confirmDeleteBoardMenu(boardDetail.boardId, boardDetail.boardName)">삭제</button>
            </div>
        </div>
    </div>
    <!-- 게시판 메뉴 수정 모달 -->
    <div v-if="showEditMenuModal" class="modal fade show d-flex justify-content-center align-items-center" style="display: block;" tabindex="-1" role="dialog">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">게시판 수정</h5>
                    <button type="button" class="btn-close" @click="showEditMenuModal = false"></button>
                </div>
                <div class="modal-body">
                    <label class="form-label d-block text-start">게시판 이름</label>
                    <input v-model="editMenu.boardName" placeholder="게시판 이름을 입력하세요" class="form-control mb-2"/>
                    <label class="form-label d-block text-start">카테고리</label>
                    <Multiselect v-model="editMenu.boardCategoryId" 
                        :options="allCategories.map(c => ({ value: c.boardCategoryId, label: c.boardCategoryName }))"
                        :close-on-select="false"
                        :multiple="true"
                        placeholder="카테고리 선택"
                        class="mb-2"/>
                    <small v-if="menuEditError" class="text-danger">{{ menuEditError }}</small>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary" @click="updateBoardMenu">수정</button>
                    <button class="btn btn-secondary" @click="showEditMenuModal = false">닫기</button>
                </div>
            </div>
        </div>
    </div>
    <!-- 게시판 메뉴 삭제 모달 -->
    <div v-if="showDeleteMenuModal" class="modal fade show d-flex justify-content-center align-items-center" style="display: block;" tabindex="-1" role="dialog">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">게시판 삭제</h5>
                    <button type="button" class="btn-close" @click="showDeleteMenuModal = false"></button>
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
                    <button class="btn btn-danger" @click="deleteBoardMenu">삭제</button>
                    <button class="btn btn-secondary" @click="showDeleteMenuModal = false">닫기</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useAuthStore } from '../stores/auth.js';
import { format } from 'date-fns';
import apiClient from '../api/axios.js';
import Multiselect from '@vueform/multiselect';

    const boardDetail = defineModel('boardDetail');
    const emit = defineEmits(['boardUpdated'])
    const authStore = useAuthStore();
    // 날짜 포맷팅
    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        return format(new Date(dateString), 'yyyy-MM-dd HH:mm:ss');
    };

    const isBoardUpdated = ref(false);
    const editMenu = ref({
        boardId: null,
        boardName: '',
        boardCategoryId: [],
        updatedBy: authStore.userId,
        updatedAt: new Date().toISOString()
    });
    const menuEditError = ref(null);
    const showEditMenuModal = ref(false);
    const allCategories = ref([]);
    const selectedCategories = ref([]);

    // 게시판 상세 정보 가져오기
    const fetchBoardDetail = async (boardId) => {
        if (!boardId) {
            console.warn("boardId가 없습니다. URL을 확인하세요.");
            return;
        }
        try {
            console.log("Fetching board detail for boardId:", boardId);
            const response = await apiClient.get(`/board/detail/${boardId}`,{ 
                headers: { Authorization: `Bearer ${authStore.accessToken}` }
            });

            if (response.data) {
                Object.assign(boardDetail, response.data);
                console.log("✅ [fetchBoardDetail] - Updated boardDetail:", boardDetail); // ✅ 데이터 업데이트 확인 로그
            }
        } catch (error) {
            console.error('게시판 세부 정보 가져오기 실패:', error);
            deleteError.value = '게시판 정보를 불러오는 데 실패했습니다. 다시 시도해 주세요.';
        }
    };
    watch(boardDetail, (newValue) => {
        console.log("📌 boardDetail 변경 감지: ", newValue);
    });

    // 모든 카테고리 가져오기
    const fetchAllCategories = async () => {
        try {
            const response = await apiClient.get('/board/category', {
                headers: { Authorization: `Bearer ${authStore.accessToken}` }
            });
            allCategories.value = response.data; // 전체 카테고리 목록 저장
        } catch (error) {
            console.error('카테고리 목록 불러오기 실패:', error);
        }
    };

    // 게시판 메뉴 수정 데이터, 모달 세팅
    const editBoardMenu = (boardDetail) => {
        editMenu.value = { 
            boardId: boardDetail.boardId,
            boardName: boardDetail.boardName,
            updatedBy: authStore.userId,
            boardCategoryId: boardDetail.categories.map(c => c.boardCategoryId) // ✅ 기존 카테고리 ID 배열 추출
        };
        selectedCategories.value = boardDetail.categories.map(c => ({ 
            value: c.boardCategoryId,
            label: c.boardCategoryName
        }));
        editMenu.value.boardCategoryId = selectedCategories.value;

        console.log("📌 [editBoardMenu] - 업데이트 데이터: ", editMenu.value);
        console.log("📌 [editBoardMenu] - 기존 선택된 카테고리 값: ", editMenu.value.boardCategoryId);

        menuEditError.value = null;
        showEditMenuModal.value = true;

    };
    // 게시판 수정 사항 업데이트 요청
    const updateBoardMenu = async () => {
        if (!editMenu.value.boardName.trim()) {
            menuEditError.value = '게시판 이름을 입력하세요.';
            return;
        }
        // ✅ boardCategoryId가 배열인지 체크하고, 배열이 아니면 배열로 변환
        const boardCategoryIds = Array.isArray(editMenu.value.boardCategoryId)
            ? editMenu.value.boardCategoryId
            : [editMenu.value.boardCategoryId];

        // ✅ boardCategoryIds를 숫자 배열로 변환 (객체일 경우 value 값 추출)
        const categoryIds = boardCategoryIds.map(c => c?.value ?? c);
        // const categoryIds = editMenu.value.boardCategoryId;
            // ✅ 수정 전 데이터 로그 확인
            console.log("🔍 최종 업데이트 요청 데이터:", {
                boardId: editMenu.value.boardId,
                boardName: editMenu.value.boardName,
                boardCategoryId: categoryIds, // ✅ 숫자 배열로 변환됨
                updatedBy: editMenu.value.updatedBy
            });

            try {
                await apiClient.put(`/board/update/${editMenu.value.boardId}`, {
                    boardId: editMenu.value.boardId,
                    boardName: editMenu.value.boardName,
                    boardCategoryId: categoryIds,
                    updatedBy: editMenu.value.updatedBy
                }, {
                headers: { Authorization: `Bearer ${authStore.accessToken}` }
                });

                showEditMenuModal.value = false;
                emit('boardUpdated');
                isBoardUpdated.value = true;
                
            } catch (error) {
                console.error('게시판 메뉴 수정 실패:', error);
            }
        };

    watch(isBoardUpdated, async (newValue) => {
        if (newValue) {
            console.log("🔄 게시판 정보 업데이트 감지됨, 최신 데이터 불러오기...");
            await fetchBoardDetail(editMenu.value.boardId);
            isBoardUpdated.value = false;
        }
    });

    const deletePassword = ref('');
    const deleteError = ref('');
    const showDeleteMenuModal = ref(false);

    let selectedBoardId = null;
    let selectedBoardName = '';

    // 관리자 삭제 모달 비밀번호 에러 메시지 초기화
    watch(showDeleteMenuModal, (newVal) => {
        if (!newVal) {
            deleteError.value = '';
        }
    });

    // 사용자가 비밀번호 입력을 다시 시작하면 에러 메시지 제거
    watch(deletePassword, () => {
        deleteError.value = '';
    });

    // 게시판 삭제 모달 
    const confirmDeleteBoardMenu = (boardId, boardName) => {
        selectedBoardId = boardId;
        selectedBoardName = boardName;
        console.log("게시판 삭제 모달 [selectedBoardName]: ", selectedBoardName);
        showDeleteMenuModal.value = true;
    };
    
    // 게시판 삭제하기
    const deleteBoardMenu = async () => {
        if (!selectedBoardId) {
            console.error('삭제할 게시판이 선택되지 않았습니다.');
            return;
        }
        if (!deletePassword.value.trim()) {
            deleteError.value = '관리자 비밀번호를 입력하세요.';
            return;
        }
        try {
            const response = await apiClient.post('/admin/verify-password', {
            username: authStore.loginId, 
            password: deletePassword.value 
            }, {
            headers: { Authorization: `Bearer ${authStore.accessToken}` } 
            });
            if (response.data.success) {

                // const boardName = encodeURIComponent(selectedBoardName);
                await apiClient.delete(`/board/delete/${selectedBoardId}`, {
                    headers: { Authorization: `Bearer ${authStore.accessToken}` },
                    params: { 
                    deleterId: authStore.loginId,
                    boardName: selectedBoardName
                    }
                });
                showDeleteMenuModal.value = false;
                deletePassword.value = ''; // 비밀번호 입력 초기화
                emit('boardUpdated');
            } else {
                deleteError.value = '비밀번호가 일치하지 않습니다.';
            }
        } catch (error) {
            console.error('게시판을 삭제하지 못함: ', error);
            deleteError.value = '게시판 삭제 중 문제가 발생했습니다. 다시 시도해 주세요.';
        }
    };

    onMounted(() => {
        console.log("📌 컴포넌트 마운트됨, boardId 확인:", boardDetail.value?.boardId);
        if (boardDetail.value?.boardId) {
            fetchBoardDetail(boardDetail.value.boardId);
        } else {
            console.warn("⚠️ boardId가 없음. 데이터를 불러오지 않음.");
        }
        fetchAllCategories();
    });
</script>

<style lang="scss" scoped>
.board-detail-container {
  flex: 2.5;
  padding: 20px;
  background-color: var(--card-bg);
  border: 2px solid var(--card-border-purple);
  border-radius: 12px;
  box-shadow: 0 0 10px var(--shadow-color);
}
.board-detail-container h2 {
    font-size: 30px;
    font-weight: bold;
    color: var(--card-border-purple);
    margin-bottom: 15px;
}

.board-detail-content {
    flex: 2.5;
    padding: 20px;
    background-color: var(--card-bg);
    border: 2px solid var(--card-bg);
    border-radius: 12px;
    box-shadow: 0 0 10px var(--shadow-color);
}

.btn-warning {
    background-color: var(--button-warning-color);
    border-color: var(--button-warning-color);
    color: white;
}

.btn-warning:hover {
    background-color: var(--button-hover-warning);
    border-color: var(--button-hover-warning);
}

.btn-danger {
    background-color: var(--button-danger-color);
    border-color: var(--button-danger-color);
    color: white;
}

.btn-danger:hover {
    background-color: var(--button-hover-danger);
    border-color: var(--button-hover-danger);
}

.btn-primary {
    background-color: var(--button-danger-color);
    border-color: var(--button-danger-color);
    color: white;
}

.btn-primary:hover {
    background-color: var(--button-hover-danger);
    border-color: var(--button-hover-danger);
}

.btn-secondary {
    background-color: var(--button-warning-color);
    border-color: var(--button-warning-color);
    color: white;
}

.btn-secondary:hover {
    background-color: var(--button-hover-warning);
    border-color: var(--button-hover-warning);
}
</style>