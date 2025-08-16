<template>
  <div class="chat-container">
    <div class="chat-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <div class="header-right">
        <h2>"世另我"AI 智能体 🤖</h2>
        <div class="status-indicator" :class="{ connected: isConnected }">
          {{ isConnected ? '已连接' : '未连接' }}
        </div>
      </div>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
             <div v-for="message in messages" :key="message.id" :class="['message', message.type, { 
         'step-message': message.isStep, 
         'thinking-message': message.isThinking,
         'error-message': message.isError,
         'tool-message': message.isTool,
         'result-message': message.isResult,
         'completion-message': message.isCompletion
       }]">
        <div class="message-avatar" v-if="message.type === 'ai'">
          <div class="avatar ai-avatar">🤖</div>
        </div>
        <div class="message-avatar" v-if="message.type === 'user'">
          <div class="avatar user-avatar">👤</div>
        </div>
                 <div class="message-bubble" :class="{ 
           'step-bubble': message.isStep, 
           'thinking-bubble': message.isThinking,
           'error-bubble': message.isError,
           'tool-bubble': message.isTool,
           'result-bubble': message.isResult,
           'completion-bubble': message.isCompletion
         }">
          <div class="message-content" v-html="formatMessage(message.content)"></div>
          <div class="message-time">{{ formatTime(message.timestamp) }}</div>
        </div>
      </div>
      
      <div v-if="isLoading" class="message ai">
        <div class="message-bubble">
          <div class="loading">AI智能体正在分析处理中...</div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <div class="input-group">
        <input 
          v-model="inputMessage" 
          @keypress.enter="sendMessage"
          placeholder="请输入您的问题，AI智能体将为您提供全方位的智能服务..."
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
import { startManusChatSSE } from '../services/api'

