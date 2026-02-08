<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'
import { ElNotification } from 'element-plus'

const loading = ref(false)
const questions = ref([])
const activeIndex = ref(0)
const selected = ref([])
const selectedSingle = ref('')
const mode = ref('all')
const view = ref('practice')
const result = ref(null)
const showAnalysis = ref(false)
const courses = ref([])
const courseId = ref('')
const chapterId = ref('')
const type = ref('')
const difficulty = ref('')
const page = ref(1)
const size = ref(6)
const total = ref(0)
const favoriteIds = ref(new Set())
const chapters = ref([])
const directoryQuestions = ref([])
const search = ref('')
const stats = ref({
  total: 0,
  correct: 0,
  accuracy: 0,
  wrongCount: 0,
  favoriteCount: 0,
  wrongRedoCount: 0
})
const route = useRoute()
const router = useRouter()

const loadCourses = async () => {
  try {
    const res = await http.get('/api/courses')
    courses.value = res.data || []
  } catch (e) {
    // ignore
  }
}

const loadChapters = async () => {
  if (!courseId.value) {
    chapters.value = []
    chapterId.value = ''
    return
  }
  try {
    const res = await http.get(`/api/courses/${courseId.value}`)
    chapters.value = res.data?.chapters || []
  } catch (e) {
    chapters.value = []
  }
}

const load = async () => {
  loading.value = true
  try {
    const url =
      mode.value === 'wrong'
        ? '/api/questions/wrong'
        : mode.value === 'favorite'
          ? '/api/questions/favorites'
          : '/api/questions'
    const params = {
      page: page.value,
      size: size.value
    }
    if (courseId.value) params.courseId = courseId.value
    if (chapterId.value) params.chapterId = chapterId.value
    if (mode.value === 'all' && type.value) params.type = type.value
    if (mode.value === 'all' && difficulty.value) params.difficulty = difficulty.value

    const res = await http.get(url, { params })
    questions.value = res.data?.records || []
    total.value = res.data?.total || 0
    activeIndex.value = 0
    selected.value = []
    selectedSingle.value = ''
    result.value = null
    showAnalysis.value = false
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '题库加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const loadDirectory = async () => {
  try {
    const url =
      mode.value === 'wrong'
        ? '/api/questions/wrong'
        : mode.value === 'favorite'
          ? '/api/questions/favorites'
          : '/api/questions'
    const params = { page: 1, size: 200 }
    if (courseId.value) params.courseId = courseId.value
    if (chapterId.value) params.chapterId = chapterId.value
    if (mode.value === 'all' && type.value) params.type = type.value
    if (mode.value === 'all' && difficulty.value) params.difficulty = difficulty.value
    const res = await http.get(url, { params })
    directoryQuestions.value = res.data?.records || []
  } catch (e) {
    directoryQuestions.value = []
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
      wrongRedoCount: res.data?.wrongRedoCount || 0
    }
  } catch (e) {
    // ignore
  }
}

const chapterMap = computed(() => {
  const map = new Map()
  chapters.value.forEach((c) => map.set(c.id, c.title))
  return map
})

const filteredDirectory = computed(() => {
  if (!search.value) return directoryQuestions.value
  return directoryQuestions.value.filter((q) => q.title?.includes(search.value))
})

const directoryGroups = computed(() => {
  const groups = {}
  filteredDirectory.value.forEach((q) => {
    const key = q.chapterId || 'none'
    if (!groups[key]) {
      groups[key] = { title: chapterMap.value.get(q.chapterId) || '未分章', items: [] }
    }
    groups[key].items.push(q)
  })
  return Object.entries(groups).map(([key, val]) => ({ key, ...val }))
})

const activeQuestion = computed(() => questions.value[activeIndex.value])
const isMulti = computed(() => activeQuestion.value?.type === 'multi')

