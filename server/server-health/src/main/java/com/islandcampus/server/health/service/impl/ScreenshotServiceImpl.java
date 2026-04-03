package com.islandcampus.server.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.health.entity.Screenshot;
import com.islandcampus.server.health.mapper.ScreenshotMapper;
import com.islandcampus.server.health.service.ScreenshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreenshotServiceImpl extends ServiceImpl<ScreenshotMapper, Screenshot> implements ScreenshotService {
    private static final List<String> PRIVACY_KEYWORDS = Arrays.asList(
            "chrome", "firefox", "edge", "wechat", "qq", "dingtalk", "微信", "QQ", "钉钉", "浏览器", "聊天");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Screenshot saveScreenshot(Long computerId, String imageUrl, Integer w, Integer h) {
        Screenshot s = new Screenshot(); s.setComputerId(computerId); s.setImageUrl(imageUrl);
        s.setWidth(w); s.setHeight(h); s.setCapturedAt(LocalDateTime.now()); s.setBlurred(0);
        this.save(s); return s;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Screenshot uploadScreenshot(Long computerId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("文件不能为空");
        String url = "/uploads/screenshots/" + computerId + "_" + System.currentTimeMillis() + ".png";
        Screenshot s = new Screenshot(); s.setComputerId(computerId); s.setImageUrl(url);
        s.setImageSizeKb(file.getSize() / 1024); s.setCapturedAt(LocalDateTime.now()); s.setBlurred(0);
        this.save(s); return s;
    }

    @Override
    public List<Screenshot> getRecentScreenshots(Long classId, int limit) {
        if (limit <= 0) limit = 4;
        return baseMapper.selectRecentByClassId(classId, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoCleanup() {
        LambdaQueryWrapper<Screenshot> w = new LambdaQueryWrapper<>();
        w.lt(Screenshot::getCreatedAt, LocalDateTime.now().minusDays(30));
        this.remove(w);
        log.info("截图自动清理完成");
    }

    @Override
    public boolean detectPrivacyRisk(String info) {
        if (info == null || info.isBlank()) return false;
        String lower = info.toLowerCase();
        for (String kw : PRIVACY_KEYWORDS) { if (lower.contains(kw.toLowerCase())) return true; }
        return false;
    }
}
