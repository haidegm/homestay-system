<template>
  <div class="user-messages-page">
    <h2 class="page-title">消息中心</h2>
    <p class="page-subtitle">与房东的对话</p>

    <div class="messages-container">
      <!-- 左侧会话列表 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <el-input
            v-model="search"
            placeholder="搜索房东或房源"
            size="small"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="conversation-list">
          <div v-if="loading" class="loading-state">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="filteredConversations.length === 0" class="empty-state">
            <p>{{ search ? '没有找到相关会话' : '暂无消息' }}</p>
          </div>
          <div
            v-else
            v-for="item in filteredConversations"
            :key="item.id"
            :class="['conversation', activeId === item.id ? 'active' : '']"
            @click="selectConversation(item)"
          >
            <img :src="getAvatarUrl(item.avatar)" class="avatar" @error="handleAvatarError"/>

            <div class="info">
              <div class="top">
                <span class="name">{{ item.username }}</span>
                <span class="time">{{ item.lastTime }}</span>
              </div>
              <div class="bottom">
                <span class="last">{{ item.lastMessage || '暂无消息' }}</span>
                <span
                  v-if="item.unread > 0"
                  class="badge"
                >
                  {{ item.unread }}
                </span>
              </div>
              <div v-if="item.houseId" class="house-tag">
                <img 
                  v-if="item.houseImage" 
                  :src="getHouseImageUrl(item.houseImage)" 
                  class="house-thumb-small"
                  @error="handleImageError"
                />
                <div class="house-tag-text">
                  <el-icon><House /></el-icon>
                  <span>{{ item.houseTitle || `房源 #${item.houseId}` }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧聊天窗口 -->
      <div class="chat" v-if="activeConversation">
        <div class="chat-header">
          <div class="header-content">
            <div class="user-info">
              <img :src="getAvatarUrl(activeConversation.avatar)" class="avatar" @error="handleAvatarError"/>
              <span>{{ activeConversation.username }}</span>
            </div>
            <div v-if="activeConversation.houseId" class="house-info">
              <img 
                v-if="activeConversation.houseImage" 
                :src="getHouseImageUrl(activeConversation.houseImage)" 
                class="house-thumb"
                @error="handleImageError"
              />
              <div class="house-text">
                <el-icon><House /></el-icon>
                <span>{{ activeConversation.houseTitle || `房源 #${activeConversation.houseId}` }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-body" ref="chatBody">
          <div v-if="loadingMessages" class="loading-messages">
            <el-icon class="is-loading"><Loading /></el-icon>
          </div>
          <div v-else-if="activeMessages.length === 0" class="empty-messages">
            <p>还没有消息，开始对话吧！</p>
          </div>
          <div
            v-else
            v-for="msg in activeMessages"
            :key="msg.id"
            :class="['message', msg.sender === 'me' ? 'me' : 'other']"
          >
            <div class="bubble">
              {{ msg.content }}
            </div>
            <div class="msg-time">{{ msg.time }}</div>
          </div>
        </div>

        <div class="chat-footer">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            placeholder="输入消息..."
            @keydown.enter.exact.prevent="sendMessage"
          />
          <el-button 
            type="primary" 
            @click="sendMessage"
            :loading="sending"
            :disabled="!input.trim()"
          >
            发送
          </el-button>
        </div>
      </div>

      <div class="empty" v-else>
        <el-icon><ChatDotRound /></el-icon>
        <p>请选择一个会话</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, inject } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, Search, ChatDotRound, House } from '@element-plus/icons-vue'
import request from '../../../utils/request'

const search = ref('')
const activeId = ref(null)
const input = ref('')
const chatBody = ref(null)
const conversations = ref([])
const currentUserId = ref(null)
const activeMessages = ref([])
const loading = ref(false)
const loadingMessages = ref(false)
const sending = ref(false)

// 注入父组件提供的刷新未读数方法
const refreshUnreadCount = inject('refreshUnreadCount', null)

onMounted(() => {
  loadConversations()
  getCurrentUserId()
})

