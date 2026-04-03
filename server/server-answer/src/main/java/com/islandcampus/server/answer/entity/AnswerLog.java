package com.islandcampus.server.answer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("answer_log")
public class AnswerLog implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long answerSetId;
    private String actionType; // publish_auto/publish_decrypt/view
    private Long operatorId;
    private Long tokenId;
    private String ipAddress;
    private String detail;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
