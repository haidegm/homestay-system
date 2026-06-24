<template>
  <div class="house-detail-page">
    <!-- 顶部导航栏 -->
    <div class="top-header">
      <div class="header-content">
        <div class="logo-section" @click="router.push('/')">
          <img src="../../assets/images/田园犬.png" class="logo-img">
          <span class="logo-text">W的homestay</span>
        </div>

        <div class="header-actions">
          <el-button text class="back-btn" @click="router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
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
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-else-if="house" class="house-detail">
      <!-- 房源标题 -->
      <div class="house-header">
        <div class="header-content">
          <h1 class="house-title">{{ house.title }}</h1>
          <div class="house-meta">
            <span class="rating">
              <el-icon><StarFilled /></el-icon>
              {{ averageRating }}
            </span>
            <span class="separator">·</span>
            <span class="reviews" @click="reviewsDialogVisible = true">{{ reviews.length }}条评价</span>
            <span class="separator">·</span>
            <span class="location">{{ house.city }}, {{ house.province }}</span>
          </div>
        </div>
        <el-button 
          circle
          size="large"
          @click="toggleWishlist"
          :loading="wishlistLoading"
          class="wishlist-btn"
          :class="{ 'is-favorited': isInWishlist }"
        >
          <span class="heart-icon">{{ isInWishlist ? '❤️' : '🤍' }}</span>
        </el-button>
      </div>

      <!-- 图片展示 -->
      <div class="images-grid" v-if="house.images && house.images.length">
        <div class="main-image">
          <el-image
            :src="getImageUrl(house.images[0])"
            :preview-src-list="house.images.map(img => getImageUrl(img))"
            :initial-index="0"
            fit="cover"
            preview-teleported
            style="width: 100%; height: 100%; cursor: pointer;"
          />
        </div>
        <div class="side-images" v-if="house.images.length > 1">
          <el-image
            v-for="(image, index) in house.images.slice(1, 5)" 
            :key="index"
            :src="getImageUrl(image)"
            :preview-src-list="house.images.map(img => getImageUrl(img))"
            :initial-index="index + 1"
            fit="cover"
            preview-teleported
            style="width: 100%; height: 100%; cursor: pointer;"
          />
        </div>
      </div>

      <!-- 主要内容区 -->
      <div class="content-wrapper">
        <!-- 左侧内容 -->
        <div class="left-content">
          <!-- 房东信息 -->
          <div class="host-section">
            <div class="host-info">
              <div class="host-left">
                <el-avatar 
                  :size="56" 
                  :src="getHostAvatar(house.hostAvatar)" 
                  @error="handleHostAvatarError"
                />
                <div>
                  <h2>房东：{{ house.hostName || '房东' }}</h2>
                  <p>{{ house.maxGuests }}位房客 · {{ house.bedCount }}间卧室 · {{ house.bedCount }}张床</p>
                </div>
              </div>
              <el-button @click="handleContactHost" class="contact-btn">
                <el-icon><ChatDotRound /></el-icon>
                联系房东
              </el-button>
            </div>
          </div>

          <el-divider />

          <!-- 房源简介 -->
          <div class="section" v-if="house.description">
            <h3>房源介绍</h3>
            <p class="description">{{ house.description }}</p>
          </div>

          <el-divider v-if="house.description" />

          <!-- 房源详情 -->
          <div class="section">
            <h3>房源详情</h3>
            <div class="details-list">
              <div class="detail-row">
                <span>房间面积</span>
                <span>{{ house.area }} 平方米</span>
              </div>
              <div class="detail-row">
                <span>停车</span>
                <span>{{ getParkingText(house.parking) }}</span>
              </div>
            </div>
          </div>

          <el-divider />

          <!-- 浴室配套 -->
          <div class="section" v-if="house.bathroomDesc">
            <h3>浴室配套</h3>
            <p>{{ house.bathroomDesc }}</p>
          </div>

          <!-- 设施用品 -->
          <div class="section" v-if="house.supplyDesc">
            <h3>设施用品</h3>
            <p>{{ house.supplyDesc }}</p>
          </div>

          <el-divider />

          <!-- 评价区域 -->
          <div class="section reviews-section">
            <div class="reviews-header" @click="reviewsDialogVisible = true">
              <h3>
                <el-icon><StarFilled /></el-icon>
                {{ averageRating }} · {{ reviews.length }}条评价
              </h3>
            </div>

            <!-- 评价列表 -->
            <div class="reviews-list" v-if="reviews.length > 0">
              <div 
                class="review-item" 
                v-for="review in displayedReviews" 
                :key="review.id"
              >
                <div class="review-header">
                  <img :src="review.avatar" class="reviewer-avatar" @error="handleAvatarError"/>
                  <div class="reviewer-info">
                    <div class="reviewer-name">{{ review.userName }}</div>
                    <div class="review-date">{{ formatReviewDate(review.createdTime) }}</div>
                  </div>
                </div>
                <div class="review-rating">
                  <el-rate 
                    v-model="review.rating" 
                    disabled 
                    :allow-half="true"
                    size="small"
                  />
                </div>
                <div class="review-content">{{ review.comment }}</div>
              </div>
            </div>

            <el-empty v-else description="暂无评价" :image-size="100" />

            <!-- 显示更多按钮 -->
            <el-button 
              v-if="reviews.length > 6"
              class="show-more-btn"
              @click="reviewsDialogVisible = true"
            >
              显示全部{{ reviews.length }}条评价
            </el-button>
          </div>

          <el-divider />

          <!-- 位置区域 -->
          <div class="section location-section">
            <h3>位置</h3>
            <p class="location-text">{{ house.address }}</p>
            
            <!-- 高德地图 -->
            <div id="house-map" class="map-container"></div>
            
            <p class="location-desc" v-if="house.locationDesc">
              {{ house.locationDesc }}
            </p>
          </div>

          <!-- 相似房源推荐 -->
          <div class="section similar-houses-section">
            <h3>相似房源推荐</h3>
            <RecommendedHouses 
              v-if="house.id"
              title=""
              subtitle=""
              :api-url="`/api/recommend/similar/${house.id}?limit=4`"
              :show-score="false"
            />
          </div>
        </div>

        <!-- 右侧预订卡片 -->
        <div class="right-sidebar">
          <div class="booking-card">
            <!-- 价格和评分 -->
            <div class="card-header">
              <div class="price-info">
                <span class="price">¥{{ house.price }}</span>
                <span class="unit">/ 晚</span>
              </div>
              <div class="rating-info">
                <el-icon><StarFilled /></el-icon>
                <span>{{ house.rating || '新' }}</span>
                <span class="review-count">({{ house.reviewCount || 0 }})</span>
              </div>
            </div>

            <!-- 预订表单 -->
            <div class="booking-form">
              <!-- 日期选择 -->
              <div class="form-group date-group">
                <div class="date-row">
                  <div class="date-field">
                    <label>入住</label>
                    <el-date-picker
                      v-model="bookingForm.checkIn"
                      type="date"
                      placeholder="添加日期"
                      :disabled-date="disabledDate"
                      format="YYYY/MM/DD"
                      value-format="YYYY-MM-DD"
                      size="large"
                    />
                  </div>
                  <div class="date-field">
                    <label>退房</label>
                    <el-date-picker
                      v-model="bookingForm.checkOut"
                      type="date"
                      placeholder="添加日期"
                      :disabled-date="disabledDate"
                      format="YYYY/MM/DD"
                      value-format="YYYY-MM-DD"
                      size="large"
                    />
                  </div>
                </div>
              </div>

              <!-- 人数选择 -->
              <div class="form-group guest-group">
                <label>房客</label>
                <el-input-number
                  v-model="bookingForm.guests"
                  :min="1"
                  :max="house.maxGuests"
                  size="large"
                />
              </div>

              <!-- 预订按钮 -->
              <el-button 
                type="primary" 
                size="large" 
                class="reserve-btn"
                @click="handleBooking"
                :disabled="!canBook"
              >
                {{ bookingButtonText }}
              </el-button>

              <p class="notice">预订后将进入支付</p>

              <!-- 费用明细 -->
              <div v-if="bookingForm.checkIn && bookingForm.checkOut" class="price-details">
                <div class="price-line">
                  <span>¥{{ house.price }} × {{ nightCount }}晚</span>
                  <span>¥{{ subtotal }}</span>
                </div>
                <el-divider />
                <div class="price-line total">
                  <span>总计</span>
                  <span>¥{{ totalCost }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="error-container">
      <el-empty description="房源不存在" />
      <el-button @click="$router.back()">返回</el-button>
    </div>
    </div>

    <!-- 确认预订对话框 -->
    <BookingConfirmDialog
      v-model="payDialogVisible"
      :booking-data="bookingDialogData"
      :loading="paying"
      :show-later-option="true"
      confirm-text="立即支付"
      @confirm="handleConfirmBooking"
      @close="handleClosePayDialog"
    />

    <!-- 消息对话框 -->
    <MessageDialog
      v-model="messageDialogVisible"
      :host-id="house?.hostId"
      :host-name="house?.hostName"
      :house-id="house?.id"
    />
    
    <!-- 登录注册对话框 -->
    <LoginRegisterDialog 
      v-model:visible="loginDialogVisible" 
      v-model:dialogType="dialogType" 
    />

    <!-- 评价弹窗 -->
    <ReviewsDialog
      v-model:visible="reviewsDialogVisible"
      :reviews="reviews"
      :average-rating="averageRating"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Loading, StarFilled, ChatDotRound, ArrowLeft, Menu } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import BookingConfirmDialog from '../../components/BookingConfirmDialog.vue'
