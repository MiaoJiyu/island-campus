package com.islandcampus.server.exam.service;

import com.islandcampus.server.exam.mapper.entity.ExamLog;

public interface ExamLogService {
    void recordLog(Long examId, String action, Long operatorId, String detail, String ipAddress);
    List<ExamLog> getLogsByExamId(Long examId, Long current, Long size);
}
