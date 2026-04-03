package com.islandcampus.server.mode.service;

import com.islandcampus.server.mode.dto.ModeStatusResponse;
import com.islandcampus.server.mode.dto.ModeSwitchRequest;

import java.util.List;

public interface SceneModeControlService {

    /**
     * 手动切换指定电脑到目标模式
     */
    void switchMode(ModeSwitchRequest request);

    /**
     * 按课程表/Cron自动切换(定时任务调用)
     */
    void autoSwitchBySchedule();

    /**
     * 获取当前模式状态
     */
    ModeStatusResponse getCurrentStatus(Long computerId);

    /**
     * 切换班级所有在线电脑到目标模式
     */
    void switchClassMode(Long classId, Long modeId);
}
