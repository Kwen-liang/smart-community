<template>
  <div class="map-container" ref="mapContainerRef">
    <div id="draw-map" class="map-view"></div>

    <!-- 顶部操作栏 (悬浮设计) -->
    <transition name="el-zoom-in-top">
      <div class="top-toolbar" v-if="map">
        <div class="toolbar-content">
          <div class="left-info">
            <el-tag effect="dark" :type="statusType" class="status-tag">
              {{ statusText }}
            </el-tag>
            <span class="instruction">{{ instructionText }}</span>
          </div>

          <div class="right-actions">
            <!-- 底图切换 -->
            <el-radio-group v-model="baseMap" size="small" @change="switchBaseMap" class="map-switcher">
              <el-radio-button label="standard">地图</el-radio-button>
              <el-radio-button label="satellite">卫星</el-radio-button>
            </el-radio-group>

            <el-divider direction="vertical" />

            <!-- 操作按钮组 -->
            <el-tooltip content="开始绘制网格" placement="bottom">
              <el-button type="primary" :icon="EditPen" circle @click="startDraw" :disabled="isDrawing || hasPolygon" />
            </el-tooltip>

            <el-tooltip content="撤销上一点" placement="bottom">
              <el-button type="warning" :icon="RefreshLeft" circle @click="undoPoint" :disabled="!isDrawing || points.length === 0" />
            </el-tooltip>

            <!-- 新增：显式的完成绘制按钮 -->
            <el-tooltip content="闭合图形 (至少3个点)" placement="bottom">
              <el-button type="success" :icon="CircleCheck" circle @click="finishDraw" :disabled="!isDrawing || points.length < 3" />
            </el-tooltip>

            <el-tooltip content="清除重置" placement="bottom">
              <el-button type="danger" :icon="Delete" circle @click="clearAll" :disabled="!isDrawing && !hasPolygon" />
            </el-tooltip>

            <el-divider direction="vertical" />

            <!-- 保存按钮 -->
            <el-button type="primary" :icon="Upload" round @click="openSaveDialog" :disabled="!hasPolygon">
              保存入库
            </el-button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 入库弹窗 -->
    <el-dialog v-model="dialogVisible" title="网格信息录入" width="500px" destroy-on-close>
      <el-form :model="form" ref="formRef" :rules="rules" label-width="90px">
        <el-form-item label="网格名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：文理学院-A01网格" />
        </el-form-item>
        <el-form-item label="网格编号" prop="code">
          <el-input v-model="form.code" placeholder="例如：GRID-A01" />
        </el-form-item>
        <el-form-item label="责任人ID" prop="managerId">
          <el-input-number v-model="form.managerId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="显示颜色">
          <el-color-picker v-model="form.color" show-alpha />
        </el-form-item>
        <el-form-item label="空间数据">
          <el-tag type="info">已包含 {{ polygonPointCount }} 个坐标点的多边形数据</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSave" :loading="saving">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import L from 'leaflet'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Delete, CircleCheck, RefreshLeft, Upload } from '@element-plus/icons-vue'
import request from '@/utils/request'

const mapContainerRef = ref(null)
const map = ref(null)
const drawLayer = ref(new L.LayerGroup())
const resultLayer = ref(new L.LayerGroup())
const isDrawing = ref(false)
const hasPolygon = ref(false)
const points = ref([]) // 存储当前绘制的点 [lat, lng]
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const baseMap = ref('standard')
let tileLayers = {}
let resizeObserver = null

const form = reactive({ name: '', code: '', managerId: 1, color: '#409EFF', geomJson: '' })
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入编号', trigger: 'blur' }]
}

// 状态计算属性
const statusText = computed(() => {
  if (isDrawing.value) return `绘制中 (${points.value.length})`
  if (hasPolygon.value) return '编辑模式'
  return '准备就绪'
})
const statusType = computed(() => isDrawing.value ? 'primary' : (hasPolygon.value ? 'warning' : 'info'))
const instructionText = computed(() => {
  if (isDrawing.value) return '单击添加节点，点击绿色对勾完成闭合'
  if (hasPolygon.value) return '拖动白色节点微调形状，确认无误后保存'
  return '点击笔形图标开始绘制，建议切换至卫星图以获得更精确的边界'
})
const polygonPointCount = computed(() => points.value.length)

const initMap = () => {
  // 1. 初始化地图
  map.value = L.map('draw-map', {
    zoomControl: false,
    doubleClickZoom: false // 禁用双击缩放，防止与双击结束绘制冲突
  }).setView([29.0473, 111.6617], 16)

  L.control.zoom({ position: 'bottomright' }).addTo(map.value)

  // 2. 初始化底图
  tileLayers.standard = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 })
  tileLayers.satellite = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', { maxZoom: 19 })

  tileLayers.standard.addTo(map.value)

  // 3. 添加图层
  map.value.addLayer(drawLayer.value)
  map.value.addLayer(resultLayer.value)

  // 4. 绑定地图事件
  map.value.on('click', onMapClick)
  map.value.on('mousemove', onMouseMove)
  // 【优化】移除了双击结束绘制的监听，防止误触
}

// 监听容器大小变化，修复“跑偏”问题 (地图坐标偏移)
const initResizeObserver = () => {
  resizeObserver = new ResizeObserver(() => {
    if (map.value) {
      map.value.invalidateSize()
    }
  })
  if (mapContainerRef.value) {
    resizeObserver.observe(mapContainerRef.value)
  }
}

// 切换底图
const switchBaseMap = (type) => {
  if (type === 'satellite') {
    map.value.removeLayer(tileLayers.standard)
    map.value.addLayer(tileLayers.satellite)
  } else {
    map.value.removeLayer(tileLayers.satellite)
    map.value.addLayer(tileLayers.standard)
  }
}

