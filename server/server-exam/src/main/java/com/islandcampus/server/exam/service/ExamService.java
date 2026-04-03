package com.islandcampus.server.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.exam.dto.ExamCalendarVO;
import com.islandcampus.server.exam.dto.ExamCreateDTO;
import com.islandcampus.server.exam.dto.ExamUpdateDTO;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.exam.mapper.entity.Exam;

import java.time.LocalDate;
import java.util.List;

public interface ExamService extends IService<Exam> {

    /**
     * 创建考试
     */
    Exam createExam(ExamCreateDTO dto);

    /**
     * 修改考试(仅未开始可修改)
     */
    void updateExam(Long id, ExamUpdateDTO dto);

    /**
     * 删除考试(仅未开始可删除)
     */
    void deleteExam(Long id);

    /**
     * 分页列表
     */
    PageResult<Exam> getExamList(Integer status, String startTime, String endTime, Long current, Long size);

    /**
     * 日历视图
     */
    List<ExamCalendarVO> getExamCalendar(Long orgId, int year, int month);

    /**
     * 手动开始考试
     */
    void manualStart(Long id);

    /**
     * 手动结束考试
     */
    void manualEnd(Long id);

    /**
     * 定时任务：自动检查并开始/结束到期考试
     */
    void autoCheckStartEnd();
}
