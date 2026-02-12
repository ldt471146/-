<script setup>
import { computed, onMounted, onBeforeUnmount, ref } from 'vue'
import { clearToken } from '../utils/auth'
import { useRouter } from 'vue-router'
import { getMe } from '../api/auth'
import AiAssistant from '../components/AiAssistant.vue'
import http from '../api/http'

const router = useRouter()
const collapsed = ref(false)
const user = ref({ username: '同学', avatar: '' })
const theme = ref(localStorage.getItem('theme') || 'neon')
if (!['neon', 'red', 'aurora'].includes(theme.value)) {
  theme.value = 'neon'
  localStorage.setItem('theme', theme.value)
}
const unread = ref(0)
const isTeacher = computed(() => (user.value.roles || []).includes('TEACHER'))
const isAdmin = computed(() => (user.value.roles || []).includes('ADMIN'))
const roleLabel = computed(() => {
  const roles = user.value.roles || []
  if (roles.includes('ADMIN')) return '管理员'
  if (roles.includes('TEACHER')) return '教师'
  return '学生'
})
const handleProfileUpdate = (e) => {
  const data = e?.detail || {}
  user.value = { ...user.value, ...data }
}

const toggleAside = () => {
  collapsed.value = !collapsed.value
}

const handleLogout = () => {
  clearToken()
  router.push('/login')
}

const goProfile = () => {
  router.push('/profile')
}

const goAccount = () => {
  router.push('/profile')
}

onMounted(async () => {
  document.documentElement.setAttribute('data-theme', theme.value)
  try {
    const res = await getMe()
    user.value = res.data || user.value
  } catch (e) {
    // 忽略拉取失败
  }
  window.addEventListener('profile-updated', handleProfileUpdate)
  await loadUnread()
  window.addEventListener('notice-updated', loadUnread)
})

onBeforeUnmount(() => {
  window.removeEventListener('profile-updated', handleProfileUpdate)
  window.removeEventListener('notice-updated', loadUnread)
})

const loadUnread = async () => {
  try {
    const res = await http.get('/api/notices/unread-count')
    unread.value = res.data || 0
  } catch (e) {
    // ignore
  }
}

const goNotices = () => {
  router.push('/notices')
}

const activeMenu = computed(() => {
  const path = router.currentRoute.value.path
  if (path.startsWith('/courses')) return '/courses'
  if (path.startsWith('/practice')) return '/practice'
  if (path.startsWith('/code-practice')) return '/code-practice'
  if (path.startsWith('/learning-path')) return '/learning-path'
  if (path.startsWith('/reports')) return '/reports'
  if (path.startsWith('/exams')) return '/exams'
  if (path.startsWith('/notices')) return '/notices'
  if (path.startsWith('/profile')) return '/profile'
  if (path.startsWith('/teacher/stats')) return '/teacher/stats'
  if (path.startsWith('/teacher')) return '/teacher'
  if (path.startsWith('/admin/users')) return '/admin/users'
  if (path.startsWith('/admin/courses')) return '/admin/courses'
  if (path.startsWith('/admin')) return '/admin/teacher-apply'
  return path
})

const quickRoute = ref('')
const quickOptions = computed(() => {
  const items = [
    { label: '学习总览', value: '/dashboard' },
    { label: '我的课程', value: '/courses' },
    { label: '题库练习', value: '/practice' },
    { label: '编程判题', value: '/code-practice' },
    { label: '学习路径', value: '/learning-path' },
    { label: '在线考试', value: '/exams' },
    { label: `消息通知${unread.value > 0 ? ` (${unread.value})` : ''}`, value: '/notices' },
    { label: '成长报告', value: '/reports' }
  ]
  if (isTeacher.value) items.push({ label: '教师端', value: '/teacher' })
  if (isTeacher.value) items.push({ label: '教学统计', value: '/teacher/stats' })
  if (isAdmin.value) items.push({ label: '用户管理', value: '/admin/users' })
  if (isAdmin.value) items.push({ label: '课程审核', value: '/admin/courses' })
  if (isAdmin.value) items.push({ label: '教师审核', value: '/admin/teacher-apply' })
  return items
})

