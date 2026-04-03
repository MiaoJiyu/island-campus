package com.islandcampus.server.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考试指令")
public class ExamCommand {

    @Schema(description = "指令类型: exam_start/exam_end")
    private String type;

    @Schema(description = "指令数据")
    private ExamCommandData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamCommandData {
        @Schema(description = "密码哈希")
        private String passwordHash;

        @Schema(description = "考试名称")
        private String examName;

        @Schema(description = "计划结束时间")
        private LocalDateTime endTime;
    }
}
