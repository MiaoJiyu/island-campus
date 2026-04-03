package com.islandcampus.server.announcement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "确认回执请求体")
public class ConfirmDTO {

    @NotNull(message = "公告ID不能为空")
    @Schema(description = "公告ID")
    private Long announcementId;
}
