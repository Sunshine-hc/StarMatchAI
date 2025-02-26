package com.hc.starmatchai.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "邮箱验证信息")
public class EmailVerifyDTO {
    @Schema(description = "邮箱", example = "user@example.com", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "验证码类型：1-注册，2-登录，3-重置密码", example = "1", required = true)
    @NotNull(message = "验证码类型不能为空")
    private Integer type; // 1-注册，2-登录，3-重置密码
}