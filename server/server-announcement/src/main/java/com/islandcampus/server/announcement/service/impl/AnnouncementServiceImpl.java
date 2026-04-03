package com.islandcampus.server.announcement.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.common.utils.SecurityUtils;
import com.islandcampus.server.common.websocket.WebSocketService;
import com.islandcampus.server.announcement.dto.AnnouncementCreateDTO;
import com.islandcampus.server.announcement.dto.ConfirmDTO;
import com.islandcampus.server.announcement.dto.EmergencyBroadcastDTO;
import com.islandcampus.server.announcement.mapper.AnnouncementMapper;
import com.islandcampus.server.announcement.mapper.entity.Announcement;
import com.islandcampus.server.announcement.mapper.entity.BroadcastConfirm;
import com.islandcampus.server.announcement.service.AnnouncementService;
import com.islandcampus.server.announcement.service.BroadcastConfirmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    private final BroadcastConfirmService broadcastConfirmService;
    private final WebSocketService webSocketService;

    @Override
    public Announcement create(AnnouncementCreateDTO dto) {
        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setType(dto.getType() != null ? dto.getType() : 1);
        announcement.setPublisherId(SecurityUtils.getCurrentUserId());
        announcement.setTargetScope(JSON.toJSONString(dto.getTargetScope() != null ? dto.getTargetScope() : List.of()));
        announcement.setNeedConfirm(dto.getNeedConfirm() != null ? dto.getNeedConfirm() : 0);
        announcement.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        announcement.setPublishTime(dto.getPublishTime());
        announcement.setExpireTime(dto.getExpireTime());

        // 如果没有设置发布时间，默认立即发布
        if (dto.getPublishTime() == null) {
            announcement.setStatus(1); // 已发布
            announcement.setPublishTime(LocalDateTime.now());
        } else {
            announcement.setStatus(0); // 草稿(定时发布)
        }

        this.save(announcement);

        // 如果是立即发布的，推送通知
        if (announcement.getStatus() == 1) {
            pushAnnouncement(announcement);
        }

        return announcement;
    }

    @Override
    public void update(Long id, AnnouncementCreateDTO dto) {
        Announcement announcement = this.getById(id);
        if (announcement == null || announcement.getDeleted() == 1) {
            throw new BusinessException("公告不存在");
        }
        if (announcement.getStatus() == 2) {
            throw new BusinessException("已撤回的公告不可修改");
        }
        // 只有草稿状态可以修改关键信息
        if (dto.getTitle() != null) announcement.setTitle(dto.getTitle());
        if (dto.getContent() != null) announcement.setContent(dto.getContent());
        if (dto.getType() != null) announcement.setType(dto.getType());
        if (dto.getTargetScope() != null) announcement.setTargetScope(JSON.toJSONString(dto.getTargetScope()));
        if (dto.getNeedConfirm() != null) announcement.setNeedConfirm(dto.getNeedConfirm());
        if (dto.getPriority() != null) announcement.setPriority(dto.getPriority());
        if (dto.getPublishTime() != null) announcement.setPublishTime(dto.getPublishTime());
        if (dto.getExpireTime() != null) announcement.setExpireTime(dto.getExpireTime());

        this.updateById(announcement);
    }

    @Override
    public void delete(Long id) {
        Announcement announcement = this.getById(id);
        if (announcement == null || announcement.getDeleted() == 1) {
            throw new BusinessException("公告不存在");
        }
        this.removeById(id);
    }

    @Override
    public PageResult<Announcement> getList(Integer type, Integer status, String keyword, Long current, Long size) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Announcement::getCreatedAt);

        if (type != null) wrapper.eq(Announcement::getType, type);
        if (status != null) wrapper.eq(Announcement::getStatus, status);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Announcement::getTitle, keyword).or().like(Announcement::getContent, keyword));
        }

        Page<Announcement> page = new Page<>(current != null ? current : 1,
                size != null ? size : 20);
        Page<Announcement> result = this.page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public Announcement getDetail(Long id) {
        return this.getById(id);
    }

    @Override
    public void publish(Long id) {
        Announcement announcement = this.getById(id);
        if (announcement == null || announcement.getDeleted() == 1) {
            throw new BusinessException("公告不存在");
        }
        if (announcement.getStatus() == 1) {
            throw new BusinessException("该公告已经发布");
        }
        if (announcement.getStatus() == 2) {
            throw new BusinessException("已撤回的公告不可重新发布");
        }

        announcement.setStatus(1); // 已发布
        if (announcement.getPublishTime() == null) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        this.updateById(announcement);

        pushAnnouncement(announcement);

        log.info("发布公告: id={}, title={}", id, announcement.getTitle());
    }

    @Override
    public void revoke(Long id) {
        Announcement announcement = this.getById(id);
        if (announcement == null || announcement.getDeleted() == 1) {
            throw new BusinessException("公告不存在");
        }
        if (announcement.getStatus() != 1) {
            throw new BusinessException("只有已发布状态的公告可以撤回");
        }

        announcement.setStatus(2); // 撤回
        this.updateById(announcement);

        log.info("撤回公告: id={}, title={}", id, announcement.getTitle());
    }

    @Override
    public void emergencyBroadcast(EmergencyBroadcastDTO dto) {
        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setType(2); // 紧急
        announcement.setPublisherId(SecurityUtils.getCurrentUserId());
        announcement.setTargetScope(JSON.toJSONString(dto.getTargetScope() != null ? dto.getTargetScope() : List.of()));
        announcement.setNeedConfirm(0);
        announcement.setPriority(2); // 紧急优先级
        announcement.setPublishTime(LocalDateTime.now());
        announcement.setStatus(1); // 直接发布

        this.save(announcement);

        // 通过WebSocket紧急广播推送
        List<Long> targetScope = dto.getTargetScope();
        for (Long orgId : targetScope) {
            webSocketService.sendEmergencyBroadcast(orgId,
                    Map.of(
                            "id", announcement.getId(),
                            "title", dto.getTitle(),
                            "content", dto.getContent(),
                            "priority", 2,
                            "publishTime", LocalDateTime.now().toString()
                    ));
        }

        log.info("紧急广播: title={}, targets={}", dto.getTitle(), targetScope);
    }

    @Override
    public BroadcastConfirm confirmBroadcast(ConfirmDTO dto, Long computerId, Long userId, String ipAddress) {
        return broadcastConfirmService.confirm(dto, computerId, userId, ipAddress);
    }

    @Override
    public List<BroadcastConfirm> getConfirmedList(Long announcementId, Long current, Long size) {
        return broadcastConfirmService.getConfirmedList(announcementId, current, size);
    }

    @Override
    public List<BroadcastConfirm> getUnconfirmedList(Long announcementId, Long current, Long size) {
        return broadcastConfirmService.getUnconfirmedList(announcementId, current, size);
    }

    @Override
    public List<Announcement> getActiveAnnouncements(Long orgId) {
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)
                .le(Announcement::getPublishTime, now)
                .and(w -> w.isNull(Announcement::getExpireTime).or().ge(Announcement::getExpireTime, now))
                .orderByDesc(Announcement::getPriority)
                .orderByDesc(Announcement::getPublishTime);

        return this.list(wrapper);
    }

    @Override
    public void checkScheduledPublish() {
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 0)
                .le(Announcement::getPublishTime, now)
                .isNotNull(Announcement::getPublishTime);

        List<Announcement> toPublish = this.list(wrapper);
        for (Announcement announcement : toPublish) {
            try {
                publish(announcement.getId());
                log.info("定时发布公告: id={}, title={}", announcement.getId(), announcement.getTitle());
            } catch (Exception e) {
                log.error("定时发布公告失败: id={}", announcement.getId(), e);
            }
        }
    }

    /**
     * 推送公告通知
     */
    private void pushAnnouncement(Announcement announcement) {
        // 普通公告 → 信息栏跑马灯
        if (announcement.getType() == 1) {
            webSocketService.broadcast("/topic/announcement/marquee",
                    Map.of(
                            "type", "new_announcement",
                            "id", announcement.getId(),
                            "title", announcement.getTitle(),
                            "priority", announcement.getPriority()
                    ));
        }
        // 紧急广播 → 强制弹窗
        else if (announcement.getType() == 2) {
            List<Long> targetOrgIds = JSON.parseArray(announcement.getTargetScope(), Long.class);
            for (Long orgId : targetOrgIds) {
                webSocketService.sendEmergencyBroadcast(orgId,
                        Map.of(
                                "id", announcement.getId(),
                                "title", announcement.getTitle(),
                                "content", announcement.getContent(),
                                "priority", announcement.getPriority()
                        ));
            }
        }
    }
}
