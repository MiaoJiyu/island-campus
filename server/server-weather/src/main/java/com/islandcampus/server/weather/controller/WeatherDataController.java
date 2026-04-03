package com.islandcampus.server.weather.controller;

import com.islandcampus.server.common.base.R;
import com.islandcampus.server.weather.dto.WeatherDataVO;
import com.islandcampus.server.weather.service.WeatherDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/weather/data")
@RequiredArgsConstructor
@Tag(name = "天气数据", description = "天气数据查询接口")
public class WeatherDataController {

    private final WeatherDataService weatherDataService;
    private final StringRedisTemplate redisTemplate;

    private static final String CACHE_PREFIX = "weather:cache:";

    @GetMapping("/current")
    @Operation(summary = "获取当前天气")
    public R<WeatherDataVO> current(@RequestParam(required = false) String city) {
        if (city != null && !city.isBlank()) {
            return R.ok(weatherDataService.getWeatherCache(city.trim()));
        }
        return R.ok(weatherDataService.getWeatherCacheByComputerId(null));
    }

    @GetMapping("/forecast")
    @Operation(summary = "获取天气预报(含逐时)")
    public R<WeatherDataVO> forecast(@RequestParam(required = false) String city) {
        if (city != null && !city.isBlank()) {
            return R.ok(weatherDataService.getWeatherCache(city.trim()));
        }
        return R.ok(weatherDataService.getWeatherCacheByComputerId(null));
    }

    @GetMapping("/cache-status")
    @Operation(summary = "缓存状态查看")
    public R<Map<String, Object>> cacheStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("prefix", CACHE_PREFIX);
        try {
            var keys = redisTemplate.keys(CACHE_PREFIX + "*");
            status.put("cachedCities", keys != null ? keys.size() : 0);
            status.put("cachedKeys", keys);
        } catch (Exception e) {
            status.put("error", e.getMessage());
        }
        return R.ok(status);
    }
}
