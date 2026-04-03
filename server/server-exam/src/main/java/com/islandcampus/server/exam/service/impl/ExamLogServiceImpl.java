package com.islandcampus.server.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.islandcampus.server.exam.mapper.ExamLogMapper;
import com.islandcampus.server.exam.mapper.entity.ExamLog;
import com.islandcampus.server.exam.service.ExamLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamLogServiceImpl implements ExamLogService {

    private final ExamLogMapper examLogMapper;

    @Override
    public void recordLog(Long examId, String action, Long operatorId, String detail, String ipAddress) {
        ExamLog logEntity = new ExamLog();
        logEntity.setExamId(examId);
        logEntity.setAction(action);
        logEntity.setOperatorId(operatorId);
        logEntity.setDetail(detail);
        logEntity.setIpAddress(ipAddress);
        logEntity.setCreatedAt(LocalDateTime.now());
        examLogMapper.insert(logEntity);
        log.info("记录考试日志: examId={}, action={}", examId, action);
    }

    @Override
    public List<ExamLog> getLogsByExamId(Long examId, Long current, Long size) {
        Page<ExamLog> page = new Page<>(current != null ? current : 1,
                size != null ? size : 20);
        LambdaQueryWrapper<ExamLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamLog::getExamId, examId)
                .orderByDesc(ExamLog::getCreatedAt);
        Page<ExamLog> result = examLogMapper.selectPage(page, wrapper);
        return result.getRecords();
    }
}
