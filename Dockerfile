FROM openjdk:21-slim

# 安装必要工具：curl、git、解压工具
RUN apt-get update && apt-get install -y curl xz-utils git && apt-get clean && rm -rf /var/lib/apt/lists/*

# 下载 Node.js 官方二进制并解压到 /usr/local
RUN curl -fsSL https://npmmirror.com/mirrors/node/v20.8.1/node-v20.8.1-linux-x64.tar.xz -o node.tar.xz && \
    tar -xJf node.tar.xz -C /usr/local --strip-components=1 && \
    rm node.tar.xz

# 确保 Node.js/npm/npx 在 PATH
ENV PATH="/usr/local/bin:$PATH"

WORKDIR /app

# 复制 jar 包和 MCP 配置文件
COPY ddlwlrma-ai-agent-0.0.1-SNAPSHOT.jar app.jar
COPY mcp-servers.json mcp-servers.json

EXPOSE 8123

# 启动 Spring Boot 应用
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=local", "--server.port=8123"]
