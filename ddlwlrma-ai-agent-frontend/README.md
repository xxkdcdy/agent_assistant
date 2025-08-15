# DDLWLRMA AI Agent Frontend

一个基于 Vue3 的前端项目，提供两个AI应用的聊天界面：AI恋爱大师和"世另我"AI超级智能体。

## 功能特色

### 🏠 主页
- 美观的应用选择界面
- 响应式设计，支持移动端
- 渐变背景和卡片式布局

### 💕 AI恋爱大师
- 专业的恋爱咨询和情感建议
- 实时聊天界面，支持流式对话
- 自动生成会话ID，区分不同对话
- 通过SSE实时显示AI回复

### 🤖 "世另我"AI超级智能体
- 全方位的智能服务
- 强大的多模态AI能力
- 实时连接状态显示
- 流式对话体验

## 技术栈

- **Vue 3** - 前端框架
- **Vue Router** - 路由管理
- **Axios** - HTTP请求库
- **Vite** - 构建工具
- **SSE (Server-Sent Events)** - 实时通信
- **Node 16**
## 项目结构

```
src/
├── components/          # 可复用组件
├── views/              # 页面组件
│   ├── Home.vue        # 主页
│   ├── LoveChat.vue    # AI恋爱大师聊天页面
│   └── ManusChat.vue   # "世另我"智能体聊天页面
├── router/             # 路由配置
│   └── index.js
├── services/           # API服务
│   └── api.js          # 接口和SSE工具
├── App.vue             # 根组件
├── main.js             # 入口文件
└── style.css           # 全局样式
```

## 后端接口

项目需要配合SpringBoot后端使用，接口地址：`http://localhost:8123/api`

### API端点

1. **AI恋爱大师聊天**
   - `GET /ai/love_app/chat/sse`
   - 参数：`message`, `chatId`
   - 返回：SSE流式数据

2. **"世另我"智能体聊天**
   - `GET /ai/manus/chat`
   - 参数：`message`
   - 返回：SSE流式数据

## 安装和运行

### 安装依赖
```bash
npm install
```

### 开发模式
```bash
npm run dev
```
访问：http://localhost:3000

### 构建生产版本
```bash
npm run build
```

### 预览生产版本
```bash
npm run preview
```

## 配置说明

### 代理配置
项目已配置Vite代理，将`/api`请求转发到`http://localhost:8123`：

```javascript
// vite.config.js
proxy: {
  '/api': {
    target: 'http://localhost:8123',
    changeOrigin: true
  }
}
```

### SSE连接
使用自定义的SSEConnection类处理Server-Sent Events：
- 自动重连机制
- 错误处理
- 消息流式显示

## 特性说明

### 🎨 UI设计
- 现代化渐变设计
- 响应式布局，适配移动端
- 流畅的动画效果
- 直观的用户体验

### 🔄 实时通信
- SSE流式对话
- 实时连接状态显示
- 自动滚动到最新消息
- 输入状态管理

### 📱 移动端优化
- 响应式设计
- 触摸友好的界面
- 移动端适配样式

## 开发指南

### 添加新的聊天应用
1. 在`src/views/`创建新的聊天组件
2. 在`src/router/index.js`添加路由
3. 在`src/services/api.js`添加对应的SSE函数
4. 在主页`Home.vue`添加应用卡片

### 自定义样式
- 全局样式在`src/style.css`
- 组件样式使用scoped CSS
- 响应式断点：768px

## 注意事项

1. 确保后端服务运行在`localhost:8123`
2. 后端需要支持CORS跨域请求
3. SSE连接需要正确的Content-Type头
4. 建议在HTTPS环境下使用以获得最佳体验

## 许可证

MIT License
