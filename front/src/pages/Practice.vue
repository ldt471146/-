<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'
import { ElNotification } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const mode = ref('all')
const courseId = ref('')
const chapterId = ref('')
const type = ref('')
const difficulty = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

const courses = ref([])
const chapters = ref([])
const questions = ref([])
const activeId = ref(null)
const search = ref('')

const selected = ref([])
const selectedSingle = ref('')
const result = ref(null)
const showAnalysis = ref(true)

const favoriteIds = ref(new Set())
const stats = ref({
  total: 0,
  correct: 0,
  accuracy: 0,
  wrongCount: 0,
  favoriteCount: 0,
  wrongRedoCount: 0,
  recentWrong: []
})
const codeProblems = ref([])

const activeQuestion = computed(() => questions.value.find((q) => q.id === activeId.value) || null)
const activeIndex = computed(() => questions.value.findIndex((q) => q.id === activeId.value))
const isMulti = computed(() => activeQuestion.value?.type === 'multi')

const visibleQuestions = computed(() => {
  if (!search.value) return questions.value
  const kw = search.value.trim()
  return questions.value.filter((q) => q.title?.includes(kw))
})

const statCards = computed(() => [
  { label: '正确率', value: `${stats.value.accuracy || 0}%` },
  { label: '错题', value: `${stats.value.wrongCount || 0}` },
  { label: '收藏', value: `${stats.value.favoriteCount || 0}` },
  { label: '错题改正', value: `${stats.value.wrongRedoCount || 0}` }
])

const loadCourses = async () => {
  const res = await http.get('/api/courses')
  courses.value = res.data || []
}

const loadChapters = async () => {
  if (!courseId.value) {
    chapters.value = []
    chapterId.value = ''
    return
  }
  const res = await http.get(`/api/courses/${courseId.value}`)
  chapters.value = res.data?.chapters || []
}

const buildQuestionUrl = () => {
  if (mode.value === 'wrong') return '/api/questions/wrong'
  if (mode.value === 'favorite') return '/api/questions/favorites'
  return '/api/questions'
}

const loadQuestions = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (courseId.value) params.courseId = courseId.value
    if (chapterId.value) params.chapterId = chapterId.value
    if (mode.value === 'all' && type.value) params.type = type.value
    if (mode.value === 'all' && difficulty.value) params.difficulty = difficulty.value
    const res = await http.get(buildQuestionUrl(), { params })
    questions.value = res.data?.records || []
    total.value = res.data?.total || 0
    activeId.value = questions.value[0]?.id || null
    clearAnswerState()
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '题目加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const loadCodeProblems = async () => {
  try {
    const params = {}
    if (courseId.value) params.courseId = courseId.value
    if (chapterId.value) params.chapterId = chapterId.value
    const res = await http.get('/api/code/problems', { params })
    codeProblems.value = res.data || []
  } catch (e) {
    // ignore
  }
}

const loadFavorites = async () => {
  try {
    const res = await http.get('/api/questions/favorite-ids')
    favoriteIds.value = new Set(res.data || [])
  } catch (e) {
    // ignore
  }
}

const loadStats = async () => {
  try {
    const res = await http.get('/api/questions/stats')
    stats.value = {
      total: res.data?.total || 0,
      correct: res.data?.correct || 0,
      accuracy: res.data?.accuracy || 0,
      wrongCount: res.data?.wrongCount || 0,
      favoriteCount: res.data?.favoriteCount || 0,
      wrongRedoCount: res.data?.wrongRedoCount || 0,
      recentWrong: res.data?.recentWrong || []
    }
  } catch (e) {
    // ignore
  }
}

const clearAnswerState = () => {
  selected.value = []
  selectedSingle.value = ''
  result.value = null
  showAnalysis.value = true
}

const pickQuestion = (id) => {
  activeId.value = id
  clearAnswerState()
}

