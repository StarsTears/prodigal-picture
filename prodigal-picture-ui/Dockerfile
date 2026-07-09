FROM node:18-alpine AS builder
WORKDIR /build

COPY package*.json ./

# 使用淘宝 npm 镜像加速
RUN npm config set registry https://registry.npmmirror.com && npm ci

COPY . .
RUN npm run build-only || npm run build

FROM nginx:alpine

RUN apk add --no-cache curl

COPY --from=builder /build/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:81/ || exit 1

EXPOSE 81
CMD ["nginx", "-g", "daemon off;"]
