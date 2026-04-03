package com.islandcampus.server.mode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "切换计划创建DTO")
public class ScheduleCreateDTO {

    @NotNull(message = "班级ID不能为空")
    @Schema(description = "班级ID")
    private Long classId;

    @NotNull(message = "模式ID不能为空")
    @Schema(description = "目标模式ID")
    private Long modeId;

    /**
     * 触发类型: 1手动 2课程表 3Cron
     */
    @NotNull(message = "触发类型不能为空")
    @Schema(description = "触发类型: 1手动 2课程表 3Cron")
    private Integer triggerType;

    @Schema(description = "Cron表达式(触发类型为3时必填)")
    private String cronExpr;

    @Schema(description = "目标模式ID(用于自动切换)")
    private Long targetModeId;

    @Schema(description = "生效开始时间")
    private LocalDateTime effectiveFrom;

    @Schema(description = "生效结束时间")
    private LocalDateTime effectiveTo;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;
}
