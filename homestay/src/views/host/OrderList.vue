<template>
  <div class="order-page">

    <!-- 标题 -->
    <div class="page-header">
      <h2>订单管理</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="stats">
      <div class="stat-card">
        <div class="stat-title">订单总数</div>
        <div class="stat-value">{{ stats.totalOrders }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">待入住</div>
        <div class="stat-value">{{ stats.pendingCheckin }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">总收入</div>
        <div class="stat-value">¥{{ stats.totalIncome }}</div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="statusFilter" placeholder="订单状态" style="width:150px">
        <el-option label="全部" value="" />
        <el-option label="待支付" value="PENDING" />
        <el-option label="已确认" value="CONFIRMED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>

      <el-input
        v-model="searchKeyword"
        placeholder="搜索客人姓名"
        style="width:200px"
      />

      <el-button type="primary">查询</el-button>
    </div>

    <!-- 订单列表 -->
    <div class="order-list">
      <div
        class="order-card"
        v-for="order in filteredOrders"
        :key="order.id"
      >
        <div class="left">
          <img :src="getCover(order)" />
        </div>

        <div class="middle">
          <div class="house-name">{{ order.houseName || '未命名房源' }}</div>
          <div class="guest">
            客人：{{ order.guestName || ('用户#' + order.userId) }}
          </div>
          <div class="date">
            {{ formatDate(order.checkInDate) }} - {{ formatDate(order.checkOutDate) }}
          </div>
          <div class="price">
            总价：¥{{ order.totalPrice }}
          </div>
          <div class="house-id">订单号：{{ order.orderNo || ('#' + order.id) }}</div>
        </div>

        <div class="right">
          <div class="status" :class="order.status.toLowerCase()">
            {{ formatStatus(order.status) }}
          </div>

          <div class="actions">
            <el-button size="small">详情</el-button>
            <el-button
              size="small"
              type="success"
              v-if="order.status === 'CONFIRMED'"
            >
              联系客人
            </el-button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../utils/request'

/* 筛选 */
const statusFilter = ref('')
const searchKeyword = ref('')

const orders = ref([])
const stats = ref({
  totalOrders: 0,
  pendingCheckin: 0,
  totalIncome: 0
})

/* 筛选逻辑 */
const filteredOrders = computed(() => {
  const keyword = searchKeyword.value.trim()
  return orders.value.filter(order => {
    const matchStatus = statusFilter.value
      ? order.status === statusFilter.value
      : true

    const matchSearch = keyword
      ? String(order.orderNo || '').includes(keyword) ||
        String(order.houseId || '').includes(keyword) ||
        String(order.userId || '').includes(keyword) ||
        String(order.guestName || '').includes(keyword) ||
        String(order.houseName || '').includes(keyword)
      : true

    return matchStatus && matchSearch
  })
})

/* 状态显示 */
const formatStatus = (status) => {
  const map = {
    PENDING: '待支付',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const formatDate = (value) => {
  if (!value) return ''
  return String(value).slice(0, 10)
}

const getCover = (order) => {
  if (!order.coverImage) return 'https://picsum.photos/200/120'
  return order.coverImage.startsWith('http')
    ? order.coverImage
    : `http://localhost:8080${order.coverImage}`
}

const loadStats = async () => {
  const res = await request.get('/api/host/stats')
  stats.value.totalOrders = res.totalOrders || 0
  stats.value.totalIncome = res.totalIncome || 0
}

const loadOrders = async () => {
  const res = await request.get('/api/host/orders')
  orders.value = Array.isArray(res) ? res : []
  stats.value.pendingCheckin = orders.value.filter(o => o.status === 'CONFIRMED').length
  if (stats.value.totalOrders === 0) {
    stats.value.totalOrders = orders.value.length
  }
}

onMounted(async () => {
  await Promise.all([loadStats(), loadOrders()])
})
</script>

<style scoped>
.order-page {
  padding: 30px;
  background: #f7f7f7;
}

.page-header {
  margin-bottom: 20px;
}

.stats {
  display: flex;
  gap: 20px;
  margin-bottom: 25px;
}

.stat-card {
  flex: 1;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.stat-title {
  font-size: 14px;
  color: #888;
}

.stat-value {
  font-size: 22px;
  font-weight: bold;
  margin-top: 5px;
}

.filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-card {
  display: flex;
  background: #fff;
  border-radius: 14px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: all 0.2s ease;
}

.order-card:hover {
  transform: translateY(-2px);
}

.left img {
  width: 200px;
  height: 120px;
  object-fit: cover;
  border-radius: 10px;
}

.middle {
  flex: 1;
  margin-left: 20px;
}

.house-name {
  font-size: 18px;
  font-weight: 600;
}

.guest,
.date,
.price,
.house-id {
  margin-top: 6px;
  color: #666;
}

.right {
  text-align: right;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.status {
  font-weight: bold;
  padding: 4px 10px;
  border-radius: 20px;
  display: inline-block;
}

.status.pending {
  background: #fff3cd;
  color: #856404;
}

.status.confirmed {
  background: #d4edda;
  color: #155724;
}

.status.completed {
  background: #e2e3ff;
  color: #383d7c;
}

.status.cancelled {
  background: #f8d7da;
  color: #721c24;
}
</style>
