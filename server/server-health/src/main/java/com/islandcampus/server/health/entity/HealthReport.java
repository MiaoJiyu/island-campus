package com.islandcampus.server.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("health_report")
public class HealthReport implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long computerId;
    private BigDecimal cpuTemp;
    private BigDecimal cpuUsage;
    private BigDecimal memoryUsage;
    private BigDecimal diskTotalGb;
    private BigDecimal diskFreeGb;
    private String processList;
    private String suspiciousProcesses;
    private Long uptimeSeconds;
    private String clientVersion;
    private LocalDateTime reportedAt;
}
