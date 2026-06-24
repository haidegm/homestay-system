<template>
  <el-container class="host-layout">
    <!-- 顶部导航栏 -->
    <el-header class="top-header">
      <div class="header-content">
        <div class="logo-section" @click="router.push('/user')">
          <img src="../assets/images/田园犬.png" class="logo-img">
          <span class="logo-text">W的homestay</span>
        </div>

        <div class="header-actions">
          <el-button text class="switch-btn" @click="router.push('/user')">
            <el-icon><House /></el-icon>
            切换到旅行模式
          </el-button>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-menu">
              <el-icon><Menu /></el-icon>
              <el-avatar :size="32" :src="userAvatar" />
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <div class="user-info-dropdown">
                  <el-avatar :size="48" :src="userAvatar" />
                  <div class="user-details">
                    <div class="username">{{ username }}</div>
                    <div class="user-role">房东账号</div>
                  </div>
                </div>
                <el-divider style="margin: 8px 0" />
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  账号设置
                </el-dropdown-item>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <el-container class="main-container">
      <!-- 左侧导航 -->
      <el-aside width="240px" class="sidebar">
        <el-menu
          :default-active="route.path"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/host/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>控制台</span>
          </el-menu-item>

          <el-menu-item index="/host/house/list">
            <el-icon><House /></el-icon>
            <span>房源列表</span>
          </el-menu-item>

          <el-menu-item index="/host/house/add">
            <el-icon><Plus /></el-icon>
            <span>新增房源</span>
          </el-menu-item>

          <el-menu-item index="/host/order">
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </el-menu-item>

          <el-menu-item index="/host/income">
            <el-icon><Money /></el-icon>
            <span>收入统计</span>
          </el-menu-item>

          <el-menu-item index="/host/review">
            <el-icon><Star /></el-icon>
            <span>评价管理</span>
          </el-menu-item>

          <el-menu-item index="/host/hostmessage" class="message-menu-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>消息中心</span>
            <el-badge 
              v-if="unreadCount > 0" 
              :value="unreadCount" 
              :max="99"
              class="message-badge"
            />
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="content-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, watch, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  House, Menu, User, SwitchButton, DataAnalysis, 
  Plus, Document, Money, Star, ChatDotRound 
} from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const unreadCount = ref(0)

// 定义加载未读数的方法（必须在使用前定义）
const loadUnreadCount = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      return
    }
    
    const res = await request.get('/api/messages/unread-count')
    unreadCount.value = res.unreadCount || 0
  } catch (error) {
    console.error('获取未读消息数失败:', error)
    // 静默失败，不影响用户体验
    unreadCount.value = 0
  }
}

// 提供刷新未读数的方法给子组件
provide('refreshUnreadCount', loadUnreadCount)

// 监听路由变化，当进入消息中心时刷新未读数
watch(() => route.path, (newPath) => {
  if (newPath === '/host/hostmessage') {
    // 延迟刷新，等待消息标记为已读
    setTimeout(() => loadUnreadCount(), 1000)
  }
})

const userAvatar = computed(() => {
  const avatar = userStore.avatarUrl
  if (!avatar || avatar.includes('default')) {
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
  return avatar
})

onMounted(async () => {
  // 加载用户信息
  username.value = localStorage.getItem('username') || '房东'
  
  // 如果store中没有用户信息，尝试加载
  if (!userStore.nickname) {
    await userStore.loadUser()
  }
  
  if (userStore.nickname) {
    username.value = userStore.nickname
  }
  
  // 立即加载未读消息数（不需要检查角色，因为已经在房东布局中）
  await loadUnreadCount()
  
  // 每30秒刷新一次未读消息数
  setInterval(loadUnreadCount, 30000)
})

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/host/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/user')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.host-layout {
  height: 100vh;
  background: #f7f7f7;
}

/* 顶部导航栏 */
.top-header {
  background: white;
  border-bottom: 1px solid #ebebeb;
  padding: 0;
  height: 80px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
}

.header-content {
  width: 100%;
  padding: 0 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-section {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: opacity 0.2s;
}

.logo-section:hover {
  opacity: 0.8;
}

.logo-img {
  width: 32px;
  height: 32px;
  margin-right: 8px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #FF385C;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.switch-btn {
  font-size: 14px;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 22px;
  transition: background 0.2s;
}

.switch-btn:hover {
  background: #f7f7f7;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 5px 5px 5px 12px;
  border: 1px solid #DDDDDD;
  border-radius: 21px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.user-menu:hover {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.18);
}

.user-info-dropdown {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #222;
}

.user-role {
  font-size: 12px;
  color: #717171;
}

/* 主容器 */
.main-container {
  height: calc(100vh - 80px);
}

/* 侧边栏 */
.sidebar {
  background: white;
  border-right: 1px solid #ebebeb;
  overflow-y: auto;
}

.sidebar-menu {
  border: none;
  padding: 16px 0;
}

.sidebar-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  margin: 4px 12px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
}

.sidebar-menu .el-menu-item:hover {
  background: #f7f7f7;
}

.sidebar-menu .el-menu-item.is-active {
  background: #f7f7f7;
  color: #222;
}

.sidebar-menu .el-menu-item .el-icon {
  font-size: 18px;
  margin-right: 12px;
}

.message-menu-item {
  position: relative;
}

.message-badge {
  position: absolute;
  right: 16px;
  top: 20%;
  transform: translateY(-50%);
}

.message-badge :deep(.el-badge__content) {
  background-color: #FF385C;
  border: none;
  font-size: 11px;
  height: 18px;
  line-height: 18px;
  padding: 0 6px;
}

/* 主内容区 */
.content-main {
  background: #f7f7f7;
  padding: 24px;
  overflow-y: auto;
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu__item) {
  padding: 10px 16px;
  font-size: 14px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 8px;
  font-size: 16px;
}
</style>