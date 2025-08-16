import axios from 'axios'
import { API_BASE_URL } from '../../env.config.js'

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000
})

// 生成唯一的聊天ID
export function generateChatId() {
  return 'chat_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

// SSE连接类 - 支持新的complete事件
export class SSEConnection {
  constructor(url, onMessage, onError, onOpen, onClose, onComplete) {
    this.url = url
    this.onMessage = onMessage
    this.onError = onError
    this.onOpen = onOpen
    this.onClose = onClose
    this.onComplete = onComplete
    this.eventSource = null
    this.hasReceivedData = false
    this.isClosed = false // 防止自动重连的关键标志
  }

  connect() {
    try {
      // 如果已经标记为关闭，不创建新连接
      if (this.isClosed) {
        console.log('连接已关闭，跳过重新连接')
        return
      }
      
      this.eventSource = new EventSource(this.url)
      
      this.eventSource.onopen = (event) => {
        console.log('SSE连接已建立')
        if (this.onOpen) {
          this.onOpen(event)
        }
      }
      
      // 普通消息
      this.eventSource.addEventListener("message", (event) => {
        console.log('收到SSE消息:', event.data)
        this.hasReceivedData = true
        if (this.onMessage) {
          this.onMessage(event.data)
        }
      })
      
      // 完成标记
      this.eventSource.addEventListener("complete", (event) => {
        console.log('流完成:', event.data)
        this.isClosed = true
        if (this.onComplete) {
          this.onComplete(event.data)
        }
        this.close()
      })
      
      this.eventSource.onerror = (event) => {
        console.log('SSE事件触发，readyState:', this.eventSource.readyState, 'hasReceivedData:', this.hasReceivedData, 'isClosed:', this.isClosed)
        
        // 如果已经手动关闭，忽略所有错误事件
        if (this.isClosed) {
          console.log('连接已手动关闭，忽略错误事件')
          return
        }
        
        // Spring AI流结束时会关闭连接，这是正常的
        if (this.eventSource.readyState === EventSource.CLOSED) {
          if (this.hasReceivedData) {
            console.log('Spring AI流正常结束，标记为已关闭防止重连')
            this.isClosed = true // 关键：防止浏览器自动重连
            if (this.onClose) {
              this.onClose()
            }
            return
          }
        }
        
        // 只有在真正的错误情况下才调用错误处理
        console.error('SSE连接真正错误:', event)
        if (this.onError && !this.isClosed) {
          this.onError(event)
        }
      }
    } catch (error) {
      console.error('创建SSE连接失败:', error)
      if (this.onError) {
        this.onError(error)
      }
    }
  }

  close() {
    console.log('主动关闭SSE连接')
    this.isClosed = true // 防止自动重连
    if (this.eventSource) {
      this.eventSource.close()
      this.eventSource = null
    }
  }
}

// 恋爱大师聊天API - 更新接口路径
export function startLoveChatSSE(message, chatId, onMessage, onError, onOpen, onClose, onComplete) {
  const url = `${API_BASE_URL}/ai/love_app/chat/sse/emitter?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`
  return new SSEConnection(url, onMessage, onError, onOpen, onClose, onComplete)
}

// 世另我智能体聊天API - 添加complete事件支持
export function startManusChatSSE(message, onMessage, onError, onOpen, onClose, onComplete) {
  const url = `${API_BASE_URL}/ai/manus/chat?message=${encodeURIComponent(message)}`
  return new SSEConnection(url, onMessage, onError, onOpen, onClose, onComplete)
}

export default api

