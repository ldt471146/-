import http from './http'

export const createMockExam = (data) => http.post('/api/exams/mock', data)
export const submitMockExam = (data) => http.post('/api/exams/submit', data)
export const fetchExamTasks = () => http.get('/api/exam-tasks')
export const startExamTask = (id) => http.post(`/api/exam-tasks/${id}/start`)
export const fetchMyExamSubmissions = () => http.get('/api/exam-tasks/my-submissions')
