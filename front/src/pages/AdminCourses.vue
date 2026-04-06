<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElNotification } from 'element-plus'
import { fetchAdminCourseOverview, fetchAdminCourses, reviewAdminCourse } from '../api/admin'

const loading = ref(false)
const overview = ref({ totalCourses: 0, publishedCourses: 0, unpublishedCourses: 0, finishedCourses: 0, updatingCourses: 0, teacherCount: 0 })
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

const summaryCards = computed(() => ([
  { label: '课程总量', value: overview.value.totalCourses, hint: `教师覆盖 ${overview.value.teacherCount}` },
  { label: '已上架', value: overview.value.publishedCourses, hint: `下架 ${overview.value.unpublishedCourses}` },
  { label: '已结课', value: overview.value.finishedCourses, hint: `更新中 ${overview.value.updatingCourses}` },
  {
    label: '当前筛选',
    value: status.value === 'all' ? '全部' : status.value === '1' ? '上架中' : '已下架',
    hint: keyword.value.trim() ? `关键词：${keyword.value.trim()}` : '无关键词筛选'
  }
]))

const statusCards = computed(() => ([
  { value: 'all', label: '全部课程', count: overview.value.totalCourses, desc: '查看完整课程治理面板' },
  { value: '1', label: '上架课程', count: overview.value.publishedCourses, desc: '已对学生开放学习入口' },
  { value: '0', label: '下架课程', count: overview.value.unpublishedCourses, desc: '等待调整后重新开放' }
]))

const statusText = (value) => (value === 1 ? '已上架' : '已下架')
const statusTone = (value) => (value === 1 ? 'success' : 'info')
const finishText = (value) => (value === 1 ? '已结课' : '更新中')
const finishTone = (value) => (value === 1 ? 'success' : 'warning')

