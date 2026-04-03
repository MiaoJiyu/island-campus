package com.islandcampus.server.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.base.R;
import com.islandcampus.server.system.dto.*;
import com.islandcampus.server.system.entity.User;
import com.islandcampus.server.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户CRUD、密码管理、批量导入")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "分页查询用户")
    public R<PageResult<User>> list(PageQuery query) {
        IPage<User> page = userService.pageQuery(query);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(),
                page.getCurrent(), page.getSize()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public R<User> detail(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public R<User> create(@Valid @RequestBody UserCreateDTO dto) {
        return R.ok(userService.createUser(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public R<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return R.ok();
    }

    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置用户密码")
    public R<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return R.ok();
    }

    @PostMapping("/batch-import")
    @Operation(summary = "批量导入用户")
    public R<Integer> batchImport(@RequestParam("file") MultipartFile file,
                                 @RequestParam(required = false) Long orgId,
                                 @RequestParam(required = false) Long roleId) {
        return R.ok(userService.batchImport(file, orgId, roleId));
    }
}
