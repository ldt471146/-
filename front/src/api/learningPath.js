import http from './http'

export const fetchLearningPath = (courseId) => http.get('/api/learning-path', { params: { courseId } })
export const markLearningPathProgress = (data) => http.post('/api/learning-path/progress', data)

