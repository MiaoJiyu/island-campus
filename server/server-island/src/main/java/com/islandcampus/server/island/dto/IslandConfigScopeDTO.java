package com.islandcampus.server.island.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "灵动岛配置范围DTO")
public class IslandConfigScopeDTO {

    @NotBlank(message = "配置名称不能为空")
    @Schema(description = "配置名称")
    private String configName;

    @NotNull(message = "作用域类型不能为空")
    @Schema(description = "作用域类型: 1全局 2年级 3班级")
    private Integer scopeType;

    @Schema(description = "作用域ID(全局时为null)")
    private Long scopeId;

    @Schema(description = "配置JSON")
    private String configJson;
}
