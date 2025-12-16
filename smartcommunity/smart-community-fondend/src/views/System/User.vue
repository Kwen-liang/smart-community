<template>
  <div class="app-container">
    <!-- 头部搜索与操作栏 -->
    <el-card class="filter-container" shadow="never">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <!-- 新增：所属部门筛选 -->
        <el-form-item label="所属部门">
          <el-tree-select v-model="queryParams.deptId" :data="deptOptions"
            :props="{ value: 'deptId', label: 'deptName', children: 'children' }" value-key="deptId" placeholder="选择部门"
            check-strictly clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="success" icon="Plus" @click="handleAdd">新增用户</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户数据表格 -->
    <el-card class="table-container" shadow="never">
      <el-table :data="userList" border style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="姓名" width="120" />
        <el-table-column prop="deptName" label="所属部门/网格" min-width="180">
          <template #default="scope">
            <el-tag effect="plain" type="info">{{ scope.row.deptName || '暂无部门' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roleKey" label="角色" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.roleKey === 'admin' ? 'danger' : 'primary'" effect="light">
              {{ scope.row.roleKey === 'admin' ? '管理员' : '网格员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="150" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <!-- 状态切换：调用独立接口 -->
            <el-switch v-model="scope.row.status" active-value="1" inactive-value="0" inline-prompt active-text="启"
              inactive-text="停" @change="handleStatusChange(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="warning" size="small" icon="Key" @click="handleResetPwd(scope.row)">重置密码</el-button>
            <el-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(scope.row)">
              <template #reference>
                <el-button link type="danger" size="small" icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery" @current-change="handleQuery" />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" append-to-body destroy-on-close>
      <el-form :model="form" ref="userFormRef" :rules="rules" label-width="80px">
        <el-form-item label="归属部门" prop="deptId">
          <el-tree-select v-model="form.deptId" :data="deptOptions"
            :props="{ value: 'deptId', label: 'deptName', children: 'children' }" value-key="deptId"
            placeholder="请选择归属部门" check-strictly style="width: 100%" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="登录账号" :disabled="!!form.userId" />
        </el-form-item>
        <el-form-item label="姓名" prop="nickname">
          <el-input v-model="form.nickname" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="roleKey">
          <el-radio-group v-model="form.roleKey">
            <el-radio label="admin">管理员</el-radio>
            <el-radio label="user">网格员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="1">正常</el-radio>
            <el-radio label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, toRefs, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const userFormRef = ref(null)
const userList = ref([])
const deptOptions = ref([]) // 部门树选项

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    username: '',
    phone: '',
    deptId: null
  },
  form: {},
  rules: {
    deptId: [{ required: true, message: '请选择归属部门', trigger: 'change' }],
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    nickname: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
    roleKey: [{ required: true, message: '请选择角色', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

// 获取部门树
const getDeptTree = async () => {
  try {
    const res = await request.get('/system/dept/list')
    if (res.code === 200) {
      deptOptions.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

// 查询列表
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await request.get('/system/user/list', {
      params: queryParams.value
    })
    if (res.code === 200) {
      userList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    username: '',
    phone: '',
    deptId: null
  }
  handleQuery()
}

// 状态修改 (对接后端 /changeStatus)
const handleStatusChange = async (row) => {
  const text = row.status === '1' ? '启用' : '停用'
  try {
    await request.put('/system/user/changeStatus', { userId: row.userId, status: row.status })
    ElMessage.success(`已${text}用户 ${row.username}`)
  } catch (error) {
    console.error(error)
    row.status = row.status === '1' ? '0' : '1' // 失败回滚
  }
}

// 重置密码 (对接后端 /resetPwd)
const handleResetPwd = (row) => {
  ElMessageBox.prompt(`请输入用户 "${row.username}" 的新密码`, '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'password',
    inputPattern: /^.{6,20}$/,
    inputErrorMessage: '密码长度需在6-20位之间',
    inputValue: '123456' // 默认值
  }).then(async ({ value }) => {
    try {
      await request.put('/system/user/resetPwd', { userId: row.userId, password: value })
      ElMessage.success('密码重置成功')
    } catch (e) { console.error(e) }
  }).catch(() => { })
}

// 新增/编辑
const handleAdd = () => {
  dialogTitle.value = '新增用户'
  form.value = { status: '1', roleKey: 'user' }
  getDeptTree() // 刷新部门数据
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  form.value = { ...row }
  getDeptTree()
  dialogVisible.value = true
}

const submitForm = () => {
  userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (form.value.userId) {
          await request.put('/system/user', form.value)
          ElMessage.success('修改成功')
        } else {
          await request.post('/system/user', form.value)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        handleQuery()
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = async (row) => {
  try {
    await request.delete(`/system/user/${row.userId}`)
    ElMessage.success('删除成功')
    handleQuery()
  } catch (error) { console.error(error) }
}

onMounted(() => {
  handleQuery()
  getDeptTree()
})
</script>

<style scoped>
.filter-container {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
