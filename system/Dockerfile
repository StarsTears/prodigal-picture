FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 复制父 pom 和子模块 pom(多模块项目)
COPY pom.xml ./
COPY system/pom.xml ./system/pom.xml

# 先下载依赖（利用 Docker 缓存）
RUN mvn -pl system -am dependency:go-offline -B

# 复制源码并构建
COPY system ./system
RUN mvn -pl system -am clean package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 安装必要工具并创建非root用户
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r appuser && useradd -r -g appuser -d /app -s /sbin/nologin appuser

# 复制构建产物
COPY --from=builder /build/system/target/*.jar /app/app.jar

# 设置文件权限
RUN chown -R appuser:appuser /app

# 切换到非root用户
USER appuser

# 暴露端口(与application.yml和docker-compose保持一致)
EXPOSE 9999

# 设置时区
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC" \
    SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -sf -X POST http://localhost:9999/api/health/check | grep -q OK || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
