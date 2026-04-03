package com.islandcampus.server.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.system.entity.Permission;
import com.islandcampus.server.system.mapper.PermissionMapper;
import com.islandcampus.server.system.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission>
        implements PermissionService {

    @Override
    public List<Permission> getTree() {
        List<Permission> all = list(new LambdaQueryWrapper<Permission>()
                .orderByAsc(Permission::getSortOrder));
        return buildTree(all, 0L);
    }

    @Override
    public List<Permission> getMenuPermissions() {
        List<Permission> all = list(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getType, 2)
                .orderByAsc(Permission::getSortOrder));
        return buildTree(all, 0L);
    }

    private List<Permission> buildTree(List<Permission> all, Long parentId) {
        Map<Long, List<Permission>> childrenMap = all.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.groupingBy(Permission::getParentId));

        return all.stream()
                .filter(p -> p.getParentId() == null || p.getParentId().equals(parentId))
                .toList();
    }
}
