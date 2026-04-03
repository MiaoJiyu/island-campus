package com.islandcampus.server.weather.mapper.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("weather_config")
public class WeatherConfig extends BaseEntity {

    private String name;

    private String apiUrl;

    /**
     * API请求方式: GET/POST
     */
    private String apiMethod;

    private String apiKey;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String requestHeaders;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String requestBodyTemplate;

    private String cityParamName;

    private String responseJsonPath;

    private String tempPath;

    private String weatherPath;

    private String weatherIconPath;

    private String forecastPath;

    private String hourlyPath;

    private String aqiPath;

    /**
     * 是否默认配置
     */
    private Integer isDefault;

    private String cityDefault;

    /**
     * 拉取间隔(分钟)
     */
    private Integer fetchIntervalMinutes;

    private Integer enabled;
}
