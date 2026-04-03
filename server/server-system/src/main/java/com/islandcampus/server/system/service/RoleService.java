package com.islandcampus.server.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.system.dto.PageQuery;
import com.islandcampus.server.system.dto.RoleCreateDTO;
import com.islandcampus.server.system.dto.RoleUpdateDTO;
import com.islandcampus.server.system.entity.Role;

public interface RoleService extends IService<Role> {

    IPage<Role> pageQuery(PageQuery query);

    Role create(RoleCreateDTO dto);

    void updateRole(Long id, RoleUpdateDTO dto);

    void deleteRole(Long id);

    void assignPermissions(Long roleId, List<Long> permissionIds);
}
