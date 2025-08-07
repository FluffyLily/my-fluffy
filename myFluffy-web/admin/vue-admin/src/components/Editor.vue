<template>
  <div class="post-editor">
    <!-- form-options 상단 -->
    <div class="form-options">
      <div class="form-row row-options">
        <!-- 게시판 선택 -->
        <div class="form-group">
          <label>게시판 선택</label>
          <select v-model="newPost.boardId" class="form-control">
            <option :value="null">게시판을 선택하세요</option>
            <option v-for="board in boards" :key="board.boardId" :value="board.boardId">
              {{ board.boardName }}
            </option>
          </select>
        </div>
        <!-- 노출 여부 -->
        <div class="form-group">
          <label>게시글 노출 여부</label>
          <div>
            <label style="margin-right: 10px;">
              <input
                type="checkbox"
                :checked="newPost.isVisible"
                @change="newPost.isVisible = true"
              />
              <span class="checkbox-label">노출</span>
            </label>
            <label>
              <input
                type="checkbox"
                :checked="!newPost.isVisible"
                @change="newPost.isVisible = false"
              />
              <span class="checkbox-label">비노출</span>
            </label>
          </div>
        </div>
        <!-- 상단 고정 여부 -->
        <div class="form-group">
          <label>상단 고정 여부</label>
          <div>
            <input type="checkbox" v-model="newPost.isPinned" />
          </div>
        </div>
      </div>
    </div>

    <!-- 게시글 제목 입력 -->
    <div class="form-group">
      <label for="post-title"></label>
      <input
        id="post-title"
        type="text"
        v-model="newPost.title"
        placeholder="게시글 제목을 입력하세요"
        class="form-control"
      />
    </div>

    <!-- 게시글 내용 에디터 -->
    <div class="form-group">
      <label for="post-content"></label>
      <ckeditor
        v-if="editor"
        id="post-content"
        :editor="editor"
        v-model="newPost.content"
        :config="editorConfig"
        @ready="onEditorReady"
      />
      <div id="word-count" class="word-count"></div>
      <p v-if="isUploading" style="color: var(--secondary-color); font-size: 0.9rem;">이미지 업로드 중입니다...</p>
      <p v-if="uploadError" style="color: red; font-size: 0.9rem;">{{ uploadError }}</p>
    </div>

    <!-- form-options 하단 -->
    <div class="form-options">
      <div class="form-row">
        <!-- 게시글 태그 -->
        <div class="form-group">
          <label>게시글 태그</label>
          <input
            type="text"
            v-model="tagInput"
            @compositionstart="handleCompositionStart"
            @compositionend="handleCompositionEnd"
            @keydown="handleKeyDown"
            :disabled="isTagLimitReached"
            :placeholder="isTagLimitReached ? '태그는 3개까지 입력 가능' : '태그 입력 후 Enter'"
            class="form-control"
          />
          <!-- 태그 추천 기능 -->
          <ul v-if="tagSuggestions.length" class="tag-suggestions">
            <li v-for="tag in tagSuggestions" :key="tag" @click="selectTag(tag)">
              #{{ tag }}
            </li>
          </ul>
          <div class="selected-tags" v-if="newPost.postCategory.length">
            <span class="tag" v-for="(tag, index) in newPost.postCategory" :key="tag">
              #{{ tag }}
              <span class="remove-tag" @click="removeTag(index)">x</span>
            </span>
          </div>
        </div>
        <!-- 상단 고정 우선순위 -->
        <div class="form-group">
          <label>상단 고정 우선순위</label>
          <select v-model="newPost.sortOrder" class="form-control" :disabled="!newPost.isPinned">
            <option :value="null">우선순위</option>
            <option v-for="n in 3" :key="n" :value="n">{{ n }}순위</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 취소/작성/수정/삭제하기 버튼 그룹 -->
    <div class="d-flex justify-content-center gap-2 mt-3">
      <button @click="cancelEdit" class="btn btn-warning">
        취소하기
      </button>
      <button @click="writePost" :disabled="!canSubmit" class="btn btn-primary">
        {{ props.postId ? '수정하기' : '작성하기' }}
      </button>
      <button v-if="canDeletePost" @click="confirmDeletePost(newPost.postId, newPost.title)" class="btn btn-danger">
        삭제하기
      </button>
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
            <input type="password" v-model="deletePassword" class="form-control mb-2" @keyup.enter="deletePassword && deletePost()"/>
            <div v-if="deleteError" class="text-danger mb-3">{{ deleteError }}</div>
          </div>
          <div class="text-danger mb-3">
            <small>⚠️ 이 작업은 되돌릴 수 없습니다.</small>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-danger" @click="deletePost">삭제</button>
          <button class="btn btn-secondary" @click="closeDeleteModal">닫기</button>
        </div>
      </div>
    </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import Fuse from 'fuse.js';
