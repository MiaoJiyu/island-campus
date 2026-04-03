package com.islandcampus.server.computer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "灵动岛个性配置覆盖请求")
public class IslandConfigOverrideDTO {

    @NotBlank(message = "配置JSON不能为空")
    @Schema(description = "灵动岛个性化配置JSON", example = "{\"layout\":\"compact\",\"widgets\":[\"weather\",\"time\"]}")
    private String configJson;
}
