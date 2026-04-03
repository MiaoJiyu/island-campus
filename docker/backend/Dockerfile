# 构建阶段：官方 Maven 镜像
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# 对于多模块项目，需要一次性复制整个项目结构
COPY server/ .
# 使用跳过依赖下载的模式进行构建，依赖会在构建过程中自动下载
RUN mvn clean package -DskipTests -B

# 运行时阶段：JRE 基础镜像
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="IslandCampus Team"

# 优化：替换为阿里云源，加速 apk add
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories && \
    apk add --no-cache curl fontconfig ttf-dejavu && \
    # 创建必要的目录
    mkdir -p /app/plugins /app/screenshots /app/uploads /app/logs && \
    chmod 777 /app/plugins /app/screenshots /app/uploads /app/logs

WORKDIR /app

# 复制最终的可执行jar文件。根据项目的 pom，jar 名称可能是 server-app-2.1.0.jar
COPY --from=builder /build/server-app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", \
    "-Xms256m", "-Xmx512m", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Duser.timezone=Asia/Shanghai", \
    "-jar", "app.jar"]