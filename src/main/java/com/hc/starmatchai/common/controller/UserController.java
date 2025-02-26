package com.hc.starmatchai.common.controller;

import com.hc.starmatchai.common.result.Result;
import com.hc.starmatchai.common.dto.EmailVerifyDTO;
import com.hc.starmatchai.common.dto.UserLoginDTO;
import com.hc.starmatchai.common.dto.UserRegisterDTO;
import com.hc.starmatchai.service.UserService;
import com.hc.starmatchai.common.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册", description = "通过邮箱、密码和验证码注册新用户")
    @PostMapping("/register")
    public Result<UserVO> register(
            @Parameter(description = "注册信息") @RequestBody @Validated UserRegisterDTO registerDTO) {
        UserVO userVO = userService.register(registerDTO);
        return Result.success(userVO);
    }

    @Operation(summary = "用户登录", description = "通过邮箱和密码登录系统，返回JWT令牌")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Parameter(description = "登录信息") @RequestBody @Validated UserLoginDTO loginDTO) {
        String token = userService.login(loginDTO);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        return Result.success(result);
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        UserVO userVO = userService.getCurrentUser();
        return Result.success(userVO);
    }

    @Operation(summary = "发送验证码", description = "向指定邮箱发送验证码，支持注册、登录和重置密码场景")
    @PostMapping("/send-code")
    public Result<String> sendVerificationCode(
            @Parameter(description = "邮箱验证信息") @RequestBody @Validated EmailVerifyDTO emailVerifyDTO) {
        userService.sendVerificationCode(emailVerifyDTO.getEmail(), emailVerifyDTO.getType());
        return Result.success("");
    }

    @Operation(summary = "用户登出", description = "使当前用户的令牌失效")
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        userService.logout(token);
        return Result.success("");
    }
}