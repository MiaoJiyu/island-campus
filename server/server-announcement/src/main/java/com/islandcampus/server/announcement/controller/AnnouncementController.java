package com.islandcampus.server.announcement.controller;

import com.islandcampus.server.announcement.dto.AnnouncementCreateDTO;
import com.islandcampus.server.announcement.dto.ConfirmDTO;
import com.islandcampus.server.announcement.dto.EmergencyBroadcastDTO;
import com.islandcampus.server.announcement.mapper.entity.Announcement;
import com.islandcampus.server.announcement.mapper.entity.BroadcastConfirm;
import com.islandcampus.server.announcement.service.AnnouncementService;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.base.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/announcement")
@RequiredArgsConstructor
@Tag(name = "公告广播管理", description = "公告CRUD、发布、撤回、紧急广播接口")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    @Operation(summary = "创建公告")
    public R<Announcement> create(@Valid @RequestBody AnnouncementCreateDTO dto) {
        return R.ok(announcementService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改公告")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody AnnouncementCreateDTO dto) {
        announcementService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告")
    public R<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取公告详情")
    public R<Announcement> detail(@PathVariable Long id) {
        return R.ok(announcementService.getDetail(id));
    }

    @GetMapping
    @Operation(summary = "分页列表")
    public R<PageResult<Announcement>> list(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return R.ok(announcementService.getList(type, status, keyword, current, size));
    }

    @PostMapping("/publish/{id}")
    @Operation(summary = "发布公告")
    public R<Void> publish(@PathVariable Long id) {
        announcementService.publish(id);
        return R.ok();
    }

    @PostMapping("/revoke/{id}")
    @Operation(summary = "撤回公告")
    public R<Void> revoke(@PathVariable Long id) {
        announcementService.revoke(id);
        return R.ok();
    }

    @PostMapping("/emergency")
    @Operation(summary = "紧急广播")
    public R<Announcement> emergencyBroadcast(@Valid @RequestBody EmergencyBroadcastDTO dto) {
        announcementService.emergencyBroadcast(dto);
        return R.ok();
    }

    @GetMapping("/active")
    @Operation(summary = "有效公告列表(客户端用)")
    public R<List<Announcement>> activeList(@RequestParam(required = false) Long orgId) {
        return R.ok(announcementService.getActiveAnnouncements(orgId));
    }

    @GetMapping("/{id}/confirms")
    @Operation(summary = "确认列表")
    public R<List<BroadcastConfirm>> confirms(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return R.ok(announcementService.getConfirmedList(id, current, size));
    }

    @GetMapping("/{id}/unconfirmed")
    @Operation(summary = "未确认列表")
    public R<List<BroadcastConfirm>> unconfirmed(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return R.ok(announcementService.getUnconfirmedList(id, current, size));
    }

    @PostMapping("/confirm")
    @Operation(summary = "确认回执")
    public R<BroadcastConfirm> confirm(
            @Valid @RequestBody ConfirmDTO dto,
            HttpServletRequest request) {
        String ip = getClientIp(request);
        // 从SecurityUtils获取当前用户信息，这里简化处理
        Long userId = null; // SecurityUtils.getCurrentUserId()
        Long computerId = null; // 可从请求头中获取
        return R.ok(announcementService.confirmBroadcast(dto, computerId, userId, ip));
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
