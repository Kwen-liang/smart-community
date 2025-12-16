<template>
  <div class="not-found-container">
    <div class="content">
      <div class="error-code">404</div>
      <div class="error-desc">抱歉，您访问的页面迷路了 (Page Not Found)</div>
      <div class="actions">
        <el-button type="primary" size="large" @click="$router.push('/')">返回首页</el-button>
        <el-button size="large" @click="$router.go(-1)">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.not-found-container {
  height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  background-image: radial-gradient(#e2e8f0 2px, transparent 2px);
  background-size: 40px 40px;
}

.content {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.error-code {
  font-size: 120px;
  font-weight: 900;
  color: #1e293b;
  line-height: 1;
  margin-bottom: 20px;
  text-shadow: 4px 4px 0px #cbd5e1;
}

.error-desc {
  font-size: 18px;
  color: #64748b;
  margin-bottom: 40px;
}

.actions {
  display: flex;
  gap: 20px;
  justify-content: center;
}
</style>
