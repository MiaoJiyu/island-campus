package com.islandcampus.server.message.controller;

import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.base.R;
import com.islandcampus.server.common.utils.SecurityUtils;
import com.islandcampus.server.message.dto.MessageVO;
import com.islandcampus.server.message.dto.SendMessageDTO;
import com.islandcampus.server.message.service.RemoteMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "远程消息管理")
@RestController
@RequestMapping("/api/v1/remote")
@RequiredArgsConstructor
public class RemoteMessageController {
    private final RemoteMessageService remoteMessageService;

    @Operation(summary = "发送远程消息")
    @PostMapping("/message")
    public R<Void> sendMessage(@Valid @RequestBody SendMessageDTO dto) {
        remoteMessageService.send(dto);
        return R.ok("消息发送成功", null);
    }

    @Operation(summary = "我的消息列表(分页)")
    @GetMapping("/messages")
    public R<PageResult<MessageVO>> listMessages(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return R.ok(remoteMessageService.listMessages(SecurityUtils.getCurrentUserId(), current, size));
    }

    @Operation(summary = "未读消息数量")
    @GetMapping("/unread-count")
    public R<Long> getUnreadCount() {
        return R.ok(remoteMessageService.getUnreadCount(SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "标记单条消息已读")
    @PutMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id) {
        remoteMessageService.markRead(id, SecurityUtils.getCurrentUserId());
        return R.ok(null);
    }

    @Operation(summary = "标记所有消息已读")
    @PutMapping("/read-all")
    public R<Void> markAllRead() {
        remoteMessageService.markAllRead(SecurityUtils.getCurrentUserId());
        return R.ok(null);
    }
}
