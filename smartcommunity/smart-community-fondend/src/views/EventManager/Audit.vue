<template>
  <div class="page-container">
    <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
      <!-- 待审核 -->
      <el-tab-pane name="pending" label="待审核">
        <el-table :data="pendingList" v-loading="loading">
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="urgency" label="紧急度">
            <template #default="{ row }">
              <el-tag :type="row.urgency === 'high' ? 'danger' : 'warning'">{{ row.urgency }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="occurTime" label="发生时间" width="180" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="openDispatch(row)">分派</el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 处理中 -->
      <el-tab-pane name="processing" label="处理中">
        <el-table :data="processingList" v-loading="loading">
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="handlerId" label="处理人ID">
            <template #default="{ row }">
              <!-- 简单的ID展示，实际项目通常需要根据ID查姓名或后端直接返回姓名 -->
              {{ row.handlerId }}
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handleClose(row)">结案</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 分派弹窗 -->
    <el-dialog v-model="dispatchVisible" title="任务分派" width="400px">
      <el-form>
        <el-form-item label="处理人">
          <el-select v-model="targetHandler" placeholder="请选择网格员" style="width:100%" filterable>
            <!-- 动态渲染用户列表 -->
            <el-option v-for="user in handlerList" :key="user.userId"
              :label="user.nickname + ' (' + user.username + ')'" :value="user.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDispatch">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const activeTab = ref('pending')
const loading = ref(false)
const pendingList = ref([])
const processingList = ref([])

const dispatchVisible = ref(false)
const targetHandler = ref(null)
const currentRow = ref(null)
const handlerList = ref([]) // 存储可分派的用户列表

const fetchList = async (status) => {
  loading.value = true
  try {
    const res = await request.get('/event/listByStatus', { params: { status } })
    if (res.code === 200) {
      if (status === 'pending') pendingList.value = res.data
      if (status === 'processing') processingList.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const handleTabChange = (tab) => {
  fetchList(tab)
}

// 获取可分派的用户列表 (状态正常的)
const fetchHandlers = async () => {
  try {
    const res = await request.get('/system/user/list', {
      params: {
        pageNum: 1,
        pageSize: 100, // 获取前100个用户，实际场景可用下拉搜索
        status: '1'    // 只获取状态为"启用"的用户
      }
    })
    if (res.code === 200) {
      handlerList.value = res.data.records
    }
  } catch (e) {
    console.error(e)
  }
}

// 分派逻辑
const openDispatch = (row) => {
  currentRow.value = row
  targetHandler.value = null
  // 打开弹窗时动态获取最新用户列表
  fetchHandlers()
  dispatchVisible.value = true
}

const confirmDispatch = async () => {
  if (!targetHandler.value) return ElMessage.warning('请选择处理人')
  try {
    await request.post('/event/dispatch', null, {
      params: { eventId: currentRow.value.eventId, handlerId: targetHandler.value }
    })
    ElMessage.success('分派成功')
    dispatchVisible.value = false
    fetchList('pending')
  } catch (e) { console.error(e) }
}

// 驳回逻辑
const handleReject = async (row) => {
  try {
    await request.post('/event/reject', null, {
      params: { eventId: row.eventId, reason: '信息不全' }
    })
    ElMessage.success('已驳回')
    fetchList('pending')
  } catch (e) {
    console.error(e)
  }
}

// 结案逻辑
const handleClose = async (row) => {
  try {
    await request.post('/event/close', null, {
      params: { eventId: row.eventId }
    })
    ElMessage.success('已结案')
    fetchList('processing')
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchList('pending')
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
