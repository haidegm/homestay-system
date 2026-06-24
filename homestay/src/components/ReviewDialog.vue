<template>
  <el-dialog
    v-model="visible"
    title="评价房源"
    width="600px"
    @close="handleClose"
  >
    <div class="review-dialog">
      <!-- 房源信息 -->
      <div class="house-info" v-if="orderInfo">
        <el-image
          :src="orderInfo.houseCoverImage"
          fit="cover"
          class="house-image"
        />
        <div class="house-details">
          <h3>{{ orderInfo.houseTitle }}</h3>
          <p>入住时间：{{ orderInfo.checkInDate }} - {{ orderInfo.checkOutDate }}</p>
        </div>
      </div>

      <!-- 评分 -->
      <div class="rating-section">
        <label class="section-label">整体评分</label>
        <el-rate
          v-model="form.rating"
          :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          size="large"
          show-text
          :texts="['非常差', '差', '一般', '满意', '非常满意']"
          allow-half
          :max="5"
        />
        <div class="rating-value">{{ form.rating }} 分</div>
      </div>

      <!-- 评论内容 -->
      <div class="comment-section">
        <label class="section-label">分享您的体验</label>
        <el-input
          v-model="form.comment"
          type="textarea"
          :rows="6"
          placeholder="告诉大家您的入住体验吧..."
          maxlength="500"
          show-word-limit
        />
      </div>

      <!-- 上传图片 -->
      <div class="upload-section">
        <label class="section-label">上传照片（最多9张）</label>
        <el-upload
          :action="uploadUrl"
          list-type="picture-card"
          :on-success="handleUploadSuccess"
          :on-remove="handleRemove"
          :before-upload="beforeUpload"
          :file-list="fileList"
          :limit="9"
          accept="image/*"
          :headers="uploadHeaders"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <div class="upload-tip">支持jpg、png格式，单张图片不超过5MB</div>
      </div>

      <!-- 匿名选项 -->
      <div class="anonymous-section">
        <el-checkbox v-model="form.isAnonymous">
          匿名评价
        </el-checkbox>
        <span class="anonymous-tip">（勾选后，房东将看不到您的昵称和头像）</span>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        提交评价
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../utils/request'

const props = defineProps({
  modelValue: Boolean,
  orderInfo: Object
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const submitting = ref(false)
const fileList = ref([])
const uploadedImages = ref([])

const uploadUrl = computed(() => 'http://localhost:8080/api/review/image/upload')
const uploadHeaders = computed(() => ({
  'Authorization': 'Bearer ' + localStorage.getItem('token')
}))

const form = ref({
  rating: 5,
  comment: '',
  isAnonymous: false
})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    // 重置表单
    form.value = {
      rating: 5,
      comment: '',
      isAnonymous: false
    }
    fileList.value = []
    uploadedImages.value = []
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response, file) => {
  if (response.success) {
    uploadedImages.value.push(response.url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

const handleRemove = (file) => {
  // 从已上传列表中移除
  const index = fileList.value.findIndex(f => f.uid === file.uid)
  if (index > -1 && uploadedImages.value[index]) {
    uploadedImages.value.splice(index, 1)
  }
}

const handleClose = () => {
  visible.value = false
}

const handleSubmit = async () => {
  if (!form.value.rating) {
    ElMessage.warning('请选择评分')
    return
  }

  if (!form.value.comment || form.value.comment.trim() === '') {
    ElMessage.warning('请填写评价内容')
    return
  }

  submitting.value = true

  try {
    await request.post('/api/review', {
      orderId: props.orderInfo.id,
      rating: form.value.rating,
      comment: form.value.comment,
      isAnonymous: form.value.isAnonymous,
      images: uploadedImages.value.join(',') // 用逗号分隔多张图片
    })

    ElMessage.success('评价成功')
    emit('success')
    handleClose()
  } catch (error) {
    console.error('评价失败:', error)
    ElMessage.error(error.response?.data?.message || '评价失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.review-dialog {
  padding: 20px 0;
}

.house-info {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f7f7f7;
  border-radius: 12px;
  margin-bottom: 24px;
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
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #222;
}

.house-details p {
  font-size: 14px;
  color: #717171;
  margin: 0;
}

.rating-section,
.comment-section {
  margin-bottom: 24px;
}

.section-label {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #222;
  margin-bottom: 12px;
}

.rating-section :deep(.el-rate) {
  height: 40px;
}

.rating-section :deep(.el-rate__icon) {
  font-size: 32px;
}

.rating-section :deep(.el-rate__text) {
  font-size: 14px;
  color: #717171;
  margin-left: 12px;
}

.rating-value {
  margin-top: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #FF9900;
}

.comment-section :deep(.el-textarea__inner) {
  border-radius: 8px;
  font-size: 14px;
}

.anonymous-section {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f7f7f7;
  border-radius: 8px;
}

.anonymous-tip {
  font-size: 13px;
  color: #717171;
}

.upload-section {
  margin-bottom: 24px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  border-radius: 8px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
  border-radius: 8px;
}
</style>
