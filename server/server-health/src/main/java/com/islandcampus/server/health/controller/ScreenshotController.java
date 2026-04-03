package com.islandcampus.server.health.controller;

import com.islandcampus.server.common.base.R;
import com.islandcampus.server.health.entity.Screenshot;
import com.islandcampus.server.health.service.ScreenshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "截图管理")
@RestController
@RequestMapping("/api/v1/screenshot")
@RequiredArgsConstructor
public class ScreenshotController {
    private final ScreenshotService screenshotService;

    @Operation(summary = "上传截图")
    @PostMapping("/upload")
    public R<Screenshot> upload(@RequestParam("file") MultipartFile file,
                                @RequestParam Long computerId,
                                @RequestParam(defaultValue = "false") Boolean blurred) {
        Screenshot s = screenshotService.uploadScreenshot(computerId, file);
        if (Boolean.TRUE.equals(blurred)) { s.setBlurred(1); screenshotService.updateById(s); }
        return R.ok("上传成功", s);
    }

    @Operation(summary = "最近截图列表(令牌用)")
    @GetMapping("/list")
    public R<List<Screenshot>> list(@RequestParam Long classId, @RequestParam(defaultValue = "4") Integer limit) {
        return R.ok(screenshotService.getRecentScreenshots(classId, limit));
    }

    @Operation(summary = "获取单张截图信息")
    @GetMapping("/{id}")
    public R<Screenshot> get(@PathVariable Long id) {
        Screenshot s = screenshotService.getById(id);
        return s != null ? R.ok(s) : R.fail(404, "截图不存在");
    }
}
