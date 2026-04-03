package com.islandcampus.server.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.base.R;
import com.islandcampus.server.system.dto.PageQuery;
import com.islandcampus.server.system.dto.RoleCreateDTO;
import com.islandcampus.server.system.dto.RoleUpdateDTO;
import com.islandcampus.server.system.entity.Role;
import com.islandcampus.server.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色CRUD与权限分配")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "分页查询角色")
    public R<PageResult<Role>> list(PageQuery query) {
        IPage<Role> page = roleService.pageQuery(query);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(),
                page.getCurrent(), page.getSize()));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有角色(下拉选择)")
    public R<List<Role>> all() {
        return R.ok(roleService.list());
    }

    @PostMapping
    @Operation(summary = "创建角色")
    public R<Role> create(@Valid @RequestBody RoleCreateDTO dto) {
        return R.ok(roleService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO dto) {
        roleService.updateRole(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public R<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return R.ok();
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "分配权限")
    public R<Void> assignPermissions(@PathVariable Long id,
                                     @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return R.ok();
    }
}
