package com.islandcampus.server.token.mapper.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("access_token")
public class AccessToken extends BaseEntity {

    /**
     * 64位随机令牌码
     */
    private String tokenCode;

    private String name;

    private Long classId;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 最大使用次数，null表示不限
     */
    private Integer maxUseCount;

    /**
     * 已使用次数
     */
    private Integer usedCount;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String permissions;

    /**
     * 状态: 0-禁用/过期 1-有效
     */
    private Integer status;

    /**
     * 撤销时间
     */
    private LocalDateTime revokedAt;
}
