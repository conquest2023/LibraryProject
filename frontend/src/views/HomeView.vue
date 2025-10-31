<template>
  <AppHeader />
  <main>
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          찾고 있던 그 책,<br>가장 가까운 도서관을 알려드려요.
        </h1>
        <p class="hero-subtitle">
          Book Voyager에서 원하는 책을 검색하고, 지금 바로 대출 가능한 도서관을 확인하세요.
        </p>
        <SearchForm @search="handleSearch" class="hero-search-form" />
        <div v-if="recentBooks.length" class="recent-history">
          <h3>📚 최근 검색한 책</h3>
          <ul class="recent-list">
            <li v-for="book in recentBooks" :key="book" @click="handleSearch(book)">
              {{ book }}
            </li>
          </ul>
        </div>
        <p v-else class="recent-empty">{{ recentMessage }}</p>
      </div>
    </section>

    <div v-if="hasSearched" class="container content-section">
      <BookList
          :books="books"
          :status-message="statusMessage"
          :is-loading="isLoading"
      />
    </div>

    <div v-else>
      <section class="container how-it-works">
        <div class="feature-item">
          <div class="feature-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24"><path fill="currentColor" d="M9.5 16q.625 0 1.063-.437T11 14.5t-.437-1.063T9.5 13t-1.063.438T8 14.5t.438 1.063T9.5 16m6-4q.625 0 1.063-.437T17 10.5t-.437-1.063T15.5 9t-1.063.438T14 10.5t.438 1.063T15.5 12M12 22q-2.075 0-3.9-.788t-3.175-2.137q-1.35-1.35-2.137-3.175T2 12q0-2.075.788-3.9t2.137-3.175q1.35-1.35 3.175-2.137T12 2q2.075 0 3.9.788t3.175 2.137q1.35 1.35 2.137 3.175T22 12q0-2.075-.788-3.9t-2.137-3.175q-1.35-1.35-3.175-2.137T12 2Zm0 18q1.55 0 2.975-.587t2.4-1.613q.975-.975 1.613-2.4T19.6 12q0-1.55-.587-2.975t-1.613-2.4q-.975-.975-2.4-1.613T12 4.4q-1.55 0-2.975.588T6.625 6.6q-.975.975-1.613 2.4T4.4 12q0 1.55.588 2.975t1.613 2.4q.975.975 2.4 1.613T12 20Z"/></svg>
          </div>
          <h3>1. 도서 검색</h3>
          <p>읽고 싶은 책을<br>자유롭게 검색하세요.</p>
        </div>
        <div class="connector"></div>
        <div class="feature-item">
          <div class="feature-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24"><path fill="currentColor" d="M12 12q-.825 0-1.412-.587T10 10q0-.825.588-1.412T12 8q.825 0 1.413.588T14 10q0 .825-.587 1.413T12 12m0 10q-4.025-3.425-6.012-6.362T4 10.2q0-3.75 2.413-5.975T12 2q3.175 0 5.588 2.225T20 10.2q0 2.5-1.987 5.438T12 22"/></svg>
          </div>
          <h3>2. 도서관 확인</h3>
          <p>가장 가까운 대출 가능<br>도서관 위치를 확인합니다.</p>
        </div>
        <div class="connector"></div>
        <div class="feature-item">
          <div class="feature-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24"><path fill="currentColor" d="M5.5 22q-.65 0-1.075-.425T4 20.5q0-.65.425-1.075T5.5 19q.65 0 1.075.425T7 20.5q0 .65-.425 1.075T5.5 22m13 0q-.65 0-1.075-.425T17 20.5q0-.65.425-1.075T18.5 19q.65 0 1.075.425T20 20.5q0 .65-.425 1.075T18.5 22M4.45 17L3 4h18l-1.6 11.2q-.125.875-.825 1.488T17 17zM17 15q.275 0 .475-.113t.3-.337L19 6H5l1.55 9zM3 2h2.2l1.6 11.2q.05.3.263.5t.487.2h8.9q.275 0 .475-.113t.3-.337L19 4h2l-3 13H8.1L7 16l-4-9H1V2z"/></svg>
          </div>

          <h3>3. 방문 대출</h3>
          <p>도서관에 방문하여<br>책을 바로 대출하세요.</p>
        </div>

      </section>








