<template>
  <div class="detail-page-container">
    <div v-if="isLoading" class="loading-state">
      <p>책 정보를 불러오는 중입니다...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <p>오류 발생: {{ error }}</p>
      <p>잠시 후 다시 시도해 주세요.</p>
    </div>

    <div v-else-if="book" class="book-content-wrap">
      <div class="book-header">
        <button class="back-button" @click="goToHome" aria-label="뒤로 가기">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 18 9 12 15 6"></polyline>
          </svg>
        </button>
        <div class="book-main-info">
          <div class="cover-image">
            <img :src="book.bookImageURL || fallbackImage" :alt="book.bookname + ' 표지'">
          </div>
          <div class="text-info">
            <h1 class="book-title">{{ book.bookname }}</h1>
            <p class="book-meta">
              {{ book.authors || '저자 미상' }} 지음 | {{ book.publisher || '출판사 미상' }} | {{ book.publication_year || '-' }}
            </p>
            <div class="book-tags" v-if="book.kdcName">
              <span class="tag-item">{{ book.kdcName }}</span>
              <span class="tag-item" v-if="book.kdcCode"> {{ book.kdcCode }}</span>
            </div>
            <button class="action-button">관심 도서</button>
            <button @click="openMapModal" class="action-button-icon" title="가까운 도서관 찾기">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              <circle cx="12" cy="10" r="3"></circle>
              </svg>
            </button>
            <LibraryMapModal
                v-if="isMapModalVisible"
                :isbn="book.isbn13"
                @close="closeMapModal"
            />
          </div>
        </div>
      </div>

      <section class="book-section description-section">
        <h2>책 소개</h2>
        <p v-if="book.description">{{ book.description }}</p>
        <p v-else class="no-description">제공되는 책 소개가 없습니다.</p>
      </section>
    </div>

    <RelatedBooks
        v-if="book && recoBooks.length > 0"
        :related="recoBooks"
        @select="handleRelatedBookSelect"
    />

    <div v-else-if="!book && !isLoading && !error" class="no-book-found">
      <p>해당 책 정보를 찾을 수 없습니다.</p>
      <button @click="$router.push('/')">검색 페이지로 돌아가기</button>
    </div>
  </div>
</template>

<script setup>

import { ref, onMounted, watch } from 'vue';
import {createRouter as $router, useRoute, useRouter} from 'vue-router';
import RelatedBooks from '@/components/RelatedBooks.vue'; // 연관 도서 컴포넌트 임포트
import LibraryMapModal from '@/components/LibraryMapModal.vue';
const route = useRoute();
const router = useRouter();

const book = ref(null);
const recoBooks = ref([]);
const isLoading = ref(true);
const error = ref(null);
const isMapModalVisible = ref(false);
const fallbackImage = 'data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22280%22 fill=%22%23e0e0e0%22><rect width=%22100%25%22 height=%22100%25%22/><text x=%2250%25%22 y=%2250%25%22 font-family=%22sans-serif%22 font-size=%2224%22 fill=%22%23757575%22 text-anchor=%22middle%22 dominant-baseline=%22middle%22>표지 없음</text></svg>';

const goToHome = () => {
  router.push('/');
};
const openMapModal = () => {
  isMapModalVisible.value = true;
};

const closeMapModal = () => {
  isMapModalVisible.value = false;
};
const fetchBookDetail = async (isbn) => {
  try {
    const res = await fetch(`/api/detail/book?isbn=${isbn}`);
    if (!res.ok) throw new Error('책 상세 정보 불러오기 실패');
    book.value = await res.json();
  } catch (err) {
    console.error("책 상세 정보 fetch 에러:", err);
    error.value = "책 상세 정보를 불러오지 못했습니다.";
    book.value = null; // 실패 시 book 초기화
  }
};

const fetchRecommendations = async (isbn) => {
  try {
    const res = await fetch(`/api/recommend/book?isbn=${isbn}`);
    if (!res.ok) throw new Error('연관 도서 불러오기 실패');
    const data = await res.json();
    recoBooks.value = data.books || [];
  } catch (err) {
    console.error("연관 도서 fetch 에러:", err);
    // 연관 도서 로딩 실패는 주 정보 로딩에 방해되지 않도록 error.value에 직접 반영하지 않음
    recoBooks.value = [];
  }
};

// ISBN에 따라 모든 데이터를 불러오는 함수
const loadBookData = async (isbn) => {
  isLoading.value = true;
  error.value = null; // 새로운 검색 시작 시 에러 초기화
  book.value = null;
  recoBooks.value = [];

  try {
    await Promise.all([
      fetchBookDetail(isbn),
      fetchRecommendations(isbn)
    ]);
  } finally {
    isLoading.value = false;
  }
};

// 연관 도서 클릭 시 페이지 이동 (새로운 ISBN으로 상세 정보 로드)
const handleRelatedBookSelect = (isbn) => {
  if (isbn && isbn !== route.params.isbn) { // 현재 페이지의 책이 아닌 경우에만 이동
    router.push({name: 'bookDetail', params: {isbn: isbn}});
  }
};

// 컴포넌트 마운트 시 초기 데이터 로드
onMounted(() => {
  const isbn = route.params.isbn;
  if (isbn) {
    loadBookData(isbn);
  } else {
    error.value = "ISBN 정보가 없습니다.";
    isLoading.value = false;
  }
});

