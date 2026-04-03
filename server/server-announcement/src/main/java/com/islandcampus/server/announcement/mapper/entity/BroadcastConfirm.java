package com.islandcampus.server.announcement.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("broadcast_confirm")
public class BroadcastConfirm {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long announcementId;

    private Long computerId;

    private Long userId;

    private LocalDateTime confirmedAt;

    private String ipAddress;
}
