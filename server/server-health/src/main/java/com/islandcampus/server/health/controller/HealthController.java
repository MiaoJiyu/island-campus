package com.islandcampus.server.health.controller;

import com.islandcampus.server.common.base.R;
import com.islandcampus.server.health.dto.FixCommandDTO;
import com.islandcampus.server.health.dto.HealthDashboardVO;
import com.islandcampus.server.health.dto.HealthReportDTO;
import com.islandcampus.server.health.dto.HealthStatusVO;
import com.islandcampus.server.health.entity.HealthReport;
import com.islandcampus.server.health.service.HealthReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "健康管家")
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {
    private final HealthReportService healthService;

    @Operation(summary = "客户端上报健康数据")
    @PostMapping("/report")
    public R<Void> report(@RequestParam Long computerId, @Valid @RequestBody HealthReportDTO dto) {
        healthService.report(computerId, dto); return R.ok(null);
    }

    @Operation(summary = "获取最新健康报告")
    @GetMapping("/latest")
    public R<HealthReport> latest(@RequestParam Long computerId) { return R.ok(healthService.getLatestReport(computerId)); }

    @Operation(summary = "获取历史趋势")
    @GetMapping("/history")
    public R<List<Map<String, Object>>> history(@RequestParam Long computerId, @RequestParam(defaultValue = "7") int days) {
        return R.ok(healthService.getHistory(computerId, days));
    }

    @Operation(summary = "健康看板")
    @GetMapping("/dashboard")
    public R<HealthDashboardVO> dashboard(@RequestParam(required = false) Long orgId) {
        return R.ok(healthService.getDashboard(orgId));
    }

    @Operation(summary = "手动触发修复")
    @PostMapping("/fix/{computerId}")
    public R<Void> fix(@PathVariable Long computerId, @Valid @RequestBody FixCommandDTO dto) {
        healthService.executeFix(computerId, dto); return R.ok("修复指令已下发", null);
    }

    @Operation(summary = "设备状态列表")
    @GetMapping("/status-list")
    public R<List<HealthStatusVO>> statusList(@RequestParam(required = false) Long orgId) {
        return R.ok(healthService.getStatusList(orgId));
    }
}
