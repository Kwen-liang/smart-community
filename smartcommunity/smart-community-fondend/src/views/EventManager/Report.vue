<template>
  <div class="page-container">
    <el-card shadow="hover" class="report-card">
      <template #header>
        <div class="card-header">
          <span><el-icon>
              <Warning />
            </el-icon> 事件上报</span>
          <el-button type="text" disabled>请如实填写现场情况</el-button>
        </div>
      </template>

      <el-form ref="reportFormRef" :model="form" :rules="rules" label-width="100px" label-position="right">
        <!-- 基础信息 -->
        <div class="section-title">基础信息</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="事件标题" prop="title">
              <el-input v-model="form.title" placeholder="简要描述事件，如：xx路井盖缺失" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="事件类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择类型" style="width:100%">
                <el-option label="设施维护" value="facilities" />
                <el-option label="市容环境" value="sanitation" />
                <el-option label="治安防控" value="security" />
                <el-option label="突发事件" value="emergency" />
                <el-option label="便民服务" value="service" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="紧急程度" prop="urgency">
              <el-radio-group v-model="form.urgency">
                <el-radio label="low"><el-tag type="info">一般</el-tag></el-radio>
                <el-radio label="medium"><el-tag type="warning">紧急</el-tag></el-radio>
                <el-radio label="high"><el-tag type="danger">特急</el-tag></el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发生时间" prop="occurTime">
              <el-date-picker v-model="form.occurTime" type="datetime" placeholder="选择时间" style="width:100%"
                format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 位置信息 -->
        <div class="section-title">位置信息</div>
        <el-form-item label="发生地点" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址或点击右侧按钮地图选点">
            <template #append>
              <el-button @click="openMapDialog">
                <el-icon>
                  <Location />
                </el-icon> 地图选点
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="空间坐标" prop="geomJson">
          <el-input v-model="form.geomJson" disabled placeholder="请通过地图选点获取坐标" />
          <div class="form-tip" v-if="form.geomJson">
            <el-icon color="#67C23A">
              <CircleCheck />
            </el-icon> 已获取精确坐标，系统将自动匹配所属网格
          </div>
        </el-form-item>

        <!-- 详细描述与附件 -->
        <div class="section-title">详情与证据</div>
        <el-form-item label="详细描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述现场情况、涉及人员、已采取的措施等..."
            show-word-limit maxlength="500" />
        </el-form-item>

        <el-form-item label="现场照片">
          <el-upload v-model:file-list="fileList" action="/api/common/upload" list-type="picture-card"
            :on-success="handleUploadSuccess" :on-preview="handlePictureCardPreview" :on-remove="handleRemove"
            :headers="uploadHeaders" accept=".jpg,.jpeg,.png">
            <el-icon>
              <Plus />
            </el-icon>
          </el-upload>
          <div class="form-tip">支持 jpg/png 格式，最多上传 9 张</div>
        </el-form-item>

        <!-- 提交按钮 -->
        <div class="form-footer">
          <el-button @click="resetForm">重 置</el-button>
          <el-button type="primary" @click="submit" :loading="submitting" size="large" style="width: 150px;">
            立即上报
          </el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 地图选点弹窗 -->
    <el-dialog v-model="mapDialogVisible" title="选择发生地点" width="800px" top="5vh" destroy-on-close>
      <div class="map-picker-container">
        <div id="picker-map" style="width: 100%; height: 500px;"></div>
        <div class="map-tip">点击地图确定位置</div>
      </div>
      <template #footer>
        <el-button @click="mapDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmLocation" :disabled="!tempLocation">确定选择</el-button>
      </template>
    </el-dialog>

    <!-- 图片预览弹窗 -->
    <el-dialog v-model="dialogVisible">
      <img w-full :src="dialogImageUrl" alt="Preview Image" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, Location, Plus, CircleCheck } from '@element-plus/icons-vue'
import request from '@/utils/request'
import L from 'leaflet'
// 确保引入 leaflet 样式 (通常在 main.js 已全局引入，这里为了保险)
import 'leaflet/dist/leaflet.css'

// 修复 Leaflet 默认图标路径问题
import icon from 'leaflet/dist/images/marker-icon.png'
import iconShadow from 'leaflet/dist/images/marker-shadow.png'
const DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41]
})
L.Marker.prototype.options.icon = DefaultIcon

