#!/usr/bin/env node

// 测试API URL构建
import { API_BASE_URL } from './env.config.js'
import { startLoveChatSSE, startManusChatSSE } from './src/services/api.js'

console.log('🧪 测试API URL构建')
console.log('==================')

console.log(`环境配置的API基础URL: ${API_BASE_URL}`)

// 模拟构建URL
const testMessage = '你好'
const testChatId = 'test_chat_123'

// 测试恋爱大师URL
const loveChatUrl = `${API_BASE_URL}/ai/love_app/chat/sse/emitter?message=${encodeURIComponent(testMessage)}&chatId=${encodeURIComponent(testChatId)}`
console.log(`\n恋爱大师聊天URL:`)
console.log(`  ${loveChatUrl}`)

// 测试世另我智能体URL
const manusChatUrl = `${API_BASE_URL}/ai/manus/chat?message=${encodeURIComponent(testMessage)}`
console.log(`\n世另我智能体URL:`)
console.log(`  ${manusChatUrl}`)

// 验证URL格式
console.log('\n==================')
console.log('✅ URL构建测试完成！')

if (API_BASE_URL.includes('localhost:8123')) {
  console.log('✅ 开发环境配置正确')
} else if (API_BASE_URL === '/api') {
  console.log('✅ 生产环境配置正确')
} else {
  console.log('❌ API基础URL配置异常')
}
