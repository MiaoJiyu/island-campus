package com.islandcampus.server.island.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alibaba.fastjson2.JSON;
import com.islandcampus.server.common.redis.PublisherService;
import com.islandcampus.server.common.websocket.WebSocketService;
import com.islandcampus.server.island.dto.IslandConfigDTO;
import com.islandcampus.server.island.dto.IslandConfigScopeDTO;
import com.islandcampus.server.island.entity.Computer;
import com.islandcampus.server.island.entity.IslandConfig;
import com.islandcampus.server.island.mapper.ComputerMapper;
import com.islandcampus.server.island.mapper.IslandConfigMapper;
import com.islandcampus.server.island.service.IslandConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IslandConfigServiceImpl
        extends ServiceImpl<IslandConfigMapper, IslandConfig>
        implements IslandConfigService {

    private final ComputerMapper computerMapper;
    private final PublisherService publisherService;
    private final WebSocketService webSocketService;

    @Override
    public Map<String, Object> getEffectiveConfig(Long computerId) {
        if (computerId == null) {
            return getGlobalConfig();
        }

        Computer computer = computerMapper.selectById(computerId);
        if (computer == null || computer.getClassId() == null) {
            return getGlobalConfig();
        }

        Long classId = computer.getClassId();

        // 优先级: 班级 > 年级 > 全局

        // 1. 尝试获取班级级别配置
        IslandConfig classConfig = getOne(new LambdaQueryWrapper<IslandConfig>()
                .eq(IslandConfig::getScopeType, 3)
                .eq(IslandConfig::getScopeId, classId)
                .eq(IslandConfig::getIsActive, 1)
                .last("LIMIT 1"));

        if (classConfig != null && classConfig.getConfigJson() != null) {
            return parseConfig(classConfig.getConfigJson(), "class", classConfig.getId());
        }

        // 2. 尝试获取年级级别配置(需要通过组织表查找年级ID，此处简化处理)
        // 实际项目中可通过 org_id 查找父级年级
        IslandConfig gradeConfig = findGradeConfig(classId);
        if (gradeConfig != null) {
            return parseConfig(gradeConfig.getConfigJson(), "grade", gradeConfig.getId());
        }

        // 3. 使用全局默认
        return getGlobalConfig();
    }

    private IslandConfig findGradeConfig(Long classId) {
        // 简化实现：实际应通过 sys_organization 查找班级所属年级
        return null; // 可扩展
    }

    private Map<String, Object> getGlobalConfig() {
        IslandConfig globalConfig = getOne(new LambdaQueryWrapper<IslandConfig>()
                .eq(IslandConfig::getScopeType, 1)
                .eq(IslandConfig::getIsActive, 1)
                .last("LIMIT 1"));
        if (globalConfig == null || globalConfig.getConfigJson() == null) {
            return getDefaultEmptyConfig();
        }
        return parseConfig(globalConfig.getConfigJson(), "global", globalConfig.getId());
    }

    @Override
    @Transactional
    public void updateGlobalConfig(String configJson) {
        LambdaQueryWrapper<IslandConfig> wrapper = new LambdaQueryWrapper<IslandConfig>()
                .eq(IslandConfig::getScopeType, 1);
        IslandConfig existing = getOne(wrapper);

        String operator = com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername();
        System.out.println("[AUDIT] 更新全局灵动岛配置: operator=" + operator);

        if (existing != null) {
            existing.setConfigJson(configJson);
            updateById(existing);
        } else {
            existing = new IslandConfig();
            existing.setConfigName("全局默认");
            existing.setScopeType(1);
            existing.setConfigJson(configJson);
            existing.setIsActive(1);
            save(existing);
        }

        publisherService.broadcast("ISLAND_CONFIG_GLOBAL_UPDATE");
        webSocketService.broadcast("/island/config/update",
                Map.of("type", "global_update", "scopeType", 1));
    }

    @Override
    @Transactional
    public void updateScopeConfig(IslandConfigScopeDTO dto) {
        IslandConfig entity = new IslandConfig();
        entity.setConfigName(dto.getConfigName());
        entity.setScopeType(dto.getScopeType());
        entity.setScopeId(dto.getScopeId());
        entity.setConfigJson(dto.getConfigJson());
        entity.setIsActive(1);

        save(entity);

        System.out.println("[AUDIT] 创建/更新灵动岛覆盖配置: name=" + dto.getConfigName()
                + ", scopeType=" + dto.getScopeType()
                + ", operator=" + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());

        publisherService.notifyIslandConfigUpdate(null);
        webSocketService.notifyIslandConfigUpdate(null);
    }

    @Override
    @Transactional
    public void deleteScopeConfig(Long id) {
        removeById(id);
        System.out.println("[AUDIT] 删除灵动岛覆盖配置: configId=" + id
                + ", operator=" + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());

        publisherService.notifyIslandConfigUpdate(null);
        webSocketService.notifyIslandConfigUpdate(null);
    }

    @Override
    public List<IslandConfig> listConfigs() {
        return list(new LambdaQueryWrapper<IslandConfig>()
                .orderByAsc(IslandConfig::getScopeType)
                .orderByDesc(IslandConfig::getCreatedAt));
    }

    @Override
    public IslandConfigDTO preview(Long computerId) {
        Map<String, Object> effective = getEffectiveConfig(computerId);
        return JSON.parseObject(JSON.toJSONString(effective), IslandConfigDTO.class);
    }

    private Map<String, Object> parseConfig(String json, String scopeSource, Long configId) {
        Map<String, Object> map = JSON.parseObject(json,
                new com.alibaba.fastjson2.TypeReference<LinkedHashMap<String, Object>>() {});
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        map.put("_scopeSource", scopeSource);
        map.put("_configId", configId);
        return map;
    }

    private Map<String, Object> getDefaultEmptyConfig() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("position", "top-center");
        defaults.put("height", 40);
        defaults.put("backgroundColor", "rgba(0,0,0,0.75)");
        defaults.put("textColor", "#ffffff");
        defaults.put("borderRadius", 12);
        defaults.put("showLogo", true);
        defaults.put("showDateTime", true);
        defaults.put("dateTimeFormat", "MM-dd HH:mm");
        defaults.put("showCurrentCourse", true);
        defaults.put("showWeather", true);
        defaults.put("showMarquee", true);
        defaults.put("showModeIcon", true);
        defaults.put("showHealthDot", true);
        defaults.put("showMessageBadge", true);
        defaults.put("autoHideFullscreen", true);
        defaults.put("_scopeSource", "default");
        defaults.put("_configId", null);
        return defaults;
    }
}
