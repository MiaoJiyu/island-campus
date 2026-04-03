package com.islandcampus.server.island.controller;

import com.islandcampus.server.common.base.R;
import com.islandcampus.server.island.dto.IslandConfigDTO;
import com.islandcampus.server.island.dto.IslandConfigScopeDTO;
import com.islandcampus.server.island.entity.IslandConfig;
import com.islandcampus.server.island.service.IslandConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/island")
@RequiredArgsConstructor
@Tag(name = "灵动岛配置", description = "灵动岛配置管理")
public class IslandConfigController {

    private final IslandConfigService islandConfigService;

    @GetMapping("/config")
    @Operation(summary = "获取配置列表")
    public R<List<IslandConfig>> list() {
        return R.ok(islandConfigService.listConfigs());
    }

    @GetMapping("/config/effective")
    @Operation(summary = "获取电脑有效配置")
    public R<Map<String, Object>> getEffective(@RequestParam(required = false) Long computerId) {
        return R.ok(islandConfigService.getEffectiveConfig(computerId));
    }

    @PutMapping("/config/global")
    @Operation(summary = "更新全局默认配置")
    public R<Void> updateGlobal(@RequestBody Map<String, Object> configJson) {
        String json = com.alibaba.fastjson2.JSON.toJSONString(configJson);
        islandConfigService.updateGlobalConfig(json);
        return R.ok();
    }

    @PostMapping("/config/scope")
    @Operation(summary = "创建覆盖配置")
    public R<IslandConfig> createScope(@Valid @RequestBody IslandConfigScopeDTO dto) {
        islandConfigService.updateScopeConfig(dto);
        // 返回刚创建的配置
        return R.ok();
    }

    @PutMapping("/config/scope/{id}")
    @Operation(summary = "修改覆盖配置")
    public R<Void> updateScope(@PathVariable Long id,
                               @RequestBody IslandConfigScopeDTO dto) {
        // 更新已存在的配置
        com.islandcampus.server.island.entity.IslandConfig existing = islandConfigService.getById(id);
        if (existing != null) {
            existing.setConfigName(dto.getConfigName());
            existing.setScopeType(dto.getScopeType());
            existing.setScopeId(dto.getScopeId());
            existing.setConfigJson(dto.getConfigJson());
            islandConfigService.updateById(existing);

            System.out.println("[AUDIT] 修改灵动岛覆盖配置: configId=" + id
                    + ", operator=" + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());
        }
        return R.ok();
    }

    @DeleteMapping("/config/scope/{id}")
    @Operation(summary = "删除覆盖配置")
    public R<Void> deleteScope(@PathVariable Long id) {
        islandConfigService.deleteScopeConfig(id);
        return R.ok();
    }

    @GetMapping("/config/preview")
    @Operation(summary = "预览配置效果")
    public R<IslandConfigDTO> preview(@RequestParam(required = false) Long computerId) {
        return R.ok(islandConfigService.preview(computerId));
    }
}
