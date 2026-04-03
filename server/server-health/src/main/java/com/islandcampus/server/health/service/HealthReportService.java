package com.islandcampus.server.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.health.dto.FixCommandDTO;
import com.islandcampus.server.health.dto.HealthDashboardVO;
import com.islandcampus.server.health.dto.HealthReportDTO;
import com.islandcampus.server.health.dto.HealthStatusVO;
import com.islandcampus.server.health.entity.HealthReport;

import java.util.List;
import java.util.Map;

public interface HealthReportService extends IService<HealthReport> {
    void report(Long computerId, HealthReportDTO dto);
    HealthReport getLatestReport(Long computerId);
    List<Map<String, Object>> getHistory(Long computerId, int days);
    String evaluateHealth(HealthReport report);
    FixResult checkAutoFix(Long computerId);
    void executeFix(Long computerId, FixCommandDTO dto);
    HealthDashboardVO getDashboard(Long orgId);
    List<HealthStatusVO> getStatusList(Long orgId);

    record FixResult(boolean needFix, String action, String command, String reason) {}
}
