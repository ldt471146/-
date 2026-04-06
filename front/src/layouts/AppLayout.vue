<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getMe } from '../api/auth'
import http from '../api/http'
import AiAssistant from '../components/AiAssistant.vue'
import { clearToken, getRemember } from '../utils/auth'
import {
  clearStoredUser,
  getActiveRole,
  getAvailableRoles,
  getRoleHome,
  getStoredUser,
  setActiveRole,
  setStoredUser
} from '../utils/session'
import { getPortalSections, getPortalTitle, getQuickActions, THEME_ORDER } from '../config/portal'
import {
  Bell,
  ChatDotRound,
  Collection,
  Compass,
  DataAnalysis,
  DocumentChecked,
  Finished,
  Histogram,
  House,
  Lightning,
  Monitor,
  Reading,
  Share,
  SwitchButton,
  User,
  UserFilled
} from '@element-plus/icons-vue'

const iconMap = {
  Bell,
  ChatDotRound,
  Collection,
  DataAnalysis,
  DocumentChecked,
  Finished,
  Histogram,
  House,
  Monitor,
  Reading,
  Share,
  User,
  UserFilled
}

const roleMeta = {
  STUDENT: {
    mark: '学',
    shortLabel: '学生',
    label: '学生门户',
    description: '聚焦课程学习、练习巩固和成长反馈。'
  },
  TEACHER: {
    mark: '师',
    shortLabel: '教师',
    label: '教师门户',
    description: '聚焦课程编排、题库管理和教学执行。'
  },
  ADMIN: {
    mark: '管',
    shortLabel: '管理',
    label: '管理员门户',
    description: '聚焦治理看板、审核流程和平台秩序。'
  }
}

const themeOptions = [
  { key: 'neon', label: '霓虹夜航', hint: '赛博荧光' },
  { key: 'red', label: '赤焰竞速', hint: '热能暗红' },
  { key: 'aurora', label: '极光晨雾', hint: '轻透高对比' }
]

const rolePathPrefixes = {
  STUDENT: ['/dashboard', '/courses', '/practice', '/code-practice', '/learning-path', '/homework', '/reports', '/exams'],
  TEACHER: ['/teacher'],
  ADMIN: ['/admin']
}

const router = useRouter()
const collapsed = ref(false)
const unread = ref(0)
const quickRoute = ref('')
const theme = ref(localStorage.getItem('theme') || 'neon')
const user = ref(getStoredUser() || { username: '同学', avatar: '', roles: ['STUDENT'] })
const activeRole = ref(getActiveRole(user.value))

if (!THEME_ORDER.includes(theme.value)) {
  theme.value = THEME_ORDER[0]
}

const availableRoles = computed(() => getAvailableRoles(user.value))
const currentRoleMeta = computed(() => roleMeta[activeRole.value] || roleMeta.STUDENT)
const portalTitle = computed(() => getPortalTitle(user.value, activeRole.value))
const portalSections = computed(() => getPortalSections(user.value, unread.value, activeRole.value))
const quickActions = computed(() => getQuickActions(user.value, unread.value, activeRole.value))
const activeTheme = computed(() => themeOptions.find((item) => item.key === theme.value) || themeOptions[0])
const todayLabel = computed(() => {
  const now = new Date()
  return `${now.getMonth() + 1}月${now.getDate()}日`
})
const userInitial = computed(() => String(user.value?.username || '同').slice(0, 1))
const resolvedAvatar = computed(() => resolveAssetUrl(user.value?.avatar))
const activeMenu = computed(() => {
  const path = router.currentRoute.value.path
  const prefixes = quickActions.value
    .map((item) => item.path)
    .sort((left, right) => right.length - left.length)
  return prefixes.find((prefix) => path === prefix || path.startsWith(`${prefix}/`)) || path
})

