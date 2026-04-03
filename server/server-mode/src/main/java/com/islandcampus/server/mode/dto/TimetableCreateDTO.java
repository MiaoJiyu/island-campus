package com.islandcampus.server.mode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "课程表创建DTO")
public class TimetableCreateDTO {

    @NotNull(message = "班级ID不能为空")
    @Schema(description = "班级ID")
    private Long classId;

    @NotNull(message = "周几不能为空")
    @Schema(description = "周几(1-7)")
    private Integer dayOfWeek;

    @NotNull(message = "第几节不能为空")
    @Schema(description = "第几节")
    private Integer period;

    @NotBlank(message = "课程名不能为空")
    @Schema(description = "科目/课程名")
    private String subject;

    @Schema(description = "教师姓名")
    private String teacherName;

    @Schema(description = "教室")
    private String room;

    @Schema(description = "开始时间 HH:mm")
    private String startTime;

    @Schema(description = "结束时间 HH:mm")
    private String endTime;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "学期")
    private String semester;
}
