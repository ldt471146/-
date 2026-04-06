<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import {
  createTeacherCourse,
  deleteTeacherCourse,
  fetchTeacherCourses,
  updateTeacherCourse
} from '../api/teacher'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const courses = ref([])
const dialogOpen = ref(false)
const editingId = ref(null)
const keyword = ref('')
const statusFilter = ref('all')
const finishFilter = ref('all')
const form = ref({
  title: '',
  intro: '',
  cover: '',
  status: 1,
  finishStatus: 0
})

const normalizeUrl = (value) => {
  const raw = String(value || '').trim()
  if (!raw) return ''
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('//')) return `https:${raw}`
  return `https://${raw}`
}

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (part) => String(part).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const clipText = (value, max = 90) => {
  const raw = String(value || '').trim()
  if (!raw) return '暂未填写课程简介，建议补充学习目标与内容结构。'
  return raw.length > max ? `${raw.slice(0, max)}...` : raw
}

const getStatusLabel = (value) => (value === 1 ? '已上架' : '已下架')
const getStatusType = (value) => (value === 1 ? 'success' : 'warning')
const getFinishLabel = (value) => (value === 1 ? '已完结' : '更新中')
const getFinishType = (value) => (value === 1 ? 'success' : 'info')

const load = async () => {
  loading.value = true
  try {
    const response = await fetchTeacherCourses()
    courses.value = response.data || []
  } catch (exception) {
    ElNotification({
      title: '加载失败',
      message: exception?.message || '课程加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  editingId.value = null
  form.value = {
    title: '',
    intro: '',
    cover: '',
    status: 1,
    finishStatus: 0
  }
}

const openCreate = () => {
  resetForm()
  dialogOpen.value = true
}

const openEdit = (course) => {
  editingId.value = course.id
  form.value = {
    title: course.title || '',
    intro: course.intro || '',
    cover: course.cover || '',
    status: course.status ?? 1,
    finishStatus: course.finishStatus ?? 0
  }
  dialogOpen.value = true
}

const closeCourseDialog = () => {
  dialogOpen.value = false
}

const saveCourse = async () => {
  if (saving.value) return
  if (!form.value.title.trim()) {
    ElNotification({
      title: '请填写标题',
      message: '课程标题不能为空',
      type: 'warning',
      duration: 1500
    })
    return
  }

  try {
    saving.value = true
    const payload = {
      ...form.value,
      title: form.value.title.trim(),
      intro: form.value.intro?.trim() || '',
      cover: normalizeUrl(form.value.cover)
    }
    if (editingId.value) {
      await updateTeacherCourse(editingId.value, payload)
    } else {
      await createTeacherCourse(payload)
    }
    dialogOpen.value = false
    await load()
    ElNotification({
      title: '保存成功',
      message: editingId.value ? '课程信息已更新' : '课程已创建',
      type: 'success',
      duration: 1600
    })
  } catch (exception) {
    ElNotification({
      title: '保存失败',
      message: exception?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  } finally {
    saving.value = false
  }
}

const removeCourse = async (course) => {
  try {
    await deleteTeacherCourse(course.id)
    await load()
    ElNotification({
      title: '已删除',
      message: '课程已删除',
      type: 'success',
      duration: 1600
    })
  } catch (exception) {
    ElNotification({
      title: '删除失败',
      message: exception?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  }
}

const syncCourseStatus = async (course, patch) => {
  try {
    await updateTeacherCourse(course.id, {
      title: course.title,
      intro: course.intro,
      cover: course.cover,
      status: patch.status ?? course.status,
      finishStatus: patch.finishStatus ?? course.finishStatus
    })
    await load()
    ElNotification({
      title: '状态已更新',
      message: '课程状态已同步',
      type: 'success',
      duration: 1500
    })
  } catch (exception) {
    ElNotification({
      title: '状态更新失败',
      message: exception?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  }
}

const toggleStatus = (course) => {
  syncCourseStatus(course, { status: course.status === 1 ? 0 : 1 })
}

const toggleFinish = (course) => {
  syncCourseStatus(course, { finishStatus: course.finishStatus === 1 ? 0 : 1 })
}

const goDetail = (course) => {
  router.push(`/teacher/courses/${course.id}`)
}

const goQuestionBank = (courseId) => {
  if (courseId) {
    router.push({ path: '/teacher/questions', query: { courseId } })
    return
  }
  router.push('/teacher/questions')
}

const goHomework = (courseId) => {
  if (courseId) {
    router.push({ path: '/teacher/homework', query: { courseId } })
    return
  }
  router.push('/teacher/homework')
}
const goExams = () => router.push('/teacher/exams')
const goStats = () => router.push('/teacher/stats')

const filteredCourses = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  return [...courses.value]
    .filter((course) => {
      if (statusFilter.value !== 'all' && String(course.status) !== statusFilter.value) return false
      if (finishFilter.value !== 'all' && String(course.finishStatus) !== finishFilter.value) return false
      if (!query) return true
      return [course.title, course.intro]
        .filter(Boolean)
        .some((field) => String(field).toLowerCase().includes(query))
    })
    .sort((left, right) => new Date(right.updatedAt || right.createdAt || 0) - new Date(left.updatedAt || left.createdAt || 0))
})

const kpis = computed(() => {
  const totalLessons = courses.value.reduce((sum, course) => sum + (course.lessonCount || 0), 0)
  return [
    { label: '课程总数', value: courses.value.length, hint: '当前教师名下课程' },
    { label: '已上架', value: courses.value.filter((course) => course.status === 1).length, hint: '学生可见课程' },
    { label: '更新中', value: courses.value.filter((course) => course.finishStatus !== 1).length, hint: '仍在维护主线' },
    { label: '总课时', value: totalLessons, hint: '所有课程课时合计' }
  ]
})

onMounted(load)
</script>

<template>
  <div class="page">
    <section class="hero">
      <div class="hero-main">
        <div class="hero-eyebrow">Course Builder</div>
        <div class="hero-title display">教师课程工作台</div>
        <div class="hero-subtitle">
          以课程为中心组织章节、题库、作业与考试，把发布状态和内容编排放在一个更顺手的入口里。
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="openCreate">新建课程</el-button>
          <el-button @click="goQuestionBank">题库工作台</el-button>
          <el-button @click="goHomework">作业管理</el-button>
          <el-button @click="goExams">考试任务</el-button>
          <el-button @click="goStats">教学统计</el-button>
        </div>
      </div>

      <div class="hero-kpis">
        <div v-for="item in kpis" :key="item.label" class="kpi-card">
          <div class="kpi-label">{{ item.label }}</div>
          <div class="kpi-value">{{ item.value }}</div>
          <div class="kpi-hint">{{ item.hint }}</div>
        </div>
      </div>
    </section>

    <section class="workflow-grid">
      <button type="button" class="flow-card" @click="goQuestionBank">
        <div class="flow-title">题库联动</div>
        <div class="flow-copy">先搭课程结构，再去题库工作台补齐章节对应题目。</div>
      </button>
      <button type="button" class="flow-card" @click="goHomework">
        <div class="flow-title">作业发布</div>
        <div class="flow-copy">课程内容稳定后，直接发布作业做章节性训练。</div>
      </button>
      <button type="button" class="flow-card" @click="goExams">
        <div class="flow-title">考试测评</div>
        <div class="flow-copy">课程收尾后配置考试任务，形成完整教学闭环。</div>
      </button>
    </section>

    <el-card class="toolbar" shadow="never">
      <div class="toolbar-grid">
        <el-input v-model="keyword" clearable placeholder="搜索课程标题 / 简介" />
        <el-select v-model="statusFilter">
          <el-option label="全部上架状态" value="all" />
          <el-option label="已上架" value="1" />
          <el-option label="已下架" value="0" />
        </el-select>
        <el-select v-model="finishFilter">
          <el-option label="全部维护状态" value="all" />
          <el-option label="更新中" value="0" />
          <el-option label="已完结" value="1" />
        </el-select>
      </div>
    </el-card>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="course-grid">
          <div v-for="index in 4" :key="index" class="course-card skeleton-card"></div>
        </div>
      </template>

      <template #default>
        <div v-if="filteredCourses.length" class="course-grid">
          <el-card v-for="course in filteredCourses" :key="course.id" class="course-card" shadow="hover">
            <div class="cover">
              <img v-if="normalizeUrl(course.cover)" :src="normalizeUrl(course.cover)" class="cover-image" alt="course-cover" />
              <div class="cover-overlay"></div>
              <div class="cover-title">{{ course.title }}</div>
            </div>

            <div class="chip-row">
              <el-tag :type="getStatusType(course.status)">{{ getStatusLabel(course.status) }}</el-tag>
              <el-tag :type="getFinishType(course.finishStatus)">{{ getFinishLabel(course.finishStatus) }}</el-tag>
            </div>

            <div class="course-intro">{{ clipText(course.intro) }}</div>

            <div class="meta-grid">
              <div class="meta-block">
                <div class="meta-label">章节</div>
                <div class="meta-value">{{ course.chapterCount || 0 }}</div>
              </div>
              <div class="meta-block">
                <div class="meta-label">课时</div>
                <div class="meta-value">{{ course.lessonCount || 0 }}</div>
              </div>
              <div class="meta-block wide">
                <div class="meta-label">最近更新</div>
                <div class="meta-value small">{{ formatDate(course.updatedAt || course.createdAt) }}</div>
              </div>
            </div>

            <div class="action-row">
              <el-button type="primary" @click="goDetail(course)">编排课程</el-button>
              <el-button plain @click="goQuestionBank(course.id)">去题库</el-button>
              <el-button plain @click="openEdit(course)">编辑信息</el-button>
            </div>
            <div class="action-row secondary">
              <el-button plain @click="goHomework(course.id)">课程作业</el-button>
              <el-button plain @click="toggleStatus(course)">
                {{ course.status === 1 ? '下架课程' : '上架课程' }}
              </el-button>
              <el-button plain @click="toggleFinish(course)">
                {{ course.finishStatus === 1 ? '继续更新' : '标记完结' }}
              </el-button>
              <el-button plain class="danger-button" @click="removeCourse(course)">删除</el-button>
            </div>
          </el-card>
        </div>

        <el-card v-else class="empty-card" shadow="never">
          <el-empty description="还没有符合筛选条件的课程">
            <el-button type="primary" @click="openCreate">新建第一门课程</el-button>
          </el-empty>
        </el-card>
      </template>
    </el-skeleton>

    <el-dialog
      v-model="dialogOpen"
      :title="editingId ? '编辑课程' : '新建课程'"
      width="560px"
      top="6vh"
      class="course-dialog"
      :close-on-click-modal="false"
      append-to-body
      destroy-on-close
      @closed="closeCourseDialog"
    >
      <el-form label-position="top">
        <el-form-item label="课程标题">
          <el-input v-model="form.title" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="课程简介">
          <el-input v-model="form.intro" type="textarea" :rows="4" maxlength="300" show-word-limit />
        </el-form-item>
        <el-form-item label="封面链接">
          <el-input v-model="form.cover" placeholder="https://example.com/cover.png" />
        </el-form-item>
        <div class="dialog-grid">
          <el-form-item label="上架状态">
            <el-select v-model="form.status">
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="维护状态">
            <el-select v-model="form.finishStatus">
              <el-option label="更新中" :value="0" />
              <el-option label="已完结" :value="1" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCourse">保存课程</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 18px;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(280px, 0.9fr);
  gap: 16px;
  padding: 22px;
  border-radius: 24px;
  border: 1px solid var(--ui-border);
  background:
    radial-gradient(circle at 12% 10%, color-mix(in srgb, var(--ui-accent) 20%, transparent), transparent 28%),
    radial-gradient(circle at 88% 12%, color-mix(in srgb, var(--ui-accent-2) 18%, transparent), transparent 24%),
    var(--ui-content-bg);
  box-shadow: var(--ui-content-shadow);
}

.hero-main,
.hero-kpis,
.workflow-grid,
.course-grid,
.course-card,
.meta-grid {
  display: grid;
  gap: 14px;
}

.hero-eyebrow,
.hero-subtitle,
.kpi-label,
.kpi-hint,
.course-intro,
.meta-label {
  color: var(--ui-text-muted);
}

.hero-eyebrow {
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.hero-title {
  font-size: clamp(38px, 5vw, 52px);
  line-height: 0.96;
  color: var(--ui-text);
}

.hero-actions,
.action-row,
.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-kpis {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.kpi-card,
.flow-card,
.toolbar,
.course-card,
.empty-card {
  border-radius: 20px;
}

.kpi-card,
.flow-card,
.course-card {
  padding: 16px;
  border: 1px solid var(--ui-border-soft);
  background: color-mix(in srgb, var(--ui-surface) 94%, transparent);
}

.kpi-value,
.meta-value {
  font-weight: 700;
  color: var(--ui-text);
}

.kpi-value {
  font-size: 28px;
}

.workflow-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.flow-card {
  text-align: left;
  cursor: pointer;
}

.flow-title,
.cover-title {
  font-weight: 700;
  color: var(--ui-text);
}

.toolbar {
  border: 1px solid var(--ui-border);
  background: color-mix(in srgb, var(--ui-surface) 95%, transparent);
}

:deep(.toolbar .el-card__body) {
  padding: 16px 18px;
}

.toolbar-grid,
.dialog-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) 180px 180px;
  gap: 12px;
}

:deep(.course-dialog) {
  width: min(560px, calc(100vw - 24px)) !important;
  max-width: calc(100vw - 24px);
}

:deep(.course-dialog .el-dialog__body) {
  max-height: calc(100vh - 220px);
  overflow-y: auto;
  padding-top: 8px;
}

.course-grid {
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
}

.course-card {
  border: 1px solid var(--ui-border);
  background: color-mix(in srgb, var(--ui-surface) 96%, transparent);
}

:deep(.course-card .el-card__body) {
  display: grid;
  gap: 14px;
  padding: 16px;
}

.skeleton-card {
  min-height: 280px;
}

.cover {
  position: relative;
  min-height: 152px;
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(135deg, color-mix(in srgb, var(--ui-accent) 26%, transparent), color-mix(in srgb, var(--ui-accent-2) 24%, transparent));
}

.cover-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent, rgba(5, 10, 18, 0.76));
}

.cover-title {
  position: absolute;
  left: 14px;
  right: 14px;
  bottom: 14px;
  z-index: 1;
  font-size: 22px;
}

.meta-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.meta-block {
  padding: 12px;
  border-radius: 16px;
  border: 1px solid var(--ui-border-soft);
  background: color-mix(in srgb, var(--ui-surface-soft) 88%, transparent);
}

.meta-block.wide {
  grid-column: span 1;
}

.meta-value.small {
  font-size: 13px;
}

.action-row.secondary {
  border-top: 1px solid var(--ui-border-soft);
  padding-top: 12px;
}

.danger-button {
  color: #f97316;
  border-color: color-mix(in srgb, #f97316 50%, var(--ui-border));
}

.empty-card {
  border: 1px dashed var(--ui-border);
  background: color-mix(in srgb, var(--ui-surface) 94%, transparent);
}

@media (max-width: 1100px) {
  .hero,
  .workflow-grid,
  .toolbar-grid,
  .dialog-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .page {
    gap: 14px;
  }

  .hero {
    padding: 16px;
  }

  .hero-kpis,
  .meta-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
