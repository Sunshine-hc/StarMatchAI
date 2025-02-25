import Cookies from 'js-cookie'
import { logout as apiLogout } from '@/api/user'

const TokenKey = 'star_match_token'
const DeviceIdKey = 'star_match_device_id'

// Token相关操作
export function getToken() {
    return localStorage.getItem('token')
}

export function setToken(token) {
    localStorage.setItem('token', token)
}

export function removeToken() {
    localStorage.removeItem('token')
}

// 设备ID相关操作（用于未登录状态下的记录关联）
export function getDeviceId() {
    let deviceId = localStorage.getItem(DeviceIdKey)
    if (!deviceId) {
        deviceId = generateDeviceId()
        localStorage.setItem(DeviceIdKey, deviceId)
    }
    return deviceId
}

function generateDeviceId() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        const r = Math.random() * 16 | 0
        const v = c === 'x' ? r : (r & 0x3 | 0x8)
        return v.toString(16)
    })
}

// 登出方法
export async function userLogout() {
    try {
        await apiLogout()
    } catch (error) {
        console.error('Logout API error:', error)
    } finally {
        // 无论请求成功与否，都清除本地token
        removeToken()
    }
}

// 检查 frontCode/src/api/user.js 是否包含 logout 方法
export function logout() {
    return request({
        url: '/api/user/logout',
        method: 'post'
    })
}

// 检查是否已登录
export function isLoggedIn() {
    return !!getToken()
} 