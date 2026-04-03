package com.islandcampus.server.token.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "令牌验证请求")
public class TokenValidateDTO {

    @NotBlank(message = "令牌码不能为空")
    @Schema(description = "令牌码")
    private String tokenCode;
}
