package com.islandcampus.server.mode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.mode.dto.TimetableCreateDTO;
import com.islandcampus.server.mode.entity.Timetable;
import com.islandcampus.server.mode.mapper.TimetableMapper;
import com.islandcampus.server.mode.service.TimetableService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TimetableServiceImpl extends ServiceImpl<TimetableMapper, Timetable>
        implements TimetableService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public Timetable createTimetable(TimetableCreateDTO dto) {
        Timetable entity = new Timetable();
        BeanUtils.copyProperties(dto, entity);
        save(entity);
        System.out.println("[AUDIT] 创建课程: subject=" + dto.getSubject()
                + ", classId=" + dto.getClassId()
                + ", operator=" + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());
        return entity;
    }

    @Override
    public void updateTimetable(Long id, TimetableCreateDTO dto) {
        Timetable existing = getById(id);
        if (existing == null) {
            throw new com.islandcampus.server.common.exception.BusinessException("课程记录不存在");
        }
        BeanUtils.copyProperties(dto, existing, "id", "createdAt");
        updateById(existing);
        System.out.println("[AUDIT] 更新课程: courseId=" + id
                + ", operator=" + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());
    }

    @Override
    public IPage<Timetable> pageQuery(com.islandcampus.server.system.dto.PageQuery query, Long classId) {
        Page<Timetable> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<Timetable> wrapper = new LambdaQueryWrapper<>();
        if (classId != null) {
            wrapper.eq(Timetable::getClassId, classId);
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(Timetable::getSubject, query.getKeyword())
                    .or().like(Timetable::getTeacherName, query.getKeyword()));
        }
        wrapper.orderByAsc(Timetable::getDayOfWeek).orderByAsc(Timetable::getPeriod);
        return page(page, wrapper);
    }

    @Override
    public List<Timetable> getWeeklyTable(Long classId) {
        return list(new LambdaQueryWrapper<Timetable>()
                .eq(Timetable::getClassId, classId)
                .orderByAsc(Timetable::getDayOfWeek)
                .orderByAsc(Timetable::getPeriod));
    }

    @Override
    public Timetable getCurrentCourse(Long classId) {
        int dayOfWeek = java.time.LocalDate.now().getDayOfWeek().getValue();
        LocalTime now = LocalTime.now();

        List<Timetable> courses = list(new LambdaQueryWrapper<Timetable>()
                .eq(Timetable::getClassId, classId)
                .eq(Timetable::getDayOfWeek, dayOfWeek)
                .orderByAsc(Timetable::getStartTime));

        for (Timetable course : courses) {
            if (course.getStartTime() == null || course.getEndTime() == null) continue;
            try {
                LocalTime start = LocalTime.parse(course.getStartTime(), TIME_FORMATTER);
                LocalTime end = LocalTime.parse(course.getEndTime(), TIME_FORMATTER);
                if (!now.isBefore(start) && now.isBefore(end)) {
                    return course;
                }
            } catch (Exception ignored) {
                // 时间格式错误则跳过
            }
        }
        return null;
    }
}