// URL 파라미터(isbn)가 변경될 때마다 데이터 다시 로드
watch(() => route.params.isbn, (newIsbn) => {
  if (newIsbn) {
    loadBookData(newIsbn);
  } else {
    error.value = "ISBN 정보가 없습니다.";
    isLoading.value = false;
  }
});
</script>

<style scoped>
.button-group {
  margin-top: 15px;
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: center; /* 모바일 가운데 정렬 */
}
.action-button-icon {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 44px;
  height: 44px;
  background-color: #f0f2f5; /* 다른 색으로 구분 */
  color: #4a4a4a;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background: #555;
}


.detail-page-container {
  display: grid;
  grid-template-columns: 1fr; /* 기본적으로 1컬럼 */
  gap: 30px; /* 간격 살짝 증가 */

  width: 100%; /* 화면 너비의 90%를 사용하도록 변경 */
  max-width: 2000px; /* 최대 너비를 1400px로 늘림 */

  margin: 40px auto;
  padding: 0 20px;
  min-height: calc(100vh - 80px);
}

@media (min-width: 992px) {
  /* 넓은 화면에서 2컬럼 레이아웃 */
  .detail-page-container {
    grid-template-columns: 3fr 1fr; /* 메인 콘텐츠 3, 사이드바 1 */
  }

  .book-content-wrap {
    grid-column: 1 / 2;
  }

  .side {
    grid-column: 2 / 3;
  }
}

/* 로딩/에러/책 없음 상태 메시지 */
.loading-state, .error-state, .no-book-found {
  grid-column: 1 / -1; /* 전체 너비를 차지 */
  text-align: center;
  padding: 50px 0;
  font-size: 1.1rem;
  color: #555;
}

.error-state p {
  color: #d32f2f;
  font-weight: bold;
}

.no-book-found button {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  margin-top: 20px;
}

/* 메인 콘텐츠 랩퍼 */
.book-content-wrap {
  background: #ffffff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

/* 뒤로 가기 버튼 */
.back-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 10px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  color: #555;
  font-size: 1rem;
  transition: color 0.2s;
}

.back-button:hover {
  color: #007bff;
}

.back-button svg {
  margin-right: 5px;
}

/* 책 주요 정보 섹션 (커버 이미지, 제목, 메타 정보 등) */
.book-main-info {
  display: flex;
  flex-direction: column; /* 모바일 우선: 세로 정렬 */
  gap: 30px;
  align-items: center; /* 이미지와 텍스트 가운데 정렬 */
  margin-bottom: 40px;
}

@media (min-width: 768px) {
  .book-main-info {
    flex-direction: row; /* 데스크톱: 가로 정렬 */
    align-items: flex-start; /* 상단 정렬 */
  }
}

.cover-image {
  flex-shrink: 0; /* 이미지 크기 유지 */
  width: 200px;
  height: 280px;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  background-color: #f9f9f9;
  display: flex;
  justify-content: center;
  align-items: center;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.text-info {
  flex-grow: 1;
  color: #333; /* 글씨색 기본값 설정 */
  text-align: center; /* 모바일 기본 가운데 정렬 */
}

@media (min-width: 768px) {
  .text-info {
    text-align: left; /* 데스크톱 왼쪽 정렬 */
  }
}

.book-title {
  font-size: 2.2rem;
  font-weight: 700;
  margin: 0 0 10px;
  line-height: 1.3;
  color: #222; /* 어두운 글씨색 */
}

.book-meta {
  font-size: 1.1rem;
  color: #555;
  margin-bottom: 15px;
}

/* 태그/분류 스타일 */
.book-tags {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center; /* 모바일 가운데 정렬 */
  gap: 8px;
}

@media (min-width: 768px) {
  .book-tags {
    justify-content: flex-start; /* 데스크톱 왼쪽 정렬 */
  }
}

.tag-item {
  display: inline-flex;
  padding: 6px 12px;
  background-color: #eef1f5;
  border-radius: 20px;
  font-size: 0.85rem;
  color: #6b7480;
  white-space: nowrap;
}

/* 액션 버튼 */
.action-button {
  background-color: #4a4a4a; /* 어두운 회색 배경 */
  color: #ffffff;
  border: none;
  padding: 12px 25px;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.1s ease;
  margin-top: 15px;
}

.action-button:hover {
  background-color: #333333;
  transform: translateY(-1px);
}

.action-button:active {
  transform: translateY(0);
}

/* 일반 섹션 스타일 (책 소개, 상세 정보 등) */
.book-section {
  background: #ffffff;
  padding: 25px;
  border-top: 1px solid #eee; /* 섹션 구분선 */
  margin-top: 30px;
}

.book-section:first-of-type {
  border-top: none;
  margin-top: 0;
}

/* 첫 섹션은 구분선 없음 */

.book-section h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #222;
  margin-bottom: 20px;
}

.description-section p {
  line-height: 1.8;
  color: #444;
  font-size: 1rem;
}

.no-description {
  color: #888;
  font-style: italic;
}

.detail-info-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.detail-info-section li {
  margin-bottom: 10px;
  font-size: 0.95rem;
  color: #444;
}

.detail-info-section strong {
  color: #333;
}
</style>