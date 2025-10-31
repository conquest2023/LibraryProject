<!--<template>-->
<!--  <div-->
<!--      v-if="open"-->
<!--      class="rs-dropdown"-->
<!--      @mousedown.prevent-->
<!--  >-->
<!--    <div class="rs-header">-->
<!--      <span>최근 검색어</span>-->
<!--      <button class="rs-clear" @click="clearAll" :disabled="clearing">-->
<!--        전체삭제-->
<!--      </button>-->
<!--    </div>-->

<!--    <ul class="rs-list" v-if="items.length">-->
<!--      <li-->
<!--          v-for="it in items"-->
<!--          :key="it.book + '-' + it.date"-->
<!--          class="rs-item"-->
<!--          @click="select(it.book)"-->
<!--      >-->
<!--        <span class="rs-left">-->
<!--          <span class="rs-icon">🕘</span>-->
<!--          <span class="rs-text">{{ it.book }}</span>-->
<!--        </span>-->

<!--        <span class="rs-right">-->
<!--          <span class="rs-date">{{ it.date }}</span>-->
<!--          <button-->
<!--              class="rs-del"-->
<!--              title="삭제"-->
<!--              @click.stop="remove(it.book)"-->
<!--              :disabled="deleting[it.book]"-->
<!--          >✕</button>-->
<!--        </span>-->
<!--      </li>-->
<!--    </ul>-->

<!--    <div class="rs-empty" v-else>-->
<!--      최근 검색하신 책이 없습니다-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { ref, watch, onMounted, onBeforeUnmount } from 'vue';-->

<!--const props = defineProps({-->
<!--  open: { type: Boolean, default: false },-->
<!--});-->

<!--const emit = defineEmits(['select', 'close']);-->

<!--const items = ref([]); // [{ book, date }]-->
<!--const deleting = ref({});-->
<!--const clearing = ref(false);-->

<!--async function load() {-->
<!--  try {-->
<!--    const res = await fetch('/api/history', {-->
<!--      headers: { Accept: 'application/json' }-->
<!--    });-->
<!--    const data = await res.json();-->
<!--    console.log(data)-->
<!--    // 백엔드에서 ["책1","책2"] 형식이라고 하셨으므로 날짜는 오늘 기준으로 간단 표기-->
<!--    if (Array.isArray(data.books)) {-->
<!--      const today = new Date();-->
<!--      const fmt = (d) =>-->
<!--          `${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}.`;-->
<!--      items.value = data.books.map((b) => ({ book: b, date: fmt(today) }));-->
<!--    } else {-->
<!--      items.value = [];-->
<!--    }-->
<!--  } catch {-->
<!--    items.value = [];-->
<!--  }-->
<!--}-->

<!--function select(book) {-->
<!--  emit('select', book);-->
<!--  emit('close');-->
<!--}-->

<!--async function remove(book) {-->
<!--  deleting.value[book] = true;-->
<!--  try {-->
<!--    await fetch(`/api/history/ㅁㄴㅇ`, { method: 'DELETE' });-->
<!--  } catch {}-->
<!--  delete deleting.value[book];-->
<!--  items.value = items.value.filter((i) => i.book !== book);-->
<!--  if (!items.value.length) emit('close');-->
<!--}-->

<!--async function clearAll() {-->
<!--  clearing.value = true;-->
<!--  try {-->
<!--    await fetch('/api/history/ㅁㄴㅇ', { method: 'DELETE' });-->
<!--  } catch {}-->
<!--  clearing.value = false;-->
<!--  items.value = [];-->
<!--  emit('close');-->
<!--}-->

<!--// 외부 클릭 시 닫기-->
<!--function onDocClick(e) {-->
<!--  const dropdown = document.querySelector('.rs-dropdown');-->
<!--  if (dropdown && !dropdown.contains(e.target)) emit('close');-->
<!--}-->

<!--watch(() => props.open, (v) => v && load());-->
<!--onMounted(() => document.addEventListener('click', onDocClick));-->
<!--onBeforeUnmount(() => document.removeEventListener('click', onDocClick));-->
<!--</script>-->

<!--<style scoped>-->
<!--.rs-dropdown{-->
<!--  position: absolute;-->
<!--  top: 100%;-->
<!--  left: 0;-->
<!--  right: 0;-->
<!--  margin-top: 8px;-->
<!--  background: #fff;-->
<!--  border: 1px solid var(&#45;&#45;border-color, #e5e7eb);-->
<!--  border-radius: 12px;-->
<!--  box-shadow: 0 10px 30px rgba(0,0,0,0.08);-->
<!--  overflow: hidden;-->
<!--  z-index: 1000;-->
<!--}-->
<!--.rs-header{-->
<!--  display:flex; justify-content:space-between; align-items:center;-->
<!--  padding: 12px 16px; font-weight:600; background:#fafbfc;-->
<!--  border-bottom:1px solid #eef1f5;-->
<!--}-->
<!--.rs-clear{-->
<!--  font-size: 0.9rem; border:none; background:transparent; color:#2563eb; cursor:pointer;-->
<!--}-->
<!--.rs-list{ list-style:none; margin:0; padding:0; max-height: 320px; overflow:auto; }-->
<!--.rs-item{-->
<!--  display:flex; align-items:center; justify-content:space-between;-->
<!--  padding: 12px 16px; cursor:pointer;-->
<!--}-->
<!--.rs-item:hover{ background:#f7f9fb; }-->
<!--.rs-left{ display:flex; align-items:center; gap:10px; min-width:0; }-->
<!--.rs-icon{ opacity:.6; }-->
<!--.rs-text{ white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }-->
<!--.rs-right{ display:flex; align-items:center; gap:12px; }-->
<!--.rs-date{ color:#6b7280; font-size:.9rem; }-->
<!--.rs-del{-->
<!--  width:28px; height:28px; border:none; border-radius:50%;-->
<!--  background:#f1f5f9; cursor:pointer;-->
<!--}-->
<!--.rs-del:hover{ background:#e2e8f0; }-->
<!--.rs-empty{ padding:20px; color:#6b7280; text-align:center; }-->
<!--</style>-->
