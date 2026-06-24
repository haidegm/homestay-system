<template>
  <div class="house-management">
    <div class="page-header">
      <h1>房源管理</h1>
      <p class="subtitle">管理平台所有房源信息</p>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索房源标题或地址"
            clearable
            style="width: 300px"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="房源类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" clearable style="width: 150px">
            <el-option label="整套房源" value="entire" />
            <el-option label="独立房间" value="private" />
            <el-option label="合住房间" value="shared" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已上架" value="ACTIVE" />
            <el-option label="已下架" value="INACTIVE" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 房源列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="houseList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="房源信息" width="350">
          <template #default="{ row }">
            <div class="house-info">
              <el-image
                :src="row.coverImage"
                fit="cover"
                class="house-image"
              />
              <div class="house-details">
                <div class="house-title">{{ row.title }}</div>
                <div class="house-address">{{ row.address }}</div>
                <div class="house-type">{{ getTypeText(row.type) }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="房东" width="150">
          <template #default="{ row }">
            <div class="host-info">
              <el-avatar :size="32" :src="row.hostAvatar" />
              <span>{{ row.hostName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格/晚" width="120">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="guests" label="可住人数" width="100" align="center" />
        <el-table-column prop="bedrooms" label="卧室" width="80" align="center" />
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            <div class="rating" v-if="row.rating">
              <el-icon color="#FFB400"><StarFilled /></el-icon>
              <span>{{ row.ratingDisplay }}</span>
            </div>
            <span v-else class="new-house">新房源</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            
            <!-- 待审核状态显示审核按钮 -->
            <template v-if="row.status === 'PENDING'">
              <el-button link type="success" size="small" @click="handleApprove(row)">
                通过
              </el-button>
              <el-button link type="danger" size="small" @click="handleReject(row)">
                拒绝
              </el-button>
            </template>
            
            <!-- 已上架状态显示下架按钮 -->
            <el-button 
              v-else-if="row.status === 'ACTIVE'"
              link 
              type="warning" 
              size="small"
              @click="handleToggleStatus(row)"
            >
              下架
            </el-button>
            
            <!-- 已下架或已拒绝状态显示上架按钮 -->
            <el-button 
              v-else-if="row.status === 'INACTIVE' || row.status === 'REJECTED'"
              link 
              type="success" 
              size="small"
              @click="handleToggleStatus(row)"
            >
              上架
            </el-button>
            
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectVisible" title="拒绝审核" width="500px" @close="resetRejectForm">
      <p class="reject-house-title" v-if="rejectTarget">
        <strong>房源：</strong>{{ rejectTarget.title }}
      </p>

      <el-radio-group v-model="selectedReason" class="reason-radio-group">
        <el-radio
          v-for="(reason, index) in predefinedReasons"
          :key="index"
          :label="reason"
          :value="reason"
          class="reason-radio"
        />
        <el-radio label="__OTHER__" :value="'__OTHER__'" class="reason-radio">
          其他原因（请填写）
        </el-radio>
      </el-radio-group>

      <el-input
        v-if="selectedReason === '__OTHER__'"
        v-model="customReason"
        type="textarea"
        :rows="3"
        placeholder="请输入具体的拒绝原因..."
        class="custom-reason-input"
        maxlength="200"
        show-word-limit
      />

      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button
          type="danger"
          @click="confirmReject"
          :disabled="selectedReason === '__OTHER__' && !customReason.trim()"
        >
          确认拒绝
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="房源详情" width="800px">
      <div v-if="currentHouse" class="detail-content">
        <el-image
          :src="currentHouse.coverImage"
          fit="cover"
          class="detail-image"
        />
        
        <h3 class="detail-title">{{ currentHouse.title }}</h3>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="房源ID">{{ currentHouse.id }}</el-descriptions-item>
          <el-descriptions-item label="房源类型">{{ getTypeText(currentHouse.type) }}</el-descriptions-item>
          <el-descriptions-item label="房东">{{ currentHouse.hostName }}</el-descriptions-item>
          <el-descriptions-item label="价格/晚">¥{{ currentHouse.price }}</el-descriptions-item>
          <el-descriptions-item label="可住人数">{{ currentHouse.guests }}人</el-descriptions-item>
          <el-descriptions-item label="卧室数">{{ currentHouse.bedrooms }}间</el-descriptions-item>
          <el-descriptions-item label="床位数">{{ currentHouse.beds }}张</el-descriptions-item>
          <el-descriptions-item label="卫生间">{{ currentHouse.bathrooms }}间</el-descriptions-item>
          <el-descriptions-item label="评分">
            <div class="rating" v-if="currentHouse.rating">
              <el-icon color="#FFB400"><StarFilled /></el-icon>
              <span>{{ currentHouse.ratingDisplay }}</span>
            </div>
            <span v-else class="new-house">新房源</span>
          </el-descriptions-item>
          <el-descriptions-item label="订单数">{{ currentHouse.orderCount }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentHouse.status)">
              {{ getStatusText(currentHouse.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentHouse.status === 'REJECTED' && currentHouse.rejectReason" label="拒绝原因" :span="2">
            <span style="color: #E6A23C;">{{ currentHouse.rejectReason }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentHouse.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ currentHouse.address }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ currentHouse.description }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, StarFilled } from '@element-plus/icons-vue'
import request from '../../utils/request'

const loading = ref(false)
const detailVisible = ref(false)
const currentHouse = ref(null)

// 拒绝原因弹窗状态
const rejectVisible = ref(false)
const rejectTarget = ref(null)
const selectedReason = ref('')
const customReason = ref('')

const predefinedReasons = [
  '图片不符合规范（模糊、带水印、数量不足等）',
  '房源信息不完整（地址、描述、设施等缺失）',
  '房源描述不真实（与实际情况不符）',
  '不符合平台定位（类型或品质不达标）',
  '涉嫌虚假房源（盗图、不存在的房源等）'
]

const searchForm = ref({
  keyword: '',
  type: '',
  status: ''
})

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const houseList = ref([])

onMounted(() => {
  loadHouseList()
})

const loadHouseList = async () => {
  loading.value = true
  
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size
    }
    
    if (searchForm.value.keyword) {
      params.keyword = searchForm.value.keyword
    }
    
    if (searchForm.value.type) {
      params.type = searchForm.value.type
    }
    
    if (searchForm.value.status) {
      params.status = searchForm.value.status
    }
    
    const res = await request.get('/api/admin/houses', { params })
    
    // console.log('房源列表原始数据:', res.list)
    
    houseList.value = res.list.map(house => ({
      ...house,
      coverImage: house.coverImage 
        ? (house.coverImage.startsWith('http') ? house.coverImage : `http://localhost:8080${house.coverImage}`)
        : 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=400',
      hostAvatar: house.hostAvatar 
        ? (house.hostAvatar.startsWith('http') ? house.hostAvatar : `http://localhost:8080${house.hostAvatar}`)
        : 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      rating: house.rating, // 保持原始值，null表示没有评分
      ratingDisplay: house.rating ? Number(house.rating).toFixed(1) : '新房源',
      createdAt: formatDateTime(house.createdAt)
    }))
    
    // console.log('处理后的房源列表:', houseList.value)
    
    pagination.value.total = res.total
  } catch (error) {
    console.error('加载房源列表失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const getTypeText = (type) => {
  const map = {
    'entire': '整套房源',
    'private': '独立房间',
    'shared': '合住房间'
  }
  return map[type] || type
}

const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'ACTIVE': 'success',
    'INACTIVE': 'info',
    'REJECTED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待审核',
    'ACTIVE': '已上架',
    'INACTIVE': '已下架',
    'REJECTED': '已拒绝'
  }
  return map[status] || status
}

const handleSearch = () => {
  pagination.value.page = 1
  loadHouseList()
}

const handlePageChange = () => {
  loadHouseList()
}

const handleReset = () => {
  searchForm.value = {
    keyword: '',
    type: '',
    status: ''
  }
  handleSearch()
}

const handleViewDetail = (row) => {
  currentHouse.value = row
  detailVisible.value = true
}

const handleToggleStatus = (row) => {
  const action = row.status === 'ACTIVE' ? '下架' : '上架'
  ElMessageBox.confirm(`确定要${action}该房源吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/api/admin/houses/${row.id}/toggle-status`)
      row.status = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
      ElMessage.success(`${action}成功`)
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const handleApprove = (row) => {
  ElMessageBox.confirm('确定通过该房源的审核吗？', '审核确认', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await request.put(`/api/admin/houses/${row.id}/approve`)
      row.status = 'ACTIVE'
      ElMessage.success('审核通过，房源已上架')
    } catch (error) {
      console.error('审核失败:', error)
      ElMessage.error('审核失败')
    }
  }).catch(() => {})
}

const handleReject = (row) => {
  rejectTarget.value = row
  selectedReason.value = predefinedReasons[0]
  customReason.value = ''
  rejectVisible.value = true
}

const confirmReject = async () => {
  const reason = selectedReason.value === '__OTHER__'
    ? customReason.value.trim()
    : selectedReason.value

  if (!reason) {
    ElMessage.warning('请选择或填写拒绝原因')
    return
  }

  try {
    await request.put(`/api/admin/houses/${rejectTarget.value.id}/reject`, null, {
      params: { reason }
    })
    rejectTarget.value.status = 'REJECTED'
    rejectTarget.value.rejectReason = reason
    ElMessage.success('已拒绝该房源，已通知房东')
    rejectVisible.value = false
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const resetRejectForm = () => {
  rejectTarget.value = null
  selectedReason.value = ''
  customReason.value = ''
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该房源吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
      await request.delete(`/api/admin/houses/${row.id}`)
      const index = houseList.value.findIndex(item => item.id === row.id)
      if (index > -1) {
        houseList.value.splice(index, 1)
        pagination.value.total--
      }
      ElMessage.success('删除成功')
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.house-management {
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
  margin-bottom: 20px;
  border-radius: 12px;
}

.table-card {
  border-radius: 12px;
}

.house-info {
  display: flex;
  gap: 12px;
}

.house-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  flex-shrink: 0;
}

.house-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.house-title {
  font-size: 14px;
  font-weight: 600;
  color: #222;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.house-address {
  font-size: 12px;
  color: #717171;
}

.house-type {
  font-size: 12px;
  color: #717171;
}

.host-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.price-text {
  font-weight: 600;
  color: #FF385C;
  font-size: 16px;
}

.rating {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
}

.new-house {
  color: #909399;
  font-size: 13px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  padding: 20px 0;
}

.detail-image {
  width: 100%;
  height: 300px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.detail-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #222;
}

/* 拒绝原因弹窗 */
.reject-house-title {
  margin: 0 0 20px 0;
  font-size: 14px;
  color: #333;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.reason-radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reason-radio {
  margin-right: 0;
  height: 36px;
  line-height: 36px;
}

.custom-reason-input {
  margin-top: 16px;
}
</style>
