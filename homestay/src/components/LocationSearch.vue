<template>
  <div class="location-search">
    <el-input
      ref="inputRef"
      v-model="searchText"
      :placeholder="placeholder"
      @input="handleInput"
      @focus="handleFocus"
      @blur="handleBlur"
      :size="size"
      clearable
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>
    
    <!-- 搜索建议列表 -->
    <div v-if="showSuggestions && suggestions.length > 0" class="suggestions-panel">
      <div
        v-for="(item, index) in suggestions"
        :key="index"
        class="suggestion-item"
        @mousedown.prevent="selectLocation(item)"
      >
        <el-icon class="location-icon"><Location /></el-icon>
        <div class="suggestion-content">
          <div class="suggestion-name">{{ item.name }}</div>
          <div class="suggestion-address">{{ item.district }}</div>
        </div>
      </div>
    </div>
    
    <!-- 无结果提示 -->
    <div v-if="showSuggestions && searchText && suggestions.length === 0 && !searching" class="suggestions-panel">
      <div class="no-result">未找到相关地点</div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted } from 'vue'
import { Search, Location } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '输入城市、地标或位置'
  },
  size: {
    type: String,
    default: 'default'
  }
})

const emit = defineEmits(['update:modelValue', 'select'])

const inputRef = ref(null)
const searchText = ref(props.modelValue)
const suggestions = ref([])
const showSuggestions = ref(false)
const searching = ref(false)
const autoComplete = ref(null)
const useAmapApi = ref(false)


// 监听输入值变化
watch(() => props.modelValue, (newVal) => {
  searchText.value = newVal
})

watch(searchText, (newVal) => {
  emit('update:modelValue', newVal)
})

// 初始化高德地图自动完成
const initAutoComplete = () => {
  return new Promise((resolve, reject) => {
    if (!window.AMap) {
//      console.log('高德地图 API 未加载，使用模拟数据')
      useAmapApi.value = false
      resolve()
      return
    }
    
    try {
      autoComplete.value = new AMap.AutoComplete({
        city: '全国',
        citylimit: false
      })
      useAmapApi.value = true
//      console.log('高德地图自动完成初始化成功')
      resolve()
    } catch (error) {
//      console.error('初始化高德地图自动完成失败:', error)
      useAmapApi.value = false
      resolve() // 即使失败也继续，使用模拟数据
    }
  })
}

// 组件挂载时初始化
onMounted(async () => {
  await initAutoComplete()
})

// 模拟搜索
const mockSearch = (keyword) => {
  const filtered = mockCities.filter(city => 
    city.name.includes(keyword) || city.district.includes(keyword)
  )
  return filtered.slice(0, 8)
}

// 处理输入
const handleInput = async (value) => {
  if (!value || !value.trim()) {
    suggestions.value = []
    return
  }
  
  searching.value = true
  
  if (useAmapApi.value && autoComplete.value) {
    // 使用高德地图 API
    autoComplete.value.search(value, (status, result) => {
      searching.value = false
      
      if (status === 'complete' && result.tips) {
        suggestions.value = result.tips
          .filter(tip => tip.location) // 只保留有坐标的结果
          .slice(0, 8)
          .map(tip => ({
            name: tip.name,
            district: tip.district,
            location: tip.location,
            adcode: tip.adcode
          }))
        
        console.log('高德搜索结果:', suggestions.value)
      } else {
        console.log('高德搜索失败，使用模拟数据. 状态:', status, result)
        suggestions.value = mockSearch(value)
      }
    })
  } else {
    // 使用模拟数据
    setTimeout(() => {
      suggestions.value = mockSearch(value)
      searching.value = false
      console.log('模拟搜索结果:', suggestions.value)
    }, 300)
  }
}

// 获取焦点
const handleFocus = () => {
  showSuggestions.value = true
  // 如果有输入内容，重新搜索
  if (searchText.value && searchText.value.trim()) {
    handleInput(searchText.value)
  }
}

// 选择位置
const selectLocation = (item) => {
  searchText.value = item.name
  showSuggestions.value = false
  
  let lng, lat
  
  if (useAmapApi.value && item.location) {
    // 高德地图数据
    if (typeof item.location === 'string') {
      [lng, lat] = item.location.split(',').map(Number)
    } else if (item.location.lng && item.location.lat) {
      lng = item.location.lng
      lat = item.location.lat
    }
  } else {
    // 模拟数据
    lng = item.longitude
    lat = item.latitude
  }
  
  if (lng && lat) {
    emit('select', {
      name: item.name,
      district: item.district,
      latitude: lat,
      longitude: lng,
      adcode: item.adcode
    })
    console.log('选中位置:', item.name, lng, lat)
  }
}

// 失去焦点
const handleBlur = () => {
  // 延迟隐藏，让点击事件先执行
  setTimeout(() => {
    showSuggestions.value = false
  }, 300)
}

// 暴露方法给父组件
defineExpose({
  focus: () => {
    nextTick(() => {
      if (inputRef.value) {
        inputRef.value.focus()
      }
    })
  }
})
</script>

<style scoped>
.location-search {
  position: relative;
  width: 100%;
}

.suggestions-panel {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 2000;
  max-height: 300px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f5f5f5;
}

.suggestion-item:hover {
  background-color: #f5f7fa;
}

.suggestion-item:last-child {
  border-bottom: none;
}

.location-icon {
  color: #909399;
  margin-right: 12px;
  font-size: 16px;
  flex-shrink: 0;
}

.suggestion-content {
  flex: 1;
  min-width: 0;
}

.suggestion-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.suggestion-address {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.no-result {
  padding: 16px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}
</style>