package com.islandcampus.server.token.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.token.dto.*;
import com.islandcampus.server.token.mapper.entity.AccessToken;

public interface AccessTokenService extends IService<AccessToken> {

    /**
     * 创建令牌
     */
    AccessToken createToken(TokenCreateDTO dto);

    /**
     * 撤销令牌
     */
    void revokeToken(Long id);

    /**
     * 验证令牌有效性（公开接口）
     */
    boolean validateToken(String tokenCode);

    /**
     * 根据令牌码获取令牌信息
     */
    AccessToken getTokenByCode(String tokenCode);

    /**
     * 记录访问并递增使用计数
     */
    void recordAccess(String tokenCode, String ip);

    /**
     * 令牌列表
     */
    PageResult<TokenVO> listTokens(Long classId, Long current, Long size);

    /**
     * 获取公开信息（无需认证）
     */
    PublicInfoVO getPublicInfo(String tokenCode);

    /**
     * 定时清理过期/撤销令牌
     */
    void autoCleanup();
}
