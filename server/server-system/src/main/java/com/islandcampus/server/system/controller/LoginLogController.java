package com.islandcampus.server.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.base.R;
import com.islandcampus.server.system.dto.PageQuery;
import com.islandcampus.server.system.entity.LoginLog;
import com.islandcampus.server.system.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/login-logs")
@RequiredArgsConstructor
@Tag(name = "登录日志", description = "登录日志查询")
public class LoginLogController {

    private final LoginLogService loginLogService;

    @GetMapping
    @Operation(summary = "分页查询登录日志")
    public R<PageResult<LoginLog>> list(PageQuery query) {
        IPage<LoginLog> page = loginLogService.pageQuery(query);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(),
                page.getCurrent(), page.getSize()));
    }
}
