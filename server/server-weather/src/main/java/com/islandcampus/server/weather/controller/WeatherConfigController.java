package com.islandcampus.server.weather.controller;

import com.islandcampus.server.common.base.R;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.weather.dto.WeatherConfigCreateDTO;
import com.islandcampus.server.weather.dto.WeatherTestRequest;
import com.islandcampus.server.weather.mapper.entity.WeatherConfig;
import com.islandcampus.server.weather.service.WeatherConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/weather/configs")
@RequiredArgsConstructor
@Tag(name = "天气配置管理", description = "天气API配置CRUD接口")
public class WeatherConfigController {

    private final WeatherConfigService weatherConfigService;

    @PostMapping
    @Operation(summary = "创建天气配置")
    public R<WeatherConfig> create(@Valid @RequestBody WeatherConfigCreateDTO dto) {
        weatherConfigService.createConfig(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改天气配置")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody WeatherConfigCreateDTO dto) {
        weatherConfigService.updateConfig(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除天气配置")
    public R<Void> delete(@PathVariable Long id) {
        weatherConfigService.deleteConfig(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取天气配置详情")
    public R<WeatherConfig> detail(@PathVariable Long id) {
        return R.ok(weatherConfigService.getConfigById(id));
    }

    @GetMapping
    @Operation(summary = "天气配置列表")
    public R<PageResult<WeatherConfig>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        var result = weatherConfigService.page(page);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @PostMapping("/test")
    @Operation(summary = "测试API连接")
    public R<Boolean> testConnection(@Valid @RequestBody WeatherTestRequest request) throws Exception {
        boolean success = weatherConfigService.testConnection(request.getConfigId(), request.getCity());
        return R.ok(success ? "连接成功" : "连接失败", success);
    }

    @PostMapping("/{id}/set-default")
    @Operation(summary = "设为默认配置")
    public R<Void> setDefault(@PathVariable Long id) {
        weatherConfigService.setDefaultConfig(id);
        return R.ok();
    }
}
