<template>
  <div class="trips-section">
    <h2 class="section-title">行程</h2>
    
    <div v-if="trips.length === 0" class="empty-trips">
      <el-icon :size="60" color="#EBEBEB"><Suitcase /></el-icon>
      <h3>还没有预订行程</h3>
      <p>是时候开启下一段旅程了！</p>
      <el-button @click="router.push('/')" class="btn-search">开始搜索</el-button>
    </div>

    <div v-else class="trips-list">
      <div v-for="trip in trips" :key="trip.id" class="trip-card">
        <img :src="getCover(trip)" class="trip-img" @click="goToHouseDetail(trip.houseId)">
        <div class="trip-info">
          <span class="trip-status" :class="getStatusClass(trip.status)">
            {{ formatStatus(trip.status) }}
          </span>
          <h4 @click="goToHouseDetail(trip.houseId)" class="trip-title">
            {{ trip.houseTitle || '未命名房源' }}
          </h4>
          <p class="order-no">订单号：{{ trip.orderNo || 'N/A' }}</p>
          <p class="trip-dates">
            <el-icon><Calendar /></el-icon>
            {{ formatDate(trip.checkInDate) }} - {{ formatDate(trip.checkOutDate) }}
          </p>
          <p class="trip-guests">
            <el-icon><User /></el-icon>
            {{ trip.guestCount }} 位房客
          </p>
          <div class="trip-price">总计：¥{{ trip.totalPrice }}</div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="trip-actions">
          <!-- 待支付状态 -->
          <template v-if="trip.status === 'PENDING'">
            <el-button type="primary" size="small" @click="handlePay(trip)">
              立即支付
            </el-button>
            <el-button size="small" @click="handleDelete(trip)">
              删除订单
            </el-button>
          </template>
          
          <!-- 已确认状态 -->
          <template v-else-if="trip.status === 'CONFIRMED'">
            <el-button size="small" @click="handleContactHost(trip)">
              <el-icon><ChatDotRound /></el-icon>
              联系商家
            </el-button>
            <el-button size="small" @click="handleCancel(trip)">
              取消预订
            </el-button>
          </template>
          
          <!-- 已完成状态 -->
          <template v-else-if="trip.status === 'COMPLETED'">
            <el-button size="small" @click="handleContactHost(trip)">
              <el-icon><ChatDotRound /></el-icon>
              联系商家
            </el-button>
            <el-button type="primary" size="small" @click="handleReview(trip)" v-if="!trip.hasReview">
              写评价
            </el-button>
            <template v-else>
              <el-button size="small" type="success" @click="goToMyReviews">
                查看评价
              </el-button>
            </template>
          </template>
          
          <!-- 已取消状态 -->
          <template v-else-if="trip.status === 'CANCELLED'">
            <el-button size="small" @click="handleDelete(trip)">
              删除订单
            </el-button>
          </template>
        </div>
      </div>
    </div>

    <!-- 支付对话框 -->
    <BookingConfirmDialog
      v-model="payDialogVisible"
      :booking-data="currentBookingData"
      :loading="paying"
      @confirm="confirmPay"
    />

    <!-- 评价对话框 -->
    <ReviewDialog
      v-model="reviewDialogVisible"
      :order-info="currentReviewOrder"
      @success="handleReviewSuccess"
    />

    <!-- 消息对话框 -->
    <MessageDialog
      v-model="messageDialogVisible"
      :host-id="currentHostId"
      :host-name="currentHostName"
      :house-id="currentHouseId"
      :order-id="currentOrderId"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Suitcase, Calendar, User, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../../utils/request'
import BookingConfirmDialog from '../../../components/BookingConfirmDialog.vue'
import ReviewDialog from '../../../components/ReviewDialog.vue'
import MessageDialog from '../../../components/MessageDialog.vue'

