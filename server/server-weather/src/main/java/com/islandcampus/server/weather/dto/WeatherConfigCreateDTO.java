package com.islandcampus.server.weather.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "天气配置创建/更新请求体")
public class WeatherConfigCreateDTO {

    @NotBlank(message = "配置名称不能为空")
    @Schema(description = "配置名称")
    private String name;

    @NotBlank(message = "API地址不能为空")
    @Schema(description = "API地址")
    private String apiUrl;

    @Schema(description = "API请求方式: GET/POST")
    private String apiMethod = "GET";

    @Schema(description = "API密钥")
    private String apiKey;

    @Schema(description = "请求头(JSON)")
    private String requestHeaders;

    @Schema(description = "请求体模板")
    private String requestBodyTemplate;

    @Schema(description = "城市参数名")
    private String cityParamName = "city";

    @Schema(description = "响应JSON路径根")
    private String responseJsonPath;

    @Schema(description = "温度JSON路径")
    private String tempPath = "$.temperature";

    @Schema(description = "天气描述JSON路径")
    private String weatherPath = "$.weather";

    @Schema(description = "天气图标JSON路径")
    private String weatherIconPath = "$.weather_icon";

    @Schema(description = "预报数据JSON路径")
    private String forecastPath = "$.forecast";

    @Schema(description = "逐时预报JSON路径")
    private String hourlyPath = "$.hourly_forecast";

    @Schema(description = "空气质量JSON路径")
    private String aqiPath = "$.aqi";

    @Schema(description = "是否默认配置")
    private Integer isDefault = 0;

    @Schema(description = "默认城市")
    private String cityDefault;

    @Schema(description = "拉取间隔(分钟)")
    private Integer fetchIntervalMinutes = 30;

    @Schema(description = "是否启用")
    private Integer enabled = 1;
}