const reportFormRef = ref(null)
const submitting = ref(false)
const mapDialogVisible = ref(false)
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const fileList = ref([])

// 上传请求头 (携带 Token)
const uploadHeaders = computed(() => {
  return { Authorization: sessionStorage.getItem('token') }
})

const form = reactive({
  title: '',
  type: '',
  urgency: 'low',
  occurTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
  description: '',
  address: '',
  geomJson: '', // 存储 GeoJSON Point
  images: '', // 存储 JSON 字符串
  reporterId: 1 // 实际上报时后端可能从 Token 解析，这里保留作为演示
})

const rules = {
  title: [{ required: true, message: '请输入事件标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择事件类型', trigger: 'change' }],
  occurTime: [{ required: true, message: '请选择发生时间', trigger: 'change' }],
  description: [{ required: true, message: '请输入详细描述', trigger: 'blur' }],
  geomJson: [{ required: true, message: '请在地图上选择位置', trigger: 'change' }]
}

// --- 文件上传处理 ---
const handleUploadSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    // 后端返回的 url
    uploadFile.url = response.data
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 修改：移除未使用的 uploadFiles 参数
const handleRemove = (uploadFile) => {
  // 实际项目中可能需要调用后端删除接口
  console.log('Remove:', uploadFile)
}

const handlePictureCardPreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url
  dialogVisible.value = true
}

// --- 地图选点处理 ---
let map = null
let marker = null
const tempLocation = ref(null) // 临时存储选点 [lat, lng]

const openMapDialog = () => {
  mapDialogVisible.value = true
  nextTick(() => {
    initMap()
  })
}

const initMap = () => {
  if (map) map.remove()

  // 初始化地图
  map = L.map('picker-map').setView([29.0473, 111.6617], 15)

  // 增加底图切换 (可选)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '© OpenStreetMap'
  }).addTo(map)

  // 之前的选点回显
  if (form.geomJson) {
    try {
      const geo = JSON.parse(form.geomJson)
      const latlng = [geo.coordinates[1], geo.coordinates[0]] // GeoJSON [lng, lat] -> Leaflet [lat, lng]
      marker = L.marker(latlng).addTo(map)
      map.setView(latlng, 16)
      tempLocation.value = latlng
    } catch (e) { console.error(e) }
  }

  // 点击事件
  map.on('click', (e) => {
    if (marker) {
      marker.setLatLng(e.latlng)
    } else {
      marker = L.marker(e.latlng).addTo(map)
    }
    tempLocation.value = [e.latlng.lat, e.latlng.lng]
  })
}

const confirmLocation = () => {
  if (!tempLocation.value) return

  // 构造 GeoJSON Point: [lng, lat]
  const geoJson = {
    type: "Point",
    coordinates: [tempLocation.value[1], tempLocation.value[0]]
  }
  form.geomJson = JSON.stringify(geoJson)

  // 简单的逆地理编码模拟 (实际应调用 GIS 服务)
  if (!form.address) {
    form.address = `经度:${tempLocation.value[1].toFixed(4)}, 纬度:${tempLocation.value[0].toFixed(4)}`
  }

  mapDialogVisible.value = false
}

// --- 提交表单 ---
const submit = () => {
  reportFormRef.value.validate(async valid => {
    if (valid) {
      submitting.value = true

      // 1. 处理图片列表 -> JSON 字符串
      const imageUrls = fileList.value.map(f => f.url).filter(url => url)
      form.images = JSON.stringify(imageUrls)

      try {
        const res = await request.post('/event/report', form)
        if (res.code === 200) {
          ElMessage.success('事件上报成功')
          resetForm()
        } else {
          ElMessage.error(res.msg || '上报失败')
        }
      } catch (e) {
        console.error(e)
      } finally {
        submitting.value = false
      }
    } else {
      ElMessage.warning('请检查必填项')
    }
  })
}

const resetForm = () => {
  if (reportFormRef.value) reportFormRef.value.resetFields()
  fileList.value = []
  form.geomJson = ''
  form.images = ''
}
</script>

<style scoped>
.page-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.report-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
  margin: 20px 0;
  background-color: #f5f7fa;
  padding: 8px 10px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.form-footer {
  margin-top: 40px;
  text-align: center;
}

.map-picker-container {
  position: relative;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.map-tip {
  position: absolute;
  top: 10px;
  left: 50px;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.9);
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}
</style>
