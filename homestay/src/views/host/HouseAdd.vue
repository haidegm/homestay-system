<template>
  <el-card>
    <h2 style="margin-bottom: 20px;">
      {{ isEdit ? "编辑房源" : "新增房源" }}
    </h2>

    <el-form :model="form" label-width="120px">
      <el-divider content-position="left">基础信息</el-divider>

      <el-form-item label="房源标题">
        <el-input v-model="form.title"/>
      </el-form-item>

      <el-form-item label="房源简介">
        <el-input
          type="textarea"
          v-model="form.description"
          :rows="4"
          placeholder="介绍一下您的房源，让房客更了解这里的特色..."
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="所在城市">
        <el-cascader
          v-model="form.city"
          :options="cityOptions"
          filterable
          placeholder="请选择城市"
        />
      </el-form-item>

      <el-form-item label="详细地址">
        <el-input v-model="form.address"/>
      </el-form-item>

      <el-form-item label="地图选点">
        <div style="width: 100%;">
          <AMapPicker v-model="location" :city="form.city"/>
        </div>
      </el-form-item>

      <el-form-item label="可住人数">
        <el-input-number v-model="form.maxGuests" :min="1"/>
      </el-form-item>

      <el-form-item label="房间面积">
        <el-input-number v-model="form.area" :min="1"/>
      </el-form-item>

      <el-form-item label="床位数量">
        <el-input-number v-model="form.bedCount" :min="1"/>
      </el-form-item>

      <el-form-item label="每晚价格">
        <el-input-number v-model="form.price" :min="1"/>
      </el-form-item>

      <el-divider content-position="left">设施</el-divider>

      <el-form-item label="基础设施">
        <el-checkbox-group v-model="form.amenities">
          <el-checkbox :label="1">WiFi</el-checkbox>
          <el-checkbox :label="2">空调</el-checkbox>
          <el-checkbox :label="3">地暖</el-checkbox>
          <el-checkbox :label="4">早餐</el-checkbox>
          <el-checkbox :label="5">瓶装水</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="停车">
        <el-radio-group v-model="form.parking">
          <el-radio label="NONE">无</el-radio>
          <el-radio label="FREE">免费</el-radio>
          <el-radio label="PAID">收费</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-divider content-position="left">说明</el-divider>

      <el-form-item label="浴室配套">
        <el-input
          type="textarea"
          v-model="form.bathroomDesc"
        />
      </el-form-item>

      <el-form-item label="设施用品">
        <el-input
          type="textarea"
          v-model="form.supplyDesc"
        />
      </el-form-item>

      <el-divider content-position="left">图片</el-divider>

      <el-form-item label="上传图片">
        <div class="upload-section">
          <div class="image-upload-container">
            <!-- 已上传的图片预览 -->
            <div 
              v-for="(file, index) in fileList" 
              :key="file.uid"
              class="image-preview-item"
            >
              <el-image
                :src="file.url || getFilePreviewUrl(file)"
                :preview-src-list="allImageUrls"
                :initial-index="index"
                fit="cover"
                class="preview-image"
              />
              <div class="image-actions">
                <el-icon class="delete-icon" @click="handleRemoveImage(index)">
                  <Delete />
                </el-icon>
              </div>
              <div v-if="index === 0" class="cover-badge">封面</div>
            </div>
            
            <!-- 上传按钮 -->
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :on-change="handleFileChange"
              :show-file-list="false"
              multiple
              accept="image/*"
              class="upload-trigger"
            >
              <div class="upload-box">
                <el-icon><Plus /></el-icon>
              </div>
            </el-upload>
          </div>
          
          <div class="upload-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>建议上传至少5张图片，第一张将作为封面。</span>
          </div>
        </div>
      </el-form-item>

      <el-form-item style="margin-top:40px">
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? "保存修改" : "发布" }}
        </el-button>
        <el-button @click="router.back()">
          返回
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import request from "../../utils/request"
import { ElMessage } from "element-plus"
import { pcTextArr } from "element-china-area-data"
import { Plus, InfoFilled, Delete } from '@element-plus/icons-vue'
import AMapPicker from "../../components/AMapPicker.vue"

const router = useRouter()
const route = useRoute()

const cityOptions = pcTextArr
const uploadRef = ref()
const fileList = ref([])
const submitting = ref(false)

const location = ref({
  latitude: "",
  longitude: ""
})

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: "",
  description: "",
  city: [],
  address: "",
  maxGuests: 1,
  area: 0,
  bedCount: 1,
  price: 0,
  amenities: [],
  parking: "NONE",
  bathroomDesc: "",
  supplyDesc: ""
})

