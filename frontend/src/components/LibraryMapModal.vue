<template>
  <div class="modal-backdrop" @click.self="closeModal">
    <div class="modal-panel">
      <div class="modal-header">
        <h3>내 주변 도서관 지도</h3>
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

// 부모로부터 도서관 목록과 사용자 위치를 props로 받음
const props = defineProps({
  isbn: { type: String, required: true },
  libraries: { type: Array, default: () => [] },
  userPosition: { type: Object, default: null }
});

const emit = defineEmits(['close']);

const mapContainer = ref(null);
const statusMessage = ref('지도를 준비 중입니다...');
let map = null;

const closeModal = () => {
  emit('close');
};

const escapeHtml = (s) => {
  return String(s).replace(/[&<>"']/g, m => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[m]));
};

onMounted(() => {
  if (!window.kakao || !window.kakao.maps) {
    statusMessage.value = '카카오 지도 SDK를 불러오는 데 실패했습니다.';
    return;
  }
  if (!mapContainer.value) {
    statusMessage.value = '지도를 표시할 컨테이너를 찾을 수 없습니다.';
    return;
  }

  // 지도 초기화
  const options = {
    center: new window.kakao.maps.LatLng(37.566826, 126.9786567), // 기본 위치: 서울시청
    level: 7
  };
  map = new window.kakao.maps.Map(mapContainer.value, options);

  const bounds = new window.kakao.maps.LatLngBounds();
  let markerCount = 0;

  // 1. 사용자 위치 마커 표시
  if (props.userPosition) {
    const userPos = new window.kakao.maps.LatLng(props.userPosition.lat, props.userPosition.lon);

    // 사용자는 다른 이미지의 마커를 사용
    const userMarkerImage = new window.kakao.maps.MarkerImage(
        'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png',
        new window.kakao.maps.Size(64, 69),
        { offset: new window.kakao.maps.Point(27, 69) }
    );
    new window.kakao.maps.Marker({
      position: userPos,
      map: map,
      image: userMarkerImage,
      title: '현재 위치'
    });
    bounds.extend(userPos);
    markerCount++;
  }

  // 2. 도서관 마커 표시
  if (props.libraries.length > 0) {
    props.libraries.forEach((lib) => {
      const libPosition = new window.kakao.maps.LatLng(lib.latitude, lib.longitude);
      const marker = new window.kakao.maps.Marker({ position: libPosition, map: map, title: lib.libName });

      const dirLink = `https://map.kakao.com/link/to/${escapeHtml(lib.libName)},${lib.latitude},${lib.longitude}`;

      const infowindow = new window.kakao.maps.InfoWindow({
        content: `
          <div style="padding:8px;font-size:12px;line-height:1.5; width: 180px;">
            <b style="display: block; margin-bottom: 4px; white-space: normal;">${escapeHtml(lib.libName)}</b>
            거리: ${lib.distanceKm?.toFixed(2)}km<br/>
            <a href="${dirLink}" target="_blank" style="color:#007bff;">길찾기</a>
          </div>`,
        removable: true
      });

      // 마커에 클릭 이벤트를 등록합니다
      window.kakao.maps.event.addListener(marker, 'click', function() {
        infowindow.open(map, marker);
      });

      bounds.extend(libPosition);
      markerCount++;
    });
  }

  // 3. 지도의 경계를 모든 마커가 보이도록 설정
  if (markerCount > 0) {
    map.setBounds(bounds);
    statusMessage.value = `총 ${props.libraries.length}개의 도서관 위치를 표시했습니다.`;
  } else {
    // 사용 위치도, 도서관도 없을 경우
    if (props.userPosition) {
      map.setCenter(new window.kakao.maps.LatLng(props.userPosition.lat, props.userPosition.lon));
    }
    statusMessage.value = '표시할 도서관 정보가 없습니다.';
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
  /* 모달의 높이가 화면보다 커지는 것을 방지 */
  max-height: calc(100vh - 150px);
  overflow-y: auto;
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