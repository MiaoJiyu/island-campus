package com.islandcampus.server.plugin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "插件上传请求")
public class PluginUploadDTO {

    @Schema(description = "插件文件", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;

    @NotNull(message = "插件类型不能为空")
    @Schema(description = "类型: 1-前端 2-Groovy 3-JAR", example = "2")
    private Integer type;

    @NotBlank(message = "插件编码不能为空")
    @Size(max = 100, message = "插件编码不能超过100字符")
    @Schema(description = "插件唯一编码")
    private String code;

    @Size(max = 200, message = "入口点超长")
    @Schema(description = "入口点（Groovy脚本类名/JAR主类/前端组件路径）")
    private String entryPoint;

    @Size(max = 500, message = "描述超长")
    @Schema(description = "插件描述")
    private String description;
}