const submit = async () => {
  if (!activeQuestion.value) return
  const answers = isMulti.value ? selected.value : (selectedSingle.value ? [selectedSingle.value] : [])
  if (!answers.length) {
    ElNotification({
      title: '请选择答案',
      message: '至少选择一个选项后再提交',
      type: 'warning',
      duration: 1500
    })
    return
  }
  try {
    const res = await http.post('/api/questions/submit', {
      questionId: activeQuestion.value.id,
      answers
    })
    result.value = res.data
    await Promise.all([loadStats(), loadFavorites()])
    if (mode.value === 'wrong' && res.data?.correct) {
      await loadQuestions()
    }
  } catch (e) {
    ElNotification({
      title: '提交失败',
      message: e?.message || '请稍后重试',
      type: 'error',
      duration: 2000
    })
  }
}

const stepQuestion = (step) => {
  if (!questions.value.length || activeIndex.value < 0) return
  const next = activeIndex.value + step
  if (next < 0 || next >= questions.value.length) return
  pickQuestion(questions.value[next].id)
}

const toggleFavorite = async (questionId) => {
  try {
    if (favoriteIds.value.has(questionId)) {
      await http.delete(`/api/questions/${questionId}/favorite`)
      favoriteIds.value.delete(questionId)
    } else {
      await http.post(`/api/questions/${questionId}/favorite`)
      favoriteIds.value.add(questionId)
    }
    favoriteIds.value = new Set(favoriteIds.value)
    await loadStats()
  } catch (e) {
    ElNotification({
      title: '操作失败',
      message: e?.message || '请稍后再试',
      type: 'error',
      duration: 1800
    })
  }
}

const resetFilters = async () => {
  mode.value = 'all'
  courseId.value = ''
  chapterId.value = ''
  type.value = ''
  difficulty.value = ''
  page.value = 1
  await loadChapters()
  await loadQuestions()
}

const onModeChange = async () => {
  page.value = 1
  await loadQuestions()
  await loadCodeProblems()
}

const onCourseChange = async () => {
  page.value = 1
  chapterId.value = ''
  await loadChapters()
  await loadQuestions()
  await loadCodeProblems()
}

const onFilterChange = async () => {
  page.value = 1
  await loadQuestions()
  await loadCodeProblems()
}

const onPageChange = async (p) => {
  page.value = p
  await loadQuestions()
}

const onSizeChange = async (s) => {
  size.value = s
  page.value = 1
  await loadQuestions()
}

const openRecentWrong = async (q) => {
  mode.value = 'wrong'
  page.value = 1
  await loadQuestions()
  const found = questions.value.find((item) => item.id === q.id)
  if (found) {
    pickQuestion(found.id)
  }
}

onMounted(async () => {
  const presetCourse = route.query.courseId ? Number(route.query.courseId) : null
  const presetMode = route.query.mode ? String(route.query.mode) : null
  if (presetMode && ['all', 'wrong', 'favorite'].includes(presetMode)) {
    mode.value = presetMode
  }
  if (presetCourse) {
    courseId.value = presetCourse
  }
  await loadCourses()
  await loadChapters()
  await Promise.all([loadFavorites(), loadStats()])
  await loadQuestions()
  await loadCodeProblems()
})
</script>

