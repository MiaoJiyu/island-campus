package com.islandcampus.server.computer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.islandcampus.server.computer.mapper.entity.Computer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ComputerMapper extends BaseMapper<Computer> {
}
