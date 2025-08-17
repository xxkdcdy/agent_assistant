<template>
  <div class="chat-container">
    <div class="chat-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <div class="header-right">
        <h2>🕊️ GitHub助手</h2>
        <div class="chat-id">会话ID: {{ chatId }}</div>
      </div>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="message in messages" :key="message.id" :class="['message', message.type]">
        <div class="message-avatar" v-if="message.type === 'ai'">
          <div class="avatar ai-avatar">🕊️</div>
        </div>
        <div class="message-avatar" v-if="message.type === 'user'">
          <div class="avatar user-avatar">👤</div>
        </div>
                 <div class="message-bubble">
           <div class="message-content" v-if="message.type === 'ai'" v-html="formatMessage(message.content)"></div>
           <div class="message-content" v-else>{{ message.content }}</div>
           <div class="message-time">{{ formatTime(message.timestamp) }}</div>
         </div>
      </div>
      
      <div v-if="isLoading" class="message ai">
        <div class="message-bubble">
          <div class="loading">GitHub助手正在思考中...</div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <div class="input-group">
        <input 
          v-model="inputMessage" 
          @keypress.enter="sendMessage"
          placeholder="请输入您的GitHub相关问题，助手会为您提供帮助..."
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
import { startGitHubHelperSSE, generateChatId } from '../services/api'
import { marked } from 'marked'

