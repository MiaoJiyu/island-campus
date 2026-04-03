package com.islandcampus.server.health.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "健康上报请求体")
public class HealthReportDTO {
    private BigDecimal cpuTemp;
    private BigDecimal cpuUsage;
    private BigDecimal memoryUsage;
    private BigDecimal diskTotalGb;
    private BigDecimal diskFreeGb;
    private String processList;
    private String suspiciousProcesses;
    private Long uptimeSeconds;
    @NotBlank(message = "客户端版本不能为空")
    private String clientVersion;
}
