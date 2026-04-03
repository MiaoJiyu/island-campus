package com.islandcampus.server.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "创建答案集请求体")
public class AnswerSetCreateDTO {
    @NotBlank(message = "答案集标题不能为空")
    private String title;

    @Schema(description = "目标班级ID列表")
    private List<Long> targetClassIds;

    private String questionsJson;

    @NotNull(message = "发布类型不能为空")
    @Schema(allowableValues = {"1", "2"})
    private Integer publishType;

    private LocalDateTime publishTime;
    private String password;
}
