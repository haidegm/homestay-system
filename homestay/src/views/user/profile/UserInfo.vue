<template>
  <div class="user-info-display">
    <div class="header-row">
      <h1 class="page-title">关于我</h1>
      <el-button v-if="hasInfo" @click="router.push('/user/profile/edit')" class="edit-btn-top">编辑</el-button>
    </div>

    <el-row :gutter="0" class="content-row">
      <el-col :span="10" class="left-card-col">
        <div class="id-card-floating">
          <div class="avatar-circle">
            <img v-if="userStore.avatarUrl" :src="userStore.avatarUrl" class="user-img" />
            <span v-else>爱</span>
          </div>
          <h2 class="user-nickname">{{ userStore.nickname || '旅行家' }}</h2>
          <p class="user-type">旅行家</p>
        </div>
      </el-col>

      <el-col :span="14" class="right-guide-col">
        
        <div v-if="!hasInfo" class="guide-content">
          <h3 class="guide-title">完善个人资料</h3>
          <p class="guide-desc">
            对于每笔订单，你的个人资料都是重要组成部分。请填写个人资料，让其他用户了解你。
          </p>
          <el-button class="start-btn" @click="router.push('/user/profile/edit')">开始</el-button>
        </div>

        <div v-else class="info-details-modules">
          <div class="info-module-item" v-if="userInfo.profession">
            <el-icon class="module-icon"><Briefcase /></el-icon>
            <div class="module-text">
              <div class="module-label">我的工作</div>
              <div class="module-value">{{ userInfo.profession }}</div>
            </div>
          </div>

          <div class="info-module-item" v-if="userInfo.targetLocation">
            <el-icon class="module-icon"><targetLocation /></el-icon>
            <div class="module-text">
              <div class="module-label">所在地</div>
              <div class="module-value">{{ userInfo.targetLocation }}</div>
            </div>
          </div>

          <div class="info-module-item" v-if="userInfo.about">
            <el-icon class="module-icon"><Document /></el-icon>
            <div class="module-text">
              <div class="module-label">自我介绍</div>
              <div class="module-value">{{ userInfo.about }}</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="footer-section">
      <div class="review-entry">
        <el-icon class="msg-icon"><ChatDotRound /></el-icon>
        <span class="entry-text">我撰写的评价</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Briefcase, Location, Document } from '@element-plus/icons-vue'
import request from '../../../utils/request'
import { useUserStore } from '../../../store/user'

const userStore = useUserStore()
const router = useRouter()

// 核心数据模型
const userInfo = ref({
  nickname: '',
  avatar: '',
  profession: '',
  targetLocation: '',
  about: ''
})

// 【关键切换逻辑】判断是否有实质性内容（决定右侧显示引导还是显示模块）
const hasInfo = computed(() => {
  // 只要上传了头像，或者填写了职业/介绍，就切换为展示态
  return !!(userInfo.value.avatar || userInfo.value.profession || userInfo.value.about)
})

// 处理图片完整路径
const fullAvatarUrl = computed(() => {
  if (!userInfo.value.avatar) return null

  const base = userInfo.value.avatar.startsWith('http')
    ? userInfo.value.avatar
    : `http://localhost:8080${userInfo.value.avatar}`

  return base + '?t=' + Date.now()
})

// 获取后端数据
const loadProfile = async () => {
  try {
    const res = await request.get('/api/user/profile/get')

    // 头像来自 user 表
    if (res.user) {
      userInfo.value.avatar = res.user.avatar
      userInfo.value.nickname = res.user.nickname
    }

    // 个人资料来自 profile 表
    if (res.profile) {
      userInfo.value.profession = res.profile.profession
      userInfo.value.targetLocation = res.profile.targetLocation
      userInfo.value.about = res.profile.bio
    }

    // 同步头像到导航栏
    if (userInfo.value.avatar) {
      localStorage.setItem('avatar', userInfo.value.avatar)
      window.dispatchEvent(new Event('storage-updated'))
    }

  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}
onMounted(async () => {
  await userStore.loadUser()
})

</script>

<style scoped>
/* 继承你刚才提供的全部 CSS 样式 */
.user-info-display { padding-right: 40px; }

.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 40px; }
.page-title { font-size: 32px; font-weight: 600; color: #222; }

.edit-btn-top {
  background: #f7f7f7; border: none; font-weight: 600; color: #222;
  padding: 10px 20px; border-radius: 8px;
}

/* 左侧身份证卡片 */
.left-card-col { padding-right: 60px; }
.id-card-floating {
  width: 100%; max-width: 320px; padding: 40px 20px;
  background: #ffffff; border-radius: 24px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid #f0f0f0; text-align: center;
}
.avatar-circle {
  width: 100px; height: 100px; background: #222222; color: #ffffff;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 40px; margin: 0 auto 20px; overflow: hidden;
}
.user-img { width: 100%; height: 100%; object-fit: cover; }

/* 右侧：模块化信息框样式 */
.info-details-modules {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.info-module-item {
  display: flex;
  align-items: center;
  padding: 18px 24px;
  border: 1px solid #EBEBEB;
  border-radius: 12px;
  transition: 0.2s;
}
.info-module-item:hover {
  background-color: #f9f9f9;
}
.module-icon { font-size: 24px; color: #222; margin-right: 16px; }
.module-text { display: flex; flex-direction: column; }
.module-label { font-size: 12px; color: #717171; }
.module-value { font-size: 16px; font-weight: 600; color: #222; }

/* 引导态样式保持不变 */
.guide-title { font-size: 22px; font-weight: 600; margin-bottom: 12px; }
.guide-desc { font-size: 16px; line-height: 1.5; color: #222; max-width: 320px; margin-bottom: 25px; }
.start-btn {
  background: #E31C5F; color: #fff; border: none; padding: 14px 35px;
  font-size: 16px; font-weight: 600; border-radius: 8px; cursor: pointer;
}

.footer-section { margin-top: 60px; padding-top: 30px; border-top: 1px solid #EBEBEB; }
.review-entry { display: flex; align-items: center; gap: 15px; cursor: pointer; }
</style>