import MessageDialog from '../../components/MessageDialog.vue'
import LoginRegisterDialog from '../../components/LoginRegisterDialog.vue'
import ReviewsDialog from '../../components/ReviewsDialog.vue'
import RecommendedHouses from '../../components/RecommendedHouses.vue'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const house = ref(null)
const reviews = ref([])
const showAllReviews = ref(false)
const isInWishlist = ref(false)
const wishlistLoading = ref(false)
const loginDialogVisible = ref(false)
const dialogType = ref('login')
const reviewsDialogVisible = ref(false)
let map = null
let marker = null

// 用户状态
const isLogin = computed(() => userStore.isLogin)
const userAvatar = computed(() => {
  const avatar = userStore.avatarUrl
  if (!avatar || avatar.includes('default')) {
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
  return avatar
})

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'login':
      dialogType.value = 'login'
      loginDialogVisible.value = true
      break
    case 'register':
      dialogType.value = 'register'
      loginDialogVisible.value = true
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

const bookingForm = reactive({
  checkIn: null,
  checkOut: null,
  guests: 1
})

const dateAvailable = ref(true)
const dateCheckMessage = ref('')

const nightCount = computed(() => {
  if (!bookingForm.checkIn || !bookingForm.checkOut) return 0
  const checkIn = new Date(bookingForm.checkIn)
  const checkOut = new Date(bookingForm.checkOut)
  const diffTime = checkOut - checkIn
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
})

const subtotal = computed(() => {
  return house.value ? house.value.price * nightCount.value : 0
})

const totalCost = computed(() => {
  return subtotal.value
})

const canBook = computed(() => {
  return bookingForm.checkIn && bookingForm.checkOut && bookingForm.guests <= (house.value?.maxGuests || 0) && dateAvailable.value
})

const bookingButtonText = computed(() => {
  if (!bookingForm.checkIn || !bookingForm.checkOut) {
    return '选择日期'
  }
  if (!dateAvailable.value) {
    return '日期不可用'
  }
  return '预订'
})

// 监听日期变化，检查可用性
watch([() => bookingForm.checkIn, () => bookingForm.checkOut], async ([newCheckIn, newCheckOut]) => {
  if (newCheckIn && newCheckOut && route.params.id) {
    try {
      const checkInDate = typeof newCheckIn === 'string' 
        ? newCheckIn 
        : new Date(newCheckIn).toISOString().split('T')[0]
      
      const checkOutDate = typeof newCheckOut === 'string'
        ? newCheckOut
        : new Date(newCheckOut).toISOString().split('T')[0]

      const availabilityRes = await request.get(`/api/order/availability/${route.params.id}`, {
        params: {
          checkIn: checkInDate,
          checkOut: checkOutDate
        }
      })

      dateAvailable.value = availabilityRes.available
      dateCheckMessage.value = availabilityRes.message || ''
      
      if (!availabilityRes.available) {
        ElMessage.warning(availabilityRes.message || '选择的日期不可用')
      }
    } catch (error) {
      console.error('检查日期可用性失败:', error)
      dateAvailable.value = true // 出错时默认可用
    }
  } else {
    dateAvailable.value = true
    dateCheckMessage.value = ''
  }
})

const displayedReviews = computed(() => {
  return showAllReviews.value ? reviews.value : reviews.value.slice(0, 6)
})

const averageRating = computed(() => {
  if (reviews.value.length === 0) return '新'
  const sum = reviews.value.reduce((acc, review) => acc + review.rating, 0)
  return (sum / reviews.value.length).toFixed(1)
})

onMounted(() => {
  // 滚动到页面顶部
  window.scrollTo(0, 0)
  
  initBookingFormFromQuery()
  loadHouseDetail()
  loadReviews()
  loadAMapScript()
  checkWishlistStatus()
  
  // 记录浏览行为
  recordBrowseHistory()
})

// 记录浏览行为
const recordBrowseHistory = async () => {
  const token = localStorage.getItem('token')
  if (!token) return // 未登录不记录
  
  try {
    await request.post(`/api/browse/record/${route.params.id}`)
  } catch (error) {
    // 静默失败，不影响用户体验
    console.log('记录浏览历史失败:', error)
  }
}

// 监听路由参数变化，重新加载数据
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    // 滚动到页面顶部
    window.scrollTo(0, 0)
    
    // 重置状态
    loading.value = true
    house.value = null
    reviews.value = []
    showAllReviews.value = false
    isInWishlist.value = false
    
    // 重新加载数据
    initBookingFormFromQuery()
    loadHouseDetail()
    loadReviews()
    checkWishlistStatus()
  }
})

