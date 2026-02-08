import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const savedTheme = localStorage.getItem('theme') || 'neon'
document.documentElement.setAttribute('data-theme', savedTheme)

createApp(App).use(router).use(ElementPlus).mount('#app')
