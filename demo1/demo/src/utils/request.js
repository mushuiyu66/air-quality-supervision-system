//导入axios
import axios from "axios";
//定义变量 记录公共前缀 - 直接访问后端/shixun路径
const baseURL = '/shixun'
const instance =axios.create({
    baseURL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})
//添加一个拦截器
instance.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = 'Bearer ' + token
        }
        return config
    },
    error => {
        console.error('[Request Error]', error)
        return Promise.reject(error)
    }
)
//添加一个拦截响应器
instance.interceptors.response.use(
    result=>{
        console.log('[Response] Status:', result.status)
        console.log('[Response] Data:', result.data)
        return result.data
    },
    error=>{
        console.error('[Response Error]:', error)
        if (error.response) {
            console.error('[Response Error] Status:', error.response.status)
            console.error('[Response Error] Data:', error.response.data)
            if (error.response.status === 401) {
                localStorage.removeItem('token')
                localStorage.removeItem('role')
                localStorage.removeItem('gmId')
                if (window.location.pathname !== '/') {
                    window.location.href = '/'
                }
            }
        }
        return Promise.reject(error)
    }
)
export default instance