package com.islandcampus.server.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public record UserInfo(Long userId, String username, Long roleId, Long orgId) {}

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserInfo info) {
            return info.userId();
        }
        return null;
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserInfo info) {
            return info.username();
        }
        return null;
    }

    public static Long getCurrentRoleId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserInfo info) {
            return info.roleId();
        }
        return null;
    }

    public static Long getCurrentOrgId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserInfo info) {
            return info.orgId();
        }
        return null;
    }
}
