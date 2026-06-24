<template>
  <div class="promo-page">

    <h2 class="page-title">促销活动</h2>

    <!-- 平台活动 -->
    <div class="section">
      <h3>平台推荐活动</h3>

      <div class="official-list">
        <div
          class="official-card"
          v-for="item in officialPromotions"
          :key="item.id"
        >
          <div>
            <div class="official-title">{{ item.title }}</div>
            <div class="official-desc">{{ item.description }}</div>
            <div class="official-discount">{{ item.discount }}</div>
          </div>

          <el-button
            type="primary"
            size="small"
            @click="joinOfficial(item)"
          >
            参加活动
          </el-button>
        </div>
      </div>
    </div>

    <!-- 我的活动 -->
    <div class="section">
      <div class="my-header">
        <h3>我的促销活动</h3>
        <el-button type="primary" @click="openCreate">
          新建活动
        </el-button>
      </div>

      <div v-if="myPromotions.length === 0" class="empty">
        暂无活动
      </div>

      <div class="my-list">
        <div
          class="my-card"
          v-for="item in myPromotions"
          :key="item.id"
        >
          <div class="left">
            <div class="title">{{ item.title }}</div>
            <div class="house">房源：{{ item.houseName }}</div>
            <div class="date">
              {{ item.startDate }} - {{ item.endDate }}
            </div>
            <div class="status" :class="getStatus(item)">
              {{ getStatusText(item) }}
            </div>
          </div>

          <div class="right">
            <div class="discount">{{ item.discount }} 折</div>

            <div class="actions">
              <el-button size="small" @click="editPromotion(item)">
                修改
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="deletePromotion(item.id)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建 / 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="促销活动" width="500px">
      <el-form :model="form">

        <el-form-item label="活动名称">
          <el-input v-model="form.title" />
        </el-form-item>

        <el-form-item label="适用房源">
          <el-select v-model="form.houseId">
            <el-option
              v-for="house in houseOptions"
              :key="house.id"
              :label="house.name"
              :value="house.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="折扣">
          <el-input-number
            v-model="form.discount"
            :min="1"
            :max="10"
          />
          <span style="margin-left:5px">折</span>
        </el-form-item>

        <el-form-item label="时间">
          <el-date-picker
            v-model="form.dates"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>

      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePromotion">
          保存
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref } from 'vue'

const today = new Date().toISOString().split('T')[0]

/* 官方活动 */
const officialPromotions = ref([
  {
    id: 1,
    title: '新房上线 7 天 8 折',
    description: '帮助新房源快速获得订单',
    discount: 8
  },
  {
    id: 2,
    title: '连续入住 7 晚 85 折',
    description: '鼓励长租',
    discount: 8.5
  }
])

/* 我的活动 */
const myPromotions = ref([
  {
    id: 1001,
    title: '春季特惠',
    houseName: '市中心海景公寓',
    startDate: '2026-03-01',
    endDate: '2026-03-31',
    discount: 9
  },
  {
    id: 1002,
    title: '清明小长假优惠',
    houseName: '北欧风两居室',
    startDate: '2026-04-03',
    endDate: '2026-04-06',
    discount: 8.5
  }
])

/* 房源假数据 */
const houseOptions = ref([
  { id: 1, name: '市中心海景公寓' },
  { id: 2, name: '北欧风两居室' }
])

/* 弹窗 */
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = ref({
  id: null,
  title: '',
  houseId: '',
  discount: 9,
  dates: []
})

/* 参加官方活动 */
const joinOfficial = (item) => {
  myPromotions.value.push({
    id: Date.now(),
    title: item.title,
    houseName: houseOptions.value[0].name,
    startDate: today,
    endDate: '2026-12-31',
    discount: item.discount
  })
}

/* 新建 */
const openCreate = () => {
  isEdit.value = false
  form.value = {
    id: null,
    title: '',
    houseId: '',
    discount: 9,
    dates: []
  }
  dialogVisible.value = true
}

/* 保存 */
const savePromotion = () => {

  const house = houseOptions.value.find(h => h.id === form.value.houseId)

  if (isEdit.value) {
    const index = myPromotions.value.findIndex(p => p.id === form.value.id)
    myPromotions.value[index] = {
      id: form.value.id,
      title: form.value.title,
      houseName: house.name,
      startDate: form.value.dates[0],
      endDate: form.value.dates[1],
      discount: form.value.discount
    }
  } else {
    myPromotions.value.push({
      id: Date.now(),
      title: form.value.title,
      houseName: house.name,
      startDate: form.value.dates[0],
      endDate: form.value.dates[1],
      discount: form.value.discount
    })
  }

  dialogVisible.value = false
}

/* 编辑 */
const editPromotion = (item) => {
  isEdit.value = true
  form.value = {
    id: item.id,
    title: item.title,
    houseId: houseOptions.value[0].id,
    discount: item.discount,
    dates: [item.startDate, item.endDate]
  }
  dialogVisible.value = true
}

/* 删除 */
const deletePromotion = (id) => {
  myPromotions.value = myPromotions.value.filter(p => p.id !== id)
}

/* 状态判断 */
const getStatus = (item) => {
  if (today < item.startDate) return 'pending'
  if (today > item.endDate) return 'ended'
  return 'active'
}

const getStatusText = (item) => {
  if (today < item.startDate) return '未开始'
  if (today > item.endDate) return '已结束'
  return '进行中'
}
</script>

<style scoped>
.promo-page {
  padding: 30px;
  background: #f7f7f7;
}

.section {
  margin-bottom: 40px;
}

.official-card,
.my-card {
  background: #fff;
  padding: 20px;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.official-list,
.my-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.official-title,
.title {
  font-size: 16px;
  font-weight: 600;
}

.official-discount,
.discount {
  color: #ff385c;
  font-weight: bold;
}

.actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.status {
  margin-top: 6px;
  font-size: 13px;
}

.status.active { color: #28a745; }
.status.pending { color: #f0ad4e; }
.status.ended { color: #dc3545; }

.empty {
  margin-top: 15px;
  color: #999;
}
</style>