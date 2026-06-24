<template>
  <div class="ai-chatbot">
    <!-- 聊天按钮 - 使用绝对定位保持固定位置 -->
    <div 
      v-show="!isOpen" 
      class="chat-button"
      :class="{ 'hiding': isOpen }"
      @click="toggleChat"
    >
      <span class="icon">💬</span>
      <span class="badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
    </div>

    <!-- 聊天窗口 - 使用绝对定位保持固定位置 -->
    <div 
      v-show="isOpen" 
      class="chat-window"
      :class="{ 'showing': isOpen }"
    >
        <!-- 头部 -->
        <div class="chat-header">
          <div class="header-left">
            <div class="avatar">🤖</div>
            <div class="info">
              <div class="name">AI助手</div>
              <div class="status">
                <span class="dot"></span>
                在线
              </div>
            </div>
          </div>
          <div class="header-actions">
            <button class="action-btn" @click="toggleChat">
              <span>✕</span>
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="chat-messages" ref="messagesContainer">
          <div class="welcome-message">
            <div class="avatar">🤖</div>
            <div class="message-content">
              <p>您好！我是AI助手，很高兴为您服务。</p>
              <p>我可以帮您：</p>
              <ul>
                <li>查找合适的房源</li>
                <li>了解预订流程</li>
                <li>解答常见问题</li>
                <li>提供旅行建议</li>
              </ul>
            </div>
          </div>

          <div 
            v-for="(msg, index) in messages" 
            :key="index"
            :class="['message', msg.role]"
          >
            <div class="avatar" v-if="msg.role === 'assistant'">🤖</div>
            <div class="message-content">
              <div class="text" v-html="formatMessage(msg.content)"></div>
              <div class="time">{{ formatTime(msg.timestamp) }}</div>
            </div>
            <div class="avatar" v-if="msg.role === 'user'">👤</div>
          </div>

          <!-- 加载中 -->
          <div v-if="isLoading" class="message assistant">
            <div class="avatar">🤖</div>
            <div class="message-content">
              <div class="typing">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入框 -->
        <div class="chat-input">
          <div class="quick-replies" v-if="showQuickReplies">
            <button 
              v-for="(reply, index) in quickReplies" 
              :key="index"
              class="quick-reply-btn"
              @click="sendQuickReply(reply)"
            >
              {{ reply }}
            </button>
          </div>
          <div class="input-wrapper">
            <textarea
              v-model="inputMessage"
              placeholder="输入您的问题..."
              @keydown.enter.exact.prevent="sendMessage"
              @keydown.shift.enter.exact="inputMessage += '\n'"
              rows="1"
              ref="textarea"
            ></textarea>
            <button 
              class="send-btn"
              @click="sendMessage"
              :disabled="!inputMessage.trim() || isLoading"
            >
              <span>➤</span>
            </button>
          </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import request from '../utils/request'

const isOpen = ref(false)
const inputMessage = ref('')
const messages = ref([])
const isLoading = ref(false)
const unreadCount = ref(0)
const messagesContainer = ref(null)
const textarea = ref(null)
const showQuickReplies = ref(true)

const quickReplies = [
  '如何预订房源？',
  '取消政策是什么？',
  '推荐热门城市',
  '价格范围查询'
]

const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    unreadCount.value = 0
    nextTick(() => {
      scrollToBottom()
      textarea.value?.focus()
    })
  }
}

const sendQuickReply = (reply) => {
  inputMessage.value = reply
  sendMessage()
  showQuickReplies.value = false
}

