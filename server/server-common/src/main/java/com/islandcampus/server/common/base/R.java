package com.islandcampus.server.common.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "统一响应体")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {
    @Schema(description = "状态码")
    private int code;

    @Schema(description = "消息")
    private String message;

    @Schema(description = "数据")
    private T data;

    @Schema(description = "时间戳")
    private long timestamp;

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(200, "操作成功", data, System.currentTimeMillis());
    }

    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data, System.currentTimeMillis());
    }

    public static <T> R<T> fail(String message) {
        return new R<>(500, message, null, System.currentTimeMillis());
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null, System.currentTimeMillis());
    }

    public static <T> R<T> fail(int code, String message, T data) {
        return new R<>(code, message, data, System.currentTimeMillis());
    }
}
