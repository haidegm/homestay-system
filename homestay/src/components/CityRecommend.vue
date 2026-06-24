<template>
  <div class="tuijwai">
    <div
      class="city-section"
      v-for="city in currentCities"
      :key="city.city"
    >
      <!-- 城市标题 -->
      <div class="city-header">
        <h2>{{ city.title || (city.city + ' · 精选民宿') }}</h2>
        <div class="actions"> 
          <button class="arrow" :disabled="scrollState[city.city]?.atStart" @click="scrollLeft(city.city)">‹</button>
          <button class="arrow" :disabled="scrollState[city.city]?.atEnd" @click="scrollRight(city.city)">›</button>
        </div>
      </div>
      <!-- 房源列表 -->
      <div
        class="house-list"
        :ref="el => setListRef(city.city, el)"
      >
        <div
          class="house-card"
          v-for="house in city.houses"
          :key="house.id"
          @click="goToHouseDetail(house.id)"
        >
          <div class="img-wrap">
            <img 
              :src="getImageUrl(house.image)" 
              :alt="house.title"
              @error="handleImageError"
              @load="handleImageLoad"
            />

            <!-- 收藏爱心 -->
            <button
              class="heart-btn"
              :class="{ active: isInWishlist(house.id) }"
              @click.stop="toggleFavorite(house.id)"
            >
              <span class="heart-icon">{{ isInWishlist(house.id) ? '❤️' : '🤍' }}</span>
            </button>
          </div>

          <div class="info">
            <div class="title">{{ house.title }}</div>
            <div class="bottom-row">
              <div class="price">￥{{ house.price }}/晚</div>
              <div v-if="house.rating && house.rating > 0" class="rating">
                <span class="star">★</span>
                <span class="rating-value">{{ house.rating.toFixed(1) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-if="!loading && currentCities.length === 0" class="empty">暂无房源数据</div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()

const props = defineProps({
  group: {
    type: String,
    required: true
  }
})

const loading = ref(true)
const cityGroups = ref({
  hot: [],
  city: []
})

// 获取房源数据
onMounted(async () => {
  try {
    loading.value = true
    
    // 加载心愿单状态（不阻塞）
    loadWishlistStatus()
    
    // 加载房源数据
    const res = await request.get('/api/house/recommend', {
      params: { group: props.group }
    })
    
    const data = res.data || res || []
    
    console.log('推荐房源数据:', data)
    
    // 按城市分组，过滤掉今日无房的房源
    const grouped = {}
    data.forEach(house => {
      console.log('处理房源:', house.id, '标题:', house.title, '图片:', house.coverImage, '今日可用:', house.availableToday)
      
      // 过滤掉今日无房的房源
      if (house.availableToday === false) {
        console.log('房源', house.id, '今日无房，跳过')
        return
      }
      
      const cityName = house.city || '未知城市'
      if (!grouped[cityName]) {
        grouped[cityName] = {
          city: cityName,
          title: cityName + ' · 精选民宿',
          houses: []
        }
      }
      
      // 确保图片字段正确映射
      const houseData = {
        id: house.id,
        title: house.title,
        price: house.price,
        image: house.coverImage, // 注意这里是 coverImage
        maxGuests: house.maxGuests,
        bedCount: house.bedCount,
        rating: house.rating || 0 // 添加评分字段
      }
      
      grouped[cityName].houses.push(houseData)
    })
    
    // 转换为数组并按房源数量排序（房源多的城市优先展示）
    const citiesArray = Object.values(grouped)
    citiesArray.sort((a, b) => b.houses.length - a.houses.length)
    
    cityGroups.value[props.group] = citiesArray
    
    console.log('分组后的数据（已按房源数量排序）:', cityGroups.value[props.group])
  } catch (error) {
    console.error('加载房源推荐失败:', error)
  } finally {
    loading.value = false
  }
})

const currentCities = computed(() => {
  const cities = cityGroups.value[props.group] || []
  // 限制最多显示6个城市
  return cities.slice(0, 6)
})

// 获取图片 URL
const getImageUrl = (imagePath) => {
  if (!imagePath) {
    return 'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=400&h=300&fit=crop'
  }
  
  // 如果是完整的 URL，直接返回
  if (imagePath.startsWith('http')) {
    return imagePath
  }
  
  // 如果是相对路径，拼接后端地址
  const fullUrl = `http://localhost:8080${imagePath}`
  return fullUrl
}


// 跳转到房源详情
const goToHouseDetail = (houseId) => {
  // 检查当前页面是否有搜索参数（比如从搜索页面返回的）
  const currentRoute = router.currentRoute.value
  const query = {}
  
  // 如果当前 URL 有搜索相关的查询参数，传递给详情页
  if (currentRoute.query.checkIn) {
    query.checkIn = currentRoute.query.checkIn
  }
  if (currentRoute.query.checkOut) {
    query.checkOut = currentRoute.query.checkOut
  }
  if (currentRoute.query.guests) {
    query.guests = currentRoute.query.guests
  }
  
  router.push({
    path: `/house/${houseId}`,
    query: Object.keys(query).length > 0 ? query : undefined
  })
}

/* 滑动控制 */
const listRefs = ref({})

const setListRef = (city, el) => {
  if (el) {
    listRefs.value[city] = el
    updateScrollState(city)
  }
}

// 每次滑动3个房源的距离：(185px卡片宽度 + 14px间距) × 3
const CARD_STEP = (185 + 14) * 3

const scrollLeft = (city) => {
  listRefs.value[city]?.scrollBy({
    left: -CARD_STEP,
    behavior: 'smooth'
  })
  setTimeout(() => updateScrollState(city), 300)
}

const scrollRight = (city) => {
  listRefs.value[city]?.scrollBy({
    left: CARD_STEP,
    behavior: 'smooth'
  })
  setTimeout(() => updateScrollState(city), 300)
}

/* 收藏 */
const favorites = ref(new Set())

// 加载用户的心愿单状态
const loadWishlistStatus = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  
  try {
    const res = await request.get('/api/wishlist/my')
    const wishlistHouseIds = res.map(item => item.houseId)
    favorites.value = new Set(wishlistHouseIds)
  } catch (error) {
    console.error('加载心愿单状态失败:', error)
  }
}

const isInWishlist = (houseId) => {
  return favorites.value.has(houseId)
}

const toggleFavorite = async (houseId) => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    if (favorites.value.has(houseId)) {
      await request.delete(`/api/wishlist/remove/${houseId}`)
      favorites.value.delete(houseId)
      ElMessage.success('已从心愿单移除')
    } else {
      await request.post(`/api/wishlist/add/${houseId}`)
      favorites.value.add(houseId)
      ElMessage.success('已添加到心愿单')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const scrollState = ref({})
const updateScrollState = (city) => {
  const el = listRefs.value[city]
  if (!el) return

  scrollState.value[city] = {
    atStart: el.scrollLeft <= 5,
    atEnd: el.scrollLeft + el.clientWidth >= el.scrollWidth - 5
  }
}
</script>

<style scoped>
.tuijwai{
  width: 95%;
  margin: 0 auto;
  margin-top: 30px;
}
.house-list {
  display: flex;
  gap: 14px;
  overflow-x: auto;
  scroll-behavior: smooth;
  padding-bottom: 6px;
}

/* 隐藏滚动条 */
.house-list::-webkit-scrollbar {
  display: none;
}

/* 关键：固定卡片宽度和高度 */
.house-card {
  flex: 0 0 185px;
  width: 185px;
  cursor: pointer;
  transition: transform 0.2s;
}

.house-card:hover {
  transform: translateY(-4px);
}

/* 图片区域：正方形，强制尺寸 */
.img-wrap {
  position: relative;
  width: 185px;
  height: 185px;
  border-radius: 18px;
  overflow: hidden;
  background: #f0f0f0;
}

.img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 爱心按钮 */
.heart-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.heart-btn:hover {
  transform: scale(1.1);
  background: white;
}

.heart-btn .heart-icon {
  font-size: 18px;
  line-height: 1;
}

.heart-btn.active {
  background: rgba(255, 56, 92, 0.1);
}

/* 信息区 */
.info {
  margin-top: 6px;
  font-size: 13px;
}

.title {
  color: #222;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.bottom-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  color: #717171;
  font-size: 12px;
}

.rating {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: #222;
}

.rating .star {
  color: #FF385C;
  font-size: 13px;
}

.rating-value {
  font-weight: 500;
}
.city-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.actions {
  display: flex;
  gap: 8px;
}

.arrow {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  font-size: 16px;
  line-height: 30px;
}

.arrow:hover {
  background: #f5f5f5;
}

.arrow:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
