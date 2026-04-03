package com.islandcampus.server.message.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.common.utils.SecurityUtils;
import com.islandcampus.server.common.websocket.WebSocketService;
import com.islandcampus.server.message.dto.MessageVO;
import com.islandcampus.server.message.dto.SendMessageDTO;
import com.islandcampus.server.message.entity.RemoteMessage;
import com.islandcampus.server.message.mapper.RemoteMessageMapper;
import com.islandcampus.server.message.service.RemoteMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteMessageServiceImpl extends ServiceImpl<RemoteMessageMapper, RemoteMessage>
        implements RemoteMessageService {
    private final WebSocketService webSocketService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void send(SendMessageDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) throw new BusinessException("用户未登录");

        Map<String, Object> scopeMap = new HashMap<>();
        scopeMap.put("targetType", dto.getTargetType());
        scopeMap.put("targetIds", dto.getTargetIds());

        RemoteMessage message = new RemoteMessage();
        message.setSenderId(currentUserId);
        message.setTitle(dto.getTitle());
        message.setContent(dto.getContent());
        message.setTargetScope(JSON.toJSONString(scopeMap));
        message.setMessageType(dto.getMessageType() != null ? dto.getMessageType() : 1);
        message.setReadBy("[]");
        message.setSentAt(LocalDateTime.now());
        this.save(message);

        log.info("远程消息发送成功: messageId={}, senderId={}", message.getId(), currentUserId);
        pushMessageToTargets(message, dto.getTargetIds());
    }

    private void pushMessageToTargets(RemoteMessage message, List<Long> targetIds) {
        Map<String, Object> msgData = new HashMap<>();
        msgData.put("id", message.getId());
        msgData.put("title", message.getTitle());
        msgData.put("content", message.getContent());
        msgData.put("type", message.getMessageType());
        for (Long userId : targetIds) {
            try { webSocketService.sendRemoteMessage(userId, msgData); }
            catch (Exception e) { log.warn("推送消息给用户失败: userId={}", userId); }
        }
    }

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<RemoteMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("read_by NOT LIKE {0}", "%\"" + userId + "\"%");
        wrapper.and(w -> w.isNull(RemoteMessage::getExpireAt).or().ge(RemoteMessage::getExpireAt, LocalDateTime.now()));
        return this.count(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long messageId, Long userId) {
        RemoteMessage message = this.getById(messageId);
        if (message == null) throw new BusinessException(404, "消息不存在");
        List<Long> readByList = parseReadBy(message.getReadBy());
        if (!readByList.contains(userId)) {
            readByList.add(userId);
            message.setReadBy(JSON.toJSONString(readByList));
            this.updateById(message);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long userId) {
        LambdaQueryWrapper<RemoteMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("read_by NOT LIKE {0}", "%\"" + userId + "\"%");
        wrapper.and(w -> w.isNull(RemoteMessage::getExpireAt).or().ge(RemoteMessage::getExpireAt, LocalDateTime.now()));
        List<RemoteMessage> messages = this.list(wrapper);
        for (RemoteMessage m : messages) {
            List<Long> list = parseReadBy(m.getReadBy());
            if (!list.contains(userId)) { list.add(userId); m.setReadBy(JSON.toJSONString(list)); this.updateById(m); }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Long> parseReadBy(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try { return JSON.parseArray(json, Long.class); } catch (Exception e) { return new ArrayList<>(); }
    }

    @Override
    public PageResult<MessageVO> listMessages(Long userId, Long current, Long size) {
        Page<RemoteMessage> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<RemoteMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(RemoteMessage::getSentAt);
        wrapper.and(w -> w.isNull(RemoteMessage::getExpireAt).or().ge(RemoteMessage::getExpireAt, LocalDateTime.now()));
        Page<RemoteMessage> rp = this.page(page, wrapper);
        List<MessageVO> voList = rp.getRecords().stream()
                .map(msg -> MessageVO.builder().id(msg.getId()).title(msg.getTitle()).content(msg.getContent())
                        .senderName("用户" + msg.getSenderId()).sentAt(msg.getSentAt())
                        .read(parseReadBy(msg.getReadBy()).contains(userId)).type(msg.getMessageType()).build())
                .collect(Collectors.toList());
        return PageResult.of(voList, rp.getTotal(), rp.getCurrent(), rp.getSize());
    }

    @Override
    public void autoExpire() {
        // 定时清理过期消息
    }
}
