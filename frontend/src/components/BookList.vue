<template>
  <section class="result-wrap">
    <div class="status">{{ statusMessage }}</div>
    <div class="grid">
      <BookCard
          v-for="book in books"
          :key="book.isbn13 || book.bookname"
          :book="book"
      />
    </div>
  </section>
</template>

<script setup>
import BookCard from './BookCard.vue';

// 부모로부터 책 목록과 상태 메시지를 props로 받음
defineProps({
  books: {
    type: Array,
    required: true,
  },
  statusMessage: {
    type: String,
    default: '',
  },
});
</script>

<style scoped>
/* 결과 리스트 관련 스타일만 이곳에 둡니다. */
.result-wrap { max-width: 1100px; margin: 24px auto 80px; padding-inline: 16px; }
.status { margin: 4px 0 20px; color: #6b7480; font-size: 14px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
</style>