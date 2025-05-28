import { ref } from 'vue'

/**
 * 한글 등 조합형 문자 입력 중 Enter 이벤트 중복 호출을 방지하는 커스텀 훅
 * @param {Function} onEnterCallback - 엔터 키 입력 시 실행할 콜백 함수
 * @returns {Object} 이벤트 핸들러들
 */
export function useCompositionInput(onEnterCallback) {
  const isComposing = ref(false)

  const handleCompositionStart = () => {
    isComposing.value = true
  }

  const handleCompositionEnd = () => {
    isComposing.value = false
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !isComposing.value) {
      onEnterCallback()
      e.preventDefault()
    }
  }

  return {
    isComposing,
    handleCompositionStart,
    handleCompositionEnd,
    handleKeyDown
  }
}