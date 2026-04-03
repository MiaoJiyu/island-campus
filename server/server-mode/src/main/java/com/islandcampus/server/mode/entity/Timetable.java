package com.islandcampus.server.mode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("timetable")
public class Timetable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long classId;

    /**
     * 周几(1-7, 1=周一)
     */
    private Integer dayOfWeek;

    /**
     * 第几节
     */
    private Integer period;

    private String subject;

    private String teacherName;

    private String room;

    private String startTime;

    private String endTime;

    private Integer sortOrder;

    private String semester;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}
