package com.hc.starmatchai.service;


import com.hc.starmatchai.dto.UserLoginDTO;
import com.hc.starmatchai.dto.UserRegisterDTO;
import com.hc.starmatchai.vo.UserVO;

public interface UserService {
    /**
     * 用户注册
     */
    UserVO register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     */
    String login(UserLoginDTO loginDTO);

    /**
     * 获取当前登录用户信息
     */
    UserVO getCurrentUser();

    /**
     * 发送邮箱验证码
     */
    void sendVerificationCode(String email, Integer type);

    /**
     * 验证邮箱验证码
     */
    boolean verifyEmailCode(String email, String code, Integer type);

    /**
     * 用户登出
     */
    void logout(String token);
}