import apiClient from '../api/axios.js';
import { useAuthStore } from '../stores/auth.js';
import { useRouter, useRoute } from 'vue-router';
import { hasAnyRole } from '../util/roleUtils.js';
import { useToast } from 'vue-toastification';

const props = defineProps({
  postId: { type: Number, default: null },
  boardId: { type: Number, default: null},
  boardName: { type: String, default: ''}
});

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();
const toast = useToast();

// 이미지 업로드 관련
const isUploading = ref(false);
const uploadError = ref('');
const uploadedImages = ref([]);

// 게시판 목록과 관련 데이터들
const boards = ref([]);

const newPost = reactive({
  boardId: props.boardId,
  boardCategoryId: null,
  title: '',
  content: '',
  sortOrder: null,
  isVisible: true,
  isPinned: false,
  authorId: authStore.userId,
  images: [],
  postCategory: [],
  postCategoryString: '',
  createdAt: new Date().toISOString(),
  createdBy: authStore.userId,
  createdByName: authStore.loginId,
  updatedAt: new Date().toISOString(),
  updatedBy: authStore.userId,
  updatedByName: authStore.loginId
})

let initialPostData = null;

// ckeditor
const editor = ref(null)

const MAX_FILE_SIZE_MB = 3;

// 태그 관련 데이터
const allTags = ref([]);
const tagSuggestions = ref([]);
const tagInput = ref('');
const isComposing = ref(false);

// fuse 설정
const fuse = computed(() => new Fuse(allTags.value, {
  includeScore: true, // 결과에 유사도 점수를 포함
  threshold: 0.4, // 0~1 사이: 수치가 낮을 수록 엄격하게 허용함
}));

const isTagLimitReached = computed(() => newPost.postCategory.length >= 3);

const selectedPostId = ref(null);
const selectedPostTitle = ref('');

const canSubmit = computed(() => {
  return newPost.boardId && newPost.title.trim() !== '' && newPost.content.trim() !== '';
});

// 현재 관리자의 게시글 삭제 권한 확인 (수정 모드일 때만 노출)
const canDeletePost = computed(() =>
  props.postId && (
    hasAnyRole(authStore.roleId, ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN']) ||
    authStore.userId === newPost.createdBy
  )
);

const deletePassword = ref('');
const deleteError = ref('');
const showDeletePostModal = ref(false);

const editorConfig = reactive({
  toolbar: {
    shouldNotGroupWhenFull: true,
    items: [
      'heading',
      '|',
      'bold', 'italic', 'underline', 'strikethrough', 'subscript', 'superscript',
      'fontColor', 'fontBackgroundColor', 'highlight',
      '|',
      'alignment',
      'link',
      'bulletedList', 'numberedList', 'outdent', 'indent',
      '|',
      'imageUpload', 'insertTable', 'blockQuote', 'codeBlock',
      '|',
      'mediaEmbed', 'specialCharacters', 'horizontalLine',
      '|',
      'undo', 'redo',
      'findAndReplace',
      'tableProperties', 'tableCellProperties'
    ]
  },
  image: {
    toolbar: [
      'imageStyle:side',
      '|',
      'imageTextAlternative'
    ],
    resizeUnit: '%',
    styles: ['side']
  },
  extraPlugins: [CustomUploadAdapterPlugin],
  table: {
    contentToolbar: [
      'tableColumn',
      'tableRow',
      'mergeTableCells'
    ]
  },
  horizontalLine: {},
  wordCount: {
    container: '#word-count'
  },
  language: 'ko',
  licenseKey: 'GPL'
})

// 함수
class MyUploadAdapter {
  constructor(loader) {
    this.loader = loader;
  }

  upload() {
    return this.loader.file.then(file => {
      return new Promise((resolve, reject) => {
        if (file.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
          uploadError.value = `이미지 용량은 ${MAX_FILE_SIZE_MB}MB 이하만 업로드 가능합니다.`;
          reject(uploadError.value);
          return;
        }

        isUploading.value = true;
        uploadError.value = '';

        const data = new FormData();
        data.append('upload', file);

        apiClient.post('/post/upload-image', data, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }).then(response => {
          isUploading.value = false;
          resolve({
            default: `/uploads/images/${response.data.default}`
          });
          // 업로드된 이미지 URL 저장
          uploadedImages.value.push(`/uploads/images/${response.data.default}`);
        }).catch(err => {
          isUploading.value = false;
          uploadError.value = '이미지 업로드 실패';
          reject(uploadError.value);
        });
      });
    });
  }

  abort() {}
}

