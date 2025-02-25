import request from '@/utils/request'
import { ref, reactive, onBeforeUnmount } from 'vue';

// 用户注册
export function register(data) {
    return request({
        url: '/api/user/register',
        method: 'post',
        data
    })
}

// 用户登录
export function login(data) {
    return request({
        url: '/api/user/login',
        method: 'post',
        data
    })
}

// 获取用户信息
export function getUserInfo() {
    return request({
        url: '/api/user/info',
        method: 'get'
    })
}

// 发送验证码
export function sendVerificationCode(data) {
    // 将type字段转换为整数
    const requestData = {
        ...data,
        type: data.type === 'register' ? 1 : 2  // 假设1表示注册，2表示其他用途
    };

    return request({
        url: '/api/user/send-code',
        method: 'post',
        data: requestData
    });
}

// 退出登录
export function logout() {
    return request({
        url: '/api/user/logout',
        method: 'post'
    })
} 