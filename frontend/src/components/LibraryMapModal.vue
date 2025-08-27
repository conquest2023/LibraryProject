<template>
  <div class="modal-backdrop" @click.self="closeModal">
    <div class="modal-panel">
      <div class="modal-header">
        <h3>가까운 도서관 찾기</h3>
        <button @click="closeModal" class="close-button" aria-label="닫기">&times;</button>
      </div>
      <div class="modal-content">
        <div id="status-message">{{ statusMessage }}</div>
        <div id="map-container" ref="mapContainer"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// 부모로부터 isbn을 props로 받음
const props = defineProps({
  isbn: { type: String, required: true }
});

// 부모에게 이벤트를 보내기 위한 emit 함수
const emit = defineEmits(['close']);

const mapContainer = ref(null);
const statusMessage = ref('위치 요청 준비중...');
let map = null;

const closeModal = () => {
  emit('close');
};

// HTML 태그 escape 함수 (XSS 방지)
const escapeHtml = (s) => {
  return String(s).replace(/[&<>"']/g, m => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[m]));
};

// Geolocation 성공 콜백
const onSuccess = async (pos) => {
  const lat = pos.coords.latitude;
  const lon = pos.coords.longitude;
  statusMessage.value = `위치 획득 완료 — 가까운 도서관 조회 중...`;

  const userPosition = new window.kakao.maps.LatLng(lat, lon);

  // 사용자 위치 마커 표시 및 지도 중앙 설정
  new window.kakao.maps.Marker({ position: userPosition, map });
  map.setCenter(userPosition);

  try {
    const resp = await fetch('/api/location', { // 백엔드 API 경로
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ latitude: lat, longitude: lon, isbn: props.isbn })
    });
    if (!resp.ok) throw new Error(`서버 에러: ${resp.status}`);

    const data = await resp.json();
    const libraries = Array.isArray(data.nearest) ? data.nearest : [];

    if (libraries.length === 0) {
      statusMessage.value = '이 책을 소장하고 있는 가까운 도서관이 없습니다.';
      return;
    }

    const bounds = new window.kakao.maps.LatLngBounds();
    bounds.extend(userPosition);

    libraries.slice(0, 5).forEach((lib, idx) => { // 최대 5개 표시
      const libPosition = new window.kakao.maps.LatLng(lib.latitude, lib.longitude);
      const marker = new window.kakao.maps.Marker({ position: libPosition, map });

      const dirLink = `https://map.kakao.com/link/from/현위치,${lat},${lon}/to/${escapeHtml(lib.libName)},${lib.latitude},${lib.longitude}`;

      const infowindow = new window.kakao.maps.InfoWindow({
        content: `
          <div style="padding:8px;font-size:12px;line-height:1.5;">
            <b>${idx + 1}. ${escapeHtml(lib.libName)}</b><br/>
            거리: ${lib.distanceKm?.toFixed(2)}km<br/>
            <a href="${dirLink}" target="_blank" style="color:#007bff;">길찾기</a>
          </div>`,
        removable: true
      });

      infowindow.open(map, marker);
      bounds.extend(libPosition);
    });

    map.setBounds(bounds);
    statusMessage.value = `책을 소장한 가까운 도서관 ${libraries.length}곳을 찾았습니다!`;

  } catch (e) {
    console.error(e);
    statusMessage.value = '도서관 조회 실패: ' + e.message;
  }
};

// Geolocation 실패 콜백
const onError = (err) => {
  let msg = '알 수 없는 오류';
  if (err.code === 1) msg = '사용자가 위치 정보 접근을 거부했습니다.';
  else if (err.code === 2) msg = '위치 정보를 사용할 수 없습니다.';
  else if (err.code === 3) msg = '위치 정보 요청이 시간 초과되었습니다.';
  statusMessage.value = '오류: ' + msg;
};

// 컴포넌트가 마운트되면 지도 초기화 및 위치 요청
onMounted(() => {
  if (window.kakao && window.kakao.maps && mapContainer.value) {
    const options = {
      center: new window.kakao.maps.LatLng(37.566826, 126.9786567),
      level: 3
    };
    map = new window.kakao.maps.Map(mapContainer.value, options);

    // Geolocation API 요청
    if (navigator.geolocation) {
      statusMessage.value = '현재 위치를 가져오는 중...';
      navigator.geolocation.getCurrentPosition(onSuccess, onError);
    } else {
      statusMessage.value = '이 브라우저에서는 위치 정보를 지원하지 않습니다.';
    }
  } else {
    statusMessage.value = '카카오 지도 SDK를 불러오는 데 실패했습니다.';
  }
});
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-panel {
  background: white;
  border-radius: 16px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
  width: 90%;
  max-width: 800px;
  display: flex;
  flex-direction: column;
}
.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
}
.close-button {
  background: none;
  border: none;
  font-size: 2rem;
  font-weight: 300;
  line-height: 1;
  cursor: pointer;
  color: #888;
}
.modal-content {
  padding: 24px;
}
#status-message {
  margin-bottom: 12px;
  font-size: 0.9rem;
  color: #555;
}
#map-container {
  width: 100%;
  height: 500px;
  border-radius: 8px;
}
</style>