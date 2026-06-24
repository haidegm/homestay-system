<template>
  <div class="recommended-section">
    <div class="section-header">
      <h2>{{ title }}</h2>
      <p class="subtitle">{{ subtitle }}</p>
    </div>

    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载推荐中...</span>
    </div>

    <div v-else-if="houses.length > 0" class="houses-grid">
      <div 
        v-for="house in houses" 
        :key="house.houseId"
        class="house-card"
        @click="goToDetail(house.houseId)"
      >
        <div class="house-image">
          <img :src="getImageUrl(house.coverImage)" :alt="house.title">
          <div class="wishlist-btn">
            <el-icon><StarFilled /></el-icon>
          </div>
        </div>
        
        <div class="house-info">
          <div class="house-header">
            <h3 class="house-title">{{ house.title }}</h3>
            <div class="house-rating" v-if="house.rating > 0">
              <el-icon><StarFilled /></el-icon>
              <span>{{ house.rating.toFixed(1) }}</span>
            </div>
          </div>
          
          <p class="house-location">{{ house.city }}</p>
          
          <div class="house-footer">
            <div class="house-price">
              <span class="price-amount">¥{{ house.price }}</span>
              <span class="price-unit">/晚</span>
            </div>
            <div v-if="showScore && house.score" class="recommendation-score">
              推荐度: {{ (house.score * 100).toFixed(1) }}%
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else description="暂无推荐房源" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Loading, StarFilled } from '@element-plus/icons-vue'
import request from '../utils/request'

const props = defineProps({
  title: {
    type: String,
    default: '为你推荐'
  },
  subtitle: {
    type: String,
    default: '基于你的浏览和收藏记录'
  },
  apiUrl: {
    type: String,
    required: true
  },
  showScore: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const houses = ref([])

onMounted(() => {
  loadRecommendations()
})

// 监听路由变化，重新加载推荐
watch(() => route.path, () => {
  if (route.path === '/') {
    console.log('首页路由变化，重新加载推荐')
    loadRecommendations()
  }
})

const loadRecommendations = async () => {
  loading.value = true
  try {
    // 添加时间戳防止缓存
    const timestamp = new Date().getTime()
    const url = `${props.apiUrl}${props.apiUrl.includes('?') ? '&' : '?'}_t=${timestamp}`
    const res = await request.get(url)
    houses.value = res || []
    console.log('推荐房源加载成功:', houses.value.length, '个')
  } catch (error) {
    console.error('加载推荐失败:', error)
  } finally {
    loading.value = false
  }
}

const getImageUrl = (imagePath) => {
  if (!imagePath) {
    return 'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=400&h=300&fit=crop'
  }
  return imagePath.startsWith('http') ? imagePath : `http://localhost:8080${imagePath}`
}

const goToDetail = (houseId) => {
  // 如果是在房源详情页内跳转到另一个房源，需要强制刷新
  const currentPath = router.currentRoute.value.path
  const targetPath = `/house/${houseId}`
  
  if (currentPath.startsWith('/house/')) {
    // 在房源详情页内跳转，使用replace然后push来强制刷新
    router.push(targetPath)
  } else {
    // 从其他页面跳转
    router.push(targetPath)
  }
}
</script>

<style scoped>
.recommended-section {
  max-width: 1280px;
  margin: 0 auto;
  padding: 48px 40px;
}

.section-header {
  margin-bottom: 32px;
}

.section-header h2 {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #222;
}

.subtitle {
  font-size: 16px;
  color: #717171;
  margin: 0;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  gap: 16px;
}

.houses-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.house-card {
  width: 100%;
  cursor: pointer;
  transition: transform 0.2s;
}

.house-card:hover {
  transform: translateY(-4px);
}

.house-image {
  width: 100%;
  height: 280px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 12px;
  background: #f0f0f0;
}

.house-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.wishlist-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.wishlist-btn:hover {
  transform: scale(1.1);
  background: white;
}

.house-info {
  padding: 0 4px;
}

.house-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 4px;
}

.house-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0;
  color: #222;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 8px;
}

.house-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #222;
  flex-shrink: 0;
}

.house-rating .el-icon {
  font-size: 12px;
}

.house-location {
  font-size: 14px;
  color: #717171;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.house-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.house-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-amount {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}

.price-unit {
  font-size: 14px;
  color: #717171;
}

.recommendation-score {
  font-size: 12px;
  color: #FF385C;
  font-weight: 500;
}

@media (max-width: 1280px) {
  .houses-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 950px) {
  .houses-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 550px) {
  .houses-grid {
    grid-template-columns: 1fr;
  }
}
</style>
