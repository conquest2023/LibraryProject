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
      <header class="page-header">
        <button class="back-button" @click="goToHome" aria-label="목록으로 가기">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
          <span>목록으로</span>
        </button>
      </header>

      <div class="book-main-info">
        <div class="cover-image">
          <img :src="book.bookImageURL || fallbackImage" :alt="book.bookname + ' 표지'">
        </div>
        <div class="text-info">
          <div class="book-tags" v-if="book.kdcName">
            <span class="tag-item">{{ book.kdcName }}</span>
          </div>
          <h1 class="book-title">{{ book.bookname }}</h1>
          <p class="book-meta">
            {{ book.authors || '저자 미상' }} 지음
          </p>

          <div class="button-group">
            <button class="action-button">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path></svg>
              <span>관심 도서</span>
            </button>
          </div>
        </div>
      </div>

      <section class="book-section availability-section">
        <div class="section-header">
          <h2>내 주변 대출 가능 도서관</h2>
          <p>이 책을 소장하고 있는 가장 가까운 도서관을 찾아보세요.</p>
        </div>

        <div v-if="!isSearchingLibs && nearbyLibraries.length === 0 && !searchError" class="availability-cta">
          <button @click="findNearbyLibraries" class="action-button large">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
            <span>위치 확인하고 찾아보기</span>
          </button>
        </div>

        <div v-if="isSearchingLibs" class="loading-state-small">
          <div class="spinner"></div>
          <p>주변 도서관을 찾고 있습니다...</p>
        </div>

        <div v-if="!isSearchingLibs && nearbyLibraries.length > 0" class="availability-results">
          <ul class="library-list">
            <li v-for="lib in nearbyLibraries.slice(0, 3)" :key="lib.libCode" class="library-item">
              <div class="lib-info">
                <span class="lib-name">{{ lib.libName }}</span>
                <span class="lib-distance">약 {{ lib.distanceKm.toFixed(1) }}km</span>
              </div>
              <a :href="`https://map.kakao.com/link/to/${lib.libName},${lib.latitude},${lib.longitude}`" target="_blank" class="lib-map-link">지도</a>
            </li>
          </ul>
          <button @click="openMapModal" class="view-all-map-button">전체 지도 보기</button>
        </div>

        <div v-if="!isSearchingLibs && (searchError || (searchedAndNoResult))" class="no-content">
          <p>{{ searchError || '주변에 대출 가능한 도서관이 없습니다.' }}</p>
        </div>
      </section>

      <section class="book-section">
        <h2>책 소개</h2>
        <p v-if="book.description" class="description-text">{{ book.description }}</p>
        <p v-else class="no-content">제공되는 책 소개가 없습니다.</p>
      </section>

      <section class="book-section">
        <h2>상세 정보</h2>
        <dl class="detail-list">
          <div class="detail-item"><dt>저자</dt><dd>{{ book.authors || '-' }}</dd></div>
          <div class="detail-item"><dt>출판사</dt><dd>{{ book.publisher || '-' }}</dd></div>
          <div class="detail-item"><dt>발행년도</dt><dd>{{ book.publication_year || '-' }}</dd></div>
          <div class="detail-item"><dt>ISBN</dt><dd>{{ book.isbn13 || '-' }}</dd></div>
          <div class="detail-item" v-if="book.kdcCode"><dt>분류기호</dt><dd>{{ book.kdcCode }} ({{ book.kdcName }})</dd></div>
        </dl>
      </section>
    </div>

    <RelatedBooks
        v-if="book && recoBooks.length > 0"
        :related="recoBooks"
        @select="handleRelatedBookSelect"
    />

    <LibraryMapModal
        v-if="isMapModalVisible"
        :isbn="book.isbn13"
        :libraries="nearbyLibraries"
        :user-position="userPosition"
        @close="closeMapModal"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import RelatedBooks from '@/components/RelatedBooks.vue';
import LibraryMapModal from '@/components/LibraryMapModal.vue';

