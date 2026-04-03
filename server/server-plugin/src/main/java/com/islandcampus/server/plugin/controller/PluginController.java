package com.islandcampus.server.plugin.controller;

import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.base.R;
import com.islandcampus.server.plugin.dto.*;
import com.islandcampus.server.plugin.mapper.entity.Plugin;
import com.islandcampus.server.plugin.service.PluginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/plugin")
@RequiredArgsConstructor
@Tag(name = "插件生态", description = "插件上传/管理/执行接口")
public class PluginController {

    private final PluginService pluginService;

    @PostMapping
    @Operation(summary = "创建插件记录")
    public R<Plugin> create(@RequestBody Plugin plugin) {
        pluginService.save(plugin);
        return R.ok(plugin);
    }

    @GetMapping
    @Operation(summary = "插件列表")
    public R<PageResult<Plugin>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer enabled,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return R.ok(pluginService.listPlugins(name, type, enabled, current, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "插件详情")
    public R<Plugin> detail(@PathVariable Long id) {
        Plugin plugin = pluginService.getById(id);
        if (plugin == null || plugin.getDeleted() == 1) {
            return R.fail("插件不存在");
        }
        return R.ok(plugin);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除插件")
    public R<Void> delete(@PathVariable Long id) {
        pluginService.deletePlugin(id);
        return R.ok();
    }

    @PostMapping("/upload")
    @Operation(summary = "上传插件文件")
    public R<Plugin> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("type") Integer type,
            @RequestParam("code") String code,
            @RequestParam(value = "entryPoint", required = false) String entryPoint,
            @RequestParam(value = "description", required = false) String description) {
        return R.ok(pluginService.uploadPlugin(file, type, code, entryPoint, description));
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "启用插件")
    public R<Void> enable(@PathVariable Long id) {
        pluginService.enablePlugin(id);
        return R.ok();
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "停用插件")
    public R<Void> disable(@PathVariable Long id) {
        pluginService.disablePlugin(id);
        return R.ok();
    }

    @GetMapping("/{id}/config-schema")
    @Operation(summary = "获取插件配置Schema")
    public R<Object> configSchema(@PathVariable Long id) {
        return R.ok(pluginService.getPluginConfigSchema(id));
    }

    @PutMapping("/{id}/config")
    @Operation(summary = "更新插件配置值")
    public R<Void> updateConfig(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String valuesJson = com.alibaba.fastjson2.JSON.toJSONString(body);
        pluginService.updatePluginConfig(id, valuesJson);
        return R.ok();
    }

    @GetMapping("/frontend/{code}")
    @Operation(summary = "获取前端插件组件配置")
    public R<FrontendPluginVO> frontend(@PathVariable String code) {
        return R.ok(pluginService.executeFrontendPlugin(code));
    }

    @PostMapping("/execute")
    @Operation(summary = "执行后端Groovy插件")
    public R<Object> execute(@RequestBody PluginExecuteDTO dto) {
        return R.ok(pluginService.executeBackendPlugin(dto.getPluginCode(), dto.getParams()));
    }
}
