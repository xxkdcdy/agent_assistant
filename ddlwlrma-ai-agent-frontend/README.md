# DDLWLMA AI Agent Frontend

一个基于Vue 3的多功能AI智能体前端应用，支持多环境部署和动态API配置。

## 功能特性

- 🚀 基于Vue 3 + Vite构建
- 🔄 支持SSE (Server-Sent Events) 实时通信
- 🌍 多环境配置支持 (开发/生产)
- 📱 响应式设计，支持PC、平板、手机
- 🎯 多种AI应用集成
- 🎨 美观的现代化UI设计

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

## AI应用介绍

### 🤖 "世另我"AI智能体
- **功能**: 强大的AI助手，提供全方位的智能服务和解决方案
- **特色**: 支持多种消息类型（思考、步骤、工具、结果等）
- **主题**: 蓝色科技风格

### 🐢 海龟汤游戏
- **功能**: 经典的推理游戏，通过提问来猜出谜底
- **特色**: 考验逻辑思维能力，支持实时对话
- **主题**: 绿色自然风格

### 🕊️ GitHub助手
- **功能**: 专业的GitHub使用助手，解决代码管理相关问题
- **特色**: 提供代码审查、Git操作指导、项目管理建议
- **主题**: 金黄色专业风格

## 项目结构

```
ddlwlrma-ai-agent-frontend/
├── src/
│   ├── components/          # Vue组件
│   │   └── WebsiteAvatar.vue # 网站头像组件
│   ├── views/              # 页面视图
│   │   ├── Home.vue        # 首页
│   │   ├── ManusChat.vue   # AI智能体聊天
│   │   ├── TurtleSoupChat.vue # 海龟汤游戏
│   │   └── GitHubHelperChat.vue # GitHub助手
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
- **样式**: CSS3 + 响应式设计
- **部署**: Nginx反向代理

## 开发指南

### 添加新的AI应用

1. 在 `src/services/api.js` 中添加新的SSE接口函数
2. 在 `src/views/` 目录下创建新的聊天页面组件
3. 在 `src/router/index.js` 中添加路由配置
4. 在 `src/views/Home.vue` 中添加应用卡片和导航方法

### 样式主题

每个AI应用都有独特的主题色：
- AI智能体: 蓝色科技风格
- 海龟汤游戏: 绿色自然风格  
- GitHub助手: 金黄色专业风格

## 许可证

MIT License
