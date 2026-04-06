import axios from 'axios'
import { getToken, clearToken } from '../utils/auth'
import { clearStoredUser } from '../utils/session'

// Axios 实例
const http = axios.create({
  baseURL: '',
  timeout: 10000
})

// 请求拦截：自动带上 token
http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一处理业务错误
http.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data && data.code !== 0) {
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data
  },
  (error) => {
    if (error?.response?.status === 401) {
      clearToken()
      clearStoredUser()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default http
