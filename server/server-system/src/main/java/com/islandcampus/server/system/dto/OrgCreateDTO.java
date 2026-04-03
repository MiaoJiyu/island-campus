package com.islandcampus.server.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "组织创建DTO")
public class OrgCreateDTO {

    @NotBlank(message = "名称不能为空")
    @Schema(description = "名称")
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "父级ID")
    private Long parentId;

    /**
     * 1分区 2学校 3年级 4班级
     */
    @Schema(description = "类型: 1分区 2学校 3年级 4班级")
    private Integer type;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;

    @Schema(description = "描述")
    private String description;
}