function CustomUploadAdapterPlugin(editor) {
  editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
    return new MyUploadAdapter(loader);
  };
}

// 에디터 내 이미지 URL 추출하기
const extractImageUrls = (htmlContent) => {
  const urls = [];
  const imgTagRegex = /<img[^>]+src="([^">]+)"/g;
  let match;
  while ((match = imgTagRegex.exec(htmlContent)) !== null) {
    urls.push(match[1]);
  }
  return urls;
};

const handleCompositionStart = () => { isComposing.value = true; };
const handleCompositionEnd = () => { isComposing.value = false; };

const handleKeyDown = (event) => {
  if (event.key === 'Enter' && !isComposing.value) {
    addTag();
    event.preventDefault();
  }
};

// 태그 추천 + 자동 완성 
const selectTag = (tag) => {
  if (!newPost.postCategory.includes(tag)) {
    newPost.postCategory.push(tag);
  }
  tagInput.value = '';
  tagSuggestions.value = [];
};

// 단어 입력 후 # 태그 자동 입력
const addTag = () => {
  if (newPost.postCategory.length >= 3) return;
  const value = tagInput.value.trim().replace(/^#/, '');
  if (value && !newPost.postCategory.includes(value)) {
    newPost.postCategory.push(value);
  }
  tagInput.value = '';
  tagSuggestions.value = [];
};

const removeTag = (index) => {
  newPost.postCategory.splice(index, 1);
};

const fetchTags = async () => {
  try {
    const response = await apiClient.get('/post/tags');
    allTags.value = response.data;
  } catch (e) {
    console.error('태그 불러오기 실패:', e);
  }
};

const onEditorReady = (editorInstance) => {
  const wordCountPlugin = editorInstance.plugins.get('WordCount');
  
  if (wordCountPlugin) {
    wordCountPlugin.on('update', (event, stats) => {
      const container = document.querySelector('#word-count');
      if (container) {
        container.textContent = `글자 수: ${stats.characters}`;
      }
    });
  }
};

// 게시판 목록 가져오기
const fetchBoards = async () => {
  try {
    const response = await apiClient.get('/board/list');
    boards.value = response.data;
  } catch (e) {
    console.error('게시판 목록 불러오기 실패:', e);
  }
};

// 게시글 세부내용 조회
const fetchPost = async () => {
  if (!props.postId) return;
  try {
    const response = await apiClient.get(`/post/detail/${props.postId}`);
    Object.assign(newPost, response.data);
    // 이미지 정보 유지
    if (response.data.images?.length) {
      newPost.images = response.data.images;
    }
    // 태그 복원
    if (!newPost.postCategory && newPost.postCategoryString) {
      newPost.postCategory = newPost.postCategoryString
        .split(',')
        .map(s => s.trim())
        .filter(Boolean);
    } else if (!newPost.postCategory) {
      newPost.postCategory = [];
    }
    // 게시글 데이터 초기값 저장 (수정 감지용)
    initialPostData = JSON.parse(JSON.stringify(newPost));
  } catch (error) {
    console.error('게시글 세부내용 조회 실패: ', error);
  }
}

const cancelEdit = () => {
    router.push({
      name: 'PostManagement',
      query: {
        ...route.query,
        focusedPostId: newPost.postId
      }
    })
  }

// 게시글 작성 & 수정
const writePost = async () => {
  if (!canSubmit.value) return;

  const board = boards.value.find(b => b.boardId === newPost.boardId);
  newPost.boardCategoryId = board ? board.boardCategoryId : null;

  if (!newPost.isPinned) {
    newPost.sortOrder = null;
  }

  // 이미지 URL 추출 후, 본문 내 이미지가 하나라도 있으면 images를 덮어씀
  const currentImages = extractImageUrls(newPost.content);
  if (currentImages.length > 0) {
    newPost.images = currentImages.map(url => ({ imageUrl: url }));
  }

  if (props.postId) {
    if (!confirm("게시글을 수정하시겠습니까?")) return;

    try {
      await apiClient.put(`/post/update/${props.postId}`, newPost);
      
      // 본문에 사용된 이미지와 업로드된 이미지 비교하여 미사용 이미지 삭제
      const usedImages = extractImageUrls(newPost.content);
      const unusedImages = uploadedImages.value.filter((url) => !usedImages.includes(url));

      if (unusedImages.length > 0) {
        try {
          await apiClient.post('/post/cleanup-temp', unusedImages);
        } catch (cleanupErr) {
          console.warn('사용되지 않은 이미지 삭제 실패:', cleanupErr);
        }
      }
      uploadedImages.value = [];

      const query = { edited: 'true' };
      if (route.query.filteredByBoard === 'true' && route.query.boardId) {
        query.boardId = newPost.boardId;
        query.boardName = board?.boardName ?? '';
        query.filteredByBoard = 'true';
      }

      router.push({
        name: 'PostDetail',
        params: { postId: props.postId },
        query,
        replace: true
      });

    } catch (error) {
      console.error('게시글 수정 실패: ', error);
    }
  } else {
    if (!confirm("새로운 게시글을 작성하시겠습니까?")) return;

    try {
      const response = await apiClient.post('/post/write', newPost);
      // 본문에 사용된 이미지와 업로드된 이미지 비교하여 미사용 이미지 삭제
      const usedImages = extractImageUrls(newPost.content);
      const unusedImages = uploadedImages.value.filter((url) => !usedImages.includes(url));
      if (unusedImages.length > 0) {
        try {
          await apiClient.post('/post/cleanup-temp', unusedImages);
        } catch (cleanupErr) {
          console.warn('사용되지 않은 이미지 삭제 실패:', cleanupErr);
        }
      }
      uploadedImages.value = [];

      const query = { edited: 'true' };
      if (route.query.filteredByBoard === 'true' && route.query.boardId) {
        query.boardId = newPost.boardId;
        query.boardName = board?.boardName ?? '';
        query.filteredByBoard = 'true';
      }

      router.push({
        name: 'PostDetail',
        params: { postId: response.data.postId },
        query,
        replace: true
      });

    } catch (error) {
      console.error('게시글 작성 실패: ', error);
    }
  }
}

const confirmDeletePost = (postId, postTitle) => {
  selectedPostId.value = postId;
  selectedPostTitle.value = postTitle;
  showDeletePostModal.value = true;
}

const closeDeleteModal = () => {
  showDeletePostModal.value = false;
  deletePassword.value = '';
};

// 게시글 삭제하기
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
        router.push({
          name: 'PostManagement',
          query: route.query.filteredByBoard === 'true' && route.query.boardId
            ? {
                boardId: route.query.boardId,
                boardName: route.query.boardName,
                filteredByBoard: 'true',
              }
            : {}
        });
    } else {
        deleteError.value = '비밀번호가 일치하지 않습니다.';
    }
  } catch (error) {
    console.error('게시글을 삭제하지 못함: ', error);
    deleteError.value = '게시글 삭제 중 문제가 발생했습니다. 다시 시도해 주세요.';
  }
}

