<!-- BookToc.vue -->
<template>
  <section class="book-section">
    <h2 class="section-title">목차</h2>

    <div v-if="loading" class="toc-skel">목차 불러오는 중…</div>
    <div v-else-if="visibleItems.length === 0" class="no-content">제공되는 목차가 없습니다.</div>
    <ul v-else class="toc-list" :class="{ 'is-collapsed': !expanded }" ref="tocList">
      <li v-for="(item, idx) in visibleItems" :key="idx" :style="{ paddingLeft: `${item.level * 16}px` }">
        <span class="dot"></span>
        <span class="text">{{ item.text }}</span>
      </li>
    </ul>

    <div v-if="items.length > initialCount" class="toc-actions">
      <button class="btn-toggle" type="button" @click="expanded = !expanded">
        <span>{{ expanded ? '접기' : '펼치기' }}</span>
        <span class="ico" :class="{ up: expanded }">▾</span>
      </button>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// props: 도서 식별용
const props = defineProps({
  bookName: { type: String, required: true }, // 백엔드의 /crawling?bookName=...
  endpoint: { type: String, default: '/crawling' }, // 필요 시 바꿔쓰게
  initialCount: { type: Number, default: 8 }        // 처음에 보여줄 개수
})

const loading = ref(false)
const expanded = ref(false)
const items = ref([]) // [{ text, level }]

const visibleItems = computed(() => {
  if (expanded.value) return items.value
  return items.value.slice(0, props.initialCount)
})

// "__" 개수 → 들여쓰기 레벨 계산 + 라인 정리
function normalizeLine(line) {
  if (!line) return null
  const trimmed = line.replace(/\u00A0/g, ' ').replace(/\s+/g, ' ').trim()

  // "__" 들여쓰기: "__" 묶음 개수로 레벨 추정
  const underscoreMatch = trimmed.match(/^(_+)/)
  const level = underscoreMatch ? Math.floor(underscoreMatch[1].length / 2) : 0
  const text = trimmed.replace(/^_+/, '').trim()

  // 광고성/잡문구 필터 예시(선택)
  if (text.length === 0 || text.length > 500) return null
  return { text, level }
}

async function fetchToc() {
  loading.value = true
  try {
    const res = await fetch(`/api/crawling?bookName=${encodeURIComponent(props.bookName)}`)

    const json = await res.json()
    const lines = Array.isArray(json.result) ? json.result : []

    // 혹시 한 줄에 전부 붙어온 케이스 → 강제 분할(챕터 시작 패턴으로 split)
    const exploded = lines.flatMap(l => {
      if (/\d장|\d+\.\d/.test(l) && l.includes('__')) {
        // "__" 앞/뒤를 분리 + 숫자목차 단서로 분절
        return l.split(/\s(?=(?:\d장|\d+\.\d|__))/g)
      }
      return [l]
    })

    items.value = exploded
        .map(normalizeLine)
        .filter(Boolean)
  } catch (e) {
    console.error(e)
    items.value = []
  } finally {
    loading.value = false
  }
}

onMounted(fetchToc)
</script>

<style scoped>
.book-section { margin-top: 24px; }
.section-title { font-weight: 700; font-size: 18px; margin-bottom: 12px; }
.toc-skel, .no-content { color: #666; font-size: 14px; }
.toc-list { list-style: none; padding: 0; margin: 0; }
.toc-list li { display: flex; align-items: flex-start; gap: 8px; line-height: 1.4; padding: 6px 0; }
.toc-list .dot { margin-top: 7px; width: 4px; height: 4px; border-radius: 999px; background: currentColor; opacity: .6; display: inline-block; }
.toc-list .text { flex: 1; word-break: keep-all; }
.toc-actions { margin-top: 8px; }
.btn-toggle { border: 1px solid #ddd; background: #fafafa; padding: 6px 10px; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-toggle .ico { display: inline-block; transition: transform .2s; margin-left: 4px; }
.btn-toggle .ico.up { transform: rotate(180deg); }
</style>
