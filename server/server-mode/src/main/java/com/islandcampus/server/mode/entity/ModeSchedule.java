package com.islandcampus.server.mode.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mode_schedule")
public class ModeSchedule extends BaseEntity {

    private Long classId;

    private Long modeId;

    /**
     * 触发类型: 1手动 2课程表 3Cron
     */
    private Integer triggerType;

    /**
     * Cron表达式
     */
    private String cronExpr;

    private Long targetModeId;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;

    private Integer status;
}
