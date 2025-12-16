<template>
  <div class="page-container">
    <el-card shadow="never">
      <!-- 顶部搜索栏 -->
      <template #header>
        <div class="card-header">
          <div class="left-panel">
            <el-input v-model="searchQuery" placeholder="搜索网格名称/编号" style="width: 240px" clearable
              @clear="handleSearch">
              <template #prefix><el-icon>
                  <Search />
                </el-icon></template>
            </el-input>
            <el-button type="primary" style="margin-left: 12px" @click="handleSearch">查询</el-button>
          </div>
          <div class="right-panel">
            <el-button type="success" :icon="Plus" @click="$router.push('/grid/map')">新增网格</el-button>
            <el-button type="warning" :icon="Download">导出数据</el-button>
          </div>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <el-table-column prop="code" label="网格编号" width="120" sortable />
        <el-table-column prop="name" label="网格名称" min-width="150" />
        <el-table-column prop="managerName" label="责任网格长" width="120">
          <template #default="scope">
            <div class="manager-info">
              <el-avatar :size="24" style="background:#409eff">{{ (scope.row.managerName || '未')[0] }}</el-avatar>
              <span style="margin-left: 8px">{{ scope.row.managerName || '未分配' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="managerPhone" label="联系电话" width="140" />
        <el-table-column prop="area" label="覆盖面积 (㎡)" width="140" sortable />
        <el-table-column prop="population" label="人口数" width="100" sortable />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'normal' ? 'success' : 'danger'" effect="dark">
              {{ scope.row.status === 'normal' ? '正常' : '预警' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="View">详情</el-button>
            <el-popconfirm title="确定要删除该网格吗？" @confirm="handleDelete(scope.row)">
              <template #reference>
                <el-button link type="danger" size="small" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Download, View, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

const searchQuery = ref('')
const loading = ref(false)
const rawList = ref([]) // 原始数据

// 前端简单的过滤搜索
const tableData = computed(() => {
  if (!searchQuery.value) return rawList.value
  return rawList.value.filter(item =>
    (item.name && item.name.includes(searchQuery.value)) ||
    (item.code && item.code.includes(searchQuery.value))
  )
})

// 1. 获取网格列表 (真实接口)
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/grid/list')
    if (res.code === 200) {
      rawList.value = res.data
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  ElMessage.success('列表已刷新')
}

// 【关键修改】对接真实后端删除接口
const handleDelete = async (row) => {
  try {
    // 调用 DELETE /grid/{id}
    const res = await request.delete(`/grid/${row.gridId}`)

    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchList() // 删除后刷新列表
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('请求出错，请检查控制台')
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-panel,
.right-panel {
  display: flex;
  align-items: center;
}

.manager-info {
  display: flex;
  align-items: center;
}
</style>
