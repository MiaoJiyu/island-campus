package com.islandcampus.server.computer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Schema(description = "设备视图对象")
public class ComputerVO {

    @Schema(description = "设备ID")
    private Long id;

    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "MAC地址")
    private String macAddress;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "所属班级名称")
    private String className;

    @Schema(description = "是否在线")
    private Boolean isOnline;

    @Schema(description = "健康状态: 0红 1黄 2绿")
    private Integer healthStatus;

    @Schema(description = "客户端版本")
    private String clientVersion;

    @Schema(description = "最后心跳时间")
    private LocalDateTime lastHeartbeat;

    @Schema(description = "在线时长（分钟）")
    private Long onlineDuration;

    @Schema(description = "屏幕分辨率")
    private String resolution;

    @Schema(description = "操作系统信息")
    private String osInfo;

    @Schema(description = "备注")
    private String remark;
}
