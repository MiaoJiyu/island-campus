package com.islandcampus.server.weather.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.islandcampus.server.weather.mapper.entity.WeatherConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WeatherConfigMapper extends BaseMapper<WeatherConfig> {
}
