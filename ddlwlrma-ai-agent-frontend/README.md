# DDLWLMA AI Agent Frontend

一个基于Vue 3的AI智能体前端应用，支持多环境部署和动态API配置。

## 功能特性

- 🚀 基于Vue 3 + Vite构建
- 🔄 支持SSE (Server-Sent Events) 实时通信
- 🌍 多环境配置支持 (开发/生产)
- 📱 响应式设计
- 🎯 智能体聊天功能

## 快速开始

### 安装依赖
```bash
npm install
```

### 开发环境
```bash
npm run dev
```
开发环境将连接到 `http://localhost:8123/api`

### 生产环境构建
```bash
# 构建生产版本
npm run build:prod

# 或使用默认构建
npm run build
```

## 环境配置

项目支持多环境部署，通过环境变量自动配置API基础URL：

### 开发环境
- API基础URL: `http://localhost:8123/api`
- 启动命令: `npm run dev`
- 构建命令: `npm run build:dev`

### 生产环境
- API基础URL: `/api` (通过nginx代理到后端)
- 构建命令: `npm run build:prod`
- 部署目录: `dist/`

## 项目结构

```
ddlwlrma-ai-agent-frontend/
├── src/
│   ├── components/          # Vue组件
│   ├── views/              # 页面视图
│   ├── services/           # API服务
│   │   └── api.js         # API配置和SSE连接
│   └── router/            # 路由配置
├── env.config.js          # 环境配置
├── build.js              # 构建脚本
├── vite.config.js        # Vite配置
└── nginx.conf           # Nginx配置示例
```

## API配置

项目使用动态API配置，根据环境自动切换：

```javascript
// 根据环境变量设置 API 基础 URL
const API_BASE_URL = process.env.NODE_ENV === 'production' 
 ? '/api' // 生产环境使用相对路径，nginx会代理到后端服务
 : 'http://localhost:8123/api' // 开发环境指向本地后端服务
```

## 部署说明

详细的部署说明请参考 [DEPLOYMENT.md](./DEPLOYMENT.md)

### 关键配置

1. **开发环境**: 直接连接本地后端服务
2. **生产环境**: 使用nginx反向代理，前端请求 `/api` 路径
3. **SSE支持**: nginx配置支持Server-Sent Events长连接

## 测试配置

运行配置测试脚本：
```bash
node test-config.js
```

## 技术栈

- **前端框架**: Vue 3
- **构建工具**: Vite
- **HTTP客户端**: Axios
- **路由**: Vue Router 4
- **实时通信**: Server-Sent Events (SSE)

## 许可证

MIT License
