<template>
  <div class="review-page">

    <h2 class="page-title">评价管理</h2>

    <!-- 筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="filter">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="replied">已回复</el-radio-button>
        <el-radio-button label="pending">未回复</el-radio-button>
        <el-radio-button label="low">差评</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 列表 -->
    <div class="review-list" v-loading="loading">

      <el-empty 
        v-if="!loading && filteredReviews.length === 0" 
        description="暂无评价"
        :image-size="120"
      >
        <el-button type="primary" @click="$router.push('/host/house/list')">
          查看房源
        </el-button>
      </el-empty>

      <div
        class="review-card"
        v-for="item in filteredReviews"
        :key="item.id"
      >

        <div class="top">
          <div class="user-info">
            <img :src="item.avatar" class="avatar" @error="handleImageError"/>
            <div>
              <div class="username">{{ item.username }}</div>
              <el-rate
                v-model="item.rating"
                disabled
                show-score
                text-color="#ff9900"
                :allow-half="true"
              />
            </div>
          </div>

          <div class="house-info">
            <div class="house">{{ item.houseName }}</div>
            <div class="time" v-if="item.createdTime">
              {{ formatDate(item.createdTime) }}
            </div>
          </div>
        </div>

        <div class="content">
          {{ item.content }}
        </div>

        <!-- 回复 -->
        <div v-if="item.reply" class="reply-box">
          <div class="reply-label">你的回复：</div>
          <div>{{ item.reply }}</div>
        </div>

        <!-- 操作按钮 -->
        <div class="actions">
          <el-button
            v-if="!item.reply"
            size="small"
            type="primary"
            @click="openReply(item)"
          >
            回复
          </el-button>

          <!-- <el-button
            size="small"
            type="danger"
            plain
            @click="reportReview(item)"
          >
            举报
          </el-button> -->
        </div>

      </div>

    </div>

    <!-- 回复弹窗 -->
    <el-dialog v-model="dialogVisible" title="回复评价" width="500px">
      <el-input
        v-model="replyContent"
        type="textarea"
        rows="4"
        placeholder="请输入回复内容"
      />
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitReply">
          提交
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const filter = ref('all')
const loading = ref(false)
const reviews = ref([])

onMounted(() => {
  loadReviews()
})

const loadReviews = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/review/host/reviews')
    console.log('后端返回的评价数据:', res)
    
    reviews.value = res.map(item => {
      console.log('处理评价项:', item)
      return {
        id: item.id,
        username: item.userName || '匿名用户',
        avatar: item.isAnonymous 
          ? 'https://api.dicebear.com/7.x/avataaars/svg?seed=anonymous'
          : (item.userAvatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${item.userId}`),
        rating: item.rating,
        houseName: item.houseTitle || '未命名房源',
        content: item.comment || '',
        reply: item.reply || '',
        createdTime: item.createdTime,
        isAnonymous: item.isAnonymous
      }
    })
    
    console.log('处理后的评价数据:', reviews.value)
  } catch (error) {
    console.error('加载评价失败:', error)
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}

const filteredReviews = computed(() => {
  if (filter.value === 'replied') {
    return reviews.value.filter(r => r.reply)
  }
  if (filter.value === 'pending') {
    return reviews.value.filter(r => !r.reply)
  }
  if (filter.value === 'low') {
    return reviews.value.filter(r => r.rating <= 3)
  }
  return reviews.value
})

const dialogVisible = ref(false)
const currentReview = ref(null)
const replyContent = ref('')

const openReply = (item) => {
  currentReview.value = item
  replyContent.value = ''
  dialogVisible.value = true
}

const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  try {
    await request.put(`/api/review/${currentReview.value.id}/reply`, {
      reply: replyContent.value
    })
    
    // 更新本地数据
    currentReview.value.reply = replyContent.value
    
    ElMessage.success('回复成功')
    dialogVisible.value = false
    replyContent.value = ''
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error(error.response?.data?.message || '回复失败')
  }
}

const reportReview = (item) => {
  ElMessage.success('已提交举报，平台会进行审核')
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  if (days < 365) return `${Math.floor(days / 30)}个月前`
  return `${Math.floor(days / 365)}年前`
}

const handleImageError = (e) => {
  e.target.src = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
}
</script>

<style scoped>
.review-page {
  padding: 30px;
  background: #f7f7f7;
}

.filter-bar {
  margin-bottom: 20px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-card {
  background: #fff;
  padding: 20px;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.avatar {
  width: 45px;
  height: 45px;
  border-radius: 50%;
}

.username {
  font-weight: 600;
}

.house-info {
  text-align: right;
}

.house {
  font-size: 14px;
  color: #222;
  font-weight: 500;
  margin-bottom: 4px;
}

.time {
  font-size: 12px;
  color: #999;
}

.content {
  margin-top: 15px;
  line-height: 1.6;
}

.reply-box {
  margin-top: 15px;
  padding: 12px;
  background: #f2f2f2;
  border-radius: 8px;
}

.reply-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}

.actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}
</style>