const router = useRouter()
const trips = ref([])
const payDialogVisible = ref(false)
const paying = ref(false)
const currentTrip = ref(null)
const reviewDialogVisible = ref(false)
const currentReviewOrder = ref(null)
const messageDialogVisible = ref(false)
const currentHostId = ref(null)
const currentHostName = ref(null)
const currentHouseId = ref(null)
const currentOrderId = ref(null)

const formatStatus = (status) => {
  const map = {
    PENDING: '待支付',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const getStatusClass = (status) => {
  return {
    'status-pending': status === 'PENDING',
    'status-confirmed': status === 'CONFIRMED',
    'status-completed': status === 'COMPLETED',
    'status-cancelled': status === 'CANCELLED'
  }
}

const formatDate = (value) => {
  if (!value) return ''
  return String(value).slice(0, 10)
}

const getCover = (trip) => {
  if (!trip.coverImage) return 'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=400'
  return trip.coverImage.startsWith('http')
    ? trip.coverImage
    : `http://localhost:8080${trip.coverImage}`
}

const goToHouseDetail = (houseId) => {
  router.push(`/house/${houseId}`)
}

// 计算预订对话框数据
const currentBookingData = computed(() => {
  if (!currentTrip.value) {
    return {
      checkIn: '',
      checkOut: '',
      guests: 1,
      pricePerNight: 0,
      nights: 0,
      subtotal: 0,
      cleaningFee: 0,
      serviceFee: 0,
      totalCost: 0
    }
  }

  const trip = currentTrip.value
  const checkIn = new Date(trip.checkInDate)
  const checkOut = new Date(trip.checkOutDate)
  const nights = Math.ceil((checkOut - checkIn) / (1000 * 60 * 60 * 24))
  
  // 反推单价（假设清洁费10元，服务费5元）
  const cleaningFee = 10
  const serviceFee = 5
  const subtotal = trip.totalPrice - cleaningFee - serviceFee
  const pricePerNight = Math.round(subtotal / nights)

  return {
    checkIn: formatDate(trip.checkInDate),
    checkOut: formatDate(trip.checkOutDate),
    guests: trip.guestCount,
    pricePerNight: pricePerNight,
    nights: nights,
    subtotal: subtotal,
    cleaningFee: cleaningFee,
    serviceFee: serviceFee,
    totalCost: trip.totalPrice
  }
})

const loadTrips = async () => {
  try {
    const res = await request.get('/api/order/my')
    trips.value = Array.isArray(res) ? res : []
    
    // 检查每个已完成订单是否已评价
    for (const trip of trips.value) {
      if (trip.status === 'COMPLETED') {
        try {
          const canReviewRes = await request.get(`/api/review/can-review/${trip.id}`)
          trip.hasReview = !canReviewRes.canReview
        } catch (error) {
          console.error('检查评价状态失败:', error)
          trip.hasReview = false
        }
      }
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单失败')
  }
}

// 立即支付
const handlePay = (trip) => {
  currentTrip.value = trip
  payDialogVisible.value = true
}

// 确认支付
const confirmPay = async () => {
  if (!currentTrip.value) return

  paying.value = true
  try {
    const res = await request.post(`/api/order/pay/${currentTrip.value.id}`)
    
    if (res.success) {
      ElMessage.success('支付成功！')
      payDialogVisible.value = false
      loadTrips() // 刷新列表
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败，请稍后重试')
  } finally {
    paying.value = false
  }
}

// 取消预订
const handleCancel = async (trip) => {
  try {
    await ElMessageBox.confirm(
      '取消预订后将无法恢复，确定要取消吗？',
      '取消预订',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '我再想想',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    console.log('取消订单请求:', trip.id)
    const res = await request.post(`/api/order/cancel/${trip.id}`)
    console.log('取消订单响应:', res)
    
    if (res.success) {
      ElMessage.success('已取消预订')
      loadTrips() // 刷新列表
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预订失败:', error)
      console.error('错误详情:', error.response)
      ElMessage.error(error.response?.data?.message || '取消失败，请稍后重试')
    }
  }
}

// 删除订单
const handleDelete = async (trip) => {
  try {
    await ElMessageBox.confirm(
      '删除后将无法恢复，确定要删除这个订单吗？',
      '删除订单',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    console.log('删除订单请求:', trip.id)
    const res = await request.delete(`/api/order/${trip.id}`)
    console.log('删除订单响应:', res)
    
    if (res.success) {
      ElMessage.success('订单已删除')
      loadTrips() // 刷新列表
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除订单失败:', error)
      console.error('错误详情:', error.response)
      ElMessage.error(error.response?.data?.message || '删除失败，请稍后重试')
    }
  }
}

// 写评价
const handleReview = (trip) => {
  currentReviewOrder.value = {
    id: trip.id,
    houseTitle: trip.houseTitle,
    houseCoverImage: getCover(trip),
    checkInDate: formatDate(trip.checkInDate),
    checkOutDate: formatDate(trip.checkOutDate)
  }
  reviewDialogVisible.value = true
}

// 评价成功回调
const handleReviewSuccess = () => {
  loadTrips() // 刷新列表
}

// 联系商家
const handleContactHost = async (trip) => {
  try {
    // 获取房源详情以获取房东ID
    const res = await request.get(`/api/host/house/detail/${trip.houseId}`)
    if (res && res.hostId) {
      currentHostId.value = res.hostId
      currentHostName.value = res.hostName || '房东'
      currentHouseId.value = trip.houseId
      currentOrderId.value = trip.id
      messageDialogVisible.value = true
    } else {
      ElMessage.error('无法获取房东信息')
    }
  } catch (error) {
    console.error('获取房东信息失败:', error)
    ElMessage.error('无法联系商家')
  }
}

// 跳转到我的评价页面
const goToMyReviews = () => {
  router.push('/user/profile/reviews')
}

onMounted(() => {
  loadTrips()
})
</script>

<style scoped>
.section-title { 
  font-size: 24px; 
  font-weight: 600;
  margin-bottom: 24px;
  color: #222;
}

.empty-trips {
  padding: 60px 40px;
  border: 1px dashed #DDD;
  border-radius: 12px;
  text-align: center;
  color: #717171;
}

.empty-trips h3 { 
  color: #222;
  font-size: 18px;
  font-weight: 600;
  margin: 15px 0 5px;
}

.empty-trips p {
  color: #717171;
  margin-bottom: 20px;
}

.btn-search { 
  margin-top: 20px;
  border: 1px solid #222;
  font-weight: 600;
  color: #222;
  background: white;
}

.btn-search:hover {
  background: #f7f7f7;
}

.trips-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.trip-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  border: 1px solid #EBEBEB;
  border-radius: 12px;
  transition: box-shadow 0.2s;
  background: white;
}

.trip-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.trip-img {
  width: 160px;
  height: 160px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  flex-shrink: 0;
}

.trip-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.trip-status {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 12px;
  display: inline-block;
  width: fit-content;
}

.status-pending {
  background: #FFF4E6;
  color: #FA8C16;
}

.status-confirmed {
  background: #E6F7FF;
  color: #1890FF;
}

.status-completed {
  background: #F6FFED;
  color: #52C41A;
}

.status-cancelled {
  background: #F5F5F5;
  color: #8C8C8C;
}

.trip-title {
  font-size: 18px;
  font-weight: 600;
  color: #222;
  margin: 0;
  cursor: pointer;
}

.trip-title:hover {
  color: #FF385C;
}

.order-no {
  font-size: 12px;
  color: #717171;
  margin: 0;
}

.trip-dates,
.trip-guests {
  font-size: 14px;
  color: #717171;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.trip-dates .el-icon,
.trip-guests .el-icon {
  font-size: 14px;
}

.trip-price {
  font-size: 16px;
  font-weight: 600;
  color: #222;
  margin-top: auto;
}

.trip-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
  align-items: flex-end;
}

.trip-actions .el-button {
  min-width: 100px;
}
</style>
