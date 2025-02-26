import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import store from '@/store'

// 创建axios实例
const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '',
    timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 添加时间戳参数，避免GET请求被缓存
        if (config.method.toLowerCase() === 'get') {
            config.params = {
                ...config.params,
                _t: new Date().getTime()
            }
        }

        // 添加防缓存头
        config.headers['Cache-Control'] = 'no-cache'
        config.headers['Pragma'] = 'no-cache'

        // 从localStorage获取token
        const token = localStorage.getItem('token')

        // 如果有token则添加到请求头
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }

        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data

        // 如果返回的状态码不是200，则判断为错误
        if (res.code !== 200) {
            ElMessage({
                message: res.message || '请求失败',
                type: 'error',
                duration: 5 * 1000
            })

            // 401: 未登录或token过期
            if (res.code === 401) {
                // 清除token并跳转到登录页
                localStorage.removeItem('token')
                location.href = '/login'
            }

            return Promise.reject(new Error(res.message || '请求失败'))
        } else {
            return res
        }
    },
    error => {
        console.error('响应错误:', error)

        // 处理网络错误
        let message = '网络错误，请稍后重试'
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    message = '未授权，请重新登录'
                    // 清除token并跳转到登录页
                    localStorage.removeItem('token')
                    location.href = '/login'
                    break
                case 403:
                    message = '拒绝访问'
                    break
                case 404:
                    message = '请求的资源不存在'
                    break
                case 500:
                    message = '服务器错误'
                    break
                default:
                    message = `请求失败: ${error.response.status}`
            }
        } else if (error.request) {
            message = '服务器未响应'
        }

        ElMessage({
            message: message,
            type: 'error',
            duration: 5 * 1000
        })

        return Promise.reject(error)
    }
)

export default service