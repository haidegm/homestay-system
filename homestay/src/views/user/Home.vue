<template>
  <div class="hero">
    <!-- 中央内容 -->
    <div class="hero-content">
      <!-- 大标题 -->
      <h1 class="title" :style="titleStyle">你想去哪里？</h1>
      <!-- 搜索栏 -->
      <div class="search-bar" :style="searchStyle">
        <!-- 地点 -->
        <div class="search-item location">
          <span class="label">地点</span>
          <LocationSearch
            v-model="location"
            placeholder="输入城市、地标或位置"
            @select="handleLocationSelect"
          />
        </div>
        <!-- 日期 -->
        <div class="search-item date">
          <span class="label">日期</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="入住"
            end-placeholder="退房"
            size="large"
            :disabled-date="disabledDate"
          />
        </div>
        <!-- 人数 -->
        <div class="search-item guest">
          <span class="label">人数</span>
          <el-dropdown trigger="click">
            <span class="guest-text">
              {{ guestText }}
            </span>
            <template #dropdown>
              <el-dropdown-menu class="guest-panel">
                <div class="guest-row">
                  成人
                  <el-input-number v-model="guests.adult" :min="1" />
                </div>
                <div class="guest-row">
                  儿童
                  <el-input-number v-model="guests.child" :min="0" />
                </div>
                <div class="guest-row">
                  婴儿
                  <el-input-number v-model="guests.infant" :min="0" />
                </div>
                <div class="guest-row">
                  携带宠物
                  <el-switch v-model="guests.pet" />
                </div>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <!-- 搜索按钮 -->
        <div class="search-btn">
          <el-button type="primary" size="large" @click="handleSearch">
            搜索
          </el-button>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 最近浏览 -->
  <RecentlyViewed />
  
  <!-- 第二屏：推荐城市 -->
  <CityRecommend group="hot" />
  
  <!-- 推荐房源区域（仅登录用户显示） -->
  <RecommendedHouses 
    v-if="isLogin"
    title="为你推荐"
    subtitle="基于你的浏览和收藏记录"
    api-url="/api/recommend/for-me?limit=8"
    :show-score="true"
  />
  
  <!-- 第3屏：推荐城市 -->
<!-- //  <CityRecommend group="city" /> -->
  <!-- 第四屏：分类标签和地区展示 -->
  <CategoryTabs @categorySelected="handleCategoryChange" />
  <RegionList :selectedCategory="selectedCategory" />
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import CityRecommend from '../../components/CityRecommend.vue'
import CategoryTabs from '../../components/CategoryTabs.vue'
import RegionList from '../../components/RegionList.vue'
import LocationSearch from '../../components/LocationSearch.vue'
import RecommendedHouses from '../../components/RecommendedHouses.vue'
import RecentlyViewed from '../../components/RecentlyViewed.vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()

// 登录状态
const isLogin = computed(() => userStore.isLogin)

const location = ref('')
const dateRange = ref([])
const selectedLocation = ref(null) // 存储选中的位置信息

const guests = ref({
  adult: 1,
  child: 0,
  infant: 0,
  pet: false
})
const scrollY = ref(0)

// 动画发生的距离
const ANIMATE_DISTANCE = 300

const progress = computed(() => {
  return Math.min(scrollY.value / ANIMATE_DISTANCE, 1)
})

const handleScroll = () => {
  scrollY.value = window.scrollY
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const guestText = computed(() => {
  const g = guests.value
  let total = g.adult + g.child
  return total + ' 位房客'
})
/* 标题动画 */
const titleStyle = computed(() => {
  return {
    opacity: 1 - progress.value,
    transform: `translateY(${-30 * progress.value}px)`
  }
})

/* Hero 搜索动画 */
const searchStyle = computed(() => {
  return {
    opacity: 1 - progress.value,
    transform: `
      translateY(${-120 * progress.value}px)
      scale(${1 - 0.15 * progress.value})
    `
  }
})

const handleSearch = () => {
  if (!selectedLocation.value) {
    ElMessage.warning('请选择搜索位置')
    return
  }
  
  // 验证日期范围
  if (dateRange.value && dateRange.value.length === 2) {
    const checkIn = new Date(dateRange.value[0])
    const checkOut = new Date(dateRange.value[1])
    const diffTime = checkOut.getTime() - checkIn.getTime()
    const diffDays = diffTime / (1000 * 60 * 60 * 24)
    
    if (diffDays < 1) {
      ElMessage.warning('退房日期必须晚于入住日期至少一天')
      return
    }
  }
  
  // 格式化日期为 YYYY-MM-DD
  let formattedDateRange = []
  if (dateRange.value && dateRange.value.length === 2) {
    formattedDateRange = dateRange.value.map(date => {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    })
  }
  
  const searchParams = {
    location: selectedLocation.value.name,
    lat: selectedLocation.value.latitude,
    lng: selectedLocation.value.longitude,
    dateRange: JSON.stringify(formattedDateRange),
    guests: guests.value.adult + guests.value.child
  }
  
  // 跳转到搜索结果页
  router.push({
    path: '/search',
    query: searchParams
  })
}

// 禁用过去的日期
const disabledDate = (time) => {
  // 禁用今天之前的日期
  return time.getTime() < Date.now() - 8.64e7 // 8.64e7 = 24小时的毫秒数
}

// 处理位置选择
const handleLocationSelect = (locationData) => {
  selectedLocation.value = locationData
  location.value = locationData.name
}

// 当前选中的分类
const selectedCategory = ref('热门')

// 处理分类变化
const handleCategoryChange = (category) => {
  selectedCategory.value = category
}
</script>

<style scoped>
*{
  box-sizing: border-box;
}
.hero {
  height: 550px;
  background-image: url('../../assets/images/hero-bg.png');
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0;
  width: 100%;
  padding-top: 40px;
}
.hero-content {
  text-align: center;
  width: 100%;
  max-width: 1000px;
  margin-top: -100px;
}

.title {
  font-size: 48px;
  margin-bottom: 24px;
  font-weight: 600;
}
.title:hover{
  cursor: default;
}
.search-bar {
  display: flex;
  background: #fff;
  border-radius: 50px;
  padding: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  align-items: center;
}

.search-item {
  flex: 1;
  padding: 0 20px;
  text-align: left;
  position: relative;
}

.search-item.location {
  flex: 1.5;
}

.search-item input {
  width: 100%;
  border: none;
  outline: none;
  font-size: 14px;
}

.search-item :deep(.el-input__wrapper) {
  border: none;
  box-shadow: none;
  background: transparent;
}

.search-item :deep(.el-input__inner) {
  font-size: 14px;
}
.search-item input:hover{
  cursor: pointer;
}
.label {
  font-size: 12px;
  color: #999;
}

.search-btn {
  padding-right: 10px;
}

.guest-text {
  cursor: pointer;
  display: block;
  padding-top: 6px;
}

.guest-panel {
  padding: 10px 20px;
}

.guest-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.title {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.search-bar {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

</style>
