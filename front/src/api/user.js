import http from './http'

export const updateProfile = (data) => http.put('/api/user/profile', data)

export const changePassword = (data) => http.put('/api/user/password', data)
