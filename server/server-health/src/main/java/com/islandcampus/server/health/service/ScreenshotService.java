package com.islandcampus.server.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.health.entity.Screenshot;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ScreenshotService extends IService<Screenshot> {
    Screenshot saveScreenshot(Long computerId, String imageUrl, Integer width, Integer height);
    Screenshot uploadScreenshot(Long computerId, MultipartFile file);
    List<Screenshot> getRecentScreenshots(Long classId, int limit);
    void autoCleanup();
    boolean detectPrivacyRisk(String processInfo);
}
