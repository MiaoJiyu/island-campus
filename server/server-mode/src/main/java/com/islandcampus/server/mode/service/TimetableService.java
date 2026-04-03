package com.islandcampus.server.mode.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.mode.dto.TimetableCreateDTO;
import com.islandcampus.server.mode.dto.PageQuery;
import com.islandcampus.server.mode.entity.Timetable;

import java.time.DayOfWeek;
import java.util.List;

public interface TimetableService extends IService<Timetable> {

    Timetable createTimetable(TimetableCreateDTO dto);

    void updateTimetable(Long id, TimetableCreateDTO dto);

    IPage<Timetable> pageQuery(PageQuery query, Long classId);

    List<Timetable> getWeeklyTable(Long classId);

    /**
     * 获取当前正在上的课程
     */
    Timetable getCurrentCourse(Long classId);
}
