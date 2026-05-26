import axios from 'axios'
import { ElMessage } from 'element-plus'

const apiBaseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const request = axios.create({
  baseURL: apiBaseURL,
  timeout: 300000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('xuance_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const body = response.data
    if (body && body.code !== 200) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body ? body.data : response.data
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('xuance_user')
      localStorage.removeItem('xuance_token')
      window.location.href = '/login'
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request
