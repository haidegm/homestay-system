import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import { createPinia } from 'pinia'
import { useUserStore } from './store/user'

const app = createApp(App)

const pinia = createPinia()

app.use(ElementPlus)
app.use(pinia)
app.use(router)

app.mount('#app')

// 初始化用户信息
const userStore = useUserStore()

// 只有普通用户和房东才加载用户信息，管理员不需要
if (userStore.token && userStore.role !== 'ROLE_ADMIN') {
  userStore.loadUser()
}