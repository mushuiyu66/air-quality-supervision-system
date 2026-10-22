import './assets/main.scss'
import router from '@/router'
import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'

const app = createApp(App)
const pinia = createPinia()

// 应用启动时清除登录状态,确保每次启动都从登录页开始
localStorage.removeItem('role')

app.use(ElementPlus)
app.use(router)
app.use(pinia)

// 在 pinia 注册后获取 store 并清除信息
const userStore = useUserStore()
userStore.clearInfo()

app.mount('#app')