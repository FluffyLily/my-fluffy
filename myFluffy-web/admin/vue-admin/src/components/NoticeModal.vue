<template>
    <div v-if="show" class="modal-overlay">
      <div class="modal-content modal-content-details">
        <div class="modal-header">
          <h2 class="modal-title">공지 내용</h2>
          <button class="btn-close" @click="closeModal"></button>
        </div>
        <div class="modal-body details-view">
          <div class="modal-field">
            <h3>{{ notice.title }}</h3>
            <p class="notice-content">{{ notice.content }}</p>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeModal">닫기</button>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  
  const props = defineProps({
    show: Boolean,
    notice: {
      type: Object,
      default: () => ({ title: '', content: '' })
    }
  });
  
  const emits = defineEmits(['close']);
  
  const closeModal = () => {
    emits('close');
  };
  </script>
  
  <style scoped lang="scss">
// 모달 스타일
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 10px;
  width: 500px;
  max-width: 90%;
  padding: 20px;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.3);

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    border-bottom: 1px solid #ddd;

    .modal-title {
      font-size: 1.8rem;
      font-weight: 600;
      color: var(--sun-honey);
      flex: 1;
      text-align: center;
    }
  }

  .modal-body {
    padding: 16px 0;
    display: flex;
    flex-direction: column;
    gap: 12px;

    .modal-field {
      display: flex;
      flex-direction: column;
      gap: 6px;

      label {
        font-weight: 500;
        font-size: 14px;
      }

      input,
      textarea {
        padding: 10px;
        border: 1px solid #ddd;
        border-radius: 6px;
        font-size: 14px;
        background-color: white;
        outline: none;
        transition: border 0.2s;

        &:focus {
          border-color: var(--button-search-color);
        }
      }

      textarea {
        resize: vertical;
        min-height: 150px;
      }
    }
  }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding: 12px 20px;
    gap: 10px;
    border-top: 1px solid #ddd;

    button {
      padding: 8px 16px;
      border: none;
      border-radius: 6px;
      font-weight: bold;
      cursor: pointer;
      transition: background-color 0.2s;
    }

    .submit-btn {
      background-color: var(--button-add-color);
      color: white;
    }

    .cancel-btn {
      background-color: var(--button-close-color);
      color: white;
    }

    button:hover {
      opacity: 0.9;
    }
  }
}

.modal-content-details {
  max-width: 600px;
  padding: 20px;
}

.modal-body.details-view {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  gap: 12px;
  max-height: 70vh;
  overflow-y: auto;
  padding: 40px 24px;
}

.modal-field h3 {
  font-size: 1.3rem;
  font-weight: bold;
  color: var(--text-color);
  margin-bottom: 10px;
  text-align: left;
}

.notice-content {
  font-size: 1rem;
  color: var(--text-color);
  white-space: pre-wrap;
  line-height: 1.5;
  max-height: 60vh;
  overflow-y: auto;
  text-align: left;
}
  </style>