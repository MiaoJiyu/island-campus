package com.islandcampus.server.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.plugin.dto.*;
import com.islandcampus.server.plugin.mapper.entity.Plugin;
import org.springframework.web.multipart.MultipartFile;

public interface PluginService extends IService<Plugin> {

    /**
     * 上传插件文件
     */
    Plugin uploadPlugin(MultipartFile file, Integer type, String code,
                        String entryPoint, String description);

    /**
     * 启用插件
     */
    void enablePlugin(Long id);

    /**
     * 停用插件
     */
    void disablePlugin(Long id);

    /**
     * 启动时加载所有启用的插件
     */
    void loadPlugins();

    /**
     * 获取前端插件Vue组件配置
     */
    FrontendPluginVO executeFrontendPlugin(String code);

    /**
     * 执行后端Groovy脚本（沙箱环境）
     */
    Object executeBackendPlugin(String pluginCode, java.util.Map<String, Object> params);

    /**
     * 获取插件配置项Schema
     */
    Object getPluginConfigSchema(Long id);

    /**
     * 更新插件配置值
     */
    void updatePluginConfig(Long id, String valuesJson);

    /**
     * 删除插件（含文件）
     */
    void deletePlugin(Long id);

    /**
     * 分页列表
     */
    PageResult<Plugin> listPlugins(String name, Integer type, Integer enabled,
                                    Long current, Long size);
}
