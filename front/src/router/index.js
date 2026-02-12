import { createRouter, createWebHistory } from 'vue-router'
import Login from '../pages/Login.vue'
import Dashboard from '../pages/Dashboard.vue'
import Courses from '../pages/Courses.vue'
import CourseDetail from '../pages/CourseDetail.vue'
import Practice from '../pages/Practice.vue'
import CodePractice from '../pages/CodePractice.vue'
import Reports from '../pages/Reports.vue'
import Exams from '../pages/Exams.vue'
import Profile from '../pages/Profile.vue'
import Notices from '../pages/Notices.vue'
import TeacherDashboard from '../pages/TeacherDashboard.vue'
import TeacherCourseDetail from '../pages/TeacherCourseDetail.vue'
import TeacherQuestions from '../pages/TeacherQuestions.vue'
import TeacherExams from '../pages/TeacherExams.vue'
import AdminTeacherApply from '../pages/AdminTeacherApply.vue'
import AppLayout from '../layouts/AppLayout.vue'
import { getToken } from '../utils/auth'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  {
    path: '/',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'courses', component: Courses },
      { path: 'courses/:id', component: CourseDetail },
      { path: 'practice', component: Practice },
      { path: 'code-practice', component: CodePractice },
      { path: 'profile', component: Profile },
      { path: 'notices', component: Notices },
      { path: 'reports', component: Reports },
      { path: 'exams', component: Exams },
      { path: 'teacher', component: TeacherDashboard },
      { path: 'teacher/questions', component: TeacherQuestions },
      { path: 'teacher/exams', component: TeacherExams },
      { path: 'teacher/courses/:id', component: TeacherCourseDetail },
      { path: 'admin/teacher-apply', component: AdminTeacherApply }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 简单路由守卫：有 token 才能进入受保护页面
router.beforeEach((to) => {
  const token = getToken()
  if (to.path === '/login' && token) {
    return '/dashboard'
  }
  if (to.meta.requiresAuth && !token) {
    return '/login'
  }
  return true
})

export default router
