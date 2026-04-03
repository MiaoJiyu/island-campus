package com.islandcampus.server.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.system.entity.Permission;

import java.util.List;

public interface PermissionService extends IService<Permission> {

    List<Permission> getTree();

    List<Permission> getMenuPermissions();
}