const getCurrentUserId = () => {
  const userStr = localStorage.getItem('user')
  const userId = localStorage.getItem('userId')
  
  if (userStr) {
    const user = JSON.parse(userStr)
    currentUserId.value = user.userId || user.id || parseInt(userId)
  } else if (userId) {
    currentUserId.value = parseInt(userId)
  }
  
  console.log('用户端当前用户ID:', currentUserId.value)
}

const loadConversations = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/messages/conversations')
    console.log('用户端会话列表原始数据:', res)
    conversations.value = res.map(conv => ({
      id: conv.id,
      username: conv.otherName || '房东',
      avatar: conv.otherAvatar,
      lastMessage: conv.lastMessage || '暂无消息',
      lastTime: formatTime(conv.lastMessageTime),
      unread: conv.unreadCount || 0,
      userId: conv.userId,
      hostId: conv.hostId,
      houseId: conv.houseId,
      houseTitle: conv.houseTitle,
      houseImage: conv.houseImage,
      orderId: conv.orderId
    }))
    console.log('用户端处理后的会话列表:', conversations.value)
  } catch (error) {
    console.error('加载会话列表失败:', error)
    ElMessage.error('加载会话列表失败')
  } finally {
    loading.value = false
  }
}

const filteredConversations = computed(() => {
  return conversations.value.filter(c =>
    c.username.includes(search.value) || 
    (c.houseTitle && c.houseTitle.includes(search.value))
  )
})

const activeConversation = computed(() => {
  return conversations.value.find(c => c.id === activeId.value)
})

