package com.islandcampus.server.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "创建考试请求体")
public class ExamCreateDTO {

    @NotBlank(message = "考试名称不能为空")
    @Schema(description = "考试名称")
    private String name;

    @NotNull(message = "范围类型不能为空")
    @Schema(description = "范围类型: 1-班级 2-年级 3-全校")
    private Integer scopeType;

    @Schema(description = "目标组织ID列表")
    private List<Long> targetOrgIds;

    @Schema(description = "目标电脑ID列表")
    private List<Long> targetComputerIds;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "释放密码(可选，不填自动生成)")
    private String releasePassword;

    @Schema(description = "备注")
    private String remark;
}
