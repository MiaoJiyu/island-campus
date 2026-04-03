package com.islandcampus.server.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页查询通用DTO")
public class PageQuery {

    @Schema(description = "当前页码")
    private Long current = 1L;

    @Schema(description = "每页大小")
    private Long size = 10L;

    @Schema(description = "关键字搜索")
    private String keyword;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序方向 asc/desc")
    private String sortOrder = "asc";
}
