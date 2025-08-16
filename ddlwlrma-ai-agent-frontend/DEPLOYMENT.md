# 部署说明

## 环境配置

本项目支持多环境部署，通过环境变量自动配置API基础URL。

### 开发环境
- API基础URL: `http://localhost:8123/api`
- 启动命令: `npm run dev`
- 构建命令: `npm run build:dev`

### 生产环境
- API基础URL: `/api` (通过nginx代理到后端)
- 构建命令: `npm run build:prod`
- 部署目录: `dist/`

## 部署步骤

### 1. 开发环境
```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 2. 生产环境构建
```bash
# 构建生产版本
npm run build:prod

# 或者使用默认构建
npm run build
```

### 3. Nginx配置
生产环境使用nginx进行反向代理，配置示例：

```nginx
server {
    listen       80;
    server_name  localhost;
    
    # 前端静态文件根目录
    root   /usr/share/nginx/html;

    # 所有HTML请求都返回index.html（解决Vue路由的404问题）
    location / {
        index  index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

    # API请求反向代理到后端
    location ^~ /api/ {
        proxy_pass http://47.117.35.195:8123/api/;
        proxy_set_header Host 47.117.35.195;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # SSE配置
        proxy_set_header Connection "";
        proxy_http_version 1.1;
        proxy_buffering off;
        proxy_cache off;
        chunked_transfer_encoding off;
        proxy_read_timeout 600s;
    }
}
```

## 环境变量说明

- `VITE_API_BASE_URL`: 自定义API基础URL
- `NODE_ENV`: 环境标识 (development/production)
- `VITE_MODE`: Vite模式 (development/production)

## 注意事项

1. 生产环境使用相对路径 `/api`，依赖nginx代理
2. 开发环境直接连接本地后端服务
3. SSE连接需要特殊的nginx配置支持
4. 确保后端服务在指定端口运行

