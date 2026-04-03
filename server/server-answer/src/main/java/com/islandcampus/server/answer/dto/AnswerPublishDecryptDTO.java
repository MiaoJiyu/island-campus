package com.islandcampus.server.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "解密公布请求体")
public class AnswerPublishDecryptDTO {
    @NotBlank(message = "解密密码不能为空")
    private String password;
}
