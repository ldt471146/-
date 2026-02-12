<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElNotification } from 'element-plus'
import { fetchAdminCourses, reviewAdminCourse } from '../api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const status = ref('all')

const reviewDialogOpen = ref(false)
const current = ref(null)
const reviewStatus = ref(1)
const reviewRemark = ref('')
const submitting = ref(false)

const load = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (status.value !== 'all') params.status = Number(status.value)
    const res = await fetchAdminCourses(params)
    const data = res.data || {}
    list.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '课程列表加载失败',
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

const openReview = (row, targetStatus) => {
  current.value = row
  reviewStatus.value = targetStatus
  reviewRemark.value = ''
  reviewDialogOpen.value = true
}

const closeReview = () => {
  reviewDialogOpen.value = false
}

const submitReview = async () => {
  if (!current.value || submitting.value) return
  submitting.value = true
  try {
    await reviewAdminCourse(current.value.id, {
      status: reviewStatus.value,
      remark: reviewRemark.value
    })
    current.value.status = reviewStatus.value
    reviewDialogOpen.value = false
    ElNotification({
      title: '审核成功',
      message: reviewStatus.value === 1 ? '课程已通过并上架' : '课程已下架',
      type: 'success',
      duration: 1600
    })
  } catch (e) {
    ElNotification({
      title: '审核失败',
      message: e?.message || '请稍后重试',
      type: 'error',
      duration: 2000
    })
  } finally {
    submitting.value = false
  }
}

const statusText = computed(() => (v) => (v === 1 ? '已上架' : '已下架'))
const statusTag = computed(() => (v) => (v === 1 ? 'success' : 'info'))

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">课程审核</div>
        <div class="subtitle">管理员审核课程发布状态与可见性</div>
      </div>
    </div>

    <el-card class="toolbar" shadow="never">
      <div class="toolbar-grid">
        <el-input v-model="keyword" placeholder="搜索课程标题" clearable @keyup.enter="search" />
        <el-select v-model="status" @change="search">
          <el-option label="全部状态" value="all" />
          <el-option label="已上架" value="1" />
          <el-option label="已下架" value="0" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>
    </el-card>

    <el-table :data="list" v-loading="loading" class="table">
      <el-table-column label="课程标题" prop="title" min-width="180" />
      <el-table-column label="教师" prop="teacherName" min-width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="进度" width="100">
        <template #default="{ row }">
          <el-tag :type="row.finishStatus === 1 ? 'success' : 'warning'">
            {{ row.finishStatus === 1 ? '完结' : '更新中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="openReview(row, 1)">通过上架</el-button>
          <el-button size="small" type="warning" @click="openReview(row, 0)">下架</el-button>
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
      v-model="reviewDialogOpen"
      title="课程审核"
      width="500px"
      :close-on-click-modal="false"
      append-to-body
      destroy-on-close
    >
      <div class="dialog-body">
        <div class="dialog-title">{{ current?.title || '-' }}</div>
        <div class="dialog-sub">教师：{{ current?.teacherName || '-' }}</div>
        <el-input
          v-model="reviewRemark"
          type="textarea"
          :rows="3"
          placeholder="可填写审核备注（当前版本仅记录前端说明）"
        />
      </div>
      <template #footer>
        <el-button @click="closeReview">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReview">确认</el-button>
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

.pager {
  display: flex;
  justify-content: flex-end;
}

.dialog-body {
  display: grid;
  gap: 8px;
}

.dialog-title {
  font-weight: 700;
}

.dialog-sub {
  font-size: 12px;
  color: var(--ui-text-muted);
}

@media (max-width: 960px) {
  .toolbar-grid {
    grid-template-columns: 1fr;
  }
}
</style>