export default {
  name: 'GitHubHelperChat',
  data() {
    return {
      messages: [],
      inputMessage: '',
      isLoading: false,
      sseConnection: null,
      currentAIMessage: '',
      isProcessingSSE: false,
      chatId: null
    }
  },
  created() {
    this.addWelcomeMessage()
    this.chatId = generateChatId()
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
        content: '🕊️ 欢迎使用GitHub助手！\n\n我是您的GitHub专属助手，可以帮您解决各种GitHub相关的问题。\n\n• 代码审查和优化建议\n• Git操作指导\n• GitHub功能使用说明\n• 项目管理和协作建议\n• 问题排查和解决方案\n\n请告诉我您需要什么帮助，我会尽力为您提供专业的建议！',
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
      
      // 重置状态
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
        console.log('创建新的GitHub助手SSE连接:', `/api/ai/github_helper/chat/sse/emitter?message=${encodeURIComponent(messageToSend)}&chatId=${this.chatId}`)
        
        // 重置状态
        this.currentAIMessage = ''
        this.isProcessingSSE = true
        
        // 创建新的SSE连接
        this.sseConnection = startGitHubHelperSSE(
          messageToSend,
          this.chatId,
          (data) => this.handleSSEMessage(data),
          (error) => this.handleSSEError(error),
          () => this.handleSSEOpen(),
          () => this.handleSSEClose(),
          (data) => this.handleSSEComplete(data)
        )
        
        this.sseConnection.connect()
      }, 200)
      
      this.scrollToBottom()
    },
    
    handleSSEOpen() {
      console.log('GitHub助手SSE连接已建立')
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
        
        // 检查是否是结束标记
        if (data.trim() === '[END_CONVERSATION]') {
          console.log('检测到对话结束标记，调用complete处理')
          this.handleSSEComplete(data)
          return
        }
        
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
    
    handleSSEComplete(data) {
      console.log('Spring AI流式传输完成，最终消息长度:', this.currentAIMessage.length)
      this.isLoading = false
      this.isProcessingSSE = false
      
      // 标记流式传输结束
      const aiMessages = this.messages.filter(msg => msg.type === 'ai' && msg.isStreaming)
      if (aiMessages.length > 0) {
        const lastAIMessage = aiMessages[aiMessages.length - 1]
        lastAIMessage.isStreaming = false
        console.log('标记消息传输完成，最终内容长度:', lastAIMessage.content.length)
      }
      
      // 确保连接完全关闭
      if (this.sseConnection) {
        this.sseConnection.close()
        this.sseConnection = null
      }
    },
    
    handleSSEError(error) {
      console.error('GitHub助手SSE连接错误:', error, 'currentAIMessage长度:', this.currentAIMessage.length)
      
      // 如果已经不在加载状态，说明连接已经正常结束，不应该显示错误
      if (!this.isLoading) {
        console.log('连接已正常结束，忽略后续错误事件')
        return
      }
      
      // 如果已经有AI回复内容，说明连接是正常的，这只是正常的流结束
      if (this.currentAIMessage && this.currentAIMessage.length > 0) {
        console.log('检测到AI已有回复内容，忽略连接关闭错误')
        this.isLoading = false
        this.isProcessingSSE = false
        
        // 标记流式传输结束
        const aiMessages = this.messages.filter(msg => msg.type === 'ai' && msg.isStreaming)
        if (aiMessages.length > 0) {
          const lastAIMessage = aiMessages[aiMessages.length - 1]
          lastAIMessage.isStreaming = false
          console.log('标记消息传输完成，最终内容长度:', lastAIMessage.content.length)
        }
        
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
      let errorMessage = '抱歉，无法连接到GitHub助手服务。'
      errorMessage += '\n\n可能的原因：\n• 后端服务未启动\n• 网络连接问题\n• 服务器暂时不可用\n\n请确保后端服务正常运行后重试。'
      
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
      // 只对AI消息进行Markdown解析，用户消息保持原样
      return this.parseMarkdown(content)
    },
    
    parseMarkdown(content) {
      try {
        // 配置marked选项
        marked.setOptions({
          breaks: true, // 支持换行
          gfm: true,    // 支持GitHub风格Markdown
          sanitize: false // 允许HTML标签
        })
        return marked(content)
      } catch (error) {
        console.error('Markdown解析错误:', error)
        // 如果解析失败，回退到简单的换行处理
        return content.replace(/\n/g, '<br>')
      }
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
/* 全屏样式覆盖 */
.chat-container {
  margin: 0;
  padding: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

/* 头像样式 */
.message-avatar {
  display: flex;
  align-items: flex-end;
  margin: 0 8px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.8);
}

.ai-avatar {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: white;
}

.user-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

/* 调整消息布局 */
.message {
  margin-bottom: 16px;
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.message.user {
  justify-content: flex-end;
  flex-direction: row-reverse;
}

.message.ai {
  justify-content: flex-start;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f8f9fa;
  width: 100%;
  box-sizing: border-box;
}

.chat-input {
  padding: 20px;
  background: white;
  border-top: 1px solid #e1e8ed;
  width: 100%;
  box-sizing: border-box;
}

.back-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s ease;
  position: relative;
  z-index: 10;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  padding: 20px;
  color: white;
  font-size: 18px;
  font-weight: 600;
  width: 100%;
  box-sizing: border-box;
}

.chat-header h2 {
  flex: 1;
  text-align: center;
  margin: 0;
}

.chat-header .header-right {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  width: auto;
}

.chat-id {
  font-size: 12px;
  opacity: 0.8;
  min-width: 150px;
  text-align: right;
}

.message-content {
  margin-bottom: 4px;
  line-height: 1.5;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
  text-align: right;
}

.message.ai .message-time {
  text-align: left;
}

.message.ai .message-bubble {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: 1px solid #dee2e6;
}

.loading {
  background: linear-gradient(90deg, #FFD700, #FFA500, #FFD700);
  background-size: 200% 200%;
  animation: gradientShift 2s ease-in-out infinite;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 500;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 平板设备优化 */
@media (max-width: 1024px) and (min-width: 769px) {
  .chat-messages {
    padding: 20px 30px;
  }
  
  .chat-header,
  .chat-input {
    padding: 20px 30px;
  }
  
  .message-bubble {
    max-width: 75%;
  }
}

/* 移动设备优化 */
@media (max-width: 768px) {
  .chat-container {
    width: 100vw;
    height: 100vh;
    height: 100dvh; /* 动态视口高度，支持移动设备地址栏 */
  }
  
  .chat-header {
    flex-direction: row;
    gap: 15px;
    align-items: center;
    padding: 15px;
  }
  
  .chat-header h2 {
    text-align: left;
    font-size: 16px;
    margin-bottom: 2px;
  }
  
  .chat-id {
    text-align: right;
    font-size: 11px;
    min-width: auto;
  }
  
  .chat-header .header-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    flex: 1;
    position: static;
    left: auto;
    transform: none;
    width: auto;
  }
  
  .chat-header h2 {
    text-align: right;
    font-size: 16px;
    margin-bottom: 2px;
  }
  
  .chat-id {
    text-align: right;
    font-size: 11px;
    min-width: auto;
  }
  
  .chat-messages {
    padding: 15px;
    flex: 1;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch; /* iOS平滑滚动 */
  }
  
  .chat-input {
    padding: 15px;
  }
  
  .chat-id {
    text-align: center;
    min-width: auto;
  }
  
  .message-bubble {
    max-width: 75%;
    font-size: 14px;
  }
  
  .avatar {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
  
  .input-group {
    gap: 10px;
  }
  
     .chat-input input {
     font-size: 16px; /* 防止iOS缩放 */
   }
 }

 /* Markdown样式支持 - 仅针对AI消息气泡 */
 .message.ai .message-content {
   line-height: 1.6;
   color: #333;
 }

 .message.ai .message-content h1,
 .message.ai .message-content h2,
 .message.ai .message-content h3,
 .message.ai .message-content h4,
 .message.ai .message-content h5,
 .message.ai .message-content h6 {
   margin: 20px 0 12px 0;
   font-weight: 700;
   color: #2c3e50;
   border-bottom: 2px solid #FFD700;
   padding-bottom: 8px;
 }

 .message.ai .message-content h1 { font-size: 1.8em; }
 .message.ai .message-content h2 { font-size: 1.5em; }
 .message.ai .message-content h3 { font-size: 1.3em; }

 .message.ai .message-content p {
   margin: 12px 0;
   line-height: 1.7;
 }

 .message.ai .message-content ul,
 .message.ai .message-content ol {
   margin: 12px 0;
   padding-left: 24px;
 }

 .message.ai .message-content li {
   margin: 8px 0;
   line-height: 1.6;
 }

 /* 项目推荐样式优化 */
 .message.ai .message-content h3 + ul,
 .message.ai .message-content h3 + ol {
   background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
   border: 1px solid #dee2e6;
   border-radius: 8px;
   padding: 16px 20px;
   margin: 16px 0;
   box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
 }

 .message.ai .message-content h3 + ul li,
 .message.ai .message-content h3 + ol li {
   background: white;
   padding: 8px 12px;
   margin: 8px 0;
   border-radius: 6px;
   border-left: 3px solid #FFD700;
   box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
 }

 .message.ai .message-content code {
   background-color: #f1f3f4;
   padding: 3px 8px;
   border-radius: 4px;
   font-family: 'Courier New', monospace;
   font-size: 0.9em;
   color: #d73a49;
   border: 1px solid #e1e4e8;
 }

 .message.ai .message-content pre {
   background-color: #f6f8fa;
   padding: 16px;
   border-radius: 8px;
   overflow-x: auto;
   margin: 16px 0;
   border: 1px solid #e1e4e8;
   box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
 }

 .message.ai .message-content pre code {
   background-color: transparent;
   padding: 0;
   color: #24292e;
   border: none;
 }

 .message.ai .message-content blockquote {
   border-left: 4px solid #FFD700;
   margin: 16px 0;
   padding: 12px 20px;
   background-color: #f8f9fa;
   border-radius: 0 6px 6px 0;
   color: #6c757d;
   font-style: italic;
 }

 .message.ai .message-content a {
   color: #FFD700;
   text-decoration: none;
   font-weight: 500;
   border-bottom: 1px solid transparent;
   transition: all 0.3s ease;
 }

 .message.ai .message-content a:hover {
   color: #FFA500;
   border-bottom-color: #FFA500;
   text-decoration: none;
 }

 .message.ai .message-content table {
   border-collapse: collapse;
   width: 100%;
   margin: 16px 0;
   border-radius: 8px;
   overflow: hidden;
   box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
 }

 .message.ai .message-content th,
 .message.ai .message-content td {
   border: 1px solid #e1e4e8;
   padding: 12px 16px;
   text-align: left;
 }

 .message.ai .message-content th {
   background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
   color: white;
   font-weight: 600;
 }

 .message.ai .message-content tr:nth-child(even) {
   background-color: #f8f9fa;
 }

 .message.ai .message-content tr:hover {
   background-color: #f1f3f4;
 }

 .message.ai .message-content strong {
   font-weight: 700;
   color: #2c3e50;
   background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
   -webkit-background-clip: text;
   -webkit-text-fill-color: transparent;
   background-clip: text;
 }

 .message.ai .message-content em {
   font-style: italic;
   color: #6c757d;
 }

 .message.ai .message-content hr {
   border: none;
   border-top: 2px solid #FFD700;
   margin: 24px 0;
   opacity: 0.3;
 }

 /* 特殊格式优化 */
 .message.ai .message-content .highlight {
   background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
   color: white;
   padding: 2px 6px;
   border-radius: 4px;
   font-weight: 500;
 }
 </style>
