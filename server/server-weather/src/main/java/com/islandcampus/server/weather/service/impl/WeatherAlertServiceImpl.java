package com.islandcampus.server.weather.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.common.redis.PublisherService;
import com.islandcampus.server.common.websocket.WebSocketService;
import com.islandcampus.server.weather.dto.AlertRuleCreateDTO;
import com.islandcampus.server.weather.dto.WeatherDataVO;
import com.islandcampus.server.weather.mapper.WeatherAlertRuleMapper;
import com.islandcampus.server.weather.mapper.entity.WeatherAlertRule;
import com.islandcampus.server.weather.service.WeatherAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherAlertServiceImpl extends ServiceImpl<WeatherAlertRuleMapper, WeatherAlertRule> implements WeatherAlertService {

    private final WebSocketService webSocketService;
    private final PublisherService publisherService;

    @Override
    public void createRule(AlertRuleCreateDTO dto) {
        WeatherAlertRule rule = new WeatherAlertRule();
        rule.setName(dto.getName());
        rule.setConditionExpr(dto.getConditionExpr());
        rule.setActionType(dto.getActionType());
        rule.setActionConfig(dto.getActionConfig());
        rule.setWeatherConfigId(dto.getWeatherConfigId());
        rule.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        rule.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : 1);
        this.save(rule);
    }

    @Override
    public void updateRule(Long id, AlertRuleCreateDTO dto) {
        WeatherAlertRule rule = this.getById(id);
        if (rule == null || rule.getDeleted() == 1) {
            throw new BusinessException("规则不存在");
        }
        rule.setName(dto.getName());
        rule.setConditionExpr(dto.getConditionExpr());
        rule.setActionType(dto.getActionType());
        rule.setActionConfig(dto.getActionConfig());
        rule.setWeatherConfigId(dto.getWeatherConfigId());
        rule.setPriority(dto.getPriority());
        rule.setEnabled(dto.getEnabled());
        this.updateById(rule);
    }

    @Override
    public void deleteRule(Long id) {
        WeatherAlertRule rule = this.getById(id);
        if (rule == null || rule.getDeleted() == 1) {
            throw new BusinessException("规则不存在");
        }
        this.removeById(id);
    }

    @Override
    public void evaluateRules(WeatherDataVO weatherData) {
        if (weatherData == null) return;

        LambdaQueryWrapper<WeatherAlertRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeatherAlertRule::getEnabled, 1)
                .orderByAsc(WeatherAlertRule::getPriority);
        List<WeatherAlertRule> rules = this.list(wrapper);

        JSONObject jsonData = JSON.parseObject(JSON.toJSONString(weatherData));

        for (WeatherAlertRule rule : rules) {
            try {
                if (matchCondition(rule, jsonData)) {
                    executeAlert(rule, jsonData);
                }
            } catch (Exception e) {
                log.error("评估预警规则失败: ruleId={}", rule.getId(), e);
            }
        }
    }

    @Override
    public void executeAlert(WeatherAlertRule rule, JSONObject weatherData) {
        switch (rule.getActionType()) {
            case 1 -> executeBlinkAlert(rule, weatherData);
            case 2 -> executeAnnouncementAlert(rule, weatherData);
            case 3 -> executeRemoteNotifyAlert(rule, weatherData);
            case 4 -> executeMultiActionAlert(rule, weatherData);
            default -> log.warn("未知动作类型: {}", rule.getActionType());
        }
        log.info("执行预警提醒: ruleName={}, actionType={}", rule.getName(), rule.getActionType());
    }

    @Override
    public boolean testRule(Long ruleId, JSONObject weatherData) {
        WeatherAlertRule rule = this.getById(ruleId);
        if (rule == null || rule.getDeleted() == 1) {
            throw new BusinessException("规则不存在");
        }
        return matchCondition(rule, weatherData);
    }

    private boolean matchCondition(WeatherAlertRule rule, JSONObject weatherData) {
        if (rule.getConditionExpr() == null || rule.getConditionExpr().isBlank()) {
            return false;
        }
        try {
            JSONArray conditions = JSON.parseArray(rule.getConditionExpr());
            if (conditions == null || conditions.isEmpty()) return false;

            for (int i = 0; i < conditions.size(); i++) {
                JSONObject cond = conditions.getJSONObject(i);
                if (!evaluateSingleCondition(cond, weatherData)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("解析条件表达式失败", e);
            return false;
        }
    }

    private boolean evaluateSingleCondition(JSONObject condition, JSONObject weatherData) {
        String field = condition.getString("field");
        String op = condition.getString("operator");
        Number value = condition.getNumber("value");

        if (field == null || op == null || value == null) return false;

        Object fieldValue = weatherData.get(field);
        if (!(fieldValue instanceof Number numValue)) return false;

        double actual = numValue.doubleValue();
        double target = value.doubleValue();

        return switch (op) {
            case "gt" -> actual > target;
            case "lt" -> actual < target;
            case "eq" -> Double.compare(actual, target) == 0;
            case "gte" -> actual >= target;
            case "lte" -> actual <= target;
            default -> false;
        };
    }

    private void executeBlinkAlert(WeatherAlertRule rule, JSONObject weatherData) {
        webSocketService.broadcast("/topic/weather/alert",
                Map.of(
                        "type", "blink_alert",
                        "ruleId", rule.getId(),
                        "ruleName", rule.getName(),
                        "message", buildAlertMessage(rule, weatherData)
                ));
    }

    private void executeAnnouncementAlert(WeatherAlertRule rule, JSONObject weatherData) {
        publisherService.publish("announcement:weather_alert", JSON.toJSONString(Map.of(
                "type", "auto_announcement",
                "title", "[天气预警] " + rule.getName(),
                "content", buildAlertMessage(rule, weatherData),
                "priority", 2,
                "source", "weather_alert"
        )));
    }

    private void executeRemoteNotifyAlert(WeatherAlertRule rule, JSONObject weatherData) {
        publisherService.publish("message:weather_notify", JSON.toJSONString(Map.of(
                "type", "remote_message",
                "title", "天气提醒",
                "content", buildAlertMessage(rule, weatherData),
                "priority", rule.getPriority()
        )));
    }

    private void executeMultiActionAlert(WeatherAlertRule rule, JSONObject weatherData) {
        if (rule.getActionConfig() != null && !rule.getActionConfig().isBlank()) {
            JSONObject actionCfg = JSON.parseObject(rule.getActionConfig());
            Integer blink = actionCfg.getInteger("blink");
            Integer announcement = actionCfg.getInteger("announcement");
            Integer remoteNotify = actionCfg.getInteger("remoteNotify");

            if (blink != null && blink == 1) executeBlinkAlert(rule, weatherData);
            if (announcement != null && announcement == 1) executeAnnouncementAlert(rule, weatherData);
            if (remoteNotify != null && remoteNotify == 1) executeRemoteNotifyAlert(rule, weatherData);
        }
    }

    private String buildAlertMessage(WeatherAlertRule rule, JSONObject weatherData) {
        StringBuilder msg = new StringBuilder();
        msg.append("【").append(rule.getName()).append("】 ");
        if (weatherData != null) {
            if (weatherData.containsKey("temperature")) {
                msg.append("温度: ").append(weatherData.get("temperature")).append("\u00B0C ");
            }
            if (weatherData.containsKey("weather")) {
                msg.append("天气: ").append(weatherData.get("weather")).append(" ");
            }
            if (weatherData.containsKey("aqi")) {
                msg.append("AQI: ").append(weatherData.get("aqi")).append(" ");
            }
        }
        return msg.toString();
    }
}
