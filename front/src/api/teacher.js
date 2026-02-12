import http from './http'
import { getToken } from '../utils/auth'

export const fetchTeacherCourses = () => http.get('/api/teacher/courses')
export const createTeacherCourse = (data) => http.post('/api/teacher/courses', data)
export const updateTeacherCourse = (id, data) => http.put(`/api/teacher/courses/${id}`, data)
export const deleteTeacherCourse = (id) => http.delete(`/api/teacher/courses/${id}`)
export const fetchTeacherCourseDetail = (id) => http.get(`/api/teacher/courses/${id}`)
export const addTeacherChapter = (courseId, data) =>
  http.post(`/api/teacher/courses/${courseId}/chapters`, data)
export const updateTeacherChapter = (id, data) => http.put(`/api/teacher/chapters/${id}`, data)
export const deleteTeacherChapter = (id) => http.delete(`/api/teacher/chapters/${id}`)
export const addTeacherLesson = (chapterId, data) =>
  http.post(`/api/teacher/chapters/${chapterId}/lessons`, data)
export const updateTeacherLesson = (id, data) => http.put(`/api/teacher/lessons/${id}`, data)
export const deleteTeacherLesson = (id) => http.delete(`/api/teacher/lessons/${id}`)

export const fetchTeacherQuestions = (params) => http.get('/api/teacher/questions', { params })
export const createTeacherQuestion = (data) => http.post('/api/teacher/questions', data)
export const updateTeacherQuestion = (id, data) => http.put(`/api/teacher/questions/${id}`, data)
export const deleteTeacherQuestion = (id) => http.delete(`/api/teacher/questions/${id}`)
export const importTeacherQuestions = (data) => http.post('/api/teacher/questions/import', data)

export const fetchTeacherCodeProblems = (params) => http.get('/api/teacher/code-problems', { params })
export const createTeacherCodeProblem = (data) => http.post('/api/teacher/code-problems', data)
export const updateTeacherCodeProblem = (id, data) => http.put(`/api/teacher/code-problems/${id}`, data)
export const deleteTeacherCodeProblem = (id) => http.delete(`/api/teacher/code-problems/${id}`)

export const createTeacherExamTask = (data) => http.post('/api/teacher/exams', data)
export const fetchTeacherExamTasks = () => http.get('/api/teacher/exams')
export const deleteTeacherExamTask = (id) => http.delete(`/api/teacher/exams/${id}`)

export const fetchTeacherStatsOverview = () => http.get('/api/teacher/stats/overview')
export const exportTeacherStats = async (type = 'students') => {
  const token = getToken()
  const res = await fetch(`/api/teacher/stats/export?type=${encodeURIComponent(type)}`, {
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    }
  })
  if (!res.ok) {
    throw new Error('导出失败')
  }
  return res.blob()
}
