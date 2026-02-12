import http from './http'

export const fetchMyHomework = (params) => http.get('/api/homework', { params })
export const fetchMyHomeworkDetail = (id) => http.get(`/api/homework/${id}`)

export const createTeacherHomework = (data) => http.post('/api/teacher/homework', data)
export const fetchTeacherHomework = (params) => http.get('/api/teacher/homework', { params })
export const fetchTeacherHomeworkDetail = (id) => http.get(`/api/teacher/homework/${id}`)
export const deleteTeacherHomework = (id) => http.delete(`/api/teacher/homework/${id}`)

