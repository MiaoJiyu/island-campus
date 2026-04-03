package com.islandcampus.server.mode.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.base.R;
import com.islandcampus.server.mode.dto.ModeStatusResponse;
import com.islandcampus.server.mode.dto.ModeSwitchRequest;
import com.islandcampus.server.mode.dto.ScheduleCreateDTO;
import com.islandcampus.server.mode.entity.ModeSchedule;
import com.islandcampus.server.mode.entity.SceneMode;
import com.islandcampus.server.mode.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mode")
@RequiredArgsConstructor
@Tag(name = "情景模式", description = "情景模式管理与切换")
public class ModeController {

    private final SceneModeService sceneModeService;
    private final ModeScheduleService modeScheduleService;
    private final SceneModeControlService controlService;

    @GetMapping("/modes")
    @Operation(summary = "获取情景模式列表")
    public R<java.util.List<SceneMode>> listModes() {
        return R.ok(sceneModeService.list());
    }

    @PostMapping("/modes")
    @Operation(summary = "创建情景模式")
    public R<SceneMode> createMode(@RequestBody SceneMode mode) {
        sceneModeService.save(mode);
        return R.ok(mode);
    }

    @PutMapping("/modes/{id}")
    @Operation(summary = "更新情景模式")
    public R<Void> updateMode(@PathVariable Long id, @RequestBody SceneMode mode) {
        mode.setId(id);
        sceneModeService.updateById(mode);
        return R.ok();
    }

    @DeleteMapping("/modes/{id}")
    @Operation(summary = "删除情景模式")
    public R<Void> deleteMode(@PathVariable Long id) {
        sceneModeService.deleteMode(id);
        return R.ok();
    }

    @GetMapping("/schedules")
    @Operation(summary = "获取切换计划列表")
    public R<IPage<ModeSchedule>> listSchedules(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long classId) {

        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ModeSchedule> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (classId != null) {
            wrapper.eq(ModeSchedule::getClassId, classId);
        }
        wrapper.orderByDesc(ModeSchedule::getCreatedAt);
        IPage<ModeSchedule> result = modeScheduleService.page(page, wrapper);
        return R.ok(result);
    }

    @PostMapping("/schedules")
    @Operation(summary = "创建切换计划")
    public R<ModeSchedule> createSchedule(@Valid @RequestBody ScheduleCreateDTO dto) {
        return R.ok(modeScheduleService.create(dto));
    }

    @PutMapping("/schedules/{id}")
    @Operation(summary = "修改切换计划")
    public R<Void> updateSchedule(@PathVariable Long id,
                                  @Valid @RequestBody ScheduleCreateDTO dto) {
        modeScheduleService.updateSchedule(id, dto);
        return R.ok();
    }

    @DeleteMapping("/schedules/{id}")
    @Operation(summary = "删除切换计划")
    public R<Void> deleteSchedule(@PathVariable Long id) {
        modeScheduleService.removeById(id);
        return R.ok();
    }

    @PostMapping("/switch")
    @Operation(summary = "手动切换模式")
    public R<Void> switchMode(@Valid @RequestBody ModeSwitchRequest request) {
        controlService.switchMode(request);
        return R.ok();
    }

    @GetMapping("/current-status")
    @Operation(summary = "获取当前模式状态")
    public R<ModeStatusResponse> currentStatus(@RequestParam(required = false) Long computerId) {
        if (computerId == null) {
            throw new com.islandcampus.server.common.exception.BusinessException("computerId不能为空");
        }
        return R.ok(controlService.getCurrentStatus(computerId));
    }
}
