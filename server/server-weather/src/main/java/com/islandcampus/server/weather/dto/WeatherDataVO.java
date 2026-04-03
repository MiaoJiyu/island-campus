package com.islandcampus.server.weather.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "天气数据VO")
public class WeatherDataVO {

    @Schema(description = "温度")
    private Double temperature;

    @Schema(description = "天气描述")
    private String weather;

    @Schema(description = "天气图标")
    private String weatherIcon;

    @Schema(description = "湿度")
    private Double humidity;

    @Schema(description = "风向")
    private String windDirection;

    @Schema(description = "风力")
    private String windPower;

    @Schema(description = "体感温度")
    private Double feelsLike;

    @Schema(description = "空气质量指数")
    private Integer aqi;

    @Schema(description = "空气质量等级")
    private String aqiCategory;

    @Schema(description = "预报列表")
    private List<ForecastItem> forecastList;

    @Schema(description = "逐时预报列表")
    private List<HourlyForecastItem> hourlyForecastList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastItem {
        @Schema(description = "日期")
        private String date;

        @Schema(description = "白天天气")
        private String dayWeather;

        @Schema(description = "夜间天气")
        private String nightWeather;

        @Schema(description = "最高温")
        private Double maxTemp;

        @Schema(description = "最低温")
        private Double minTemp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyForecastItem {
        @Schema(description = "时间")
        private String time;

        @Schema(description = "温度")
        private Double temperature;

        @Schema(description = "天气")
        private String weather;

        @Schema(description = "湿度")
        private Double humidity;
    }
}
