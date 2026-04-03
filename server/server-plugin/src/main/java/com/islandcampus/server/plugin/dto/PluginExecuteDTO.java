package com.islandcampus.server.plugin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "后端插件执行请求")
public class PluginExecuteDTO {

    @NotBlank(message = "插件编码不能为空")
    @Schema(description = "要执行的插件编码")
    private String pluginCode;

    @Schema(description = "执行参数")
    private Map<String, Object> params;
}
