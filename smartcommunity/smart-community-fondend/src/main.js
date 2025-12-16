import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 1. 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 引入 Element Plus 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 2. 引入 Leaflet 全局样式 (非常重要，否则地图会乱码)
import 'leaflet/dist/leaflet.css'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus) // 使用 Element Plus

app.mount('#app')
