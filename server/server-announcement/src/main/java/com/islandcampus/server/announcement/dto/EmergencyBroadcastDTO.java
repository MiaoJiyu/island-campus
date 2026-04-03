package com.islandcampus.server.announcement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "紧急广播请求体")
public class EmergencyBroadcastDTO {

    @NotBlank(message = "标题不能为空")
    @Schema(description = "标题")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "内容")
    private String content;

    @Schema(description = "目标范围(组织ID列表)")
    private List<Long> targetScope;
}
