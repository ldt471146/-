<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElNotification } from 'element-plus'
import { fetchAdminUsers, updateAdminUserRoles, updateAdminUserStatus } from '../api/admin'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const status = ref('all')

const roleDialogOpen = ref(false)
const currentUser = ref(null)
const selectedRoles = ref([])

const roleOptions = ['STUDENT', 'TEACHER', 'ADMIN']

const load = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (status.value !== 'all') params.status = Number(status.value)
    const res = await fetchAdminUsers(params)
    const data = res.data || {}
    list.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '用户列表加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const search = async () => {
  page.value = 1
  await load()
}

const updateStatus = async (row, nextStatus) => {
  try {
    await updateAdminUserStatus(row.id, { status: nextStatus })
    row.status = nextStatus
    ElNotification({
      title: '操作成功',
      message: nextStatus === 1 ? '用户已启用' : '用户已禁用',
      type: 'success',
      duration: 1600
    })
  } catch (e) {
    ElNotification({
      title: '操作失败',
      message: e?.message || '请稍后重试',
      type: 'error',
      duration: 2000
    })
  }
}

const openRoleDialog = (row) => {
  currentUser.value = row
  selectedRoles.value = [...(row.roles || [])]
  roleDialogOpen.value = true
}

const closeRoleDialog = () => {
  roleDialogOpen.value = false
}

const saveRoles = async () => {
  if (!currentUser.value || saving.value) return
  if (!selectedRoles.value.length) {
    ElNotification({
      title: '请选择角色',
      message: '至少保留一个角色',
      type: 'warning',
      duration: 1500
    })
    return
  }
  saving.value = true
  try {
    await updateAdminUserRoles(currentUser.value.id, { roleCodes: selectedRoles.value })
    currentUser.value.roles = [...selectedRoles.value]
    roleDialogOpen.value = false
    ElNotification({
      title: '更新成功',
      message: '用户角色已更新',
      type: 'success',
      duration: 1600
    })
  } catch (e) {
    ElNotification({
      title: '更新失败',
      message: e?.message || '请稍后重试',
      type: 'error',
      duration: 2000
    })
  } finally {
    saving.value = false
  }
}

const statusText = computed(() => (s) => (s === 1 ? '正常' : '禁用'))
const statusTag = computed(() => (s) => (s === 1 ? 'success' : 'danger'))

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">用户管理</div>
        <div class="subtitle">账号状态与角色权限治理</div>
      </div>
    </div>

    <el-card class="toolbar" shadow="never">
      <div class="toolbar-grid">
        <el-input v-model="keyword" placeholder="搜索用户名 / 邮箱 / 手机号" clearable @keyup.enter="search" />
        <el-select v-model="status" @change="search">
          <el-option label="全部状态" value="all" />
          <el-option label="正常" value="1" />
          <el-option label="禁用" value="0" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>
    </el-card>

    <el-table :data="list" v-loading="loading" class="table">
      <el-table-column label="用户名" prop="username" min-width="120" />
      <el-table-column label="邮箱" prop="email" min-width="180" />
      <el-table-column label="手机号" prop="phone" min-width="130" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="角色" min-width="160">
        <template #default="{ row }">
          <div class="role-list">
            <el-tag v-for="r in row.roles || []" :key="r" size="small">{{ r }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="openRoleDialog(row)">改角色</el-button>
          <el-button
            size="small"
            type="warning"
            v-if="row.status === 1"
            @click="updateStatus(row, 0)"
          >
            禁用
          </el-button>
          <el-button
            size="small"
            type="success"
            v-else
            @click="updateStatus(row, 1)"
          >
            启用
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        background
        layout="total, prev, pager, next, sizes"
        :current-page="page"
        :page-size="size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        @current-change="(v) => { page = v; load() }"
        @size-change="(v) => { size = v; page = 1; load() }"
      />
    </div>

    <el-dialog
      v-model="roleDialogOpen"
      title="调整用户角色"
      width="460px"
      :close-on-click-modal="false"
      :show-close="true"
      append-to-body
      destroy-on-close
    >
      <div class="dialog-body">
        <div class="dialog-user">
          {{ currentUser?.username || '-' }}（ID: {{ currentUser?.id || '-' }}）
        </div>
        <el-checkbox-group v-model="selectedRoles">
          <el-checkbox v-for="r in roleOptions" :key="r" :label="r">{{ r }}</el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="closeRoleDialog">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 14px;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 22px;
  font-weight: 700;
}

.subtitle {
  margin-top: 6px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.toolbar {
  border: 1px solid var(--ui-border);
}

.toolbar-grid {
  display: grid;
  grid-template-columns: 1fr 180px 100px;
  gap: 10px;
}

.table {
  border-radius: 12px;
  overflow: hidden;
}

.role-list {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.pager {
  display: flex;
  justify-content: flex-end;
}

.dialog-body {
  display: grid;
  gap: 10px;
}

.dialog-user {
  font-size: 13px;
  color: var(--ui-text-muted);
}

@media (max-width: 960px) {
  .toolbar-grid {
    grid-template-columns: 1fr;
  }
}
</style>

