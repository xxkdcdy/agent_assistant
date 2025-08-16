// 测试环境配置脚本
import { API_BASE_URL, ENV_INFO } from './env.config.js'

console.log('🔧 环境配置测试')
console.log('================')
console.log(`环境模式: ${ENV_INFO.mode}`)
console.log(`是否生产环境: ${ENV_INFO.isProduction}`)
console.log(`是否开发环境: ${ENV_INFO.isDevelopment}`)
console.log(`API基础URL: ${API_BASE_URL}`)
console.log('================')

// 验证配置
if (ENV_INFO.isProduction && API_BASE_URL !== '/api') {
  console.error('❌ 生产环境API基础URL配置错误')
  process.exit(1)
}

if (ENV_INFO.isDevelopment && !API_BASE_URL.includes('localhost:8123')) {
  console.error('❌ 开发环境API基础URL配置错误')
  process.exit(1)
}

console.log('✅ 环境配置验证通过！')

