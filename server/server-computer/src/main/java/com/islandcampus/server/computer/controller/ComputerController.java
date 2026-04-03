package com.islandcampus.server.computer.controller;

import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.base.R;
import com.islandcampus.server.computer.dto.*;
import com.islandcampus.server.computer.mapper.entity.Computer;
import com.islandcampus.server.computer.service.ComputerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/computer")
@RequiredArgsConstructor
@Tag(name = "设备管理", description = "电脑设备注册/心跳/管理接口")
public class ComputerController {

    private final ComputerService computerService;

    @PostMapping("/register")
    @Operation(summary = "设备注册")
    public R<Computer> register(@Valid @RequestBody ComputerRegisterDTO dto) {
        return R.ok(computerService.register(dto));
    }

    @PostMapping("/{id}/heartbeat")
    @Operation(summary = "心跳上报")
    public R<Void> heartbeat(@PathVariable Long id,
                             @Valid @RequestBody ComputerHeartbeatDTO dto) {
        computerService.heartbeat(id, dto);
        return R.ok();
    }

    @GetMapping
    @Operation(summary = "分页列表")
    public R<PageResult<ComputerVO>> list(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Integer isOnline,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return R.ok(computerService.listComputers(orgId, classId, isOnline, current, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "设备详情")
    public R<Computer> detail(@PathVariable Long id) {
        return R.ok(computerService.getComputerDetail(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改设备信息")
    public R<Void> update(@PathVariable Long id,
                          @Valid @RequestBody ComputerUpdateDTO dto) {
        computerService.updateComputer(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除设备")
    public R<Void> delete(@PathVariable Long id) {
        computerService.deleteComputer(id);
        return R.ok();
    }

    @PutMapping("/{id}/island-config")
    @Operation(summary = "更新灵动岛个性配置")
    public R<Void> updateIslandConfig(@PathVariable Long id,
                                      @Valid @RequestBody IslandConfigOverrideDTO dto) {
        computerService.updateIslandConfig(id, dto);
        return R.ok();
    }

    @GetMapping("/online-count")
    @Operation(summary = "在线数量统计")
    public R<Long> onlineCount(@RequestParam(required = false) Long orgId) {
        return R.ok(computerService.getOnlineCount(orgId));
    }
}
