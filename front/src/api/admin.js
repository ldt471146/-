import http from './http'

export const fetchTeacherApplyList = (params) => http.get('/api/admin/teacher-apply', { params })
export const reviewTeacherApply = (id, data) =>
  http.post(`/api/admin/teacher-apply/${id}/review`, data)
