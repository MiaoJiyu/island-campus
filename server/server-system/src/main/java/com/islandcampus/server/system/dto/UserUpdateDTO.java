package com.islandcampus.server.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "用户更新DTO")
public class UserUpdateDTO {

    @Schema(description = "真实姓名")
    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "组织ID")
    private Long orgId;

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;
}
