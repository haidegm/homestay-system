<template>
  <div class="my-reviews">
    <h2 class="page-title">我的评价</h2>

    <div v-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-else-if="reviews.length === 0" class="empty">
      <el-empty description="还没有评价记录" />
    </div>

    <div v-else class="reviews-list">
      <div v-for="review in reviews" :key="review.id" class="review-card">
        <!-- 房源信息 -->
        <div class="house-info">
          <el-image
            :src="review.houseCoverImage || defaultImage"
            fit="cover"
            class="house-image"
          />
          <div class="house-details">
            <h3>{{ review.houseTitle }}</h3>
            <div class="review-meta">
              <el-rate
                v-model="review.rating"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value} 分"
              />
              <span class="review-date">{{ formatDate(review.createdTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 评价内容 -->
        <div class="review-content">
          <p class="comment">{{ review.comment }}</p>
          
          <!-- 评价图片 -->
          <div v-if="review.images" class="review-images">
            <el-image
              v-for="(img, index) in getImageList(review.images)"
              :key="index"
              :src="getImageUrl(img)"
              :preview-src-list="getImageList(review.images).map(i => getImageUrl(i))"
              fit="cover"
              class="review-img"
            />
          </div>
        </div>

        <!-- 匿名状态 -->
        <div class="review-status">
          <el-tag :type="review.isAnonymous ? 'info' : 'success'" size="small">
            {{ review.isAnonymous ? '匿名评价' : '实名评价' }}
          </el-tag>
        </div>

        <!-- 房东回复 -->
        <div v-if="review.reply" class="host-reply">
          <div class="reply-label">房东回复：</div>
          <p>{{ review.reply }}</p>
        </div>

        <!-- 操作按钮 -->
        <div class="review-actions">
          <el-button
            size="small"
            @click="toggleAnonymous(review)"
          >
            切换为{{ review.isAnonymous ? '实名' : '匿名' }}
          </el-button>
          <el-button
            size="small"
            type="danger"
            @click="handleDelete(review)"
          >
            删除评价
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import request from '../../../utils/request'

const loading = ref(false)
const reviews = ref([])
const defaultImage = 'https://via.placeholder.com/100x100?text=No+Image'

// 加载我的评价
const loadReviews = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/review/my')
    reviews.value = res || []
  } catch (error) {
    console.error('加载评价失败:', error)
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 获取图片列表
const getImageList = (images) => {
  if (!images) return []
  return images.split(',').filter(img => img.trim())
}

// 获取图片URL
const getImageUrl = (img) => {
  if (!img) return defaultImage
  if (img.startsWith('http')) return img
  return `http://localhost:8080${img}`
}

// 切换匿名状态
const toggleAnonymous = async (review) => {
  try {
    const res = await request.put(`/api/review/${review.id}/toggle-anonymous`)
    review.isAnonymous = res.isAnonymous
    ElMessage.success('已切换为' + (res.isAnonymous ? '匿名' : '实名') + '评价')
  } catch (error) {
    console.error('切换失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除评价
const handleDelete = async (review) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评价吗？此操作不可恢复。',
      '删除评价',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await request.delete(`/api/review/${review.id}`)
    ElMessage.success('删除成功')
    loadReviews()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
.my-reviews {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 24px 0;
  color: #222;
}

.loading {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.loading .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.empty {
  padding: 60px 0;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-card {
  background: white;
  border: 1px solid #ebebeb;
  border-radius: 12px;
  padding: 20px;
  transition: box-shadow 0.2s;
}

.review-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.house-info {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.house-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  flex-shrink: 0;
}

.house-details {
  flex: 1;
}

.house-details h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #222;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.review-date {
  font-size: 14px;
  color: #717171;
}

.review-content {
  margin-bottom: 16px;
}

.comment {
  font-size: 15px;
  line-height: 1.6;
  color: #222;
  margin: 0 0 12px 0;
}

.review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.review-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  cursor: pointer;
}

.review-status {
  margin-bottom: 12px;
}

.host-reply {
  background: #f7f7f7;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.reply-label {
  font-size: 13px;
  font-weight: 600;
  color: #717171;
  margin-bottom: 6px;
}

.host-reply p {
  font-size: 14px;
  line-height: 1.6;
  color: #222;
  margin: 0;
}

.review-actions {
  display: flex;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebebeb;
}
</style>
