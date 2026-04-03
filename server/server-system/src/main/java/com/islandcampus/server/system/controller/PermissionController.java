package com.islandcampus.server.system.controller;

import com.islandcampus.server.common.base.R;
import com.islandcampus.server.system.entity.Permission;
import com.islandcampus.server.system.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限树与菜单查询")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @Operation(summary = "获取权限树")
    public R<List<Permission>> tree() {
        return R.ok(permissionService.getTree());
    }

    @GetMapping("/menu")
    @Operation(summary = "获取菜单权限")
    public R<List<Permission>> menu() {
        return R.ok(permissionService.getMenuPermissions());
    }
}
