package com.islandcampus.server.weather.service;

import com.islandcampus.server.weather.dto.WeatherDataVO;
import com.islandcampus.server.weather.dto.WeatherTestRequest;

public interface WeatherDataService {
    WeatherDataVO fetchWeather(Long configId, String city);
    WeatherDataVO parseWeatherResponse(String responseBody, WeatherConfig config);
    WeatherDataVO getWeatherCache(String city);
    WeatherDataVO getWeatherCacheByComputerId(Long computerId);
    void scheduleFetchAll();
}
