package com.islandcampus.server.computer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.computer.dto.*;
import com.islandcampus.server.computer.mapper.entity.Computer;

public interface ComputerService extends IService<Computer> {

    /**
     * 设备注册
     */
    Computer register(ComputerRegisterDTO dto);

    /**
     * 心跳上报
     */
    void heartbeat(Long computerId, ComputerHeartbeatDTO dto);

    /**
     * 分页列表
     */
    PageResult<ComputerVO> listComputers(Long orgId, Long classId, Integer isOnline,
                                          Long current, Long size);

    /**
     * 设备详情
     */
    Computer getComputerDetail(Long id);

    /**
     * 修改设备信息
     */
    void updateComputer(Long id, ComputerUpdateDTO dto);

    /**
     * 删除设备
     */
    void deleteComputer(Long id);

    /**
     * 更新灵动岛个性化配置
     */
    void updateIslandConfig(Long id, IslandConfigOverrideDTO dto);

    /**
     * 在线数量统计
     */
    long getOnlineCount(Long orgId);

    /**
     * 定时任务：离线检查（超过5分钟未心跳标记为离线）
     */
    void offlineCheck();
}
