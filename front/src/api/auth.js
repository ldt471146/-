import http from './http'

// 登录
export const login = (payload) => http.post('/api/auth/login', payload)

// 注册
export const register = (payload) => http.post('/api/auth/register', payload)

// 发送邮箱验证码
export const sendCode = (payload) => http.post('/api/auth/send-code', payload)

// 获取当前用户
export const getMe = () => http.get('/api/user/me')