const queryParams = () => {
  const params = { page: page.value, size: size.value }
  if (keyword.value.trim()) params.keyword = keyword.value.trim()
  if (status.value !== 'all') params.status = Number(status.value)
  return params
}

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (item) => String(item).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const load = async () => {
  loading.value = true
  try {
    const [listRes, overviewRes] = await Promise.all([
      fetchAdminCourses(queryParams()),
      fetchAdminCourseOverview({ keyword: keyword.value.trim() || undefined })
    ])
    const pageData = listRes.data || {}
    list.value = pageData.records || []
    total.value = pageData.total || 0
    overview.value = overviewRes.data || overview.value
  } catch (e) {
    ElNotification({ title: '加载失败', message: e?.message || '课程治理数据加载失败', type: 'error', duration: 2000 })
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
  await search()
}

const applyStatus = async (value) => {
  status.value = value
  await search()
}

const openReview = (row, targetStatus) => {
  current.value = row
  reviewStatus.value = targetStatus
  reviewRemark.value = targetStatus === 1 ? '课程符合发布要求' : '课程暂不满足开放条件'
  reviewDialogOpen.value = true
}

const submitReview = async () => {
  if (!current.value || submitting.value) return
  submitting.value = true
  try {
    await reviewAdminCourse(current.value.id, { status: reviewStatus.value, remark: reviewRemark.value })
    reviewDialogOpen.value = false
    ElNotification({
      title: '审核成功',
      message: reviewStatus.value === 1 ? '课程已通过并上架' : '课程已下架',
      type: 'success',
      duration: 1600
    })
    await load()
  } catch (e) {
    ElNotification({ title: '审核失败', message: e?.message || '请稍后重试', type: 'error', duration: 2000 })
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <section class="hero">
      <div class="hero-copy">
        <div class="eyebrow">管理员 · 课程治理</div>
        <div class="title display">课程审核与治理</div>
        <div class="subtitle">先看上架、下架、结课与教师覆盖，再进入具体课程审核，避免只靠表格逐行巡检。</div>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="search">刷新工作台</el-button>
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

    <section class="status-grid">
      <button v-for="item in statusCards" :key="item.value" type="button" class="status-card" :class="{ active: status === item.value }" @click="applyStatus(item.value)">
        <div>
          <div class="status-title">{{ item.label }}</div>
          <div class="status-desc">{{ item.desc }}</div>
        </div>
        <div class="status-count">{{ item.count }}</div>
      </button>
    </section>

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
      <el-table-column label="课程信息" min-width="260">
        <template #default="{ row }">
          <div class="course-cell">
            <div class="course-title">{{ row.title }}</div>
            <div class="course-meta">教师：{{ row.teacherName || '-' }}</div>
            <div class="course-meta">最近更新：{{ formatDate(row.updatedAt || row.createdAt) }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="发布状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTone(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="课程进度" width="110">
        <template #default="{ row }">
          <el-tag :type="finishTone(row.finishStatus)">{{ finishText(row.finishStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="openReview(row, 1)">通过上架</el-button>
          <el-button size="small" type="warning" @click="openReview(row, 0)">下架课程</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, prev, pager, next, sizes" :current-page="page" :page-size="size" :page-sizes="[10, 20, 50]" :total="total" @current-change="(v) => { page = v; load() }" @size-change="(v) => { size = v; page = 1; load() }" />
    </div>

    <el-dialog v-model="reviewDialogOpen" title="课程审核" width="520px" :close-on-click-modal="false" append-to-body destroy-on-close>
      <div class="dialog-body">
        <div class="dialog-title">{{ current?.title || '-' }}</div>
        <div class="dialog-sub">教师：{{ current?.teacherName || '-' }}</div>
        <el-input v-model="reviewRemark" type="textarea" :rows="4" placeholder="填写审核备注，帮助教师理解治理原因" />
      </div>
      <template #footer>
        <el-button @click="reviewDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReview">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page,.hero,.hero-copy,.summary-grid,.status-grid,.course-cell,.dialog-body{display:grid;gap:14px}
.page{gap:16px}.hero{grid-template-columns:minmax(0,1fr) auto;padding:22px;border-radius:24px;border:1px solid var(--ui-border);background:radial-gradient(circle at 12% 16%,color-mix(in srgb,var(--ui-accent) 16%,transparent),transparent 24%),radial-gradient(circle at 88% 16%,color-mix(in srgb,var(--ui-accent-2) 18%,transparent),transparent 22%),var(--ui-content-bg);box-shadow:var(--ui-content-shadow)}
.eyebrow,.subtitle,.summary-label,.summary-hint,.status-desc,.course-meta,.dialog-sub{color:var(--ui-text-muted)}.eyebrow{font-size:12px;letter-spacing:.16em;text-transform:uppercase}.title{font-size:clamp(30px,4vw,42px);line-height:.98;color:var(--ui-text)}.hero-actions{display:flex;gap:10px;align-items:flex-start;flex-wrap:wrap}
.summary-grid{grid-template-columns:repeat(4,minmax(0,1fr))}.summary-card,.status-card{padding:16px;border-radius:20px;border:1px solid var(--ui-border-soft);background:color-mix(in srgb,var(--ui-surface) 95%,transparent)}.summary-value,.status-count,.course-title,.dialog-title{font-weight:700;color:var(--ui-text)}.summary-value{font-size:28px}
.status-grid{grid-template-columns:repeat(3,minmax(0,1fr))}.status-card{display:flex;justify-content:space-between;align-items:center;text-align:left;cursor:pointer}.status-card.active{border-color:var(--ui-accent);box-shadow:0 12px 28px rgba(0,0,0,.12)}.status-title{font-size:16px;font-weight:700;color:var(--ui-text)}.status-count{font-size:26px}
.toolbar{border:1px solid var(--ui-border)}.toolbar-grid{display:grid;grid-template-columns:1fr 180px 100px;gap:10px}.table{border-radius:16px;overflow:hidden}.pager{display:flex;justify-content:flex-end}
@media (max-width:1100px){.summary-grid,.status-grid,.hero{grid-template-columns:1fr}} @media (max-width:760px){.toolbar-grid{grid-template-columns:1fr}.page{gap:14px}.hero{padding:16px}}
</style>