const selectConversation = async (item) => {
  activeId.value = item.id
  item.unread = 0
  
  loadingMessages.value = true
  try {
    const res = await request.get(`/api/messages/conversation/${item.id}`)
    if (res.success) {
      activeMessages.value = res.messages.map(msg => {
        const isMine = msg.senderId === currentUserId.value
        return {
          id: msg.id,
          sender: isMine ? 'me' : 'other',
          content: msg.content,
          time: formatTime(msg.createdTime)
        }
      })
      
      scrollBottom()
      
      // 延迟刷新未读数，等待后端标记消息为已读
      setTimeout(() => {
        if (refreshUnreadCount) {
          refreshUnreadCount()
        }
      }, 500)
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  } finally {
    loadingMessages.value = false
  }
}

const sendMessage = async () => {
  if (!input.value.trim() || !activeConversation.value) return

  sending.value = true
  try {
    const receiverId = activeConversation.value.userId === currentUserId.value 
      ? activeConversation.value.hostId 
      : activeConversation.value.userId

    const res = await request.post('/api/messages/send', {
      receiverId: receiverId,
      content: input.value,
      houseId: activeConversation.value.houseId,
      orderId: activeConversation.value.orderId
    })

    if (res.success) {
      const newMsg = {
        id: res.data.id,
        sender: 'me',
        content: input.value,
        time: formatTime(new Date())
      }

      activeMessages.value.push(newMsg)
      activeConversation.value.lastMessage = input.value
      activeConversation.value.lastTime = newMsg.time

      input.value = ''
      scrollBottom()
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

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 86400000) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (diff < 172800000) {
    return '昨天'
  } else {
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  }
}

const getAvatarUrl = (avatar) => {
  if (!avatar) {
    return 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
  }
  if (avatar.startsWith('http')) {
    return avatar
  }
  return `http://localhost:8080${avatar}`
}

const getHouseImageUrl = (image) => {
  if (!image) {
    return 'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=400'
  }
  if (image.startsWith('http')) {
    return image
  }
  return `http://localhost:8080${image}`
}

const handleAvatarError = (e) => {
  e.target.src = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
}

const handleImageError = (e) => {
  e.target.src = 'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=400'
}

const scrollBottom = () => {
  nextTick(() => {
    if (chatBody.value) {
      chatBody.value.scrollTop = chatBody.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.user-messages-page {
  padding: 0;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #222;
}

.page-subtitle {
  font-size: 16px;
  color: #717171;
  margin: 0 0 32px 0;
}

.messages-container {
  display: flex;
  height: 70vh;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebebeb;
}

.sidebar {
  width: 360px;
  background: #fff;
  border-right: 1px solid #EBEBEB;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #EBEBEB;
}

.sidebar-header :deep(.el-input__wrapper) {
  border-radius: 24px;
  box-shadow: 0 0 0 1px #DDDDDD inset;
}

.sidebar-header :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #222 inset;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
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

.conversation {
  display: flex;
  padding: 16px 20px;
  cursor: pointer;
  gap: 12px;
  transition: background 0.2s;
  border-bottom: 1px solid #F7F7F7;
}

.conversation:hover {
  background: #F7F7F7;
}

.conversation.active {
  background: #F0F0F0;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.name {
  font-size: 15px;
  font-weight: 600;
  color: #222;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  font-size: 12px;
  color: #717171;
  flex-shrink: 0;
  margin-left: 8px;
}

.bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.last {
  font-size: 14px;
  color: #717171;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.badge {
  background: #FF385C;
  color: #fff;
  border-radius: 12px;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.house-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
}

.house-thumb-small {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  object-fit: cover;
}

.house-tag-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #717171;
}

.house-tag-text .el-icon {
  font-size: 12px;
}

.chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.chat-header {
  padding: 20px 24px;
  border-bottom: 1px solid #EBEBEB;
  background: white;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info .avatar {
  width: 40px;
  height: 40px;
}

.user-info span {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}

.house-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 52px;
}

.house-thumb {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
}

.house-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #717171;
}

.house-text .el-icon {
  font-size: 14px;
}

.chat-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #FAFAFA;
}

.loading-messages,
.empty-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #717171;
  gap: 12px;
}

.loading-messages .el-icon {
  font-size: 32px;
}

.empty-messages p {
  margin: 0;
  font-size: 14px;
}

.message {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
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

.message.me {
  align-items: flex-end;
}

.bubble {
  max-width: 60%;
  padding: 12px 16px;
  border-radius: 18px;
  word-wrap: break-word;
  white-space: pre-wrap;
  font-size: 15px;
  line-height: 1.5;
}

.message.me .bubble {
  background: #FF385C;
  color: white;
  border-radius: 18px 18px 4px 18px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message.other .bubble {
  background: white;
  color: #222;
  border-radius: 18px 18px 18px 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
}

.msg-time {
  font-size: 11px;
  color: #717171;
  margin-top: 6px;
}

.chat-footer {
  padding: 20px 24px;
  border-top: 1px solid #EBEBEB;
  display: flex;
  gap: 12px;
  background: white;
}

.chat-footer :deep(.el-textarea) {
  flex: 1;
}

.chat-footer :deep(.el-textarea__inner) {
  border-radius: 8px;
  border-color: #DDDDDD;
  font-size: 14px;
  resize: none;
}

.chat-footer :deep(.el-textarea__inner):focus {
  border-color: #222;
}

.chat-footer :deep(.el-button) {
  height: 48px;
  padding: 0 24px;
  border-radius: 8px;
  font-weight: 600;
  background: linear-gradient(to right, #E61E4D, #E31C5F, #D70466);
  border: none;
}

.chat-footer :deep(.el-button:hover) {
  background: linear-gradient(to right, #D01346, #CA1A5B, #C10366);
}

.chat-footer :deep(.el-button.is-disabled) {
  background: #DDDDDD;
  color: #ABABAB;
}

.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #717171;
  gap: 16px;
  background: white;
}

.empty .el-icon {
  font-size: 64px;
  color: #DDDDDD;
}

.empty p {
  margin: 0;
  font-size: 16px;
}

.conversation-list::-webkit-scrollbar,
.chat-body::-webkit-scrollbar {
  width: 6px;
}

.conversation-list::-webkit-scrollbar-track,
.chat-body::-webkit-scrollbar-track {
  background: transparent;
}

.conversation-list::-webkit-scrollbar-thumb,
.chat-body::-webkit-scrollbar-thumb {
  background: #DDDDDD;
  border-radius: 3px;
}

.conversation-list::-webkit-scrollbar-thumb:hover,
.chat-body::-webkit-scrollbar-thumb:hover {
  background: #B0B0B0;
}
</style>
