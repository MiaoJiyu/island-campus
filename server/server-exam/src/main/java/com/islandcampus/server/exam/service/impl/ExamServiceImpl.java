package com.islandcampus.server.exam.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.common.utils.SecurityUtils;
import com.islandcampus.server.common.websocket.WebSocketService;
import com.islandcampus.server.exam.dto.*;
import com.islandcampus.server.exam.mapper.ExamMapper;
import com.islandcampus.server.exam.mapper.entity.Exam;
import com.islandcampus.server.exam.service.ExamLogService;
import com.islandcampus.server.exam.service.ExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements ExamService {

    private final ExamLogService examLogService;
    private final WebSocketService webSocketService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public Exam createExam(ExamCreateDTO dto) {
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }

        // 冲突检测：同一电脑同时间段只能一场考试
        checkComputerConflict(null, dto.getTargetComputerIds(), dto.getStartTime(), dto.getEndTime());

        Exam exam = new Exam();
        exam.setName(dto.getName());
        exam.setScopeType(dto.getScopeType());
        exam.setTargetOrgIds(JSON.toJSONString(dto.getTargetOrgIds() != null ? dto.getTargetOrgIds() : List.of()));
        exam.setTargetComputerIds(JSON.toJSONString(dto.getTargetComputerIds() != null ? dto.getTargetComputerIds() : List.of()));
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setRemark(dto.getRemark());
        exam.setStatus(0); // 未开始
        exam.setCreatorId(SecurityUtils.getCurrentUserId());

        if (StrUtil.isNotBlank(dto.getReleasePassword())) {
            exam.setReleasePassword(dto.getReleasePassword());
            exam.setReleasePasswordHash(hashPassword(dto.getReleasePassword()));
        } else {
            String generatedPwd = generatePassword();
            exam.setReleasePassword(generatedPwd);
            exam.setReleasePasswordHash(hashPassword(generatedPwd));
            log.info("自动生成考试密码: examName={}, pwd={}", dto.getName(), generatedPwd);
        }

        this.save(exam);

        examLogService.recordLog(exam.getId(), "create", SecurityUtils.getCurrentUserId(),
                "创建考试: " + exam.getName(), null);

        return exam;
    }

    @Override
    public void updateExam(Long id, ExamUpdateDTO dto) {
        Exam exam = this.getById(id);
        if (exam == null || exam.getDeleted() == 1) {
            throw new BusinessException("考试不存在");
        }
        if (exam.getStatus() != 0) {
            throw new BusinessException("只有未开始的考试可以修改");
        }

        if (dto.getStartTime() != null && dto.getEndTime() != null && dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }

        List<Long> targetComputerIds = dto.getTargetComputerIds() != null ? dto.getTargetComputerIds()
                : JSON.parseArray(exam.getTargetComputerIds(), Long.class);
        LocalDateTime startTime = dto.getStartTime() != null ? dto.getStartTime() : exam.getStartTime();
        LocalDateTime endTime = dto.getEndTime() != null ? dto.getEndTime() : exam.getEndTime();

        checkComputerConflict(id, targetComputerIds, startTime, endTime);

        if (dto.getName() != null) exam.setName(dto.getName());
        if (dto.getScopeType() != null) exam.setScopeType(dto.getScopeType());
        if (dto.getTargetOrgIds() != null) exam.setTargetOrgIds(JSON.toJSONString(dto.getTargetOrgIds()));
        if (dto.getTargetComputerIds() != null) exam.setTargetComputerIds(JSON.toJSONString(dto.getTargetComputerIds()));
        if (dto.getStartTime() != null) exam.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) exam.setEndTime(dto.getEndTime());
        if (dto.getRemark() != null) exam.setRemark(dto.getRemark());
        if (dto.getReleasePassword() != null) {
            exam.setReleasePassword(dto.getReleasePassword());
            exam.setReleasePasswordHash(hashPassword(dto.getReleasePassword()));
        }

        this.updateById(exam);

        examLogService.recordLog(exam.getId(), "update", SecurityUtils.getCurrentUserId(),
                "修改考试: " + exam.getName(), null);
    }

    @Override
    public void deleteExam(Long id) {
        Exam exam = this.getById(id);
        if (exam == null || exam.getDeleted() == 1) {
            throw new BusinessException("考试不存在");
        }
        if (exam.getStatus() != 0) {
            throw new BusinessException("只有未开始的考试可以删除");
        }

        this.removeById(id);

        examLogService.recordLog(exam.getId(), "delete", SecurityUtils.getCurrentUserId(),
                "删除考试: " + exam.getName(), null);
    }

    @Override
    public com.islandcampus.server.common.base.PageResult<Exam> getExamList(Integer status, String startTime,
                                                                              String endTime, Long current, Long size) {
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Exam::getCreatedAt);
        if (status != null) {
            wrapper.eq(Exam::getStatus, status);
        }
        if (StrUtil.isNotBlank(startTime)) {
            wrapper.ge(Exam::getStartTime, LocalDateTime.parse(startTime));
        }
        if (StrUtil.isNotBlank(endTime)) {
            wrapper.le(Exam::getEndTime, LocalDateTime.parse(endTime));
        }

        Page<Exam> page = new Page<>(current != null ? current : 1,
                size != null ? size : 20);
        Page<Exam> result = this.page(page, wrapper);
        return com.islandcampus.server.common.base.PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<ExamCalendarVO> getExamCalendar(Long orgId, int year, int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        LocalDateTime rangeStart = monthStart.atStartOfDay();
        LocalDateTime rangeEnd = monthEnd.atTime(23, 59, 59);

        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Exam::getStartTime, rangeStart)
                .le(Exam::getEndTime, rangeEnd)
                .ne(Exam::getStatus, 3)
                .orderByAsc(Exam::getStartTime);

        List<Exam> exams = this.list(wrapper);
        List<ExamCalendarVO> voList = new ArrayList<>();

        for (Exam exam : exams) {
            ExamCalendarVO vo = new ExamCalendarVO();
            vo.setDate(exam.getStartTime().toLocalDate());
            vo.setExamName(exam.getName());
            vo.setStatus(exam.getStatus());
            vo.setTimeRange(exam.getStartTime().format(TIME_FORMATTER) + "-" +
                    exam.getEndTime().format(TIME_FORMATTER));
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public void manualStart(Long id) {
        Exam exam = this.getById(id);
        if (exam == null || exam.getDeleted() == 1) {
            throw new BusinessException("考试不存在");
        }
        if (exam.getStatus() == 1) {
            throw new BusinessException("考试已在进行中");
        }
        if (exam.getStatus() == 2) {
            throw new BusinessException("考试已结束");
        }

        doStartExam(exam);
    }

    @Override
    public void manualEnd(Long id) {
        Exam exam = this.getById(id);
        if (exam == null || exam.getDeleted() == 1) {
            throw new BusinessException("考试不存在");
        }
        if (exam.getStatus() != 1) {
            throw new BusinessException("只有进行中的考试可以手动结束");
        }

        doEndExam(exam);
    }

    @Override
    public void autoCheckStartEnd() {
        LocalDateTime now = LocalDateTime.now();

        // 查找应该开始但还没开始的考试（start_time <= now 且 status=0）
        LambdaQueryWrapper<Exam> startWrapper = new LambdaQueryWrapper<>();
        startWrapper.le(Exam::getStartTime, now)
                .eq(Exam::getStatus, 0);
        List<Exam> toStartExams = this.list(startWrapper);
        for (Exam exam : toStartExams) {
            try {
                doStartExam(exam);
                log.info("定时任务自动开始考试: {}", exam.getId());
            } catch (Exception e) {
                log.error("自动开始考试失败: examId={}", exam.getId(), e);
            }
        }

        // 查找应该结束但还在进行的考试（end_time <= now 且 status=1）
        LambdaQueryWrapper<Exam> endWrapper = new LambdaQueryWrapper<>();
        endWrapper.le(Exam::getEndTime, now)
                .eq(Exam::getStatus, 1);
        List<Exam> toEndExams = this.list(endWrapper);
        for (Exam exam : toEndExams) {
            try {
                doEndExam(exam);
                log.info("定时任务自动结束考试: {}", exam.getId());
            } catch (Exception e) {
                log.error("自动结束考试失败: examId={}", exam.getId(), e);
            }
        }
    }

    /**
     * 执行考试开始逻辑
     */
    private void doStartExam(Exam exam) {
        exam.setStatus(1); // 进行中
        exam.setActualStart(LocalDateTime.now());
        this.updateById(exam);

        // 通过WebSocket发送考试指令给目标电脑
        sendExamCommandToComputers(exam, "exam_start");

        examLogService.recordLog(exam.getId(), SecurityUtils.getCurrentUserId() != null ? "manual_start" : "start",
                SecurityUtils.getCurrentUserId(),
                "开始考试: " + exam.getName(), null);
    }

    /**
     * 执行考试结束逻辑
     */
    private void doEndExam(Exam exam) {
        exam.setStatus(2); // 已结束
        exam.setActualEnd(LocalDateTime.now());
        this.updateById(exam);

        // 发送考试结束指令给目标电脑
        sendExamCommandToComputers(exam, "exam_end");

        examLogService.recordLog(exam.getId(), SecurityUtils.getCurrentUserId() != null ? "manual_end" : "end",
                SecurityUtils.getCurrentUserId(),
                "结束考试: " + exam.getName(), null);
    }

    /**
     * 发送考试指令到目标电脑
     */
    private void sendExamCommandToComputers(Exam exam, String commandType) {
        List<Long> computerIds = JSON.parseArray(exam.getTargetComputerIds(), Long.class);
        if (computerIds == null || computerIds.isEmpty()) {
            return;
        }

        ExamCommand cmd = new ExamCommand();
        cmd.setType(commandType);
        cmd.setData(new ExamCommand.ExamCommandData(
                exam.getReleasePasswordHash(),
                exam.getName(),
                exam.getEndTime()
        ));

        for (Long computerId : computerIds) {
            webSocketService.sendExamCommand(computerId, Map.of(
                    "type", commandType,
                    "data", Map.of(
                            "passwordHash", exam.getReleasePasswordHash(),
                            "examName", exam.getName(),
                            "endTime", exam.getEndTime().toString()
                    )
            ));
        }

        log.info("发送{}指令到{}台电脑, examId={}", commandType, computerIds.size(), exam.getId());
    }

    /**
     * 冲突检测：同一电脑同时间段是否已有其他考试
     */
    private void checkComputerConflict(Long excludeExamId, List<Long> targetComputerIds,
                                       LocalDateTime startTime, LocalDateTime endTime) {
        if (targetComputerIds == null || targetComputerIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<Exam> conflictWrapper = new LambdaQueryWrapper<>();
        conflictWrapper.ne(Exam::getStatus, 3) // 排除已取消的
                .and(w -> w.le(Exam::getStartTime, startTime).ge(Exam::getEndTime, startTime))
                .or(w -> w.le(Exam::getStartTime, endTime).ge(Exam::getEndTime, endTime))
                .or(w -> w.ge(Exam::getStartTime, startTime).le(Exam::getEndTime, endTime));

        if (excludeExamId != null) {
            conflictWrapper.ne(Exam::getId, excludeExamId);
        }

        List<Exam> conflicts = this.list(conflictWrapper);
        if (!conflicts.isEmpty()) {
            throw new BusinessException("时间段冲突，该时段已有考试: " + conflicts.get(0).getName());
        }
    }

    /**
     * 生成6位随机密码
     */
    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 密码哈希(SHA-256)
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("密码哈希算法不可用", e);
        }
    }
}
