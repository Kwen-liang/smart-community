<template>
  <div class="login-container">
    <!-- 动态背景装饰球 -->
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>
    <div class="bg-shape shape-3"></div>

    <div class="login-card">
      <!-- 左侧：3D 插画/品牌区 (保持不变) -->
      <div class="illustration-side">
        <div class="brand-content">
          <div class="logo-area">
            <el-icon class="logo-icon">
              <DataAnalysis />
            </el-icon>
            <span class="logo-text">智慧社区 GIS</span>
          </div>
          <div class="illustration-text">
            <h2>数字孪生 · 精细治理</h2>
            <p>基于 PostGIS 与 WebGL 的新一代网格化管理平台</p>
          </div>
          <img
            src="https://images.unsplash.com/photo-1573164713988-8665fc963095?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
            alt="Smart City 3D Illustration" class="hero-image" />
          <div class="glass-overlay"></div>
        </div>
      </div>

      <!-- 右侧：登录表单 -->
      <div class="form-side">
        <div class="form-header">
          <h3>欢迎登录</h3>
          <p class="sub-title">请输入您的管理员账号</p>
        </div>

        <el-form class="login-form" @keyup.enter="handleLogin">
          <el-form-item class="input-item">
            <div class="input-label">账号 / Username</div>
            <el-input v-model="loginForm.username" placeholder="请输入账号 (admin)" size="large" :prefix-icon="User"
              class="custom-input" />
          </el-form-item>

          <el-form-item class="input-item">
            <div class="input-label">密码 / Password</div>
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" :prefix-icon="Lock"
              show-password class="custom-input" />
          </el-form-item>

          <div class="form-footer">
            <el-checkbox label="记住我" class="remember-me" />
          </div>

          <el-button type="primary" class="login-btn" size="large" :loading="loading" @click="handleLogin">
            登 录 <el-icon class="el-icon--right">
              <ArrowRight />
            </el-icon>
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, DataAnalysis, ArrowRight } from '@element-plus/icons-vue'
import request from '@/utils/request' // 引入封装好的 axios

const router = useRouter()
const route = useRoute()

const loginForm = reactive({
  username: 'admin',
  password: ''
})
const loading = ref(false)

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    return ElMessage.warning('请输入完整的账号和密码')
  }

  loading.value = true

  try {
    // 【修改点】调用真实后端接口
    // 后端 Controller 地址: /auth/login
    const res = await request.post('/auth/login', {
      username: loginForm.username,
      password: loginForm.password
    })

    if (res.code === 200) {
      ElMessage.success('登录成功，欢迎回来')
      // 保存 Token 到 SessionStorage
      sessionStorage.setItem('token', res.data.token)
      // 保存用户信息 (可选)
      sessionStorage.setItem('userInfo', JSON.stringify(res.data.user))

      // 跳转
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    } else {
      ElMessage.error(res.msg || '登录失败')
    }
  } catch (error) {
    console.error(error)
    // request.js 拦截器会处理通用错误，这里可以不写或做特殊处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 样式保持原文件不变，此处省略以节省空间，直接复用原样式即可 */
.login-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #0f172a;
  background-image:
    radial-gradient(at 0% 0%, hsla(253, 16%, 7%, 1) 0, transparent 50%),
    radial-gradient(at 50% 0%, hsla(225, 39%, 30%, 1) 0, transparent 50%),
    radial-gradient(at 100% 0%, hsla(339, 49%, 30%, 1) 0, transparent 50%);
  position: relative;
  overflow: hidden;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
}

/* ... (保留原文件的 CSS 内容) ... */
.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  animation: float 10s infinite ease-in-out;
}

.shape-1 {
  width: 400px;
  height: 400px;
  background: #4f46e5;
  top: -100px;
  left: -100px;
  opacity: 0.4;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: #ec4899;
  bottom: -50px;
  right: -50px;
  opacity: 0.3;
  animation-delay: -2s;
}

.shape-3 {
  width: 200px;
  height: 200px;
  background: #06b6d4;
  top: 40%;
  left: 40%;
  opacity: 0.2;
  animation-delay: -5s;
}

@keyframes float {

  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(20px, -20px);
  }
}

.login-card {
  width: 1000px;
  height: 600px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255, 255, 255, 0.1);
  display: flex;
  overflow: hidden;
  z-index: 1;
  position: relative;
  transform: perspective(1000px) rotateX(0deg);
  transition: transform 0.3s ease;
}

.illustration-side {
  flex: 1;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 40px;
  color: white;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 1px;
}

.illustration-text {
  margin-top: 40px;
}

.illustration-text h2 {
  font-size: 32px;
  margin-bottom: 12px;
  font-weight: 800;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.illustration-text p {
  font-size: 16px;
  opacity: 0.9;
  font-weight: 300;
}

.hero-image {
  position: absolute;
  bottom: -50px;
  right: -50px;
  width: 120%;
  height: auto;
  object-fit: cover;
  transform: rotate(-15deg) translateY(20px);
  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 1) 50%, rgba(0, 0, 0, 0) 100%);
  opacity: 0.8;
  mix-blend-mode: overlay;
  pointer-events: none;
}

.glass-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0) 100%);
  z-index: 1;
}

.form-side {
  width: 450px;
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #ffffff;
}

.form-header {
  margin-bottom: 40px;
}

.form-header h3 {
  font-size: 28px;
  color: #1e293b;
  margin-bottom: 8px;
  font-weight: 700;
}

.sub-title {
  color: #64748b;
  font-size: 14px;
}

.input-item {
  margin-bottom: 24px;
}

.input-label {
  font-size: 14px;
  color: #475569;
  margin-bottom: 8px;
  font-weight: 500;
}

:deep(.custom-input .el-input__wrapper) {
  background-color: #f8fafc;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  padding: 8px 15px;
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.custom-input .el-input__wrapper:hover) {
  background-color: #fff;
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: 0 0 0 2px #3b82f6 inset !important;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  font-size: 14px;
}

.forgot-pwd {
  color: #3b82f6;
  cursor: pointer;
  transition: color 0.2s;
}

.forgot-pwd:hover {
  color: #2563eb;
  text-decoration: underline;
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(90deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  transition: transform 0.1s, box-shadow 0.2s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -10px rgba(59, 130, 246, 0.5);
  background: linear-gradient(90deg, #2563eb 0%, #1d4ed8 100%);
}

.login-btn:active {
  transform: translateY(0);
}

@media (max-width: 1024px) {
  .login-card {
    width: 90%;
    height: auto;
    flex-direction: column;
  }

  .illustration-side {
    height: 200px;
    padding: 24px;
  }

  .illustration-text,
  .hero-image {
    display: none;
  }

  .form-side {
    width: 100%;
    padding: 40px 24px;
  }
}
</style>
