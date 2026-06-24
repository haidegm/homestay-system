import axios from 'axios'
import { ElMessage } from 'element-plus'

// 1. 创建实例 (名字叫 request)
const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000
})

// 2. 请求拦截器 (统一添加 Token)
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      // 统一加上 Bearer 前缀
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 3. 响应拦截器 (统一处理错误)
request.interceptors.response.use(
  response => {
    // 直接返回数据部分，方便前端调用
    return response.data
  },
  error => {
    if (error.response) {
      const status = error.response.status
      const msg = error.response.data?.msg
      
      if (status === 401) {
        ElMessage.error('请先登录或登录已过期')
        localStorage.removeItem('token')
        // 可选：跳转到登录页
        // window.location.href = '/login'
      } else if (status === 403) {
        // 403可能是账号被禁用或权限不足
        ElMessage.error(msg || '访问被拒绝')
        
        // 如果是账号被禁用，清除登录状态并跳转
        if (msg && msg.includes('禁用')) {
          localStorage.clear()
          setTimeout(() => {
            window.location.href = '/user'
          }, 1500)
        }
      } else {
        ElMessage.error(msg || '请求出错')
      }
    } else {
      ElMessage.error('网络错误，请检查连接')
    }
    return Promise.reject(error)
  }
)

// 4. 导出实例
export default request