onMounted(async () => {
  editor.value = window.ClassicEditor
  await fetchBoards();
  if (props.postId) {
    await fetchPost();
  }
  await fetchTags();

  // 작성/수정 성공 후 edited 쿼리 감지 & 토스트 후 쿼리 제거
  if (route.query.edited) {
    toast.success('게시글이 성공적으로 작성되었습니다.');

    const { edited, ...restQuery } = route.query;
    router.replace({ query: restQuery });
  }
})

// 태그 추천
watch(tagInput, (val) => {
  const keyword = val.replace('#', '').toLowerCase();
  tagSuggestions.value = fuse.value.search(keyword).map(result => result.item);
});

// 관리자 삭제 모달 비밀번호 에러 메시지 초기화
watch(showDeletePostModal, (newVal) => {
  if (!newVal) {
      deleteError.value = '';
  }
});

// 사용자가 비밀번호 입력을 다시 시작하면 에러 메시지 제거
watch(deletePassword, () => {
    deleteError.value = '';
});
</script>

<style scoped lang="scss">
.post-editor {
  max-width: 800px;
  margin: 20px auto;
  background-color: var(--background-color);
  padding: 2rem;
  border: 3px solid var(--card-border-pink);
  border-radius: 15px;
  box-shadow: 0px 8px 15px var(--shadow-color);
}

