# 🚀 DDLWLRMA AI Agent Frontend

一个基于 Vue3 的现代化AI聊天前端项目，提供两个专业的AI应用聊天界面：AI恋爱大师和"世另我"AI智能体。项目采用响应式设计，支持PC、平板和移动端，提供流畅的实时对话体验。

![Vue](https://img.shields.io/badge/Vue-3.4+-green.svg)
![Vite](https://img.shields.io/badge/Vite-5.0+-blue.svg)
![Node](https://img.shields.io/badge/Node-16+-orange.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

## ✨ 功能特色

### 🏠 智能主页
- 🎨 现代化渐变设计，美观的应用选择界面
- 📱 完全响应式设计，完美适配PC、平板、移动端
- 🎯 卡片式布局，直观的应用导航
- 🚀 网站专属头像和品牌标识
- 📋 完整的网站信息展示

### 💕 AI恋爱大师 (LoveChat)
- 💬 专业的恋爱咨询和情感建议服务
- ⚡ 实时流式对话，即时显示AI回复
- 🔄 自动生成会话ID，区分不同对话
- 🎭 专属头像和个性化界面
- 📊 实时连接状态监控
- 🎨 优雅的消息气泡设计

### 🤖 "世另我"AI智能体 (ManusChat)
- 🌟 全方位的智能服务支持
- 🔧 强大的多模态AI能力
- 📝 文本创作与编辑
- 🔍 信息查询与分析
- 💡 问题解决方案
- 🎯 专业建议与指导
- 🌐 多领域知识支持
- ⚡ 实时思考过程展示
- 🔄 步骤化任务执行

## 🛠️ 技术架构

### 前端技术栈
- **Vue 3** - 渐进式JavaScript框架
- **Vue Router** - 官方路由管理器
- **Vite** - 下一代前端构建工具
- **Axios** - 基于Promise的HTTP客户端
- **SSE (Server-Sent Events)** - 实时单向通信
- **CSS3** - 现代CSS特性（Grid、Flexbox、动画）

### 核心特性
- 🎯 **组件化架构** - 高度模块化的组件设计
- 🔄 **响应式数据** - Vue3 Composition API
- ⚡ **实时通信** - SSE流式数据传输
- 📱 **移动优先** - 响应式设计理念
- 🎨 **现代化UI** - 渐变、阴影、动画效果
- 🔧 **错误处理** - 完善的异常处理机制

## 📁 项目结构

```
ddlwlrma-ai-agent-frontend/
├── public/                     # 静态资源
│   ├── rocket-icon.svg        # 网站图标
│   └── vite.svg               # Vite默认图标
├── src/
│   ├── components/            # 可复用组件
│   │   └── WebsiteAvatar.vue  # 网站头像组件
│   ├── views/                 # 页面组件
│   │   ├── Home.vue          # 主页
│   │   ├── LoveChat.vue      # AI恋爱大师聊天页面
│   │   └── ManusChat.vue     # "世另我"智能体聊天页面
│   ├── router/               # 路由配置
│   │   └── index.js          # 路由定义
│   ├── services/             # API服务
│   │   └── api.js            # 接口和SSE工具类
│   ├── App.vue               # 根组件
│   ├── main.js               # 应用入口
│   └── style.css             # 全局样式
├── index.html                # HTML模板
├── package.json              # 项目配置
├── vite.config.js            # Vite配置
├── start.bat                 # Windows启动脚本
└── README.md                 # 项目文档
```

## 🔌 后端接口

项目需要配合SpringBoot后端使用，默认接口地址：`http://localhost:8123/api`

### API端点

#### 1. AI恋爱大师聊天接口
```http
GET /api/ai/love_app/chat/sse
```

**参数：**
- `message` (string) - 用户输入的消息
- `chatId` (string) - 会话ID，用于区分不同对话

**返回：** SSE流式数据，实时返回AI回复

#### 2. "世另我"智能体聊天接口
```http
GET /api/ai/manus/chat
```

**参数：**
- `message` (string) - 用户输入的消息

**返回：** SSE流式数据，包含思考过程和执行步骤

### 数据格式
```javascript
// SSE消息格式
data: "AI回复内容"

// 特殊消息类型
data: "💭 思考: 思考内容"
data: "🔧 工具: 工具调用信息"
data: "❌ 错误: 错误信息"
data: "✅ 完成: 任务完成信息"
```

## 🚀 快速开始

### 环境要求
- Node.js 16.0 或更高版本
- npm 8.0 或更高版本
- 现代浏览器（Chrome 90+, Firefox 88+, Safari 14+）

### 1. 克隆项目
```bash
git clone https://github.com/your-username/ddlwlrma-ai-agent-frontend.git
cd ddlwlrma-ai-agent-frontend
```

### 2. 安装依赖
```bash
npm install
```

### 3. 启动开发服务器
```bash
npm run dev
```

访问：http://localhost:3000

### 4. 构建生产版本
```bash
npm run build
```

### 5. 预览生产版本
```bash
npm run preview
```

## ⚙️ 配置说明

### 开发环境配置

#### Vite代理配置
项目已配置Vite代理，将`/api`请求转发到后端服务：

```javascript
// vite.config.js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8123',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
```

#### 环境变量
创建 `.env.local` 文件（可选）：
```env
VITE_API_BASE_URL=http://localhost:8123
VITE_APP_TITLE=DDLWLRMA AI Agent
```

### 生产环境配置

#### 构建配置
```bash
# 构建生产版本
npm run build

# 构建产物位于 dist/ 目录
```

#### 部署到服务器
1. 将 `dist/` 目录内容上传到Web服务器
2. 配置Nginx反向代理（推荐）
3. 确保后端API地址正确配置

## 🎨 界面特性

### 响应式设计
- **PC端** (>1024px): 完整功能，三列布局
- **平板端** (769px-1024px): 优化布局，双列显示
- **移动端** (≤768px): 单列布局，触摸优化

### 视觉设计
- 🌈 **渐变背景** - 现代化的渐变色彩搭配
- 🎭 **头像系统** - 用户和AI专属头像
- 💬 **消息气泡** - 不同类型消息的视觉区分
- ⚡ **加载动画** - 流畅的加载和过渡效果
- 🎯 **状态指示** - 实时连接状态显示

### 交互体验
- 🔄 **自动滚动** - 新消息自动滚动到视图
- ⌨️ **键盘支持** - Enter键发送消息
- 📱 **触摸优化** - 移动端友好的触摸交互
- ⚡ **实时反馈** - 即时的用户操作反馈

## 🔧 开发指南

### 添加新的聊天应用

1. **创建聊天组件**
```bash
# 在 src/views/ 目录下创建新组件
touch src/views/NewChat.vue
```

2. **添加路由配置**
```javascript
// src/router/index.js
{
  path: '/new-chat',
  name: 'NewChat',
  component: () => import('../views/NewChat.vue')
}
```

3. **添加API服务**
```javascript
// src/services/api.js
export function startNewChatSSE(message, onMessage, onError, onOpen, onClose) {
  // SSE连接实现
}
```

4. **更新主页**
```vue
<!-- src/views/Home.vue -->
<div class="app-card">
  <h3>新应用</h3>
  <p>应用描述</p>
  <router-link to="/new-chat" class="start-btn">开始聊天</router-link>
</div>
```

### 自定义样式

#### 全局样式
- 主样式文件：`src/style.css`
- 响应式断点：768px, 1024px
- 颜色变量：建议使用CSS变量定义主题色

#### 组件样式
```vue
<style scoped>
/* 组件专用样式 */
.component-name {
  /* 样式定义 */
}

/* 响应式设计 */
@media (max-width: 768px) {
  .component-name {
    /* 移动端样式 */
  }
}
</style>
```

### 调试技巧

#### 开发工具
- Vue DevTools - Vue组件调试
- 浏览器开发者工具 - 网络和性能分析
- 控制台日志 - SSE连接状态监控

#### 常见问题
1. **SSE连接失败** - 检查后端服务状态
2. **跨域问题** - 确认代理配置正确
3. **样式异常** - 检查CSS兼容性

## 📊 性能优化

### 前端优化
- 🚀 **Vite构建** - 快速的开发构建
- 📦 **代码分割** - 按需加载组件
- 🖼️ **图片优化** - SVG图标，减少HTTP请求
- 🎨 **CSS优化** - 压缩和合并样式

### 用户体验优化
- ⚡ **SSE流式传输** - 实时显示AI回复
- 🔄 **智能重连** - 网络异常自动重连
- 💾 **状态管理** - 保持用户操作状态
- 📱 **移动端优化** - 触摸友好的界面

## 🔒 安全考虑

### 前端安全
- ✅ **输入验证** - 客户端输入检查
- 🔒 **HTTPS** - 生产环境强制HTTPS
- 🛡️ **XSS防护** - 内容安全策略
- 🔐 **敏感信息** - 避免在前端存储敏感数据

### 通信安全
- 🔒 **HTTPS/WSS** - 加密通信
- 🛡️ **CORS配置** - 跨域安全策略
- 🔐 **API认证** - 后端接口认证机制

## 🤝 贡献指南

### 开发流程
1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

### 代码规范
- 使用ESLint进行代码检查
- 遵循Vue.js官方风格指南
- 编写清晰的注释和文档
- 保持代码简洁和可维护性

## 📝 更新日志

### v1.0.0 (2025-08-15)
- ✨ 初始版本发布
- 🎨 现代化UI设计
- 💬 双AI聊天应用
- 📱 响应式设计
- ⚡ SSE实时通信

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和用户！

---

⭐ 如果这个项目对你有帮助，请给它一个星标！
