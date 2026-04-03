package com.islandcampus.server.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "答案详情视图对象")
public class AnswerDetailVO {
    private Long id;
    private String title;
    private Integer status;
    private String statusName;
    private LocalDateTime publishedAt;
    private List<Object> questions; // QuestionItemDTO list
    private String targetClassIds;
}