const submit = async () => {
  if (!activeQuestion.value) return
  const answers = isMulti.value
    ? selected.value
    : (selectedSingle.value ? [selectedSingle.value] : [])
  if (answers.length === 0) {
    ElNotification({
      title: '请选择答案',
      message: '至少选择一个选项',
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
    showAnalysis.value = true
    await loadStats()
    if (mode.value === 'wrong' && res.data?.correct) {
      await load()
      await loadDirectory()
    }
  } catch (e) {
    ElNotification({
      title: '提交失败',
      message: e?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  }
}

const next = () => {
  if (activeIndex.value < questions.value.length - 1) {
    activeIndex.value += 1
    selected.value = []
    selectedSingle.value = ''
    result.value = null
    showAnalysis.value = false
  }
}

const prev = () => {
  if (activeIndex.value > 0) {
    activeIndex.value -= 1
    selected.value = []
    selectedSingle.value = ''
    result.value = null
    showAnalysis.value = false
  }
}

const switchMode = async (val) => {
  mode.value = val
  page.value = 1
  await load()
  await loadDirectory()
}

const onCourseChange = async () => {
  page.value = 1
  await loadChapters()
  await load()
  await loadDirectory()
}

const onFilterChange = async () => {
  page.value = 1
  await load()
  await loadDirectory()
}

const onPageChange = async (p) => {
  page.value = p
  await load()
}

const onSizeChange = async (s) => {
  size.value = s
  page.value = 1
  await load()
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
      duration: 2000
    })
  }
}

const setActiveQuestion = (q) => {
  const idx = questions.value.findIndex((item) => item.id === q.id)
  if (idx === -1) {
    questions.value = directoryQuestions.value
    total.value = questions.value.length
    activeIndex.value = questions.value.findIndex((item) => item.id === q.id)
  } else {
    activeIndex.value = idx
  }
  selected.value = []
  selectedSingle.value = ''
  result.value = null
  showAnalysis.value = false
}

const redo = () => {
  selected.value = []
  selectedSingle.value = ''
  result.value = null
  showAnalysis.value = false
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
  await loadFavorites()
  await loadStats()
  await load()
  await loadDirectory()
})
</script>