const jumpQuick = (path) => {
  if (!path) return
  router.push(path)
  quickRoute.value = ''
}

const themes = ['neon', 'red', 'aurora']
const toggleTheme = () => {
  const idx = themes.indexOf(theme.value)
  theme.value = themes[(idx + 1) % themes.length]
  localStorage.setItem('theme', theme.value)
  document.documentElement.setAttribute('data-theme', theme.value)
}
</script>

<template>
  <div class="layout-wrap">
    <div class="hud-grid"></div>

    <el-container class="layout">
      <el-aside :width="collapsed ? '72px' : '240px'" class="aside">
        <div class="brand">
          <div class="brand-core">NEON</div>
          <div v-if="!collapsed" class="brand-name">NEON LAB</div>
        </div>

        <el-menu class="menu" :default-active="activeMenu" :default-openeds="['learn', 'grow', 'manage']" :collapse="collapsed" router>
          <el-menu-item index="/dashboard">
            <span class="menu-dot"></span>
            学习总览
          </el-menu-item>
          <el-sub-menu index="learn">
            <template #title>
              <span class="menu-dot"></span>
              学习训练
            </template>
            <el-menu-item index="/courses">我的课程</el-menu-item>
            <el-menu-item index="/practice">题库练习</el-menu-item>
            <el-menu-item index="/code-practice">编程判题</el-menu-item>
            <el-menu-item index="/learning-path">学习路径</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/exams">
            <span class="menu-dot"></span>
            在线考试
          </el-menu-item>
          <el-sub-menu index="grow">
            <template #title>
              <span class="menu-dot"></span>
              互动成长
            </template>
            <el-menu-item index="/notices">
              消息通知
              <el-badge v-if="unread > 0" :value="unread" class="menu-badge" />
            </el-menu-item>
            <el-menu-item index="/reports">成长报告</el-menu-item>
          </el-sub-menu>
          <el-sub-menu v-if="isTeacher || isAdmin" index="manage">
            <template #title>
              <span class="menu-dot"></span>
              管理工作台
            </template>
            <el-menu-item v-if="isTeacher" index="/teacher">教师端</el-menu-item>
            <el-menu-item v-if="isTeacher" index="/teacher/stats">教学统计</el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/users">用户管理</el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/courses">课程审核</el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/teacher-apply">教师审核</el-menu-item>
          </el-sub-menu>
        </el-menu>

        <div class="aside-footer">
          <el-button v-if="!collapsed" type="danger" plain @click="handleLogout">退出登录</el-button>
        </div>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-button class="collapse-btn" text @click="toggleAside">
              {{ collapsed ? '展开' : '收起' }}
            </el-button>
            <div>
              <div class="title display">学习控制台</div>
              <div class="subtitle">更少入口，更快到达目标功能</div>
            </div>
          </div>
          <div class="header-right">
            <el-select
              v-model="quickRoute"
              class="quick-jump"
              size="small"
              filterable
              clearable
              placeholder="快速跳转"
              @change="jumpQuick"
            >
              <el-option v-for="item in quickOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <div class="status">
              <span class="status-dot"></span>
              online
            </div>
            <el-button class="theme-btn" size="small" @click="toggleTheme">
              {{ theme === 'neon' ? '赛博红' : theme === 'red' ? '极光' : '霓虹' }}
            </el-button>
            <el-dropdown trigger="click">
              <div class="user-entry">
                <el-avatar :src="user.avatar" :size="44">
                  {{ user.username?.slice(0, 1) || '同' }}
                </el-avatar>
                <div class="user-name">{{ user.username || '同学' }}</div>
                <div class="role-chip">{{ roleLabel }}</div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="goProfile">个人设置</el-dropdown-item>
                  <el-dropdown-item @click="goAccount">账号管理</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
    <AiAssistant />
  </div>
</template>

