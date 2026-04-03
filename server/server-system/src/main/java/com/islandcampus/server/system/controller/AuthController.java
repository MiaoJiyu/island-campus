package com.islandcampus.server.system.controller;

import com.islandcampus.server.common.base.R;
import com.islandcampus.server.system.dto.LoginRequest;
import com.islandcampus.server.system.dto.LoginResponse;
import com.islandcampus.server.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "登录、登出、令牌管理")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                  HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        LoginResponse response = userService.login(request, ipAddress, userAgent);
        return R.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌")
    public R<LoginResponse> refresh(@RequestParam String refreshToken) {
        return R.ok(userService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    public R<Void> logout() {
        // Token黑名单或清除服务端缓存可在此处处理
        System.out.println("[AUDIT] 用户登出: operator="
                + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());
        return R.ok();
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public R<Object> info() {
        Long userId = com.islandcampus.server.common.utils.SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return com.islandcampus.server.common.base.R.fail(401, "未登录");
        }
        var user = userService.getById(userId);
        // Return user info without sensitive fields
        return R.ok(Map.of(
                "userId", user.getId(),
                "username", user.getUsername(),
                "realName", user.getRealName(),
                "roleId", user.getRoleId(),
                "orgId", user.getOrgId()
        ));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
