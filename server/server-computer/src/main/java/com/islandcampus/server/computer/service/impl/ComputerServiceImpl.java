package com.islandcampus.server.computer.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.common.redis.PublisherService;
import com.islandcampus.server.common.websocket.WebSocketService;
import com.islandcampus.server.computer.dto.*;
import com.islandcampus.server.computer.mapper.ComputerMapper;
import com.islandcampus.server.computer.mapper.entity.Computer;
import com.islandcampus.server.computer.service.ComputerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComputerServiceImpl extends ServiceImpl<ComputerMapper, Computer> implements ComputerService {

    private final WebSocketService webSocketService;
    private final PublisherService publisherService;

    private static final int OFFLINE_THRESHOLD_MINUTES = 5;

    @Override
    public Computer register(ComputerRegisterDTO dto) {
        // MAC地址唯一性检查
        LambdaQueryWrapper<Computer> macCheck = new LambdaQueryWrapper<>();
        macCheck.eq(Computer::getMacAddress, dto.getMacAddress());
        if (this.count(macCheck) > 0) {
            throw new BusinessException("该MAC地址已注册: " + dto.getMacAddress());
        }

        Computer computer = new Computer();
        computer.setName(dto.getName());
        computer.setMacAddress(dto.getMacAddress());
        computer.setIpAddress(StrUtil.isNotBlank(dto.getIpAddress()) ? dto.getIpAddress() : "0.0.0.0");
        computer.setClassId(dto.getClassId());
        computer.setClientVersion(dto.getClientVersion());
        computer.setOsInfo(dto.getOsInfo());
        computer.setResolution(dto.getResolution());
        computer.setIsOnline(1); // 注册时默认在线
        computer.setLastHeartbeat(LocalDateTime.now());
        computer.setHealthStatus(2); // 默认绿色（正常）
        computer.setStatus(1); // 默认正常状态

        this.save(computer);
        log.info("设备注册成功: name={}, mac={}", dto.getName(), dto.getMacAddress());

        return computer;
    }

    @Override
    public void heartbeat(Long computerId, ComputerHeartbeatDTO dto) {
        Computer computer = this.getById(computerId);
        if (computer == null || computer.getDeleted() == 1) {
            log.warn("心跳上报失败，设备不存在或已删除: computerId={}", computerId);
            return;
        }

        computer.setIsOnline(1);
        computer.setLastHeartbeat(LocalDateTime.now());
        if (StrUtil.isNotBlank(dto.getIpAddress())) {
            computer.setIpAddress(dto.getIpAddress());
        }
        if (StrUtil.isNotBlank(dto.getClientVersion())) {
            computer.setClientVersion(dto.getClientVersion());
        }
        if (StrUtil.isNotBlank(dto.getResolution())) {
            computer.setResolution(dto.getResolution());
        }
        if (StrUtil.isNotBlank(dto.getOsInfo())) {
            computer.setOsInfo(dto.getOsInfo());
        }

        this.updateById(computer);
    }

    @Override
    public PageResult<ComputerVO> listComputers(Long orgId, Long classId, Integer isOnline,
                                                 Long current, Long size) {
        LambdaQueryWrapper<Computer> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Computer::getLastHeartbeat);

        if (classId != null) {
            wrapper.eq(Computer::getClassId, classId);
        }
        if (isOnline != null) {
            wrapper.eq(Computer::getIsOnline, isOnline);
        }

        Page<Computer> page = new Page<>(current != null ? current : 1,
                size != null ? size : 20);
        Page<Computer> result = this.page(page, wrapper);

        List<ComputerVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public Computer getComputerDetail(Long id) {
        Computer computer = this.getById(id);
        if (computer == null || computer.getDeleted() == 1) {
            throw new BusinessException("设备不存在");
        }
        return computer;
    }

    @Override
    public void updateComputer(Long id, ComputerUpdateDTO dto) {
        Computer computer = this.getById(id);
        if (computer == null || computer.getDeleted() == 1) {
            throw new BusinessException("设备不存在");
        }

        if (dto.getName() != null) {
            computer.setName(dto.getName());
        }
        if (dto.getClassId() != null) {
            computer.setClassId(dto.getClassId());
        }
        if (dto.getResolution() != null) {
            computer.setResolution(dto.getResolution());
        }
        if (dto.getRemark() != null) {
            computer.setRemark(dto.getRemark());
        }

        this.updateById(computer);
    }

    @Override
    public void deleteComputer(Long id) {
        Computer computer = this.getById(id);
        if (computer == null || computer.getDeleted() == 1) {
            throw new BusinessException("设备不存在");
        }
        this.removeById(id);
        log.info("设备已删除: id={}, name={}", id, computer.getName());
    }

    @Override
    public void updateIslandConfig(Long id, IslandConfigOverrideDTO dto) {
        Computer computer = this.getById(id);
        if (computer == null || computer.getDeleted() == 1) {
            throw new BusinessException("设备不存在");
        }

        computer.setIslandConfigJson(dto.getConfigJson());
        this.updateById(computer);

        // 通过PublisherService和WebSocket通知客户端配置更新
        publisherService.notifyIslandConfigUpdate(id);
        webSocketService.notifyIslandConfigUpdate(id);

        log.info("灵动岛配置已更新: computerId={}", id);
    }

    @Override
    public long getOnlineCount(Long orgId) {
        LambdaQueryWrapper<Computer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Computer::getIsOnline, 1)
                .eq(Computer::getStatus, 1);
        return this.count(wrapper);
    }

    @Override
    public void offlineCheck() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(OFFLINE_THRESHOLD_MINUTES);

        LambdaQueryWrapper<Computer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Computer::getIsOnline, 1)
                .lt(Computer::getLastHeartbeat, threshold);

        List<Computer> offlineComputers = this.list(wrapper);
        for (Computer computer : offlineComputers) {
            computer.setIsOnline(0);
            computer.setHealthStatus(0); // 离线标记为红色异常
            this.updateById(computer);
        }

        if (!offlineComputers.isEmpty()) {
            log.info("离线检测完成，标记{}台设备为离线", offlineComputers.size());
        }
    }

    private ComputerVO toVO(Computer computer) {
        ComputerVO vo = new ComputerVO();
        vo.setId(computer.getId());
        vo.setName(computer.getName());
        vo.setMacAddress(computer.getMacAddress());
        vo.setIpAddress(computer.getIpAddress());
        vo.setClassName(getClassDisplayName(computer.getClassId()));
        vo.setIsOnline(computer.getIsOnline() == 1);
        vo.setHealthStatus(computer.getHealthStatus());
        vo.setClientVersion(computer.getClientVersion());
        vo.setLastHeartbeat(computer.getLastHeartbeat());
        vo.setOnlineDuration(calculateOnlineDuration(computer));
        vo.setResolution(computer.getResolution());
        vo.setOsInfo(computer.getOsInfo());
        vo.setRemark(computer.getRemark());
        return vo;
    }

    private String getClassDisplayName(Long classId) {
        if (classId == null) {
            return "未分配";
        }
        // TODO: 可通过班级服务查询实际名称，此处返回占位
        return "班级" + classId;
    }

    private Long calculateOnlineDuration(Computer computer) {
        if (computer.getLastHeartbeat() == null || computer.getIsOnline() != 1) {
            return 0L;
        }
        return Duration.between(computer.getLastHeartbeat(), LocalDateTime.now()).toMinutes();
    }
}