const resolveAssetUrl = (value) => {
  const raw = String(value || '').trim()
  if (!raw) return ''
  if (/^data:/i.test(raw)) return raw
  if (/^https?:\/\//i.test(raw) || raw.startsWith('//') || raw.startsWith('/')) return raw
  return `/${raw.replace(/^\/+/, '')}`
}

const syncPortalRole = (path = router.currentRoute.value.path) => {
  const matchedRole = Object.entries(rolePathPrefixes).find(([, prefixes]) =>
    prefixes.some((prefix) => path === prefix || path.startsWith(`${prefix}/`))
  )?.[0]

  if (matchedRole && availableRoles.value.includes(matchedRole)) {
    activeRole.value = matchedRole
    setActiveRole(matchedRole, getRemember())
    return
  }

  activeRole.value = getActiveRole(user.value)
}

const toggleAside = () => {
  collapsed.value = !collapsed.value
}

const handleProfileUpdate = (event) => {
  const payload = event?.detail || {}
  user.value = { ...user.value, ...payload }
  setStoredUser(user.value, getRemember())
  syncPortalRole()
}

const loadCurrentUser = async () => {
  try {
    const response = await getMe()
    if (response.data) {
      user.value = response.data
      setStoredUser(response.data, getRemember())
      activeRole.value = getActiveRole(response.data)
    }
  } catch {
    // 401 会由拦截器统一处理
  }
}

const loadUnread = async () => {
  try {
    const response = await http.get('/api/notices/unread-count')
    unread.value = response.data || 0
  } catch {
    unread.value = 0
  }
}

const applyTheme = (nextTheme) => {
  if (!THEME_ORDER.includes(nextTheme)) return
  theme.value = nextTheme
  localStorage.setItem('theme', nextTheme)
  document.documentElement.setAttribute('data-theme', nextTheme)
}

const switchPortal = (role) => {
  if (!role || role === activeRole.value) return
  activeRole.value = role
  setActiveRole(role, getRemember())
  router.push(getRoleHome(user.value, role))
}

const jumpQuick = (path) => {
  if (!path) return
  quickRoute.value = ''
  router.push(path)
}

const goHome = () => {
  router.push(getRoleHome(user.value, activeRole.value))
}

const goProfile = () => {
  router.push('/profile')
}

const handleLogout = () => {
  clearToken()
  clearStoredUser()
  router.push('/login')
}

watch(
  () => router.currentRoute.value.fullPath,
  () => syncPortalRole()
)

onMounted(async () => {
  document.documentElement.setAttribute('data-theme', theme.value)
  await Promise.all([loadCurrentUser(), loadUnread()])
  syncPortalRole()
  window.addEventListener('profile-updated', handleProfileUpdate)
  window.addEventListener('notice-updated', loadUnread)
})

onBeforeUnmount(() => {
  window.removeEventListener('profile-updated', handleProfileUpdate)
  window.removeEventListener('notice-updated', loadUnread)
})
</script>

<template>
  <div class="layout-shell">
    <div class="layout-noise"></div>
    <el-container class="layout-frame">
      <el-aside class="portal-aside" :width="collapsed ? '96px' : '312px'">
        <div class="brand-block">
          <button class="brand-mark" type="button" @click="goHome">{{ currentRoleMeta.mark }}</button>
          <div v-if="!collapsed" class="brand-copy">
            <div class="brand-eyebrow">Code Galaxy</div>
            <div class="brand-title">{{ currentRoleMeta.label }}</div>
          </div>
          <button class="collapse-btn" type="button" @click="toggleAside">{{ collapsed ? '→' : '←' }}</button>
        </div>

        <section class="role-panel" :class="{ 'is-collapsed': collapsed }">
          <div class="role-panel__eyebrow">当前门户</div>
          <div class="role-panel__title">{{ currentRoleMeta.label }}</div>
          <div v-if="!collapsed" class="role-panel__desc">{{ currentRoleMeta.description }}</div>
          <div v-if="availableRoles.length > 1" class="role-panel__switcher">
            <button
              v-for="role in availableRoles"
              :key="role"
              type="button"
              class="role-panel__switch"
              :class="{ 'is-active': activeRole === role }"
              @click="switchPortal(role)"
            >
              <span>{{ roleMeta[role]?.shortLabel || role }}</span>
            </button>
          </div>
        </section>

        <el-scrollbar class="aside-scroll">
          <el-menu
            :default-active="activeMenu"
            class="portal-menu"
            :collapse="collapsed"
            :collapse-transition="false"
            router
          >
            <template v-for="section in portalSections" :key="section.id">
              <div v-if="!collapsed" class="menu-section-title">{{ section.title }}</div>
              <el-menu-item v-for="item in section.items" :key="item.path" :index="item.path">
                <el-icon><component :is="iconMap[item.icon] || House" /></el-icon>
                <template #title>
                  <div class="menu-item-row">
                    <span>{{ item.label }}</span>
                    <el-badge v-if="item.badge" :value="item.badge" :max="99" />
                  </div>
                </template>
              </el-menu-item>
            </template>
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <el-container>
        <el-header class="portal-header">
          <div class="header-copy">
            <div class="header-eyebrow">{{ portalTitle.eyebrow }}</div>
            <div class="header-title display">{{ portalTitle.title }}</div>
            <div class="header-subtitle">{{ portalTitle.subtitle }}</div>
            <div class="header-chips">
              <span class="header-chip">{{ todayLabel }}</span>
              <span class="header-chip">当前主题：<strong>{{ activeTheme.label }}</strong></span>
              <span class="header-chip">未读通知 {{ unread }}</span>
            </div>
          </div>

          <div class="header-actions">
            <el-button class="home-btn" circle @click="goHome">
              <el-icon><Compass /></el-icon>
            </el-button>

            <el-select v-model="quickRoute" class="quick-jump" clearable placeholder="快速跳转" @change="jumpQuick">
              <el-option v-for="item in quickActions" :key="item.path" :label="item.label" :value="item.path" />
            </el-select>

            <div class="theme-switcher">
              <div class="theme-switcher__meta">
                <span>Theme</span>
                <strong>{{ activeTheme.label }}</strong>
                <em>{{ activeTheme.hint }}</em>
              </div>
              <div class="theme-switcher__options">
                <button
                  v-for="item in themeOptions"
                  :key="item.key"
                  type="button"
                  class="theme-switcher__option"
                  :class="{ 'is-active': theme === item.key }"
                  @click="applyTheme(item.key)"
                >
                  <span class="theme-switcher__swatch" :class="`is-${item.key}`"></span>
                  <span class="theme-switcher__name">{{ item.label }}</span>
                </button>
              </div>
            </div>

            <button class="user-card" type="button" @click="goProfile">
              <el-avatar :src="resolvedAvatar" :size="42">{{ userInitial }}</el-avatar>
              <div class="user-card__meta">
                <strong>{{ user.username || '同学' }}</strong>
                <span>{{ currentRoleMeta.label }}</span>
              </div>
            </button>

            <el-button class="logout-btn" circle @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
            </el-button>
          </div>
        </el-header>

        <el-main class="portal-main">
          <div class="content-panel">
            <router-view />
          </div>
        </el-main>
      </el-container>
    </el-container>

    <AiAssistant />
  </div>
</template>

<style scoped>
.layout-shell {
  min-height: 100vh;
  background: var(--ui-shell-backdrop);
  position: relative;
}

.layout-noise {
  position: absolute;
  inset: 0;
  background:
    var(--ui-shell-orb-1),
    var(--ui-shell-orb-2),
    var(--ui-shell-orb-3),
    linear-gradient(var(--ui-grid-line) 1px, transparent 1px),
    linear-gradient(90deg, var(--ui-grid-line) 1px, transparent 1px);
  background-size: auto, auto, auto, 32px 32px, 32px 32px;
  opacity: var(--ui-grid-opacity);
  pointer-events: none;
}

.layout-frame {
  position: relative;
  min-height: 100vh;
}

.portal-aside {
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr);
  gap: 18px;
  padding: 20px 18px 20px 20px;
  border-right: 1px solid var(--ui-aside-border);
  background: var(--ui-aside-bg);
  backdrop-filter: blur(18px);
}

