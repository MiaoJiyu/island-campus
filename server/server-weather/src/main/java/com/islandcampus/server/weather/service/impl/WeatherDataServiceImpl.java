package com.islandcampus.server.weather.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.weather.dto.WeatherDataVO;
import com.islandcampus.server.weather.mapper.entity.WeatherConfig;
import com.islandcampus.server.weather.service.WeatherConfigService;
import com.islandcampus.server.weather.service.WeatherDataService;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherDataServiceImpl implements WeatherDataService {

    private final StringRedisTemplate redisTemplate;
    private final WeatherConfigService weatherConfigService;

    private static final String WEATHER_CACHE_PREFIX = "weather:cache:";
    private static final long CACHE_TTL_MINUTES = 30;

    @Override
    public WeatherDataVO fetchWeather(Long configId, String city) {
        WeatherConfig config;
        if (configId != null) {
            config = weatherConfigService.getById(configId);
        } else {
            config = weatherConfigService.getDefaultConfig();
        }
        if (config == null || config.getDeleted() == 1) {
            throw new BusinessException("天气配置不存在或未设置默认配置");
        }

        if (StrUtil.isBlank(city)) {
            city = config.getCityDefault();
        }
        if (StrUtil.isBlank(city)) {
            throw new BusinessException("城市参数不能为空且未配置默认城市");
        }

        try {
            String apiUrl = buildUrlWithParams(config, city);
            HttpRequest request;

            if ("POST".equalsIgnoreCase(config.getApiMethod())) {
                request = HttpRequest.post(apiUrl);
                if (StrUtil.isNotBlank(config.getRequestBodyTemplate())) {
                    String body = replacePlaceholders(config.getRequestBodyTemplate(), city);
                    request.body(body);
                }
            } else {
                request = HttpRequest.get(apiUrl);
            }

            // 设置请求头
            if (StrUtil.isNotBlank(config.getRequestHeaders())) {
                try {
                    Map<String, String> headers = JSON.parseObject(config.getRequestHeaders(), Map.class);
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        request.header(entry.getKey(), entry.getValue().replace("{apiKey}", config.getApiKey()));
                    }
                } catch (Exception e) {
                    log.warn("解析请求头失败: {}", e.getMessage());
                }
            } else {
                // 默认uapis.cn的认证方式
                if (StrUtil.isNotBlank(config.getApiKey())) {
                    request.header("Authorization", "Bearer " + config.getApiKey());
                }
            }

            HttpResponse response = request.timeout(10000).execute();
            String body = response.body();

            if (!response.isOk()) {
                throw new BusinessException("天气API请求失败: HTTP " + response.getStatus());
            }

            return parseWeatherResponse(body, config);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取天气数据异常: city={}", city, e);
            throw new BusinessException("获取天气数据失败: " + e.getMessage());
        }
    }

    @Override
    public WeatherDataVO parseWeatherResponse(String responseBody, WeatherConfig config) {
        DocumentContext ctx = JsonPath.parse(responseBody);

        // 使用自定义JSON路径或默认值
        String tempPath = StrUtil.isNotBlank(config.getTempPath()) ? config.getTempPath() : "$.temperature";
        String weatherPath = StrUtil.isNotBlank(config.getWeatherPath()) ? config.getWeatherPath() : "$.weather";
        String iconPath = StrUtil.isNotBlank(config.getWeatherIconPath()) ? config.getWeatherIconPath() : "$.weather_icon";
        String forecastPath = StrUtil.isNotBlank(config.getForecastPath()) ? config.getForecastPath() : "$.forecast";
        String hourlyPath = StrUtil.isNotBlank(config.getHourlyPath()) ? config.getHourlyPath() : "$.hourly_forecast";
        String aqiPath = StrUtil.isNotBlank(config.getAqiPath()) ? config.getAqiPath() : "$.aqi";

        WeatherDataVO.WeatherDataVOBuilder builder = WeatherDataVO.builder();

        Object tempObj = safeRead(ctx, tempPath);
        builder.temperature(tempObj instanceof Number ? ((Number) tempObj).doubleValue() : null);

        builder.weather(safeString(ctx, weatherPath));
        builder.weatherIcon(safeString(ctx, iconPath));

        // 湿度、风向、风力等可选字段
        try { builder.humidity(safeDouble(ctx, "$.humidity")); } catch (Exception ignored) {}
        try { builder.windDirection(safeString(ctx, "$.wind_direction")); } catch (Exception ignored) {}
        try { builder.windPower(safeString(ctx, "$.wind_power")); } catch (Exception ignored) {}
        try { builder.feelsLike(safeDouble(ctx, "$.feels_like")); } catch (Exception ignored) {}

        // AQI
        Object aqiObj = safeRead(ctx, aqiPath);
        if (aqiObj instanceof Number) {
            builder.aqi(((Number) aqiObj).intValue());
        }
        try { builder.aqiCategory(safeString(ctx, "$.aqi_category")); } catch (Exception ignored) {}

        // 预报列表
        List<WeatherDataVO.ForecastItem> forecastList = new ArrayList<>();
        try {
            JSONArray forecastArray = ctx.read(forecastPath);
            for (int i = 0; i < Math.min(forecastArray.size(), 7); i++) {
                JSONObject item = forecastArray.getJSONObject(i);
                forecastList.add(WeatherDataVO.ForecastItem.builder()
                        .date(item.getString("date"))
                        .dayWeather(item.getString("day_weather") != null ? item.getString("day_weather") : item.getString("text_day"))
                        .nightWeather(item.getString("night_weather") != null ? item.getString("night_weather") : item.getString("text_night"))
                        .maxTemp(item.getDoubleValue("high") != null ? item.getDoubleValue("high") : item.getDoubleValue("max_temp"))
                        .minTemp(item.getDoubleValue("low") != null ? item.getDoubleValue("low") : item.getDoubleValue("min_temp"))
                        .build());
            }
        } catch (Exception e) {
            log.debug("解析预报数据失败: {}", e.getMessage());
        }
        builder.forecastList(forecastList);

        // 逐时预报
        List<WeatherDataVO.HourlyForecastItem> hourlyList = new ArrayList<>();
        try {
            JSONArray hourlyArray = ctx.read(hourlyPath);
            for (int i = 0; i < Math.min(hourlyArray.size(), 24); i++) {
                JSONObject item = hourlyArray.getJSONObject(i);
                hourlyList.add(WeatherDataVO.HourlyForecastItem.builder()
                        .time(item.getString("time") != null ? item.getString("time") : item.getString("hour"))
                        .temperature(item.getDouble("temp") != null ? item.getDouble("temp") : item.getDouble("temperature"))
                        .weather(item.getString("weather") != null ? item.getString("weather") : item.getString("text"))
                        .humidity(item.getDouble("humidity"))
                        .build());
            }
        } catch (Exception e) {
            log.debug("解析逐时预报数据失败: {}", e.getMessage());
        }
        builder.hourlyForecastList(hourlyList);

        return builder.build();
    }

    @Override
    public WeatherDataVO getWeatherCache(String city) {
        String cacheKey = WEATHER_CACHE_PREFIX + city;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return JSON.parseObject(cached, WeatherDataVO.class);
        }

        // 缓存不存在，拉取并缓存
        WeatherConfig defaultConfig = weatherConfigService.getDefaultConfig();
        if (defaultConfig != null) {
            WeatherDataVO data = fetchWeather(defaultConfig.getId(), city);
            if (data != null) {
                redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(data),
                        CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            }
            return data;
        }
        return null;
    }

    @Override
    public WeatherDataVO getWeatherCacheByComputerId(Long computerId) {
        // 根据computerId查找关联的城市，这里简化为使用默认城市的缓存
        WeatherConfig defaultConfig = weatherConfigService.getDefaultConfig();
        if (defaultConfig != null && StrUtil.isNotBlank(defaultConfig.getCityDefault())) {
            return getWeatherCache(defaultConfig.getCityDefault());
        }
        return null;
    }

    @Override
    public void scheduleFetchAll() {
        LambdaQueryWrapper<com.islandcampus.server.weather.mapper.entity.WeatherConfig> wrapper =
                new LambdaQueryWrapper<>();
        wrapper.eq(com.islandcampus.server.weather.mapper.entity.WeatherConfig::getEnabled, 1);
        var configs = weatherConfigService.list(wrapper);

        for (var config : configs) {
            try {
                if (config.getCityDefault() != null) {
                    WeatherDataVO data = fetchWeather(config.getId(), config.getCityDefault());
                    if (data != null) {
                        String cacheKey = WEATHER_CACHE_PREFIX + config.getCityDefault();
                        long ttl = config.getFetchIntervalMinutes() != null ?
                                config.getFetchIntervalMinutes() : 30;
                        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(data),
                                ttl, TimeUnit.MINUTES);
                    }
                }
            } catch (Exception e) {
                log.error("定时拉取天气数据失败: configId={}", config.getId(), e);
            }
        }
    }

    private String buildUrlWithParams(WeatherConfig config, String city) {
        String url = config.getApiUrl();
        String paramName = StrUtil.isNotBlank(config.getCityParamName()) ? config.getCityParamName() : "city";

        if (url.contains("?")) {
            return url + "&" + paramName + "=" + city + "&extended=true&forecast=true&hourly=true&lang=zh";
        } else {
            return url + "?" + paramName + "=" + city + "&extended=true&forecast=true&hourly=true&lang=zh";
        }
    }

    private String replacePlaceholders(String template, String city) {
        return template.replace("{city}", city).replace("{apiKey}", "");
    }

    private Object safeRead(DocumentContext ctx, String path) {
        try {
            return ctx.read(path);
        } catch (Exception e) {
            return null;
        }
    }

    private String safeString(DocumentContext ctx, String path) {
        Object val = safeRead(ctx, path);
        return val != null ? val.toString() : null;
    }

    private Double safeDouble(DocumentContext ctx, String path) {
        Object val = safeRead(ctx, path);
        return val instanceof Number ? ((Number) val).doubleValue() : null;
    }
}
