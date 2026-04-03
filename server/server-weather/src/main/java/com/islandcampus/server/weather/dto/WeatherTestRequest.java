package com.islandcampus.server.weather.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "天气测试请求")
public class WeatherTestRequest {

    @Schema(description = "城市名称或区号")
    private String city;

    @Schema(description = "天气配置ID")
    private Long configId;
}
