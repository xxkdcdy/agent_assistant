# ==========================
# 前端构建阶段
# ==========================
FROM node:16-alpine AS build

# 设置工作目录
WORKDIR /app

# 复制 package.json 和 package-lock.json
COPY package*.json ./

# 安装依赖
RUN npm install

# 复制前端源码
COPY . .

# 构建前端
RUN npm run build

# ==========================
# 运行阶段 - Nginx 托管静态文件
# ==========================
FROM nginx:alpine

# 默认 Nginx 静态文件目录
ENV NGINX_HTML_DIR /usr/share/nginx/html

# 复制构建产物到 Nginx
COPY --from=build /app/dist ${NGINX_HTML_DIR}

# 可选：复制自定义 nginx 配置，替换默认配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]
