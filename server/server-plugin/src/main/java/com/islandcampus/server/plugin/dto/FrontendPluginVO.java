package com.islandcampus.server.plugin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "前端插件视图对象")
public class FrontendPluginVO {

    @Schema(description = "插件编码")
    private String code;

    @Schema(description = "插件名称")
    private String name;

    @Schema(description = "组件路径")
    private String componentPath;

    @Schema(description = "配置Schema")
    private Object configSchema;

    @Schema(description = "插件版本")
    private String version;

    @Schema(description = "插件描述")
    private String description;
}