.brand-block,
.role-panel,
.portal-header,
.content-panel {
  border: 1px solid var(--ui-border-soft);
  box-shadow: var(--ui-chip-shadow);
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 22px;
  background: color-mix(in srgb, var(--ui-surface) 88%, transparent);
}

.brand-mark {
  width: 52px;
  height: 52px;
  border: 1px solid var(--ui-brand-border);
  border-radius: 18px;
  background: var(--ui-brand-bg);
  color: var(--ui-brand-text);
  font: 700 24px/1 var(--font-display);
  cursor: pointer;
}

.brand-copy {
  display: grid;
}

.brand-eyebrow,
.menu-section-title,
.role-panel__eyebrow,
.header-eyebrow {
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--ui-aside-muted);
}

.brand-title,
.role-panel__title {
  color: var(--ui-aside-title);
  font-weight: 700;
}

.collapse-btn,
.role-panel__switch,
.user-card {
  background: transparent;
}

.collapse-btn {
  margin-left: auto;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  border: 1px solid var(--ui-border-soft);
  color: var(--ui-aside-title);
  cursor: pointer;
}

.role-panel {
  display: grid;
  gap: 10px;
  padding: 16px;
  border-radius: 24px;
  background: var(--ui-aside-card-bg);
}

.role-panel__desc {
  color: var(--ui-aside-body);
  font-size: 13px;
}

.role-panel__switcher {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.role-panel__switch {
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px solid var(--ui-border-soft);
  color: var(--ui-aside-title);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, background-color 0.2s ease;
}

.role-panel__switch:hover {
  transform: translateY(-1px);
  background: var(--ui-aside-hover-bg);
}

.role-panel__switch.is-active {
  border-color: color-mix(in srgb, var(--ui-accent) 60%, transparent);
  background: linear-gradient(135deg, color-mix(in srgb, var(--ui-accent) 18%, transparent), color-mix(in srgb, var(--ui-accent-2) 14%, transparent));
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--ui-accent) 20%, transparent) inset;
}

