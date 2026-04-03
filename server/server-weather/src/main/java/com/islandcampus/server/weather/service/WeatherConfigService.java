package com.islandcampus.server.weather.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.weather.dto.WeatherConfigCreateDTO;
import com.islandcampus.server.weather.mapper.entity.WeatherConfig;

public interface WeatherConfigService extends IService<WeatherConfig> {
    void createConfig(WeatherConfigCreateDTO dto);
    void updateConfig(Long id, WeatherConfigCreateDTO dto);
    void deleteConfig(Long id);
    WeatherConfig getConfigById(Long id);
    WeatherConfig getDefaultConfig();
    void setDefaultConfig(Long id);
    boolean testConnection(Long configId, String city) throws Exception;
}
