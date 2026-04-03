package com.islandcampus.server.answer.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("answer_set")
public class AnswerSet extends BaseEntity {
    private String title;
    private Long creatorId;
    private String targetClassIds;
    private String questionsJson;
    private Integer publishType; // 1=定时 2=解密
    private LocalDateTime publishTime;
    private String password;
    private String passwordHash;
    private Integer status = 0; // 0=草稿 1=已公布 2=已撤回
}
