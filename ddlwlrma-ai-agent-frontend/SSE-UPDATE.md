# SSE接口更新部署说明

## 🔧 更新概述

根据后端接口变更，前端已更新为支持新的SSE接口格式：

### 接口变更
- **恋爱大师聊天接口**: `GET /ai/love_app/chat/sse` → `GET /love_app/chat/sse/emitter`
- **完成标记**: 后端发送 `[END_CONVERSATION]` 消息来标记对话结束
- **事件类型**: 支持 `message`、`error`、`complete` 三种事件类型

## ✅ 更新内容

### 1. API服务更新 (`src/services/api.js`)

#### SSE连接类改进
```javascript
// 支持新的complete事件
export class SSEConnection {
  constructor(url, onMessage, onError, onOpen, onClose, onComplete) {
    // 新增 onComplete 回调
  }
  
  connect() {
    // 普通消息
    this.eventSource.addEventListener("message", (event) => {
      // 处理普通消息
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
  }
}
```

#### 接口路径更新
```javascript
// 恋爱大师聊天API - 更新接口路径
export function startLoveChatSSE(message, chatId, onMessage, onError, onOpen, onClose, onComplete) {
  const url = `/api/love_app/chat/sse/emitter?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`
  return new SSEConnection(url, onMessage, onError, onOpen, onClose, onComplete)
}

// 世另我智能体聊天API - 添加complete事件支持
export function startManusChatSSE(message, onMessage, onError, onOpen, onClose, onComplete) {
  const url = `/api/ai/manus/chat?message=${encodeURIComponent(message)}`
  return new SSEConnection(url, onMessage, onError, onOpen, onClose, onComplete)
}
```

### 2. LoveChat页面更新 (`src/views/LoveChat.vue`)

#### 添加complete事件处理
```javascript
// 创建SSE连接时添加complete回调
this.sseConnection = startLoveChatSSE(
  messageToSend,
  this.chatId,
  (data) => this.handleSSEMessage(data),
  (error) => this.handleSSEError(error),
  () => this.handleSSEOpen(),
  () => this.handleSSEClose(),
  (data) => this.handleSSEComplete(data)  // 新增
)

// 新增complete事件处理方法
handleSSEComplete(data) {
  console.log('Spring AI流式传输完成，最终消息长度:', this.currentAIMessage.length)
  this.isLoading = false
  this.isProcessingSSE = false
  this.isConnected = false
  
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
}
```

### 3. ManusChat页面更新 (`src/views/ManusChat.vue`)

#### 添加complete事件支持
```javascript
// 创建SSE连接时添加complete回调
this.sseConnection = startManusChatSSE(
  messageToSend,
  (data) => this.handleSSEMessage(data, newConnectionId),
  (error) => this.handleSSEError(error, newConnectionId),
  () => this.handleSSEOpen(newConnectionId),
  () => this.handleSSEClose(newConnectionId),
  (data) => this.handleSSEComplete(data, newConnectionId)  // 新增
)

// 新增complete事件处理方法
handleSSEComplete(data, connectionId) {
  // 检查连接ID是否匹配
  if (connectionId && this.connectionId !== connectionId) {
    console.log('忽略过期连接的Complete事件:', connectionId, '当前连接:', this.connectionId)
    return
  }
  
  console.log('Spring AI流式传输完成，连接ID:', connectionId)
  
  // 处理剩余的缓冲区内容
  if (this.currentStepBuffer.trim()) {
    this.addStepMessage(`Step ${this.stepMessages.length + 1}: ${this.currentStepBuffer.trim()}`)
  }
  
  this.isLoading = false
  this.isConnected = false
  this.isProcessingSSE = false
  
  // 确保连接完全关闭
  if (this.sseConnection) {
    this.sseConnection.close()
    this.sseConnection = null
  }
  
  console.log('智能体任务完成，共', this.stepMessages.length, '个步骤')
}
```

## 🚀 部署步骤

### 1. 构建更新版本
```bash
npm run build:prod
```

### 2. 部署到服务器
将 `dist/` 目录的内容复制到nginx服务器的静态文件目录：
```bash
# 示例：复制到nginx默认目录
cp -r dist/* /usr/share/nginx/html/
```

### 3. 重启nginx服务
```bash
sudo systemctl restart nginx
# 或
sudo service nginx restart
```

## 🧪 验证更新

### 1. 测试恋爱大师聊天
- 访问LoveChat页面
- 发送消息测试新的接口路径
- 观察是否正确接收到complete事件

### 2. 测试世另我智能体
- 访问ManusChat页面
- 发送消息测试complete事件处理
- 观察是否正确结束思考状态

### 3. 检查控制台日志
在浏览器开发者工具的控制台中，应该能看到：
```
流完成: [END_CONVERSATION]
Spring AI流式传输完成，最终消息长度: XXX
标记消息传输完成，最终内容长度: XXX
```

## 📋 更新文件清单

- `src/services/api.js` - SSE连接类和API函数更新
- `src/views/LoveChat.vue` - 恋爱大师页面complete事件支持
- `src/views/ManusChat.vue` - 世另我智能体页面complete事件支持

## 🔍 故障排除

### 如果更新后出现问题：

1. **检查接口路径**
   - 确认后端已更新为新的接口路径
   - 检查nginx配置是否正确代理新路径

2. **检查complete事件**
   - 确认后端正确发送complete事件
   - 检查浏览器控制台是否有相关日志

3. **检查网络连接**
   - 确保SSE连接正常建立
   - 检查防火墙和代理设置

## 📞 技术支持

如果部署后仍有问题，请检查：
1. 浏览器控制台是否有错误信息
2. 网络面板中SSE连接的状态
3. 后端服务日志
4. nginx错误日志

---

**更新版本**: v1.1.0  
**更新日期**: 2024-01-XX  
**影响范围**: SSE连接处理、接口路径、事件处理机制
