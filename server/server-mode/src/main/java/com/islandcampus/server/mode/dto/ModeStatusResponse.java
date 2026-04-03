package com.islandcampus.server.mode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "当前模式状态响应")
public class ModeStatusResponse {

    @Schema(description = "当前模式名称")
    private String currentModeName;

    @Schema(description = "当前模式编码")
    private String currentModeCode;

    @Schema(description = "下次切换时间")
    private String nextModeTime;

    @Schema(description = "当前课程信息")
    private String currentCourse;
}