const loadHouseDetail = async () => {
  try {
    const res = await request.get(`/api/host/house/detail/${route.params.id}`)
    house.value = res.data || res
    console.log('房源详情:', house.value)
    
    // 加载地图
    if (house.value && house.value.longitude && house.value.latitude) {
      initMap()
    }
  } catch (error) {
    console.error('加载房源详情失败', error)
  } finally {
    loading.value = false
  }
}

const loadReviews = async () => {
  try {
    const res = await request.get(`/api/review/house/${route.params.id}`)
    reviews.value = res.map(item => ({
      id: item.id,
      userName: item.userName || '匿名用户',
      avatar: item.userAvatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${item.userId}`,
      rating: item.rating,
      comment: item.comment,
      createdTime: item.createdTime,
      images: item.images || '', // 评价图片
      reply: item.reply || '' // 房东回复
    }))
    console.log('评价列表:', reviews.value)
  } catch (error) {
    console.error('加载评价失败:', error)
  }
}

const formatReviewDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  return `${year}年${month}月`
}

const handleAvatarError = (e) => {
  e.target.src = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
}

const getHostAvatar = (avatar) => {
  if (!avatar) {
    return 'https://api.dicebear.com/7.x/avataaars/svg?seed=host'
  }
  return avatar.startsWith('http') ? avatar : `http://localhost:8080${avatar}`
}

const handleHostAvatarError = (e) => {
  e.target.src = 'https://api.dicebear.com/7.x/avataaars/svg?seed=host'
}

// 加载高德地图脚本
const loadAMapScript = () => {
  if (window.AMap) {
    return Promise.resolve()
  }
  
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = 'https://webapi.amap.com/maps?v=2.0&key=d6c1c1895f0e4b0f9e0e8f3e5f5e5f5e'
    script.onload = resolve
    script.onerror = reject
    document.head.appendChild(script)
  })
}

// 初始化地图
const initMap = () => {
  if (!window.AMap || !house.value) return
  
  setTimeout(() => {
    const mapContainer = document.getElementById('house-map')
    if (!mapContainer) return
    
    map = new window.AMap.Map('house-map', {
      zoom: 15,
      center: [house.value.longitude, house.value.latitude],
      viewMode: '2D'
    })
    
    marker = new window.AMap.Marker({
      position: [house.value.longitude, house.value.latitude],
      title: house.value.title
    })
    
    map.add(marker)
  }, 500)
}

const initBookingFormFromQuery = () => {
  const query = route.query
  console.log('URL参数:', query)
  
  if (query.checkIn) {
    bookingForm.checkIn = query.checkIn
  }
  if (query.checkOut) {
    bookingForm.checkOut = query.checkOut
  }
  if (query.guests) {
    bookingForm.guests = parseInt(query.guests) || 1
  }
}

const getImageUrl = (imagePath) => {
  if (!imagePath) {
    return 'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=800'
  }
  return imagePath.startsWith('http') ? imagePath : `http://localhost:8080${imagePath}`
}

const getParkingText = (parking) => {
  const map = {
    'NONE': '无停车位',
    'FREE': '免费停车',
    'PAID': '付费停车'
  }
  return map[parking] || '未知'
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const payDialogVisible = ref(false)
const pendingOrder = ref({})
const paying = ref(false)
const messageDialogVisible = ref(false)

// 预订对话框数据
const bookingDialogData = computed(() => ({
  checkIn: bookingForm.checkIn,
  checkOut: bookingForm.checkOut,
  guests: bookingForm.guests,
  pricePerNight: house.value?.price || 0,
  nights: nightCount.value,
  subtotal: subtotal.value,
  cleaningFee: 0,
  serviceFee: 0,
  totalCost: totalCost.value
}))

const handleBooking = async () => {
  if (!canBook.value) {
    ElMessage.warning('请完善预订信息')
    return
  }
  
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录')
      return
    }

    const checkInDate = typeof bookingForm.checkIn === 'string' 
      ? bookingForm.checkIn 
      : new Date(bookingForm.checkIn).toISOString().split('T')[0]
    
    const checkOutDate = typeof bookingForm.checkOut === 'string'
      ? bookingForm.checkOut
      : new Date(bookingForm.checkOut).toISOString().split('T')[0]

    const availabilityRes = await request.get(`/api/order/availability/${route.params.id}`, {
      params: {
        checkIn: checkInDate,
        checkOut: checkOutDate
      }
    })

    if (!availabilityRes.available) {
      ElMessage.error(availabilityRes.message || '选择的日期不可用')
      return
    }

    const orderData = {
      houseId: parseInt(route.params.id),
      checkInDate: checkInDate,
      checkOutDate: checkOutDate,
      guestCount: bookingForm.guests,
      totalPrice: totalCost.value
    }

    const orderRes = await request.post('/api/order/create', orderData)
    console.log('订单创建响应:', orderRes)
    
    if (orderRes.success) {
      pendingOrder.value = orderRes
      console.log('订单创建成功，订单ID:', orderRes.orderId)
      // 显示支付对话框
      payDialogVisible.value = true
    } else {
      ElMessage.error(orderRes.message || '预订失败')
    }

  } catch (error) {
    console.error('预订失败:', error)
    ElMessage.error('预订失败，请稍后重试')
  }
}

// 从确认预订对话框点击确认后的处理
const handleConfirmBooking = async () => {
  paying.value = true

  try {
    // 调用支付接口，将订单状态改为 CONFIRMED
    const payRes = await request.post(`/api/order/pay/${pendingOrder.value.orderId}`)

    if (payRes.success) {
      ElMessage.success('预订成功！')
      payDialogVisible.value = false
      
      // 跳转到我的行程
      setTimeout(() => {
        router.push('/user/profile/trips')
      }, 1000)
    } else {
      ElMessage.error(payRes.message || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败，请稍后重试')
  } finally {
    paying.value = false
  }
}

// 关闭支付对话框时，跳转到行程页（订单保持 PENDING 状态）
const handleClosePayDialog = () => {
  payDialogVisible.value = false
  ElMessage.info('订单已创建，您可以稍后在"我的行程"中完成支付')
  
  setTimeout(() => {
    router.push('/user/profile/trips')
  }, 1000)
}

// 检查心愿单状态
const checkWishlistStatus = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  
  try {
    const res = await request.get(`/api/wishlist/check/${route.params.id}`)
    isInWishlist.value = res.isInWishlist
  } catch (error) {
    console.error('检查心愿单状态失败:', error)
  }
}

// 切换心愿单状态
const toggleWishlist = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }
  
  wishlistLoading.value = true
  try {
    if (isInWishlist.value) {
      await request.delete(`/api/wishlist/remove/${route.params.id}`)
      isInWishlist.value = false
      ElMessage.success('已从心愿单移除')
    } else {
      await request.post(`/api/wishlist/add/${route.params.id}`)
      isInWishlist.value = true
      ElMessage.success('已添加到心愿单')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    wishlistLoading.value = false
  }
}

// 联系房东
const handleContactHost = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }
  messageDialogVisible.value = true
}
</script>

