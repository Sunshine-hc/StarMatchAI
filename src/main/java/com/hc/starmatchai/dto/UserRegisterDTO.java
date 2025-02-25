package com.hc.starmatchai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "用户注册信息")
public class UserRegisterDTO {
    @Schema(description = "邮箱", example = "user@example.com", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "密码", example = "password123", required = true)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;

    @Schema(description = "验证码", example = "123456", required = true)
    @NotBlank(message = "验证码不能为空")
    private String code;

    @Schema(description = "昵称", example = "星座爱好者")
    private String nickname;
}