const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || isLoading.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: content,
    timestamp: new Date()
  })

  inputMessage.value = ''
  isLoading.value = true
  
  nextTick(() => {
    scrollToBottom()
    adjustTextareaHeight()
  })

  try {
    // 调用后端API
    const res = await request.post('/api/ai/chat', {
      message: content,
      history: messages.value.slice(-10) // 只发送最近10条消息作为上下文
    })

    // 添加AI回复
    messages.value.push({
      role: 'assistant',
      content: res.reply || res.message || '抱歉，我现在无法回答这个问题。',
      timestamp: new Date()
    })

    nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    console.error('AI回复失败:', error)
    messages.value.push({
      role: 'assistant',
      content: '抱歉，服务暂时不可用，请稍后再试。',
      timestamp: new Date()
    })
  } finally {
    isLoading.value = false
  }
}

const formatMessage = (content) => {
  // 简单的markdown格式化
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const adjustTextareaHeight = () => {
  if (textarea.value) {
    textarea.value.style.height = 'auto'
    textarea.value.style.height = Math.min(textarea.value.scrollHeight, 120) + 'px'
  }
}

watch(inputMessage, () => {
  nextTick(() => {
    adjustTextareaHeight()
  })
})
</script>

<style scoped>
.ai-chatbot {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
}

/* 聊天按钮 */
.chat-button {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  opacity: 1;
  transform: scale(1);
}

.chat-button.hiding {
  opacity: 0;
  transform: scale(0.8);
  pointer-events: none;
}

.chat-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.chat-button.hiding:hover {
  transform: scale(0.8);
}

.chat-button .icon {
  font-size: 28px;
}

.chat-button .badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #ff4757;
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

/* 聊天窗口 */
.chat-window {
  width: 380px;
  height: 600px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-header .avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.chat-header .info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-header .name {
  font-size: 16px;
  font-weight: 600;
}

.chat-header .status {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  opacity: 0.9;
}

.status .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4ade80;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  transition: background 0.2s;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f7f7f7;
}

.welcome-message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.welcome-message .avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.welcome-message .message-content {
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  max-width: 80%;
}

.welcome-message p {
  margin: 0 0 8px 0;
  color: #222;
  font-size: 14px;
  line-height: 1.5;
}

.welcome-message ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
  color: #666;
  font-size: 13px;
}

.welcome-message li {
  margin: 4px 0;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message.user {
  flex-direction: row-reverse;
}

.message .avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message.user .avatar {
  background: #667eea;
}

.message .message-content {
  max-width: 70%;
}

.message.user .message-content {
  background: #667eea;
  color: white;
  padding: 10px 14px;
  border-radius: 12px 12px 4px 12px;
}

.message.assistant .message-content {
  background: white;
  padding: 10px 14px;
  border-radius: 12px 12px 12px 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.message .text {
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;
}

.message.assistant .text {
  color: #222;
}

.message .time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}

.message.assistant .time {
  color: #999;
}

/* 打字动画 */
.typing {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.typing span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #999;
  animation: typing 1.4s infinite;
}

.typing span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-10px); }
}

/* 输入框 */
.chat-input {
  background: white;
  border-top: 1px solid #ebebeb;
}

.quick-replies {
  padding: 12px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  border-bottom: 1px solid #ebebeb;
}

.quick-reply-btn {
  padding: 6px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  background: white;
  color: #666;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-reply-btn:hover {
  background: #f5f5f5;
  border-color: #667eea;
  color: #667eea;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  padding: 12px 16px;
  gap: 12px;
}

.input-wrapper textarea {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  font-family: inherit;
  max-height: 120px;
  overflow-y: auto;
}

.send-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: #667eea;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.2s;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background: #5568d3;
  transform: scale(1.05);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 动画 */
.bounce-enter-active {
  animation: bounce-in 0.5s;
}

.bounce-leave-active {
  animation: bounce-out 0.3s;
}

@keyframes bounce-in {
  0% { transform: scale(0); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

@keyframes bounce-out {
  0% { transform: scale(1); }
  100% { transform: scale(0); }
}

.slide-up-enter-active {
  animation: slide-up 0.3s ease-out;
}

.slide-up-leave-active {
  animation: slide-down 0.3s ease-in;
}

@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slide-down {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(20px);
  }
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #ccc;
}
</style>