<!--      <section class="container content-section new-arrivals">-->
<!--        <div class="section-header">-->
<!--          <h2>오늘 들어온 신간 도서</h2>-->
<!--          <p>가장 먼저 만나보는 따끈따끈한 새 책들입니다.</p>-->
<!--        </div>-->
<!--        <BookList-->
<!--            :books="newBooks"-->
<!--            :is-loading="isNewBooksLoading"-->
<!--            status-message=""-->
<!--        />-->
<!--      </section>-->

<!--      <section class="container content-section">-->
<!--        <div class="section-header">-->
<!--          <h2>요즘 주목받는 도서</h2>-->
<!--          <p>전국 도서관에서 가장 많이 찾고 있는 책들을 만나보세요.</p>-->
<!--        </div>-->
<!--        <BookList-->
<!--            :books="popularBooks"-->
<!--            :is-loading="isPopularLoading"-->
<!--            status-message=""-->
<!--        />-->
<!--      </section>-->
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import AppHeader from '@/components/AppHeader.vue';
import SearchForm from '@/components/SearchForm.vue';
import BookList from '@/components/BookList.vue';

const hasSearched = ref(false);

// 검색 관련 상태
const books = ref([]);
const statusMessage = ref('');
const isLoading = ref(false);

// "오늘의 신간" 관련 상태
const newBooks = ref([]);
const isNewBooksLoading = ref(true);

// 인기 도서 관련 상태
const popularBooks = ref([]);
const isPopularLoading = ref(true);
const recentBooks = ref([]);
const recentMessage = ref('');

// 📚 최근 검색한 책 가져오기
const fetchHistory = async () => {
  try {
    const res = await fetch('/api/history');
    if (!res.ok) throw new Error('히스토리를 불러올 수 없습니다.');
    const data = await res.json();

    if (Array.isArray(data.books)) {
      recentBooks.value = data.books;
      recentMessage.value = '';
    } else {
      recentBooks.value = [];
      recentMessage.value = data.books; // "최근 검색하신 책이 없습니다"
    }
  } catch (error) {
    console.error(error);
    recentMessage.value = '최근 검색 기록을 가져오는 중 오류가 발생했습니다.';
  }
};

// 책 검색 함수
const doSearch = async (keyword) => {
  if (!keyword) return;

  hasSearched.value = true;
  isLoading.value = true;
  statusMessage.value = '검색 중…';
  books.value = [];
  try {
    const url = `/api/search/book?title=${encodeURIComponent(keyword)}`;
    const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
    if (!res.ok) throw new Error('서버 응답 오류');
    const data = await res.json();
    books.value = data.books || [];
    statusMessage.value = `총 ${books.value.length}건의 검색 결과가 있습니다.`;
  } catch (error) {
    statusMessage.value = '검색 중 오류가 발생했습니다.';
  } finally {
    isLoading.value = false;
  }
};

const handleSearch = (query) => {
  doSearch(query);
};

// "오늘의 신간" 목록을 가져오는 함수
const fetchNewBooks = async () => {
  isNewBooksLoading.value = true;
  try {
    const res = await fetch('/api/increase');
    if (!res.ok) throw new Error('신간 도서 목록을 가져오지 못했습니다.');
    const data = await res.json();
    newBooks.value = data.books || [];
  } catch (error) {
    console.error('신간 도서 로딩 실패:', error);
  } finally {
    isNewBooksLoading.value = false;
  }
};

