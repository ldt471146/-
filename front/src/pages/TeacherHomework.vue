<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { ElMessageBox, ElNotification } from 'element-plus'
import { fetchTeacherCourses, fetchTeacherQuestions } from '../api/teacher'
import {
  createTeacherHomework,
  updateTeacherHomework,
  deleteTeacherHomework,
  fetchTeacherHomework,
  fetchTeacherHomeworkDetail
} from '../api/homework'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const courses = ref([])
const queryCourseId = ref(null)

const formOpen = ref(false)
const editingId = ref(null)
const suppressCourseWatch = ref(false)
const form = ref({
  courseId: null,
  title: '',
  deadline: '',
  problems: []
})

const questions = ref([])
const questionLoading = ref(false)

const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

const fmt = (v) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '-')

const courseMap = computed(() => {
  const map = {}
  for (const c of courses.value) map[c.id] = c.title
  return map
})

const loadBase = async () => {
  try {
    const [courseRes, listRes] = await Promise.all([
      fetchTeacherCourses(),
      fetchTeacherHomework({ courseId: queryCourseId.value || undefined })
    ])
    courses.value = courseRes.data || []
    list.value = listRes.data || []
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '作业数据加载失败',
      type: 'error',
      duration: 2000
    })
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await fetchTeacherHomework({ courseId: queryCourseId.value || undefined })
    list.value = res.data || []
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '作业列表加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const addProblem = () => {
  form.value.problems.push({ problemId: null, score: 100 })
}

const removeProblem = (idx) => {
  form.value.problems.splice(idx, 1)
}

const openCreate = () => {
  editingId.value = null
  form.value = {
    courseId: null,
    title: '',
    deadline: '',
    problems: [{ problemId: null, score: 100 }]
  }
  questions.value = []
  formOpen.value = true
}

const loadQuestions = async (courseId) => {
  if (!courseId) {
    questions.value = []
    return
  }
  questionLoading.value = true
  try {
    const res = await fetchTeacherQuestions({ courseId, page: 1, size: 300 })
    questions.value = res.data?.records || []
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '题库加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    questionLoading.value = false
  }
}

watch(() => form.value.courseId, (id) => {
  if (!id) {
    questions.value = []
    return
  }
  if (!suppressCourseWatch.value) {
    form.value.problems = form.value.problems.map((x) => ({ ...x, problemId: null }))
  }
  suppressCourseWatch.value = false
  loadQuestions(id)
})

const submitForm = async () => {
  const payload = {
    courseId: form.value.courseId,
    title: form.value.title?.trim(),
    deadline: form.value.deadline || null,
    problems: form.value.problems
      .filter((x) => x.problemId)
      .map((x) => ({ problemId: x.problemId, score: Number(x.score || 100) }))
  }
  if (!payload.courseId || !payload.title) {
    ElNotification({ title: '请补全信息', message: '课程和标题不能为空', type: 'warning', duration: 1500 })
    return
  }
  if (!payload.problems.length) {
    ElNotification({ title: '请至少添加一题', message: '作业需包含题目', type: 'warning', duration: 1500 })
    return
  }
  submitting.value = true
  try {
    if (editingId.value) {
      await updateTeacherHomework(editingId.value, payload)
    } else {
      await createTeacherHomework(payload)
    }
    formOpen.value = false
    await loadList()
    ElNotification({
      title: editingId.value ? '更新成功' : '创建成功',
      message: editingId.value ? '作业已更新' : '作业已发布',
      type: 'success',
      duration: 1600
    })
  } catch (e) {
    ElNotification({
      title: editingId.value ? '更新失败' : '创建失败',
      message: e?.message || '请稍后重试',
      type: 'error',
      duration: 2000
    })
  } finally {
    submitting.value = false
  }
}

const openEdit = async (row) => {
  formOpen.value = true
  submitting.value = false
  editingId.value = row.id
  try {
    const res = await fetchTeacherHomeworkDetail(row.id)
    const data = res.data || {}
    suppressCourseWatch.value = true
    await loadQuestions(data.courseId)
    form.value = {
      courseId: data.courseId || null,
      title: data.title || '',
      deadline: data.deadline || '',
      problems: (data.problems || []).map((p) => ({
        problemId: p.problemId,
        score: p.score || 100
      }))
    }
    if (!form.value.problems.length) {
      form.value.problems = [{ problemId: null, score: 100 }]
    }
  } catch (e) {
    formOpen.value = false
    ElNotification({
      title: '加载失败',
      message: e?.message || '作业详情加载失败',
      type: 'error',
      duration: 2000
    })
  }
}

