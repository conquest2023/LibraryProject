<template>
  <main>
    <SearchForm @search="handleSearch" />

    <BookList :books="books" :status-message="statusMessage" />
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import SearchForm from '@/components/SearchForm.vue';
import BookList from '@/components/BookList.vue';

// 자식 컴포넌트에 전달할 반응형 데이터
const books = ref([]);
const statusMessage = ref('');

/**
 * 키워드를 받아 백엔드 API를 호출하고, 결과를 상태에 반영하는 함수
 * @param {string} keyword - 검색할 키워드
 */
const doSearch = async (keyword) => {
  if (!keyword) return;

  statusMessage.value = '검색 중…';
  books.value = []; // 이전 결과 초기화

  try {
    // 백엔드의 검색 API 주소
    const url = `/api/search/book?title=${encodeURIComponent(keyword)}`;
    const res = await fetch(url, {headers: {'Accept': 'application/json'}});

    if (!res.ok) {
      throw new Error('서버 응답에 문제가 발생했습니다.');
    }

    const data = await res.json();
    const resultBooks = Array.isArray(data.books) ? data.books : [];

    books.value = resultBooks;
    statusMessage.value = resultBooks.length
        ? `총 ${resultBooks.length}건의 검색 결과가 있습니다.`
        : '검색 결과가 없습니다.';

  } catch (error) {
    console.error('검색 중 오류 발생:', error);
    statusMessage.value = '오류가 발생했습니다. 잠시 후 다시 시도해 주세요.';
  }
};


const handleSearch = (query) => {
  doSearch(query);
};

// 컴포넌트가 처음 마운트될 때 실행
onMounted(() => {
  // 페이지 URL에 'title' 쿼리가 있으면, 그 값으로 자동 검색 실행
  const initialQuery = new URLSearchParams(location.search).get('title');
  if (initialQuery) {
    doSearch(initialQuery);
  }
});
</script>

<style scoped>
main {
  width: 100%;
}
</style>