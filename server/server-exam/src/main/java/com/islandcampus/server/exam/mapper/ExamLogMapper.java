package com.islandcampus.server.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.islandcampus.server.exam.mapper.entity.ExamLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExamLogMapper extends BaseMapper<ExamLog> {
}
