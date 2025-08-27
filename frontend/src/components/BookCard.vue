<template>
  <router-link :to="`/book/${book.isbn13}`" class="card-link">
    <div class="card">
      <img :src="imageUrl" alt="표지" />
      <div>
        <div class="title">{{ book.bookname || '(제목 없음)' }}</div>
        <div class="meta">{{ metaInfo }}</div>
        <div v-if="book.isbn13" class="isbn">ISBN13: {{ book.isbn13 }}</div>
        <div v-if="book.description" class="desc">{{ book.description }}</div>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue';

// 부모로부터 book 객체를 props로 받음
const props = defineProps({
  book: {
    type: Object,
    required: true,
  },
});

// 이미지 URL 처리 (이미지가 없으면 빈 SVG 플레이스홀더 사용)
const imageUrl = computed(() => {
  return (props.book.bookImageURL && props.book.bookImageURL.trim())
      ? props.book.bookImageURL
      : 'data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2288%22 height=%22120%22></svg>';
});

// 저자, 출판사, 출판년도 정보 조합
const metaInfo = computed(() => {
  const authors = props.book.authors || '저자 미상';
  const publisher = props.book.publisher || '출판사 미상';
  const year = props.book.publication_year || '-';
  return `${authors} · ${publisher} · ${year}`;
});
</script>

<style scoped>
/* 카드 관련 스타일만 이곳에 둡니다. */
.card {
  border:1px solid #e8ecf0; border-radius:16px; padding:14px; background:#fff;
  display:grid; grid-template-columns:88px 1fr; gap:12px; align-items:start;
}
.card img {
  width:88px; height:120px; object-fit:cover; border-radius:8px; border:1px solid #eef1f5; background:#f8fafc;
}
.title { font-weight:700; font-size:16px; margin:2px 0 6px; }
.meta { color:#6b7480; font-size:13px; line-height:1.45; }
.isbn { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace; font-size:12px; color:#8a94a3; }
.desc { color:#5a6372; font-size:13px; margin-top:6px; max-height:4.2em; overflow:hidden; display:-webkit-box; -webkit-line-clamp:3; -webkit-box-orient:vertical; }
</style>