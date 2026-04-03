package com.islandcampus.server.weather.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.weather.dto.WeatherConfigCreateDTO;
import com.islandcampus.server.weather.mapper.WeatherConfigMapper;
import com.islandcampus.server.weather.mapper.entity.WeatherConfig;
import com.islandcampus.server.weather.service.WeatherConfigService;
import com.islandcampus.server.weather.service.WeatherDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherConfigServiceImpl extends ServiceImpl<WeatherConfigMapper, WeatherConfig> implements WeatherConfigService {

    private final WeatherDataService weatherDataService;

    @Override
    public void createConfig(WeatherConfigCreateDTO dto) {
        WeatherConfig config = new WeatherConfig();
        copyDtoToEntity(dto, config);

        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearExistingDefault();
        }

        this.save(config);
    }

    @Override
    public void updateConfig(Long id, WeatherConfigCreateDTO dto) {
        WeatherConfig config = this.getById(id);
        if (config == null || config.getDeleted() == 1) {
            throw new BusinessException("配置不存在");
        }

        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearExistingDefault();
        }

        copyDtoToEntity(dto, config);
        this.updateById(config);
    }

    @Override
    public void deleteConfig(Long id) {
        WeatherConfig config = this.getById(id);
        if (config == null || config.getDeleted() == 1) {
            throw new BusinessException("配置不存在");
        }
        this.removeById(id);
    }

    @Override
    public WeatherConfig getConfigById(Long id) {
        return this.getById(id);
    }

    @Override
    public WeatherConfig getDefaultConfig() {
        LambdaQueryWrapper<WeatherConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeatherConfig::getIsDefault, 1)
                .eq(WeatherConfig::getEnabled, 1)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public void setDefaultConfig(Long id) {
        WeatherConfig config = this.getById(id);
        if (config == null || config.getDeleted() == 1) {
            throw new BusinessException("配置不存在");
        }
        clearExistingDefault();
        config.setIsDefault(1);
        this.updateById(config);
    }

    @Override
    public boolean testConnection(Long configId, String city) throws Exception {
        try {
            WeatherDataVO data = weatherDataService.fetchWeather(configId, city);
            log.info("天气API测试连接成功: configId={}, city={}", configId, city);
            return data != null && data.getTemperature() != null;
        } catch (Exception e) {
            log.error("天气API测试连接失败: configId={}, city={}", configId, city, e);
            throw e;
        }
    }

    private void clearExistingDefault() {
        LambdaQueryWrapper<WeatherConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeatherConfig::getIsDefault, 1);
        WeatherConfig existing = this.getOne(wrapper);
        if (existing != null) {
            existing.setIsDefault(0);
            this.updateById(existing);
        }
    }

    private void copyDtoToEntity(WeatherConfigCreateDTO dto, WeatherConfig entity) {
        entity.setName(dto.getName());
        entity.setApiUrl(dto.getApiUrl());
        entity.setApiMethod(dto.getApiMethod() != null ? dto.getApiMethod() : "GET");
        entity.setApiKey(dto.getApiKey());
        entity.setRequestHeaders(dto.getRequestHeaders());
        entity.setRequestBodyTemplate(dto.getRequestBodyTemplate());
        entity.setCityParamName(dto.getCityParamName() != null ? dto.getCityParamName() : "city");
        entity.setResponseJsonPath(dto.getResponseJsonPath());
        entity.setTempPath(dto.getTempPath() != null ? dto.getTempPath() : "$.temperature");
        entity.setWeatherPath(dto.getWeatherPath() != null ? dto.getWeatherPath() : "$.weather");
        entity.setWeatherIconPath(dto.getWeatherIconPath() != null ? dto.getWeatherIconPath() : "$.weather_icon");
        entity.setForecastPath(dto.getForecastPath() != null ? dto.getForecastPath() : "$.forecast");
        entity.setHourlyPath(dto.getHourlyPath() != null ? dto.getHourlyPath() : "$.hourly_forecast");
        entity.setAqiPath(dto.getAqiPath() != null ? dto.getAqiPath() : "$.aqi");
        entity.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        entity.setCityDefault(dto.getCityDefault());
        entity.setFetchIntervalMinutes(dto.getFetchIntervalMinutes() != null ? dto.getFetchIntervalMinutes() : 30);
        entity.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : 1);
    }
}
