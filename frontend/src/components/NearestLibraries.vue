<template>
  <section v-if="locationError || libraries.length > 0" class="book-section">
    <h2>내 주변 도서관</h2>

    <div v-if="locationError" class="status-message error">
      {{ locationError }}
    </div>

    <div v-else-if="libraries.length > 0">
      <ul class="library-list">
        <li v-for="lib in top3" :key="lib.libCode">
          <div class="lib-info">
            <span class="lib-name">{{ lib.libName }}</span>
            <span class="lib-distance">{{ lib.distanceKm?.toFixed(1) }}km</span>
          </div>
          <div class="lib-status">
              <span class="status-badge available">
                대출가능
              </span>
          </div>
        </li>
      </ul>
      <button @click="$emit('open-map')" class="view-all-button">
        <span>전체 지도에서 보기</span>
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path><polyline points="15 3 21 3 21 9"></polyline><line x1="10" y1="14" x2="21" y2="3"></line></svg>
      </button>
    </div>

    <div v-else class="status-message">
      이 책을 소장한 가까운 도서관이 없습니다.
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  libraries: {
    type: Array,
    required: true,
  },
  locationError: {
    type: String,
    default: null,
  },
});

defineEmits(['open-map']);

// 전달받은 도서관 목록에서 상위 3개만 잘라서 사용
const top3 = computed(() => props.libraries.slice(0, 3));
</script>

<style scoped>
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
.status-message {
  color: var(--text-color-secondary);
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: var(--border-radius);
  text-align: center;
}
.status-message.error {
  background-color: #fff5f5;
  color: #e53e3e;
}

.library-list {
  list-style: none;
  padding: 0;
  margin: 0 0 1.5rem 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.library-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  transition: all 0.2s ease;
}
.library-list li:hover {
  border-color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.lib-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.lib-name {
  font-weight: 600;
  color: var(--text-color);
}
.lib-distance {
  font-size: 0.9rem;
  color: var(--text-color-secondary);
}
.status-badge {
  font-size: 0.85rem;
  padding: 0.25rem 0.6rem;
  border-radius: 20px;
  font-weight: 500;
  white-space: nowrap;
}
.status-badge.available {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}
.status-badge.unavailable {
  background-color: #fff1f0;
  color: #f5222d;
  border: 1px solid #ffa39e;
}

.view-all-button {
  width: 100%;
  background: none;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  padding: 0.75rem 1rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-color-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}
.view-all-button:hover {
  background-color: #f8f9fa;
  color: var(--primary-color);
  border-color: var(--primary-color);
}
</style>