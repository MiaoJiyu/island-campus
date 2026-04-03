# 灵岛校园（IslandCampus）部署指南

## 项目概述

灵岛校园是一套专为中小学课堂电脑设计的集群管理与教学辅助系统，包含：
- **Spring Boot 3.2 + Java 17** 后端（12个业务模块）
- **Vue 3 + TypeScript** Web管理端（17个管理页面）
- **Electron 28** 客户端（灵动岛窗口 + 考试锁定）
- **Docker Compose** 全栈容器化部署

## 文件结构
```
/opt/island-campus/
├── docker-compose.yml              # Docker编排配置
├── docker/                         # Docker相关文件
├── server/                         # Spring Boot后端（136个Java文件）
├── web-admin/                      # Vue3 Web管理端（17个页面）
├── electron-client/                # Electron客户端
└── DEPLOYMENT.md                   # 本部署文档
```

## 系统要求

### 硬件要求
- CPU：2核心以上
- 内存：4GB以上 
- 磁盘空间：10GB以上

### 软件要求
- Docker & Docker Compose
- 可选：Java 17+（开发环境）
- 可选：Node.js 18+（开发环境）

## 快速部署（推荐）

### 方式一：Docker Compose一键启动

1. **准备环境变量**
   ```bash
   # 天气API密钥（可选，不配置则使用默认预设）
   export WEATHER_API_KEY="your_uapis_cn_key"
   
   # JWT密钥
   export JWT_SECRET="island-campus-secret-key-change-in-production"
   
   # 数据库密码
   export MYSQL_ROOT_PASSWORD="island@2026"
   export MYSQL_PASSWORD="island@2026"
   
   # Redis密码
   export REDIS_PASSWORD="island@2026"
   ```

2. **启动服务**
   ```bash
   cd /opt/island-campus
   docker-compose up -d
   ```

3. **访问系统**
   - Web管理端：https://localhost:8443
     - 账号：`admin`
     - 密码：`Admin@2026!`
   - API文档：https://localhost:8443/doc.html

### 方式二：手动部署

#### 1. 数据库初始化
```bash
# 启动MariaDB
docker run -d \
  --name island-mysql \
  -e MYSQL_ROOT_PASSWORD=island@2026 \
  -e MYSQL_DATABASE=island_campus \
  -p 3306:3306 \
  mariadb:10.11

# 导入初始数据
docker exec -i island-mysql mysql -uisland -pisland@2026 island_campus < docker/mysql/init/01_init.sql
```

#### 2. Redis启动
```bash
docker run -d \
  --name island-redis \
  -e REDIS_PASSWORD=island@2026 \
  -p 6379:6379 \
  redis:7.0-alpine
```

#### 3. 后端服务启动
```bash
cd /opt/island-campus/server
# Maven构建（开发环境）
mvn clean package -DskipTests
java -jar server-app/target/server-app-2.1.0.jar

# 或使用Docker
docker build -t island-backend -f docker/backend/Dockerfile .
docker run -d --name backend -p 8080:8080 island-backend
```

#### 4. Web管理端
```bash
cd /opt/island-campus/web-admin
npm install
npm run build

# 静态文件部署
# 复制dist/目录内容到Nginx/Apache等Web服务器
```

#### 5. Electron客户端打包
```bash
cd /opt/island-campus/electron-client
npm install
npm run build  # 生成安装包
```

## 端口说明

| 服务 | 端口 | 描述 |
|------|------|------|
| Nginx | 8443 (HTTPS) | 主入口，反向代理 |
| 后端API | 8080 | Spring Boot应用 |
| MariaDB | 3306 | 数据库 |
| Redis | 6379 | 缓存与消息队列 |

## 配置说明

### 后端配置文件
- `server/server-app/src/main/resources/application.yml` - 主配置文件
- `docker/nginx/nginx.conf` - Nginx代理配置
- `docker/mysql/conf/my.cnf` - MariaDB配置
- `docker/redis/redis.conf` - Redis配置

### 关键配置项
```yaml
# JWT配置
jwt:
  secret: ${JWT_SECRET:default-secret}
  access-token-expiration: 2h
  refresh-token-expiration: 7d

# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://island-mysql:3306/island_campus
    username: island
    password: ${MYSQL_PASSWORD}

# Redis配置
redis:
  host: island-redis
  password: ${REDIS_PASSWORD}
```

### 天气API配置
1. 访问 https://uapis.cn 注册获取API密钥
2. 在Web管理端「天气配置」页面设置
3. 或通过环境变量 `WEATHER_API_KEY` 注入

## 默认账号

内置管理员账号：
- **用户名**: `admin`
- **密码**: `Admin@2026!`
- **角色**: 超级管理员（SYSTEM_ADMIN）

其他内置角色：
- PARTITION_ADMIN - 分区管理员
- SCHOOL_ADMIN - 学校管理员
- GRADE_ADMIN - 年级管理员
- CLASS_ADMIN - 班级管理员
- REGULAR_USER - 普通用户

