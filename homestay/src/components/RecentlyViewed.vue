<template>
  <div v-if="houses.length > 0" class="recently-viewed">
    <div class="section-header">
      <h2>最近浏览</h2>
      <div class="nav-buttons">
        <button 
          class="nav-btn" 
          @click="scrollLeft" 
          :disabled="scrollPosition <= 0"
        >
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <button 
          class="nav-btn" 
          @click="scrollRight" 
          :disabled="scrollPosition >= maxScroll"
        >
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>
    </div>

    <div class="scroll-container" ref="scrollContainer">
      <div class="houses-list" :style="{ transform: `translateX(-${scrollPosition}px)` }">
        <div 
          v-for="house in houses" 
          :key="house.houseId"
          class="house-card"
          @click="goToDetail(house.houseId)"
        >
          <div class="house-image">
            <img :src="getImageUrl(house.coverImage)" :alt="house.title">
          </div>
          <div class="house-info">
            <h3 class="house-title">{{ house.title }}</h3>
            <p class="house-location">{{ house.city }}</p>
            <div class="house-footer">
              <div class="house-price">
                <span class="price-amount">¥{{ house.price }}</span>
                <span class="price-unit">/晚</span>
              </div>
              <div v-if="house.rating > 0" class="house-rating">
                <el-icon><StarFilled /></el-icon>
                <span>{{ house.rating.toFixed(1) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight, StarFilled } from '@element-plus/icons-vue'
import request from '../utils/request'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const houses = ref([])
const scrollContainer = ref(null)
const scrollPosition = ref(0)

// 每次滚动的距离：卡片宽度(240) + 间距(20)
const SCROLL_STEP = 260

// 只在登录时加载
onMounted(() => {
  if (userStore.isLogin) {
    loadRecentlyViewed()
  }
})

const loadRecentlyViewed = async () => {
  try {
    const res = await request.get('/api/browse/recent-houses?limit=10')
    houses.value = res || []
  } catch (error) {
    console.error('加载最近浏览失败:', error)
    houses.value = []
  }
}

const maxScroll = computed(() => {
  if (!scrollContainer.value || houses.value.length === 0) return 0
  // 总宽度 - 可见宽度
  const totalWidth = houses.value.length * 260 - 20 // 最后一个不需要间距
  const visibleWidth = scrollContainer.value.offsetWidth
  return Math.max(0, totalWidth - visibleWidth)
})

const scrollLeft = () => {
  scrollPosition.value = Math.max(0, scrollPosition.value - SCROLL_STEP)
}

const scrollRight = () => {
  scrollPosition.value = Math.min(maxScroll.value, scrollPosition.value + SCROLL_STEP)
}

const getImageUrl = (imagePath) => {
  if (!imagePath) {
    return 'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=400&h=300&fit=crop'
  }
  return imagePath.startsWith('http') ? imagePath : `http://localhost:8080${imagePath}`
}

const goToDetail = (houseId) => {
  router.push(`/house/${houseId}`)
}
</script>

<style scoped>
.recently-viewed {
  max-width: 1280px;
  margin: 0 auto;
  padding: 48px 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  color: #222;
}

.nav-buttons {
  display: flex;
  gap: 12px;
}

.nav-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.nav-btn:hover:not(:disabled) {
  background: #f7f7f7;
  border-color: #222;
}

.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.scroll-container {
  overflow: hidden;
  position: relative;
}

.houses-list {
  display: flex;
  gap: 20px;
  transition: transform 0.3s ease;
}

.house-card {
  flex: 0 0 240px;
  cursor: pointer;
  transition: transform 0.2s;
}

.house-card:hover {
  transform: translateY(-4px);
}

.house-image {
  position: relative;
  width: 240px;
  height: 240px;
  border-radius: 12px;
  overflow: hidden;
  background: #f0f0f0;
  margin-bottom: 12px;
}

.house-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.house-info {
  padding: 0 4px;
}

.house-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #222;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.house-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #222;
}

.house-rating .el-icon {
  font-size: 12px;
  color: #FF385C;
}
</style>
