package com.islandcampus.server.common.websocket;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 广播消息到所有订阅者
     */
    public void broadcast(String destination, Object message) {
        try {
            messagingTemplate.convertAndSend("/topic" + destination, message);
            log.debug("广播消息: dest={}, msg={}", destination, JSON.toJSONString(message));
        } catch (Exception e) {
            log.error("广播失败: dest={}", destination, e);
        }
    }

    /**
     * 发送消息到指定用户
     */
    public void sendToUser(Long userId, String destination, Object message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId), "/queue" + destination, message);
            log.debug("发送用户消息: userId={}, dest={}", userId, destination);
        } catch (Exception e) {
            log.error("用户消息发送失败: userId={}", userId, e);
        }
    }

    /**
     * 发送消息到指定电脑客户端
     */
    public void sendToComputer(Long computerId, Object message) {
        broadcast("/computer/" + computerId + "/command", message);
    }

    /**
     * 灵动岛配置更新通知
     */
    public void notifyIslandConfigUpdate(Long computerId) {
        broadcast("/computer/" + computerId + "/island/config", Map.of("type", "config_update"));
    }

    /**
     * 考试模式指令
     */
    public void sendExamCommand(Long computerId, Map<String, Object> command) {
        sendToComputer(computerId, Map.of("type", "exam_command", "data", command));
    }

    /**
     * 紧急广播推送
     */
    public void sendEmergencyBroadcast(Long orgId, Map<String, Object> broadcastData) {
        broadcast("/org/" + orgId + "/emergency", Map.of("type", "emergency_broadcast", "data", broadcastData));
    }

    /**
     * 远程消息推送
     */
    public void sendRemoteMessage(Long userId, Map<String, Object> message) {
        sendToUser(userId, "/message", Map.of("type", "remote_message", "data", message));
    }
}
