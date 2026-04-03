package com.islandcampus.server.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.system.dto.PageQuery;
import com.islandcampus.server.system.dto.RoleCreateDTO;
import com.islandcampus.server.system.dto.RoleUpdateDTO;
import com.islandcampus.server.system.entity.Role;
import com.islandcampus.server.system.mapper.RoleMapper;
import com.islandcampus.server.system.service.RoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public IPage<Role> pageQuery(PageQuery query) {
        Page<Role> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Role::getName, query.getKeyword())
                    .or().like(Role::getCode, query.getKeyword()));
        }
        wrapper.orderByAsc(Role::getId);
        return page(page, wrapper);
    }

    @Override
    public Role create(RoleCreateDTO dto) {
        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        role.setIsBuiltin(0);
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        save(role);
        System.out.println("[AUDIT] 创建角色: name=" + dto.getName()
                + ", operator=" + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());
        return role;
    }

    @Override
    public void updateRole(Long id, RoleUpdateDTO dto) {
        Role existing = getById(id);
        if (existing == null) {
            throw new BusinessException("角色不存在");
        }
        BeanUtils.copyProperties(dto, existing, "id", "isBuiltin");
        updateById(existing);
        System.out.println("[AUDIT] 更新角色: roleId=" + id + ", operator="
                + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());
    }

    @Override
    public void deleteRole(Long id) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        if (role.getIsBuiltin() != null && role.getIsBuiltin() == 1) {
            throw new BusinessException("内置角色不可删除");
        }
        removeById(id);
        System.out.println("[AUDIT] 删除角色: roleId=" + id + ", operator="
                + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());
    }

    @Override
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        // Build permission mask from IDs (simplified)
        long mask = 0;
        if (permissionIds != null) {
            for (Long pid : permissionIds) {
                mask |= (1L << (pid % 64));
            }
        }
        role.setPermissionMask(mask);
        updateById(role);
        System.out.println("[AUDIT] 分配权限: roleId=" + roleId + ", mask=" + mask
                + ", operator=" + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());
    }
}
