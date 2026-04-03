package com.islandcampus.server.token.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "创建令牌请求")
public class TokenCreateDTO {

    @NotBlank(message = "令牌名称不能为空")
    @Size(max = 100, message = "令牌名称不能超过100字符")
    @Schema(description = "令牌名称")
    private String name;

    @NotNull(message = "所属班级ID不能为空")
    @Schema(description = "关联班级ID")
    private Long classId;

    @NotNull(message = "过期时间不能为空")
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "最大使用次数（null表示不限制）")
    private Integer maxUseCount;

    @Schema(description = "权限列表", example = "[\"course\",\"announcement\",\"health\",\"answer\"]")
    private List<String> permissions;
}
