package com.islandcampus.server.announcement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.islandcampus.server.announcement.dto.ConfirmDTO;
import com.islandcampus.server.announcement.mapper.BroadcastConfirmMapper;
import com.islandcampus.server.announcement.mapper.entity.Announcement;
import com.islandcampus.server.announcement.mapper.entity.BroadcastConfirm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastConfirmService {

    private final BroadcastConfirmMapper broadcastConfirmMapper;

    public BroadcastConfirm confirm(ConfirmDTO dto, Long computerId, Long userId, String ipAddress) {
        // 先检查是否已确认过
        LambdaQueryWrapper<BroadcastConfirm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BroadcastConfirm::getAnnouncementId, dto.getAnnouncementId())
                .eq(computerId != null ? BroadcastConfirm::getComputerId : (s) -> true,
                        computerId != null ? computerId : 0L)
                .eq(userId != null ? BroadcastConfirm::getUserId : (s) -> true,
                        userId != null ? userId : 0L);
        if (computerId == null && userId != null) {
            wrapper.eq(BroadcastConfirm::getUserId, userId);
        } else if (userId == null && computerId != null) {
            wrapper.eq(BroadcastConfirm::getComputerId, computerId);
        }
        BroadcastConfirm existing = broadcastConfirmMapper.selectOne(wrapper.last("LIMIT 1"));

        if (existing != null) {
            // 更新已有记录
            existing.setConfirmedAt(LocalDateTime.now());
            existing.setIpAddress(ipAddress);
            broadcastConfirmMapper.updateById(existing);
            return existing;
        }

        // 新建确认记录
        BroadcastConfirm confirm = new BroadcastConfirm();
        confirm.setAnnouncementId(dto.getAnnouncementId());
        confirm.setComputerId(computerId);
        confirm.setUserId(userId);
        confirm.setConfirmedAt(LocalDateTime.now());
        confirm.setIpAddress(ipAddress);
        broadcastConfirmMapper.insert(confirm);
        log.info("公告确认回执: announcementId={}, computerId={}, userId={}", dto.getAnnouncementId(), computerId, userId);
        return confirm;
    }

    public List<BroadcastConfirm> getConfirmedList(Long announcementId, Long current, Long size) {
        LambdaQueryWrapper<BroadcastConfirm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BroadcastConfirm::getAnnouncementId, announcementId)
                .orderByDesc(BroadcastConfirm::getConfirmedAt);
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current != null ? current : 1,
                size != null ? size : 20);
        return broadcastConfirmMapper.selectPage(page, wrapper).getRecords();
    }

    public List<BroadcastConfirm> getUnconfirmedList(Long announcementId, Long current, Long size) {
        LambdaQueryWrapper<BroadcastConfirm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BroadcastConfirm::getAnnouncementId, announcementId)
                .orderByAsc(BroadcastConfirm::getId);
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current != null ? current : 1,
                size != null ? size : 20);
        return broadcastConfirmMapper.selectPage(page, wrapper).getRecords();
    }
}
