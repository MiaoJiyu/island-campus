package com.islandcampus.server.mode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.mode.dto.ScheduleCreateDTO;
import com.islandcampus.server.mode.entity.ModeSchedule;
import com.islandcampus.server.mode.mapper.ModeScheduleMapper;
import com.islandcampus.server.mode.service.ModeScheduleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ModeScheduleServiceImpl extends ServiceImpl<ModeScheduleMapper, ModeSchedule>
        implements ModeScheduleService {

    @Override
    public ModeSchedule createSchedule(ScheduleCreateDTO dto) {
        ModeSchedule schedule = new ModeSchedule();
        BeanUtils.copyProperties(dto, schedule);
        if (schedule.getStatus() == null) {
            schedule.setStatus(1);
        }
        save(schedule);
        System.out.println("[AUDIT] 创建切换计划: classId=" + dto.getClassId()
                + ", modeId=" + dto.getModeId()
                + ", operator=" + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());
        return schedule;
    }

    @Override
    public void updateSchedule(Long id, ScheduleCreateDTO dto) {
        ModeSchedule existing = getById(id);
        if (existing == null) {
            throw new com.islandcampus.server.common.exception.BusinessException("切换计划不存在");
        }
        BeanUtils.copyProperties(dto, existing, "id", "createdAt");
        updateById(existing);
        System.out.println("[AUDIT] 更新切换计划: scheduleId=" + id
                + ", operator=" + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());
    }

    @Override
    public Long determineCurrentMode(Long classId) {
        // 根据课程表判断当前应处于什么模式
        // 1. 查找当前时间对应的课程
        // 2. 根据课程类型映射到模式

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue(); // 1=Mon ... 7=Sun
        String currentTime = now.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        // 简化实现: 查找当前时间段内的课程
        var timetableList = listByClassAndTime(classId, dayOfWeek, currentTime);

        if (!timetableList.isEmpty()) {
            // 上课中 -> study模式
            var studyMode = getByCode("study");
            return studyMode != null ? studyMode.getId() : null;
        } else {
            // 非上课时间 -> break模式
            var breakMode = getByCode("break");
            return breakMode != null ? breakMode.getId() : null;
        }
    }

    private List<?> listByClassAndTime(Long classId, int dayOfWeek, String currentTime) {
        // 实际查询timetable表，简化返回空列表
        return java.util.Collections.emptyList();
    }

    @Override
    public com.islandcampus.server.mode.entity.SceneMode getByCode(String code) {
        return null; // 应通过SceneModeService获取，此处为简化
    }

    @Override
    public ModeSchedule create(ScheduleCreateDTO dto) {
        return createSchedule(dto);
    }
}