const openDetail = async (row) => {
  detailOpen.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await fetchTeacherHomeworkDetail(row.id)
    detail.value = res.data || null
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '详情加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    detailLoading.value = false
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除作业「${row.title}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteTeacherHomework(row.id)
    await loadList()
    ElNotification({ title: '删除成功', message: '作业已删除', type: 'success', duration: 1500 })
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElNotification({ title: '删除失败', message: e?.message || '请稍后重试', type: 'error', duration: 2000 })
    }
  }
}

onMounted(async () => {
  loading.value = true
  await loadBase()
  loading.value = false
})
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">作业管理</div>
        <div class="subtitle">按课程发布作业并分配题目分值，支持截止时间管理</div>
      </div>
      <el-button type="primary" @click="openCreate">新建作业</el-button>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="queryCourseId" clearable placeholder="按课程筛选" @change="loadList">
          <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
        </el-select>
      </div>
      <el-table v-loading="loading" :data="list">
        <el-table-column prop="title" label="作业标题" min-width="220" />
        <el-table-column label="课程" min-width="160">
          <template #default="{ row }">{{ row.courseTitle || courseMap[row.courseId] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="questionCount" label="题量" width="90" />
        <el-table-column prop="totalScore" label="总分" width="90" />
        <el-table-column label="截止时间" min-width="170">
          <template #default="{ row }">{{ fmt(row.deadline) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button size="small" type="primary" plain @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !list.length" description="暂无作业，点击右上角新建" />
    </el-card>

    <el-dialog v-model="formOpen" :title="editingId ? '编辑作业' : '新建作业'" width="760px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="课程">
          <el-select v-model="form.courseId" placeholder="请选择课程">
            <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="例如：第一章基础练习" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="form.deadline"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="可选，不限时可留空"
          />
        </el-form-item>
        <el-form-item label="题目配置">
          <div class="problems">
            <div v-for="(p, idx) in form.problems" :key="idx" class="problem-row">
              <el-select
                v-model="p.problemId"
                filterable
                clearable
                class="problem-select"
                :loading="questionLoading"
                placeholder="选择题目"
              >
                <el-option v-for="q in questions" :key="q.id" :label="q.title" :value="q.id" />
              </el-select>
              <el-input-number v-model="p.score" :min="1" :max="300" />
              <el-button type="danger" plain @click="removeProblem(idx)">删除</el-button>
            </div>
            <el-button plain @click="addProblem">+ 添加题目</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formOpen = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">{{ editingId ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailOpen" title="作业详情" size="760px" destroy-on-close>
      <el-skeleton :loading="detailLoading" animated>
        <template #template>
          <div class="skeleton"></div>
        </template>
        <template #default>
          <div v-if="detail" class="detail">
            <div class="meta-grid">
              <div class="meta-item">
                <div class="meta-k">课程</div>
                <div class="meta-v">{{ detail.courseTitle }}</div>
              </div>
              <div class="meta-item">
                <div class="meta-k">截止时间</div>
                <div class="meta-v">{{ fmt(detail.deadline) }}</div>
              </div>
              <div class="meta-item">
                <div class="meta-k">题量</div>
                <div class="meta-v">{{ detail.questionCount || 0 }}</div>
              </div>
              <div class="meta-item">
                <div class="meta-k">总分</div>
                <div class="meta-v">{{ detail.totalScore || 0 }}</div>
              </div>
            </div>
            <el-table :data="detail.problems || []">
              <el-table-column prop="title" label="题目" min-width="240" />
              <el-table-column prop="difficulty" label="难度" width="90" />
              <el-table-column prop="score" label="分值" width="90" />
            </el-table>
          </div>
        </template>
      </el-skeleton>
    </el-drawer>
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
  gap: 10px;
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
  margin-bottom: 10px;
  display: flex;
  justify-content: flex-end;
}

.problems {
  width: 100%;
  display: grid;
  gap: 10px;
}

.problem-row {
  display: grid;
  grid-template-columns: 1fr 120px 90px;
  gap: 10px;
}

.problem-select {
  width: 100%;
}

.detail {
  display: grid;
  gap: 12px;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.meta-item {
  border: 1px solid var(--ui-border-soft);
  border-radius: 10px;
  padding: 10px;
  background: var(--ui-surface-soft);
}

.meta-k {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.meta-v {
  margin-top: 4px;
  font-weight: 700;
}

.skeleton {
  height: 340px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
}

@media (max-width: 960px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }
  .problem-row {
    grid-template-columns: 1fr;
  }
  .meta-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
