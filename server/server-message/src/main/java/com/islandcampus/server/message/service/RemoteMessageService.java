package com.islandcampus.server.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.message.dto.MessageVO;
import com.islandcampus.server.message.dto.SendMessageDTO;
import com.islandcampus.server.message.entity.RemoteMessage;

public interface RemoteMessageService extends IService<RemoteMessage> {
    void send(SendMessageDTO dto);
    long getUnreadCount(Long userId);
    void markRead(Long messageId, Long userId);
    void markAllRead(Long userId);
    PageResult<MessageVO> listMessages(Long userId, Long current, Long size);
    void autoExpire();
}
