<template>
  <router-link :to="`/book/${book.isbn13}`" class="card-link">
    <div class="card">
      <div class="cover-image">
        <img :src="imageUrl" :alt="book.bookname + ' 표지'" loading="lazy" />
      </div>
      <div class="card-content">
        <h3 class="title">{{ book.bookname || '(제목 없음)' }}</h3>
        <p class="meta">{{ metaInfo }}</p>
        <p v-if="book.description" class="desc">{{ book.description }}</p>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import {computed} from 'vue';

const props = defineProps({
  book: {type: Object, required: true},
});

const imageUrl = computed(() => {
  return (props.book.bookImageURL && props.book.bookImageURL.trim())
      ? props.book.bookImageURL
      : 'data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22100%22 height=%22140%22></svg>';
});

const metaInfo = computed(() => {
  const authors = props.book.authors || '저자 미상';
  const publisher = props.book.publisher || '출판사 미상';
  return `${authors} · ${publisher}`;
});
</script>

<style scoped>
.card-link {
  text-decoration: none;
  color: inherit;
  display: block;
}

.card {
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: var(--card-hover-shadow);
}

.cover-image {
  width: 100%;
  aspect-ratio: 4 / 2; /* 가로가 더 긴 이미지 비율 */
  background-color: #f0f0f0;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.cover-image img {
  width: auto;
  height: 100%;
  object-fit: contain;
}

.card-content {
  padding: 1rem 1.25rem;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  /* 두 줄 이상일 때 말줄임표 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta {
  font-size: 0.85rem;
  color: var(--text-color-secondary);
  margin-bottom: 0.75rem;
}

.desc {
  font-size: 0.9rem;
  color: var(--text-color-secondary);
  line-height: 1.5;
  margin-top: auto; /* 설명을 카드 하단에 위치시키기 */
  padding-top: 0.5rem;
  /* 세 줄 이상일 때 말줄임표 */
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>