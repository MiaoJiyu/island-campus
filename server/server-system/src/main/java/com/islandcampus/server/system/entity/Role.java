package com.islandcampus.server.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role")
public Role {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String code;

    /**
     * 是否内置角色，内置角色不可删除
     */
    private Integer isBuiltin;

    private Long permissionMask;

    /**
     * 数据范围: 1全部 2本部门 3本部门及以下 4仅本人
     */
    private Integer dataScope;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}
