package com.islandcampus.server.computer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "设备注册请求")
public class ComputerRegisterDTO {

    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称不能超过100字符")
    @Schema(description = "设备名称")
    private String name;

    @NotBlank(message = "MAC地址不能为空")
    @Size(max = 50, message = "MAC地址格式不正确")
    @Schema(description = "MAC地址")
    private String macAddress;

    @Size(max = 45, message = "IP地址长度超限")
    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "所属班级ID")
    private Long classId;

    @Size(max = 50, message = "客户端版本号超长")
    @Schema(description = "客户端版本")
    private String clientVersion;

    @Size(max = 200, message = "操作系统信息超长")
    @Schema(description = "操作系统信息")
    private String osInfo;

    @Size(max = 50, message = "分辨率格式超长")
    @Schema(description = "屏幕分辨率，如1920x1080")
    private String resolution;
}