<style scoped>
.layout-wrap {
  min-height: 100vh;
  background: var(--ui-bg);
  color: var(--ui-text);
  position: relative;
  overflow: hidden;
}

.hud-grid {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle at 8% 10%, rgba(94, 247, 194, 0.09), transparent 28%),
    radial-gradient(circle at 94% 14%, rgba(56, 189, 248, 0.08), transparent 24%);
  pointer-events: none;
}


.layout {
  min-height: 100vh;
}

.aside {
  background: var(--ui-surface);
  border-right: 1px solid var(--ui-border-soft);
  display: flex;
  flex-direction: column;
  padding: 16px 12px;
  transition: width 0.2s ease;
  backdrop-filter: blur(6px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.brand-core {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--ui-accent), var(--ui-accent-2));
  color: #07101a;
  font-weight: 700;
  display: grid;
  place-items: center;
  letter-spacing: 0.08em;
}

.brand-name {
  font-size: 13px;
  letter-spacing: 0.1em;
  color: var(--ui-text);
}

.display {
  font-family: var(--font-display);
  letter-spacing: 0.04em;
  line-height: 1;
}

.menu {
  background: transparent;
  border: none;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: var(--ui-text);
  --el-menu-hover-bg-color: rgba(86, 255, 213, 0.07);
  --el-menu-active-color: var(--ui-accent);
}

.menu-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(86, 255, 213, 0.6);
  margin-right: 10px;
  box-shadow: 0 0 8px rgba(86, 255, 213, 0.6);
}

.quick-jump {
  width: 150px;
}

.aside-footer {
  margin-top: auto;
  display: flex;
  justify-content: center;
  padding-top: 12px;
}

.header {
  padding: 26px 26px;
  border-bottom: 1px solid var(--ui-border-soft);
  background: var(--ui-surface);
  display: flex;
  align-items: center;
  justify-content: space-between;
  backdrop-filter: blur(8px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--ui-accent);
  box-shadow: 0 0 12px rgba(86, 255, 213, 0.7);
  animation: pulse 2s ease-in-out infinite;
}

.user-name {
  font-size: 14px;
  color: var(--ui-text);
  font-weight: 600;
}

.user-entry {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid var(--ui-border-soft);
  background: rgba(86, 255, 213, 0.05);
}

.role-chip {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  border: 1px solid var(--ui-border);
  color: var(--ui-text);
  background: var(--ui-surface-soft);
}

.collapse-btn {
  color: var(--ui-text);
}

.title {
  font-size: 22px;
  font-weight: 600;
}

.subtitle {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 4px;
}

.main {
  padding: 20px 22px;
}

:deep(.el-card) {
  background: var(--ui-card);
  border: 1px solid var(--ui-border-soft);
  color: var(--ui-text);
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--ui-border-soft);
}

.theme-btn {
  border: 1px solid var(--ui-border);
  color: var(--ui-text);
  background: rgba(86, 255, 213, 0.06);
}

:deep(.el-menu-item) {
  border-radius: 10px;
  margin: 3px 0;
}

:deep(.el-sub-menu .el-sub-menu__title) {
  border-radius: 10px;
  margin: 3px 0;
}

:deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--ui-accent);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(120deg, rgba(86, 255, 213, 0.16), rgba(0, 210, 255, 0.12));
  border: 1px solid var(--ui-border);
  box-shadow: 0 0 16px rgba(86, 255, 213, 0.2);
}

:deep(.el-menu-item.is-active .menu-dot) {
  background: var(--ui-accent);
  box-shadow: 0 0 12px rgba(86, 255, 213, 0.9);
}


.menu-badge {
  margin-left: auto;
}

:deep(.menu-badge .el-badge__content) {
  background: var(--ui-accent-2);
  color: #041018;
  border: none;
  box-shadow: 0 0 10px rgba(0, 210, 255, 0.35);
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
  }
}

@media (max-width: 960px) {
  .quick-jump {
    display: none;
  }
  .header {
    padding: 18px 14px;
  }
  .main {
    padding: 14px;
  }
}
</style>
