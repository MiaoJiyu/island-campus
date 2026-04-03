package com.islandcampus.server.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("login_log")
public LoginLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String username;

    private LocalDateTime loginTime;

    private String ipAddress;

    private String userAgent;

    /**
     * 登录是否成功
     */
    private Integer success;

    private String failReason;
}
