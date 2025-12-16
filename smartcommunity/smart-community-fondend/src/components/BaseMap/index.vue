<template>
  <div id="map-container"></div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import L from 'leaflet'

// 定义地图实例
const map = ref(null)

const initMap = () => {
  // 1. 初始化地图容器，设置中心点和缩放级别
  // 修改为：湖南文理学院 [29.0473, 111.6617]
  map.value = L.map('map-container').setView([29.0473, 111.6617], 15)

  // 2. 加载底图 (文档 5.4 功能3：多底图切换)
  // 这里使用 OSM 开源底图作为默认
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors',
    maxZoom: 18,
  }).addTo(map.value)
}

onMounted(() => {
  initMap()
})

// 暴露地图实例供父组件调用 (如绘制网格时)
defineExpose({ map })
</script>

<style scoped>
#map-container {
  width: 100%;
  height: 100%;
  /* 确保父容器有高度 */
  z-index: 1;
}
</style>
