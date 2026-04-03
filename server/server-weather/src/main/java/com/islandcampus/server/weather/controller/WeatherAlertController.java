package com.islandcampus.server.weather.controller;

import com.alibaba.fastjson2.JSONObject;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.base.R;
import com.islandcampus.server.weather.dto.AlertRuleCreateDTO;
import com.islandcampus.server.weather.mapper.entity.WeatherAlertRule;
import com.islandcampus.server.weather.service.WeatherAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/weather/alert-rules")
@RequiredArgsConstructor
@Tag(name = "天气预警规则", description = "天气预警规则CRUD接口")
public class WeatherAlertController {

    private final WeatherAlertService weatherAlertService;

    @PostMapping
    @Operation(summary = "创建预警规则")
    public R<WeatherAlertRule> create(@Valid @RequestBody AlertRuleCreateDTO dto) {
        weatherAlertService.createRule(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改预警规则")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody AlertRuleCreateDTO dto) {
        weatherAlertService.updateRule(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除预警规则")
    public R<Void> delete(@PathVariable Long id) {
        weatherAlertService.deleteRule(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取规则详情")
    public R<WeatherAlertRule> detail(@PathVariable Long id) {
        return R.ok(weatherAlertService.getById(id));
    }

    @GetMapping
    @Operation(summary = "预警规则列表")
    public R<PageResult<WeatherAlertRule>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        var result = weatherAlertService.page(page);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "测试规则匹配")
    public R<Boolean> testRule(@PathVariable Long id, @RequestBody JSONObject weatherData) {
        boolean matched = weatherAlertService.testRule(id, weatherData);
        return R.ok(matched ? "条件满足，将触发提醒" : "条件不满足", matched);
    }
}
