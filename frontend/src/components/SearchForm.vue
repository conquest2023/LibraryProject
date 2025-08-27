<template>
  <section class="center">
    <div class="search-wrap">
      <form class="search-box" role="search" @submit.prevent="handleSubmit">
        <label for="q" class="sr-only">검색어</label>
        <input
            id="q"
            v-model="query"
            type="search"
            class="search-input"
            placeholder="책 제목, 저자 또는 출판사를 입력하세요"
            autocomplete="off"
            enterkeyhint="search"
            autofocus
        />
        <button class="search-btn" type="submit" aria-label="검색">
          <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <circle cx="11" cy="11" r="7" stroke="#6b7480" stroke-width="2"></circle>
            <line x1="16.65" y1="16.65" x2="21" y2="21" stroke="#6b7480" stroke-width="2" stroke-linecap="round"></line>
          </svg>
          <span class="sr-only">검색</span>
        </button>
      </form>
    </div>
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
/* 검색폼 관련 스타일만 이곳에 둡니다. */
.center { min-height: 60vh; display: flex; align-items: center; justify-content: center; padding: 24px; }
.search-wrap { position: relative; width: min(1100px, 92vw); }
.search-input {
  width: 100%; height: 88px; border: var(--border) solid #e5e8ec; border-radius: var(--radius);
  outline: none; padding: 0 84px 0 28px; font-size: clamp(18px, 2.2vw, 28px);
  transition: box-shadow .15s ease, border-color .15s ease;
}
.search-input::placeholder { color: #a8b0bd; }
.search-box:focus-within .search-input { border-color: #cfd6dd; box-shadow: 0 8px 28px rgba(0,0,0,.06); }
.search-btn {
  position: absolute; right: 16px; top: 50%; transform: translateY(-50%);
  height: 56px; width: 56px; border: 0; border-radius: 50%; background: #eef1f5;
  cursor: pointer; display: inline-flex; align-items: center; justify-content: center;
}
</style>