.role-panel.is-collapsed .role-panel__switcher {
  grid-template-columns: 1fr;
}

.aside-scroll {
  min-height: 0;
}

.portal-menu {
  border: none;
  background: transparent;
}

.menu-section-title {
  padding: 12px 12px 6px;
}

.menu-item-row {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.portal-header {
  margin: 20px 20px 0 10px;
  height: auto;
  min-height: 0;
  padding: 18px 20px;
  border-radius: 28px;
  background: color-mix(in srgb, var(--ui-surface) 86%, transparent);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.header-copy {
  display: grid;
  gap: 8px;
}

.header-title {
  font-size: clamp(36px, 5vw, 54px);
  line-height: 1.04;
  color: var(--ui-text);
}

.header-subtitle {
  max-width: 640px;
  color: var(--ui-text-muted);
}

.header-chips,
.header-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.header-chip,
.theme-switcher,
.user-card,
.home-btn,
.logout-btn {
  border-radius: 16px;
  border: 1px solid var(--ui-border-soft);
  background: color-mix(in srgb, var(--ui-surface) 90%, transparent);
}

.header-chip {
  padding: 8px 12px;
  color: var(--ui-text-muted);
}

.header-chip strong {
  color: var(--ui-accent);
}

.quick-jump {
  width: 176px;
}

.theme-switcher {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  padding: 10px 12px;
}

.theme-switcher__meta {
  display: grid;
  min-width: 116px;
}

.theme-switcher__meta span,
.theme-switcher__meta em {
  font-size: 11px;
  color: var(--ui-text-muted);
  font-style: normal;
}

.theme-switcher__meta strong {
  color: var(--ui-text);
}

.theme-switcher__options {
  display: flex;
  gap: 8px;
}

.theme-switcher__option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  min-width: 92px;
  border-radius: 14px;
  border: 1px solid transparent;
  color: var(--ui-text-muted);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, background-color 0.2s ease;
}

.theme-switcher__option:hover {
  transform: translateY(-1px);
  border-color: var(--ui-border);
  color: var(--ui-text);
}

.theme-switcher__option.is-active {
  border-color: color-mix(in srgb, var(--ui-accent) 60%, transparent);
  background: linear-gradient(135deg, color-mix(in srgb, var(--ui-accent) 14%, transparent), color-mix(in srgb, var(--ui-accent-2) 10%, transparent));
  color: var(--ui-text);
}

.theme-switcher__swatch {
  width: 16px;
  height: 16px;
  border-radius: 999px;
}

.theme-switcher__swatch.is-neon {
  background: linear-gradient(135deg, #5ef7c2, #38bdf8);
}

.theme-switcher__swatch.is-red {
  background: linear-gradient(135deg, #ff546f, #ffb454);
}

.theme-switcher__swatch.is-aurora {
  background: linear-gradient(135deg, #0ea5e9, #22c55e);
}

.theme-switcher__name {
  font-size: 12px;
  font-weight: 600;
}

.user-card {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  color: var(--ui-text);
  cursor: pointer;
}

.user-card__meta {
  display: grid;
  text-align: left;
}

.user-card__meta span {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.portal-main {
  padding: 10px 20px 20px 10px;
}

.content-panel {
  min-height: calc(100vh - 176px);
  padding: 20px;
  border-radius: 28px;
  background: var(--ui-content-bg);
  backdrop-filter: blur(14px);
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  min-height: 44px;
  margin: 4px 0;
  border-radius: 14px;
  color: var(--ui-aside-title);
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: var(--ui-aside-hover-bg);
}

:deep(.el-menu-item.is-active) {
  border: 1px solid color-mix(in srgb, var(--ui-accent) 42%, transparent);
  background: linear-gradient(135deg, color-mix(in srgb, var(--ui-accent) 16%, transparent), color-mix(in srgb, var(--ui-accent-2) 14%, transparent));
  box-shadow: 0 0 18px color-mix(in srgb, var(--ui-accent) 16%, transparent);
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 14px;
  background: var(--ui-chip-bg);
  box-shadow: 0 0 0 1px var(--ui-chip-border) inset;
}

@media (max-width: 1320px) {
  .portal-header {
    flex-direction: column;
  }

  .header-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 900px) {
  .layout-frame {
    display: block;
  }

  .portal-aside {
    width: 100% !important;
    grid-template-rows: auto;
  }

  .portal-main,
  .portal-header {
    margin-left: 20px;
  }

  .theme-switcher {
    flex-direction: column;
    align-items: stretch;
  }

  .theme-switcher__options {
    flex-wrap: wrap;
  }
}
</style>
