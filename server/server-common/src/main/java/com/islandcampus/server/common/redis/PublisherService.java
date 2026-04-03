package com.islandcampus.server.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherService {
    private final StringRedisTemplate redisTemplate;

    /**
     * 发布消息到指定频道
     */
    public void publish(String channel, String message) {
        try {
            redisTemplate.convertAndSend(channel, message);
            log.debug("Redis发布: channel={}, msg={}", channel, message);
        } catch (Exception e) {
            log.error("Redis发布失败: channel={}", channel, e);
        }
    }

    /**
     * 灵动岛配置更新通知
     */
    public void notifyIslandConfigUpdate(Long computerId) {
        publish("island:config_update:" + computerId, "CONFIG_UPDATE");
    }

    /**
     * 广播频道
     */
    public void broadcast(String message) {
        publish("island:broadcast", message);
    }
}
