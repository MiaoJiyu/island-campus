package com.islandcampus.server.weather.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "天气预警规则创建/更新请求体")
public class AlertRuleCreateDTO {

    @NotBlank(message = "规则名称不能为空")
    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "条件表达式(JSON)")
    private String conditionExpr;

    @NotNull(message = "动作类型不能为空")
    @Schema(description = "动作类型: 1-闪烁 2-公告 3-远程通知 4-多动作")
    private Integer actionType;

    @Schema(description = "动作配置(JSON)")
    private String actionConfig;

    @NotNull(message = "关联的天气配置ID不能为空")
    @Schema(description = "关联的天气配置ID")
    private Long weatherConfigId;

    @Schema(description = "优先级")
    private Integer priority = 0;

    @Schema(description = "是否启用")
    private Integer enabled = 1;
}
