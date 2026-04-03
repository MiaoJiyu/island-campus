package com.islandcampus.server.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.system.dto.PageQuery;
import com.islandcampus.server.system.entity.LoginLog;

public interface LoginLogService extends IService<LoginLog> {

    void recordLogin(Long userId, String username, String ipAddress, String userAgent,
                     boolean success, String failReason);

    IPage<LoginLog> pageQuery(PageQuery query);
}
