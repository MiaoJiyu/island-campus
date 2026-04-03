package com.islandcampus.server.exam.mapper.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam")
public class Exam extends BaseEntity {

    private String name;

    /**
     * 范围类型: 1-班级 2-年级 3-全校
     */
    private Integer scopeType;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String targetOrgIds;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String targetComputerIds;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String releasePassword;

    private String releasePasswordHash;

    /**
     * 状态: 0-未开始 1-进行中 2-已结束 3-已取消
     */
    private Integer status;

    private Long creatorId;

    private LocalDateTime actualStart;

    private LocalDateTime actualEnd;

    private String remark;
}
