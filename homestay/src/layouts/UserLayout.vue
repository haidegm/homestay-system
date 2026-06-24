<template>
  <el-container>
    <el-header class="header" :class="{ 'expanded': isExpanded }">
      <div class="header-inner">
        <div class="logo-wrapper" @click="router.push('/')">
          <img src="../assets/images/田园犬.png" class="logo-img">
          <span class="logo-text">W的homestay</span>
        </div>

        <div class="header-search-container">
          <transition name="fade">
            <div class="header-search" v-if="showStickySearch && !isProfilePage">
              <StickySearch />
            </div>
          </transition>
        </div>

        <div class="auth-container">
          <!-- 房东用户：显示切换到出租模式按钮 -->
          <div v-if="isLogin && userRole === 'ROLE_HOST'" class="host-mode-btn" @click="router.push('/host')">
            <el-icon><House /></el-icon>
            <span>切换至出租模式</span>
          </div>

          <!-- 普通用户：显示消息中心按钮 -->
          <div v-else-if="isLogin" class="message-btn" @click="router.push('/user/profile/messages')">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
              <el-icon :size="20"><ChatDotRound /></el-icon>
            </el-badge>
          </div>

          <el-dropdown trigger="click" placement="bottom-end" @command="handleCommand">
            <div class="airbnb-user-btn">
              <el-icon :size="16"><Menu /></el-icon>
              <el-avatar :size="30" :src="userAvatar" />
            </div>
            
            <template #dropdown>
              <el-dropdown-menu class="airbnb-dropdown">
                <template v-if="!isLogin">
                  <el-dropdown-item command="login" class="bold-text">登录</el-dropdown-item>
                  <el-dropdown-item command="register">注册</el-dropdown-item>
                </template>
                <template v-else>
                  <el-dropdown-item command="profile" class="bold-text">个人资料</el-dropdown-item>
                  <el-dropdown-item command="orders">我的行程</el-dropdown-item>
                  <el-dropdown-item command="wishlist">心愿单</el-dropdown-item>
                  <el-dropdown-item command="messages">
                    <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
                      <span>消息中心</span>
                      <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" style="margin-left: 8px;" />
                    </div>
                  </el-dropdown-item>
                  <el-divider style="margin: 8px 0" />
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </template>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <el-main class="main-content">
      <router-view />
    </el-main>

    <LoginRegisterDialog 
      v-model:visible="dialogVisible" 
      v-model:dialogType="dialogType" 
    />

    <!-- AI客服 -->
    <AIChatBot />
  </el-container>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, provide } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Menu, House, ChatDotRound } from '@element-plus/icons-vue'
import StickySearch from '../components/StickySearch.vue'
import LoginRegisterDialog from '../components/LoginRegisterDialog.vue'
import AIChatBot from '../components/AIChatBot.vue'
import { useUserStore } from '../store/user'
import request from '../utils/request'

const userStore = useUserStore()

const router = useRouter()
const route = useRoute()

const showStickySearch = ref(false)
const isExpanded = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('login')
const unreadCount = ref(0)

// 判断是否处于个人中心
const isProfilePage = computed(() => {
  return route.path.includes('/user/profile')
})

// 用户基础状态
const isLogin = computed(() => userStore.isLogin)
const userRole = computed(() => localStorage.getItem('role') || '')

// --- 头像实时更新核心逻辑 ---

// 计算属性：处理头像显示路径及缓存问题
const userAvatar = computed(() => {
  const avatar = userStore.avatarUrl

  if (!avatar) {
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }

  const baseUrl = avatar.startsWith('http')
    ? avatar
    : `http://localhost:8080${avatar}`

  return `${baseUrl}?t=${Date.now()}`
})

// 加载未读消息数
const loadUnreadCount = async () => {
  if (!isLogin.value || userRole.value === 'ROLE_HOST') {
    unreadCount.value = 0
    return
  }
  
  try {
    const token = localStorage.getItem('token')
    if (!token) return
    
    const res = await request.get('/api/messages/unread-count')
    unreadCount.value = res.unreadCount || 0
  } catch (error) {
    console.error('获取未读消息数失败:', error)
    unreadCount.value = 0
  }
}

// 提供刷新未读数的方法给子组件
provide('refreshUnreadCount', loadUnreadCount)

const handleScroll = () => {
  if (isProfilePage.value) {
    showStickySearch.value = false
    isExpanded.value = false
    return
  }
  const scrollTop = window.scrollY
  showStickySearch.value = scrollTop > 300
  isExpanded.value = scrollTop > 50
}

watch(() => route.path, (newPath) => {
  if (newPath.includes('/user/profile')) {
    showStickySearch.value = false
    isExpanded.value = false
  }
  // 离开消息中心时刷新未读数
  if (!newPath.includes('/messages')) {
    loadUnreadCount()
  }
})

watch(() => isLogin.value, (newVal) => {
  if (newVal) {
    loadUnreadCount()
  } else {
    unreadCount.value = 0
  }
})

const handleCommand = (command) => {

  if (command === 'login') {
    dialogType.value = 'login'
    dialogVisible.value = true
  }

  else if (command === 'register') {
    dialogType.value = 'register'
    dialogVisible.value = true
  }

  else if (command === 'profile') {
    router.push('/user/profile/info')
  }

  else if (command === 'orders') {
    router.push('/user/profile/trips')
  }

  else if (command === 'wishlist') {
    router.push('/user/profile/wishlist')
  }

  else if (command === 'messages') {
    router.push('/user/profile/messages')
  }

  else if (command === 'logout') {
    userStore.logout()
    window.location.reload()
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  handleScroll()
  
  // 加载未读消息数
  if (isLogin.value && userRole.value !== 'ROLE_HOST') {
    loadUnreadCount()
    // 每30秒刷新一次
    setInterval(loadUnreadCount, 30000)
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: #FCFCFC;
  transition: all 0.3s ease;
  height: 80px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ebebeb;
}

.header.expanded {
  height: 100px;
  background: #fff;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.header-inner {
  width: 100%;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  min-width: 200px;
}

.logo-img {
  width: 32px;
  margin-right: 8px;
  transition: transform 0.3s;
}

.header.expanded .logo-img {
  transform: scale(1.1);
}

.logo-text {
  font-weight: bold;
  font-size: 16px;
}

.header-search-container {
  flex: 1;
  display: flex;
  justify-content: center;
  min-width: 400px; 
}

.auth-container {
  display: flex;
  align-items: center;
  gap: 15px;
  min-width: 200px;
  justify-content: flex-end;
}

.airbnb-user-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 5px 5px 5px 12px;
  border: 1px solid #DDDDDD;
  border-radius: 21px;
  cursor: pointer;
  background: #fff;
  transition: box-shadow 0.2s;
}

.airbnb-user-btn:hover {
  box-shadow: 0 2px 4px rgba(0,0,0,0.18);
}

.host-mode-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  padding: 10px 16px;
  border-radius: 22px;
  transition: background 0.2s;
}

.host-mode-btn:hover { 
  background: #f7f7f7; 
}

.message-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}

.message-btn:hover {
  background: #f7f7f7;
}

.message-btn :deep(.el-badge__content) {
  background-color: #FF385C;
  border: 2px solid #fff;
  font-size: 11px;
  height: 18px;
  line-height: 14px;
  padding: 0 5px;
  min-width: 18px;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.main-content {
  padding: 0;
}
</style>
