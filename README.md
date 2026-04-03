# 灵岛校园 IslandCampus

> 中小学课堂电脑集群管理与教学辅助系统
> 版本：v2.1 | 更新：2026-04-03

## 项目结构

```
island-campus/
├── server/                 # Spring Boot 3.2 后端
│   ├── server-common/      # 公共模块（安全、工具、配置）
│   ├── server-system/      # 系统管理（组织、用户、角色、权限）
│   ├── server-island/      # 灵动岛（信息栏配置、实时推送）
│   ├── server-mode/        # 情景模式（上课/课间/自习/考试策略）
│   ├── server-exam/        # 考试管理（创建、锁定、日志）
│   ├── server-weather/     # 天气服务（API配置、规则引擎、联动提醒）
│   ├── server-announcement/# 公告广播（普通公告、紧急广播）
│   ├── server-message/     # 远程消息（即时通知、未读角标）
│   ├── server-answer/      # 答案公布（定时/解密公布）
│   ├── server-health/      # 健康管家（探针、看板、自动修复）
│   ├── server-computer/    # 设备管理（注册、心跳、状态）
│   ├── server-token/       # 查询令牌（家长/教师只读访问）
│   └── server-plugin/      # 插件生态（前后端插件加载、沙箱）
├── web-admin/              # Vue3 Web管理端 (PC/Mobile响应式)
├── electron-client/        # Electron客户端程序
├── docker/                 # Docker配置文件
│   ├── mysql/              # MariaDB初始化脚本
│   ├── nginx/              # Nginx配置
│   └── redis/              # Redis配置
├── docker-compose.yml      # 容器编排
└── docs/                   # 文档
```

## 核心特色

- **不锁屏原则** — 日常教学中教师电脑始终可操作
- **考试全屏锁定** — 考试期间仅显示数字时钟，杜绝作弊
- **灵动岛信息栏** — 类macOS Dynamic Island，高度可配置
- **逐级权限** — 系统→分区→学校→年级→班级→电脑
- **开放生态** — 自定义天气API、Groovy插件、Vue组件扩展

## 快速启动

```bash
# 1. 克隆项目
git clone <repo-url>
cd island-campus

# 2. Docker一键启动全部服务
docker compose up -d

# 3. 访问
# Web管理端: https://localhost:8443
# API文档: https://localhost:8443/doc.html
# 默认账号: admin / admin123456
```

## 技术栈

| 层 | 技术 |
|---|------|
| 后端 | Spring Boot 3.2 + Java 17 + MyBatis-Plus + MariaDB + Redis + Quartz |
| 前端Web | Vue 3 + TypeScript + Vite + Element Plus + Vant + Pinia |
| 客户端 | Electron + Vue 3 |
| 部署 | Docker Compose + Nginx + SSL(8443) |

## License

MIT License - see [LICENSE](LICENSE)
