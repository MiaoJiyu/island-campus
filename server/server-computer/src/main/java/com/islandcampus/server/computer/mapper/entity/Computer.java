package com.islandcampus.server.computer.mapper.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("computer")
public class Computer extends BaseEntity {

    private String name;

    private String macAddress;

    private String ipAddress;

    private Long classId;

    private String osInfo;

    private String clientVersion;

    /**
     * 是否在线: 0-离线 1-在线
     */
    private Integer isOnline;

    private LocalDateTime lastHeartbeat;

    /**
     * 健康状态: 0-红色(异常) 1-黄色(警告) 2-绿色(正常)
     */
    private Integer healthStatus;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String islandConfigJson;

    private String resolution;

    private String remark;

    /**
     * 状态: 0-禁用 1-正常
     */
    private Integer status;
}
