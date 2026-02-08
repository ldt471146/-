<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import http from '../api/http'
import { createMockExam, fetchExamTasks, fetchMyExamSubmissions, startExamTask, submitMockExam } from '../api/exam'
import { ElNotification } from 'element-plus'

const courses = ref([])
const chapters = ref([])
const loading = ref(false)
const creating = ref(false)
const submitting = ref(false)
const mode = ref('mock')
const taskLoading = ref(false)
const tasks = ref([])
const submissions = ref([])

const form = ref({
  courseId: '',
  chapterId: '',
  questionCount: 10,
  durationMinutes: 30
})

const exam = ref(null)
const answers = ref({})
const result = ref(null)
const leftSeconds = ref(0)
let timer = null

const hasExam = computed(() => !!exam.value)
const hasResult = computed(() => !!result.value)
const progress = computed(() => {
  if (!exam.value?.questions?.length) return 0
  const total = exam.value.questions.length
  const answered = exam.value.questions.filter((q) => (answers.value[q.id] || []).length > 0).length
  return Math.round((answered * 100) / total)
})

const formatTime = (secs) => {
  const s = Math.max(0, secs || 0)
  const mm = String(Math.floor(s / 60)).padStart(2, '0')
  const ss = String(s % 60).padStart(2, '0')
  return `${mm}:${ss}`
}

const loadCourses = async () => {
  const res = await http.get('/api/courses')
  courses.value = res.data || []
}

const loadChapters = async () => {
  if (!form.value.courseId) {
    chapters.value = []
    form.value.chapterId = ''
    return
  }
  const res = await http.get(`/api/courses/${form.value.courseId}`)
  chapters.value = res.data?.chapters || []
}

const startTimer = () => {
  clearTimer()
  leftSeconds.value = (exam.value?.durationMinutes || 30) * 60
  timer = setInterval(() => {
    leftSeconds.value -= 1
    if (leftSeconds.value <= 0) {
      clearTimer()
      if (hasExam.value && !hasResult.value) {
        onSubmit(true)
      }
    }
  }, 1000)
}

const clearTimer = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const onStartExam = async () => {
  if (!form.value.courseId) {
    ElNotification({ title: '请选择课程', type: 'warning', duration: 1500 })
    return
  }
  creating.value = true
  try {
    const payload = {
      courseId: Number(form.value.courseId),
      chapterId: form.value.chapterId ? Number(form.value.chapterId) : null,
      questionCount: Number(form.value.questionCount || 10),
      durationMinutes: Number(form.value.durationMinutes || 30)
    }
    const res = await createMockExam(payload)
    exam.value = res.data
    result.value = null
    answers.value = {}
    startTimer()
    ElNotification({ title: '考试已开始', message: '请在规定时间内完成', type: 'success', duration: 1500 })
  } catch (e) {
    ElNotification({ title: '创建失败', message: e?.message || '请稍后再试', type: 'error', duration: 2000 })
  } finally {
    creating.value = false
  }
}

const refreshTasks = async () => {
  taskLoading.value = true
  try {
    const [taskRes, subRes] = await Promise.all([
      fetchExamTasks(),
      fetchMyExamSubmissions()
    ])
    tasks.value = taskRes.data || []
    submissions.value = subRes.data || []
  } finally {
    taskLoading.value = false
  }
}

const startTask = async (taskId) => {
  if (hasExam.value && !hasResult.value) {
    ElNotification({ title: '请先完成当前考试', type: 'warning', duration: 1500 })
    return
  }
  creating.value = true
  try {
    const res = await startExamTask(taskId)
    exam.value = res.data
    result.value = null
    answers.value = {}
    mode.value = 'mock'
    startTimer()
    ElNotification({ title: '任务考试已开始', type: 'success', duration: 1500 })
  } catch (e) {
    ElNotification({ title: '无法开始', message: e?.message || '请稍后再试', type: 'error', duration: 1800 })
  } finally {
    creating.value = false
  }
}

const setSingle = (qid, val) => {
  answers.value[qid] = val ? [val] : []
}

const setMulti = (qid, vals) => {
  answers.value[qid] = vals || []
}

