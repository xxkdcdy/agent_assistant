#!/usr/bin/env node

import fs from 'fs'
import path from 'path'
import { API_BASE_URL, ENV_INFO } from './env.config.js'

console.log('🔍 项目配置验证')
console.log('================')

// 检查关键文件是否存在
const requiredFiles = [
  'package.json',
  'vite.config.js',
  'env.config.js',
  'src/services/api.js',
  'src/App.vue',
  'index.html'
]

console.log('\n📁 检查关键文件:')
let allFilesExist = true
requiredFiles.forEach(file => {
  const exists = fs.existsSync(file)
  console.log(`${exists ? '✅' : '❌'} ${file}`)
  if (!exists) allFilesExist = false
})

// 检查环境配置
console.log('\n🌍 环境配置:')
console.log(`环境模式: ${ENV_INFO.mode}`)
console.log(`是否生产环境: ${ENV_INFO.isProduction}`)
console.log(`是否开发环境: ${ENV_INFO.isDevelopment}`)
console.log(`API基础URL: ${API_BASE_URL}`)

// 验证API配置
let apiConfigValid = true
if (ENV_INFO.isProduction && API_BASE_URL !== '/api') {
  console.log('❌ 生产环境API基础URL配置错误')
  apiConfigValid = false
}

if (ENV_INFO.isDevelopment && !API_BASE_URL.includes('localhost:8123')) {
  console.log('❌ 开发环境API基础URL配置错误')
  apiConfigValid = false
}

if (apiConfigValid) {
  console.log('✅ API配置验证通过')
}

// 检查package.json脚本
console.log('\n📦 检查构建脚本:')
try {
  const packageJson = JSON.parse(fs.readFileSync('package.json', 'utf8'))
  const scripts = packageJson.scripts || {}
  
  const requiredScripts = ['dev', 'build', 'build:prod', 'build:dev']
  requiredScripts.forEach(script => {
    const exists = scripts[script]
    console.log(`${exists ? '✅' : '❌'} npm run ${script}`)
  })
} catch (error) {
  console.log('❌ 无法读取package.json')
}

// 检查nginx配置
console.log('\n🌐 检查nginx配置:')
if (fs.existsSync('nginx.conf')) {
  const nginxConfig = fs.readFileSync('nginx.conf', 'utf8')
  const hasApiProxy = nginxConfig.includes('/api/')
  const hasSSEConfig = nginxConfig.includes('proxy_buffering off')
  
  console.log(`${hasApiProxy ? '✅' : '❌'} API代理配置`)
  console.log(`${hasSSEConfig ? '✅' : '❌'} SSE配置`)
} else {
  console.log('⚠️  nginx.conf文件不存在')
}

// 最终验证结果
console.log('\n================')
if (allFilesExist && apiConfigValid) {
  console.log('🎉 项目配置验证通过！')
  console.log('\n📋 下一步操作:')
  console.log('1. 开发环境: npm run dev')
  console.log('2. 生产构建: npm run build:prod')
  console.log('3. 部署: 将dist/目录部署到nginx服务器')
} else {
  console.log('❌ 项目配置存在问题，请检查上述错误')
  process.exit(1)
}

