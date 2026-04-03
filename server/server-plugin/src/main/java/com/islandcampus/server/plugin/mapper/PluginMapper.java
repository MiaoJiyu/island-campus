package com.islandcampus.server.plugin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.islandcampus.server.plugin.mapper.entity.Plugin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PluginMapper extends BaseMapper<Plugin> {
}
