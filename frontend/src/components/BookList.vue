<template>
  <section class="result-section">

    <Transition name="slide-fade">
      <div v-if="showErrorToast" class="error-toast">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="8" x2="12" y2="12"></line>
          <line x1="12" y1="16" x2="12.01" y2="16"></line>
        </svg>
        <span>{{ errorMessage }}</span>
        <button @click="dismissError" class="close-btn">&times;</button>
      </div>
    </Transition>

    <div v-if="isLoading" class="loading-state">
      <div class="spinner"></div>
      <p>데이터를 불러오는 중입니다...</p>
    </div>

    <div v-else>
      <div v-if="!isErrorData && statusMessage" class="status">{{ statusMessage }}</div>

      <div v-if="!isErrorData && books.length > 0" class="grid">
        <BookCard
            v-for="book in books"
            :key="book.isbn13 || book.bookname"
            :book="book"
        />
      </div>

      <div v-else-if="!isErrorData && statusMessage" class="empty-state">
        <p>검색어와 일치하는 도서가 없습니다.</p>
        <p class="sub-text">다른 키워드로 검색해 보세요.</p>
      </div>

      <div v-else-if="isErrorData" class="empty-state">
        <div class="maintenance-icon">⚠️</div>
        <p>서비스 점검 중입니다.</p>
        <p class="sub-text">잠시 후 다시 시도해주세요.</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router'; // [1] 라우터 임포트 추가
import BookCard from './BookCard.vue';

const router = useRouter(); // [2] 라우터 사용 설정

const props = defineProps({
  books: { type: Array, required: true },
  statusMessage: { type: String, default: '' },
  isLoading: { type: Boolean, default: false }
});

const isDismissed = ref(false);

const isErrorData = computed(() => {
  if (props.books && props.books.length > 0) {
    const first = props.books[0];
    return first.isError || (first.bookname && first.bookname.includes('지연'));
  }
  return false;
});

const errorMessage = computed(() => {
  if (isErrorData.value) {
    return props.books[0].bookname;
  }
  return '';
});

// 토스트 표시 여부
const showErrorToast = computed(() => {
  return isErrorData.value && !isDismissed.value;
});

const dismissError = () => {
  isDismissed.value = true;

  window.location.href = '/';
};

watch(() => props.isLoading, (newVal) => {
  if (newVal) isDismissed.value = false;
});
</script>

<style scoped>
.result-section {
  min-height: 300px;
  position: relative;
}

/* 에러 토스트 스타일 */
.error-toast {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  z-index: 999;

  display: flex;
  align-items: center;
  gap: 0.75rem;

  background-color: #fff1f0;
  border: 1px solid #ffccc7;
  color: #cf1322;
  padding: 1rem 1.5rem;
  border-radius: 50px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
  font-weight: 600;
  white-space: nowrap;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  color: #ff4d4f;
  cursor: pointer;
  margin-left: 0.5rem;
}

.maintenance-icon {
  font-size: 2rem;
  margin-bottom: 1rem;
}

/* 애니메이션 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.5, 1);
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translate(-50%, -20px);
  opacity: 0;
}

/* 기존 스타일 */
.status {
  margin-bottom: 1.5rem;
  color: var(--text-color-secondary);
  font-size: 0.9rem;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}
.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 4rem 0;
  color: var(--text-color-secondary);
  text-align: center;
}
.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid var(--border-color);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>