<template>
  <div class="page">
    <div class="hero">
      <div class="hero-left">
        <div class="title display">题库练习</div>
        <div class="subtitle">更顺手的练习流程：选题、作答、复盘一屏完成</div>
      </div>
      <div class="hero-right">
        <div class="pill">本页 {{ questions.length }} 题 / 共 {{ total }} 题</div>
      </div>
    </div>

    <div class="toolbar">
      <el-segmented
        v-model="mode"
        :options="[
          { label: '全部', value: 'all' },
          { label: '错题本', value: 'wrong' },
          { label: '收藏', value: 'favorite' }
        ]"
        @change="onModeChange"
      />
      <el-select v-model="courseId" placeholder="课程" class="sel" clearable @change="onCourseChange">
        <el-option label="全部课程" value="" />
        <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
      </el-select>
      <el-select v-model="chapterId" placeholder="章节" class="sel" clearable @change="onFilterChange">
        <el-option label="全部章节" value="" />
        <el-option v-for="ch in chapters" :key="ch.id" :label="ch.title" :value="ch.id" />
      </el-select>
      <el-select v-if="mode === 'all'" v-model="type" placeholder="题型" class="mini" clearable @change="onFilterChange">
        <el-option label="单选题" value="single" />
        <el-option label="多选题" value="multi" />
      </el-select>
      <el-select v-if="mode === 'all'" v-model="difficulty" placeholder="难度" class="mini" clearable @change="onFilterChange">
        <el-option label="简单" :value="1" />
        <el-option label="中等" :value="2" />
        <el-option label="困难" :value="3" />
      </el-select>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <div class="layout">
      <aside class="nav-panel">
        <div class="nav-head">
          <span>题目导航</span>
          <input v-model.trim="search" placeholder="搜索题目..." />
        </div>
        <div class="nav-list">
          <button
            v-for="(q, i) in visibleQuestions"
            :key="q.id"
            class="nav-item"
            :class="{ active: activeId === q.id }"
            @click="pickQuestion(q.id)"
          >
            <span class="idx">{{ i + 1 }}</span>
            <span class="q-title">{{ q.title }}</span>
          </button>
          <div v-if="!visibleQuestions.length" class="empty">暂无题目</div>
        </div>
      </aside>

      <el-skeleton :loading="loading" animated class="question-wrap">
        <template #template>
          <div class="skeleton"></div>
        </template>
        <template #default>
          <div v-if="activeQuestion" class="question-card">
            <div class="q-head">
              <div>
                <div class="q-main">{{ activeQuestion.title }}</div>
                <div class="q-meta">
                  <span>{{ activeQuestion.type === 'multi' ? '多选题' : '单选题' }}</span>
                  <span>难度 {{ activeQuestion.difficulty || 1 }}</span>
                </div>
              </div>
              <button
                class="fav-btn"
                :class="{ active: favoriteIds.has(activeQuestion.id) }"
                @click="toggleFavorite(activeQuestion.id)"
              >
                {{ favoriteIds.has(activeQuestion.id) ? '已收藏' : '收藏' }}
              </button>
            </div>

            <div class="q-options">
              <el-checkbox-group v-if="isMulti" v-model="selected">
                <el-checkbox v-for="opt in activeQuestion.options" :key="opt.label" :label="opt.label">
                  <span class="opt-key">{{ opt.label }}.</span>
                  {{ opt.content }}
                </el-checkbox>
              </el-checkbox-group>
              <el-radio-group v-else v-model="selectedSingle">
                <el-radio v-for="opt in activeQuestion.options" :key="opt.label" :label="opt.label">
                  <span class="opt-key">{{ opt.label }}.</span>
                  {{ opt.content }}
                </el-radio>
              </el-radio-group>
            </div>

            <div class="q-actions">
              <el-button @click="stepQuestion(-1)" :disabled="activeIndex <= 0">上一题</el-button>
              <el-button type="primary" @click="submit">提交</el-button>
              <el-button @click="stepQuestion(1)" :disabled="activeIndex >= questions.length - 1">下一题</el-button>
              <el-button v-if="result" @click="showAnalysis = !showAnalysis">
                {{ showAnalysis ? '收起解析' : '查看解析' }}
              </el-button>
            </div>

            <div v-if="result" class="answer">
              <div class="answer-title" :class="{ ok: result.correct }">
                {{ result.correct ? '回答正确' : '回答错误' }}
              </div>
              <div class="answer-meta">正确答案：{{ (result.correctAnswers || []).join('、') || '-' }}</div>
              <div v-if="showAnalysis" class="answer-analysis">{{ result.analysis || '暂无解析' }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无题目，请先筛选课程" />
        </template>
      </el-skeleton>

      <aside class="right-panel">
        <div class="stat-grid">
          <div class="stat-card" v-for="s in statCards" :key="s.label">
            <div class="s-label">{{ s.label }}</div>
            <div class="s-value">{{ s.value }}</div>
          </div>
        </div>
        <div class="recent">
          <div class="recent-head">最近错题</div>
          <button
            v-for="q in stats.recentWrong"
            :key="q.id"
            class="recent-item"
            @click="openRecentWrong(q)"
          >
            {{ q.title }}
          </button>
          <div v-if="!stats.recentWrong?.length" class="empty">暂无最近错题</div>
        </div>
        <div class="recent">
          <div class="recent-head">编程题训练</div>
          <button
            v-for="q in codeProblems.slice(0, 5)"
            :key="q.id"
            class="recent-item"
            @click="router.push({ path: '/code-practice', query: { problemId: q.id, courseId: courseId || '' } })"
          >
            {{ q.title }}
          </button>
          <button class="recent-item" @click="router.push({ path: '/code-practice', query: { courseId: courseId || '' } })">
            进入编程判题页
          </button>
          <div v-if="!codeProblems?.length" class="empty">当前筛选暂无编程题</div>
        </div>
      </aside>
    </div>

    <div class="pager">
      <el-pagination
        v-if="total > 0"
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next, sizes"
        :page-sizes="[10, 20, 30]"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
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
  gap: 14px;
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

.pill {
  border: 1px solid var(--ui-border);
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.sel {
  width: 200px;
}

.mini {
  width: 120px;
}

.layout {
  display: grid;
  grid-template-columns: 280px 1fr 300px;
  gap: 12px;
}

.nav-panel,
.question-card,
.right-panel {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 14px;
}

.nav-panel {
  padding: 10px;
  display: grid;
  gap: 10px;
  max-height: 620px;
}

.nav-head {
  display: grid;
  gap: 8px;
}

.nav-head span {
  font-size: 12px;
  color: var(--ui-text-muted);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.nav-head input {
  border: 1px solid var(--ui-border-soft);
  background: var(--ui-surface-soft);
  color: var(--ui-text);
  border-radius: 10px;
  padding: 8px 10px;
  outline: none;
}

.nav-list {
  display: grid;
  gap: 6px;
  overflow: auto;
}

.nav-item {
  border: 1px solid transparent;
  background: var(--ui-surface-soft);
  border-radius: 10px;
  text-align: left;
  display: grid;
  grid-template-columns: 26px 1fr;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  color: var(--ui-text);
  cursor: pointer;
}

.nav-item.active {
  border-color: var(--ui-accent);
  box-shadow: 0 0 10px rgba(86, 255, 213, 0.2);
}

.idx {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: linear-gradient(120deg, var(--ui-accent), var(--ui-accent-2));
  color: #07101a;
  font-size: 11px;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.q-title {
  font-size: 12px;
  line-height: 1.35;
}

.question-wrap {
  min-height: 420px;
}

.question-card {
  padding: 14px;
}

.q-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.q-main {
  font-size: 16px;
  font-weight: 700;
}

.q-meta {
  margin-top: 6px;
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.fav-btn {
  border: 1px solid var(--ui-border);
  background: transparent;
  color: var(--ui-text);
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  cursor: pointer;
}

.fav-btn.active {
  border-color: var(--ui-accent);
  color: var(--ui-accent);
}

.q-options {
  margin-top: 14px;
}

.opt-key {
  font-weight: 700;
  margin-right: 6px;
}

.q-actions {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.answer {
  margin-top: 14px;
  padding-top: 10px;
  border-top: 1px dashed var(--ui-border);
}

.answer-title {
  font-weight: 700;
  color: #ff4d6d;
}

.answer-title.ok {
  color: #22c55e;
}

.answer-meta {
  margin-top: 6px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.answer-analysis {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
}

.right-panel {
  padding: 10px;
  display: grid;
  gap: 10px;
  align-content: start;
}

.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.stat-card {
  border: 1px solid var(--ui-border-soft);
  border-radius: 10px;
  background: var(--ui-surface-soft);
  padding: 8px;
}

.s-label {
  font-size: 11px;
  color: var(--ui-text-muted);
}

.s-value {
  margin-top: 4px;
  font-size: 18px;
  font-family: var(--font-mono);
  font-weight: 700;
}

.recent {
  border: 1px solid var(--ui-border-soft);
  border-radius: 10px;
  padding: 10px;
  background: var(--ui-surface-soft);
  display: grid;
  gap: 8px;
}

.recent-head {
  font-size: 12px;
  color: var(--ui-text-muted);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.recent-item {
  border: 1px solid var(--ui-border-soft);
  background: var(--ui-surface);
  color: var(--ui-text);
  text-align: left;
  border-radius: 8px;
  padding: 8px;
  font-size: 12px;
  cursor: pointer;
}

.empty {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.pager {
  display: flex;
  justify-content: flex-end;
}

.skeleton {
  height: 360px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
}

@media (max-width: 1100px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .right-panel {
    order: 3;
  }
}
</style>
