<template>
  <el-container class="admin-layout">
    <!-- 顶部导航栏 -->
    <el-header class="top-header">
      <div class="header-content">
        <div class="logo-section">
          <img src="../assets/images/田园犬.png" class="logo-img">
          <span class="logo-text">W的homestay</span>
          <el-tag type="danger" size="small" style="margin-left: 12px">管理员</el-tag>
        </div>

        <div class="header-actions">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-menu">
              <el-icon><Menu /></el-icon>
              <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <div class="user-info-dropdown">
                  <el-avatar :size="48" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                  <div class="user-details">
                    <div class="username">管理员</div>
                    <div class="user-role">系统管理员</div>
                  </div>
                </div>
                <el-divider style="margin: 8px 0" />
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
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>控制台</span>
          </el-menu-item>

          <el-menu-item index="/admin/hosts">
            <el-icon><UserFilled /></el-icon>
            <span>房东管理</span>
          </el-menu-item>

          <el-menu-item index="/admin/houses">
            <el-icon><House /></el-icon>
            <span>房源管理</span>
          </el-menu-item>

          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>

          <el-menu-item index="/admin/orders">
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  Menu, SwitchButton, DataAnalysis, UserFilled,
  House, User, Document
} from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      // 使用userStore的logout方法清除所有状态
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/user')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.admin-layout {
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
