package com.islandcampus.server.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_organization")
public class SysOrganization extends BaseEntity {

    private Long parentId;

    private String name;

    private String code;

    /**
     * 1分区 2学校 3年级 4班级
     */
    private Integer type;

    private Integer level;

    private Integer sortOrder;

    private Integer status;

    private String description;
}
