package com.islandcampus.server.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private String username;

    private String passwordHash;

    private String salt;

    private String realName;

    private String phone;

    private String email;

    private String avatarUrl;

    private Long orgId;

    private Long roleId;

    private Integer mustChangePwd;

    private Integer loginCount;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private Integer loginFailCount;

    private LocalDateTime lockedUntil;

    private Integer status;

    private Integer twoFactorEnabled;

    private String twoFactorSecret;
}
