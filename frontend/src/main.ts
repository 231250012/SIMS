import './assets/main.css'
import './assets/compatibility.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// Pinia store
import { createPinia } from 'pinia'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// Create Vue app
const app = createApp(App)

// Use Pinia
app.use(createPinia())

// Use router
app.use(router)

// Use Element Plus
app.use(ElementPlus)

// Register Element Plus icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
