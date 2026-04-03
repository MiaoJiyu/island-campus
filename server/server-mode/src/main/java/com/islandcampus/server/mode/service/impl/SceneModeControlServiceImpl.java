package com.islandcampus.server.mode.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.common.websocket.WebSocketService;
import com.islandcampus.server.mode.dto.ModeStatusResponse;
import com.islandcampus.server.mode.dto.ModeSwitchRequest;
import com.islandcampus.server.mode.entity.Computer;
import com.islandcampus.server.mode.entity.ModeSchedule;
import com.islandcampus.server.mode.entity.SceneMode;
import com.islandcampus.server.mode.entity.Timetable;
import com.islandcampus.server.mode.mapper.ComputerMapper;
import com.islandcampus.server.mode.mapper.ModeScheduleMapper;
import com.islandcampus.server.mode.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SceneModeControlServiceImpl implements SceneModeControlService {

    private final ComputerMapper computerMapper;
    private final SceneModeService sceneModeService;
    private final ModeScheduleService modeScheduleService;
    private final TimetableService timetableService;
    private final WebSocketService webSocketService;

    @Override
    public void switchMode(ModeSwitchRequest request) {
        SceneMode targetMode = sceneModeService.getById(request.getModeId());
        if (targetMode == null) {
            throw new BusinessException("目标模式不存在");
        }

        for (Long computerId : request.getTargetComputerIds()) {
            doSwitchComputer(computerId, request.getModeId(), targetMode.getName(),
                    targetMode.getCode(), "manual");
        }

        System.out.println("[AUDIT] 手动切换模式: computerIds=" + request.getTargetComputerIds()
                + ", modeId=" + request.getModeId()
                + ", operator=" + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());
    }

    @Override
    public void autoSwitchBySchedule() {
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue();

        // 获取所有启用的Cron/课程表触发计划
        List<ModeSchedule> schedules = modeScheduleService.list(
                new LambdaQueryWrapper<ModeSchedule>()
                        .eq(ModeSchedule::getStatus, 1)
                        .ge(ModeSchedule::getEffectiveFrom, now.minusDays(1))
                        .le(ModeSchedule::getEffectiveTo, now.plusDays(1)));

        for (ModeSchedule schedule : schedules) {
            boolean shouldTrigger = false;

            if (schedule.getTriggerType() == 3 && schedule.getCronExpr() != null) {
                // Cron触发(简化判断，实际应用建议用Quartz/Spring Scheduler)
                shouldTrigger = checkCronMatch(schedule.getCronExpr(), now);
            } else if (schedule.getTriggerType() == 2) {
                // 课程表触发
                Timetable currentCourse = timetableService.getCurrentCourse(schedule.getClassId());
                if (currentCourse != null) {
                    shouldTrigger = true;
                }
            }

            if (shouldTrigger && schedule.getTargetModeId() != null) {
                switchClassMode(schedule.getClassId(), schedule.getTargetModeId());
                System.out.println("[AUDIT] 自动切换模式: classId=" + schedule.getClassId()
                        + ", targetModeId=" + schedule.getTargetModeId()
                        + ", triggerType=" + schedule.getTriggerType());
            }
        }
    }

    @Override
    public ModeStatusResponse getCurrentStatus(Long computerId) {
        Computer computer = computerMapper.selectById(computerId);
        if (computer == null || computer.getClassId() == null) {
            return new ModeStatusResponse(null, null, null, null);
        }

        Long currentModeId = modeScheduleService.determineCurrentMode(computer.getClassId());

        SceneMode currentMode = currentModeId != null ? sceneModeService.getById(currentModeId) : null;
        Timetable currentCourse = timetableService.getCurrentCourse(computer.getClassId());

        return new ModeStatusResponse(
                currentMode != null ? currentMode.getName() : "无",
                currentMode != null ? currentMode.getCode() : "none",
                calculateNextSwitchTime(computer.getClassId()),
                currentCourse != null ? currentCourse.getSubject() : "无"
        );
    }

    @Override
    public void switchClassMode(Long classId, Long modeId) {
        // 获取该班级所有在线电脑
        List<Computer> onlineComputers = computerMapper.selectList(
                new LambdaQueryWrapper<Computer>()
                        .eq(Computer::getClassId, classId)
                        .eq(Computer::getIsOnline, 1));

        SceneMode targetMode = sceneModeService.getById(modeId);

        for (Computer computer : onlineComputers) {
            doSwitchComputer(computer.getId(), modeId,
                    targetMode != null ? targetMode.getName() : "未知",
                    targetMode != null ? targetMode.getCode() : "unknown",
                    "auto_schedule");
        }
    }

    private void doSwitchComputer(Long computerId, Long modeId, String modeName,
                                  String modeCode, String source) {
        Map<String, Object> command = new HashMap<>();
        command.put("type", "MODE_SWITCH");
        command.put("computerId", computerId);
        command.put("targetModeId", modeId);
        command.put("targetModeName", modeName);
        command.put("targetModeCode", modeCode);
        command.put("source", source);
        command.put("timestamp", System.currentTimeMillis());

        webSocketService.sendToComputer(computerId, command);
        log.info("发送模式切换指令: computerId={}, mode={}, source={}", computerId, modeName, source);
    }

    private boolean checkCronMatch(String cronExpr, LocalDateTime time) {
        // 简化版cron匹配，生产环境建议使用 Quartz 或 Spring CronSequenceGenerator
        // 这里做基本的整点/半点匹配
        int minute = time.getMinute();
        int hour = time.getHour();

        if ("0 */30 * * * *".equals(cronExpr)) {
            return minute == 0 || minute == 30;
        }
        if ("0 0 * * * *".equals(cronExpr)) {
            return minute == 0 && time.getSecond() < 5;
        }
        return false;
    }

    private String calculateNextSwitchTime(Long classId) {
        // 计算下次自动切换时间(基于课程表下一节)
        Timetable nextCourse = getNextCourse(classId);
        if (nextCourse != null && nextCourse.getStartTime() != null) {
            return "下节课: " + nextCourse.getSubject()
                    + " (" + nextCourse.getStartTime() + ")";
        }
        return "无计划";
    }

    private Timetable getNextCourse(Long classId) {
        int dayOfWeek = java.time.LocalDate.now().getDayOfWeek().getValue();
        java.time.LocalTime now = java.time.LocalTime.now();

        List<Timetable> courses = timetableService.list(
                new LambdaQueryWrapper<Timetable>()
                        .eq(Timetable::getClassId, classId)
                        .eq(Timetable::getDayOfWeek, dayOfWeek)
                        .orderByAsc(Timetable::getStartTime));

        for (Timetable course : courses) {
            if (course.getStartTime() == null) continue;
            try {
                java.time.LocalTime start = java.time.LocalTime.parse(
                        course.getStartTime(), java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                if (start.isAfter(now)) {
                    return course;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }
}