// 获取所有图片URL用于预览
const allImageUrls = computed(() => {
  return fileList.value.map(file => {
    if (file.url) return file.url
    if (file.raw) return URL.createObjectURL(file.raw)
    return ''
  })
})

// 获取文件预览URL
const getFilePreviewUrl = (file) => {
  if (file.raw) {
    return URL.createObjectURL(file.raw)
  }
  return ''
}

const handleFileChange = (file, files) => {
  // 添加新文件到列表
  fileList.value.push({
    name: file.name,
    raw: file.raw,
    uid: file.uid
  })
}

const handleRemoveImage = (index) => {
  fileList.value.splice(index, 1)
}

/* ======================
   加载房源详情（编辑）
====================== */
onMounted(async () => {
  if (!isEdit.value) return

  try {
    const res = await request.get(`/api/host/house/detail/${route.params.id}`)
    const data = res.data || res

    console.log('加载的房源数据:', data)

    form.title = data.title
    form.description = data.description || ""
    form.city = [data.province, data.city]
    form.address = data.address
    form.maxGuests = data.maxGuests
    form.area = data.area
    form.bedCount = data.bedCount
    form.price = data.price
    form.bathroomDesc = data.bathroomDesc
    form.supplyDesc = data.supplyDesc
    form.parking = data.parking
    form.amenities = data.amenities || []

    location.value.latitude = data.latitude
    location.value.longitude = data.longitude

    // 加载已有图片
    if (data.images && data.images.length) {
      fileList.value = data.images.map((url, index) => ({
        name: `image-${index + 1}.jpg`,
        url: url.startsWith('http') ? url : `http://localhost:8080${url}`,
        uid: Date.now() + index
      }))
    }

  } catch (e) {
    console.error('加载房源失败:', e)
    ElMessage.error("读取房源失败")
  }
})

/* ======================
   提交
====================== */
const submitForm = async () => {
  if (submitting.value) return

  try {
    submitting.value = true

    const payload = {
      title: form.title,
      description: form.description,
      province: form.city[0],
      city: form.city[1],
      address: form.address,
      latitude: location.value.latitude,
      longitude: location.value.longitude,
      maxGuests: form.maxGuests,
      area: form.area,
      bedCount: form.bedCount,
      price: form.price,
      bathroomDesc: form.bathroomDesc,
      supplyDesc: form.supplyDesc,
      parking: form.parking
    }

    console.log('提交的数据:', payload)

    let houseId

    if (isEdit.value) {
      console.log('编辑模式，PUT请求到:', `/api/host/house/${route.params.id}`)
      const res = await request.put(`/api/host/house/${route.params.id}`, payload)
      console.log('编辑响应:', res)
      houseId = route.params.id
    } else {
      console.log('新增模式，POST请求')
      const res = await request.post("/api/host/house", payload)
      console.log('新增响应:', res)
      houseId = res.id || res.data?.id
    }

    // 保存设施
    if (form.amenities.length) {
      await request.post("/api/host/house/amenity", form.amenities, {
        params: { houseId }
      })
    }

    // 上传新图片
    const newFiles = fileList.value.filter(f => f.raw)
    if (newFiles.length) {
      const formData = new FormData()
      newFiles.forEach(f => {
        formData.append("files", f.raw)
      })
      formData.append("houseId", houseId)
      await request.post("/api/host/house/image", formData)
    }

    ElMessage.success(isEdit.value ? "修改成功" : "发布成功")
    router.push("/host/house/list")

  } catch (e) {
    console.error('提交失败:', e)
    ElMessage.error("操作失败: " + (e.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.el-card {
  margin: 20px;
}

.upload-section {
  width: 100%;
}

.image-upload-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.image-preview-item {
  position: relative;
  width: 148px;
  height: 148px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.image-actions {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s;
}

.image-preview-item:hover .image-actions {
  opacity: 1;
}

.delete-icon {
  font-size: 20px;
  color: white;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.3);
}

.delete-icon:hover {
  background: rgba(255, 73, 73, 0.8);
}

.cover-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 2px 8px;
  background: #409eff;
  color: white;
  font-size: 12px;
  border-radius: 3px;
}

.upload-trigger {
  display: inline-block;
}

.upload-box {
  width: 148px;
  height: 148px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s;
}

.upload-box:hover {
  border-color: #409eff;
}

.upload-box .el-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 14px;
}

.upload-tip .el-icon {
  color: #409eff;
  font-size: 16px;
}
</style>