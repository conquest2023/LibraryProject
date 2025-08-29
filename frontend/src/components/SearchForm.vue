<template>
  <section class="search-section">
    <div class="intro-text">
      <h2>도서 정보 검색</h2>
      <p>찾고 싶은 책의 제목, 저자, 출판사를 검색해 보세요.</p>
    </div>
    <form class="search-box" role="search" @submit.prevent="handleSubmit">
      <div class="input-wrapper">
        <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"></circle>
          <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
        </svg>
        <input
            id="q"
            v-model="query"
            type="search"
            class="search-input"
            placeholder="예: 개발자를 위한 레디스"
            autocomplete="off"
            enterkeyhint="search"
            autofocus
        />
      </div>
      <button class="search-btn" type="submit">검색</button>
    </form>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// v-model을 통해 입력값과 동기화될 반응형 데이터
const query = ref('');

// 부모 컴포넌트로 이벤트를 보내기 위한 emit 함수 정의
const emit = defineEmits(['search']);

// 폼 제출 시 실행될 함수
const handleSubmit = () => {
  const searchTerm = query.value.trim();
  if (!searchTerm) return;

  // 'search' 이벤트를 발생시켜 검색어를 부모에게 전달
  emit('search', searchTerm);

  // 브라우저 주소창에 검색어 반영 (새로고침/뒤로가기 대비)
  const url = new URL(location.href);
  url.searchParams.set('title', searchTerm);
  history.replaceState({}, '', url);
};

// 컴포넌트가 마운트될 때, URL에 검색어가 있으면 자동으로 입력창에 채우기
onMounted(() => {
  const initialQuery = new URLSearchParams(location.search).get('title');
  if (initialQuery) {
    query.value = initialQuery;
  }
});
</script>

<style scoped>
.search-section {
  text-align: center;
  padding: 3rem 0;
  margin-bottom: 2rem;
}
.intro-text {
  margin-bottom: 2rem;
}
.intro-text h2 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}
.intro-text p {
  font-size: 1.1rem;
  color: var(--text-color-secondary);
}
.search-box {
  display: flex;
  gap: 0.75rem;
  max-width: 700px;
  margin: 0 auto;
}
.input-wrapper {
  position: relative;
  flex-grow: 1;
}
.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-color-secondary);
  pointer-events: none;
}
.search-input {
  width: 100%;
  height: 52px;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  outline: none;
  padding: 0 1rem 0 3rem; /* 아이콘 공간 확보 */
  font-size: 1rem;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  background-color: var(--card-background);
}
.search-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(0, 112, 243, 0.2);
}
.search-btn {
  height: 52px;
  border: none;
  border-radius: var(--border-radius);
  background-color: var(--primary-color);
  color: white;
  font-size: 1rem;
  font-weight: 600;
  padding: 0 1.5rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}
.search-btn:hover {
  background-color: var(--primary-color-dark);
}
</style>