package com.islandcampus.server.exam.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_log")
public class ExamLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long examId;

    /**
     * 操作: create/start/end/cancel/manual_start/manual_end
     */
    private String action;

    private Long operatorId;

    private String detail;

    private String ipAddress;

    private LocalDateTime createdAt;
}
