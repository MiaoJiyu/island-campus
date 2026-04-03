package com.islandcampus.server.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.common.security.JwtTokenProvider;
import com.islandcampus.server.common.utils.SecurityUtils;
import com.islandcampus.server.system.dto.*;
import com.islandcampus.server.system.entity.LoginLog;
import com.islandcampus.server.system.entity.Role;
import com.islandcampus.server.system.entity.SysOrganization;
import com.islandcampus.server.system.entity.User;
import com.islandcampus.server.system.mapper.LoginLogMapper;
import com.islandcampus.server.system.mapper.RoleMapper;
import com.islandcampus.server.system.mapper.SysOrganizationMapper;
import com.islandcampus.server.system.mapper.UserMapper;
import com.islandcampus.server.system.service.LoginLogService;
import com.islandcampus.server.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginLogService loginLogService;
    private final RoleMapper roleMapper;
    private final SysOrganizationMapper orgMapper;

    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final long LOCK_DURATION_MINUTES = 30;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (user == null) {
            loginLogService.recordLogin(null, request.getUsername(), ipAddress,
                    userAgent, false, "用户不存在");
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            loginLogService.recordLogin(user.getId(), request.getUsername(),
                    ipAddress, userAgent, false, "账号已禁用");
            throw new BusinessException(401, "账号已被禁用");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            loginLogService.recordLogin(user.getId(), request.getUsername(),
                    ipAddress, userAgent, false, "账号已锁定");
            throw new BusinessException(401, "账号已锁定，请" + LOCK_DURATION_MINUTES + "分钟后重试");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleLoginFailure(user, request.getUsername(), ipAddress, userAgent);
            throw new BusinessException(401, "用户名或密码错误");
        }

        handleLoginSuccess(user, ipAddress, userAgent);

        Role role = roleMapper.selectById(user.getRoleId());
        SysOrganization org = orgMapper.selectById(user.getOrgId());

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(), user.getUsername(), user.getRealName(),
                user.getRoleId(), user.getOrgId(),
                role != null ? role.getName() : null,
                org != null ? org.getName() : null
        );

        String token = jwtTokenProvider.generateAccessToken(user.getId(),
                user.getUsername(), user.getRoleId(), user.getOrgId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return new LoginResponse(token, refreshToken, userInfo);
    }

    private void handleLoginFailure(User user, String username, String ip, String agent) {
        int failCount = user.getLoginFailCount() == null ? 0 : user.getLoginFailCount() + 1;
        user.setLoginFailCount(failCount);

        if (failCount >= MAX_LOGIN_FAIL_COUNT) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }

        updateById(user);
        loginLogService.recordLogin(user.getId(), username, ip, agent, false,
                failCount >= MAX_LOGIN_FAIL_COUNT ? "登录失败次数过多，已锁定" : "密码错误");
    }

    private void handleLoginSuccess(User user, String ip, String agent) {
        user.setLoginFailCount(0);
        user.setLockedUntil(null);
        user.setLoginCount((user.getLoginCount() == null ? 0 : user.getLoginCount()) + 1);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        updateById(user);
        loginLogService.recordLogin(user.getId(), user.getUsername(), ip, agent, true, null);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new BusinessException(401, "刷新令牌无效或已过期");
        }
        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
        User user = getById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new BusinessException(401, "令牌无效或用户已禁用");
        }

        Role role = roleMapper.selectById(user.getRoleId());
        SysOrganization org = orgMapper.selectById(user.getOrgId());
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(), user.getUsername(), user.getRealName(),
                user.getRoleId(), user.getOrgId(),
                role != null ? role.getName() : null,
                org != null ? org.getName() : null
        );

        String token = jwtTokenProvider.generateAccessToken(user.getId(),
                user.getUsername(), user.getRoleId(), user.getOrgId());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return new LoginResponse(token, newRefreshToken, userInfo);
    }

    @Override
    public void changePassword(PasswordChangeDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = getById(currentUserId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("旧密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setMustChangePwd(0);
        updateById(user);
        System.out.println("[AUDIT] 用户修改密码: userId=" + currentUsername);
    }

    @Override
    public void resetPassword(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String defaultPassword = "Abc@123456";
        user.setPasswordHash(passwordEncoder.encode(defaultPassword));
        user.setMustChangePwd(1);
        updateById(user);
        System.out.println("[AUDIT] 重置用户密码: userId=" + userId + ", operator="
                + SecurityUtils.getCurrentUsername());
    }

    @Override
    @Transactional
    public int batchImport(MultipartFile file, Long orgId, Long roleId) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int successCount = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Cell usernameCell = row.getCell(0);
                Cell realNameCell = row.getCell(1);
                Cell phoneCell = row.getCell(2);
                if (usernameCell == null) continue;

                String username = getCellValue(usernameCell);
                if (username == null || username.isBlank()) continue;

                long count = count(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
                if (count > 0) continue;

                User newUser = new User();
                newUser.setUsername(username.trim());
                newUser.setRealName(realNameCell != null ? getCellValue(realNameCell) : null);
                newUser.setPhone(phoneCell != null ? getCellValue(phoneCell) : null);
                newUser.setSalt(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
                newUser.setPasswordHash(passwordEncoder.encode("Abc@123456"));
                newUser.setMustChangePwd(1);
                newUser.setOrgId(orgId);
                newUser.setRoleId(roleId);
                newUser.setStatus(1);
                save(newUser);
                successCount++;
            }
            System.out.println("[AUDIT] 批量导入用户: successCount=" + successCount
                    + ", operator=" + SecurityUtils.getCurrentUsername());
            return successCount;
        } catch (IOException e) {
            throw new BusinessException("文件解析失败: " + e.getMessage());
        }
    }

    @Override
    public boolean checkMustChangePassword(Long userId) {
        User user = getById(userId);
        return user != null && user.getMustChangePwd() != null && user.getMustChangePwd() == 1;
    }

    @Override
    public IPage<User> pageQuery(PageQuery query) {
        Page<User> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(User::getUsername, query.getKeyword())
                    .or().like(User::getRealName, query.getKeyword())
                    .or().like(User::getPhone, query.getKeyword()));
        }
        wrapper.orderByDesc(User::getCreatedAt);
        return page(page, wrapper);
    }

    @Override
    public User createUser(UserCreateDTO dto) {
        long exists = count(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (exists > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setSalt(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        if (user.getMustChangePwd() == null) {
            user.setMustChangePwd(0);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        save(user);
        System.out.println("[AUDIT] 创建用户: username=" + dto.getUsername()
                + ", operator=" + SecurityUtils.getCurrentUsername());
        return user;
    }

    @Override
    public void updateUser(Long id, UserUpdateDTO dto) {
        User existing = getById(id);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        BeanUtils.copyProperties(dto, existing, "id", "username", "passwordHash", "salt");
        updateById(existing);
        System.out.println("[AUDIT] 更新用户: userId=" + id + ", operator="
                + SecurityUtils.getCurrentUsername());
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }
}