<style scoped>
.house-detail-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #fff;
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

/* 页面内容区域 */
.page-content {
  max-width: 1120px;
  margin: 0 auto;
  padding: 24px 40px;
  width: 100%;
}

.house-detail {
  margin: 0 auto;
  padding: 24px 40px;
}

.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  gap: 16px;
}

/* 标题区 */
.house-header {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.header-content {
  flex: 1;
}

.house-title {
  font-size: 26px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #222;
}

.wishlist-btn {
  flex-shrink: 0;
  border: 1px solid #DDDDDD;
  background: white;
  transition: all 0.2s;
}

.wishlist-btn .heart-icon {
  font-size: 20px;
  line-height: 1;
}

.wishlist-btn:hover {
  transform: scale(1.1);
  border-color: #222;
}

.wishlist-btn.is-favorited {
  border-color: #FF385C;
}

.house-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #222;
}

.rating {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
}

.rating .el-icon {
  color: #FF385C;
  font-size: 12px;
}

.separator {
  color: #717171;
}

.reviews {
  text-decoration: underline;
  cursor: pointer;
  font-weight: 600;
}

.location {
  font-weight: 600;
}

/* 图片网格 */
.images-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  height: 480px;
  margin-bottom: 48px;
  border-radius: 12px;
  overflow: hidden;
}

