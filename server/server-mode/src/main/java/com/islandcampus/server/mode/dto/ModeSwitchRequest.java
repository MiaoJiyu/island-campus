package com.islandcampus.server.mode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "模式切换请求")
public class ModeSwitchRequest {

    @NotEmpty(message = "目标电脑ID列表不能为空")
    @Schema(description = "目标电脑ID列表")
    private List<Long> targetComputerIds;

    @NotNull(message = "模式ID不能为空")
    @Schema(description = "目标模式ID")
    private Long modeId;
}
