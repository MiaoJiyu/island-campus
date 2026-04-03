package com.islandcampus.server.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("permission")
public Permission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String code;

    /**
     * 1API 2菜单 3按钮
     */
    private Integer type;

    private Long parentId;

    private String path;

    private String method;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private Integer deleted;
}
