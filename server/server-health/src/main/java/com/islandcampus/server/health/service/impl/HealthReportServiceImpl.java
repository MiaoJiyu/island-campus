package com.islandcampus.server.health.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.common.redis.PublisherService;
import com.islandcampus.server.common.websocket.WebSocketService;
import com.islandcampus.server.health.dto.*;
import com.islandcampus.server.health.entity.HealthReport;
import com.islandcampus.server.health.mapper.HealthReportMapper;
import com.islandcampus.server.health.service.HealthReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthReportServiceImpl extends ServiceImpl<HealthReportMapper, HealthReport>
        implements HealthReportService {
    private final PublisherService publisherService;
    private final WebSocketService webSocketService;

    private static final BigDecimal CRIT_DISK = new BigDecimal("5");
    private static final BigDecimal WARN_DISK = new BigDecimal("20");
    private static final BigDecimal CRIT_TEMP = new BigDecimal("85");
    private static final BigDecimal WARN_TEMP = new BigDecimal("70");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void report(Long computerId, HealthReportDTO dto) {
        HealthReport r = new HealthReport();
        r.setComputerId(computerId); r.setCpuTemp(dto.getCpuTemp()); r.setCpuUsage(dto.getCpuUsage());
        r.setMemoryUsage(dto.getMemoryUsage()); r.setDiskTotalGb(dto.getDiskTotalGb());
        r.setDiskFreeGb(dto.getDiskFreeGb()); r.setProcessList(dto.getProcessList());
        r.setSuspiciousProcesses(dto.getSuspiciousProcesses()); r.setUptimeSeconds(dto.getUptimeSeconds());
        r.setClientVersion(dto.getClientVersion()); r.setReportedAt(LocalDateTime.now());
        this.save(r);

        try {
            FixResult fix = checkAutoFix(computerId);
            if (fix.needFix()) {
                Map<String, Object> cmd = Map.of("action", fix.action(), "command", fix.command(),
                        "timestamp", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
                publisherService.publish("health:fix:" + computerId, JSON.toJSONString(cmd));
                webSocketService.sendToComputer(computerId, Map.of("type", "health_fix", "data", cmd));
            }
        } catch (Exception e) { log.error("自动修复检查异常: computerId={}", computerId, e); }
    }

    @Override public HealthReport getLatestReport(Long computerId) { return baseMapper.selectLatestByComputerId(computerId); }
    @Override public List<Map<String, Object>> getHistory(Long computerId, int days) { return baseMapper.selectHistoryTrend(computerId, LocalDateTime.now().minusDays(days)); }

    @Override
    public String evaluateHealth(HealthReport r) {
        if (r == null) return "unknown";
        int score = 0;
        if (r.getCpuTemp() != null) { if (r.getCpuTemp().compareTo(CRIT_TEMP) >= 0) score += 3; else if (r.getCpuTemp().compareTo(WARN_TEMP) >= 0) score++; }
        if (r.getDiskFreeGb() != null) { if (r.getDiskFreeGb().compareTo(CRIT_DISK) < 0) score += 3; else if (r.getDiskFreeGb().compareTo(WARN_DISK) < 0) score++; }
        if (r.getSuspiciousProcesses() != null && !r.getSuspiciousProcesses().isBlank()) score += 2;
        return score >= 5 ? "red" : score >= 2 ? "yellow" : "green";
    }

    @Override
    public FixResult checkAutoFix(Long computerId) {
        HealthReport latest = getLatestReport(computerId);
        if (latest == null) return new FixResult(false, null, null, "无数据");
        if (latest.getDiskFreeGb() != null && latest.getDiskFreeGb().compareTo(CRIT_DISK) < 0)
            return new FixResult(true, "clean", "clean_temp", "磁盘不足" + latest.getDiskFreeGb() + "GB");
        if (latest.getSuspiciousProcesses() != null && !latest.getSuspiciousProcesses().isBlank())
            return new FixResult(true, "kill", latest.getSuspiciousProcesses(), "发现可疑进程");
        return new FixResult(false, null, null, "正常");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeFix(Long computerId, FixCommandDTO dto) {
        Map<String, Object> cmd = new HashMap<>();
        cmd.put("action", dto.getAction()); cmd.put("params", dto.getParams() != null ? dto.getParams() : Map.of());
        cmd.put("triggeredBy", "manual"); cmd.put("timestamp", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
        log.info("手动修复指令: computerId={}, action={}", computerId, dto.getAction());
        webSocketService.sendToComputer(computerId, Map.of("type", "health_fix", "data", cmd));
        publisherService.publish("health:fix:" + computerId, JSON.toJSONString(cmd));
    }

    @Override
    public HealthDashboardVO getDashboard(Long orgId) {
        // 简化实现 - 实际项目中应按组织聚合统计
        LambdaQueryWrapper<ComputerRef> w = new LambdaQueryWrapper<>(); // placeholder for actual query
        return HealthDashboardVO.builder().totalComputers(0L).onlineCount(0L).healthyCount(0L).warningCount(0L).errorCount(0L).classStats(List.of()).build();
    }
    // Placeholder for Computer entity reference
    record ComputerRef(Long id, String name, Long classId) {}

    @Override
    public List<HealthStatusVO> getStatusList(Long orgId) {
        LambdaQueryWrapper<HealthReport> w = new LambdaQueryWrapper<>();
        w.orderByDesc(HealthReport::getReportedAt).last("LIMIT 50");
        return this.list(w).stream()
                .map(r -> HealthStatusVO.builder().computerId(r.getComputerId()).name("设备-" + r.getComputerId())
                        .status(evaluateHealth(r)).cpuTemp(r.getCpuTemp()).diskFreeGb(r.getDiskFreeGb())
                        .lastReportTime(r.getReportedAt()).build())
                .toList();
    }
}
