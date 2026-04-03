package com.islandcampus.server.computer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "心跳上报请求")
public class ComputerHeartbeatDTO {

    @Size(max = 45, message = "IP地址长度超限")
    @Schema(description = "当前IP地址")
    private String ipAddress;

    @Size(max = 50, message = "客户端版本号超长")
    @Schema(description = "客户端版本")
    private String clientVersion;

    @Size(max = 50, message = "分辨率格式超长")
    @Schema(description = "屏幕分辨率")
    private String resolution;

    @Size(max = 200, message = "操作系统信息超长")
    @Schema(description = "操作系统信息")
    private String osInfo;
}
