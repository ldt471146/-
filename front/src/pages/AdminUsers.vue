<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElNotification } from 'element-plus'
import {
  fetchAdminUserOverview,
  fetchAdminUsers,
  updateAdminUserRoles,
  updateAdminUserStatus
} from '../api/admin'

const loading = ref(false)
const overview = ref({ totalUsers: 0, activeUsers: 0, disabledUsers: 0, mutedUsers: 0, multiRoleUsers: 0, studentUsers: 0, teacherUsers: 0, adminUsers: 0 })
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const status = ref('all')
const role = ref('all')
const roleDialogOpen = ref(false)
const currentUser = ref(null)
const selectedRoles = ref([])
const saving = ref(false)
const roleOptions = ['STUDENT', 'TEACHER', 'ADMIN']
const roleMeta = {
  STUDENT: { label: '学生', tone: '', desc: '默认学习身份' },
  TEACHER: { label: '教师', tone: 'success', desc: '负责课程与题库内容' },
  ADMIN: { label: '管理员', tone: 'danger', desc: '负责平台治理与运营' }
}

const summaryCards = computed(() => ([
  { label: '账号总量', value: overview.value.totalUsers, hint: `正常 ${overview.value.activeUsers}` },
  { label: '禁用账号', value: overview.value.disabledUsers, hint: `禁言 ${overview.value.mutedUsers}` },
  { label: '复合身份', value: overview.value.multiRoleUsers, hint: '拥有多个角色的账号' },
  {
    label: '当前筛选',
    value: role.value === 'all' ? '全部' : roleMeta[role.value]?.label || role.value,
    hint: status.value === 'all' ? '全部状态' : status.value === '1' ? '仅正常' : '仅禁用'
  }
]))

const roleCards = computed(() => ([
  { code: 'STUDENT', count: overview.value.studentUsers },
  { code: 'TEACHER', count: overview.value.teacherUsers },
  { code: 'ADMIN', count: overview.value.adminUsers }
]))

const queryParams = () => {
  const params = {}
  if (keyword.value.trim()) params.keyword = keyword.value.trim()
  if (status.value !== 'all') params.status = Number(status.value)
  if (role.value !== 'all') params.role = role.value
  return params
}

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (item) => String(item).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const roleLabel = (code) => roleMeta[code]?.label || code
const roleTone = (code) => roleMeta[code]?.tone || ''
const statusText = (row) => (row.status === 1 ? '正常' : '禁用')
const statusTone = (row) => (row.status === 1 ? 'success' : 'danger')

const load = async () => {
  loading.value = true
  try {
    const params = queryParams()
    const [listRes, overviewRes] = await Promise.all([
      fetchAdminUsers({ ...params, page: page.value, size: size.value }),
      fetchAdminUserOverview(params)
    ])
    const pageData = listRes.data || {}
    list.value = pageData.records || []
    total.value = pageData.total || 0
    overview.value = overviewRes.data || overview.value
  } catch (e) {
    ElNotification({ title: '加载失败', message: e?.message || '用户治理数据加载失败', type: 'error', duration: 2000 })
  } finally {
    loading.value = false
  }
}

const search = async () => {
  page.value = 1
  await load()
}

const resetFilters = async () => {
  keyword.value = ''
  status.value = 'all'
  role.value = 'all'
  await search()
}

const toggleRoleFilter = async (code) => {
  role.value = role.value === code ? 'all' : code
  await search()
}

const updateStatus = async (row, nextStatus) => {
  try {
    await updateAdminUserStatus(row.id, { status: nextStatus })
    ElNotification({
      title: '操作成功',
      message: nextStatus === 1 ? '用户已启用' : '用户已禁用',
      type: 'success',
      duration: 1600
    })
    await load()
  } catch (e) {
    ElNotification({ title: '操作失败', message: e?.message || '请稍后重试', type: 'error', duration: 2000 })
  }
}

const openRoleDialog = (row) => {
  currentUser.value = row
  selectedRoles.value = [...(row.roles || [])]
  roleDialogOpen.value = true
}