## 初始数据

数据库初始化时自动创建：
1. **19张核心数据表**（organization, computer, user, role, permission等）
2. **6个内置角色**及权限分配
3. **4种情景模式**（上课、课间、自习、考试）
4. **uapis.cn天气API预设配置**
5. **默认灵动岛配置**

## 健康检查

### 服务健康检查
```bash
# API健康检查
curl -k https://localhost:8443/api/v1/health

# WebSocket连接测试
wss://localhost:8443/ws
```

### 客户端状态检查
1. Electron客户端心跳应正常（10秒间隔）
2. 客户端健康上报应正常（5分钟间隔）
3. WebSocket连接应保持

## 故障排查

### 常见问题

1. **端口冲突**
   ```bash
   # 检查占用端口
   netstat -tulpn | grep :8443
   
   # 修改docker-compose.yml中的端口映射
   ```

2. **数据库连接失败**
   ```bash
   # 检查数据库服务状态
   docker logs island-mysql
   
   # 验证连接
   mysql -h localhost -u island -pisland@2026
   ```

3. **SSL证书问题**
   ```bash
   # 重新生成自签名证书
   cd docker/nginx
   bash generate-ssl.sh
   ```

4. **Electron客户端无法连接**
   - 确认服务器地址配置正确
   - 检查防火墙/安全组设置
   - 确认WebSocket端口可访问

### 日志查看
```bash
# 查看全部服务日志
docker-compose logs

# 查看后端日志
docker logs island-backend

# 查看Nginx日志
docker logs island-nginx
```

## 备份与恢复

### 数据库备份
```bash
# 导出数据库
docker exec island-mysql mysqldump -uisland -pisland@2026 island_campus > backup.sql

# 导入数据库
docker exec -i island-mysql mysql -uisland -pisland@2026 island_campus < backup.sql
```

### 配置文件备份
```bash
# 备份重要配置
tar -czf config_backup.tar.gz \
  server/server-app/src/main/resources/application.yml \
  docker/nginx/nginx.conf \
  docker/redis/redis.conf
```

## 开发环境

### 本地开发
```bash
# 1. 启动基础设施
cd /opt/island-campus
docker-compose up mysql redis -d

# 2. 启动后端（开发模式）
cd server
mvn spring-boot:run

# 3. 启动Web管理端
cd ../web-admin
npm run dev

# 4. 启动Electron客户端（开发模式）
cd ../electron-client
npm run dev
```

### 热重载配置
- **后端**: Spring Boot DevTools
- **前端**: Vite HMR
- **Electron**: Vite Electron插件

## 性能优化建议

### 生产环境优化
1. **数据库优化**
   ```sql
   -- 添加索引
   ALTER TABLE remote_message ADD INDEX idx_recipient_type (recipient_id, type);
   ALTER TABLE health_report ADD INDEX idx_computer_time (computer_id, created_at);
   ```

2. **JVM参数优化**
   ```yaml
   # application.yml
   spring:
     jpa:
       properties:
         hibernate.jdbc.batch_size: 20
         hibernate.order_inserts: true
   ```

3. **Redis缓存策略**
   - 天气数据：30分钟TTL
   - 课程表：按天缓存
   - 权限信息：JWT中携带

### 监控建议
- 使用Prometheus + Grafana监控系统性能
- ELK Stack收集日志
- 客户端健康状态监控

## 安全配置

### 生产环境必须修改
1. **修改所有默认密码**
   - MariaDB root密码
   - Redis密码
   - JWT密钥

2. **配置HTTPS证书**
   - 替换自签名证书为正式证书
   - 配置域名和DNS

3. **网络隔离**
   - 后端API仅内网可访问
   - 数据库不暴露公网
   - Redis设置密码和IP白名单

### 定期安全审计
1. 检查用户权限分配
2. 审计关键操作日志
3. 更新依赖库安全补丁

## 升级说明

### 版本升级步骤
1. 备份数据库和配置文件
2. 停止当前服务
3. 更新代码/镜像
4. 执行数据库迁移（如有）
5. 启动新版本服务
6. 验证功能完整性

### 数据迁移
- 使用Flyway管理数据库版本
- 每个迁移脚本前需充分测试
- 准备回滚方案

---

## 联系支持

### 技术支持
- 查看详细文档：项目内 `README.md`
- 监控告警配置：参考监控建议部分
- 紧急故障：查看故障排查章节

### 问题反馈
1. 查看日志定位问题
2. 提供复现步骤
3. 提供相关配置和环境信息

---

**系统状态检查清单** ✅
- [ ] 所有容器运行正常
- [ ] 数据库连接成功
- [ ] Redis连接成功
- [ ] WebSocket连接正常
- [ ] 客户端心跳正常
- [ ] 权限验证正常
- [ ] 定时任务执行正常
- [ ] 日志记录正常

**部署完成！开启灵岛校园之旅吧！🚀**