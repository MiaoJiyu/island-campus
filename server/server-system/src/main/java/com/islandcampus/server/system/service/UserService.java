package com.islandcampus.server.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.system.dto.*;
import com.islandcampus.server.system.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {

    /**
     * 登录验证
     */
    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);

    /**
     * 刷新令牌
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 修改密码
     */
    void changePassword(PasswordChangeDTO dto);

    /**
     * 重置用户密码
     */
    void resetPassword(Long userId);

    /**
     * 批量导入用户
     */
    int batchImport(MultipartFile file, Long orgId, Long roleId);

    /**
     * 检查是否需要首次登录修改密码
     */
    boolean checkMustChangePassword(Long userId);

    /**
     * 分页查询用户
     */
    IPage<User> pageQuery(PageQuery query);

    /**
     * 创建用户
     */
    User createUser(UserCreateDTO dto);

    /**
     * 更新用户信息
     */
    void updateUser(Long id, UserUpdateDTO dto);
}
