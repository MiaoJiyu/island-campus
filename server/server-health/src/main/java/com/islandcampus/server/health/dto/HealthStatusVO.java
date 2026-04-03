package com.islandcampus.server.health.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatusVO {
    private Long computerId; private String name; private String status;
    private BigDecimal cpuTemp; private BigDecimal diskFreeGb; private LocalDateTime lastReportTime;
}
