package com.islandcampus.server.system.controller;

import com.islandcampus.server.common.base.R;
import com.islandcampus.server.system.dto.OrgCreateDTO;
import com.islandcampus.server.system.dto.OrgUpdateDTO;
import com.islandcampus.server.system.entity.SysOrganization;
import com.islandcampus.server.system.service.SysOrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/org")
@RequiredArgsConstructor
@Tag(name = "组织管理", description = "组织架构CRUD与树状查询")
public class OrgController {

    private final SysOrganizationService orgService;

    @GetMapping
    @Operation(summary = "获取组织列表(分页)")
    public R<List<SysOrganization>> list() {
        return R.ok(orgService.list());
    }

    @GetMapping("/tree")
    @Operation(summary = "获取组织树")
    public R<List<SysOrganization>> tree() {
        return R.ok(orgService.getTree());
    }

    @GetMapping("/{id}/children")
    @Operation(summary = "获取子节点列表")
    public R<List<SysOrganization>> children(@PathVariable Long id) {
        return R.ok(orgService.getChildren(id));
    }

    @PostMapping
    @Operation(summary = "创建组织")
    public R<SysOrganization> create(@Valid @RequestBody OrgCreateDTO dto) {
        return R.ok(orgService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新组织")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody OrgUpdateDTO dto) {
        orgService.updateOrg(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除组织")
    public R<Void> delete(@PathVariable Long id) {
        orgService.removeById(id);
        return R.ok();
    }
}
