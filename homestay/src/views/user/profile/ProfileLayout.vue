<template>
  <div class="profile-page-wrapper">
    <div class="profile-content-inner">
      <el-row :gutter="0" class="main-row">
        <el-col :span="7" class="left-aside">
          <h1 class="aside-title">个人资料</h1>
          <div class="aside-menu">
            <div 
              class="menu-item" 
              :class="{ active: currentPath?.includes('info') }" 
              @click="router.push('/user/profile/info')"
            >
              <div class="icon-box black">W</div>
              <span>关于我</span>
            </div>

            <div 
              class="menu-item" 
              :class="{ active: currentPath?.includes('trips') }" 
              @click="router.push('/user/profile/trips')"
            >
              <div class="icon-box"><img src="https://img.icons8.com/color/48/suitcase.png" width="24"/></div>
              <span>过往行程</span>
            </div>

            <div 
              class="menu-item" 
              :class="{ active: currentPath?.includes('wishlist') }" 
              @click="router.push('/user/profile/wishlist')"
            >
              <div class="icon-box"><img src="https://img.icons8.com/color/48/hearts.png" width="24"/></div>
              <span>心愿单</span>
            </div>

            <div 
              class="menu-item" 
              :class="{ active: currentPath?.includes('reviews') }" 
              @click="router.push('/user/profile/reviews')"
            >
              <div class="icon-box"><img src="https://img.icons8.com/color/48/star--v1.png" width="24"/></div>
              <span>我的评价</span>
            </div>

            <div 
              class="menu-item" 
              :class="{ active: currentPath?.includes('messages') }" 
              @click="router.push('/user/profile/messages')"
            >
              <div class="icon-box"><img src="https://img.icons8.com/color/48/chat--v1.png" width="24"/></div>
              <span>消息中心</span>
              <el-badge 
                v-if="unreadCount > 0" 
                :value="unreadCount" 
                :max="99"
                class="message-badge"
              />
            </div>
          </div>
        </el-col>

        <el-col :span="17" class="right-content">
          <router-view />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../../utils/request'

const route = useRoute()
const router = useRouter()
const unreadCount = ref(0)

// 确保 path 存在，防止 includes 报错
const currentPath = computed(() => route?.path || '')

// 注入父组件（UserLayout）提供的刷新方法
const parentRefreshUnreadCount = inject('refreshUnreadCount', null)

// 加载未读消息数
const loadUnreadCount = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) return
    
    const res = await request.get('/api/messages/unread-count')
    unreadCount.value = res.unreadCount || 0
    
    // 同时刷新父组件（UserLayout）的未读数
    if (parentRefreshUnreadCount) {
      parentRefreshUnreadCount()
    }
  } catch (error) {
    console.error('获取未读消息数失败:', error)
    unreadCount.value = 0
  }
}

// 提供刷新方法给子组件（UserMessages）
provide('refreshUnreadCount', loadUnreadCount)

onMounted(() => {
  loadUnreadCount()
  // 每30秒刷新一次
  setInterval(loadUnreadCount, 30000)
})
</script>

<style scoped>
/* 全屏背景 */
.profile-page-wrapper { 
  padding-top: 80px; 
  min-height: 100vh; 
  background: #fff; 
}

/* 内容限宽居中，但不带边框 */
.profile-content-inner { 
  max-width: 1080px; 
  margin: 0 auto; 
}

.main-row { 
  min-height: 80vh; 
}

/* 左侧样式：去掉边框，改用轻微的边距 */
.left-aside { 
  padding: 40px 40px 40px 0; /* 左侧留空靠边 */
}

.aside-title { 
  font-size: 32px; 
  font-weight: 600; 
  margin-bottom: 40px; 
  color: #222;
}


.menu-item {
  display: flex; 
  align-items: center; 
  gap: 15px; 
  padding: 12px 15px;
  border-radius: 12px; 
  cursor: pointer; 
  margin-bottom: 8px; 
  transition: 0.2s;
}

.menu-item:hover { 
  background: #f7f7f7; 
}

.menu-item.active { 
  background: #f7f7f7; 
  font-weight: 600; 
}

.icon-box { 
  width: 32px; 
  height: 32px; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  border-radius: 50%; 
}

.icon-box.black { 
  background: #222; 
  color: #fff; 
  font-size: 12px; 
}

.menu-item span { 
  font-size: 16px; 
  color: #222; 
  flex: 1;
}

.message-badge {
  margin-left: auto;
}

.message-badge :deep(.el-badge__content) {
  background-color: #FF385C;
  border: none;
  font-size: 11px;
  height: 18px;
  line-height: 18px;
  padding: 0 6px;
}

/* 右侧样式：通过左边框或间距与导航区分 */
.right-content { 
  padding: 40px 0 40px 60px; 
  border-left: 1px solid #ebebeb; /* 只有这一根竖线 */
}
</style>