package com.islandcampus.server.mode.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.base.R;
import com.islandcampus.server.mode.dto.TimetableCreateDTO;
import com.islandcampus.server.mode.entity.Timetable;
import com.islandcampus.server.mode.service.TimetableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/timetable")
@RequiredArgsConstructor
@Tag(name = "课程表", description = "课程表管理")
public class TimetableController {

    private final TimetableService timetableService;

    @GetMapping
    @Operation(summary = "分页查询课程表")
    public R<IPage<Timetable>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String keyword) {

        com.islandcampus.server.system.dto.PageQuery query =
                new com.islandcampus.server.system.dto.PageQuery();
        query.setCurrent(current);
        query.setSize(size);
        query.setKeyword(keyword);

        return R.ok(timetableService.pageQuery(query, classId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取课程详情")
    public R<Timetable> detail(@PathVariable Long id) {
        return R.ok(timetableService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建课程")
    public R<Timetable> create(@Valid @RequestBody TimetableCreateDTO dto) {
        return R.ok(timetableService.createTimetable(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新课程")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody TimetableCreateDTO dto) {
        timetableService.updateTimetable(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除课程")
    public R<Void> delete(@PathVariable Long id) {
        timetableService.removeById(id);
        return R.ok();
    }

    @GetMapping("/weekly/{classId}")
    @Operation(summary = "获取周课表")
    public R<List<Timetable>> weekly(@PathVariable Long classId) {
        return R.ok(timetableService.getWeeklyTable(classId));
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前正在上的课程")
    public R<Timetable> currentCourse(@RequestParam Long classId) {
        return R.ok(timetableService.getCurrentCourse(classId));
    }
}
