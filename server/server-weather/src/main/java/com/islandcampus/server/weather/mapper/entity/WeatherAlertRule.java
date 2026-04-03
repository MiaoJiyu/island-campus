package com.islandcampus.server.weather.mapper.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("weather_alert_rule")
public class WeatherAlertRule extends BaseEntity {

    private String name;

    /**
     * 条件表达式(JSON条件组)
     */
    private String conditionExpr;

    /**
     * 动作类型: 1-闪烁 2-公告 3-远程通知 4-多动作
     */
    private Integer actionType;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String actionConfig;

    private Long weatherConfigId;

    private Integer priority;

    private Integer enabled;
}
