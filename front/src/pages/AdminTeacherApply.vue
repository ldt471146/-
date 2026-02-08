<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchTeacherApplyList, reviewTeacherApply } from '../api/admin'
import { ElNotification } from 'element-plus'

const loading = ref(false)
const list = ref([])
const tab = ref('pending')
const remark = ref('')
const dialogOpen = ref(false)
const currentId = ref(null)
const reviewStatus = ref(1)
const submitting = ref(false)

const load = async () => {
  loading.value = true
  try {
    const status = tab.value === 'pending' ? 0 : tab.value === 'approved' ? 1 : 2
    const res = await fetchTeacherApplyList({ status })
    list.value = res.data || []
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '申请列表加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const openReview = (row, status) => {
  currentId.value = row.id
  reviewStatus.value = status
  remark.value = ''
  dialogOpen.value = true
}

const submitReview = async () => {
  if (submitting.value) return
  try {
    submitting.value = true
    await reviewTeacherApply(currentId.value, {
      status: reviewStatus.value,
      remark: remark.value
    })
    dialogOpen.value = false
    await load()
    ElNotification({
      title: '已处理',
      message: reviewStatus.value === 1 ? '已通过' : '已拒绝',
      type: 'success',
      duration: 1600
    })
  } catch (e) {
    ElNotification({
      title: '处理失败',
      message: e?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  } finally {
    submitting.value = false
  }
}

const closeDialog = () => {
  dialogOpen.value = false
}

const statusLabel = (s) => (s === 1 ? '已通过' : s === 2 ? '已拒绝' : '待审核')
const statusTag = (s) => (s === 1 ? 'success' : s === 2 ? 'danger' : 'warning')

const emptyText = computed(() => (tab.value === 'pending' ? '暂无待审核申请' : '暂无数据'))

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">教师申请审核</div>
        <div class="subtitle">审核新注册教师账号</div>
      </div>
      <div class="tab-wrap">
        <el-segmented
          v-model="tab"
          :options="[
            { label: '待审核', value: 'pending' },
            { label: '已通过', value: 'approved' },
            { label: '已拒绝', value: 'rejected' }
          ]"
          @change="load"
        />
      </div>
    </div>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-card"></div>
      </template>
      <template #default>
        <div v-if="list.length" class="grid">
          <div v-for="item in list" :key="item.id" class="card">
            <div class="card-left">
              <div class="badge">{{ statusLabel(item.status) }}</div>
              <div class="card-title">{{ item.username }} · {{ item.email }}</div>
              <div class="card-meta">用户ID：{{ item.userId }} · 申请ID：{{ item.id }}</div>
              <div class="card-meta">备注：{{ item.remark || '无' }}</div>
            </div>
            <div class="card-right">
              <el-tag :type="statusTag(item.status)">{{ statusLabel(item.status) }}</el-tag>
              <div class="actions" v-if="tab === 'pending'">
                <el-button native-type="button" size="small" type="success" @click="openReview(item, 1)">通过</el-button>
                <el-button native-type="button" size="small" type="danger" @click="openReview(item, 2)">拒绝</el-button>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else :description="emptyText" />
      </template>
    </el-skeleton>

    <el-dialog
      v-model="dialogOpen"
      title="审核申请"
      width="520px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="true"
      append-to-body
      destroy-on-close
    >
      <div class="dialog-body">
        <div class="dialog-label">
          审核结果：{{ reviewStatus === 1 ? '通过' : '拒绝' }}
        </div>
        <el-input v-model="remark" placeholder="可填写审核备注" type="textarea" :rows="3" />
      </div>
      <template #footer>
        <el-button native-type="button" @click="closeDialog">取消</el-button>
        <el-button native-type="button" type="primary" :loading="submitting" @click="submitReview">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 22px;
  font-weight: 600;
}

.subtitle {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 6px;
}

.grid {
  display: grid;
  gap: 14px;
}

.card {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 16px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.card-left {
  display: grid;
  gap: 6px;
}

.badge {
  font-size: 10px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--ui-text-muted);
}

.card-title {
  font-weight: 600;
  font-size: 15px;
}

.card-meta {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.card-right {
  display: grid;
  gap: 8px;
  justify-items: end;
}

.actions {
  display: flex;
  gap: 8px;
}

.dialog-body {
  display: grid;
  gap: 10px;
}

.dialog-label {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.skeleton-card {
  height: 180px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
}
</style>
