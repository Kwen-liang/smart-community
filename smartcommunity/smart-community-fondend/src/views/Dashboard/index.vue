<template>
  <div class="dashboard-container">
    <!-- 1. 顶部统计卡片 -->
    <el-row :gutter="20" class="panel-group">
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-grid">
            <el-icon><Grid /></el-icon>
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">网格总数</div>
            <div class="card-panel-num">{{ cards.gridCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-people">
            <el-icon><User /></el-icon>
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">实有人口</div>
            <div class="card-panel-num">{{ cards.population }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-message">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">今日上报</div>
            <div class="card-panel-num">{{ cards.todayCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-money">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">待处理</div>
            <div class="card-panel-num text-danger">{{ cards.pendingCount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 2. 核心图表区域 (统计 + 占比) -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 左侧：柱状图 -->
      <el-col :xs="24" :sm="24" :lg="14">
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>各社区事件统计 (近7日)</span>
            </div>
          </template>
          <!-- 暂时使用模拟数据，如果要真实数据需再写一个接口 -->
          <BarChart :chart-data="barData" height="350px" />
        </el-card>
      </el-col>

      <!-- 右侧：饼图 -->
      <el-col :xs="24" :sm="24" :lg="10">
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>事件类型分布</span>
            </div>
          </template>
          <PieChart :chart-data="pieData" height="350px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 3. 空间热力图区域 (对接真实接口) -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover" v-loading="loadingHeatmap">
          <template #header>
            <div class="clearfix">
              <span>社区网格事件密度热力图 (Real-time GIS Data)</span>
              <el-tag size="small" type="warning">高频预警</el-tag>
            </div>
          </template>
          <HeatMap v-if="heatMapData.xData.length > 0" :chart-data="heatMapData" height="400px" title="各网格事件分布密度" />
          <el-empty v-else description="暂无数据，请先上报一些事件" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 4. 底部：最新动态 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span>最新动态 (Real-time)</span>
          </template>
          <el-table :data="recentList" style="width: 100%" size="small">
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="user" label="操作人" width="120" />
            <el-table-column prop="action" label="操作内容" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.status === '完成' ? 'success' : 'warning'">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import request from '@/utils/request' // 引入封装的 axios
import BarChart from '@/components/Charts/BarChart.vue'
import PieChart from '@/components/Charts/PieChart.vue'
import HeatMap from '@/components/Charts/HeatMap.vue'

// 顶部卡片数据
const cards = reactive({
  gridCount: 0,
  population: 0,
  todayCount: 0,
  pendingCount: 0
})

// 饼图数据
const pieData = ref([])

// 热力图数据
const loadingHeatmap = ref(false)
const heatMapData = ref({
  xData: [],
  yData: [],
  data: []
})

// 模拟柱状图数据 (可后续开发)
const barData = ref({
  categories: ['幸福社区', '平安社区', '阳光社区', '花园社区', '老街坊'],
  data: [120, 82, 91, 154, 162]
})

// 模拟最新动态
const recentList = ref([
  { time: '2023-10-27 10:23', user: '张三', action: '上报了 [井盖丢失] 事件', status: '待处理' },
  { time: '2023-10-27 09:15', user: '李四', action: '完成了 [A区网格] 巡查', status: '完成' }
])

// --- 核心：获取真实统计数据 ---
const fetchAllStats = async () => {
  try {
    // 1. 获取卡片数据
    const cardRes = await request.get('/stats/cards')
    if (cardRes.code === 200) {
      Object.assign(cards, cardRes.data)
    }

    // 2. 获取饼图数据
    const pieRes = await request.get('/stats/pie')
    if (pieRes.code === 200) {
      pieData.value = pieRes.data
    }

    // 3. 获取热力图数据
    loadingHeatmap.value = true
    const heatRes = await request.get('/stats/heatmap')
    if (heatRes.code === 200 && heatRes.data && heatRes.data.xData) {
      heatMapData.value = heatRes.data
    }
    loadingHeatmap.value = false

  } catch (error) {
    console.error('获取统计数据失败', error)
    loadingHeatmap.value = false
  }
}

onMounted(() => {
  fetchAllStats()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.card-panel {
  height: 108px;
  cursor: pointer;
  font-size: 12px;
  position: relative;
  overflow: hidden;
  color: #666;
  background: #fff;
  box-shadow: 4px 4px 40px rgba(0, 0, 0, .05);
  border-color: rgba(0, 0, 0, .05);
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  border-radius: 6px;
}

.card-panel-icon-wrapper {
  float: left;
  margin: 14px 0 0 14px;
  padding: 16px;
  transition: all 0.38s ease-out;
  border-radius: 6px;
  font-size: 48px;
}

.card-panel-icon-wrapper .el-icon {
  font-size: 48px;
}

.card-panel-description {
  float: right;
  font-weight: bold;
  margin: 26px 26px 26px 0;
  margin-left: auto;
}

.card-panel-text {
  line-height: 18px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 16px;
  margin-bottom: 12px;
}

.card-panel-num {
  font-size: 20px;
}

.text-danger {
  color: #f56c6c;
}

/* 图标颜色 */
.icon-grid { color: #40c9c6; }
.icon-people { color: #36a3f7; }
.icon-message { color: #f4516c; }
.icon-money { color: #e6a23c; }

.clearfix {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
