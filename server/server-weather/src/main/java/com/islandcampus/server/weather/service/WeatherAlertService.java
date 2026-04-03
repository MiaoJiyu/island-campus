package com.islandcampus.server.weather.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.weather.dto.AlertRuleCreateDTO;
import com.islandcampus.server.weather.mapper.entity.WeatherAlertRule;

public interface WeatherAlertService extends IService<WeatherAlertRule> {
    void createRule(AlertRuleCreateDTO dto);
    void updateRule(Long id, AlertRuleCreateDTO dto);
    void deleteRule(Long id);
    void evaluateRules(WeatherDataVO weatherData);
    void executeAlert(WeatherAlertRule rule, JSONObject weatherData);
    boolean testRule(Long ruleId, JSONObject weatherData);
}