.form-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
}

.form-row .form-group {
  flex: 1;
}
.row-options {
margin-bottom: 0.25rem;
}

.form-group {
  margin-bottom: 0.5rem;
  position: relative;
}

.form-row + .form-group {
  margin-top: 0rem;
}

label {
  font-weight: 500;
  color: var(--secondary-color);
  margin-bottom: 0.5rem;
  display: inline-block;
}

.form-control {
  width: 100%;
  padding: 0.75rem;
  font-size: 1rem;
  border: 1.5px solid var(--selected-color);
  border-radius: 8px;
  background-color: var(--card-bg);
  color: var(--text-color);
  transition: border-color 0.3s ease, background-color 0.3s ease;
}

.form-control:focus {
  border-color: var(--primary-color);
  background-color: #fff;
  outline: none;
}

select.form-control {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20width='10'%20height='5'%20viewBox='0%200%2010%205'%3E%3Cpath%20fill='%23666'%20d='M0%200l5%205%205-5z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  background-size: 0.65rem auto;
  cursor: pointer;
}

.form-row .form-control {
  padding: 0.3rem;
  font-size: 0.85rem;
}

// 버튼 공통 스타일
.btn {
  margin-top: 1rem;
  padding: 8px 14px;
  font-size: 14px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease, border-color 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 80px;
  color: #fff;
}

.btn-primary {
  background-color: var(--button-add-color);
}
.btn-primary:hover {
  background-color: var(--button-hover-add);
}

.btn-danger {
  background-color: var(--button-danger-color);
}
.btn-danger:hover {
  background-color: var(--button-hover-danger);
}

.btn-warning {
  background-color: var(--button-warning-color);
}
.btn-warning:hover {
  background-color: var(--button-hover-warning);
}

.btn:disabled,
.btn[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-options {
  border: 2px solid var(--card-border-mint);
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
  background-color: var(--card-bg-mint);
}

.checkbox-label {
  color: #333;
  font-weight: normal;
  margin-left: 6px;
}

.word-count {
  color: var(--secondary-color);
}

.tag-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  z-index: 10;
  list-style: none;
  margin-top: 0.2rem;
  padding: 0.5rem;
  border: 1px solid var(--selected-color);
  background-color: white;
  border-radius: 5px;
  max-height: 150px;
  overflow-y: auto;

  li {
    padding: 0.15rem 0.5rem;
    font-size: 0.85rem;
    cursor: pointer;
  }

  li:hover {
    display: inline-block;
    background-color: var(--selected-color);
    color: white;
    border-radius: 3px;
  }
}

.selected-tags {
  margin-top: 0.3rem;
  .tag {
    display: inline-block;
    background: var(--peach-sherbet);
    padding: 2px 8px;
    margin-left: 4px;
    margin-right: 4px;
    color: var(--text-color);
    font-size: 0.85rem;
    border-radius: 14px;

    .remove-tag {
      margin-left: 3px;
      cursor: pointer;
      font-weight: bold;
      font-size: 0.90rem;
    }
  }
}

// === Modal===
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
          padding: 10px 18px;
          font-size: 14px;
          min-width: auto;
          transition: background-color 0.3s ease, border-color 0.3s ease;

          &.btn-primary {
            background-color: var(--button-add-color);
            border-color: var(--button-add-color);

            &:hover {
              background-color: var(--button-hover-add);
              border-color: var(--button-hover-add);
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

.image-style-full {
  display: block;
  margin-left: auto;
  margin-right: auto;
  max-width: 100%;
}
</style>

<style lang="scss">

.ck-editor__editable_inline {
  min-height: 300px;
  background-color: #fff;
  padding: 0.8em;
  font-size: 1rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.ck-toolbar,
.ck-toolbar .ck-button__label,
.ck-toolbar .ck-dropdown__button .ck-button__label {
  color: black !important;
}

.ck-toolbar .ck-icon {
  fill: black !important;
}

</style>
