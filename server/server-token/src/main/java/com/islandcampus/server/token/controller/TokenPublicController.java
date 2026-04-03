package com.islandcampus.server.token.controller;

import com.islandcampus.server.common.base.R;
import com.islandcampus.server.token.dto.PublicInfoVO;
import com.islandcampus.server.token.dto.TokenValidateDTO;
import com.islandcampus.server.token.service.AccessTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 公开令牌接口，无需JWT认证
 */
@RestController
@RequestMapping("/api/v1/token/public")
@RequiredArgsConstructor
@Tag(name = "公开令牌接口", description = "通过令牌码获取公开信息的接口（无需JWT认证）")
public class TokenPublicController {

    private final AccessTokenService accessTokenService;

    @GetMapping("/info")
    @Operation(summary = "获取公开信息", description = "通过令牌码获取绑定的班级课程表、公告、健康概览等")
    public R<PublicInfoVO> getPublicInfo(@RequestParam String code) {
        return R.ok(accessTokenService.getPublicInfo(code));
    }

    @PostMapping("/validate")
    @Operation(summary = "验证令牌有效性")
    public R<Map<String, Object>> validate(@Valid @RequestBody TokenValidateDTO dto,
                                           HttpServletRequest request) {
        boolean valid = accessTokenService.validateToken(dto.getTokenCode());
        if (valid) {
            accessTokenService.recordAccess(dto.getTokenCode(),
                    request.getRemoteAddr());
        }
        return R.ok(Map.of(
                "valid", valid,
                "tokenCode", dto.getTokenCode()
        ));
    }
}