.main-image {
  grid-row: span 2;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.side-images {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 8px;
  height: 100%;
}

.side-images img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  aspect-ratio: 1 / 1;
}

/* 内容区 */
.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 80px;
}

.left-content {
  min-width: 0;
}

/* 房东信息 */
.host-section {
  padding: 32px 0;
}

.host-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.host-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.host-info h2 {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.host-info p {
  font-size: 14px;
  color: #717171;
  margin: 0;
}

.contact-btn {
  border: 1px solid #222;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  background: white;
  color: #222;
  display: flex;
  align-items: center;
  gap: 8px;
}

.contact-btn:hover {
  background: #F7F7F7;
}

/* 各个section */
.section {
  padding: 32px 0;
}

.section h3 {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 16px 0;
}

.section p {
  font-size: 16px;
  line-height: 24px;
  color: #222;
  margin: 0;
}

.description {
  white-space: pre-wrap;
}

.details-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  font-size: 16px;
}

.detail-row span:first-child {
  color: #717171;
}

.detail-row span:last-child {
  color: #222;
  font-weight: 500;
}

/* 右侧预订卡片 */
.right-sidebar {
  position: relative;
}

.booking-card {
  position: sticky;
  top: 80px;
  border: 1px solid #DDDDDD;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  background: white;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price {
  font-size: 22px;
  font-weight: 600;
  color: #222;
}