<template>
  <div class="page">
    <div class="hero">
      <div class="hero-left">
        <div class="title display">题库练习</div>
        <div class="subtitle">章节练习 · 题目目录 · 错题复盘</div>
      </div>
      <div class="hero-right">
        <div class="counter-pill">共 {{ total }} 题</div>
        <div class="view-switch">
          <button class="view-btn" :class="{ active: view === 'practice' }" @click="view = 'practice'">
            练习
          </button>
          <button class="view-btn" :class="{ active: view === 'directory' }" @click="view = 'directory'">
            目录
          </button>
        </div>
      </div>
    </div>

    <div class="stats-bar">
      <div class="stat-card">
        <div class="stat-label">题库总量</div>
        <div class="stat-value">{{ stats.total }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">正确率</div>
        <div class="stat-value">{{ stats.accuracy }}%</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">错题数</div>
        <div class="stat-value">{{ stats.wrongCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">收藏数</div>
        <div class="stat-value">{{ stats.favoriteCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">错题重做</div>
        <div class="stat-value">{{ stats.wrongRedoCount }}</div>
      </div>
    </div>

    <div class="mode-bar">
      <div class="mode-left">
        <el-segmented
          v-model="mode"
          :options="[
            { label: '全部题目', value: 'all' },
            { label: '错题本', value: 'wrong' },
            { label: '收藏', value: 'favorite' }
          ]"
          @change="switchMode"
        />
        <el-select
          v-if="mode !== 'favorite'"
          v-model="courseId"
          placeholder="筛选课程"
          class="course-select"
          @change="onCourseChange"
          clearable
        >
          <el-option label="全部课程" value="" />
          <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
        </el-select>
        <el-select
          v-if="mode !== 'favorite'"
          v-model="chapterId"
          placeholder="筛选章节"
          class="course-select"
          @change="onFilterChange"
          clearable
        >
          <el-option label="全部章节" value="" />
          <el-option v-for="ch in chapters" :key="ch.id" :label="ch.title" :value="ch.id" />
        </el-select>
        <el-select
          v-if="mode === 'all'"
          v-model="type"
          placeholder="题型"
          class="filter-select"
          @change="onFilterChange"
          clearable
        >
          <el-option label="全部题型" value="" />
          <el-option label="单选题" value="single" />
          <el-option label="多选题" value="multi" />
        </el-select>
        <el-select
          v-if="mode === 'all'"
          v-model="difficulty"
          placeholder="难度"
          class="filter-select"
          @change="onFilterChange"
          clearable
        >
          <el-option label="全部难度" value="" />
          <el-option label="简单" :value="1" />
          <el-option label="中等" :value="2" />
          <el-option label="困难" :value="3" />
        </el-select>
      </div>
    </div>

    <div class="layout">
      <aside class="directory">
        <div class="dir-header">
          <div class="dir-head">题目目录</div>
          <div class="dir-search">
            <input v-model.trim="search" placeholder="搜索题目..." />
            <span class="dir-search-icon">⌕</span>
          </div>
        </div>
        <div class="dir-list">
          <el-collapse accordion>
            <el-collapse-item
              v-for="group in directoryGroups"
              :key="group.key"
              :name="group.key"
            >
              <template #title>
                <div class="dir-group-title">{{ group.title }}</div>
              </template>
              <div class="dir-group-list">
                <button
                  v-for="q in group.items"
                  :key="q.id"
                  class="dir-item"
                  :class="{ active: q.id === activeQuestion?.id }"
                  @click="setActiveQuestion(q)"
                >
                  <span class="dir-index">{{ q.id }}</span>
                  <span class="dir-title">{{ q.title }}</span>
                </button>
              </div>
            </el-collapse-item>
          </el-collapse>
          <div v-if="!directoryGroups.length" class="dir-empty">暂无题目</div>
        </div>
      </aside>

      <el-skeleton :loading="loading" animated>
        <template #template>
          <div class="skeleton-card"></div>
        </template>
        <template #default>
          <div v-if="activeQuestion && view === 'practice'" class="question-card">
            <div class="question-head">
              <div class="head-left">
                <div class="index">Q{{ activeIndex + 1 }}</div>
                <div>
                  <div class="question-title">{{ activeQuestion.title }}</div>
                  <div class="question-meta">
                    <span>{{ activeQuestion.type === 'multi' ? '多选题' : '单选题' }}</span>
                    <span>难度 {{ activeQuestion.difficulty || 1 }}</span>
                  </div>
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

            <div class="options">
              <el-checkbox-group v-if="isMulti" v-model="selected">
                <el-checkbox
                  v-for="opt in activeQuestion.options"
                  :key="opt.label"
                  :label="opt.label"
                >
                  <span class="opt-label">{{ opt.label }}.</span>
                  {{ opt.content }}
                </el-checkbox>
              </el-checkbox-group>
              <el-radio-group v-else v-model="selectedSingle">
                <el-radio v-for="opt in activeQuestion.options" :key="opt.label" :label="opt.label">
                  <span class="opt-label">{{ opt.label }}.</span>
                  {{ opt.content }}
                </el-radio>
              </el-radio-group>
            </div>

            <div class="actions">
              <el-button @click="prev" :disabled="activeIndex === 0">上一题</el-button>
              <el-button type="primary" @click="submit">提交答案</el-button>
              <el-button @click="next" :disabled="activeIndex >= questions.length - 1">下一题</el-button>
              <el-button v-if="result" @click="showAnalysis = !showAnalysis">
                {{ showAnalysis ? '收起解析' : '查看解析' }}
              </el-button>
              <el-button v-if="mode === 'wrong'" @click="redo">错题重做</el-button>
            </div>

            <div v-if="result" class="result">
              <div class="result-title" :class="{ ok: result.correct }">
                {{ result.correct ? '回答正确' : '回答错误' }}
              </div>
              <div class="result-meta">
                正确答案：{{ (result.correctAnswers || []).join('、') || '暂无' }}
              </div>
              <div v-if="showAnalysis" class="result-analysis">{{ result.analysis || '暂无解析' }}</div>
            </div>
          </div>

          <div v-else-if="view === 'directory'" class="directory-view">
            <div class="dir-view-title">题目目录视图</div>
            <div class="dir-view-list">
              <div v-for="group in directoryGroups" :key="group.key" class="dir-view-group">
                <div class="dir-view-group-title">{{ group.title }}</div>
                <div class="dir-view-items">
                  <div v-for="q in group.items" :key="q.id" class="dir-view-item">
                    <div class="dir-badge">Q{{ q.id }}</div>
                    <div class="dir-info">
                      <div class="dir-name">{{ q.title }}</div>
                      <div class="dir-meta">
                        {{ q.type === 'multi' ? '多选题' : '单选题' }} · 难度 {{ q.difficulty || 1 }}
                      </div>
                    </div>
                    <button class="dir-go" @click="setActiveQuestion(q); view='practice'">开始</button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <el-empty v-else description="暂无题目，请先录入题库" />
        </template>
      </el-skeleton>
    </div>

    <div class="pager">
      <el-pagination
        v-if="total > 0"
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next, sizes"
        :page-sizes="[6, 10, 20]"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 6px;
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

.hero-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stats-bar {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 12px;
  padding: 12px;
  display: grid;
  gap: 6px;
}

.stat-label {
  font-size: 11px;
  color: var(--ui-text-muted);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--ui-text);
}

.counter-pill {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid var(--ui-border);
  font-size: 12px;
  color: var(--ui-text);
}

.view-switch {
  display: inline-flex;
  gap: 6px;
  padding: 4px;
  border-radius: 999px;
  border: 1px solid var(--ui-border);
  background: var(--ui-surface);
}

.view-btn {
  border: none;
  background: transparent;
  color: var(--ui-text-muted);
  padding: 6px 12px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 12px;
}

.view-btn.active {
  color: var(--ui-text);
  background: rgba(86, 255, 213, 0.15);
}

.mode-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mode-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.course-select {
  width: 180px;
}

.filter-select {
  width: 120px;
}

.layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 16px;
}

.directory {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 16px;
  padding: 14px;
  display: grid;
  gap: 10px;
}

.dir-header {
  display: grid;
  gap: 10px;
}

.dir-head {
  font-size: 11px;
  color: var(--ui-text-muted);
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.dir-search input {
  width: 100%;
  border-radius: 10px;
  border: 1px solid var(--ui-border-soft);
  padding: 8px 10px;
  background: var(--ui-surface-soft);
  color: var(--ui-text);
  outline: none;
}

.dir-search {
  position: relative;
}

.dir-search input {
  padding-right: 28px;
}

.dir-search-icon {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: var(--ui-text-muted);
}

.dir-list {
  display: grid;
  gap: 8px;
  max-height: 520px;
  overflow: auto;
}

.dir-group-title {
  font-size: 13px;
  color: var(--ui-text);
  font-weight: 600;
  margin: 6px 0 4px;
}

.dir-group-list {
  display: grid;
  gap: 6px;
}

.dir-item {
  display: grid;
  grid-template-columns: 28px 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid transparent;
  background: var(--ui-surface-soft);
  color: var(--ui-text);
  text-align: left;
  cursor: pointer;
}

.dir-item.active {
  border-color: var(--ui-accent);
  box-shadow: 0 0 12px rgba(86, 255, 213, 0.2);
}

.dir-list :deep(.el-collapse) {
  border: none;
  background: transparent;
}

.dir-list :deep(.el-collapse-item__header) {
  background: transparent;
  color: var(--ui-text);
  border: none;
  padding: 0 6px;
}

.dir-list :deep(.el-collapse-item__content) {
  padding: 6px 0 8px;
}

.dir-index {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: linear-gradient(120deg, var(--ui-accent), var(--ui-accent-2));
  color: #07101a;
  font-weight: 700;
  display: grid;
  place-items: center;
  font-size: 12px;
}

.dir-title {
  font-size: 12px;
  line-height: 1.4;
}

.dir-item::after {
  content: '›';
  color: var(--ui-text-muted);
}

.dir-empty {
  font-size: 12px;
  color: var(--ui-text-muted);
  text-align: center;
  padding: 10px 0;
}

.question-card {
  padding: 18px;
  border-radius: 14px;
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
}

.question-head {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.head-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.index {
  font-size: 18px;
  font-weight: 700;
  color: var(--ui-accent);
}

.question-title {
  font-size: 16px;
  font-weight: 600;
}

.question-meta {
  margin-top: 4px;
  font-size: 12px;
  color: var(--ui-text-muted);
  display: flex;
  gap: 12px;
}

.fav-btn {
  border: 1px solid var(--ui-border);
  color: var(--ui-text);
  background: transparent;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  cursor: pointer;
}

.fav-btn.active {
  color: var(--ui-accent);
  border-color: var(--ui-accent);
  box-shadow: 0 0 10px rgba(86, 255, 213, 0.2);
}

.options {
  margin-top: 16px;
  display: grid;
  gap: 10px;
}

.opt-label {
  font-weight: 600;
  margin-right: 6px;
  color: var(--ui-text);
}

.actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.result {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed var(--ui-border);
}

.result-title {
  font-weight: 600;
  color: #ff4d6d;
}

.result-title.ok {
  color: #22c55e;
}

.result-meta {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 4px;
}

.result-analysis {
  margin-top: 6px;
  font-size: 13px;
}

.skeleton-card {
  height: 320px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
}

.pager {
  display: flex;
  justify-content: flex-end;
}

.directory-view {
  padding: 18px;
  border-radius: 14px;
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
}

.dir-view-title {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 10px;
}

.dir-view-list {
  display: grid;
  gap: 10px;
}

.dir-view-item {
  display: grid;
  grid-template-columns: 56px 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  background: var(--ui-surface-soft);
  border: 1px solid var(--ui-border-soft);
}

.dir-badge {
  width: 56px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(120deg, var(--ui-accent), var(--ui-accent-2));
  color: #07101a;
  display: grid;
  place-items: center;
  font-weight: 700;
}

.dir-info {
  display: grid;
  gap: 4px;
}

.dir-name {
  font-weight: 600;
}

.dir-meta {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.dir-go {
  border: 1px solid var(--ui-border);
  background: transparent;
  color: var(--ui-text);
  padding: 6px 12px;
  border-radius: 999px;
  cursor: pointer;
}

@media (max-width: 960px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .directory {
    order: 2;
  }
  .stats-bar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
