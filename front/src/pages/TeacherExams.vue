<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElNotification } from 'element-plus'
import {
  createTeacherExamTask,
  deleteTeacherExamTask,
  fetchTeacherCourses,
  fetchTeacherExamTasks
} from '../api/teacher'
import http from '../api/http'

const loading = ref(false)
const saving = ref(false)
const tasks = ref([])
const courses = ref([])
const chapters = ref([])
const open = ref(false)
const form = ref({
  title: '',
  courseId: '',
  chapterId: '',
  questionCount: 10,
  durationMinutes: 30,
  startTime: '',
  endTime: ''
})

const loadCourses = async () => {
  const res = await fetchTeacherCourses()
  courses.value = res.data || []
}

const loadTasks = async () => {
  const res = await fetchTeacherExamTasks()
  tasks.value = res.data || []
}

const load = async () => {
  loading.value = true
  try {
    await Promise.all([loadCourses(), loadTasks()])
  } finally {
    loading.value = false
  }
}

const loadChapters = async () => {
  if (!form.value.courseId) {
    chapters.value = []
    form.value.chapterId = ''
    return
  }
  const res = await http.get(`/api/teacher/courses/${form.value.courseId}`)
  chapters.value = res.data?.chapters || []
}

const openCreate = () => {
  form.value = {
    title: '',
    courseId: '',
    chapterId: '',
    questionCount: 10,
    durationMinutes: 30,
    startTime: '',
    endTime: ''
  }
  chapters.value = []
  open.value = true
}

const closeCreate = () => {
  open.value = false
}

const submitCreate = async () => {
  if (!form.value.title || !form.value.courseId) {
    ElNotification({ title: '请填写必填项', type: 'warning', duration: 1600 })
    return
  }
  saving.value = true
  try {
    await createTeacherExamTask({
      title: form.value.title,
      courseId: Number(form.value.courseId),
      chapterId: form.value.chapterId ? Number(form.value.chapterId) : null,
      questionCount: Number(form.value.questionCount || 10),
      durationMinutes: Number(form.value.durationMinutes || 30),
      startTime: form.value.startTime || null,
      endTime: form.value.endTime || null
    })
    open.value = false
    await loadTasks()
    ElNotification({ title: '发布成功', message: '考试任务已创建', type: 'success', duration: 1600 })
  } catch (e) {
    ElNotification({ title: '发布失败', message: e?.message || '请稍后再试', type: 'error', duration: 2000 })
  } finally {
    saving.value = false
  }
}

const removeTask = async (row) => {
  try {
    await deleteTeacherExamTask(row.id)
    await loadTasks()
    ElNotification({ title: '删除成功', type: 'success', duration: 1600 })
  } catch (e) {
    ElNotification({ title: '删除失败', message: e?.message || '请稍后再试', type: 'error', duration: 2000 })
  }
}

const fmt = (v) => {
  if (!v) return '-'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return '-'
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const statusText = computed(() => (s) => (s === 2 ? '已结束' : s === 1 ? '进行中' : '未发布'))

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">考试任务管理</div>
        <div class="subtitle">发布考试 · 指定时长 · 管理任务</div>
      </div>
      <el-button type="primary" @click="openCreate">新建考试任务</el-button>
    </div>

    <el-table v-loading="loading" :data="tasks" class="table">
      <el-table-column prop="title" label="考试标题" min-width="180" />
      <el-table-column prop="courseTitle" label="课程" min-width="150" />
      <el-table-column prop="chapterTitle" label="章节" min-width="140" />
      <el-table-column prop="questionCount" label="题数" width="80" />
      <el-table-column prop="durationMinutes" label="时长(分钟)" width="110" />
      <el-table-column label="时间窗口" min-width="260">
        <template #default="{ row }">
          <div>{{ fmt(row.startTime) }} ~ {{ fmt(row.endTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 2 ? 'info' : 'success'">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="removeTask(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="open" title="新建考试任务" width="560px" :show-close="true" append-to-body destroy-on-close>
      <el-form label-width="94px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="例如：第一章阶段测验" />
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="form.courseId" placeholder="选择课程" @change="loadChapters">
            <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="章节">
          <el-select v-model="form.chapterId" placeholder="可选，不选表示全课程" clearable>
            <el-option v-for="ch in chapters" :key="ch.id" :label="ch.title" :value="ch.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目数量">
          <el-input-number v-model="form.questionCount" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="考试时长">
          <el-input-number v-model="form.durationMinutes" :min="5" :max="180" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="不填默认立即开始" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="可不填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeCreate">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">发布</el-button>
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
.table {
  border-radius: 12px;
  overflow: hidden;
}
</style>

