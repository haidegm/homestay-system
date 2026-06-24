<template>
  <div class="house-list-page">
    <el-card class="list-card">
      <div class="header">
        <h2>我的房源</h2>
        <el-button type="primary" @click="goAdd" :icon="Plus">
          新增房源
        </el-button>
      </div>

      <el-table :data="houseList" style="width: 100%" v-loading="loading">
        <!-- 封面 -->
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="'http://localhost:8080' + row.coverImage"
              :preview-src-list="[row.coverImage ? 'http://localhost:8080' + row.coverImage : '']"
              :initial-index="0"
              fit="cover"
              class="cover-image"
              preview-teleported
            />
            <div v-else class="no-image">暂无图片</div>
          </template>
        </el-table-column>

        <!-- 标题 -->
        <el-table-column prop="title" label="房源标题" min-width="200" />

        <!-- 城市 -->
        <el-table-column prop="city" label="城市" width="100" />

        <!-- 价格 -->
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price }}/晚</span>
          </template>
        </el-table-column>

        <!-- 停车 -->
        <el-table-column label="停车" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.parking === 'FREE'" type="success" size="small">免费停车</el-tag>
            <el-tag v-else-if="row.parking === 'PAID'" type="warning" size="small">收费停车</el-tag>
            <el-tag v-else type="info" size="small">无停车位</el-tag>
          </template>
        </el-table-column>

        <!-- 设施 -->
        <el-table-column label="设施" min-width="150">
          <template #default="{ row }">
            <el-tag
              v-for="item in row.amenities"
              :key="item"
              size="small"
              class="amenity-tag"
            >
              {{ item }}
            </el-tag>
            <span v-if="!row.amenities || row.amenities.length === 0" class="no-data">无</span>
          </template>
        </el-table-column>

        <!-- 位置 -->
        <el-table-column label="位置" width="100">
          <template #default="{ row }">
            <el-button
              v-if="row.latitude && row.longitude"
              link
              type="primary"
              size="small"
              @click="openMap(row)"
            >
              查看地图
            </el-button>
            <span v-else class="no-data">未设置</span>
          </template>
        </el-table-column>

        <!-- 状态 -->
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PENDING'" type="warning" size="small">待审核</el-tag>
            <el-tag v-else-if="row.status === 'ACTIVE'" type="success" size="small">已上架</el-tag>
            <el-tag v-else-if="row.status === 'INACTIVE'" type="info" size="small">已下架</el-tag>
            <el-tag v-else-if="row.status === 'REJECTED'" type="danger" size="small">已拒绝</el-tag>
            <el-tag v-else type="info" size="small">草稿</el-tag>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="goEdit(row.id)" :icon="Edit">
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(row)"
              :icon="Delete"
            >
              下架
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && houseList.length === 0" description="暂无房源，快去添加吧！" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '../../utils/request'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const houseList = ref([])
const loading = ref(false)

const loadHouseList = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/host/house/list/view')
    
    if (Array.isArray(res)) {
      houseList.value = res
    } else if (res && res.data) {
      houseList.value = res.data
    } else {
      console.warn('返回数据格式不匹配:', res)
    }
  } catch (error) {
    console.error('加载房源失败：', error)
    ElMessage.error('加载房源失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadHouseList()
})

const goAdd = () => {
  router.push('/host/house/add')
}

const goEdit = (id) => {
  router.push(`/host/house/edit/${id}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要下架房源"${row.title}"吗？此操作将删除房源及其所有相关数据，且无法恢复。`,
      '确认下架',
      {
        confirmButtonText: '确定下架',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    console.log('删除房源请求:', row.id)
    const res = await request.delete(`/api/host/house/${row.id}`)
    console.log('删除房源响应:', res)
    
    if (res.success) {
      ElMessage.success('房源已下架')
      loadHouseList() // 重新加载列表
    } else {
      ElMessage.error(res.message || '下架失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架失败:', error)
      ElMessage.error(error.response?.data?.message || '下架失败，请稍后重试')
    }
  }
}

const openMap = (row) => {
  if (!row.latitude || !row.longitude) {
    ElMessage.warning('该房源未设置坐标')
    return
  }
  
  const url = `https://uri.amap.com/marker?position=${row.longitude},${row.latitude}`
  window.open(url)
}
</script>

<style scoped>
.house-list-page {
  max-width: 1400px;
  margin: 0 auto;
}

.list-card {
  border-radius: 12px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #222;
}

.cover-image {
  width: 100px;
  height: 70px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.cover-image:hover {
  transform: scale(1.05);
}

.no-image {
  width: 100px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
  color: #999;
  font-size: 12px;
}

.price-text {
  font-weight: 600;
  color: #222;
}

.amenity-tag {
  margin-right: 6px;
  margin-bottom: 4px;
}

.no-data {
  color: #999;
  font-size: 14px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background: #fafafa;
  color: #222;
  font-weight: 600;
}

:deep(.el-button--small) {
  padding: 5px 12px;
}
</style>