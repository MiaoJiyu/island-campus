package com.islandcampus.server.mode.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scene_mode")
public class SceneMode extends BaseEntity {

    private String name;

    /**
     * class/break/study/exam
     */
    private String code;

    private String icon;

    private String color;

    /**
     * 是否锁定屏幕
     */
    private Integer isLockScreen;

    /**
     * 是否屏蔽网络
     */
    private Integer blockInternet;

    /**
     * 屏蔽应用列表(JSON)
     */
    private String blockApps;

    /**
     * 提醒间隔(秒)
     */
    private Integer reminderInterval;

    private String reminderText;

    /**
     * 策略脚本(Groovy脚本)
     */
    private String strategyScript;

    /**
     * 是否系统内置
     */
    private Integer isSystem;

    private Integer status;
}
