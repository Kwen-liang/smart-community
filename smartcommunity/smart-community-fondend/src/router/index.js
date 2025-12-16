import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  // 修改点：从 sessionStorage 获取 Token
  const hasToken = sessionStorage.getItem('token')

  if (hasToken) {
    if (to.path === '/login') {
      // 1. 已登录，又去访问登录页 -> 强制跳回首页
      next({ path: '/' })
    } else {
      // 2. 已登录，访问其他页面 -> 放行
      next()
    }
  } else {
    // 3. 未登录
    if (to.meta.requiresAuth) {
      ElMessage.warning('会话已过期，请先登录')
      next(`/login?redirect=${to.fullPath}`)
    } else {
      next()
    }
  }
})

export default router
