-- ============================================
-- 灵岛校园 IslandCampus v2.1 数据库初始化脚本
-- MariaDB 10.11+ / MySQL 8.0+
-- 字符集: utf8mb4
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------
-- 1. 组织架构表 (sys_organization)
-- 树状结构: 分区 → 学校 → 年级 → 班级
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_organization` (
    `id`          BIGINT       NOT NULL COMMENT '主键ID',
    `parent_id`   BIGINT       DEFAULT NULL COMMENT '父节点ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '组织名称',
    `code`        VARCHAR(50)  DEFAULT NULL COMMENT '组织编码',
    `type`        TINYINT      NOT NULL DEFAULT 3 COMMENT '类型: 1=分区,2=学校,3=年级,4=班级',
    `level`       TINYINT      NOT NULL DEFAULT 1 COMMENT '层级深度',
    `sort_order`  INT          DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用,1=启用',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    `version`     INT          DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织架构树';

-- --------------------------------------------
-- 2. 角色表 (role)
-- 内置角色不可删除: super_admin(1), zone_admin(2), school_admin(3), grade_admin(4), class_admin(5), viewer(6)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `role` (
    `id`          BIGINT        NOT NULL COMMENT '主键ID',
    `name`        VARCHAR(50)   NOT NULL COMMENT '角色名称',
    `code`        VARCHAR(50)   NOT NULL COMMENT '角色编码',
    `is_builtin`  TINYINT       NOT NULL DEFAULT 0 COMMENT '是否内置: 0=自定义,1=内置',
    `permission_mask` BIGINT    DEFAULT 0 COMMENT '权限位掩码',
    `data_scope`  TINYINT      DEFAULT 1 COMMENT '数据范围: 1=全部,2=本分区,3=本学校,4=本年级,5=本班级,6=本人',
    `status`      TINYINT      NOT NULL DEFAULT 1,
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色';

-- --------------------------------------------
-- 3. 权限资源表 (permission)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `permission` (
    `id`          BIGINT       NOT NULL,
    `name`        VARCHAR(100) NOT NULL COMMENT '权限名称',
    `code`        VARCHAR(100) NOT NULL COMMENT '权限编码(如 api:v1:user:list)',
    `type`        TINYINT      DEFAULT 1 COMMENT '类型: 1=API端点,2=菜单,3=按钮',
    `parent_id`   BIGINT       DEFAULT NULL,
    `path`        VARCHAR(200) DEFAULT NULL COMMENT 'API路径或路由路径',
    `method`      VARCHAR(10)  DEFAULT NULL COMMENT 'HTTP方法(GET/POST/PUT/DELETE)',
    `sort_order`  INT          DEFAULT 0,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    INDEX `idx_type` (`type`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限资源';

-- --------------------------------------------
-- 4. 用户表 (user)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id`              BIGINT       NOT NULL,
    `username`        VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password_hash`   VARCHAR(255) NOT NULL COMMENT 'BCrypt密码哈希',
    `salt`            VARCHAR(64)  DEFAULT NULL COMMENT '密码盐值',
    `real_name`       VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`           VARCHAR(100) DEFAULT NULL,
    `avatar_url`      VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `org_id`          BIGINT       DEFAULT NULL COMMENT '所属组织ID',
    `role_id`         BIGINT       NOT NULL DEFAULT 6 COMMENT '角色ID',
    `must_change_pwd` TINYINT      DEFAULT 1 COMMENT '首次登录强制改密码: 0=否,1=是',
    `login_count`     INT          DEFAULT 0 COMMENT '登录次数',
    `last_login_time` DATETIME     DEFAULT NULL,
    `last_login_ip`   VARCHAR(50)  DEFAULT NULL,
    `login_fail_count`INT         DEFAULT 0 COMMENT '连续失败次数',
    `locked_until`    DATETIME     DEFAULT NULL COMMENT '锁定到期时间',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '0=禁用,1=启用',
    `two_factor_enabled` TINYINT   DEFAULT 0 COMMENT '双因素认证',
    `two_factor_secret` VARCHAR(100) DEFAULT NULL,
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT      NOT NULL DEFAULT 0,
    `version`         INT          DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    INDEX `idx_org_id` (`org_id`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号';

-- --------------------------------------------
-- 5. 电脑设备表 (computer)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `computer` (
    `id`                BIGINT        NOT NULL,
    `name`              VARCHAR(100)  NOT NULL COMMENT '设备名称',
    `mac_address`       VARCHAR(17)   NOT NULL COMMENT 'MAC地址',
    `ip_address`        VARCHAR(45)   DEFAULT NULL COMMENT 'IP地址',
    `class_id`          BIGINT        DEFAULT NULL COMMENT '所属班级组织ID',
    `os_info`           VARCHAR(200)  DEFAULT NULL COMMENT '操作系统信息',
    `client_version`    VARCHAR(30)   DEFAULT NULL COMMENT '客户端版本',
    `is_online`         TINYINT       DEFAULT 0 COMMENT '在线状态: 0=离线,1=在线',
    `last_heartbeat`    DATETIME      DEFAULT NULL COMMENT '最后心跳时间',
    `health_status`     TINYINT       DEFAULT 1 COMMENT '健康状态: 0=异常红,1=警告黄,2=正常绿',
    `island_config_json`TEXT         DEFAULT NULL COMMENT '灵动岛个性化配置JSON覆盖',
    `resolution`        VARCHAR(20)   DEFAULT NULL COMMENT '屏幕分辨率如1920x1080',
    `remark`            VARCHAR(500)  DEFAULT NULL,
    `status`            TINYINT       NOT NULL DEFAULT 1,
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`           TINYINT      NOT NULL DEFAULT 0,
    `version`           INT          DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_mac` (`mac_address`),
    INDEX `idx_class_id` (`class_id`),
    INDEX `idx_is_online` (`is_online`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电脑设备';

-- --------------------------------------------
-- 6. 课程表 (timetable)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `timetable` (
    `id`            BIGINT       NOT NULL,
    `class_id`      BIGINT       NOT NULL COMMENT '班级组织ID',
    `day_of_week`   TINYINT      NOT NULL COMMENT '星期几(1-7,周一到周日)',
    `period`        TINYINT      NOT NULL COMMENT '节次(1-N)',
    `subject`       VARCHAR(50)  NOT NULL COMMENT '科目',
    `teacher_name`  VARCHAR(50)  DEFAULT NULL COMMENT '教师姓名',
    `room`          VARCHAR(50)  DEFAULT NULL COMMENT '教室',
    `start_time`    TIME         DEFAULT NULL COMMENT '开始时间',
    `end_time`      TIME         DEFAULT NULL COMMENT '结束时间',
    `sort_order`    INT          DEFAULT 0,
    `semester`      VARCHAR(20)  DEFAULT NULL COMMENT '学期标识',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_class_day_period` (`class_id`, `day_of_week`, `period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- --------------------------------------------
-- 7. 情景模式定义表 (scene_mode)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `scene_mode` (
    `id`              BIGINT       NOT NULL,
    `name`            VARCHAR(50)  NOT NULL COMMENT '模式名称(上课专注/课间/自习/考试)',
    `code`            VARCHAR(30)  NOT NULL COMMENT '模式编码(class/break/study/exam)',
    `icon`            VARCHAR(50)  DEFAULT NULL COMMENT '图标标识',
    `color`           VARCHAR(20)  DEFAULT NULL COMMENT '主题颜色',
    `is_lock_screen`  TINYINT      DEFAULT 0 COMMENT '是否锁屏: 0=不锁,1=全屏锁定(仅考试)',
    `block_internet`  TINYINT      DEFAULT 0 COMMENT '限制外网访问',
    `block_apps`      TEXT         DEFAULT NULL COMMENT '禁止应用列表JSON',
    `reminder_interval` INT        DEFAULT 0 COMMENT '提醒间隔分钟数(0=无提醒)',
    `reminder_text`   VARCHAR(500) DEFAULT NULL COMMENT '提醒文案',
    `strategy_script` TEXT         DEFAULT NULL COMMENT '策略Groovy脚本',
    `is_system`       TINYINT      DEFAULT 0 COMMENT '系统内置: 0=自定义,1=内置',
    `status`          TINYINT      NOT NULL DEFAULT 1,
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT      NOT NULL DEFAULT 0,
    `version`         INT          DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情景模式定义';

-- --------------------------------------------
-- 8. 模式切换计划表 (mode_schedule)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `mode_schedule` (
    `id`            BIGINT       NOT NULL,
    `class_id`      BIGINT       DEFAULT NULL COMMENT '适用班级(NULL=全校)',
    `mode_id`       BIGINT       NOT NULL COMMENT '情景模式ID',
    `trigger_type`  TINYINT      DEFAULT 1 COMMENT '触发类型: 1=手动,2=课程表联动,3=Cron定时',
    `cron_expr`     VARCHAR(100) DEFAULT NULL COMMENT 'Cron表达式',
    `target_mode_id`BIGINT       DEFAULT NULL COMMENT '切换后恢复的目标模式ID',
    `effective_from`DATETIME     DEFAULT NULL,
    `effective_to`  DATETIME     DEFAULT NULL,
    `status`        TINYINT      DEFAULT 1,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_class_status` (`class_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模式切换计划';

-- --------------------------------------------
-- 9. 公告广播表 (announcement)
-- type: 1=普通公告, 2=紧急广播
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `announcement` (
    `id`            BIGINT       NOT NULL,
    `title`         VARCHAR(200) NOT NULL COMMENT '标题',
    `content`       TEXT         NOT NULL COMMENT '内容',
    `type`          TINYINT      NOT NULL DEFAULT 1 COMMENT '1=普通公告,2=紧急广播',
    `publisher_id`  BIGINT       NOT NULL COMMENT '发布者用户ID',
    `target_scope`  JSON         DEFAULT NULL COMMENT '目标范围[组织ID列表]',
    `need_confirm`  TINYINT      DEFAULT 0 COMMENT '紧急广播需确认: 0=不需要,1=需要',
    `priority`      TINYINT      DEFAULT 0 COMMENT '优先级: 0=普通,1=高,2=紧急',
    `publish_time`  DATETIME     DEFAULT NULL COMMENT '定时发布(NULL=立即)',
    `expire_time`   DATETIME     DEFAULT NULL COMMENT '过期时间',
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0=草稿,1=已发布,2=已撤回',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    `version`       INT          DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_type_status` (`type`, `status`),
    INDEX `idx_publisher` (`publisher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告/广播';

-- --------------------------------------------
-- 10. 广播确认记录表 (broadcast_confirm)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `broadcast_confirm` (
    `id`            BIGINT       NOT NULL,
    `announcement_id`BIGINT     NOT NULL,
    `computer_id`   BIGINT       DEFAULT NULL,
    `user_id`       BIGINT       DEFAULT NULL COMMENT '确认用户(教师)',
    `confirmed_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ip_address`    VARCHAR(45)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_announcement` (`announcement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广播确认记录';

-- --------------------------------------------
-- 11. 天气API配置表 (weather_config)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `weather_config` (
    `id`            BIGINT       NOT NULL,
    `name`          VARCHAR(100) NOT NULL COMMENT '配置名称',
    `api_url`       VARCHAR(500) NOT NULL COMMENT 'API地址',
    `api_method`    VARCHAR(10)  DEFAULT 'GET' COMMENT 'HTTP方法',
    `api_key`       VARCHAR(255) DEFAULT NULL COMMENT 'API密钥',
    `request_headers`TEXT        DEFAULT NULL COMMENT '请求头JSON',
    `request_body_template` TEXT  DEFAULT NULL COMMENT '请求体模板',
    `city_param_name`VARCHAR(50) DEFAULT 'city' COMMENT '城市参数名',
    `response_json_path`VARCHAR(255) DEFAULT '$' COMMENT '响应数据根JSONPath',
    `temp_path`     VARCHAR(100) DEFAULT '$.temperature' COMMENT '温度JSONPath',
    `weather_path`  VARCHAR(100) DEFAULT '$.weather' COMMENT '天气文本JSONPath',
    `weather_icon_path` VARCHAR(100) DEFAULT '$.weather_icon' COMMENT '天气图标代码JSONPath',
    `forecast_path` VARCHAR(100) DEFAULT '$.forecast' COMMENT '预报数组JSONPath',
    `hourly_path`   VARCHAR(100) DEFAULT '$.hourly_forecast' COMMENT '逐小时预报JSONPath',
    `aqi_path`      VARCHAR(100) DEFAULT '$.aqi' COMMENT 'AQI JSONPath',
    `is_default`    TINYINT      DEFAULT 0 COMMENT '是否默认配置',
    `city_default`  VARCHAR(50)  DEFAULT NULL COMMENT '默认城市',
    `fetch_interval_minutes INT  DEFAULT 30 COMMENT '拉取间隔(分钟)',
    `enabled`       TINYINT      DEFAULT 1,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    `version`       INT          DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='天气API配置';

-- --------------------------------------------
-- 12. 天气提醒规则表 (weather_alert_rule)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `weather_alert_rule` (
    `id`            BIGINT       NOT NULL,
    `name`          VARCHAR(100) NOT NULL COMMENT '规则名称',
    `condition_expr`TEXT         NOT NULL COMMENT '条件表达式(JSON格式条件组)',
    `action_type`   TINYINT      NOT NULL COMMENT '动作: 1=闪烁图标,2=发公告,3=远程通知,4=多动作',
    `action_config` JSON         DEFAULT NULL COMMENT '动作配置(目标范围等)',
    `weather_config_id` BIGINT   DEFAULT NULL COMMENT '关联天气API配置',
    `priority`      INT          DEFAULT 0,
    `enabled`       TINYINT      DEFAULT 1,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='天气提醒规则';

-- --------------------------------------------
-- 13. 考试表 (exam)
-- status: 0=未开始, 1=进行中, 2=已结束, 3=已取消
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `exam` (
    `id`            BIGINT       NOT NULL,
    `name`          VARCHAR(200) NOT NULL COMMENT '考试名称',
    `scope_type`    TINYINT      DEFAULT 3 COMMENT '范围: 1=指定班级,2=年级,3=全校',
    `target_org_ids`JSON         DEFAULT NULL COMMENT '目标组织ID列表',
    `target_computer_ids` JSON   DEFAULT NULL COMMENT '目标电脑ID列表(可选精确指定)',
    `start_time`    DATETIME     NOT NULL COMMENT '考试开始时间',
    `end_time`      DATETIME     NOT NULL COMMENT '考试结束时间',
    `release_password` VARCHAR(32)DEFAULT NULL COMMENT '解除密码(6位数字或NULL自动生成)',
    `release_password_hash` VARCHAR(255) DEFAULT NULL COMMENT '解除密码哈希',
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0=未开始,1=进行中,2=已结束,3=已取消',
    `creator_id`    BIGINT       NOT NULL,
    `actual_start`  DATETIME     DEFAULT NULL COMMENT '实际开始时间',
    `actual_end`    DATETIME     DEFAULT NULL COMMENT '实际结束时间',
    `remark`        VARCHAR(500) DEFAULT NULL,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    `version`       INT          DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_status_time` (`status`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试';

-- --------------------------------------------
-- 14. 考试操作日志表 (exam_log)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `exam_log` (
    `id`            BIGINT       NOT NULL,
    `exam_id`       BIGINT       NOT NULL,
    `action`        VARCHAR(30)  NOT NULL COMMENT '操作: create/start/end/cancel/manual_start/manual_end',
    `operator_id`   BIGIT       NOT NULL COMMENT '操作人',
    `detail`        TEXT         DEFAULT NULL COMMENT '详情',
    `ip_address`    VARCHAR(45)  DEFAULT NULL,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_exam_id` (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试操作日志';

-- --------------------------------------------
-- 15. 答案集表 (answer_set)
-- publish_type: 1=定时公布, 2=解密公布
-- status: 0=草稿, 1=已公布, 2=已撤回
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `answer_set` (
    `id`            BIGINT       NOT NULL,
    `title`         VARCHAR(200) NOT NULL COMMENT '答案集标题',
    `creator_id`    BIGINT       NOT NULL,
    `target_class_ids`JSON       DEFAULT NULL COMMENT '适用班级ID列表',
    `questions_json` TEXT        NOT NULL COMMENT '题目与答案JSON [{question, type(text/image), answer, score}, ...]',
    `publish_type`  TINYINT      DEFAULT 1 COMMENT '1=定时公布,2=解密公布',
    `publish_time`  DATETIME     DEFAULT NULL COMMENT '定时公布时间(解密模式可为NULL)',
    `password`      VARCHAR(32)  DEFAULT NULL COMMENT '解密密码(6位数字)',
    `password_hash` VARCHAR(255) DEFAULT NULL COMMENT '密码哈希',
    `status`        TINYINT      DEFAULT 0 COMMENT '0=草稿,1=已公布,2=已撤回',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    `version`       INT          DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_creator` (`creator_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答案集';

-- --------------------------------------------
-- 16. 答案公布日志表 (answer_log)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `answer_log` (
    `id`            BIGINT       NOT NULL,
    `answer_set_id` BIGINT       NOT NULL,
    `action_type`   VARCHAR(20)  NOT NULL COMMENT 'publish_auto/publish_decrypt/view',
    `operator_id`   BIGINT       DEFAULT NULL,
    `token_id`      BIGINT       DEFAULT NULL COMMENT '通过令牌查看时记录令牌ID',
    `ip_address`    VARCHAR(45)  DEFAULT NULL,
    `detail`        VARCHAR(500) DEFAULT NULL,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_answer_set` (`answer_set_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答案公布日志';

-- --------------------------------------------
-- 17. 健康上报记录表 (health_report)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `health_report` (
    `id`            BIGINT       NOT NULL,
    `computer_id`   BIGINT       NOT NULL,
    `cpu_temp`      DECIMAL(5,1) DEFAULT NULL COMMENT 'CPU温度℃',
    `cpu_usage`     DECIMAL(5,2) DEFAULT NULL COMMENT 'CPU使用率%',
    `memory_usage`  DECIMAL(5,2) DEFAULT NULL COMMENT '内存使用率%',
    `disk_total_gb` DECIMAL(10,2) DEFAULT NULL COMMENT '磁盘总容量GB',
    `disk_free_gb`  DECIMAL(10,2) DEFAULT NULL COMMENT '磁盘剩余GB',
    `process_list`  TEXT         DEFAULT NULL COMMENT '进程列表摘要',
    `suspicious_processes` TEXT   DEFAULT NULL COMMENT '可疑进程列表',
    `uptime_seconds`BIGINT       DEFAULT NULL COMMENT '运行时间秒',
    `client_version`VARCHAR(30) DEFAULT NULL,
    `reported_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_computer_time` (`computer_id`, `reported_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康上报记录';

-- --------------------------------------------
-- 18. 课堂快照表 (screenshot)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `screenshot` (
    `id`            BIGINT       NOT NULL,
    `computer_id`   BIGINT       NOT NULL,
    `image_url`     VARCHAR(500) NOT NULL COMMENT '图片存储路径/URL',
    `image_size_kb` INT          DEFAULT NULL,
    `width`         INT          DEFAULT NULL,
    `height`        INT          DEFAULT NULL,
    `blurred`       TINYINT      DEFAULT 1 COMMENT '是否模糊处理隐私区域: 0=否,1=是',
    `captured_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_computer_captured` (`computer_id`, `captured_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课堂快照';

-- --------------------------------------------
-- 19. 查询令牌表 (access_token)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `access_token` (
    `id`            BIGINT       NOT NULL,
    `token_code`    VARCHAR(64)  NOT NULL COMMENT '随机令牌码',
    `name`          VARCHAR(100) DEFAULT NULL COMMENT '令牌名称(用途说明)',
    `class_id`      BIGINT       NOT NULL COMMENT '绑定班级',
    `creator_id`    BIGINT       NOT NULL COMMENT '创建者',
    `expire_time`   DATETIME     NOT NULL COMMENT '过期时间',
    `max_use_count` INT          DEFAULT 0 COMMENT '最大使用次数(0=无限)',
    `used_count`    INT          DEFAULT 0 COMMENT '已使用次数',
    `permissions`   JSON         DEFAULT NULL COMMENT '权限范围 [course, announcement, health, answer, screenshot]',
    `status`        TINYINT      DEFAULT 1 COMMENT '0=已禁用/已过期,1=有效',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `revoked_at`    DATETIME     DEFAULT NULL COMMENT '撤销时间',
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_code` (`token_code`),
    INDEX `idx_class` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='查询令牌';

-- --------------------------------------------
-- 20. 远程消息表 (remote_message)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `remote_message` (
    `id`            BIGINT       NOT NULL,
    `sender_id`     BIGINT       NOT NULL COMMENT '发送者',
    `title`         VARCHAR(200) NOT NULL COMMENT '消息标题',
    `content`       TEXT         NOT NULL COMMENT '消息内容',
    `target_scope`  JSON         DEFAULT NULL COMMENT '目标范围',
    `message_type`  TINYINT      DEFAULT 1 COMMENT '1=通知,2=任务布置',
    `read_by`       JSON         DEFAULT NULL COMMENT '已读用户ID列表',
    `sent_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expire_at`     DATETIME     DEFAULT NULL,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_sender_sent` (`sender_id`, `sent_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='远程消息';

-- --------------------------------------------
-- 21. 灵动岛全局配置模板表 (island_config)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `island_config` (
    `id`            BIGINT       NOT NULL,
    `config_name`   VARCHAR(100) NOT NULL COMMENT '配置名称',
    `scope_type`    TINYINT      DEFAULT 1 COMMENT '1=全局默认,2=年级覆盖,3=班级覆盖',
    `scope_id`      BIGINT       DEFAULT NULL COMMENT '关联的组织ID(年级或班级)',
    `config_json`   TEXT         NOT NULL COMMENT '灵动岛配置JSON',
    `is_active`     TINYINT      DEFAULT 1 COMMENT '是否生效',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    `version`       INT          DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_scope` (`scope_type`, `scope_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='灵动岛配置模板';

-- --------------------------------------------
-- 22. 插件表 (plugin)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `plugin` (
    `id`            BIGINT       NOT NULL,
    `name`          VARCHAR(100) NOT NULL COMMENT '插件名称',
    `code`          VARCHAR(50)  NOT NULL COMMENT '插件编码',
    `type`          TINYINT      DEFAULT 1 COMMENT '1=前端组件,2=后端脚本(Groovy),3=Jar包',
    `version`       VARCHAR(20)  DEFAULT NULL,
    `description`   VARCHAR(500) DEFAULT NULL,
    `file_path`     VARCHAR(500) DEFAULT NULL COMMENT '插件文件路径',
    `entry_point`   VARCHAR(200) DEFAULT NULL COMMENT '入口点(类名或组件名)',
    `permissions_declared` JSON  DEFAULT NULL COMMENT '声明的权限',
    `config_schema` TEXT         DEFAULT NULL COMMENT '配置项Schema JSON',
    `config_values` TEXT         DEFAULT NULL COMMENT '当前配置值JSON',
    `author`        VARCHAR(100) DEFAULT NULL,
    `enabled`       TINYINT      DEFAULT 0,
    `installed_at`  DATETIME     DEFAULT NULL,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    `version_num`   INT          DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='插件';

-- --------------------------------------------
-- 23. 审计日志表 (audit_log)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `audit_log` (
    `id`            BIGINT       NOT NULL,
    `user_id`       BIGINT       DEFAULT NULL,
    `module`        VARCHAR(50)  NOT NULL COMMENT '模块名',
    `action`        VARCHAR(50)  NOT NULL COMMENT '操作',
    `target_type`   VARCHAR(50)  DEFAULT NULL COMMENT '目标对象类型',
    `target_id`     BIGINT       DEFAULT NULL COMMENT '目标对象ID',
    `detail`        TEXT         DEFAULT NULL COMMENT '操作详情',
    `ip_address`    VARCHAR(45)  DEFAULT NULL,
    `user_agent`    VARCHAR(500) DEFAULT NULL,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_time` (`user_id`, `created_at`),
    INDEX `idx_module_action` (`module`, `action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志';

-- --------------------------------------------
-- 24. 登录日志表 (login_log)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `login_log` (
    `id`            BIGINT       NOT NULL,
    `user_id`       BIGINT       DEFAULT NULL,
    `username`      VARCHAR(50)  DEFAULT NULL,
    `login_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ip_address`    VARCHAR(45)  DEFAULT NULL,
    `user_agent`    VARCHAR(500) DEFAULT NULL,
    `success`       TINYINT      NOT NULL COMMENT '0=失败,1=成功',
    `fail_reason`   VARCHAR(200) DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_user_time` (`user_id`, `login_time`),
    INDEX `idx_success` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志';

-- ============================================
-- 初始化种子数据
-- ============================================

-- 内置角色
INSERT INTO `role` (`id`, `name`, `code`, `is_builtin`, `permission_mask`, `data_scope`) VALUES
(1,  '超级管理员', 'SUPER_ADMIN', 1, -1, 1),
(2,  '分区管理员', 'ZONE_ADMIN',   1, 1073741823, 2),
(3,  '学校管理员', 'SCHOOL_ADMIN', 1, 268435455, 3),
(4,  '年级管理员', 'GRADE_ADMIN',  1, 67108863,  4),
(5,  '班级管理员', 'CLASS_ADMIN',  1, 16777215,  5),
(6,  '普通用户',   'VIEWER',       1, 1048575,   6);

-- 默认超级管理员 (密码: admin123456, BCrypt加密)
INSERT INTO `user` (`id`, `username`, `password_hash`, `real_name`, `org_id`, `role_id`,
                    `must_change_pwd`, `status`) VALUES
(1, 'admin', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.ajhHCoEdFwq2ne',
 '系统管理员', NULL, 1, 0, 1);

-- 内置情景模式
INSERT INTO `scene_mode` (`id`, `name`, `code`, `icon`, `color`, `is_lock_screen`,
                          `block_internet`, `reminder_interval`, `is_system`) VALUES
(1, '上课专注模式', 'CLASS',   'book-open', '#409EFF', 0, 0, 5,  1),
(2, '课间模式',     'BREAK',   'coffee',    '#67C23A', 0, 0, 0,  1),
(3, '自习/测验模式','STUDY',   'pen',       '#E6A23C', 0, 1, 0,  1),
(4, '考试模式',     'EXAM',    'lock',      '#F56C6C', 1, 1, 0,  1);

-- 默认灵动岛全局配置
INSERT INTO `island_config` (`id`, `config_name`, `scope_type`, `config_json`, `is_active`) VALUES
(1, '默认灵动岛配置', 1, '{"position":"top-center","height":48,"backgroundColor":"rgba(0,0,0,0.7)","textColor":"#ffffff","borderRadius":24,"showLogo":true,"logoUrl":"","showDateTime":true,"dateTimeFormat":"YYYY-MM-DD HH:mm:ss","showCurrentCourse":true,"showWeather":true,"showMarquee":true,"showModeIcon":true,"showHealthDot":true,"showMessageBadge":true,"autoHideFullscreen":false}', 1);

-- 默认天气API配置 (uapis.cn预设)
INSERT INTO `weather_config` (`id`, `name`, `api_url`, `api_method`, `api_key`,
                               `city_param_name`, `response_json_path`,
                               `temp_path`, `weather_path`, `weather_icon_path`,
                               `forecast_path`, `hourly_path`, `aqi_path`,
                               `is_default`, `city_default`, `fetch_interval_minutes`, `enabled`) VALUES
(1, 'UApis天气服务(默认)', 'https://uapis.cn/api/v1/misc/weather', 'GET',
 'uapi-j5jky0ggkSIABn-OreTfovNhfyEPDVRewQ3BgEBQ', 'city', '$',
 '$.temperature', '$.weather', '$.weather_icon',
 '$.forecast', '$.hourly_forecast', '$.aqi',
 1, '北京', 30, 1);

-- 基础权限数据
INSERT INTO `permission` (`id`, `name`, `code`, `type`, `path`, `method`) VALUES
-- 系统管理
(101, '组织管理', 'sys:org:list',     2, '/system/org',     NULL),
(102, '组织新增', 'sys:org:create',  3, '/api/v1/org',      'POST'),
(103, '组织修改', 'sys:org:update',  3, '/api/v1/org/*',   'PUT'),
(104, '组织删除', 'sys:org:delete',  3, '/api/v1/org/*',   'DELETE'),
(111, '用户管理', 'sys:user:list',    2, '/system/user',    NULL),
(112, '用户新增', 'sys:user:create', 3, '/api/v1/user',     'POST'),
(113, '用户修改', 'sys:user:update', 3, '/api/v1/user/*',   'PUT'),
(114, '用户删除', 'sys:user:delete', 3, '/api/v1/user/*',   'DELETE'),
(121, '角色管理', 'sys:role:list',    2, '/system/role',    NULL),
(122, '角色新增', 'sys:role:create', 3, '/api/v1/role',     'POST'),
(123, '角色修改', 'sys:role:update', 3, '/api/v1/role/*',   'PUT'),
-- 灵动岛
(201, '灵动岛配置', 'island:config:view',  2, '/island/config', NULL),
(202, '灵动岛编辑', 'island:config:edit',  3, '/api/v1/island/config', 'PUT'),
-- 情景模式
(301, '模式管理', 'mode:list',   2, '/mode/list',   NULL),
(302, '模式控制', 'mode:control', 3, '/api/v1/mode/switch', 'POST'),
-- 考试管理
(401, '考试管理', 'exam:list',   2, '/exam/list',   NULL),
(402, '考试创建', 'exam:create', 3, '/api/v1/exam',   'POST'),
(403, '考试控制', 'exam:control', 3, '/api/v1/exam/*/control', 'POST'),
-- 公告
(501, '公告管理', 'announce:list',   2, '/announcement', NULL),
(502, '公告发布', 'announce:publish', 3, '/api/v1/announcement', 'POST'),
(503, '紧急广播', 'announce:emergency',3, '/api/v1/announcement/emergency','POST'),
-- 远程消息
(601, '远程消息', 'message:send', 3, '/api/v1/remote/message', 'POST'),
-- 答案
(701, '答案管理', 'answer:manage', 2, '/answer', NULL),
(702, '答案发布', 'answer:publish',3, '/api/v1/answer/*/publish','POST'),
-- 健康
(801, '健康看板', 'health:dashboard', 2, '/health/dashboard', NULL),
(802, '健康修复', 'health:fix', 3, '/api/v1/health/fix', 'POST'),
-- 设备
(901, '设备管理', 'computer:list', 2, '/computer', NULL),
-- 令牌
(1001, '令牌管理', 'token:manage', 2, '/token', NULL),
-- 插件
(1101, '插件管理', 'plugin:manage', 2, '/plugin', NULL);

-- 根组织节点
INSERT INTO `sys_organization` (`id`, `parent_id`, `name`, `code`, `type`, `level`, `sort_order`) VALUES
(1, NULL, '灵岛校园总部', 'HQ', 1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
