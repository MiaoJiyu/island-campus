package com.islandcampus.server.exam.controller;

import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.base.R;
import com.islandcampus.server.exam.dto.*;
import com.islandcampus.server.exam.mapper.entity.Exam;
import com.islandcampus.server.exam.mapper.entity.ExamLog;
import com.islandcampus.server.exam.service.ExamLogService;
import com.islandcampus.server.exam.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/exam")
@RequiredArgsConstructor
@Tag(name = "考试管理", description = "考试创建/修改/删除/控制等接口")
public class ExamController {

    private final ExamService examService;
    private final ExamLogService examLogService;

    @PostMapping
    @Operation(summary = "创建考试")
    public R<Exam> create(@Valid @RequestBody ExamCreateDTO dto) {
        return R.ok(examService.createExam(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改考试")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ExamUpdateDTO dto) {
        examService.updateExam(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除考试")
    public R<Void> delete(@PathVariable Long id) {
        examService.deleteExam(id);
        return R.ok();
    }

    @GetMapping
    @Operation(summary = "分页列表")
    public R<PageResult<Exam>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return R.ok(examService.getExamList(status, startTime, endTime, current, size));
    }

    @GetMapping("/calendar")
    @Operation(summary = "日历视图")
    public R<List<ExamCalendarVO>> calendar(
            @RequestParam(required = false) Long orgId,
            @RequestParam int year,
            @RequestParam int month) {
        return R.ok(examService.getExamCalendar(orgId, year, month));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "手动开始考试")
    public R<Void> manualStart(@PathVariable Long id) {
        examService.manualStart(id);
        return R.ok();
    }

    @PostMapping("/{id}/end")
    @Operation(summary = "手动结束考试")
    public R<Void> manualEnd(@PathVariable Long id) {
        examService.manualEnd(id);
        return R.ok();
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "操作日志")
    public R<List<ExamLog>> logs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return R.ok(examLogService.getLogsByExamId(id, current, size));
    }
}
