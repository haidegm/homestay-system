<template>
  <div class="order-management">
    <div class="page-header">
      <h1>订单管理</h1>
      <p class="subtitle">管理平台所有订单记录</p>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索订单号、用户、房源或房东"
          clearable
          style="width: 300px"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select
          v-model="statusFilter"
          placeholder="订单状态"
          clearable
          style="width: 150px"
          @change="handleSearch"
        >
          <el-option label="全部" value="" />
          <el-option label="待支付" value="PENDING" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>

        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>

        <el-button type="success" @click="handleRefreshOrderStatus" :loading="refreshing">
          <el-icon><Refresh /></el-icon>
          刷新订单状态
        </el-button>
      </div>
    </el-card>

    <!-- 订单列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="orderList"
        v-loading="loading"
        style="width: 100%"
        :header-cell-style="{ background: '#fafafa', color: '#222' }"
      >
        <el-table-column label="订单信息" width="280">
          <template #default="{ row }">
            <div class="order-info">
              <div class="order-no">{{ row.orderNo }}</div>
              <div class="order-time">{{ formatDate(row.createdTime) }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="房源" width="300">
          <template #default="{ row }">
            <div class="house-info">
              <img :src="row.houseCoverImage || defaultImage" class="house-img" />
              <div class="house-details">
                <div class="house-title">{{ row.houseTitle }}</div>
                <div class="house-address">{{ row.houseAddress }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="用户" width="180">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="36" :src="row.userAvatar || defaultAvatar" />
              <div class="user-details">
                <div class="user-name">{{ row.userName }}</div>
                <div class="user-phone">{{ row.userPhone || '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="房东" width="180">
          <template #default="{ row }">
            <div class="host-info">
              <div class="host-name">{{ row.hostName }}</div>
              <div class="host-phone">{{ row.hostPhone || '-' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="入住信息" width="200">
          <template #default="{ row }">
            <div class="stay-info">
              <div>入住: {{ row.checkInDate }}</div>
              <div>退房: {{ row.checkOutDate }}</div>
              <div class="nights">{{ row.nights }} 晚 · {{ row.guestCount }} 位房客</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">
            <div class="price">¥{{ row.totalPrice }}</div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                v-if="row.status === 'PENDING' || row.status === 'CONFIRMED'"
                type="warning"
                size="small"
                @click="handleCancel(row)"
              >
                取消订单
              </el-button>
              <el-button
                type="primary"
                size="small"
                @click="openStatusDialog(row)"
              >
                更改订单状态
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="statusDialogVisible"
      title="Change Order Status"
      width="420px"
    >
      <el-form label-width="100px">
        <el-form-item label="Order No.">
          <span>{{ currentOrder?.orderNo || '-' }}</span>
        </el-form-item>
        <el-form-item label="Current">
          <el-tag v-if="currentOrder" :type="getStatusType(currentOrder.status)">
            {{ formatStatus(currentOrder.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="New Status">
          <el-select v-model="selectedStatus" placeholder="Select status" style="width: 100%">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="updatingStatus" @click="handleUpdateStatus">
          Save
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import request from '../../utils/request'

const loading = ref(false)
const refreshing = ref(false)
const updatingStatus = ref(false)
const orderList = ref([])
const searchKeyword = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusDialogVisible = ref(false)
const currentOrder = ref(null)
const selectedStatus = ref('')

const statusOptions = [
  { label: '待支付', value: 'PENDING' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

const defaultImage = 'https://via.placeholder.com/80x60?text=No+Image'
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    
    const res = await request.get('/api/admin/orders', { params })
    orderList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadOrders()
}

// 翻页
const handlePageChange = () => {
  loadOrders()
}

// 刷新订单状态
const handleRefreshOrderStatus = async () => {
  refreshing.value = true
  try {
    const res = await request.post('/api/admin/scheduler/check-orders')
    if (res.success) {
      ElMessage.success('订单状态已更新')
      // 重新加载订单列表
      await loadOrders()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    console.error('刷新订单状态失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    refreshing.value = false
  }
}

// 取消订单
const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消订单 ${order.orderNo} 吗？`,
      '取消订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await request.put(`/api/admin/orders/${order.id}/cancel`)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

// 删除订单
const openStatusDialog = (order) => {
  currentOrder.value = order
  selectedStatus.value = order.status
  statusDialogVisible.value = true
}

const handleUpdateStatus = async () => {
  if (!currentOrder.value || !selectedStatus.value) {
    ElMessage.warning('请选择订单状态')
    return
  }

  updatingStatus.value = true
  try {
    await request.put(`/api/admin/orders/${currentOrder.value.id}/status`, {
      status: selectedStatus.value
    })
    ElMessage.success('订单状态已更新')
    statusDialogVisible.value = false
    await loadOrders()
  } catch (error) {
    console.error('更新订单状态失败:', error)
    ElMessage.error('更新订单状态失败')
  } finally {
    updatingStatus.value = false
  }
}

const handleDelete = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除订单 ${order.orderNo} 吗？此操作不可恢复。`,
      '删除订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await request.delete(`/api/admin/orders/${order.id}`)
    ElMessage.success('订单已删除')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除订单失败:', error)
      ElMessage.error('删除订单失败')
    }
  }
}

// 格式化状态
const formatStatus = (status) => {
  const map = {
    PENDING: '待支付',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    CONFIRMED: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-management {
  max-width: 1600px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #222;
}

.subtitle {
  font-size: 14px;
  color: #717171;
  margin: 0;
}

.filter-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.table-card {
  border-radius: 12px;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-no {
  font-weight: 600;
  color: #222;
  font-size: 14px;
}

.order-time {
  font-size: 12px;
  color: #717171;
}

.house-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.house-img {
  width: 80px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.house-details {
  flex: 1;
  min-width: 0;
}

.house-title {
  font-weight: 500;
  color: #222;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.house-address {
  font-size: 12px;
  color: #717171;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-info {
  display: flex;
  gap: 10px;
  align-items: center;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-weight: 500;
  color: #222;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-phone {
  font-size: 12px;
  color: #717171;
  margin-top: 2px;
}

.host-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.host-name {
  font-weight: 500;
  color: #222;
  font-size: 14px;
}

.host-phone {
  font-size: 12px;
  color: #717171;
}

.stay-info {
  font-size: 13px;
  color: #222;
  line-height: 1.6;
}

.nights {
  color: #717171;
  margin-top: 4px;
}

.price {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