const saveRoles = async () => {
  if (!currentUser.value || saving.value) return
  if (!selectedRoles.value.length) {
    ElNotification({ title: '请选择角色', message: '至少保留一个角色', type: 'warning', duration: 1500 })
    return
  }
  saving.value = true
  try {
    await updateAdminUserRoles(currentUser.value.id, { roleCodes: selectedRoles.value })
    roleDialogOpen.value = false
    ElNotification({ title: '更新成功', message: '用户角色已更新', type: 'success', duration: 1600 })
    await load()
  } catch (e) {
    ElNotification({ title: '更新失败', message: e?.message || '请稍后重试', type: 'error', duration: 2000 })
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <section class="hero">
      <div class="hero-copy">
        <div class="eyebrow">管理员 · 用户治理</div>
        <div class="title display">账号与角色治理</div>
        <div class="subtitle">先看学生、教师、管理员的分布，再处理权限分配、账号禁用与复合身份账号。</div>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="search">刷新面板</el-button>
        <el-button @click="resetFilters">重置筛选</el-button>
      </div>
    </section>

    <section class="summary-grid">
      <article v-for="item in summaryCards" :key="item.label" class="summary-card">
        <div class="summary-label">{{ item.label }}</div>
        <div class="summary-value">{{ item.value }}</div>
        <div class="summary-hint">{{ item.hint }}</div>
      </article>
    </section>

    <section class="role-grid">
      <button v-for="item in roleCards" :key="item.code" type="button" class="role-card" :class="{ active: role === item.code }" @click="toggleRoleFilter(item.code)">
        <div>
          <div class="role-title">{{ roleLabel(item.code) }}</div>
          <div class="role-desc">{{ roleMeta[item.code].desc }}</div>
        </div>
        <div class="role-count">{{ item.count }}</div>
      </button>
    </section>

    <el-card class="toolbar" shadow="never">
      <div class="toolbar-grid">
        <el-input v-model="keyword" placeholder="搜索用户名 / 邮箱 / 手机号" clearable @keyup.enter="search" />
        <el-select v-model="role" @change="search">
          <el-option label="全部角色" value="all" />
          <el-option v-for="item in roleOptions" :key="item" :label="roleLabel(item)" :value="item" />
        </el-select>
        <el-select v-model="status" @change="search">
          <el-option label="全部状态" value="all" />
          <el-option label="正常" value="1" />
          <el-option label="禁用" value="0" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>
    </el-card>

    <el-table :data="list" v-loading="loading" class="table">
      <el-table-column label="用户" min-width="240">
        <template #default="{ row }">
          <div class="user-cell">
            <el-avatar :src="row.avatar" :size="44">{{ (row.username || '?').slice(0, 1) }}</el-avatar>
            <div>
              <div class="user-name">{{ row.username }}</div>
              <div class="user-meta">{{ row.email || '未绑定邮箱' }}</div>
              <div class="user-meta">{{ row.phone || '未填写手机号' }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="角色组合" min-width="220">
        <template #default="{ row }">
          <div class="role-list">
            <el-tag v-for="item in row.roles || []" :key="item" size="small" :type="roleTone(item)">{{ roleLabel(item) }}</el-tag>
          </div>
          <div class="user-meta">{{ (row.roles || []).length > 1 ? '复合身份账号' : '单一职责账号' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="账号状态" width="170">
        <template #default="{ row }">
          <div class="status-stack">
            <el-tag :type="statusTone(row)">{{ statusText(row) }}</el-tag>
            <el-tag v-if="row.muteStatus === 1" type="warning">禁言中</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="最近更新时间" min-width="160">
        <template #default="{ row }">{{ formatDate(row.updatedAt || row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openRoleDialog(row)">改角色</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="updateStatus(row, row.status === 1 ? 0 : 1)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, prev, pager, next, sizes" :current-page="page" :page-size="size" :page-sizes="[10, 20, 50]" :total="total" @current-change="(v) => { page = v; load() }" @size-change="(v) => { size = v; page = 1; load() }" />
    </div>

    <el-dialog v-model="roleDialogOpen" title="调整角色组合" width="460px" :close-on-click-modal="false" append-to-body destroy-on-close>
      <div class="dialog-body">
        <div class="dialog-user">{{ currentUser?.username || '-' }}（{{ currentUser?.email || '未绑定邮箱' }}）</div>
        <div class="dialog-tip">建议保持角色职责清晰；仅在确有跨端协同需求时再授予多角色。</div>
        <el-checkbox-group v-model="selectedRoles">
          <el-checkbox v-for="item in roleOptions" :key="item" :label="item">{{ roleLabel(item) }}</el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="roleDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page,.hero,.hero-copy,.summary-grid,.role-grid,.user-cell,.status-stack,.dialog-body{display:grid;gap:14px}
.page{gap:16px}.hero{grid-template-columns:minmax(0,1fr) auto;padding:22px;border:1px solid var(--ui-border);border-radius:24px;background:radial-gradient(circle at 12% 18%,color-mix(in srgb,var(--ui-accent) 16%,transparent),transparent 26%),radial-gradient(circle at 86% 18%,color-mix(in srgb,var(--ui-accent-2) 18%,transparent),transparent 22%),var(--ui-content-bg);box-shadow:var(--ui-content-shadow)}
.eyebrow,.subtitle,.summary-label,.summary-hint,.role-desc,.user-meta,.dialog-user,.dialog-tip{color:var(--ui-text-muted)}
.eyebrow{font-size:12px;letter-spacing:.16em;text-transform:uppercase}.title{font-size:clamp(30px,4vw,42px);line-height:.98;color:var(--ui-text)}
.hero-actions{display:flex;gap:10px;align-items:flex-start;flex-wrap:wrap}.summary-grid{grid-template-columns:repeat(4,minmax(0,1fr))}.summary-card,.role-card{padding:16px;border-radius:20px;border:1px solid var(--ui-border-soft);background:color-mix(in srgb,var(--ui-surface) 95%,transparent)}
.summary-value,.role-count,.user-name{font-weight:700;color:var(--ui-text)}.summary-value{font-size:28px}.role-grid{grid-template-columns:repeat(3,minmax(0,1fr))}.role-card{display:flex;justify-content:space-between;align-items:center;text-align:left;cursor:pointer}.role-card.active{border-color:var(--ui-accent);box-shadow:0 12px 28px rgba(0,0,0,.12)}
.role-title{font-size:16px;font-weight:700;color:var(--ui-text)}.role-count{font-size:26px}.toolbar{border:1px solid var(--ui-border)}.toolbar-grid{display:grid;grid-template-columns:1.3fr 180px 180px 100px;gap:10px}.table{border-radius:16px;overflow:hidden}.user-cell{grid-template-columns:auto 1fr;align-items:center;gap:12px}.role-list,.status-stack{display:flex;gap:6px;flex-wrap:wrap}.pager{display:flex;justify-content:flex-end}.dialog-tip{font-size:12px}
@media (max-width:1100px){.summary-grid,.role-grid,.hero{grid-template-columns:1fr}.toolbar-grid{grid-template-columns:1fr 1fr}}
@media (max-width:760px){.toolbar-grid{grid-template-columns:1fr}.page{gap:14px}.hero{padding:16px}}
</style>