<template>
  <el-dialog
    v-model="visible"
    :title="`与${hostName || '房东'}的对话`"
    width="600px"
    @close="handleClose"
    class="message-dialog"
  >
    <div class="message-container">
      <!-- 消息列表 -->
      <div class="messages-list" ref="messagesList">
        <div v-if="loading" class="loading-state">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <div v-else-if="messages.length === 0" class="empty-state">
          <p>还没有消息，开始对话吧！</p>
        </div>
        <div v-else>
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['message-item', msg.senderId === currentUserId ? 'sent' : 'received']"
          >
            <div class="message-bubble">
              <div class="message-content">{{ msg.content }}</div>
              <div class="message-time">{{ formatTime(msg.createdTime) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="message-input">
        <el-input
          v-model="newMessage"
          type="textarea"
          :rows="3"
          placeholder="输入消息..."
          @keydown.enter.exact.prevent="sendMessage"
        />
        <el-button
          type="primary"
          @click="sendMessage"
          :loading="sending"
          :disabled="!newMessage.trim()"
        >
          发送
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import request from '../utils/request'

const props = defineProps({
  modelValue: Boolean,
  hostId: Number,
  hostName: String,
  houseId: Number,
  orderId: Number
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const messages = ref([])
const newMessage = ref('')
const sending = ref(false)
const loading = ref(false)
const currentUserId = ref(null)
const conversationId = ref(null)
const messagesList = ref(null)

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadConversationAndMessages()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const loadConversationAndMessages = async () => {
  loading.value = true
  try {
    // 获取当前用户ID
    const userStr = localStorage.getItem('user')
    const userId = localStorage.getItem('userId')
    
    if (userStr) {
      const user = JSON.parse(userStr)
      currentUserId.value = user.userId || user.id || parseInt(userId)
    } else if (userId) {
      currentUserId.value = parseInt(userId)
    }

    console.log('当前用户ID:', currentUserId.value)
    console.log('房东ID:', props.hostId)
    console.log('房源ID:', props.houseId)

    if (!currentUserId.value) {
      console.error('无法获取当前用户ID，请重新登录')
      messages.value = []
      loading.value = false
      return
    }

    // 获取会话列表，查找与该房东和房源的会话
    const conversations = await request.get('/api/messages/conversations')
    console.log('会话列表:', conversations)
    
    // 查找与当前房东和房源的会话
    const existingConv = conversations.find(conv => {
      console.log('检查会话:', conv)
      // 如果有houseId，必须匹配房源
      if (props.houseId) {
        return conv.hostId === props.hostId && conv.houseId === props.houseId
      }
      // 没有houseId，只匹配房东
      return conv.hostId === props.hostId
    })

    console.log('找到的会话:', existingConv)

    if (existingConv) {
      conversationId.value = existingConv.id
      // 加载会话消息
      const res = await request.get(`/api/messages/conversation/${existingConv.id}`)
      console.log('消息响应:', res)
      if (res.success) {
        console.log('当前用户ID:', currentUserId.value)
        console.log('原始消息:', res.messages)
        
        messages.value = (res.messages || []).map(msg => {
          const isMine = msg.senderId === currentUserId.value
          console.log(`消息ID ${msg.id}: senderId=${msg.senderId}, currentUserId=${currentUserId.value}, isMine=${isMine}`)
          return msg
        })
        
        console.log('加载的消息:', messages.value)
        nextTick(() => {
          scrollToBottom()
        })
      }
    } else {
      // 没有历史会话，显示空消息列表
      console.log('没有找到历史会话，将创建新会话')
      messages.value = []
      conversationId.value = null
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    messages.value = []
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim()) return

  sending.value = true
  try {
    const res = await request.post('/api/messages/send', {
      receiverId: props.hostId,
      content: newMessage.value,
      houseId: props.houseId,
      orderId: props.orderId
    })

    if (res.success) {
      messages.value.push(res.data)
      newMessage.value = ''
      nextTick(() => {
        scrollToBottom()
      })
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  if (messagesList.value) {
    messagesList.value.scrollTop = messagesList.value.scrollHeight
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.message-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #EBEBEB;
  padding: 20px 24px;
}

.message-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #222;
}

.message-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.message-container {
  display: flex;
  flex-direction: column;
  height: 500px;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #F7F7F7;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #717171;
  gap: 12px;
}

.loading-state .el-icon {
  font-size: 32px;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.sent {
  justify-content: flex-end;
}

.message-item.received {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 70%;
}

.message-item.sent .message-bubble {
  background: #FF385C;
  color: white;
  border-radius: 18px 18px 4px 18px;
  padding: 12px 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-item.received .message-bubble {
  background: white;
  color: #222;
  border-radius: 18px 18px 18px 4px;
  padding: 12px 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
}

.message-content {
  font-size: 15px;
  line-height: 1.5;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message-time {
  font-size: 11px;
  margin-top: 6px;
  opacity: 0.7;
}

.message-input {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  padding: 16px 24px;
  background: white;
  border-top: 1px solid #EBEBEB;
}

.message-input :deep(.el-textarea) {
  flex: 1;
}

.message-input :deep(.el-textarea__inner) {
  border-radius: 8px;
  border-color: #DDDDDD;
  font-size: 14px;
  resize: none;
}

.message-input :deep(.el-textarea__inner):focus {
  border-color: #222;
}

.message-input :deep(.el-button) {
  height: 48px;
  padding: 0 24px;
  border-radius: 8px;
  font-weight: 600;
  background: linear-gradient(to right, #E61E4D, #E31C5F, #D70466);
  border: none;
}

.message-input :deep(.el-button:hover) {
  background: linear-gradient(to right, #D01346, #CA1A5B, #C10366);
}

.message-input :deep(.el-button.is-disabled) {
  background: #DDDDDD;
  color: #ABABAB;
}

.messages-list::-webkit-scrollbar {
  width: 6px;
}

.messages-list::-webkit-scrollbar-track {
  background: transparent;
}

.messages-list::-webkit-scrollbar-thumb {
  background: #DDDDDD;
  border-radius: 3px;
}

.messages-list::-webkit-scrollbar-thumb:hover {
  background: #B0B0B0;
}
</style>
