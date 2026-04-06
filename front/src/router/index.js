import { createRouter, createWebHistory } from 'vue-router'
import { getMe } from '../api/auth'
import { getToken, clearToken, getRemember } from '../utils/auth'
import {
  clearStoredUser,
  getRoleHome,
  getStoredUser,
  hasRequiredRole,
  setActiveRole,
  setStoredUser
} from '../utils/session'

const loadPage = (view) => () => import(`../pages/${view}.vue`)

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: loadPage('Login') },
  {
    path: '/',
    component: () => import('../layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', component: loadPage('Dashboard'), meta: { roles: ['STUDENT'] } },
      { path: 'courses', component: loadPage('Courses'), meta: { roles: ['STUDENT'] } },
      { path: 'courses/:id', component: loadPage('CourseDetail'), meta: { roles: ['STUDENT'] } },
      { path: 'practice', component: loadPage('Practice'), meta: { roles: ['STUDENT'] } },
      { path: 'code-practice', component: loadPage('CodePractice'), meta: { roles: ['STUDENT'] } },
      { path: 'learning-path', component: loadPage('LearningPath'), meta: { roles: ['STUDENT'] } },
      { path: 'community', component: loadPage('Community'), meta: { roles: ['STUDENT', 'TEACHER', 'ADMIN'] } },
      { path: 'community/:id', component: loadPage('CommunityPostDetail'), meta: { roles: ['STUDENT', 'TEACHER', 'ADMIN'] } },
      { path: 'profile', component: loadPage('Profile') },
      { path: 'notices', component: loadPage('Notices') },
      { path: 'reports', component: loadPage('Reports'), meta: { roles: ['STUDENT'] } },
      { path: 'exams', component: loadPage('Exams'), meta: { roles: ['STUDENT'] } },
      { path: 'homework', component: loadPage('Homework'), meta: { roles: ['STUDENT'] } },
      { path: 'teacher', component: loadPage('TeacherDashboard'), meta: { roles: ['TEACHER'] } },
      { path: 'teacher/questions', component: loadPage('TeacherQuestions'), meta: { roles: ['TEACHER'] } },
      { path: 'teacher/exams', component: loadPage('TeacherExams'), meta: { roles: ['TEACHER'] } },
      { path: 'teacher/homework', component: loadPage('TeacherHomework'), meta: { roles: ['TEACHER'] } },
      { path: 'teacher/stats', component: loadPage('TeacherStats'), meta: { roles: ['TEACHER'] } },
      { path: 'teacher/courses/:id', component: loadPage('TeacherCourseDetail'), meta: { roles: ['TEACHER'] } },
      { path: 'admin', component: loadPage('AdminDashboard'), meta: { roles: ['ADMIN'] } },
      { path: 'admin/users', component: loadPage('AdminUsers'), meta: { roles: ['ADMIN'] } },
      { path: 'admin/courses', component: loadPage('AdminCourses'), meta: { roles: ['ADMIN'] } },
      { path: 'admin/community', component: loadPage('AdminCommunity'), meta: { roles: ['ADMIN'] } },
      { path: 'admin/teacher-apply', component: loadPage('AdminTeacherApply'), meta: { roles: ['ADMIN'] } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const ensureCurrentUser = async () => {
  const cachedUser = getStoredUser()
  if (cachedUser) {
    return cachedUser
  }
  const token = getToken()
  if (!token) {
    return null
  }
  try {
    const response = await getMe()
    const user = response.data || null
    if (user) {
      setStoredUser(user, getRemember())
    }
    return user
  } catch {
    clearToken()
    clearStoredUser()
    return null
  }
}

router.beforeEach(async (to) => {
  const token = getToken()
  if (to.path === '/login') {
    if (!token) return true
    const user = await ensureCurrentUser()
    return user ? getRoleHome(user) : true
  }

  if (to.meta.requiresAuth && !token) {
    return '/login'
  }

  const requiredRoles = to.meta.roles || []
  if (!requiredRoles.length) {
    return true
  }

  const user = await ensureCurrentUser()
  if (!user) {
    return '/login'
  }

  if (!hasRequiredRole(user, requiredRoles)) {
    return getRoleHome(user)
  }

  if (requiredRoles.length === 1) {
    setActiveRole(requiredRoles[0], getRemember())
  }

  return true
})

export default router