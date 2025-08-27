<script setup>
const props = defineProps({
  detail: { type: Object, default: null },     // 선택된 책 (표시는 안 해도 OK)
  related: { type: Array, default: () => [] }, // { bookname, bookImageURL, isbn13 }
})
const emit = defineEmits(['select']) // 연관 도서 클릭 시 isbn13 올리기
const fallback = 'data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2280%22 height=%22110%22></svg>'
</script>

<template>
  <aside class="side">
    <div class="h2">연관 도서</div>
    <div class="rel-list" v-if="related.length">
      <button
          class="rel-item"
          v-for="(r, i) in related"
          :key="(r.isbn13 || r.bookname || '') + i"
          @click="emit('select', r.isbn13)"
          :title="r.bookname"
      >
        <img :src="r.bookImageURL || fallback" alt="표지" />
        <div class="name">{{ r.bookname }}</div>
      </button>
    </div>
    <div v-else class="empty">연관 도서가 없습니다.</div>
  </aside>
</template>

<style scoped>
.side{
  position: sticky; top:16px;
  max-height: calc(100vh - 32px);
  overflow: auto;             /* ← 사이드 내부 스크롤 */
  background:#fff; border:1px solid #eef1f5; border-radius:16px; padding:12px;
}
.h2{ font-weight:700; margin:6px 4px 10px; }
.rel-list{ display:grid; grid-template-columns: 1fr; gap:10px; }
.rel-item{
  display:flex; gap:10px; align-items:center; width:100%;
  border:1px solid #eef1f5; border-radius:12px; padding:8px; background:#fff; cursor:pointer;
  transition: box-shadow .15s ease, transform .05s ease; text-align:left;
}
.rel-item img{ width:42px; height:58px; object-fit:cover; border-radius:6px; border:1px solid #eef1f5; flex:0 0 auto; }
.name{
  font-size:13px; color:#333; line-height:1.35; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;
}
.empty{ color:#8a94a3; font-size:13px; padding:6px; text-align:center; }
</style>

