package com.islandcampus.server.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考试日历视图VO")
public class ExamCalendarVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "考试名称")
    private String examName;

    @Schema(description = "状态: 0-未开始 1-进行中 2-已结束 3-已取消")
    private Integer status;

    @Schema(description = "时间段")
    private String timeRange;
}
