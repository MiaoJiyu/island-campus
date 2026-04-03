package com.islandcampus.server.plugin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.plugin.dto.*;
import com.islandcampus.server.plugin.mapper.PluginMapper;
import com.islandcampus.server.plugin.mapper.entity.Plugin;
import com.islandcampus.server.plugin.sandbox.GroovySandbox;
import com.islandcampus.server.plugin.service.PluginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PluginServiceImpl extends ServiceImpl<PluginMapper, Plugin> implements PluginService {

    private final GroovySandbox groovySandbox;

    @Value("${plugin.storage.path:/opt/island-campus/plugins}")
    private String pluginStoragePath;

    @Override
    public Plugin uploadPlugin(MultipartFile file, Integer type, String code,
                               String entryPoint, String description) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("插件文件不能为空");
        }

        // 编码唯一性检查
        LambdaQueryWrapper<Plugin> codeCheck = new LambdaQueryWrapper<>();
        codeCheck.eq(Plugin::getCode, code).eq(Plugin::getDeleted, 0);
        if (this.count(codeCheck) > 0) {
            throw new BusinessException("该编码的插件已存在: " + code);
        }

        try {
            // 创建存储目录
            Path storageDir = Paths.get(pluginStoragePath);
            Files.createDirectories(storageDir);

            // 存储文件
            String originalName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalName != null && originalName.contains(".")) {
                fileExtension = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedFileName = System.currentTimeMillis() + "_" + code + fileExtension;
            Path targetPath = storageDir.resolve(storedFileName);
            file.transferTo(targetPath.toFile());

            // 构建插件实体
            Plugin plugin = new Plugin();
            plugin.setName(StrUtil.isNotBlank(originalName) ?
                    originalName.replace(fileExtension, "") : code);
            plugin.setCode(code);
            plugin.setType(type);
            plugin.setDescription(description);
            plugin.setEntryPoint(entryPoint);
            plugin.setFilePath(targetPath.toString());
            plugin.setEnabled(0); // 默认停用，需手动启用
            plugin.setInstalledAt(LocalDateTime.now());
            plugin.setVersion("1.0.0");

            this.save(plugin);
            log.info("插件上传成功: code={}, type={}, path={}", code, type, storedFileName);

            return plugin;
        } catch (Exception e) {
            log.error("插件上传失败: code={}", code, e);
            throw new BusinessException("插件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void enablePlugin(Long id) {
        Plugin plugin = this.getById(id);
        if (plugin == null || plugin.getDeleted() == 1) {
            throw new BusinessException("插件不存在");
        }
        plugin.setEnabled(1);
        this.updateById(plugin);
        log.info("插件已启用: code={}, id={}", plugin.getCode(), id);
    }

    @Override
    public void disablePlugin(Long id) {
        Plugin plugin = this.getById(id);
        if (plugin == null || plugin.getDeleted() == 1) {
            throw new BusinessException("插件不存在");
        }
        plugin.setEnabled(0);
        this.updateById(plugin);
        log.info("插件已停用: code={}, id={}", plugin.getCode(), id);
    }

    @Override
    public void loadPlugins() {
        LambdaQueryWrapper<Plugin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Plugin::getEnabled, 1)
                .eq(Plugin::getDeleted, 0);

        List<Plugin> enabledPlugins = this.list(wrapper);
        int count = 0;
        for (Plugin plugin : enabledPlugins) {
            try {
                // 根据类型加载不同类型的插件
                switch (plugin.getType()) {
                    case 1 -> { // 前端插件：无需预加载，按需返回组件配置
                        log.debug("前端插件就绪: {}", plugin.getCode());
                    }
                    case 2 -> { // Groovy插件：验证脚本可加载性
                        if (StrUtil.isNotBlank(plugin.getFilePath())) {
                            Path path = Paths.get(plugin.getFilePath());
                            if (Files.exists(path)) {
                                String content = Files.readString(path);
                                log.debug("Groovy插件已加载: {}, size={}bytes",
                                        plugin.getCode(), content.length());
                            }
                        }
                    }
                    case 3 -> { // JAR插件：记录已加载
                        log.debug("JAR插件已注册: {}", plugin.getCode());
                    }
                    default -> log.warn("未知插件类型: type={}", plugin.getType());
                }
                count++;
            } catch (Exception e) {
                log.error("插件加载失败: code={}", plugin.getCode(), e);
            }
        }
        log.info("插件加载完成: 共{}个启用插件，成功加载{}个", enabledPlugins.size(), count);
    }

    @Override
    public FrontendPluginVO executeFrontendPlugin(String code) {
        LambdaQueryWrapper<Plugin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Plugin::getCode, code)
                .eq(Plugin::getType, 1)
                .eq(Plugin::getEnabled, 1);

        Plugin plugin = this.getOne(wrapper, false);
        if (plugin == null || plugin.getDeleted() == 1) {
            throw new BusinessException("前端插件不存在或未启用: " + code);
        }

        FrontendPluginVO vo = new FrontendPluginVO();
        vo.setCode(plugin.getCode());
        vo.setName(plugin.getName());
        vo.setComponentPath(plugin.getEntryPoint()); // 入口点即组件路径
        vo.setVersion(plugin.getVersion());
        vo.setDescription(plugin.getDescription());

        if (StrUtil.isNotBlank(plugin.getConfigSchema())) {
            vo.setConfigSchema(JSON.parse(plugin.getConfigSchema()));
        }

        return vo;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object executeBackendPlugin(String pluginCode, Map<String, Object> params) {
        LambdaQueryWrapper<Plugin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Plugin::getCode, pluginCode)
                .eq(Plugin::getType, 2) // 仅允许Groovy后端插件
                .eq(Plugin::getEnabled, 1);

        Plugin plugin = this.getOne(wrapper, false);
        if (plugin == null || plugin.getDeleted() == 1) {
            throw new BusinessException("后端插件不存在或未启用: " + pluginCode);
        }

        try {
            // 读取脚本内容
            Path scriptPath = Paths.get(plugin.getFilePath());
            if (!Files.exists(scriptPath)) {
                throw new BusinessException("插件脚本文件丢失");
            }

            String scriptContent = Files.readString(scriptPath);

            // 构建执行上下文
            Map<String, Object> context = new HashMap<>();
            context.put("params", params != null ? params : Map.of());
            context.put("pluginId", plugin.getId());
            context.put("pluginCode", plugin.getCode());

            // 注入插件配置值
            if (StrUtil.isNotBlank(plugin.getConfigValues())) {
                context.put("config", JSON.parse(plugin.getConfigValues()));
            }

            // 通过沙箱执行
            long startTime = System.currentTimeMillis();
            Map<String, Object> result = groovySandbox.execute(scriptContent, context);
            long elapsed = System.currentTimeMillis() - startTime;

            log.info("Groovy插件执行完成: code={},耗时={}ms", pluginCode, elapsed);
            return result;
        } catch (SecurityException e) {
            log.error("Groovy插件安全违规: code={}", pluginCode, e);
            throw new BusinessException("插件执行被安全策略阻止: " + e.getMessage());
        } catch (Exception e) {
            log.error("Groovy插件执行失败: code={}", pluginCode, e);
            throw new BusinessException("插件执行失败: " + e.getMessage());
        }
    }

    @Override
    public Object getPluginConfigSchema(Long id) {
        Plugin plugin = this.getById(id);
        if (plugin == null || plugin.getDeleted() == 1) {
            throw new BusinessException("插件不存在");
        }
        if (StrUtil.isBlank(plugin.getConfigSchema())) {
            return Map.of("type", "object", "properties", new HashMap<>());
        }
        return JSON.parse(plugin.getConfigSchema());
    }

    @Override
    public void updatePluginConfig(Long id, String valuesJson) {
        Plugin plugin = this.getById(id);
        if (plugin == null || plugin.getDeleted() == 1) {
            throw new BusinessException("插件不存在");
        }
        plugin.setConfigValues(valuesJson);
        this.updateById(plugin);
        log.info("插件配置已更新: id={}, code={}", id, plugin.getCode());
    }

    @Override
    public void deletePlugin(Long id) {
        Plugin plugin = this.getById(id);
        if (plugin == null || plugin.getDeleted() == 1) {
            throw new BusinessException("插件不存在");
        }

        // 删除物理文件
        if (StrUtil.isNotBlank(plugin.getFilePath())) {
            try {
                Path filePath = Paths.get(plugin.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("插件文件已删除: path={}", plugin.getFilePath());
                }
            } catch (Exception e) {
                log.warn("删除插件文件失败（将继续删除记录）: path={}",
                        plugin.getFilePath(), e.getMessage());
            }
        }

        this.removeById(id);
        log.info("插件已删除: id={}, code={}", id, plugin.getCode());
    }

    @Override
    public PageResult<Plugin> listPlugins(String name, Integer type, Integer enabled,
                                          Long current, Long size) {
        LambdaQueryWrapper<Plugin> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Plugin::getInstalledAt);

        if (StrUtil.isNotBlank(name)) {
            wrapper.like(Plugin::getName, name);
        }
        if (type != null) {
            wrapper.eq(Plugin::getType, type);
        }
        if (enabled != null) {
            wrapper.eq(Plugin::getEnabled, enabled);
        }

        Page<Plugin> page = new Page<>(current != null ? current : 1,
                size != null ? size : 20);
        Page<Plugin> result = this.page(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(),
                result.getCurrent(), result.getSize());
    }
}