.unit {
  font-size: 16px;
  color: #222;
}

.rating-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.rating-info .el-icon {
  color: #FF385C;
  font-size: 12px;
}

.review-count {
  color: #717171;
  text-decoration: underline;
}

/* 预订表单 */
.booking-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  border: 1px solid #B0B0B0;
  border-radius: 8px;
  overflow: hidden;
}

.date-group {
  padding: 0;
}

.date-row {
  display: flex;
  flex-direction: column;
}

.date-field {
  padding: 12px;
  border-bottom: 1px solid #B0B0B0;
}

.date-field:last-child {
  border-bottom: none;
}

.date-field label {
  display: block;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 4px;
  color: #222;
}

.date-field :deep(.el-date-editor) {
  width: 100%;
}

.date-field :deep(.el-input__wrapper) {
  box-shadow: none;
  padding: 0;
  border: none;
}

.date-field :deep(.el-input__inner) {
  padding: 0;
  font-size: 14px;
}

.guest-group {
  padding: 12px;
}

.guest-group label {
  display: block;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 4px;
  color: #222;
}

.guest-group :deep(.el-input-number) {
  width: 100%;
}

.guest-group :deep(.el-input__wrapper) {
  box-shadow: none;
  padding: 0;
  border: none;
}

.reserve-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(to right, #E61E4D, #E31C5F, #D70466);
  border: none;
}