const onSubmit = async (auto = false) => {
  if (!exam.value?.examId || submitting.value || hasResult.value) return
  submitting.value = true
  try {
    const payload = {
      examId: exam.value.examId,
      answers: Object.entries(answers.value).map(([questionId, val]) => ({
        questionId: Number(questionId),
        answers: val || []
      }))
    }
    const res = await submitMockExam(payload)
    result.value = res.data
    clearTimer()
    await refreshTasks()
    ElNotification({
      title: auto ? '时间到，已自动交卷' : '交卷成功',
      message: `得分 ${res.data?.score || 0}`,
      type: 'success',
      duration: 1800
    })
  } catch (e) {
    ElNotification({ title: '交卷失败', message: e?.message || '请稍后再试', type: 'error', duration: 2000 })
  } finally {
    submitting.value = false
  }
}

const onReset = () => {
  clearTimer()
  exam.value = null
  result.value = null
  answers.value = {}
  leftSeconds.value = 0
}

onMounted(async () => {
  loading.value = true
  try {
    await loadCourses()
    await refreshTasks()
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  clearTimer()
})
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">在线考试</div>
        <div class="subtitle">随机抽题 · 限时作答 · 自动判分</div>
      </div>
      <div class="timer" :class="{ danger: leftSeconds <= 60 && hasExam && !hasResult }">
        {{ hasExam && !hasResult ? formatTime(leftSeconds) : '--:--' }}
      </div>
    </div>

    <el-segmented
      v-model="mode"
      :options="[
        { label: '模拟考试', value: 'mock' },
        { label: '任务考试', value: 'task' },
        { label: '成绩记录', value: 'record' }
      ]"
    />

    <el-card v-if="mode === 'mock'" class="config" shadow="never">
      <div class="config-grid">
        <el-select v-model="form.courseId" placeholder="选择课程" @change="loadChapters" :disabled="hasExam && !hasResult">
          <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
        </el-select>
        <el-select v-model="form.chapterId" placeholder="章节（可选）" clearable :disabled="hasExam && !hasResult">
          <el-option v-for="ch in chapters" :key="ch.id" :label="ch.title" :value="ch.id" />
        </el-select>
        <el-input-number v-model="form.questionCount" :min="1" :max="50" :disabled="hasExam && !hasResult" />
        <el-input-number v-model="form.durationMinutes" :min="5" :max="180" :disabled="hasExam && !hasResult" />
        <el-button type="primary" :loading="creating" :disabled="hasExam && !hasResult" @click="onStartExam">
          开始考试
        </el-button>
        <el-button v-if="hasExam" @click="onReset">重置</el-button>
      </div>
    </el-card>

    <el-card v-if="mode === 'mock' && hasExam" class="exam-panel" shadow="never">
      <div class="panel-head">
        <span>当前进度 {{ progress }}%</span>
        <el-progress :percentage="progress" :stroke-width="8" />
      </div>
      <div class="q-list">
        <div v-for="(q, idx) in exam.questions" :key="q.id" class="q-item">
          <div class="q-title">
            <span class="idx">Q{{ idx + 1 }}</span>
            <span>{{ q.title }}</span>
            <span class="meta">{{ q.type === 'multi' ? '多选题' : '单选题' }}</span>
          </div>
          <div v-if="q.type === 'multi'" class="opts">
            <el-checkbox-group :model-value="answers[q.id] || []" @change="(vals) => setMulti(q.id, vals)">
              <el-checkbox v-for="opt in q.options" :key="opt.label" :label="opt.label">
                {{ opt.label }}. {{ opt.content }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
          <div v-else class="opts">
            <el-radio-group :model-value="(answers[q.id] || [])[0] || ''" @change="(val) => setSingle(q.id, val)">
              <el-radio v-for="opt in q.options" :key="opt.label" :label="opt.label">
                {{ opt.label }}. {{ opt.content }}
              </el-radio>
            </el-radio-group>
          </div>
        </div>
      </div>
      <div class="submit-row">
        <el-button type="success" :loading="submitting" :disabled="hasResult" @click="onSubmit(false)">交卷</el-button>
      </div>
    </el-card>

    <el-card v-if="mode === 'mock' && hasResult" class="result-panel" shadow="never">
      <div class="score-head">
        <div class="score">{{ result.score }}</div>
        <div class="score-meta">
          <div>总题数：{{ result.total }}</div>
          <div>答对：{{ result.correctCount }}</div>
          <div>答错：{{ result.wrongCount }}</div>
        </div>
      </div>
      <el-table :data="result.results || []" border>
        <el-table-column prop="questionId" label="题号" width="90" />
        <el-table-column prop="title" label="题目" min-width="220" />
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.correct ? 'success' : 'danger'">{{ row.correct ? '正确' : '错误' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="你的答案" min-width="120">
          <template #default="{ row }">{{ (row.userAnswers || []).join(',') || '-' }}</template>
        </el-table-column>
        <el-table-column label="正确答案" min-width="120">
          <template #default="{ row }">{{ (row.correctAnswers || []).join(',') || '-' }}</template>
        </el-table-column>
        <el-table-column prop="analysis" label="解析" min-width="220" />
      </el-table>
    </el-card>

    <el-card v-if="mode === 'task'" class="task-panel" shadow="never" v-loading="taskLoading">
      <el-table :data="tasks" border>
        <el-table-column prop="title" label="考试标题" min-width="180" />
        <el-table-column prop="courseTitle" label="课程" min-width="140" />
        <el-table-column prop="chapterTitle" label="章节" min-width="140" />
        <el-table-column prop="questionCount" label="题数" width="80" />
        <el-table-column prop="durationMinutes" label="时长(分钟)" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'info' : 'success'">{{ row.status === 2 ? '已结束' : '进行中' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成绩" width="100">
          <template #default="{ row }">
            <span v-if="row.submitted">{{ row.latestScore ?? '-' }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              :disabled="row.status === 2"
              @click="startTask(row.id)"
            >
              {{ row.submitted ? '再次考试' : '开始考试' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="mode === 'record'" class="record-panel" shadow="never" v-loading="taskLoading">
      <el-table :data="submissions" border>
        <el-table-column prop="taskTitle" label="考试标题" min-width="220" />
        <el-table-column prop="score" label="分数" width="100" />
        <el-table-column label="正确/总题" width="140">
          <template #default="{ row }">{{ row.correctCount }}/{{ row.totalCount }}</template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="提交时间" min-width="180" />
      </el-table>
    </el-card>
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

.timer {
  min-width: 110px;
  text-align: center;
  border-radius: 12px;
  border: 1px solid var(--ui-border);
  background: var(--ui-surface);
  padding: 10px 12px;
  font-size: 24px;
  letter-spacing: 0.06em;
  font-weight: 700;
}

.timer.danger {
  color: #ff4d6d;
  border-color: rgba(255, 77, 109, 0.45);
}

.config {
  border: 1px solid var(--ui-border);
  background: var(--ui-surface);
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(120px, 1fr));
  gap: 10px;
}

.exam-panel,
.result-panel {
  border: 1px solid var(--ui-border);
  background: var(--ui-surface);
}

.panel-head {
  display: grid;
  gap: 8px;
  margin-bottom: 10px;
}

.q-list {
  display: grid;
  gap: 12px;
}

.q-item {
  border: 1px solid var(--ui-border-soft);
  border-radius: 10px;
  background: var(--ui-surface-soft);
  padding: 10px;
}

.q-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: var(--ui-text);
  font-weight: 600;
}

.idx {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: linear-gradient(120deg, var(--ui-accent), var(--ui-accent-2));
  color: #041018;
  display: inline-grid;
  place-items: center;
  font-size: 12px;
}

.meta {
  margin-left: auto;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.submit-row {
  margin-top: 12px;
}

.score-head {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 12px;
}

.score {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(140deg, var(--ui-accent), var(--ui-accent-2));
  color: #041018;
  display: grid;
  place-items: center;
  font-size: 30px;
  font-weight: 800;
}

.score-meta {
  display: grid;
  gap: 4px;
  color: var(--ui-text);
}

@media (max-width: 1080px) {
  .config-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
