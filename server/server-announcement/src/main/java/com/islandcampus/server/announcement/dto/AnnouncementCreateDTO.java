package com.islandcampus.server.announcement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "创建公告请求体")
public class AnnouncementCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "类型: 1-普通 2-紧急")
    private Integer type = 1;

    @Schema(description = "目标范围(组织ID列表)")
    private List<Long> targetScope;

    @Schema(description = "是否需要确认")
    private Integer needConfirm = 0;

    @Schema(description = "优先级: 0-普通 1-高 2-紧急")
    private Integer priority = 0;

    @Schema(description = "发布时间(为空则立即发布)")
    private LocalDateTime publishTime;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;
}
