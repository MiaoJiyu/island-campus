package com.islandcampus.server.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.islandcampus.server.system.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}
