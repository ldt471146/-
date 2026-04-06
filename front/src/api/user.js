import http from './http'

export const updateProfile = (data) => http.put('/api/user/profile', data)
export const uploadAvatar = (formData) => http.post('/api/user/avatar', formData)
export const changePassword = (data) => http.put('/api/user/password', data)
