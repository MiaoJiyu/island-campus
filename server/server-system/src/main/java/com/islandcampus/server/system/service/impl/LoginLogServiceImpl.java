package com.islandcampus.server.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.system.dto.PageQuery;
import com.islandcampus.server.system.entity.LoginLog;
import com.islandcampus.server.system.mapper.LoginLogMapper;
import com.islandcampus.server.system.service.LoginLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog>
        implements LoginLogService {

    @Override
    public void recordLogin(Long userId, String username, String ipAddress,
                            String userAgent, boolean success, String failReason) {
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setLoginTime(LocalDateTime.now());
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setSuccess(success ? 1 : 0);
        log.setFailReason(failReason);
        save(log);
    }

    @Override
    public IPage<LoginLog> pageQuery(PageQuery query) {
        Page<LoginLog> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(LoginLog::getUsername, query.getKeyword());
        }
        wrapper.orderByDesc(LoginLog::getLoginTime);
        return page(page, wrapper);
    }
}
