<template>
  <div class="app-wrapper">
    <!-- 左侧侧边栏 (深色风格) -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo-container">
        <div class="logo-icon">
          <el-icon>
            <DataAnalysis />
          </el-icon>
        </div>
        <transition name="fade">
          <span class="logo-text" v-if="!isCollapsed">智慧社区 GIS</span>
        </transition>
      </div>

      <el-menu :default-active="activeRoute" class="el-menu-vertical" background-color="#0f172a" text-color="#94a3b8"
        active-text-color="#fff" :collapse="isCollapsed" unique-opened router>
        <!-- 遍历路由生成菜单 -->
        <template v-for="route in menuRoutes" :key="route.path">
          <!-- 单级菜单 -->
          <el-menu-item v-if="!route.children || route.children.length === 1" :index="route.redirect || route.path">
            <el-icon v-if="route.children?.[0]?.meta?.icon || route.meta?.icon">
              <component :is="route.children?.[0]?.meta?.icon || route.meta?.icon" />
            </el-icon>
            <template #title>{{ route.children?.[0]?.meta?.title || route.meta?.title }}</template>
          </el-menu-item>

          <!-- 多级菜单 -->
          <el-sub-menu v-else :index="route.path">
            <template #title>
              <el-icon v-if="route.meta?.icon">
                <component :is="route.meta?.icon" />
              </el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            <el-menu-item v-for="child in route.children" :key="child.path"
              :index="resolvePath(route.path, child.path)">
              {{ child.meta?.title }}
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>

      <!-- 底部用户信息 (动态渲染 & 美化) -->
      <div class="user-profile">
        <div class="user-avatar-area">
          <el-avatar :size="isCollapsed ? 32 : 38" :src="userInfo.avatar" class="custom-avatar">
            {{ userInfo.nickname ? userInfo.nickname.charAt(0).toUpperCase() : 'U' }}
          </el-avatar>
        </div>

        <transition name="fade">
          <div class="user-details" v-if="!isCollapsed">
            <div class="username" :title="userInfo.nickname">{{ userInfo.nickname || '未登录用户' }}</div>
            <div class="role-badge">{{ userInfo.deptName || '暂无部门信息' }}</div>
          </div>
        </transition>

        <transition name="fade">
          <div class="user-actions" v-if="!isCollapsed">
            <el-tooltip content="退出登录" placement="right">
              <div class="action-btn logout" @click="handleLogout">
                <el-icon>
                  <SwitchButton />
                </el-icon>
              </div>
            </el-tooltip>
          </div>
        </transition>
      </div>
    </aside>

    <!-- 右侧主体 -->
    <div class="main-content">
      <header class="header">
        <div class="left">
          <el-icon class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="right">
          <el-badge is-dot class="item">
            <el-icon class="bell-icon">
              <Bell />
            </el-icon>
          </el-badge>
          <div class="user-welcome">
            Hi, {{ userInfo.nickname }}
          </div>
          <span class="date">{{ currentTime }}</span>
        </div>
      </header>

      <main class="content-body">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { routes } from '@/router/routes'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const isCollapsed = ref(false)
const currentTime = ref('')
let timer = null

// 用户信息响应式对象
const userInfo = reactive({
  nickname: '',
  username: '',
  deptName: '',
  avatar: ''
})

const menuRoutes = computed(() => routes.filter(r => !r.hidden))
const activeRoute = computed(() => route.path)
const currentRouteName = computed(() => route.meta.title)

const resolvePath = (base, path) => `${base}/${path}`.replace('//', '/')

const handleLogout = () => {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('userInfo') // 清除用户信息
  router.push('/login')
}

// 加载用户信息
const loadUserInfo = () => {
  try {
    const storedInfo = sessionStorage.getItem('userInfo')
    if (storedInfo) {
      const user = JSON.parse(storedInfo)
      userInfo.nickname = user.nickname || user.username || 'Admin'
      userInfo.username = user.username
      userInfo.deptName = user.deptName || (user.roleKey === 'admin' ? '系统管理员' : '社区网格员')
      userInfo.avatar = user.avatar
    } else {
      userInfo.nickname = '访客'
      userInfo.deptName = '未登录'
    }
  } catch (e) {
    console.error('读取用户信息失败', e)
  }
}

onMounted(() => {
  loadUserInfo()
  timer = setInterval(() => {
    currentTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  }, 1000)
})

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
  background-color: #f1f5f9;
}

/* 侧边栏 */
.sidebar {
  width: 240px;
  background-color: #0f172a;
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.sidebar.collapsed {
  width: 64px;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  background-color: #020617;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  overflow: hidden;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 12px;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.logo-text {
  color: white;
  font-weight: 700;
  font-size: 16px;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

/* 菜单样式重写 */
.el-menu-vertical {
  border-right: none;
  flex: 1;
  padding-top: 10px;
}

:deep(.el-menu-item) {
  margin: 4px 8px;
  border-radius: 6px;
  height: 44px;
  line-height: 44px;
}

:deep(.el-menu-item.is-active) {
  background-color: #1e293b !important;
  color: #60a5fa !important;
  font-weight: 500;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.05) !important;
}

:deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.05) !important;
}

/* 用户信息区域美化 */
.user-profile {
  margin: 16px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.sidebar.collapsed .user-profile {
  margin: 16px 8px;
  padding: 8px;
  justify-content: center;
}

.custom-avatar {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  font-weight: bold;
  font-size: 16px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.user-details {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.username {
  color: #e2e8f0;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.2;
}

.role-badge {
  color: #94a3b8;
  font-size: 11px;
  margin-top: 4px;
  background: rgba(255, 255, 255, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  align-self: flex-start;
  white-space: nowrap;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-btn.logout {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #94a3b8;
  transition: all 0.2s;
}

.action-btn.logout:hover {
  background-color: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

/* 主体内容 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 64px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #64748b;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #3b82f6;
}

.right {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 14px;
  color: #64748b;
}

.bell-icon {
  font-size: 20px;
  cursor: pointer;
  color: #64748b;
  transition: transform 0.3s;
}

.bell-icon:hover {
  transform: rotate(15deg);
  color: #3b82f6;
}

.user-welcome {
  font-weight: 500;
  color: #334155;
  background: #f1f5f9;
  padding: 4px 12px;
  border-radius: 20px;
}

.content-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  position: relative;
}

/* 动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
