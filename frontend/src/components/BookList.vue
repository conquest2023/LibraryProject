<template>
  <section class="result-section">
    <div v-if="isLoading" class="loading-state">
      <div class="spinner"></div>
      <p>데이터를 불러오는 중입니다...</p>
    </div>

    <div v-else>
      <div class="status">{{ statusMessage }}</div>

      <div v-if="books.length > 0" class="grid">
        <BookCard
            v-for="book in books"
            :key="book.isbn13 || book.bookname"
            :book="book"
        />
      </div>

      <div v-else-if="statusMessage" class="empty-state">
        <p>검색어와 일치하는 도서가 없습니다.</p>
        <p class="sub-text">다른 키워드로 검색해 보세요.</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import BookCard from './BookCard.vue';

defineProps({
  books: { type: Array, required: true },
  statusMessage: { type: String, default: '' },
  isLoading: { type: Boolean, default: false } // isLoading prop 추가
});
</script>

<style scoped>
.result-section {
  min-height: 300px;
}
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
}
.empty-state .sub-text {
  font-size: 0.9rem;
  margin-top: 0.25rem;
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