import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    open: true,
    // 核心配置：跨域代理
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端地址
        changeOrigin: true, // 允许跨域
        // 注意：因为后端 application.yml 配置了 context-path: /api
        // 所以这里不需要 rewrite 去掉 /api 前缀，直接透传即可
      }
    }
  }
})
