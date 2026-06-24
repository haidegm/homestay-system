<template>
  <div class="sticky-search" :class="{ 'expanded': isExpanded }">
    <LocationSearch
      v-model="searchForm.location"
      placeholder="目的地"
      :size="inputSize"
      @select="handleLocationSelect"
    />
    <el-date-picker
      v-model="searchForm.dateRange"
      :size="inputSize"
      type="daterange"
      start-placeholder="入住"
      end-placeholder="退房"
      :disabled-date="disabledDate"
    />
    <el-button type="primary" :size="inputSize" @click="handleSearch">搜索</el-button>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import LocationSearch from './LocationSearch.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const isExpanded = ref(false)
const inputSize = ref('small')
const selectedLocation = ref(null)

const searchForm = reactive({
  location: '',
  dateRange: []
})

const updateExpansionStatus = () => {
  isExpanded.value = window.scrollY > 50
}

const handleLocationSelect = (locationData) => {
  selectedLocation.value = locationData
  searchForm.location = locationData.name
}

// 禁用过去的日期
const disabledDate = (time) => {
  // 禁用今天之前的日期
  return time.getTime() < Date.now() - 8.64e7
}

const handleSearch = () => {
  if (!selectedLocation.value) {
    ElMessage.warning('请选择搜索位置')
    return
  }
  
  // 验证日期范围
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    const checkIn = new Date(searchForm.dateRange[0])
    const checkOut = new Date(searchForm.dateRange[1])
    const diffTime = checkOut.getTime() - checkIn.getTime()
    const diffDays = diffTime / (1000 * 60 * 60 * 24)
    
    if (diffDays < 1) {
      ElMessage.warning('退房日期必须晚于入住日期至少一天')
      return
    }
  }
  
  // 格式化日期为 YYYY-MM-DD
  let formattedDateRange = []
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    formattedDateRange = searchForm.dateRange.map(date => {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    })
  }
  
  const searchParams = {
    location: selectedLocation.value.name,
    lat: selectedLocation.value.latitude,
    lng: selectedLocation.value.longitude,
    dateRange: JSON.stringify(formattedDateRange),
    guests: 1 // 默认1人
  }
  
  router.push({
    path: '/search',
    query: searchParams
  })
}
onMounted(() => {
  window.addEventListener('scroll', updateExpansionStatus, { passive: true })
  updateExpansionStatus()
})
onUnmounted(() => {
  window.removeEventListener('scroll', updateExpansionStatus)
})
</script>

<style scoped>
.sticky-search {
  display: flex;
  gap: 10px;
  background: #fff;
  padding: 6px 12px;
  border-radius: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}
.sticky-search button{
  margin-top: 5px;
}
.sticky-search.expanded {
  padding: 12px 24px;
  border-radius: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transform: scale(1.1);
}

/* 为小尺寸输入框设置默认样式 */
.sticky-search :deep(.el-input__wrapper),
.sticky-search :deep(.el-date-editor) {
  height: 32px;
  min-height: 32px;
}

/* 为大尺寸输入框设置扩展样式 */
.sticky-search.expanded :deep(.el-input__wrapper),
.sticky-search.expanded :deep(.el-date-editor) {
  height: 40px;
  min-height: 40px;
}

/* 按钮尺寸调整 */
.sticky-search :deep(.el-button--small) {
  height: 32px;
  padding: 8px 15px;
}

.sticky-search.expanded :deep(.el-button--small) {
  height: 40px;
  padding: 12px 20px;
}

/* 让清空按钮使用绝对定位，不挤压输入框宽度 */
.sticky-search :deep(.el-input__suffix) {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
}

.sticky-search :deep(.el-input__suffix-inner) {
  display: flex;
  align-items: center;
}

/* 为输入框内容留出右侧空间 */
.sticky-search :deep(.el-input__inner) {
  padding-right: 30px;
}

/* 日期选择器的清空按钮 */
.sticky-search :deep(.el-date-editor .el-input__suffix) {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
}

.sticky-search :deep(.el-date-editor .el-input__inner) {
  padding-right: 30px;
}
</style>
