package com.islandcampus.server.announcement.mapper.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("announcement")
public class Announcement extends BaseEntity {

    private String title;

    private String content;

    /**
     * 类型: 1-普通 2-紧急
     */
    private Integer type;

    private Long publisherId;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String targetScope;

    /**
     * 是否需要确认回执
     */
    private Integer needConfirm;

    /**
     * 优先级: 0-普通 1-高 2-紧急
     */
    private Integer priority;

    private LocalDateTime publishTime;

    private LocalDateTime expireTime;

    /**
     * 状态: 0-草稿 1-发布 2-撤回
     */
    private Integer status;
}
