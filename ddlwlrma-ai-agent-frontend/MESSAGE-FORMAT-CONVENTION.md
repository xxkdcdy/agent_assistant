# 消息格式约定规范

## 📋 约定前缀规范

为了统一消息处理，我们约定以下前缀规范（类似协议）：

| 前缀 | 类型 | 气泡样式 | 说明 |
|------|------|----------|------|
| `💭 THINKING:` | 思考类消息 | `thinking-bubble` | AI的思考过程 |
| `⚙️ STEP:` | 执行步骤 | `step-bubble` | 具体的执行步骤 |
| `🛠 TOOL:` | 工具调用 | `tool-bubble` | 工具调用信息 |
| `📊 RESULT:` | 工具/步骤结果 | `result-bubble` | 执行结果 |
| `✅ COMPLETION:` | 最终回答 | `completion-bubble` | 任务完成 |
| `❌ ERROR:` | 错误消息 | `error-bubble` | 错误信息 |

## 🔧 实现细节

### 1. 消息分割逻辑

```javascript
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
      // 思考消息
      if (currentMessage.trim()) {
        this.addTypedMessage(currentMessage.trim(), messageType)
      }
      currentMessage = line
      messageType = 'thinking'
      processedLines = i
    } else if (line.startsWith('⚙️ STEP:') || line.startsWith('⚙️ 步骤:')) {
      // 步骤消息
      if (currentMessage.trim()) {
        this.addTypedMessage(currentMessage.trim(), messageType)
      }
      currentMessage = line
      messageType = 'step'
      processedLines = i
    } else if (line.startsWith('🛠 TOOL:') || line.startsWith('🛠 工具:')) {
      // 工具消息
      if (currentMessage.trim()) {
        this.addTypedMessage(currentMessage.trim(), messageType)
      }
      currentMessage = line
      messageType = 'tool'
      processedLines = i
    } else if (line.startsWith('📊 RESULT:') || line.startsWith('📊 结果:')) {
      // 结果消息
      if (currentMessage.trim()) {
        this.addTypedMessage(currentMessage.trim(), messageType)
      }
      currentMessage = line
      messageType = 'result'
      processedLines = i
    } else if (line.startsWith('✅ COMPLETION:') || line.startsWith('✅ 完成:')) {
      // 完成消息
      if (currentMessage.trim()) {
        this.addTypedMessage(currentMessage.trim(), messageType)
      }
      currentMessage = line
      messageType = 'completion'
      processedLines = i
    } else if (line.startsWith('❌ ERROR:') || line.startsWith('❌ 错误:')) {
      // 错误消息
      if (currentMessage.trim()) {
        this.addTypedMessage(currentMessage.trim(), messageType)
      }
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
}
```

### 2. 消息类型处理

```javascript
addTypedMessage(content, messageType) {
  // 生成消息唯一标识
  const messageId = content.substring(0, 30).replace(/\s+/g, ' ')
  
  // 检查是否已处理过这个消息
  if (this.processedSteps.has(messageId)) {
    console.log('跳过重复消息:', messageId)
    return
  }
  
  // 避免重复添加相同的消息
  const isDuplicate = this.messages.some(msg => 
    msg.content.substring(0, 30).replace(/\s+/g, ' ') === messageId
  )
  
  if (!isDuplicate) {
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
    this.processedSteps.add(messageId)
    console.log('添加', messageType, '消息:', messageId)
  } else {
    console.log('跳过重复消息:', messageId)
  }
}
```

## 🎨 气泡样式对应

### 思考消息 (thinking-bubble)
- **颜色**: 橙色渐变
- **图标**: 💭
- **边框**: 橙色边框

### 步骤消息 (step-bubble)
- **颜色**: 蓝色渐变
- **图标**: ⚙️
- **边框**: 蓝色边框

### 工具消息 (tool-bubble)
- **颜色**: 灰色渐变
- **图标**: 🛠
- **边框**: 深灰色边框

### 结果消息 (result-bubble)
- **颜色**: 绿色渐变
- **图标**: 📊
- **边框**: 绿色边框

### 完成消息 (completion-bubble)
- **颜色**: 深绿色渐变
- **图标**: ✅
- **边框**: 深绿色边框

### 错误消息 (error-bubble)
- **颜色**: 红色渐变
- **图标**: ❌
- **边框**: 红色边框

## 📝 使用示例

### 后端发送的消息格式示例：

```
💭 THINKING: 正在分析用户需求...
⚙️ STEP: 开始执行邮件查询任务
🛠 TOOL: 调用 getUnreadEmails 工具
📊 RESULT: 找到1封未读邮件
✅ COMPLETION: 任务完成，已为您查询到未读邮件
```

### 或者中文格式：

```
💭 思考: 正在分析用户需求...
⚙️ 步骤: 开始执行邮件查询任务
🛠 工具: 调用 getUnreadEmails 工具
📊 结果: 找到1封未读邮件
✅ 完成: 任务完成，已为您查询到未读邮件
```

## 🔄 特殊处理

### [END_CONVERSATION] 标记
- 仍然会在结束前接收到 `[END_CONVERSATION]` 标记
- 该标记会触发 `handleSSEComplete` 方法
- 确保对话正常结束

### 兼容性
- 支持英文和中文前缀
- 保持向后兼容性
- 自动识别消息类型

## 🧪 测试验证

### 测试步骤
1. 启动开发服务器：`npm run dev`
2. 访问世另我智能体页面
3. 发送测试消息
4. 观察消息气泡是否正确分割和显示

### 预期效果
- 每种消息类型都有对应的气泡样式
- 消息按前缀正确分割
- 视觉效果清晰，用户体验良好

## 📋 修复文件清单

- `src/views/ManusChat.vue` - 主要修复文件
  - `processStepBuffer` 方法更新
  - `addTypedMessage` 方法更新

## 🔍 关键改进点

1. **标准化**: 统一的消息格式约定
2. **精确识别**: 基于前缀的精确消息类型识别
3. **视觉区分**: 每种消息类型都有独特的视觉样式
4. **用户体验**: 清晰展示AI的工作流程

---

**约定版本**: v1.2.0  
**约定日期**: 2024-01-XX  
**影响范围**: 消息处理、用户体验
