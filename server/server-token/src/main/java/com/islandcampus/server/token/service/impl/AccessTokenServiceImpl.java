package com.islandcampus.server.token.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.common.utils.SecurityUtils;
import com.islandcampus.server.token.dto.*;
import com.islandcampus.server.token.mapper.AccessTokenMapper;
import com.islandcampus.server.token.mapper.entity.AccessToken;
import com.islandcampus.server.token.service.AccessTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl extends ServiceImpl<AccessTokenMapper, AccessToken> implements AccessTokenService {

    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TOKEN_CODE_LENGTH = 64;

    @Override
    public AccessToken createToken(TokenCreateDTO dto) {
        if (dto.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("过期时间不能早于当前时间");
        }

        AccessToken accessToken = new AccessToken();
        accessToken.setTokenCode(generateTokenCode());
        accessToken.setName(dto.getName());
        accessToken.setClassId(dto.getClassId());
        accessToken.setCreatorId(SecurityUtils.getCurrentUserId());
        accessToken.setExpireTime(dto.getExpireTime());
        accessToken.setMaxUseCount(dto.getMaxUseCount());
        accessToken.setUsedCount(0);
        if (dto.getPermissions() != null && !dto.getPermissions().isEmpty()) {
            accessToken.setPermissions(JSON.toJSONString(dto.getPermissions()));
        }
        accessToken.setStatus(1); // 有效

        this.save(accessToken);
        log.info("创建令牌成功: name={}, code={}", dto.getName(), maskToken(accessToken.getTokenCode()));

        return accessToken;
    }

    @Override
    public void revokeToken(Long id) {
        AccessToken token = this.getById(id);
        if (token == null || token.getDeleted() == 1) {
            throw new BusinessException("令牌不存在");
        }

        token.setStatus(0);
        token.setRevokedAt(LocalDateTime.now());
        this.updateById(token);
        log.info("令牌已撤销: id={}", id);
    }

    @Override
    public boolean validateToken(String tokenCode) {
        if (StrUtil.isBlank(tokenCode)) {
            return false;
        }

        LambdaQueryWrapper<AccessToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccessToken::getTokenCode, tokenCode)
                .eq(AccessToken::getStatus, 1);

        AccessToken token = this.getOne(wrapper, false);
        if (token == null || token.getDeleted() == 1) {
            return false;
        }

        // 检查是否过期
        if (token.getExpireTime().isBefore(LocalDateTime.now())) {
            // 自动标记过期
            token.setStatus(0);
            this.updateById(token);
            log.warn("令牌已过期: tokenCode={}", maskToken(tokenCode));
            return false;
        }

        // 检查使用次数限制
        if (token.getMaxUseCount() != null &&
                token.getUsedCount() != null &&
                token.getUsedCount() >= token.getMaxUseCount()) {
            token.setStatus(0);
            this.updateById(token);
            log.warn("令牌已达使用上限: tokenCode={}, used={}/{}",
                    maskToken(tokenCode), token.getUsedCount(), token.getMaxUseCount());
            return false;
        }

        return true;
    }

    @Override
    public AccessToken getTokenByCode(String tokenCode) {
        LambdaQueryWrapper<AccessToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccessToken::getTokenCode, tokenCode);
        return this.getOne(wrapper, false);
    }

    @Override
    public void recordAccess(String tokenCode, String ip) {
        LambdaQueryWrapper<AccessToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccessToken::getTokenCode, tokenCode);
        AccessToken token = this.getOne(wrapper, false);
        if (token != null) {
            token.setUsedCount((token.getUsedCount() == null ? 0 : token.getUsedCount()) + 1);
            this.updateById(token);
            log.debug("令牌访问记录: tokenCode={}, ip={}, usedCount={}",
                    maskToken(tokenCode), ip, token.getUsedCount());
        }
    }

    @Override
    public PageResult<TokenVO> listTokens(Long classId, Long current, Long size) {
        LambdaQueryWrapper<AccessToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AccessToken::getCreatedAt);

        if (classId != null) {
            wrapper.eq(AccessToken::getClassId, classId);
        }

        Page<AccessToken> page = new Page<>(current != null ? current : 1,
                size != null ? size : 20);
        Page<AccessToken> result = this.page(page, wrapper);

        List<TokenVO> voList = new ArrayList<>();
        for (AccessToken token : result.getRecords()) {
            voList.add(toVO(token));
        }

        return PageResult.of(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public PublicInfoVO getPublicInfo(String tokenCode) {
        AccessToken token = getTokenByCode(tokenCode);
        if (token == null || token.getDeleted() == 1) {
            throw new BusinessException("令牌无效或不存在");
        }
        if (!validateToken(tokenCode)) {
            throw new BusinessException("令牌已过期或失效");
        }

        // 记录一次访问
        recordAccess(tokenCode, null);

        PublicInfoVO vo = new PublicInfoVO();
        vo.setClassName(getClassDisplayName(token.getClassId()));
        vo.setCourseList(List.of()); // TODO: 通过课程服务获取
        vo.setAnnouncements(List.of()); // TODO: 通过公告服务获取
        vo.setHealthOverview(Map.of("online", 0, "offline", 0)); // TODO: 通过健康服务获取
        vo.setPublishedAnswers(List.of()); // TODO: 通过答案服务获取

        return vo;
    }

    @Override
    public void autoCleanup() {
        LocalDateTime now = LocalDateTime.now();

        // 查找所有有效令牌并检查过期情况
        List<AccessToken> allTokens = this.list(new LambdaQueryWrapper<AccessToken>().eq(AccessToken::getStatus, 1));
        int cleanedCount = 0;

        for (AccessToken token : allTokens) {
            boolean shouldExpire = false;

            // 过期检查
            if (token.getExpireTime() != null && token.getExpireTime().isBefore(now)) {
                shouldExpire = true;
            }

            // 使用次数超限检查
            if (!shouldExpire && token.getMaxUseCount() != null &&
                    token.getUsedCount() != null && token.getUsedCount() >= token.getMaxUseCount()) {
                shouldExpire = true;
            }

            if (shouldExpire) {
                token.setStatus(0);
                this.updateById(token);
                cleanedCount++;
            }
        }

        if (cleanedCount > 0) {
            log.info("定时清理完成，标记{}个过期令牌", cleanedCount);
        }
    }

    private TokenVO toVO(AccessToken token) {
        TokenVO vo = new TokenVO();
        vo.setId(token.getId());
        vo.setTokenCode(maskToken(token.getTokenCode()));
        vo.setName(token.getName());
        vo.setClassName(getClassDisplayName(token.getClassId()));
        vo.setExpireTime(token.getExpireTime());
        vo.setUsedCount(token.getUsedCount());
        vo.setMaxUseCount(token.getMaxUseCount());
        vo.setStatus(token.getStatus());
        vo.setPermissions(token.getPermissions());
        return vo;
    }

    private String generateTokenCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(TOKEN_CODE_LENGTH);
        for (int i = 0; i < TOKEN_CODE_LENGTH; i++) {
            sb.append(TOKEN_CHARS.charAt(random.nextInt(TOKEN_CHARS.length())));
        }
        return sb.toString();
    }

    private String maskToken(String tokenCode) {
        if (StrUtil.isBlank(tokenCode) || tokenCode.length() <= 12) {
            return "****";
        }
        return tokenCode.substring(0, 6) + "****" + tokenCode.substring(tokenCode.length() - 6);
    }

    private String getClassDisplayName(Long classId) {
        if (classId == null) {
            return "未分配";
        }
        return "班级" + classId;
    }
}