// 地图交互逻辑
const tempLine = ref(null) // 跟随鼠标的虚线

const onMapClick = (e) => {
  if (!isDrawing.value) return

  const latlng = e.latlng
  points.value.push([latlng.lat, latlng.lng])

  // 绘制绘制过程中的节点
  L.circleMarker(latlng, { radius: 4, color: '#409EFF', fillOpacity: 1 }).addTo(drawLayer.value)

  // 绘制实线路径
  redrawPolyline()
}

const onMouseMove = (e) => {
  if (!isDrawing.value || points.value.length === 0) return

  // 绘制跟随鼠标的虚线
  const lastPoint = points.value[points.value.length - 1]
  const currentPoint = [e.latlng.lat, e.latlng.lng]

  if (tempLine.value) drawLayer.value.removeLayer(tempLine.value)

  tempLine.value = L.polyline([lastPoint, currentPoint], {
    color: '#E6A23C',
    dashArray: '5, 5',
    weight: 2
  }).addTo(drawLayer.value)
}

const redrawPolyline = () => {
  // 清除旧线，重绘所有实线
  drawLayer.value.eachLayer(layer => {
    if (layer instanceof L.Polyline && layer !== tempLine.value) {
      drawLayer.value.removeLayer(layer)
    }
  })
  if (points.value.length > 1) {
    L.polyline(points.value, { color: '#409EFF', weight: 3 }).addTo(drawLayer.value)
  }
}

// --- 核心功能 ---

const startDraw = () => {
  resetState()
  isDrawing.value = true
}

const undoPoint = () => {
  if (points.value.length > 0) {
    points.value.pop()
    drawLayer.value.clearLayers()
    points.value.forEach(p => {
      L.circleMarker(p, { radius: 4, color: '#409EFF', fillOpacity: 1 }).addTo(drawLayer.value)
    })
    redrawPolyline()
  }
}

// 【新增】完成绘制并进入编辑模式
const finishDraw = () => {
  if (points.value.length < 3) {
    ElMessage.warning('多边形至少需要3个点')
    return
  }

  isDrawing.value = false
  hasPolygon.value = true
  drawLayer.value.clearLayers() // 清除辅助线和辅助点

  renderEditablePolygon()
}

// 【核心】渲染可编辑的多边形
const renderEditablePolygon = () => {
  resultLayer.value.clearLayers()

  // 1. 绘制主多边形
  const polygon = L.polygon(points.value, {
    color: form.color,
    weight: 2,
    fillOpacity: 0.4
  }).addTo(resultLayer.value)

  map.value.fitBounds(polygon.getBounds(), { padding: [50, 50] })

  // 2. 添加可拖拽的编辑顶点
  // 自定义小圆点图标，方便拖拽
  const vertexIcon = L.divIcon({
    className: 'vertex-icon',
    html: '<div style="width: 10px; height: 10px; background: white; border: 2px solid #409EFF; border-radius: 50%; box-shadow: 0 0 4px rgba(0,0,0,0.3); cursor: move;"></div>',
    iconSize: [10, 10],
    iconAnchor: [5, 5]
  })

  points.value.forEach((p, index) => {
    const marker = L.marker(p, {
      icon: vertexIcon,
      draggable: true
    }).addTo(resultLayer.value)

    // 拖拽事件监听：实时更新多边形形状
    marker.on('drag', (e) => {
      const newLatLng = e.target.getLatLng()
      // 更新数据源
      points.value[index] = [newLatLng.lat, newLatLng.lng]
      // 重绘多边形
      polygon.setLatLngs(points.value)
    })
  })
}

const resetState = () => {
  points.value = []
  drawLayer.value.clearLayers()
  resultLayer.value.clearLayers()
  isDrawing.value = false
  hasPolygon.value = false
  if (tempLine.value) tempLine.value = null
}

const clearAll = () => {
  if (hasPolygon.value || points.value.length > 0) {
    ElMessageBox.confirm('确定要放弃当前的绘制吗？', '警告', {
      type: 'warning'
    }).then(resetState).catch(() => {})
  } else {
    resetState()
  }
}

const openSaveDialog = () => {
  // 转换为 GeoJSON 格式 (闭合多边形)
  const coords = points.value.map(p => [p[1], p[0]]) // Leaflet(lat,lng) -> GeoJSON(lng,lat)
  coords.push(coords[0]) // 闭合

  const geoJson = {
    type: "Polygon",
    coordinates: [coords]
  }
  form.geomJson = JSON.stringify(geoJson)
  dialogVisible.value = true
}

const submitSave = () => {
  formRef.value.validate(async valid => {
    if (valid) {
      saving.value = true
      try {
        await request.post('/grid/save', form)
        ElMessage.success('网格入库成功')
        dialogVisible.value = false
        resetState()
      } catch (e) {
        console.error(e)
      } finally {
        saving.value = false
      }
    }
  })
}

onMounted(() => {
  initMap()
  initResizeObserver() // 启动尺寸监听
})

onUnmounted(() => {
  if (map.value) map.value.remove()
  if (resizeObserver) resizeObserver.disconnect()
})
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
  position: relative;
  background-color: #f0f2f5;
}

.map-view {
  width: 100%;
  height: 100%;
  z-index: 1;
}

/* 顶部悬浮工具栏 */
.top-toolbar {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  width: 90%;
  max-width: 800px;
}

.toolbar-content {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-radius: 8px;
  padding: 10px 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-tag {
  font-weight: bold;
  min-width: 80px;
  text-align: center;
}

.instruction {
  font-size: 13px;
  color: #606266;
}

.right-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.map-switcher :deep(.el-radio-button__inner) {
  padding: 8px 15px;
}
</style>