const route = useRoute();
const router = useRouter();
const book = ref(null);
const recoBooks = ref([]);
const isLoading = ref(true);
const error = ref(null);
const fallbackImage = 'data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22220%22 height=%22308%22 fill=%22%23e9ecef%22><rect width=%22100%25%22 height=%22100%25%22/><text x=%2250%25%22 y=%2250%25%22 font-family=%22sans-serif%22 font-size=%2224%22 fill=%22%23adb5bd%22 text-anchor=%22middle%22 dominant-baseline=%22middle%22>표지 없음</text></svg>';

// --- 모달 및 도서관 검색 관련 상태 변수 ---
const isMapModalVisible = ref(false);
const isSearchingLibs = ref(false);
const nearbyLibraries = ref([]);
const searchError = ref('');
const userPosition = ref(null);
const searchedAndNoResult = ref(false);

const goToHome = () => router.push('/');
const openMapModal = () => { isMapModalVisible.value = true; };
const closeMapModal = () => { isMapModalVisible.value = false; };

// --- 도서관 검색 로직 ---
const findNearbyLibraries = () => {
  if (!navigator.geolocation) {
    searchError.value = '이 브라우저에서는 위치 정보를 지원하지 않습니다.';
    return;
  }

  isSearchingLibs.value = true;
  searchError.value = '';
  searchedAndNoResult.value = false;
  nearbyLibraries.value = [];

  navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const { latitude, longitude } = pos.coords;
        userPosition.value = { lat: latitude, lon: longitude };

        try {
          const res = await fetch('/api/location', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ latitude, longitude, isbn: book.value.isbn13 })
          });
          if (!res.ok) throw new Error('서버에서 도서관 정보를 가져오지 못했습니다.');

          const data = await res.json();
          const libs = Array.isArray(data.nearest) ? data.nearest : [];

          // 거리순으로 정렬
          libs.sort((a, b) => a.distanceKm - b.distanceKm);
          nearbyLibraries.value = libs;

          if (libs.length === 0) {
            searchedAndNoResult.value = true;
          }

        } catch (err) {
          searchError.value = err.message;
        } finally {
          isSearchingLibs.value = false;
        }
      },
      (err) => {
        let msg = '위치 정보를 가져오는 데 실패했습니다.';
        if (err.code === 1) msg = '위치 정보 접근이 거부되었습니다. 브라우저 설정을 확인해주세요.';
        searchError.value = msg;
        isSearchingLibs.value = false;
      }
  );
};

// --- 책 정보 로딩 로직 ---
const fetchBookDetail = async (isbn) => {
  try {
    const res = await fetch(`/api/detail/book?isbn=${isbn}`);
    if (!res.ok) throw new Error('책 상세 정보 불러오기 실패');
    book.value = await res.json();
  } catch (err) {
    error.value = "책 상세 정보를 불러오지 못했습니다.";
    book.value = null;
  }
};
const fetchRecommendations = async (isbn) => {
  try {
    const res = await fetch(`/api/recommend/book?isbn=${isbn}`);
    if (!res.ok) throw new Error('연관 도서 불러오기 실패');
    const data = await res.json();
    recoBooks.value = data.books || [];
  } catch (err) {
    recoBooks.value = [];
  }
};

const loadBookData = async (isbn) => {
  isLoading.value = true;
  error.value = null;
  book.value = null;
  recoBooks.value = [];
  // 페이지 이동 시 도서관 정보 초기화
  isSearchingLibs.value = false;
  nearbyLibraries.value = [];
  searchError.value = '';
  searchedAndNoResult.value = false;

  try {
    await Promise.all([ fetchBookDetail(isbn), fetchRecommendations(isbn) ]);
  } finally {
    isLoading.value = false;
  }
};

const handleRelatedBookSelect = (isbn) => {
  if (isbn && isbn !== route.params.isbn) {
    router.push({ name: 'bookDetail', params: { isbn } });
  }
};

onMounted(() => {
  const isbn = route.params.isbn;
  if (isbn) loadBookData(isbn);
  else {
    error.value = "ISBN 정보가 없습니다.";
    isLoading.value = false;
  }
});

watch(() => route.params.isbn, (newIsbn) => {
  if (newIsbn) {
    loadBookData(newIsbn);
  }
});
</script>

<style scoped>
/* 이전 답변에서 제공한 상세 페이지 스타일과 동일합니다. */
/* ... (스타일 코드 생략 없이 모두 포함) ... */
.detail-page-container {
  display: grid;
  grid-template-columns: 1fr;
  gap: 2rem;
  width: 90%;
  max-width: 1280px;
  margin: 2rem auto;
  padding: 0 1rem;
}

