<template>
  <div class="search-result-page">
    <div class="top-header">
      <div class="header-content">
        <div class="logo-section" @click="router.push('/')">
          <img src="../../assets/images/田园犬.png" class="logo-img">
          <span class="logo-text">W的homestay</span>
        </div>

        <!-- 中间搜索栏 - 使用StickySearch样式 -->
        <div class="header-search-wrapper">
          <div class="sticky-search">
            <LocationSearch
              v-model="editLocation"
              placeholder="目的地"
              size="small"
              @select="handleLocationSelect"
            />
            <el-date-picker
              v-model="editDateRange"
              size="small"
              type="daterange"
              start-placeholder="入住"
              end-placeholder="退房"
              :disabled-date="disabledDate"
              @change="handleReSearch"
            />
            <el-button type="primary" size="small" @click="handleReSearch">搜索</el-button>
          </div>
        </div>

        <div class="header-actions">
          <el-button text class="back-btn" @click="router.push('/')">
            <el-icon><HomeFilled /></el-icon>
            返回主页
          </el-button>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-menu">
              <el-icon><Menu /></el-icon>
              <el-avatar :size="32" :src="userAvatar" />
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <template v-if="!isLogin">
                  <el-dropdown-item command="login">登录</el-dropdown-item>
                  <el-dropdown-item command="register">注册</el-dropdown-item>
                </template>
                <template v-else>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="orders">我的行程</el-dropdown-item>
                  <el-dropdown-item command="wishlist">心愿单</el-dropdown-item>
                  <el-divider style="margin: 8px 0" />
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </template>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <div class="page-content">
      <div class="house-list-section">
        <div class="search-info">
          <h2>{{ searchLocation }} 附近的房源</h2>
          <p>{{ houseList.length }} 处住宿</p>
        </div>

        <div class="house-list">
          <div
            v-for="house in houseList"
            :key="house.id"
            class="house-card"
            :class="{ unavailable: !house.available }"
            @mouseenter="handleHouseHover(house)"
            @mouseleave="handleHouseLeave"
            @click="handleHouseClick(house.id)"
          >
            <div class="house-image">
              <img :src="getImageUrl(house.coverImage)" alt="">
              <div v-if="!house.available" class="unavailable-watermark">
                {{ searchParams.dateRange.length > 0 ? '选定日期不可用' : '当日无房源' }}
              </div>
              <div class="wishlist-btn">
                <el-icon><StarFilled /></el-icon>
              </div>
            </div>

            <div class="house-info">
              <div class="house-header">
                <h3 class="house-title">{{ house.title }}</h3>
                <div class="house-rating">
                  <el-icon><StarFilled /></el-icon>
                  <span>{{ formatRating(house.rating) }}</span>
                </div>
              </div>

              <p class="house-location">{{ house.city }} · {{ house.address }}</p>
              <p class="house-distance">距离搜索位置 {{ house.distance }} km</p>
              <p class="house-features">{{ house.maxGuests }}位房客 · {{ house.bedCount }}张床</p>

              <div class="house-footer">
                <div class="house-price">
                  <span class="price-amount">￥{{ house.price }}</span>
                  <span class="price-unit">/晚</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="loading" class="loading-container">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>

          <div v-if="!loading && houseList.length === 0" class="empty-result">
            <el-empty description="未找到符合条件的房源" />
          </div>
        </div>
      </div>

      <div class="map-section">
        <div id="amap-container" class="amap-container"></div>
      </div>
    </div>

    <LoginRegisterDialog
      v-model:visible="dialogVisible"
      v-model:dialogType="dialogType"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { StarFilled, Loading, HomeFilled, Menu, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import { useUserStore } from '../../store/user'
import LoginRegisterDialog from '../../components/LoginRegisterDialog.vue'
import LocationSearch from '../../components/LocationSearch.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const searchLocation = ref('')
const houseList = ref([])
const loading = ref(false)
const map = ref(null)
const markers = ref([])
const hoveredMarker = ref(null)
const dialogVisible = ref(false)
const dialogType = ref('login')

const searchParams = ref({
  location: '',
  latitude: null,
  longitude: null,
  dateRange: [],
  guests: 1
})

// 编辑状态的搜索参数
const editLocation = ref('')
const editDateRange = ref([])
const selectedLocationData = ref(null)

const isLogin = computed(() => userStore.isLogin)
const userAvatar = computed(() => {
  const avatar = userStore.avatarUrl
  if (!avatar || avatar.includes('default')) {
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
  return avatar
})

const handleCommand = (command) => {
  switch (command) {
    case 'login':
      dialogType.value = 'login'
      dialogVisible.value = true
      break
    case 'register':
      dialogType.value = 'register'
      dialogVisible.value = true
      break
    case 'profile':
      router.push('/user/profile')
      break
    case 'orders':
      router.push('/user/profile/trips')
      break
    case 'wishlist':
      router.push('/user/profile/wishlist')
      break
    case 'logout':
      userStore.logout()
      ElMessage.success('已退出登录')
      break
  }
}

onMounted(() => {
  searchParams.value = {
    location: route.query.location || '',
    latitude: parseFloat(route.query.lat) || 39.9042,
    longitude: parseFloat(route.query.lng) || 116.4074,
    dateRange: route.query.dateRange ? JSON.parse(route.query.dateRange) : [],
    guests: parseInt(route.query.guests) || 1
  }

  searchLocation.value = searchParams.value.location || '当前位置'
  
  // 初始化编辑状态
  editLocation.value = searchParams.value.location
  if (searchParams.value.dateRange && searchParams.value.dateRange.length === 2) {
    editDateRange.value = [
      new Date(searchParams.value.dateRange[0]),
      new Date(searchParams.value.dateRange[1])
    ]
  }
  
  // 初始化选中的位置数据
  selectedLocationData.value = {
    name: searchParams.value.location,
    latitude: searchParams.value.latitude,
    longitude: searchParams.value.longitude
  }
  
  initMap()
  searchHouses()
})

onUnmounted(() => {
  if (map.value) {
    map.value.destroy()
  }
})

const initMap = () => {
  if (!window.AMap) {
    console.error('AMap API is not loaded')
    return
  }

  map.value = new AMap.Map('amap-container', {
    zoom: 13,
    center: [searchParams.value.longitude, searchParams.value.latitude],
    viewMode: '2D'
  })

  new AMap.Marker({
    position: [searchParams.value.longitude, searchParams.value.latitude],
    icon: new AMap.Icon({
      size: new AMap.Size(30, 30),
      image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-red.png',
      imageSize: new AMap.Size(30, 30)
    }),
    title: '搜索位置',
    map: map.value
  })
}

const searchHouses = async () => {
  loading.value = true
  try {
    const params = {
      latitude: searchParams.value.latitude,
      longitude: searchParams.value.longitude,
      radius: 10,
      maxGuests: searchParams.value.guests
    }

    if (searchParams.value.dateRange && searchParams.value.dateRange.length === 2) {
      params.checkIn = searchParams.value.dateRange[0]
      params.checkOut = searchParams.value.dateRange[1]
    }

    const res = await request.get('/api/house/search', { params })

    houseList.value = (res.data || res || []).map(house => ({
      ...house,
      available: house.availableToday !== false
    }))

    await checkAvailability()
    addHouseMarkers()
  } catch (error) {
    console.error('Search houses failed', error)
  } finally {
    loading.value = false
  }
}

const formatLocalDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getAvailabilityRange = () => {
  if (searchParams.value.dateRange && searchParams.value.dateRange.length === 2) {
    // 确保日期格式为 YYYY-MM-DD
    const checkIn = searchParams.value.dateRange[0]
    const checkOut = searchParams.value.dateRange[1]
    
    return {
      checkIn: typeof checkIn === 'string' && checkIn.includes('T') 
        ? checkIn.split('T')[0] 
        : (typeof checkIn === 'string' ? checkIn : formatLocalDate(new Date(checkIn))),
      checkOut: typeof checkOut === 'string' && checkOut.includes('T')
        ? checkOut.split('T')[0]
        : (typeof checkOut === 'string' ? checkOut : formatLocalDate(new Date(checkOut)))
    }
  }

  const today = new Date()
  const tomorrow = new Date(today)
  tomorrow.setDate(today.getDate() + 1)
  return {
    checkIn: formatLocalDate(today),
    checkOut: formatLocalDate(tomorrow)
  }
}

const checkAvailability = async () => {
  const range = getAvailabilityRange()
  console.log('检查可用性，日期范围:', range)

  for (const house of houseList.value) {
    try {
      const res = await request.get(`/api/order/availability/${house.id}`, {
        params: {
          checkIn: range.checkIn,
          checkOut: range.checkOut
        }
      })
      console.log(`房源${house.id}可用性:`, res)
      // request.js的响应拦截器已经返回了response.data
      // 所以res就是 { available: true/false, message: "..." }
      house.available = res.available !== false
    } catch (error) {
      console.error(`Check availability failed for house ${house.id}`, error)
      house.available = true
    }
  }
}

const formatRating = (rating) => {
  if (rating === null || rating === undefined) {
    return '新'
  }

  const value = Number(rating)
  return Number.isFinite(value) && value > 0 ? value.toFixed(1) : '新'
}

const addHouseMarkers = () => {
  if (!map.value) return

  markers.value.forEach(marker => marker.setMap(null))
  markers.value = []

  houseList.value.forEach(house => {
    if (house.latitude && house.longitude) {
      const marker = new AMap.Marker({
        position: [house.longitude, house.latitude],
        title: house.title,
        extData: { houseId: house.id },
        map: map.value
      })

      marker.on('click', () => {
        handleHouseClick(house.id)
      })

      markers.value.push(marker)
    }
  })
}

const handleHouseHover = (house) => {
  if (!house.latitude || !house.longitude || !map.value) return

  const marker = markers.value.find(m => m.getExtData().houseId === house.id)
  if (marker) {
    // 高亮标记：变红色并放大，使用CSS transition实现平滑动画
    const redIcon = new AMap.Icon({
      size: new AMap.Size(40, 40),
      image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-red.png',
      imageSize: new AMap.Size(40, 40)
    })
    
    // 添加过渡效果
    marker.setIcon(redIcon)
    marker.setzIndex(999) // 提升层级，确保在最上层
    
    hoveredMarker.value = marker
  }
}

const handleHouseLeave = () => {
  if (hoveredMarker.value) {
    // 恢复标记：变回蓝色并恢复正常大小
    const blueIcon = new AMap.Icon({
      size: new AMap.Size(30, 30),
      image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png',
      imageSize: new AMap.Size(30, 30)
    })
    
    hoveredMarker.value.setIcon(blueIcon)
    hoveredMarker.value.setzIndex(100) // 恢复正常层级
    hoveredMarker.value = null
  }
}

const handleHouseClick = (houseId) => {
  const query = {}

  if (searchParams.value.dateRange && searchParams.value.dateRange.length === 2) {
    query.checkIn = searchParams.value.dateRange[0]
    query.checkOut = searchParams.value.dateRange[1]
  }

  if (searchParams.value.guests) {
    query.guests = searchParams.value.guests
  }

  router.push({
    path: `/house/${houseId}`,
    query
  })
}

const getImageUrl = (imagePath) => {
  if (!imagePath) {
    return 'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=400&h=300&fit=crop'
  }
  return imagePath.startsWith('http') ? imagePath : `http://localhost:8080${imagePath}`
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 处理位置选择
const handleLocationSelect = (locationData) => {
  selectedLocationData.value = locationData
  editLocation.value = locationData.name
}

// 重新搜索
const handleReSearch = () => {
  if (!selectedLocationData.value) {
    ElMessage.warning('请选择搜索位置')
    return
  }

  // 验证日期范围
  if (editDateRange.value && editDateRange.value.length === 2) {
    const checkIn = new Date(editDateRange.value[0])
    const checkOut = new Date(editDateRange.value[1])
    const diffTime = checkOut.getTime() - checkIn.getTime()
    const diffDays = diffTime / (1000 * 60 * 60 * 24)
    
    if (diffDays < 1) {
      ElMessage.warning('退房日期必须晚于入住日期至少一天')
      return
    }
  }

  // 格式化日期
  let formattedDateRange = []
  if (editDateRange.value && editDateRange.value.length === 2) {
    formattedDateRange = editDateRange.value.map(date => {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    })
  }

  const newSearchParams = {
    location: selectedLocationData.value.name,
    lat: selectedLocationData.value.latitude,
    lng: selectedLocationData.value.longitude,
    dateRange: JSON.stringify(formattedDateRange),
    guests: searchParams.value.guests || 1
  }

  // 更新URL
  router.push({
    path: '/search',
    query: newSearchParams
  })

  // 更新搜索参数
  searchParams.value = {
    location: selectedLocationData.value.name,
    latitude: selectedLocationData.value.latitude,
    longitude: selectedLocationData.value.longitude,
    dateRange: formattedDateRange,
    guests: searchParams.value.guests || 1
  }

  searchLocation.value = selectedLocationData.value.name
  
  // 重新搜索
  searchHouses()
  
  // 重新初始化地图中心
  if (map.value) {
    map.value.setCenter([selectedLocationData.value.longitude, selectedLocationData.value.latitude])
  }
}
</script>
<style scoped>
.search-result-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* 椤堕儴瀵艰埅鏍?*/
.top-header {
  background: white;
  border-bottom: 1px solid #ebebeb;
  padding: 0;
  height: 80px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  width: 100%;
  padding: 0 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.logo-section {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: opacity 0.2s;
  flex-shrink: 0;
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

/* 中间搜索栏包装器 */
.header-search-wrapper {
  flex: 1;
  display: flex;
  justify-content: center;
  max-width: 850px;
}

/* 使用StickySearch样式 */
.sticky-search {
  display: flex;
  gap: 10px;
  background: #fff;
  padding: 6px 12px;
  border-radius: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.sticky-search button {
  margin-top: 5px;
}

.sticky-search :deep(.el-input__wrapper),
.sticky-search :deep(.el-date-editor) {
  height: 32px;
  min-height: 32px;
}

.sticky-search :deep(.el-button--small) {
  height: 32px;
  padding: 8px 15px;
}

/* 让清空按钮使用绝对定位，不挤压输入框宽度 */
.sticky-search :deep(.el-input__suffix) {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
}

.sticky-search :deep(.el-input__suffix-inner) {
  display: flex;
  align-items: center;
}

/* 为输入框内容留出右侧空间 */
.sticky-search :deep(.el-input__inner) {
  padding-right: 30px;
}

/* 日期选择器的清空按钮 */
.sticky-search :deep(.el-date-editor .el-input__suffix) {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
}

.sticky-search :deep(.el-date-editor .el-input__inner) {
  padding-right: 30px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.back-btn {
  font-size: 14px;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 22px;
  transition: background 0.2s;
}

.back-btn:hover {
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

/* 椤甸潰鍐呭鍖哄煙 */
.page-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.house-list-section {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #fff;
}

.search-info {
  margin-bottom: 24px;
}

.search-info h2 {
  font-size: 26px;
  font-weight: 600;
  margin-bottom: 8px;
}

.search-info p {
  color: #717171;
  font-size: 14px;
}

.house-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.house-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.house-card.unavailable {
  opacity: 0.6;
}

.house-card.unavailable .house-image img {
  filter: grayscale(50%);
}

.house-card:hover {
  border-color: #e0e0e0;
  box-shadow: 0 6px 16px rgba(0,0,0,0.12);
}

.house-image {
  position: relative;
  width: 300px;
  height: 200px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
}

.house-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.unavailable-watermark {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.75);
  color: #fff;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
  pointer-events: none;
  z-index: 2;
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
  transition: transform 0.2s;
}

.wishlist-btn:hover {
  transform: scale(1.1);
}

.house-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.house-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.house-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #222;
}

.house-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #222;
}

.house-rating .el-icon {
  color: #FF385C;
}

.house-location,
.house-distance,
.house-features {
  font-size: 14px;
  color: #717171;
  margin: 4px 0;
}

.house-distance {
  color: #222;
  font-weight: 500;
}

.house-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 8px;
}

.house-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-amount {
  font-size: 18px;
  font-weight: 600;
  color: #222;
}

.price-unit {
  font-size: 14px;
  color: #717171;
}

.map-section {
  width: 50%;
  position: sticky;
  top: 80px;
  height: calc(100vh - 80px);
}

.amap-container {
  width: 100%;
  height: 100%;
}

/* 为地图标记添加平滑过渡动画 */
:deep(.amap-marker) {
  transition: all 0.3s ease;
}

:deep(.amap-marker img) {
  transition: all 0.3s ease;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 12px;
  color: #717171;
}

.loading-container .el-icon {
  font-size: 32px;
}

.empty-result {
  padding: 60px 20px;
}

@media (max-width: 1024px) {
  .map-section {
    display: none;
  }
  
  .house-list-section {
    width: 100%;
  }
}
</style>