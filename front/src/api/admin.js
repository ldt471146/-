import http from './http'

export const fetchTeacherApplyList = (params) => http.get('/api/admin/teacher-apply', { params })
export const reviewTeacherApply = (id, data) =>
  http.post(`/api/admin/teacher-apply/${id}/review`, data)

export const fetchAdminUsers = (params) => http.get('/api/admin/users', { params })
export const updateAdminUserStatus = (id, data) => http.put(`/api/admin/users/${id}/status`, data)
export const updateAdminUserRoles = (id, data) => http.put(`/api/admin/users/${id}/roles`, data)

export const fetchAdminCourses = (params) => http.get('/api/admin/courses', { params })
export const reviewAdminCourse = (id, data) => http.post(`/api/admin/courses/${id}/review`, data)
