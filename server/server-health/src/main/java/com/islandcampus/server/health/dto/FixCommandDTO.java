package com.islandcampus.server.health.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Map;

@Data
@Schema(description = "修复命令请求体")
public class FixCommandDTO {
    @NotBlank @Schema(allowableValues = {"clean", "kill"})
    private String action;
    private Map<String, Object> params;
}