.reserve-btn:hover {
  background: linear-gradient(to right, #D01346, #CA1A5B, #C10366);
}

.notice {
  text-align: center;
  font-size: 14px;
  color: #222;
  margin: 0;
}

.price-details {
  margin-top: 8px;
}

.price-line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  font-size: 16px;
  color: #222;
}

.price-line.total {
  font-weight: 600;
  padding-top: 16px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .content-wrapper {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  
  .booking-card {
    position: static;
  }
}

@media (max-width: 768px) {
  .house-detail-page {
    padding: 16px 24px;
  }

  .images-grid {
    grid-template-columns: 1fr;
    height: 300px;
  }
  
  .side-images {
    display: none;
  }
}

/* 评价区域 */
.reviews-section {
  padding: 48px 0;
}

.reviews-header {
  cursor: pointer;
  transition: opacity 0.2s;
}

.reviews-header:hover {
  opacity: 0.7;
}

.reviews-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 32px 0;
}

.reviews-header .el-icon {
  color: #FF385C;
  font-size: 20px;
}

.reviews-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 40px 80px;
  margin-bottom: 32px;
}

.review-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reviewer-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.reviewer-info {
  flex: 1;
}

.reviewer-name {
  font-size: 16px;
  font-weight: 600;
  color: #222;
  margin-bottom: 2px;
}

.review-date {
  font-size: 14px;
  color: #717171;
}

.review-rating {
  margin: -4px 0;
}

.review-content {
  font-size: 14px;
  line-height: 20px;
  color: #222;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.show-more-btn {
  border: 1px solid #222;
  border-radius: 8px;
  padding: 13px 23px;
  font-size: 16px;
  font-weight: 600;
  background: white;
  color: #222;
}

.show-more-btn:hover {
  background: #F7F7F7;
}

/* 位置区域 */
.location-section {
  padding: 48px 0;
}

.location-text {
  font-size: 16px;
  color: #222;
  margin-bottom: 24px;
}

.map-container {
  width: 100%;
  height: 480px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
}

.location-desc {
  font-size: 16px;
  line-height: 24px;
  color: #222;
}

/* 相似房源推荐区域 */
.similar-houses-section {
  padding: 48px 0;
}

.similar-houses-section h3 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 24px 0;
  color: #222;
}

/* 覆盖RecommendedHouses组件的样式 */
.similar-houses-section :deep(.recommended-section) {
  max-width: 100%;
  padding: 0;
  margin: 0;
}

.similar-houses-section :deep(.section-header) {
  display: none;
}

.similar-houses-section :deep(.houses-grid) {
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.similar-houses-section :deep(.house-card) {
  width: 100% !important;
  max-width: 100% !important;
  min-width: 0 !important;
}

.similar-houses-section :deep(.house-image) {
  width: 100% !important;
  height: 200px !important;
  overflow: hidden !important;
  position: relative !important;
  aspect-ratio: auto !important;
}

.similar-houses-section :deep(.house-image img) {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
  position: static !important;
}

@media (max-width: 768px) {
  .reviews-list {
    grid-template-columns: 1fr;
    gap: 32px;
  }
  
  .map-container {
    height: 300px;
  }
}
</style>
