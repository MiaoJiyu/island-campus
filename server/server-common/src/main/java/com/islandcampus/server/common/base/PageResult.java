package com.islandcampus.server.common.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "分页结果")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页码")
    private long current;

    @Schema(description = "每页大小")
    private long size;

    @Schema(description = "总页数")
    private long pages;

    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        return new PageResult<>(records, total, current, size,
                (total + size - 1) / size);
    }
}
