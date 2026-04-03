package com.islandcampus.server.mode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.mode.dto.ScheduleCreateDTO;
import com.islandcampus.server.mode.entity.ModeSchedule;

public interface ModeScheduleService extends IService<ModeSchedule> {

    ModeSchedule createSchedule(ScheduleCreateDTO dto);

    void updateSchedule(Long id, ScheduleCreateDTO dto);

    /**
     * 根据时间自动判断当前模式
     */
    Long determineCurrentMode(Long classId);

    ModeSchedule create(ScheduleCreateDTO dto);
}
