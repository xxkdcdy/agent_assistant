<template>
  <div class="chat-container">
    <div class="chat-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h2>AI 恋爱大师 💕</h2>
      <div class="chat-id">会话ID: {{ chatId }}</div>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="message in messages" :key="message.id" :class="['message', message.type]">
        <div class="message-bubble">
          <div class="message-content" v-html="formatMessage(message.content)"></div>
          <div class="message-time">{{ formatTime(message.timestamp) }}</div>
        </div>
      </div>
      
      <div v-if="isLoading" class="message ai">
        <div class="message-bubble">
          <div class="loading">AI正在思考中...</div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <div class="input-group">
        <input 
          v-model="inputMessage" 
          @keypress.enter="sendMessage"
          placeholder="请输入您的问题，我来为您提供恋爱建议..."
          :disabled="isLoading"
        />
        <button 
          class="send-btn" 
          @click="sendMessage"
          :disabled="isLoading || !inputMessage.trim()"
        >
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { generateChatId, startLoveChatSSE } from '../services/api'

export default {
  name: 'LoveChat',
  data() {
    return {
      chatId: '',
      messages: [],
      inputMessage: '',
      isLoading: false,
      sseConnection: null,
      currentAIMessage: '',
      isProcessingSSE: false  // 防止重复处理
    }
  },
  created() {
    this.chatId = generateChatId()
    this.addWelcomeMessage()
  },
  beforeUnmount() {
    if (this.sseConnection) {
      this.sseConnection.close()
    }
  },
  methods: {
    addWelcomeMessage() {
      this.messages.push({
        id: Date.now(),
        type: 'ai',
        content: '你好！我是你的AI恋爱大师💕 我可以为你提供恋爱建议、情感咨询和关系指导。请告诉我你遇到的情感问题，我会尽力帮助你！',
        timestamp: new Date()
      })
    },
    
    sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) return
      
      const userMessage = {
        id: Date.now(),
        type: 'user',
        content: this.inputMessage,
        timestamp: new Date()
      }
      
      this.messages.push(userMessage)
      const messageToSend = this.inputMessage
      this.inputMessage = ''
      this.isLoading = true
      
      // 强制清理所有状态
      this.isProcessingSSE = false
      this.currentAIMessage = ''
      
      // 关闭之前的连接
      if (this.sseConnection) {
        console.log('关闭之前的SSE连接')
        this.sseConnection.close()
        this.sseConnection = null
      }
      
      // 清理可能存在的流式传输中的AI消息
      const streamingMessages = this.messages.filter(msg => msg.type === 'ai' && msg.isStreaming)
      streamingMessages.forEach(msg => {
        msg.isStreaming = false
        console.log('清理未完成的流式消息:', msg.id)
      })
      
      // 等待一下再创建新连接，确保之前的连接完全关闭
      setTimeout(() => {
        console.log('创建新的SSE连接:', `/api/ai/love_app/chat/sse?message=${encodeURIComponent(messageToSend)}&chatId=${encodeURIComponent(this.chatId)}`)
        
        // 重置状态
        this.currentAIMessage = ''
        this.isProcessingSSE = true
        
        // 创建新的SSE连接
        this.sseConnection = startLoveChatSSE(
          messageToSend,
          this.chatId,
          (data) => this.handleSSEMessage(data),
          (error) => this.handleSSEError(error),
          () => this.handleSSEOpen(),
          () => this.handleSSEClose()
        )
        
        this.sseConnection.connect()
      }, 200)
      
      this.scrollToBottom()
    },
    
    handleSSEOpen() {
      console.log('恋爱大师SSE连接已建立')
    },
    
    handleSSEClose() {
      console.log('Spring AI流正常结束，最终消息长度:', this.currentAIMessage.length)
      
      // 确保连接完全关闭，防止自动重连
      if (this.sseConnection) {
        this.sseConnection.close()
        this.sseConnection = null
      }
      
      this.isLoading = false
      this.isProcessingSSE = false
      
      // 标记流式传输结束
      const aiMessages = this.messages.filter(msg => msg.type === 'ai' && msg.isStreaming)
      console.log('找到流式传输中的消息数量:', aiMessages.length)
      if (aiMessages.length > 0) {
        const lastAIMessage = aiMessages[aiMessages.length - 1]
        lastAIMessage.isStreaming = false
        console.log('标记消息传输完成，最终内容长度:', lastAIMessage.content.length)
      }
    },
    
    handleSSEMessage(data) {
      try {
        // 防止重复处理
        if (!this.isProcessingSSE || !this.isLoading) {
          console.log('忽略SSE消息，不在处理状态')
          return
        }
        
        console.log('收到SSE数据:', data, '当前累积长度:', this.currentAIMessage.length)
        
        // Spring AI直接发送内容，不需要处理特殊前缀
        let processedData = data.trim()
        
        // 跳过空数据
        if (!processedData) {
          return
        }
        
        // 累积AI回复内容
        this.currentAIMessage += processedData
        
        // 查找当前正在流式传输的AI消息
        let aiMessage = this.messages.find(msg => msg.type === 'ai' && msg.isStreaming)
        if (!aiMessage) {
          // 只创建一次AI消息对象
          aiMessage = {
            id: Date.now(),
            type: 'ai',
            content: '',
            timestamp: new Date(),
            isStreaming: true
          }
          this.messages.push(aiMessage)
          console.log('创建新的AI消息对象，ID:', aiMessage.id)
        }
        
        // 更新消息内容
        aiMessage.content = this.currentAIMessage
        console.log('更新消息内容，当前长度:', aiMessage.content.length)
        this.scrollToBottom()
        
      } catch (error) {
        console.error('处理SSE消息错误:', error)
        // 只有在真正的错误情况下才调用错误处理
        if (this.isLoading) {
          this.handleSSEError(error)
        }
      }
    },
    
    handleSSEError(error) {
      console.error('恋爱大师SSE连接错误:', error, 'currentAIMessage长度:', this.currentAIMessage.length)
      
      // 如果已经不在加载状态，说明连接已经正常结束，不应该显示错误
      if (!this.isLoading) {
        console.log('连接已正常结束，忽略后续错误事件')
        return
      }
      
      // 如果已经有AI回复内容，说明连接是正常的，这只是正常的流结束
      if (this.currentAIMessage && this.currentAIMessage.length > 0) {
        console.log('检测到AI已有回复内容，忽略连接关闭错误')
        this.isLoading = false
        // 确保连接完全关闭
        if (this.sseConnection) {
          this.sseConnection.close()
          this.sseConnection = null
        }
        return
      }
      
      this.isLoading = false
      this.isProcessingSSE = false
      
      // 添加详细错误消息
      let errorMessage = '抱歉，无法连接到AI服务。'
      errorMessage += '\n\n可能的原因：\n• 后端服务未启动（需要运行在 localhost:8123）\n• 网络连接问题\n• 服务器暂时不可用\n\n请确保后端服务正常运行后重试。'
      
      this.messages.push({
        id: Date.now(),
        type: 'ai',
        content: errorMessage,
        timestamp: new Date()
      })
      
      if (this.sseConnection) {
        this.sseConnection.close()
        this.sseConnection = null
      }
    },
    
    formatMessage(content) {
      return content.replace(/\n/g, '<br>')
    },
    
    formatTime(timestamp) {
      return timestamp.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    },
    
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },
    
    goBack() {
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.back-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s ease;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.chat-header h2 {
  flex: 1;
  text-align: center;
  margin: 0;
}

.chat-id {
  font-size: 12px;
  opacity: 0.8;
  min-width: 150px;
  text-align: right;
}

.message-content {
  margin-bottom: 4px;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
  text-align: right;
}

.message.ai .message-time {
  text-align: left;
}

@media (max-width: 768px) {
  .chat-header {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
  
  .chat-id {
    text-align: center;
    min-width: auto;
  }
}
</style>