// 전국 인기 도서 목록을 가져오는 함수
const fetchPopularBooks = async () => {
  isPopularLoading.value = true;
  try {
    const res = await fetch('/api/books/popular');
    if (!res.ok) throw new Error('인기 도서 목록을 가져오지 못했습니다.');
    const data = await res.json();
    popularBooks.value = data.books || [];
  } catch (error) {
    console.error('인기 도서 로딩 실패:', error);
  } finally {
    isPopularLoading.value = false;
  }
};

onMounted(() => {
  const initialQuery = new URLSearchParams(location.search).get('title');
  if (initialQuery) {
    doSearch(initialQuery);
  } else {
    // 검색어가 없을 때, 신간 도서와 인기 도서를 모두 불러옵니다.
    fetchNewBooks();
    fetchPopularBooks();
    fetchHistory();

  }
});
</script>

<style scoped>
/* Hero Section */
.recent-history {
  margin-top: 2rem;
  text-align: center;
}

.recent-history h3 {
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 0.8rem;
}

.recent-list {
  display: flex;
  justify-content: center;
  gap: 0.8rem;
  flex-wrap: wrap;
  list-style: none;
  padding: 0;
}

.recent-list li {
  background-color: #eef1f5;
  border-radius: 20px;
  padding: 0.5rem 1rem;
  cursor: pointer;
  transition: 0.2s;
}

.recent-list li:hover {
  background-color: var(--primary-color);
  color: #fff;
}

.recent-empty {
  margin-top: 1rem;
  color: var(--text-color-secondary);
  font-size: 0.95rem;
}
.hero-section {
  background-color: var(--card-background);
  padding: 5rem 1.5rem; /* 👈 현재 이 값 때문에 수직으로 많은 공간을 차지합니다 */
  text-align: center;
  border-bottom: 1px solid var(--border-color);
}
.hero-content {
  max-width: 700px;
  margin: 0 auto;
}
.hero-title {
  font-size: 3rem;
  font-weight: 800;
  line-height: 1.3;
  margin-bottom: 1rem;
  color: var(--text-color);
}
.hero-subtitle {
  font-size: 1.1rem;
  color: var(--text-color-secondary);
  margin-bottom: 2.5rem;
}
:deep(.hero-search-form .intro-text) { display: none; }
:deep(.hero-search-form) { padding: 0; }

/* How it Works Section */
.how-it-works {
  display: grid;
  grid-template-columns: 1fr;
  align-items: flex-start;
  gap: 2rem;
  padding: 4rem 1.5rem;
  text-align: center;
  background-color: #f8f9fa;
  border-bottom: 1px solid var(--border-color);
}
@media(min-width: 768px) {
  .how-it-works {
    grid-template-columns: 1fr auto 1fr auto 1fr;
  }
}
.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}
.feature-icon {
  display: inline-flex;
  padding: 1rem;
  background-color: #eef1f5;
  border-radius: 50%;
  color: var(--primary-color);
}
.feature-item h3 {
  font-size: 1.25rem;
  font-weight: 600;
}
.feature-item p {
  color: var(--text-color-secondary);
  line-height: 1.5;
  min-height: 4.5em;
}
.connector {
  display: none;
}
@media(min-width: 768px) {
  .connector {
    display: block;
    width: 100%;
    height: 2px;
    background: repeating-linear-gradient(90deg,var(--border-color) 0,var(--border-color) 4px,transparent 0,transparent 8px);
    margin-top: 2.5rem;
  }
}

/* Content Section */
.container {
  max-width: 1200px;
  margin: 0 auto;
}
.content-section {
  padding: 4rem 1.5rem;
}

/* "오늘의 신간" 섹션 */
.new-arrivals {
  background-color: var(--card-background);
  border-bottom: 1px solid var(--border-color);
}

.section-header {
  text-align: center;
  margin-bottom: 2.5rem;
}
.section-header h2 {
  font-size: 2rem;
  font-weight: 700;
}
.section-header p {
  color: var(--text-color-secondary);
  margin-top: 0.5rem;
}
</style>