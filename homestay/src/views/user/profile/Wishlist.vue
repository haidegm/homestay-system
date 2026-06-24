<template>
  <div class="wishlist-section">
    <h2 class="section-title">心愿单</h2>
    
    <div v-loading="loading">
      <el-empty 
        v-if="!loading && wishlist.length === 0" 
        description="这里空空如也，去收藏心仪的房源吧"
        :image-size="120"
      >
        <el-button type="primary" @click="$router.push('/')">
          探索房源
        </el-button>
      </el-empty>
      
      <el-row :gutter="20" v-else>
        <el-col :xs="24" :sm="12" :md="12" :lg="8" v-for="item in wishlist" :key="item.id">
          <div class="wish-card" @click="goToDetail(item.houseId)">
            <div class="wish-image-wrapper">
              <img :src="item.houseCoverImage" class="wish-img" @error="handleImageError">
              <el-icon class="remove-icon" @click.stop="removeFromWishlist(item)">
                <Close />
              </el-icon>
            </div>
            <div class="wish-meta">
              <div class="location">
                <span class="city">{{ item.city }}, {{ item.province }}</span>
                <div class="rating" v-if="item.rating">
                  <el-icon><StarFilled /></el-icon>
                  <span>{{ item.rating.toFixed(1) }}</span>
                </div>
              </div>
              <span class="title">{{ item.houseTitle }}</span>
              <span class="price">¥{{ item.price }} / 晚</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Close, StarFilled } from '@element-plus/icons-vue'
import request from '../../../utils/request'

const router = useRouter()
const wishlist = ref([])
const loading = ref(false)

onMounted(() => {
  loadWishlist()
})

const loadWishlist = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/wishlist/my')
    wishlist.value = res
    console.log('心愿单:', wishlist.value)
  } catch (error) {
    console.error('加载心愿单失败:', error)
    ElMessage.error('加载心愿单失败')
  } finally {
    loading.value = false
  }
}

const removeFromWishlist = async (item) => {
  try {
    await request.delete(`/api/wishlist/remove/${item.houseId}`)
    ElMessage.success('已从心愿单移除')
    // 从列表中移除
    wishlist.value = wishlist.value.filter(w => w.id !== item.id)
  } catch (error) {
    console.error('移除失败:', error)
    ElMessage.error('移除失败')
  }
}

const goToDetail = (houseId) => {
  router.push(`/house/${houseId}`)
}

const handleImageError = (e) => {
  e.target.src = 'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=400'
}
</script>

<style scoped>
.wishlist-section {
  padding: 24px;
}

.section-title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 32px;
  color: #222;
}

.wish-card {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.wish-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.wish-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 66.67%; /* 3:2 比例 */
  overflow: hidden;
}

.wish-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-icon {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 10;
}

.remove-icon:hover {
  background: white;
  transform: scale(1.1);
}

.wish-meta {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.location {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.city {
  font-size: 14px;
  color: #717171;
  font-weight: 400;
}

.rating {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
}

.rating .el-icon {
  color: #FF385C;
  font-size: 12px;
}

.title {
  font-weight: 400;
  font-size: 15px;
  color: #222;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  font-weight: 600;
  font-size: 15px;
  color: #222;
  margin-top: 4px;
}
</style>