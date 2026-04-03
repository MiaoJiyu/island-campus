package com.islandcampus.server.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "发送消息请求体")
public class SendMessageDTO {
    @NotBlank(message = "消息标题不能为空")
    private String title;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    @NotNull(message = "目标类型不能为空")
    @Schema(allowableValues = {"org", "class", "computer"})
    private String targetType;

    @NotNull(message = "目标ID列表不能为空")
    private List<Long> targetIds;

    @Schema(description = "消息类型: 1=通知 2=任务")
    private Integer messageType = 1;
}
