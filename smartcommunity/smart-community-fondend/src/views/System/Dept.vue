<template>
  <div class="app-container">
    <el-card class="filter-container" shadow="never">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="部门名称">
          <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="部门状态" clearable style="width: 120px">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button type="primary" plain icon="Plus" @click="handleAdd()">新增根部门</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-container" shadow="never">
      <!-- 树形表格：增加 row-key 和 default-expand-all -->
      <el-table v-loading="loading" :data="deptList" row-key="deptId" default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" border>
        <el-table-column prop="deptName" label="部门/网格名称" min-width="200" />
        <el-table-column prop="orderNum" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
              {{ scope.row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="200" />
        <el-table-column label="操作" align="center" width="260" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">修改</el-button>
            <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)">新增子项</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加或修改部门对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body destroy-on-close>
      <el-form ref="deptRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select v-model="form.parentId" :data="deptOptions"
            :props="{ value: 'deptId', label: 'deptName', children: 'children' }" value-key="deptId"
            placeholder="选择上级部门" check-strictly style="width: 100%" />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="显示排序" prop="orderNum">
          <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="部门状态">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, toRefs, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(true)
const open = ref(false)
const title = ref('')
const deptRef = ref(null)
const deptList = ref([])

const data = reactive({
  form: {},
  queryParams: {
    deptName: undefined,
    status: undefined
  },
  rules: {
    parentId: [{ required: true, message: "上级部门不能为空", trigger: "change" }],
    deptName: [{ required: true, message: "部门名称不能为空", trigger: "blur" }],
    orderNum: [{ required: true, message: "显示排序不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

// 计算属性：构建带"顶级部门"的树选项
const deptOptions = computed(() => {
  const root = [{ deptId: 0, deptName: '顶级部门/根目录', children: [] }]
  return [...root, ...deptList.value]
})

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await request.get('/system/dept/list', { params: queryParams.value })
    if (res.code === 200) {
      deptList.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const reset = () => {
  form.value = {
    deptId: undefined,
    parentId: 0, // 默认为根
    deptName: undefined,
    orderNum: 0,
    status: "0"
  }
}

// 新增
const handleAdd = (row) => {
  reset()
  if (row) {
    form.value.parentId = row.deptId
  }
  open.value = true
  title.value = "添加部门"
}

// 修改
const handleEdit = (row) => {
  reset()
  form.value = { ...row }
  open.value = true
  title.value = "修改部门"
}

// 提交
const submitForm = () => {
  deptRef.value.validate(async valid => {
    if (valid) {
      // 校验：不能选自己当父级 (虽然 tree-select 还没完全禁用，但至少做个拦截)
      if (form.value.deptId && form.value.parentId === form.value.deptId) {
        ElMessage.error("上级部门不能选择自己")
        return
      }

      try {
        if (form.value.deptId) {
          await request.put('/system/dept', form.value)
          ElMessage.success("修改成功")
        } else {
          await request.post('/system/dept', form.value)
          ElMessage.success("新增成功")
        }
        open.value = false
        handleQuery()
      } catch (e) { console.error(e) }
    }
  })
}

const cancel = () => {
  open.value = false
  reset()
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`是否确认删除名称为"${row.deptName}"的数据项?`, "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(async () => {
    // 后端已经加了子部门和用户校验，如果有问题会抛出 500 error
    // request.js 拦截器会捕获并提示错误信息
    await request.delete(`/system/dept/${row.deptId}`)
    ElMessage.success("删除成功")
    handleQuery()
  })
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.filter-container {
  margin-bottom: 20px;
}
</style>
