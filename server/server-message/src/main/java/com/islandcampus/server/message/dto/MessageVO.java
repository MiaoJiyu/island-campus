package com.islandcampus.server.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息视图对象")
public class MessageVO {
    private Long id;
    private String title;
    private String content;
    private String senderName;
    private LocalDateTime sentAt;
    private Boolean read;
    private Integer type;
}
