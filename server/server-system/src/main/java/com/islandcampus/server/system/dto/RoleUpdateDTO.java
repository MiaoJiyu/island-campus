package com.islandcampus.server.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色更新DTO")
public class RoleUpdateDTO {

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "权限掩码")
    private Long permissionMask;

    @Schema(description = "数据范围: 1全部 2本部门 3本部门及以下 4仅本人")
    private Integer dataScope;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;
}
