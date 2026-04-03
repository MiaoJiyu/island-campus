package com.islandcampus.server.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("remote_message")
public class RemoteMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long senderId;
    private String title;
    private String content;
    private String targetScope;
    private Integer messageType;
    private String readBy;
    private LocalDateTime sentAt;
    private LocalDateTime expireAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