export default {
  name: 'ManusChat',
  data() {
    return {
      messages: [],
      inputMessage: '',
      isLoading: false,
      isConnected: false,
      sseConnection: null,
      currentAIMessage: '',
      isProcessingSSE: false,  // 防止重复处理
      currentStepBuffer: '',   // 当前步骤缓冲区
      stepMessages: [],        // 存储各个步骤的消息
             connectionId: null      // 连接ID，用于识别连接
    }
  },
  created() {
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
        content: '你好！我是"世另我"AI智能体🤖 我拥有强大的多模态能力，可以为您提供：\n\n• 📝 文本创作与编辑\n• 🔍 信息查询与分析\n• 💡 问题解决方案\n• 🎯 专业建议与指导\n• 🌐 多领域知识支持\n\n请告诉我您需要什么帮助！',
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
       this.currentStepBuffer = ''
       this.stepMessages = []
      
      // 清理可能存在的特殊消息标识
      this.messages.forEach(msg => {
        msg.isThinking = false
        msg.isError = false
        msg.isTool = false
        msg.isCompletion = false
      })
      
      // 生成新的连接ID
      const newConnectionId = Date.now() + '_' + Math.random().toString(36).substr(2, 9)
      this.connectionId = newConnectionId
      
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
        // 确保连接ID没有被新的请求覆盖
        if (this.connectionId !== newConnectionId) {
          console.log('连接已被新请求替代，取消创建连接')
          return
        }
        
        console.log('创建新的SSE连接:', `/api/ai/manus/chat?message=${encodeURIComponent(messageToSend)}`, '连接ID:', newConnectionId)
        
                 // 重置状态
         this.currentAIMessage = ''
         this.currentStepBuffer = ''
         this.stepMessages = []
         this.isProcessingSSE = true
        
        // 创建新的SSE连接
        this.sseConnection = startManusChatSSE(
          messageToSend,
          (data) => this.handleSSEMessage(data, newConnectionId),
          (error) => this.handleSSEError(error, newConnectionId),
          () => this.handleSSEOpen(newConnectionId),
          () => this.handleSSEClose(newConnectionId),
          (data) => this.handleSSEComplete(data, newConnectionId)
        )
        
        this.sseConnection.connect()
      }, 300)
      
      this.scrollToBottom()
    },
    
    handleSSEOpen(connectionId) {
      // 检查连接ID是否匹配
      if (connectionId && this.connectionId !== connectionId) {
        console.log('忽略过期连接的Open事件:', connectionId, '当前连接:', this.connectionId)
        return
      }
      console.log('世另我智能体SSE连接已建立，连接ID:', connectionId)
      this.isConnected = true
    },
    
    handleSSEClose(connectionId) {
      // 检查连接ID是否匹配
      if (connectionId && this.connectionId !== connectionId) {
        console.log('忽略过期连接的Close事件:', connectionId, '当前连接:', this.connectionId)
        return
      }
      
      console.log('Spring AI流正常结束，连接ID:', connectionId)
      
      // 确保连接完全关闭，防止自动重连
      if (this.sseConnection) {
        this.sseConnection.close()
        this.sseConnection = null
      }
      
      // 处理剩余的缓冲区内容
      if (this.currentStepBuffer.trim()) {
        this.addStepMessage(`Step ${this.stepMessages.length + 1}: ${this.currentStepBuffer.trim()}`)
      }
      
      this.isLoading = false
      this.isConnected = false
      this.isProcessingSSE = false
      
      // 清理连接
      if (this.sseConnection) {
        this.sseConnection = null
      }
      
      console.log('智能体任务完成，共', this.stepMessages.length, '个步骤')
    },
    
    handleSSEMessage(data, connectionId) {
      try {
        // 检查连接ID是否匹配
        if (connectionId && this.connectionId !== connectionId) {
          console.log('忽略过期连接的消息:', connectionId, '当前连接:', this.connectionId)
          return
        }
        
        // 防止重复处理
        if (!this.isProcessingSSE || !this.isLoading) {
          console.log('忽略SSE消息，不在处理状态')
          return
        }
        
        console.log('收到SSE数据:', data, '连接ID:', connectionId)
        
        // 检查是否是结束标记
        if (data.trim() === '[END_CONVERSATION]') {
          console.log('检测到对话结束标记，调用complete处理')
          this.handleSSEComplete(data, connectionId)
          return
        }
        
        // 处理单行消息
        let processedData = data.trim()
        
        // 跳过空数据
        if (!processedData) {
          return
        }
        
        // 直接处理单行消息，按照约定前缀规范识别消息类型
        this.processSingleMessage(processedData)
        
        this.scrollToBottom()
        
      } catch (error) {
        console.error('处理SSE消息错误:', error)
        // 只有在真正的错误情况下才调用错误处理
        if (this.isLoading) {
          this.handleSSEError(error, connectionId)
        }
      }
    },
    
    handleSSEComplete(data, connectionId) {
      // 检查连接ID是否匹配
      if (connectionId && this.connectionId !== connectionId) {
        console.log('忽略过期连接的Complete事件:', connectionId, '当前连接:', this.connectionId)
        return
      }
      
      console.log('Spring AI流式传输完成，连接ID:', connectionId)
      
      // 清理状态
      this.isLoading = false
      this.isConnected = false
      this.isProcessingSSE = false
      
      // 确保连接完全关闭
      if (this.sseConnection) {
        this.sseConnection.close()
        this.sseConnection = null
      }
      
      console.log('智能体任务完成')
    },
    
    processSingleMessage(data) {
      // 按照约定前缀规范识别单行消息类型
      let messageType = 'step' // 默认类型
      
      if (data.startsWith('💭 THINKING:') || data.startsWith('💭 思考:')) {
        messageType = 'thinking'
        console.log('识别为思考消息:', data.substring(0, 50) + '...')
      } else if (data.startsWith('⚙️ STEP:') || data.startsWith('⚙️ 步骤:')) {
        messageType = 'step'
        console.log('识别为步骤消息:', data.substring(0, 50) + '...')
      } else if (data.startsWith('🛠 TOOL:') || data.startsWith('🛠 工具:')) {
        messageType = 'tool'
        console.log('识别为工具消息:', data.substring(0, 50) + '...')
      } else if (data.startsWith('📊 RESULT:') || data.startsWith('📊 结果:')) {
        messageType = 'result'
        console.log('识别为结果消息:', data.substring(0, 50) + '...')
      } else if (data.startsWith('✅ COMPLETION:') || data.startsWith('✅ 完成:')) {
        messageType = 'completion'
        console.log('识别为完成消息:', data.substring(0, 50) + '...')
      } else if (data.startsWith('❌ ERROR:') || data.startsWith('❌ 错误:')) {
        messageType = 'error'
        console.log('识别为错误消息:', data.substring(0, 50) + '...')
      } else {
        // 如果没有识别到前缀，默认为步骤消息
        messageType = 'step'
        console.log('未识别前缀，默认为步骤消息:', data.substring(0, 50) + '...')
      }
      
      // 直接添加消息
      this.addTypedMessage(data, messageType)
    },
    
    processStepBuffer() {
      // 按照约定前缀规范分割不同类型的消息
      const lines = this.currentStepBuffer.split('\n')
      let currentMessage = ''
      let processedLines = 0
      let messageType = 'thinking' // 默认类型
      
      for (let i = 0; i < lines.length; i++) {
        const line = lines[i]
        
        // 按照约定前缀规范检测消息类型
        if (line.startsWith('💭 THINKING:') || line.startsWith('💭 思考:')) {
          // 如果有待处理的消息，先处理它
          if (currentMessage.trim()) {
            this.addTypedMessage(currentMessage.trim(), messageType)
          }
          // 开始新的思考消息
          currentMessage = line
          messageType = 'thinking'
          processedLines = i
        } else if (line.startsWith('⚙️ STEP:') || line.startsWith('⚙️ 步骤:')) {
          // 如果有待处理的消息，先处理它
          if (currentMessage.trim()) {
            this.addTypedMessage(currentMessage.trim(), messageType)
          }
          // 开始新的步骤消息
          currentMessage = line
          messageType = 'step'
          processedLines = i
        } else if (line.startsWith('🛠 TOOL:') || line.startsWith('🛠 工具:')) {
          // 如果有待处理的消息，先处理它
          if (currentMessage.trim()) {
            this.addTypedMessage(currentMessage.trim(), messageType)
          }
          // 开始新的工具消息
          currentMessage = line
          messageType = 'tool'
          processedLines = i
        } else if (line.startsWith('📊 RESULT:') || line.startsWith('📊 结果:')) {
          // 如果有待处理的消息，先处理它
          if (currentMessage.trim()) {
            this.addTypedMessage(currentMessage.trim(), messageType)
          }
          // 开始新的结果消息
          currentMessage = line
          messageType = 'result'
          processedLines = i
        } else if (line.startsWith('✅ COMPLETION:') || line.startsWith('✅ 完成:')) {
          // 如果有待处理的消息，先处理它
          if (currentMessage.trim()) {
            this.addTypedMessage(currentMessage.trim(), messageType)
          }
          // 开始新的完成消息
          currentMessage = line
          messageType = 'completion'
          processedLines = i
        } else if (line.startsWith('❌ ERROR:') || line.startsWith('❌ 错误:')) {
          // 如果有待处理的消息，先处理它
          if (currentMessage.trim()) {
            this.addTypedMessage(currentMessage.trim(), messageType)
          }
          // 开始新的错误消息
          currentMessage = line
          messageType = 'error'
          processedLines = i
        } else {
          // 累加到当前消息
          if (currentMessage) {
            currentMessage += '\n' + line
          } else {
            currentMessage = line
          }
        }
      }
      
      // 清理已处理的内容
      if (processedLines > 0) {
        const remainingLines = lines.slice(processedLines)
        this.currentStepBuffer = remainingLines.join('\n')
        console.log('处理了', processedLines, '行，剩余缓冲区内容:', this.currentStepBuffer.length, '字符')
      }
    },
    
         addTypedMessage(content, messageType) {
       const message = {
         id: Date.now() + Math.random(),
         type: 'ai',
         content: content,
         timestamp: new Date(),
         isStreaming: false
       }
       
       // 根据消息类型设置不同的标识
       switch (messageType) {
         case 'thinking':
           message.isThinking = true
           break
         case 'step':
           message.isStep = true
           break
         case 'tool':
           message.isTool = true
           break
         case 'result':
           message.isResult = true
           break
         case 'completion':
           message.isCompletion = true
           break
         case 'error':
           message.isError = true
           break
         default:
           message.isStep = true
       }
       
       this.messages.push(message)
       console.log('添加', messageType, '消息:', content.substring(0, 50) + '...')
     },
    
         addStepMessage(content) {
       const stepMessage = {
         id: Date.now() + Math.random(),
         type: 'ai',
         content: content,
         timestamp: new Date(),
         isStreaming: false,
         isStep: true
       }
       
       this.stepMessages.push(stepMessage)
       this.messages.push(stepMessage)
       console.log('添加步骤消息:', content.substring(0, 50) + '...')
     },
    
    addThinkingMessage(content) {
      const thinkingMessage = {
        id: Date.now() + Math.random(),
        type: 'ai',
        content: content,
        timestamp: new Date(),
        isStreaming: false,
        isThinking: true
      }
      
      this.messages.push(thinkingMessage)
      console.log('添加思考消息:', content.substring(0, 50) + '...')
      this.scrollToBottom()
    },
    
    handleSpecialMessage(data) {
      // 思考消息
      if (data.startsWith('💭 思考: ')) {
        this.addThinkingMessage(data.substring('💭 思考: '.length))
        return true
      }
      
      // 错误消息
      if (data.startsWith('❌ ') || data.includes('错误') || data.includes('失败')) {
        this.addErrorMessage(data)
        return true
      }
      
      // 工具调用信息（可能的其他特殊消息）
      if (data.startsWith('🔧 工具: ') || data.startsWith('工具调用: ')) {
        this.addToolMessage(data)
        return true
      }
      
      // 完成消息
      if (data.includes('任务完成') || data.includes('执行完成') || data.includes('处理完成')) {
        this.addCompletionMessage(data)
        return true
      }
      
      return false
    },
    
    addErrorMessage(content) {
      const errorMessage = {
        id: Date.now() + Math.random(),
        type: 'ai',
        content: content,
        timestamp: new Date(),
        isStreaming: false,
        isError: true
      }
      
      this.messages.push(errorMessage)
      console.log('添加错误消息:', content.substring(0, 50) + '...')
      this.scrollToBottom()
    },
    
    addToolMessage(content) {
      const toolMessage = {
        id: Date.now() + Math.random(),
        type: 'ai',
        content: content,
        timestamp: new Date(),
        isStreaming: false,
        isTool: true
      }
      
      this.messages.push(toolMessage)
      console.log('添加工具消息:', content.substring(0, 50) + '...')
      this.scrollToBottom()
    },
    
    addCompletionMessage(content) {
      const completionMessage = {
        id: Date.now() + Math.random(),
        type: 'ai',
        content: content,
        timestamp: new Date(),
        isStreaming: false,
        isCompletion: true
      }
      
      this.messages.push(completionMessage)
      console.log('添加完成消息:', content.substring(0, 50) + '...')
      this.scrollToBottom()
    },
    
    handleSSEError(error, connectionId) {
      // 检查连接ID是否匹配
      if (connectionId && this.connectionId !== connectionId) {
        console.log('忽略过期连接的Error事件:', connectionId, '当前连接:', this.connectionId)
        return
      }
      
      console.error('世另我智能体SSE连接错误:', error, 'currentAIMessage长度:', this.currentAIMessage.length)
      
      // 如果已经不在加载状态，说明连接已经正常结束，不应该显示错误
      if (!this.isLoading) {
        console.log('连接已正常结束，忽略后续错误事件')
        return
      }
      
      // 如果已经有AI回复内容，说明连接是正常的，这只是正常的流结束
      if (this.stepMessages.length > 0 || this.currentAIMessage.length > 0) {
        console.log('检测到AI已有回复内容，忽略连接关闭错误')
        this.isLoading = false
        this.isConnected = false
        // 确保连接完全关闭
        if (this.sseConnection) {
          this.sseConnection.close()
          this.sseConnection = null
        }
        return
      }
      
      this.isLoading = false
      this.isConnected = false
      this.isProcessingSSE = false
      
      // 添加详细错误消息
      let errorMessage = '抱歉，无法连接到AI智能体服务。'
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
       // 对于工具消息，只显示前200个字符
       if (this.isToolMessage(content)) {
         const truncated = content.length > 200 ? content.substring(0, 200) + '...' : content
         return truncated.replace(/\n/g, '<br>')
       }
       return content.replace(/\n/g, '<br>')
     },
     
     isToolMessage(content) {
       return content.startsWith('🛠 TOOL:') || content.startsWith('🛠 工具:')
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
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

.status-indicator {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  min-width: 60px;
  text-align: center;
  transition: all 0.3s ease;
}

.status-indicator.connected {
  background: rgba(46, 204, 113, 0.8);
  color: white;
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
  background: linear-gradient(90deg, #f093fb, #f5576c, #f093fb);
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

/* 步骤消息特殊样式 */
.step-message .step-bubble {
  background: linear-gradient(135deg, #e8f4fd 0%, #d1ecf1 100%);
  border: 2px solid #3498db;
  border-left: 6px solid #2980b9;
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.15);
  position: relative;
}

.step-message .message-content {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
}

.step-message .message-time {
  color: #2980b9;
  font-weight: 500;
}

/* 思考消息特殊样式 */
.thinking-message .thinking-bubble {
  background: linear-gradient(135deg, #fef9e7 0%, #fdf2e9 100%);
  border: 2px solid #f39c12;
  border-left: 6px solid #e67e22;
  box-shadow: 0 4px 12px rgba(243, 156, 18, 0.15);
  position: relative;
  animation: thinkingPulse 2s ease-in-out infinite;
}

.thinking-message .message-content {
  font-style: italic;
  font-size: 14px;
  line-height: 1.5;
  color: #d68910;
}

.thinking-message .message-time {
  color: #e67e22;
  font-weight: 500;
}

@keyframes thinkingPulse {
  0%, 100% { 
    box-shadow: 0 4px 12px rgba(243, 156, 18, 0.15);
    transform: scale(1);
  }
  50% { 
    box-shadow: 0 6px 16px rgba(243, 156, 18, 0.25);
    transform: scale(1.01);
  }
}

/* 错误消息特殊样式 */
.error-message .error-bubble {
  background: linear-gradient(135deg, #fdedec 0%, #fadbd8 100%);
  border: 2px solid #e74c3c;
  border-left: 6px solid #c0392b;
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.15);
  position: relative;
}

.error-message .message-content {
  font-weight: 500;
  font-size: 14px;
  line-height: 1.5;
  color: #c0392b;
}

.error-message .message-time {
  color: #e74c3c;
  font-weight: 500;
}

 /* 工具消息特殊样式 */
 .tool-message .tool-bubble {
   background: linear-gradient(135deg, #eaf2f8 0%, #d5dbdb 100%);
   border: 2px solid #5d6d7e;
   border-left: 6px solid #34495e;
   box-shadow: 0 4px 12px rgba(93, 109, 126, 0.15);
   position: relative;
 }
 
 .tool-message .message-content {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #34495e;
}
 
 .tool-message .message-time {
   color: #5d6d7e;
   font-weight: 500;
 }
 
 /* 结果消息特殊样式 */
 .result-message .result-bubble {
   background: linear-gradient(135deg, #e8f5e8 0%, #d4edda 100%);
   border: 2px solid #28a745;
   border-left: 6px solid #20c997;
   box-shadow: 0 4px 12px rgba(40, 167, 69, 0.15);
   position: relative;
 }
 
 .result-message .message-content {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #155724;
}
 
 .result-message .message-time {
   color: #28a745;
   font-weight: 500;
 }

/* 完成消息特殊样式 */
.completion-message .completion-bubble {
  background: linear-gradient(135deg, #eafaf1 0%, #d5f4e6 100%);
  border: 2px solid #27ae60;
  border-left: 6px solid #229954;
  box-shadow: 0 4px 12px rgba(39, 174, 96, 0.15);
  position: relative;
  animation: completionGlow 3s ease-in-out;
}

.completion-message .message-content {
  font-weight: 600;
  font-size: 14px;
  line-height: 1.5;
  color: #229954;
}

.completion-message .message-time {
  color: #27ae60;
  font-weight: 500;
}

@keyframes completionGlow {
  0%, 100% { 
    box-shadow: 0 4px 12px rgba(39, 174, 96, 0.15);
  }
  50% { 
    box-shadow: 0 6px 20px rgba(39, 174, 96, 0.3);
  }
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
  
  .status-indicator {
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
  
  .status-indicator {
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
  
  .status-indicator {
    min-width: auto;
  }
  
  .step-message .message-content {
    font-size: 13px;
  }
  
  .thinking-message .message-content {
    font-size: 13px;
  }
  
     .error-message .message-content,
   .tool-message .message-content,
   .result-message .message-content,
   .completion-message .message-content {
     font-size: 13px;
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
</style>
