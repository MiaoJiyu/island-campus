package com.islandcampus.server.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.islandcampus.server.health.entity.HealthReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface HealthReportMapper extends BaseMapper<HealthReport> {
    @Select("SELECT * FROM health_report WHERE computer_id = #{computerId} ORDER BY reported_at DESC LIMIT 1")
    HealthReport selectLatestByComputerId(@Param("computerId") Long computerId);

    @Select("SELECT DATE(reported_at) as report_date, AVG(cpu_temp) as avg_cpu, MAX(cpu_temp) as max_cpu, " +
            "AVG(disk_free_gb) as min_disk, COUNT(*) as cnt FROM health_report " +
            "WHERE computer_id = #{cid} AND reported_at >= #{start} GROUP BY DATE(reported_at)")
    List<Map<String, Object>> selectHistoryTrend(@Param("cid") Long cid, @Param("start") LocalDateTime start);
}
