package com.islandcampus.server.computer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "设备更新请求")
public class ComputerUpdateDTO {

    @Size(max = 100, message = "设备名称不能超过100字符")
    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "所属班级ID")
    private Long classId;

    @Size(max = 50, message = "分辨率格式超长")
    @Schema(description = "屏幕分辨率")
    private String resolution;

    @Size(max = 500, message = "备注不能超过500字符")
    @Schema(description = "备注")
    private String remark;
}