@media (min-width: 992px) {
  .detail-page-container {
    grid-template-columns: minmax(0, 3fr) minmax(0, 1fr);
  }
}

.loading-state, .error-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 4rem 0;
  color: var(--text-color-secondary);
}

.book-content-wrap {
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
  padding: 2rem;
}

.page-header {
  margin-bottom: 2rem;
}

.back-button {
  background: none;
  border: 1px solid var(--border-color);
  border-radius: 30px;
  padding: 0.5rem 1rem;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-color-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}
.back-button:hover {
  background-color: var(--background-color);
  border-color: #c9d1d9;
}

.book-main-info {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  align-items: center;
  margin-bottom: 3rem;
  text-align: center;
}

@media (min-width: 768px) {
  .book-main-info {
    flex-direction: row;
    align-items: flex-start;
    text-align: left;
  }
}

.cover-image {
  flex-shrink: 0;
  width: 220px;
  border-radius: var(--border-radius);
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}
.cover-image img { display: block; width: 100%; }

.text-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}
@media (min-width: 768px) {
  .text-info { align-items: flex-start; }
}

.book-tags { margin-bottom: 0.75rem; }
.tag-item {
  display: inline-flex;
  padding: 0.25rem 0.75rem;
  background-color: #eef1f5;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  color: #6b7480;
}

.book-title {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 0.5rem;
  color: var(--text-color);
}
.book-meta {
  font-size: 1.1rem;
  color: var(--text-color-secondary);
  margin-bottom: 1.5rem;
}

.button-group {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.action-button {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background-color: var(--primary-color);
  color: white;
  border: none;
  padding: 0.75rem 1.25rem;
  border-radius: var(--border-radius);
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.action-button:hover {
  background-color: var(--primary-color-dark);
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 112, 243, 0.2);
}

.book-section {
  padding-top: 2rem;
  margin-top: 2rem;
  border-top: 1px solid var(--border-color);
}
.book-section h2 {
  font-size: 1.4rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
}
.description-text {
  line-height: 1.8;
  color: #495057;
}
.no-content {
  color: var(--text-color-secondary);
  font-style: italic;
  text-align: center;
  padding: 1rem 0;
}

.detail-list {
  display: grid;
  gap: 1rem;
}
.detail-item {
  display: grid;
  grid-template-columns: 100px 1fr;
  font-size: 0.95rem;
}
.detail-item dt {
  font-weight: 600;
  color: var(--text-color-secondary);
}
.detail-item dd {
  color: var(--text-color);
}

.availability-section {
  background-color: var(--background-color);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  padding: 2rem;
  text-align: center;
}
.section-header {
  margin-bottom: 1.5rem;
}
.section-header h2 {
  font-size: 1.4rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}
.section-header p {
  color: var(--text-color-secondary);
  font-size: 1rem;
}
.action-button.large {
  padding: 0.8rem 1.5rem;
  font-size: 1.1rem;
}
.loading-state-small {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 1rem 0;
  color: var(--text-color-secondary);
}
.spinner {
  width: 36px; height: 36px; border: 4px solid var(--border-color);
  border-top-color: var(--primary-color); border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.library-list {
  list-style: none;
  padding: 0;
  margin-bottom: 1.5rem;
}
.library-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
}
.library-item:last-child {
  border-bottom: none;
}
.lib-info {
  text-align: left;
}
.lib-name {
  font-weight: 600;
  color: var(--text-color);
}
.lib-distance {
  display: block;
  font-size: 0.9rem;
  color: var(--text-color-secondary);
  margin-top: 0.25rem;
}
.lib-map-link {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--primary-color);
  text-decoration: none;
  border: 1px solid var(--border-color);
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  transition: all 0.2s;
  white-space: nowrap;
}
.lib-map-link:hover {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}
.view-all-map-button {
  background: none;
  border: 1px solid var(--border-color);
  color: var(--text-color);
  padding: 0.6rem 1.2rem;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.2s;
}
.view-all-map-button:hover {
  background-color: var(--background-color);
  border-color: #c9d1d9;
}
</style>