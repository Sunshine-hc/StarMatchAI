import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        return config
    },
    error => {
        console.error('请求错误：', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        }
        return res.data
    },
    error => {
        console.error('响应错误：', error)
        ElMessage.error(error.message || '系统异常')
        return Promise.reject(error)
    }
)

export default service 