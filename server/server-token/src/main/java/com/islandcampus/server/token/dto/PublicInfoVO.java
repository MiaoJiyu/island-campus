package com.islandcampus.server.token.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "令牌公开信息视图")
public class PublicInfoVO {

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "课程列表")
    private List<?> courseList;

    @Schema(description = "公告列表")
    private List<?> announcements;

    @Schema(description = "健康概览")
    private Map<String, Object> healthOverview;

    @Schema(description = "已公布答案列表")
    private List<?> publishedAnswers;
}
