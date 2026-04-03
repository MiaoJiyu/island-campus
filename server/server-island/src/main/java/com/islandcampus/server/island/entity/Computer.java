package com.islandcampus.server.island.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
     * 是否在线
     */
    private Integer isOnline;

    private java.time.LocalDateTime lastHeartbeat;

    /**
     * 健康状态: 0红 1黄 2绿
     */
    private Integer healthStatus;

    /**
     * 灵动岛配置JSON
     */
    private String islandConfigJson;

    private String resolution;

    private String remark;

    private Integer status;
}
