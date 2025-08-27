<script setup>
import { computed } from 'vue'
import BookCard from './BookCard.vue'

const props = defineProps({
  books: { type: Array, required: true },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
})
const emit = defineEmits(['select']) // isbn13 올리기

const statusText = computed(() => {
  if (props.loading) return '검색 중…'
  if (props.error) return props.error
  return props.books.length ? `총 ${props.books.length}건` : '검색 결과가 없습니다.'
})
</script>

<template>
  <section class="result-wrap">
    <div class="status">{{ statusText }}</div>
    <div class="grid" v-if="books.length">
      <BookCard
          v-for="(b, i) in books"
          :key="(b.isbn13 || b.bookname || '') + i"
          :book="b"
          @click="emit('select', b.isbn13)"
      />
    </div>
  </section>
</template>

<style scoped>
.result-wrap { max-width: 1100px; margin: 24px auto 80px; padding-inline: 16px; }
.status { margin: 4px 0 20px; color: #6b7480; font-size: 14px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
</style>
