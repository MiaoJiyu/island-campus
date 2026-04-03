package com.islandcampus.server.plugin.mapper.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plugin")
public class Plugin extends BaseEntity {

    private String name;

    /**
     * 插件唯一编码
     */
    private String code;

    /**
     * 类型: 1-前端 2-Groovy 3-JAR
     */
    private Integer type;

    /**
     * 版本号
     */
    private String version;

    private String description;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 入口点（类名/组件路径）
     */
    private String entryPoint;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String permissionsDeclared;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String configSchema;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String configValues;

    /**
     * 作者
     */
    private String author;

    /**
     * 是否启用: 0-停用 1-启用
     */
    private Integer enabled;

    private LocalDateTime installedAt;
}
