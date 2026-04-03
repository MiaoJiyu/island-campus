package com.islandcampus.server.token.controller;

import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.base.R;
import com.islandcampus.server.token.dto.*;
import com.islandcampus.server.token.mapper.entity.AccessToken;
import com.islandcampus.server.token.service.AccessTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
@Tag(name = "令牌管理", description = "访问令牌创建/撤销/查询接口（需认证）")
public class TokenManagementController {

    private final AccessTokenService accessTokenService;

    @PostMapping
    @Operation(summary = "创建令牌")
    public R<AccessToken> create(@Valid @RequestBody TokenCreateDTO dto) {
        return R.ok(accessTokenService.createToken(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "撤销令牌")
    public R<Void> revoke(@PathVariable Long id) {
        accessTokenService.revokeToken(id);
        return R.ok();
    }

    @GetMapping
    @Operation(summary = "令牌列表")
    public R<PageResult<TokenVO>> list(
            @RequestParam(required = false) Long classId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return R.ok(accessTokenService.listTokens(classId, current, size));
    }
}
