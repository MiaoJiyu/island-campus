package com.islandcampus.server.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.islandcampus.server.health.entity.Screenshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScreenshotMapper extends BaseMapper<Screenshot> {
    @Select("SELECT s.* FROM screenshot s INNER JOIN computer c ON s.computer_id = c.id " +
            "WHERE c.class_id = #{classId} AND s.deleted = 0 ORDER BY s.captured_at DESC LIMIT #{limit}")
    List<Screenshot> selectRecentByClassId(@Param("classId") Long classId, @Param("limit") int limit);
}
