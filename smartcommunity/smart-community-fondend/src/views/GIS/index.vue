<template>
  <div class="gis-container">
    <div id="gis-map" class="map-view"></div>

    <!-- 悬浮图层控制器 -->
    <div class="layer-control">
      <el-card shadow="hover" class="control-card">
        <template #header>
          <div class="card-header">
            <span>地图图层控制</span>
            <el-icon><MapLocation /></el-icon>
          </div>
        </template>

        <!-- 底图切换 -->
        <div class="control-section">
          <div class="section-title">底图风格</div>
          <el-radio-group v-model="baseMapType" size="small" @change="changeBaseMap">
            <el-radio-button label="standard">标准地图</el-radio-button>
            <el-radio-button label="satellite">卫星影像</el-radio-button>
          </el-radio-group>
        </div>

        <el-divider style="margin: 12px 0" />

        <!-- 业务图层开关 -->
        <div class="control-section">
          <div class="section-title">数据图层</div>
          <div class="checkbox-group">
            <el-checkbox v-model="layers.grid" @change="toggleLayer('grid')">
              <span class="layer-label"><span class="color-dot grid-dot"></span>社区网格</span>
            </el-checkbox>
            <el-checkbox v-model="layers.poi" @change="toggleLayer('poi')">
              <span class="layer-label"><span class="color-dot poi-dot"></span>POI 设施</span>
            </el-checkbox>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive, onUnmounted } from 'vue'
import L from 'leaflet'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { MapLocation } from '@element-plus/icons-vue'

const map = ref(null)
const baseMapType = ref('standard')
const layers = reactive({ grid: true, poi: true })

// 图层组
let gridLayerGroup = L.layerGroup()
let poiLayerGroup = L.layerGroup()
let standardTileLayer = null
let satelliteTileLayer = null

const initMap = () => {
  // 1. 初始化地图容器
  map.value = L.map('gis-map', {
    zoomControl: false, // 隐藏默认缩放控件，可自定义
    attributionControl: false
  }).setView([29.0473, 111.6617], 15)

  // 添加缩放控件到右下角
  L.control.zoom({ position: 'bottomright' }).addTo(map.value)

  // 2. 定义底图图层
  // 标准地图 (OpenStreetMap)
  standardTileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19
  })

  // 卫星地图 (ArcGIS World Imagery - 免费且高质量)
  satelliteTileLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    maxZoom: 19
  })

  // 默认加载标准地图
  standardTileLayer.addTo(map.value)

  // 3. 添加业务图层组
  gridLayerGroup.addTo(map.value)
  poiLayerGroup.addTo(map.value)

  // 4. 加载数据
  loadGridData()
  loadPoiData()
}

// 切换底图
const changeBaseMap = (type) => {
  if (type === 'satellite') {
    map.value.removeLayer(standardTileLayer)
    map.value.addLayer(satelliteTileLayer)
  } else {
    map.value.removeLayer(satelliteTileLayer)
    map.value.addLayer(standardTileLayer)
  }
}

// 加载网格数据
const loadGridData = async () => {
  try {
    const res = await request.get('/grid/list')
    if (res.code === 200) {
      gridLayerGroup.clearLayers() // 清除旧数据
      res.data.forEach(grid => {
        if (grid.geomJson) {
          const geoJson = JSON.parse(grid.geomJson)
          const layer = L.geoJSON(geoJson, {
            style: {
              color: grid.status === 'alert' ? '#F56C6C' : '#409EFF',
              weight: 2,
              fillOpacity: 0.2
            },
            onEachFeature: (feature, l) => {
              // 绑定 Popup
              const content = `
                <div style="font-size:14px;">
                  <h4 style="margin:0 0 5px 0;">${grid.name}</h4>
                  <p style="margin:0;color:#666;">负责人: ${grid.managerName || '未分配'}</p>
                  <p style="margin:0;color:#666;">人口: ${grid.population || 0}人</p>
                </div>
              `
              l.bindPopup(content)
              // 绑定 Tooltip (鼠标悬停显示名称)
              l.bindTooltip(grid.name, { permanent: true, direction: 'center', className: 'grid-label' })
            }
          })
          gridLayerGroup.addLayer(layer)
        }
      })
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载网格数据失败')
  }
}

// 加载POI数据
const loadPoiData = async () => {
  try {
    const res = await request.get('/poi/list')
    if (res.code === 200) {
      poiLayerGroup.clearLayers()
      res.data.forEach(poi => {
        if (poi.geomJson) {
          const geoJson = JSON.parse(poi.geomJson)
          // 根据类型选择不同颜色的圆点
          const color = poi.type === 'camera' ? '#E6A23C' : '#67C23A'

          const layer = L.geoJSON(geoJson, {
            pointToLayer: (feature, latlng) => {
              return L.circleMarker(latlng, {
                radius: 6,
                fillColor: color,
                color: '#fff',
                weight: 1,
                opacity: 1,
                fillOpacity: 0.8
              }).bindPopup(`<b>${poi.name}</b><br>类型: ${poi.type}`)
            }
          })
          poiLayerGroup.addLayer(layer)
        }
      })
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载POI数据失败')
  }
}

const toggleLayer = (type) => {
  if (type === 'grid') {
    layers.grid ? map.value.addLayer(gridLayerGroup) : map.value.removeLayer(gridLayerGroup)
  } else if (type === 'poi') {
    layers.poi ? map.value.addLayer(poiLayerGroup) : map.value.removeLayer(poiLayerGroup)
  }
}

onMounted(initMap)
onUnmounted(() => {
  if (map.value) map.value.remove()
})
</script>

<style scoped>
.gis-container {
  height: calc(100vh - 84px);
  width: 100%;
  position: relative;
  background: #000; /* 防止地图加载前白屏 */
}

.map-view {
  width: 100%;
  height: 100%;
  z-index: 1;
}

.layer-control {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 1000;
  width: 260px;
}

.control-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  color: #303133;
}

.control-section {
  padding: 5px 0;
}

.section-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.layer-label {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.color-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 8px;
  display: inline-block;
}

.grid-dot { background-color: #409EFF; }
.poi-dot { background-color: #67C23A; }

/* 覆盖 Leaflet 默认样式 */
:deep(.grid-label) {
  background: transparent;
  border: none;
  box-shadow: none;
  color: #303133;
  font-weight: bold;
  text-shadow: 1px 1px 0 #fff, -1px -1px 0 #fff, 1px -1px 0 #fff, -1px 1px 0 #fff;
}
</style>
