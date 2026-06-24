<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="emitVisible"
    :show-close="true"
    width="800px"
    center
    class="reviews-dialog"
    :close-on-click-modal="false"
  >
    <template #header>
      <div class="dialog-header">
        <h2>
          <el-icon><StarFilled /></el-icon>
          {{ averageRating }} · {{ totalReviews }}条评价
        </h2>
      </div>
    </template>

    <div class="dialog-content">
      <!-- 筛选标签 -->
      <div class="filter-tabs">
        <el-button
          :class="{ 'active': currentFilter === 'all' }"
          @click="currentFilter = 'all'"
          class="filter-btn"
        >
          全部 ({{ reviews.length }})
        </el-button>
        <el-button
          :class="{ 'active': currentFilter === 'good' }"
          @click="currentFilter = 'good'"
          class="filter-btn"
        >
          好评 ({{ goodReviews.length }})
        </el-button>
        <el-button
          :class="{ 'active': currentFilter === 'medium' }"
          @click="currentFilter = 'medium'"
          class="filter-btn"
        >
          中评 ({{ mediumReviews.length }})
        </el-button>
        <el-button
          :class="{ 'active': currentFilter === 'bad' }"
          @click="currentFilter = 'bad'"
          class="filter-btn"
        >
          差评 ({{ badReviews.length }})
        </el-button>
      </div>

      <!-- 评价列表 -->
      <div class="reviews-list" v-if="filteredReviews.length > 0">
        <div 
          class="review-item" 
          v-for="review in filteredReviews" 
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
            <span class="rating-text">{{ review.rating }}分</span>
          </div>
          
          <div class="review-content">{{ review.comment }}</div>
          
          <!-- 评价图片 -->
          <div class="review-images" v-if="review.imageList && review.imageList.length > 0">
            <el-image
              v-for="(image, index) in review.imageList" 
              :key="index"
              :src="getImageUrl(image)"
              :preview-src-list="review.imageList.map(img => getImageUrl(img))"
              :initial-index="index"
              fit="cover"
              class="review-image"
              :alt="`评价图片${index + 1}`"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>加载失败</span>
                </div>
              </template>
            </el-image>
          </div>

          <!-- 房东回复 -->
          <div class="host-reply" v-if="review.reply">
            <div class="reply-header">
              <el-icon><ChatDotRound /></el-icon>
              <span>房东回复</span>
            </div>
            <div class="reply-content">{{ review.reply }}</div>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无评价" :image-size="100" />
    </div>

  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { StarFilled, ChatDotRound, Picture } from '@element-plus/icons-vue'

const props = defineProps({
  visible: Boolean,
  reviews: {
    type: Array,
    default: () => []
  },
  averageRating: {
    type: [String, Number],
    default: '新'
  }
})

const emit = defineEmits(['update:visible'])

const emitVisible = (val) => emit('update:visible', val)

const currentFilter = ref('all')

// 处理评价数据，解析图片字段
const processedReviews = computed(() => {
  return props.reviews.map(review => {
    const imageList = review.images ? review.images.split(',').filter(img => img.trim()) : []
    return {
      ...review,
      imageList
    }
  })
})

// 好评：>4星
const goodReviews = computed(() => {
  return processedReviews.value.filter(review => review.rating > 4)
})

// 中评：>2星且<=4星
const mediumReviews = computed(() => {
  return processedReviews.value.filter(review => review.rating > 2 && review.rating <= 4)
})

// 差评：<=2星
const badReviews = computed(() => {
  return processedReviews.value.filter(review => review.rating <= 2)
})

// 根据筛选条件显示评价
const filteredReviews = computed(() => {
  switch (currentFilter.value) {
    case 'good':
      return goodReviews.value
    case 'medium':
      return mediumReviews.value
    case 'bad':
      return badReviews.value
    default:
      return processedReviews.value
  }
})

const totalReviews = computed(() => props.reviews.length)

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

const getImageUrl = (imagePath) => {
  if (!imagePath) return ''
  return imagePath.startsWith('http') ? imagePath : `http://localhost:8080${imagePath}`
}
</script>

<style scoped>
.reviews-dialog :deep(.el-dialog) {
  border-radius: 16px;
  padding: 0;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.reviews-dialog :deep(.el-dialog__header) {
  padding: 32px 32px 0;
  margin: 0;
  flex-shrink: 0;
}

.reviews-dialog :deep(.el-dialog__body) {
  padding: 24px 32px 32px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

.dialog-header {
  text-align: center;
}

.dialog-header h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #222;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.dialog-header h2 .el-icon {
  color: #FF385C;
  font-size: 22px;
}

.dialog-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 筛选标签 */
.filter-tabs {
  display: flex;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #EBEBEB;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 8px 16px;
  border-radius: 20px;
  border: 1px solid #DDDDDD;
  background: white;
  color: #222;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  height: auto;
}

.filter-btn:hover {
  border-color: #222;
  background: #F7F7F7;
}

.filter-btn.active {
  background: #222;
  border-color: #222;
  color: white;
}

/* 评价列表 */
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 32px;
  max-height: 500px;
  overflow-y: auto;
  padding-right: 8px;
}

/* 自定义滚动条 */
.reviews-list::-webkit-scrollbar {
  width: 6px;
}

.reviews-list::-webkit-scrollbar-track {
  background: #F7F7F7;
  border-radius: 3px;
}

.reviews-list::-webkit-scrollbar-thumb {
  background: #DDDDDD;
  border-radius: 3px;
}

.reviews-list::-webkit-scrollbar-thumb:hover {
  background: #B0B0B0;
}

.review-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 32px;
  border-bottom: 1px solid #EBEBEB;
}

.review-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
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
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reviewer-name {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}

.review-date {
  font-size: 14px;
  color: #717171;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-text {
  font-size: 14px;
  font-weight: 600;
  color: #222;
}

.review-content {
  font-size: 16px;
  line-height: 24px;
  color: #222;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 评价图片 */
.review-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  margin-top: 8px;
}

.review-image {
  width: 100%;
  height: 120px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
  overflow: hidden;
}

.review-image:hover {
  transform: scale(1.05);
}

.review-image :deep(.el-image__inner) {
  border-radius: 8px;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.review-image :deep(.el-image__wrapper) {
  width: 100%;
  height: 100%;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  color: #999;
  font-size: 12px;
}

/* 房东回复 */
.host-reply {
  background: #F7F7F7;
  border-radius: 8px;
  padding: 16px;
  margin-top: 8px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #222;
  margin-bottom: 8px;
}

.reply-header .el-icon {
  font-size: 16px;
  color: #717171;
}

.reply-content {
  font-size: 14px;
  line-height: 20px;
  color: #222;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 空状态 */
.el-empty {
  padding: 40px 0;
}
</style>
