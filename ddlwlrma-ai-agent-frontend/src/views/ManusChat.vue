<template>
  <div class="chat-container">
    <div class="chat-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h2>"世另我"AI 超级智能体 🤖</h2>
      <div class="status-indicator" :class="{ connected: isConnected }">
        {{ isConnected ? '已连接' : '未连接' }}
      </div>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="message in messages" :key="message.id" :class="['message', message.type, { 
        'step-message': message.isStep, 
        'thinking-message': message.isThinking,
        'error-message': message.isError,
        'tool-message': message.isTool,
        'completion-message': message.isCompletion
      }]">
        <div class="message-bubble" :class="{ 
          'step-bubble': message.isStep, 
          'thinking-bubble': message.isThinking,
          'error-bubble': message.isError,
          'tool-bubble': message.isTool,
          'completion-bubble': message.isCompletion
        }">
          <div class="message-content" v-html="formatMessage(message.content)"></div>
          <div class="message-time">{{ formatTime(message.timestamp) }}</div>
        </div>
      </div>
      
      <div v-if="isLoading" class="message ai">
        <div class="message-bubble">
          <div class="loading">AI超级智能体正在分析处理中...</div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <div class="input-group">
        <input 
          v-model="inputMessage" 
          @keypress.enter="sendMessage"
          placeholder="请输入您的问题，AI超级智能体将为您提供全方位的智能服务..."
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
      connectionId: null,      // 连接ID，用于识别连接
      processedSteps: new Set() // 已处理的步骤ID
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
        content: '你好！我是"世另我"AI超级智能体🤖 我拥有强大的多模态能力，可以为您提供：\n\n• 📝 文本创作与编辑\n• 🔍 信息查询与分析\n• 💡 问题解决方案\n• 🎯 专业建议与指导\n• 🌐 多领域知识支持\n\n请告诉我您需要什么帮助！',
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
      this.processedSteps.clear()
      
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
        this.processedSteps.clear()
        this.isProcessingSSE = true
        
        // 创建新的SSE连接
        this.sseConnection = startManusChatSSE(
          messageToSend,
          (data) => this.handleSSEMessage(data, newConnectionId),
          (error) => this.handleSSEError(error, newConnectionId),
          () => this.handleSSEOpen(newConnectionId),
          () => this.handleSSEClose(newConnectionId)
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
        
        // Spring AI直接发送内容，不需要处理特殊前缀
        let processedData = data.trim()
        
        // 跳过空数据
        if (!processedData) {
          return
        }
        
        // 检查是否是特殊类型消息
        if (this.handleSpecialMessage(processedData)) {
          return
        }
        
        // 将数据添加到步骤缓冲区
        this.currentStepBuffer += processedData
        
        // 检查是否有完整的步骤
        this.processStepBuffer()
        
        this.scrollToBottom()
        
      } catch (error) {
        console.error('处理SSE消息错误:', error)
        // 只有在真正的错误情况下才调用错误处理
        if (this.isLoading) {
          this.handleSSEError(error, connectionId)
        }
      }
    },
    
    processStepBuffer() {
      // 简单的方法：当遇到新的Step时，将前面的内容作为完整步骤处理
      const lines = this.currentStepBuffer.split('\n')
      let pendingStep = ''
      let processedLines = 0
      
      for (let i = 0; i < lines.length; i++) {
        const line = lines[i]
        
        // 检查是否是新步骤的开始
        if (line.match(/^Step \d+:/)) {
          // 如果有待处理的步骤，先处理它
          if (pendingStep.trim()) {
            this.addStepMessage(pendingStep.trim())
          }
          // 开始新的步骤
          pendingStep = line
          processedLines = i
        } else {
          // 累加到当前步骤
          pendingStep += '\n' + line
        }
        
        // 检查是否是完整的步骤（包含结束标志）
        if (pendingStep.includes('完成了它的任务') || pendingStep.includes('任务结束')) {
          this.addStepMessage(pendingStep.trim())
          pendingStep = ''
          processedLines = i + 1
        }
      }
      
      // 清理已处理的内容
      if (processedLines > 0) {
        const remainingLines = lines.slice(processedLines)
        this.currentStepBuffer = remainingLines.join('\n')
        console.log('处理了', processedLines, '行，剩余缓冲区内容:', this.currentStepBuffer.length, '字符')
      }
    },
    
    addStepMessage(content) {
      // 生成步骤唯一标识
      const stepId = content.substring(0, 30).replace(/\s+/g, ' ')
      
      // 检查是否已处理过这个步骤
      if (this.processedSteps.has(stepId)) {
        console.log('跳过重复步骤:', stepId)
        return
      }
      
      // 避免重复添加相同的步骤
      const isDuplicate = this.stepMessages.some(msg => 
        msg.content.substring(0, 30).replace(/\s+/g, ' ') === stepId
      )
      
      if (!isDuplicate) {
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
        this.processedSteps.add(stepId)
        console.log('添加步骤消息:', stepId)
      } else {
        console.log('跳过重复步骤:', stepId)
      }
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
      if (this.stepMessages.length > 0) {
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
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.chat-header h2 {
  flex: 1;
  text-align: center;
  margin: 0;
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

.step-message .step-bubble::before {
  content: "🔧";
  position: absolute;
  top: 8px;
  left: 8px;
  font-size: 14px;
  opacity: 0.7;
}

.step-message .message-content {
  margin-left: 25px;
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

.thinking-message .thinking-bubble::before {
  content: "💭";
  position: absolute;
  top: 8px;
  left: 8px;
  font-size: 14px;
  opacity: 0.8;
}

.thinking-message .message-content {
  margin-left: 25px;
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

.error-message .error-bubble::before {
  content: "❌";
  position: absolute;
  top: 8px;
  left: 8px;
  font-size: 14px;
  opacity: 0.8;
}

.error-message .message-content {
  margin-left: 25px;
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

.tool-message .tool-bubble::before {
  content: "🔧";
  position: absolute;
  top: 8px;
  left: 8px;
  font-size: 14px;
  opacity: 0.8;
}

.tool-message .message-content {
  margin-left: 25px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #34495e;
}

.tool-message .message-time {
  color: #5d6d7e;
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

.completion-message .completion-bubble::before {
  content: "✅";
  position: absolute;
  top: 8px;
  left: 8px;
  font-size: 14px;
  opacity: 0.8;
}

.completion-message .message-content {
  margin-left: 25px;
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

@media (max-width: 768px) {
  .chat-header {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
  
  .status-indicator {
    min-width: auto;
  }
  
  .step-message .message-content {
    margin-left: 20px;
    font-size: 13px;
  }
  
  .thinking-message .message-content {
    margin-left: 20px;
    font-size: 13px;
  }
  
  .error-message .message-content,
  .tool-message .message-content,
  .completion-message .message-content {
    margin-left: 20px;
    font-size: 13px;
  }
}
</style>
