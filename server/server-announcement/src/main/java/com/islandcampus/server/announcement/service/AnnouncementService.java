package com.islandcampus.server.announcement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.announcement.dto.AnnouncementCreateDTO;
import com.islandcampus.server.announcement.dto.ConfirmDTO;
import com.islandcampus.server.announcement.dto.EmergencyBroadcastDTO;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.announcement.mapper.entity.Announcement;
import com.islandcampus.server.announcement.mapper.entity.BroadcastConfirm;

import java.util.List;

public interface AnnouncementService extends IService<Announcement> {
    Announcement create(AnnouncementCreateDTO dto);
    void update(Long id, AnnouncementCreateDTO dto);
    void delete(Long id);
    PageResult<Announcement> getList(Integer type, Integer status, String keyword, Long current, Long size);
    Announcement getDetail(Long id);

    void publish(Long id);
    void revoke(Long id);
    void emergencyBroadcast(EmergencyBroadcastDTO dto);

    BroadcastConfirm confirmBroadcast(ConfirmDTO dto, Long computerId, Long userId, String ipAddress);
    List<BroadcastConfirm> getConfirmedList(Long announcementId, Long current, Long size);
    List<BroadcastConfirm> getUnconfirmedList(Long announcementId, Long current, Long size);

    List<Announcement> getActiveAnnouncements(Long orgId);

    void checkScheduledPublish();
}
