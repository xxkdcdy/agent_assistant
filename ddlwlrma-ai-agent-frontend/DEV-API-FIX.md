# 开发环境API地址修复说明

## 🔧 问题描述

开发环境中，前端请求的API地址不正确：
- **错误地址**: `http://127.0.0.1:3000/api/love_app/chat/sse/emitter`
- **正确地址**: `http://localhost:8123/api/love_app/chat/sse/emitter`

导致404错误和连接失败。

## ✅ 修复内容

### 1. API服务文件修复 (`src/services/api.js`)

#### 问题原因
API服务文件没有使用环境配置的API基础URL，而是硬编码了 `/api` 路径。

#### 修复方案
```javascript
// 修复前
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',  // 硬编码路径
  timeout: 30000
})

export function startLoveChatSSE(message, chatId, onMessage, onError, onOpen, onClose, onComplete) {
  const url = `/api/love_app/chat/sse/emitter?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`
  return new SSEConnection(url, onMessage, onError, onOpen, onClose, onComplete)
}

// 修复后
import axios from 'axios'
import { API_BASE_URL } from '../../env.config.js'

const api = axios.create({
  baseURL: API_BASE_URL,  // 使用环境配置
  timeout: 30000
})

export function startLoveChatSSE(message, chatId, onMessage, onError, onOpen, onClose, onComplete) {
  const url = `${API_BASE_URL}/ai/love_app/chat/sse/emitter?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`
  return new SSEConnection(url, onMessage, onError, onOpen, onClose, onComplete)
}
```

### 2. 环境配置验证

#### 开发环境配置
```javascript
// env.config.js
export const getApiBaseUrl = () => {
  // 检查是否在Vite环境中
  if (typeof import.meta !== 'undefined' && import.meta.env) {
    // 优先使用Vite环境变量
    if (import.meta.env.VITE_API_BASE_URL) {
      return import.meta.env.VITE_API_BASE_URL
    }
    
    // 根据环境变量设置 API 基础 URL
    return import.meta.env.PROD 
      ? '/api' // 生产环境使用相对路径，nginx会代理到后端服务
      : 'http://localhost:8123/api' // 开发环境指向本地后端服务
  }
  
  // Node.js环境下的默认配置
  const nodeEnv = process.env.NODE_ENV || 'development'
  return nodeEnv === 'production' 
    ? '/api' 
    : 'http://localhost:8123/api'
}
```

## 🧪 验证修复

### 1. 测试API URL构建
```bash
node test-api-url.js
```

**预期输出**:
```
环境配置的API基础URL: http://localhost:8123/api

恋爱大师聊天URL:
  http://localhost:8123/api/ai/love_app/chat/sse/emitter?message=%E4%BD%A0%E5%A5%BD&chatId=test_chat_123

世另我智能体URL:
  http://localhost:8123/api/ai/manus/chat?message=%E4%BD%A0%E5%A5%BD

✅ 开发环境配置正确
```

### 2. 开发环境测试
```bash
npm run dev
```

访问应用并测试聊天功能，确认：
- 不再出现404错误
- 正确连接到 `localhost:8123`
- SSE连接正常建立

## 📋 修复文件清单

- `src/services/api.js` - 主要修复文件
- `test-api-url.js` - 测试脚本（新增）

## 🔍 故障排除

### 如果问题仍然存在：

1. **检查后端服务**
   ```bash
   # 确认后端服务在8123端口运行
   curl http://localhost:8123/api/health
   ```

2. **检查环境变量**
   ```bash
   # 确认NODE_ENV设置
   echo $NODE_ENV
   ```

3. **检查网络连接**
   ```bash
   # 测试端口连通性
   telnet localhost 8123
   ```

4. **检查浏览器控制台**
   - 确认请求URL正确
   - 检查是否有CORS错误

## 📞 技术支持

如果修复后仍有问题，请检查：
1. 后端服务是否正常运行在8123端口
2. 防火墙是否阻止了8123端口
3. 浏览器控制台的具体错误信息

---

**修复版本**: v1.1.1  
**修复日期**: 2024-01-XX  
**影响范围**: 开发环境API连接
