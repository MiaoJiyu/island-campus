package com.islandcampus.server.island.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.island.dto.IslandConfigDTO;
import com.islandcampus.server.island.dto.IslandConfigScopeDTO;
import com.islandcampus.server.island.entity.IslandConfig;

import java.util.List;
import java.util.Map;

public interface IslandConfigService extends IService<IslandConfig> {

    /**
     * 获取电脑有效配置（班级覆盖 > 年级 > 全局默认）
     */
    Map<String, Object> getEffectiveConfig(Long computerId);

    /**
     * 更新全局默认配置
     */
    void updateGlobalConfig(String configJson);

    /**
     * 更新/创建覆盖配置
     */
    void updateScopeConfig(IslandConfigScopeDTO dto);

    /**
     * 删除覆盖配置
     */
    void deleteScopeConfig(Long id);

    /**
     * 获取所有配置列表
     */
    List<IslandConfig> listConfigs();

    /**
     * 预览配置效果
     */
    IslandConfigDTO preview(Long computerId);
}
