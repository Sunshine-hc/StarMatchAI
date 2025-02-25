import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import store from '@/store'

// 创建axios实例
const service = axios.create({
    baseURL: '',
    timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
    config => {
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
        return response.data
    },
    error => {
        // 处理401未授权错误
        if (error.response && error.response.status === 401) {
            // 清除用户信息
            store.dispatch('user/logout')

            // 显示提示消息
            ElMessage.error({
                message: '请登录',
                duration: 3000
            })

            // 记录当前路由，以便登录后返回
            const currentPath = router.currentRoute.value.fullPath

            // 跳转到登录页面
            router.push({
                path: '/login',
                query: { redirect: currentPath }
            })
        } else {
            // 处理其他错误
            const message = error.response?.data?.message || '请求失败，请稍后重试'
            ElMessage.error({
                message,
                duration: 3000
            })
        }

        return Promise.reject(error)
    }
)

export default service