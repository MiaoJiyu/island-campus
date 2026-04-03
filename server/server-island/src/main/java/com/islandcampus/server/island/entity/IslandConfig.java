package com.islandcampus.server.island.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("island_config")
public class IslandConfig extends BaseEntity {

    private String configName;

    /**
     * 1全局 2年级 3班级
     */
    private Integer scopeType;

    private Long scopeId;

    /**
     * 灵动岛配置JSON
     */
    private String configJson;

    private Integer isActive;
}
