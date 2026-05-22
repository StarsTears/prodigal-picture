# prodigal-picture-ui/Dockerfile
FROM node:18-alpine AS builder
WORKDIR /build

COPY package*.json ./
RUN npm ci --production=false

# 使用 ci 安装（更干净，更快）
RUN npm run build

# 复制源码
COPY . .
# 构建
RUN npm run build

FROM nginx:alpine
# 复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:81/ || exit 1

EXPOSE 81
CMD ["nginx", "-g", "daemon off;"]
