#!/usr/bin/env node

import { execSync } from 'child_process'
import fs from 'fs'
import path from 'path'

// 构建脚本 - 支持不同环境的构建
const args = process.argv.slice(2)
const env = args[0] || 'production'

console.log(`开始构建 ${env} 环境...`)

// 设置环境变量
process.env.NODE_ENV = env
process.env.VITE_MODE = env

// 根据环境设置API基础URL
if (env === 'production') {
  process.env.VITE_API_BASE_URL = '/api'
} else if (env === 'development') {
  process.env.VITE_API_BASE_URL = 'http://localhost:8123/api'
} else if (env === 'staging') {
  // 如果需要测试环境，可以在这里配置
  process.env.VITE_API_BASE_URL = '/api'
}

try {
  // 执行构建命令
  execSync('npm run build', { stdio: 'inherit' })
  
  console.log(`✅ ${env} 环境构建完成！`)
  console.log(`📁 构建输出目录: dist/`)
  console.log(`🌐 API基础URL: ${process.env.VITE_API_BASE_URL}`)
  
} catch (error) {
  console.error(`❌ 构建失败:`, error.message)
  process.exit(1)
}

