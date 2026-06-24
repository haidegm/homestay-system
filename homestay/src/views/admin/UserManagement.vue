<template>
  <div class="user-management">
    <div class="page-header">
      <h1>用户管理</h1>
      <p class="subtitle">管理平台所有用户账号</p>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户姓名、邮箱或手机号"
            clearable
            style="width: 300px"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="已禁用" value="DISABLED" />
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

    <!-- 用户列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="userList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户信息" width="280">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="48" :src="row.avatar" />
              <div class="user-details">
                <div class="username">{{ row.nickname || row.username }}</div>
                <div class="email">{{ row.email }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="orderCount" label="订单数量" width="100" align="center" />
        <el-table-column prop="wishlistCount" label="收藏数量" width="100" align="center" />
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button 
              link 
              :type="row.status === 'ACTIVE' ? 'danger' : 'success'" 
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="600px">
      <div v-if="currentUser" class="detail-content">
        <div class="detail-header">
          <el-avatar :size="80" :src="currentUser.avatar" />
          <div class="detail-info">
            <h3>{{ currentUser.nickname || currentUser.username }}</h3>
            <p>{{ currentUser.email }}</p>
          </div>
        </div>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="订单数量">{{ currentUser.orderCount }}</el-descriptions-item>
          <el-descriptions-item label="收藏数量">{{ currentUser.wishlistCount }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="currentUser.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ currentUser.status === 'ACTIVE' ? '正常' : '已禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间" :span="2">{{ currentUser.createdAt }}</el-descriptions-item>
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
import { Search } from '@element-plus/icons-vue'
import request from '../../utils/request'

const loading = ref(false)
const detailVisible = ref(false)
const currentUser = ref(null)

const searchForm = ref({
  keyword: '',
  status: ''
})

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const userList = ref([])

onMounted(() => {
  loadUserList()
})

const loadUserList = async () => {
  loading.value = true
  
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size
    }
    
    if (searchForm.value.keyword) {
      params.keyword = searchForm.value.keyword
    }
    
    if (searchForm.value.status) {
      params.status = searchForm.value.status
    }
    
    const res = await request.get('/api/admin/users', { params })
    
    console.log('用户列表原始数据:', res.list)
    
    userList.value = res.list.map(user => ({
      ...user,
      avatar: user.avatar 
        ? (user.avatar.startsWith('http') ? user.avatar : `http://localhost:8080${user.avatar}`)
        : 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      email: user.email || user.username + '@example.com',
      createdAt: formatDateTime(user.createdAt)
    }))
    
    console.log('处理后的用户列表:', userList.value)
    
    pagination.value.total = res.total
  } catch (error) {
    console.error('加载用户列表失败:', error)
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

const handleSearch = () => {
  pagination.value.page = 1
  loadUserList()
}

const handlePageChange = () => {
  loadUserList()
}

const handleReset = () => {
  searchForm.value = {
    keyword: '',
    status: ''
  }
  handleSearch()
}

const handleViewDetail = (row) => {
  currentUser.value = row
  detailVisible.value = true
}

const handleToggleStatus = (row) => {
  const action = row.status === 'ACTIVE' ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}该用户账号吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/api/admin/users/${row.id}/toggle-status`)
      row.status = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
      ElMessage.success(`${action}成功`)
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.user-management {
  max-width: 1400px;
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

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  flex: 1;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #222;
  margin-bottom: 4px;
}

.email {
  font-size: 12px;
  color: #717171;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #EBEBEB;
}

.detail-info h3 {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #222;
}

.detail-info p {
  font-size: 14px;
  color: #717171;
  margin: 0;
}
</style>
