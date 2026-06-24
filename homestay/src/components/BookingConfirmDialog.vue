<template>
  <el-dialog
    v-model="visible"
    width="600px"
    :show-close="false"
    class="booking-confirm-dialog"
    @close="handleClose"
  >
    <template #header>
      <div class="dialog-header">
        <el-icon class="close-btn" @click="handleClose"><Close /></el-icon>
      </div>
    </template>

    <div class="confirm-body">
      <h2 class="confirm-title">确认并预订</h2>
      
      <!-- 行程信息 -->
      <div class="trip-section">
        <h3>您的行程</h3>
        <div class="trip-row">
          <div class="trip-label">日期</div>
          <div class="trip-value">
            {{ bookingData.checkIn }} - {{ bookingData.checkOut }}
          </div>
        </div>
        <div class="trip-row">
          <div class="trip-label">房客</div>
          <div class="trip-value">{{ bookingData.guests }} 位房客</div>
        </div>
      </div>

      <el-divider />

      <!-- 价格明细 -->
      <div class="price-section">
        <h3>价格明细</h3>
        <div class="price-row">
          <span>¥{{ bookingData.pricePerNight }} × {{ bookingData.nights }}晚</span>
          <span>¥{{ bookingData.subtotal }}</span>
        </div>
        <div class="price-row">
          <span>清洁费</span>
          <span>¥{{ bookingData.cleaningFee }}</span>
        </div>
        <div class="price-row">
          <span>服务费</span>
          <span>¥{{ bookingData.serviceFee }}</span>
        </div>
      </div>

      <el-divider />

      <!-- 总价 -->
      <div class="total-section">
        <div class="total-row">
          <span class="total-label">总计（CNY）</span>
          <span class="total-amount">¥{{ bookingData.totalCost }}</span>
        </div>
      </div>

      <!-- 预订说明 -->
      <div class="booking-notice">
        <p>点击下方按钮即表示我同意《房屋守则》、《退订政策》</p>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button 
          v-if="showLaterOption"
          size="large"
          class="later-btn"
          @click="handleClose"
        >
          稍后支付
        </el-button>
        <el-button 
          type="primary" 
          size="large"
          class="confirm-btn"
          :class="{ 'full-width': !showLaterOption }"
          :loading="loading" 
          @click="handleConfirm"
        >
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Close } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  bookingData: {
    type: Object,
    default: () => ({
      checkIn: '',
      checkOut: '',
      guests: 1,
      pricePerNight: 0,
      nights: 0,
      subtotal: 0,
      cleaningFee: 0,
      serviceFee: 0,
      totalCost: 0
    })
  },
  loading: {
    type: Boolean,
    default: false
  },
  // 新增：是否显示"稍后支付"选项
  showLaterOption: {
    type: Boolean,
    default: false
  },
  // 新增：按钮文字
  confirmText: {
    type: String,
    default: '确认预订'
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'close'])

const visible = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
  emit('close')
}

const handleConfirm = () => {
  emit('confirm')
}
</script>

<style scoped>
/* 确认预订对话框样式 */
:deep(.booking-confirm-dialog) {
  border-radius: 12px;
}

:deep(.booking-confirm-dialog .el-dialog__header) {
  padding: 24px 24px 0;
  margin: 0;
}

:deep(.booking-confirm-dialog .el-dialog__body) {
  padding: 0 24px 24px;
}

:deep(.booking-confirm-dialog .el-dialog__footer) {
  padding: 0 24px 24px;
}

.dialog-header {
  display: flex;
  justify-content: flex-start;
}

.close-btn {
  font-size: 16px;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background 0.2s;
}

.close-btn:hover {
  background: #f7f7f7;
}

.confirm-body {
  padding-top: 16px;
}

.confirm-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 24px 0;
  color: #222;
}

.trip-section h3,
.price-section h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #222;
}

.trip-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.trip-label {
  color: #717171;
}

.trip-value {
  color: #222;
  font-weight: 500;
}

.price-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 16px;
  color: #222;
}

.total-section {
  margin-top: 16px;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-label {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}

.total-amount {
  font-size: 18px;
  font-weight: 600;
  color: #222;
}

.booking-notice {
  margin-top: 24px;
  padding: 16px;
  background: #f7f7f7;
  border-radius: 8px;
}

.booking-notice p {
  font-size: 12px;
  line-height: 18px;
  color: #717171;
  margin: 0;
}

.confirm-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(to right, #E61E4D, #E31C5F, #D70466);
  border: none;
  flex: 1;
}

.confirm-btn.full-width {
  width: 100%;
}

.confirm-btn:hover {
  background: linear-gradient(to right, #D01346, #CA1A5B, #C10366);
}

.dialog-footer {
  display: flex;
  gap: 12px;
  width: 100%;
}

.later-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  flex: 1;
  border: 1px solid #222;
  color: #222;
}

.later-btn:hover {
  background: #f7f7f7;
}
</style>
