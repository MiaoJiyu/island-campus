package com.islandcampus.server.island.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.islandcampus.server.island.entity.Computer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ComputerMapper extends BaseMapper<Computer> {
}
