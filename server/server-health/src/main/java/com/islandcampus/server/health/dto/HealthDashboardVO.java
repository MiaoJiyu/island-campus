package com.islandcampus.server.health.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "健康看板视图对象")
public class HealthDashboardVO {
    private Long totalComputers;
    private Long onlineCount;
    private Long healthyCount;
    private Long warningCount;
    private Long errorCount;
    private List<ClassStatVO> classStats;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ClassStatVO {
        private Long classId; private String className;
        private Long total; private Long online; private Long healthy; private Long warning; private Long error;
    }
}
