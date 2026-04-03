package com.islandcampus.server.token.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "令牌视图对象")
public class TokenVO {

    @Schema(description = "令牌ID")
    private Long id;

    @Schema(description = "令牌码")
    private String tokenCode;

    @Schema(description = "令牌名称")
    private String name;

    @Schema(description = "所属班级名称")
    private String className;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "已使用次数")
    private Integer usedCount;

    @Schema(description = "最大使用次数")
    private Integer maxUseCount;

    @Schema(description = "状态: 0-无效 1-有效")
    private Integer status;

    @Schema(description = "权限列表JSON")
    private String permissions;
}
