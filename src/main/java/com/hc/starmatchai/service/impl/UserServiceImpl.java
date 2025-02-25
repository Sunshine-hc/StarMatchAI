package com.hc.starmatchai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hc.starmatchai.common.BusinessException;
import com.hc.starmatchai.dto.UserLoginDTO;
import com.hc.starmatchai.dto.UserRegisterDTO;
import com.hc.starmatchai.entity.User;
import com.hc.starmatchai.mapper.UserMapper;
import com.hc.starmatchai.service.UserService;
import com.hc.starmatchai.util.JwtUtil;
import com.hc.starmatchai.util.RedisUtil;
import com.hc.starmatchai.util.UserContext;
import com.hc.starmatchai.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private JavaMailSender mailSender;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private RedisUtil redisUtil;

    @Value("${spring.mail.username}")
    private String mailFrom;

    // Redis键前缀
    private static final String EMAIL_VERIFY_PREFIX = "email:verify:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserRegisterDTO registerDTO) {
        // 验证邮箱是否已注册
        User existUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, registerDTO.getEmail()));
        if (existUser != null) {
            throw new BusinessException("该邮箱已注册");
        }

        // 验证验证码
        boolean verified = verifyEmailCode(registerDTO.getEmail(), registerDTO.getCode(), 1);
        if (!verified) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 创建用户
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setNickname(StrUtil.isBlank(registerDTO.getNickname()) ? "用户" + RandomUtil.randomString(6)
                : registerDTO.getNickname());
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        userMapper.insert(user);

        // 转换为VO返回
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public String login(UserLoginDTO loginDTO) {
        // 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, loginDTO.getEmail()));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 更新登录信息
        user.setLastLoginTime(new Date());
        userMapper.updateById(user);

        // 生成JWT令牌
        return jwtUtil.generateToken(user.getId());
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public void sendVerificationCode(String email, Integer type) {
        // 生成6位数字验证码
        String code = RandomUtil.randomNumbers(6);

        // 构建Redis键
        String redisKey = buildVerificationCodeKey(email, type);

        // 存入Redis，设置15分钟过期
        redisUtil.set(redisKey, code, 15, TimeUnit.MINUTES);

        // 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);

        String subject = "星座匹配 - ";
        String content = "您的验证码是：" + code + "，有效期15分钟，请勿泄露给他人。";

        if (type == 1) {
            subject += "注册验证码";
        } else if (type == 2) {
            subject += "登录验证码";
        } else if (type == 3) {
            subject += "重置密码验证码";
        } else {
            subject += "验证码";
        }

        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
        log.info("验证码邮件已发送至：{}", email);
    }

    @Override
    public boolean verifyEmailCode(String email, String code, Integer type) {
        // 构建Redis键
        String redisKey = buildVerificationCodeKey(email, type);

        // 从Redis获取验证码
        Object savedCode = redisUtil.get(redisKey);

        if (savedCode == null || !savedCode.toString().equals(code)) {
            return false;
        }

        // 验证成功后删除验证码
        redisUtil.delete(redisKey);

        return true;
    }

    /**
     * 构建验证码的Redis键
     */
    private String buildVerificationCodeKey(String email, Integer type) {
        String typeStr;
        switch (type) {
            case 1:
                typeStr = "register";
                break;
            case 2:
                typeStr = "login";
                break;
            case 3:
                typeStr = "reset";
                break;
            default:
                typeStr = "unknown";
        }
        return EMAIL_VERIFY_PREFIX + typeStr + ":" + email;
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        jwtUtil.invalidateToken(token);
    }
}