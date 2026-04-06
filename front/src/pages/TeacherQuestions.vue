<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElNotification } from 'element-plus'
import {
  createTeacherCodeProblem,
  createTeacherQuestion,
  deleteTeacherCodeProblem,
  deleteTeacherQuestion,
  fetchTeacherCodeProblems,
  fetchTeacherCourseDetail,
  fetchTeacherCourses,
  fetchTeacherQuestions,
  importTeacherQuestions,
  updateTeacherCodeProblem,
  updateTeacherQuestion
} from '../api/teacher'
import TeacherCodeProblemDialog from '../components/teacher/TeacherCodeProblemDialog.vue'
import TeacherCodeProblemTable from '../components/teacher/TeacherCodeProblemTable.vue'
import TeacherObjectiveQuestionTable from '../components/teacher/TeacherObjectiveQuestionTable.vue'
import TeacherQuestionDialog from '../components/teacher/TeacherQuestionDialog.vue'
import TeacherQuestionImportPanel from '../components/teacher/TeacherQuestionImportPanel.vue'

const route = useRoute()
const router = useRouter()
const OPTION_LABELS = ['A', 'B', 'C', 'D']
const IMPORT_SAMPLE = `[
  {
    "title": "Python 中用于输出内容的函数是？",
    "type": "single",
    "difficulty": 1,
    "analysis": "print() 是输出函数",
    "options": [
      { "label": "A", "content": "print()", "isCorrect": 1 },
      { "label": "B", "content": "echo()", "isCorrect": 0 },
      { "label": "C", "content": "show()", "isCorrect": 0 },
      { "label": "D", "content": "console()", "isCorrect": 0 }
    ]
  }
]`

const createQuestionForm = (overrides = {}) => ({
  title: '',
  type: 'single',
  difficulty: 1,
  analysis: '',
  courseId: '',
  chapterId: '',
  options: OPTION_LABELS.map((label, index) => ({ label, content: '', isCorrect: index === 0 ? 1 : 0 })),
  ...overrides
})

const createCodeForm = (overrides = {}) => ({
  title: '',
  content: '',
  difficulty: 1,
  timeLimit: 1000,
  memoryLimit: 256,
  status: 1,
  courseId: '',
  chapterId: '',
  testcases: [
    { inputData: '', outputData: '', isSample: 1 },
    { inputData: '', outputData: '', isSample: 0 }
  ],
  ...overrides
})

const normalizeOptions = (options = []) =>
  OPTION_LABELS.map((label, index) => {
    const matched = options.find((item) => item.label === label) || {}
    return { label, content: matched.content || '', isCorrect: matched.isCorrect || (index === 0 ? 1 : 0) }
  })

const courses = ref([])
const chapters = ref([])
const requestedCourseId = ref(Number(route.query.courseId || 0) || '')
const requestedChapterId = ref(Number(route.query.chapterId || 0) || '')
const courseId = ref(requestedCourseId.value || '')
const chapterId = ref(requestedChapterId.value || '')
const activeTab = ref('objective')
const questionLoading = ref(false)
const codeLoading = ref(false)
const questionPage = ref(1)
const questionSize = ref(10)
const questionTotal = ref(0)
const codePage = ref(1)
const codeSize = ref(10)
const codeTotal = ref(0)
const questions = ref([])
const codeProblems = ref([])
const objectiveKeyword = ref('')
const codeKeyword = ref('')
const objectiveDifficulty = ref('all')
const codeDifficulty = ref('all')
const codeStatus = ref('all')
const importJson = ref(IMPORT_SAMPLE)
const importing = ref(false)
const dialogOpen = ref(false)
const codeDialogOpen = ref(false)
const saving = ref(false)
const codeSaving = ref(false)
const editingId = ref(null)
const codeEditingId = ref(null)
const form = ref(createQuestionForm())
const codeForm = ref(createCodeForm())
const formChapters = ref([])
const codeFormChapters = ref([])

const selectedCourse = computed(() => courses.value.find((item) => item.id === courseId.value) || null)
const selectedChapter = computed(() => chapters.value.find((item) => item.id === chapterId.value) || null)

const overviewCards = computed(() => [
  { label: '当前课程', value: selectedCourse.value?.title || '未选择课程', hint: `${chapters.value.length} 个章节可管理` },
  { label: '客观题规模', value: `${questionTotal.value}`, hint: `当前页可见 ${objectiveRows.value.length} 题` },
  { label: '编程题规模', value: `${codeTotal.value}`, hint: `当前页可见 ${codeRows.value.length} 题` },
  { label: '下游动作', value: '考试 / 作业', hint: '建库完成后即可继续发布' }
])

const workflowCards = [
  { title: '发布考试任务', description: '从当前课程题库直接进入考试发布流', path: '/teacher/exams', cta: '去组卷' },
  { title: '发布课程作业', description: '把现有题库沉淀成阶段性作业任务', path: '/teacher/homework', cta: '去布置' }
]

const objectiveRows = computed(() =>
  questions.value.filter((row) => {
    const byKeyword = !objectiveKeyword.value || row.title?.includes(objectiveKeyword.value.trim())
    const byDifficulty = objectiveDifficulty.value === 'all' || String(row.difficulty) === objectiveDifficulty.value
    return byKeyword && byDifficulty
  })
)

const codeRows = computed(() =>
  codeProblems.value.filter((row) => {
    const byKeyword = !codeKeyword.value || row.title?.includes(codeKeyword.value.trim())
    const byDifficulty = codeDifficulty.value === 'all' || String(row.difficulty) === codeDifficulty.value
    const byStatus = codeStatus.value === 'all' || String(row.status) === codeStatus.value
    return byKeyword && byDifficulty && byStatus
  })
)

const activeSummary = computed(() => {
  if (activeTab.value === 'code') {
    return [
      { label: '上架题目', value: codeRows.value.filter((item) => item.status === 1).length },
      { label: '隐藏题目', value: codeRows.value.filter((item) => item.status !== 1).length },
      { label: '挑战题', value: codeRows.value.filter((item) => item.difficulty === 3).length }
    ]
  }
  if (activeTab.value === 'import') {
    return [
      { label: '导入范围', value: selectedCourse.value?.title || '未选课程' },
      { label: '当前章节', value: selectedChapter.value?.title || '全部章节' },
      { label: '建议方式', value: '先章节化，再分批导入' }
    ]
  }
  return [
    { label: '单选题', value: objectiveRows.value.filter((item) => item.type !== 'multi').length },
    { label: '多选题', value: objectiveRows.value.filter((item) => item.type === 'multi').length },
    { label: '挑战题', value: objectiveRows.value.filter((item) => item.difficulty === 3).length }
  ]
})

const loadCourses = async () => {
  const res = await fetchTeacherCourses()
  courses.value = res.data || []
  if (requestedCourseId.value && courses.value.some((item) => item.id === requestedCourseId.value)) {
    courseId.value = requestedCourseId.value
  }
  if (!courseId.value && courses.value.length) courseId.value = courses.value[0].id
}

const loadCourseDetail = async (value) => {
  if (!value) return []
  const res = await fetchTeacherCourseDetail(value)
  return res.data?.chapters || []
}

const loadFilterChapters = async () => {
  chapters.value = courseId.value ? await loadCourseDetail(courseId.value) : []
  if (requestedChapterId.value && chapters.value.some((item) => item.id === requestedChapterId.value)) {
    chapterId.value = requestedChapterId.value
    requestedChapterId.value = ''
    return
  }
  if (chapterId.value && !chapters.value.some((item) => item.id === chapterId.value)) {
    chapterId.value = ''
  }
}

const loadQuestions = async () => {
  if (!courseId.value) return
  questionLoading.value = true
  try {
    const res = await fetchTeacherQuestions({ courseId: courseId.value, chapterId: chapterId.value || undefined, page: questionPage.value, size: questionSize.value })
    questions.value = res.data?.records || []
    questionTotal.value = res.data?.total || 0
  } catch (error) {
    ElNotification({ title: '加载失败', message: error?.message || '客观题加载失败', type: 'error', duration: 2000 })
  } finally {
    questionLoading.value = false
  }
}

const loadCodeProblems = async () => {
  if (!courseId.value) return
  codeLoading.value = true
  try {
    const res = await fetchTeacherCodeProblems({ courseId: courseId.value, chapterId: chapterId.value || undefined, page: codePage.value, size: codeSize.value })
    codeProblems.value = res.data?.records || []
    codeTotal.value = res.data?.total || 0
  } catch (error) {
    ElNotification({ title: '加载失败', message: error?.message || '编程题加载失败', type: 'error', duration: 2000 })
  } finally {
    codeLoading.value = false
  }
}

const refreshAll = async () => Promise.all([loadQuestions(), loadCodeProblems()])
const goWorkflow = (path) => router.push(path)

const handleFilterCourseChange = async () => {
  chapterId.value = ''
  questionPage.value = 1
  codePage.value = 1
  await loadFilterChapters()
  await refreshAll()
}

const handleFilterChapterChange = async () => {
  questionPage.value = 1
  codePage.value = 1
  await refreshAll()
}

const openCreate = async () => {
  editingId.value = null
  form.value = createQuestionForm({ courseId: courseId.value, chapterId: chapterId.value || '' })
  formChapters.value = courseId.value ? await loadCourseDetail(courseId.value) : []
  dialogOpen.value = true
}

const openEdit = async (row) => {
  editingId.value = row.id
  form.value = createQuestionForm({ title: row.title, type: row.type, difficulty: row.difficulty || 1, analysis: row.analysis || '', courseId: row.courseId, chapterId: row.chapterId || '', options: normalizeOptions(row.options || []) })
  formChapters.value = row.courseId ? await loadCourseDetail(row.courseId) : []
  dialogOpen.value = true
}

const openCodeCreate = async () => {
  codeEditingId.value = null
  codeForm.value = createCodeForm({ courseId: courseId.value, chapterId: chapterId.value || '' })
  codeFormChapters.value = courseId.value ? await loadCourseDetail(courseId.value) : []
  codeDialogOpen.value = true
}

const openCodeEdit = async (row) => {
  codeEditingId.value = row.id
  codeForm.value = createCodeForm({ title: row.title, content: row.content || '', difficulty: row.difficulty || 1, timeLimit: row.timeLimit || 1000, memoryLimit: row.memoryLimit || 256, status: row.status == null ? 1 : row.status, courseId: row.courseId, chapterId: row.chapterId || '', testcases: (row.testcases?.length ? row.testcases : [{ inputData: '', outputData: '', isSample: 1 }]).map((item) => ({ inputData: item.inputData || '', outputData: item.outputData || '', isSample: item.isSample || 0 })) })
  codeFormChapters.value = row.courseId ? await loadCourseDetail(row.courseId) : []
  codeDialogOpen.value = true
}

const handleQuestionCourseChange = async (value) => {
  form.value.chapterId = ''
  formChapters.value = value ? await loadCourseDetail(value) : []
}

const handleCodeCourseChange = async (value) => {
  codeForm.value.chapterId = ''
  codeFormChapters.value = value ? await loadCourseDetail(value) : []
}

const toggleCorrect = (option) => {
  if (form.value.type === 'single') {
    form.value.options.forEach((item) => { item.isCorrect = 0 })
    option.isCorrect = 1
    return
  }
  option.isCorrect = option.isCorrect ? 0 : 1
}

const saveQuestion = async () => {
  if (!form.value.title || !form.value.courseId) return ElNotification({ title: '请完善题目信息', type: 'warning', duration: 1500 })
  if (!form.value.options.every((item) => item.content?.trim())) return ElNotification({ title: '请填写完整选项', type: 'warning', duration: 1500 })
  if (!form.value.options.some((item) => item.isCorrect)) return ElNotification({ title: '请至少设置一个正确答案', type: 'warning', duration: 1500 })
  saving.value = true
  const payload = { ...form.value, title: form.value.title.trim(), chapterId: form.value.chapterId || null, options: normalizeOptions(form.value.options) }
  try {
    if (editingId.value) await updateTeacherQuestion(editingId.value, payload)
    else await createTeacherQuestion(payload)
    dialogOpen.value = false
    await loadQuestions()
    ElNotification({ title: editingId.value ? '更新成功' : '创建成功', type: 'success', duration: 1600 })
  } catch (error) {
    ElNotification({ title: '保存失败', message: error?.message || '请稍后再试', type: 'error', duration: 2000 })
  } finally {
    saving.value = false
  }
}

const addCodeTestcase = () => codeForm.value.testcases.push({ inputData: '', outputData: '', isSample: 0 })
const removeCodeTestcase = (index) => { if (codeForm.value.testcases.length > 1) codeForm.value.testcases.splice(index, 1) }

const saveCodeProblem = async () => {
  const validCases = (codeForm.value.testcases || []).filter((item) => (item.inputData || '').trim() !== '' || (item.outputData || '').trim() !== '')
  if (!codeForm.value.title || !codeForm.value.courseId || !codeForm.value.content?.trim()) return ElNotification({ title: '请完善编程题信息', type: 'warning', duration: 1500 })
  if (!validCases.length) return ElNotification({ title: '请至少填写一组测试用例', type: 'warning', duration: 1500 })
  if (!validCases.some((item) => item.isSample === 1)) validCases[0].isSample = 1
  codeSaving.value = true
  const payload = { ...codeForm.value, title: codeForm.value.title.trim(), content: codeForm.value.content.trim(), chapterId: codeForm.value.chapterId || null, testcases: validCases }
  try {
    if (codeEditingId.value) await updateTeacherCodeProblem(codeEditingId.value, payload)
    else await createTeacherCodeProblem(payload)
    codeDialogOpen.value = false
    await loadCodeProblems()
    ElNotification({ title: codeEditingId.value ? '更新成功' : '创建成功', type: 'success', duration: 1600 })
  } catch (error) {
    ElNotification({ title: '保存失败', message: error?.message || '请稍后再试', type: 'error', duration: 2000 })
  } finally {
    codeSaving.value = false
  }
}

const submitImport = async () => {
  if (!courseId.value) return ElNotification({ title: '请先选择课程', type: 'warning', duration: 1500 })
  let items = []
  try {
    const parsed = JSON.parse(importJson.value || '[]')
    if (!Array.isArray(parsed)) throw new Error('导入内容必须是数组')
    items = parsed
  } catch (error) {
    return ElNotification({ title: 'JSON 格式错误', message: error?.message || '请检查导入内容', type: 'error', duration: 2200 })
  }
  if (!items.length) return ElNotification({ title: '导入内容为空', type: 'warning', duration: 1500 })
  importing.value = true
  try {
    const res = await importTeacherQuestions({ courseId: Number(courseId.value), chapterId: chapterId.value ? Number(chapterId.value) : null, items })
    ElNotification({ title: '导入成功', message: `成功导入 ${res.data || 0} 题`, type: 'success', duration: 1800 })
    activeTab.value = 'objective'
    await loadQuestions()
  } catch (error) {
    ElNotification({ title: '导入失败', message: error?.message || '请稍后再试', type: 'error', duration: 2200 })
  } finally {
    importing.value = false
  }
}

const confirmDelete = async (type, row) => {
  await ElMessageBox.confirm(`确认删除${type}「${row.title}」吗？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
}

const removeQuestion = async (row) => {
  try {
    await confirmDelete('题目', row)
    await deleteTeacherQuestion(row.id)
    await loadQuestions()
    ElNotification({ title: '删除成功', type: 'success', duration: 1500 })
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElNotification({ title: '删除失败', message: error?.message || '请稍后再试', type: 'error', duration: 2000 })
  }
}

const removeCodeProblem = async (row) => {
  try {
    await confirmDelete('编程题', row)
    await deleteTeacherCodeProblem(row.id)
    await loadCodeProblems()
    ElNotification({ title: '删除成功', type: 'success', duration: 1500 })
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElNotification({ title: '删除失败', message: error?.message || '请稍后再试', type: 'error', duration: 2000 })
  }
}

onMounted(async () => {
  await loadCourses()
  await loadFilterChapters()
  await refreshAll()
})
</script>

<template>
  <div class="page">
    <section class="hero">
      <div>
        <div class="hero-eyebrow">Question Bank Workspace</div>
        <div class="hero-title display">题库工作台</div>
        <div class="hero-copy">把客观题、编程题、批量导入和下游发布动作收束到一个清晰工作台里，让教师先建库、再组卷、最后回看结果。</div>
      </div>
      <div class="hero-actions">
        <el-button @click="activeTab = 'import'">批量导入</el-button>
        <el-button type="primary" @click="openCreate">新建客观题</el-button>
        <el-button type="success" @click="openCodeCreate">新建编程题</el-button>
      </div>
    </section>

    <section class="overview-grid">
      <article v-for="card in overviewCards" :key="card.label" class="overview-card">
        <div class="overview-label">{{ card.label }}</div>
        <div class="overview-value">{{ card.value }}</div>
        <div class="overview-hint">{{ card.hint }}</div>
      </article>
    </section>

    <section class="workspace-card">
      <div class="workspace-top">
        <div class="filters-panel">
          <div class="panel-title">统一筛选</div>
          <div class="panel-subtitle">题库、编程题和批量导入共用同一课程上下文。</div>
          <div class="filter-row">
            <el-select v-model="courseId" class="filter" placeholder="选择课程" @change="handleFilterCourseChange">
              <el-option v-for="course in courses" :key="course.id" :label="course.title" :value="course.id" />
            </el-select>
            <el-select v-model="chapterId" class="filter" clearable placeholder="选择章节" @change="handleFilterChapterChange">
              <el-option v-for="chapter in chapters" :key="chapter.id" :label="chapter.title" :value="chapter.id" />
            </el-select>
          </div>
        </div>
        <div class="workflow-grid">
          <button v-for="card in workflowCards" :key="card.path" class="workflow-card" @click="goWorkflow(card.path)">
            <div class="workflow-title">{{ card.title }}</div>
            <div class="workflow-desc">{{ card.description }}</div>
            <div class="workflow-cta">{{ card.cta }} →</div>
          </button>
        </div>
      </div>

      <div class="tab-shell">
        <div class="tab-header">
          <div>
            <div class="panel-title">分段工作区</div>
            <div class="panel-subtitle">先按题型管理，再进入考试或作业发布流。</div>
          </div>
          <el-segmented
            v-model="activeTab"
            :options="[
              { label: '客观题', value: 'objective' },
              { label: '编程题', value: 'code' },
              { label: '批量导入', value: 'import' }
            ]"
          />
        </div>

        <div class="summary-strip">
          <div v-for="item in activeSummary" :key="item.label" class="summary-pill">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>

        <TeacherObjectiveQuestionTable
          v-if="activeTab === 'objective'"
          :rows="objectiveRows"
          :loading="questionLoading"
          :total="questionTotal"
          :page="questionPage"
          :size="questionSize"
          :keyword="objectiveKeyword"
          :difficulty="objectiveDifficulty"
          @update:keyword="objectiveKeyword = $event"
          @update:difficulty="objectiveDifficulty = $event"
          @page-change="questionPage = $event; loadQuestions()"
          @size-change="questionSize = $event; questionPage = 1; loadQuestions()"
          @edit="openEdit"
          @delete="removeQuestion"
        />

        <TeacherCodeProblemTable
          v-else-if="activeTab === 'code'"
          :rows="codeRows"
          :loading="codeLoading"
          :total="codeTotal"
          :page="codePage"
          :size="codeSize"
          :keyword="codeKeyword"
          :difficulty="codeDifficulty"
          :status="codeStatus"
          @update:keyword="codeKeyword = $event"
          @update:difficulty="codeDifficulty = $event"
          @update:status="codeStatus = $event"
          @page-change="codePage = $event; loadCodeProblems()"
          @size-change="codeSize = $event; codePage = 1; loadCodeProblems()"
          @edit="openCodeEdit"
          @delete="removeCodeProblem"
        />

        <TeacherQuestionImportPanel
          v-else
          v-model="importJson"
          :importing="importing"
          :selected-course="selectedCourse?.title || '未选择课程'"
          :selected-chapter="selectedChapter?.title || '全部章节'"
          @submit="submitImport"
        />
      </div>
    </section>

    <TeacherQuestionDialog v-model:open="dialogOpen" :form="form" :courses="courses" :chapters="formChapters" :saving="saving" :is-editing="Boolean(editingId)" @course-change="handleQuestionCourseChange" @toggle-correct="toggleCorrect" @save="saveQuestion" />
    <TeacherCodeProblemDialog v-model:open="codeDialogOpen" :form="codeForm" :courses="courses" :chapters="codeFormChapters" :saving="codeSaving" :is-editing="Boolean(codeEditingId)" @course-change="handleCodeCourseChange" @add-testcase="addCodeTestcase" @remove-testcase="removeCodeTestcase" @save="saveCodeProblem" />
  </div>
</template>

<style scoped>
.page,.workspace-card,.tab-shell{display:grid;gap:18px}.hero,.overview-card,.filters-panel,.workflow-card{border:1px solid var(--ui-border-soft);background:var(--ui-surface);box-shadow:var(--ui-content-shadow)}.hero{display:flex;justify-content:space-between;gap:24px;align-items:flex-start;padding:28px;border-radius:28px}.hero-eyebrow{font-size:11px;letter-spacing:.18em;text-transform:uppercase;color:var(--ui-accent)}.hero-title{font-size:30px;font-weight:700;color:var(--ui-text)}.hero-copy{margin-top:10px;max-width:760px;color:var(--ui-text-muted);font-size:14px;line-height:1.8}.hero-actions{display:flex;gap:10px;flex-wrap:wrap;justify-content:flex-end}.overview-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:14px}.overview-card{padding:20px;border-radius:22px}.overview-label{font-size:12px;color:var(--ui-text-muted)}.overview-value{margin-top:8px;font-size:22px;font-weight:700;color:var(--ui-text)}.overview-hint{margin-top:6px;font-size:13px;color:var(--ui-text-muted)}.workspace-card{padding:22px;border-radius:28px}.workspace-top{display:grid;grid-template-columns:1.2fr .8fr;gap:18px}.filters-panel{padding:18px;border-radius:22px}.panel-title{font-size:18px;font-weight:700;color:var(--ui-text)}.panel-subtitle{margin-top:6px;color:var(--ui-text-muted);font-size:13px}.filter-row{display:flex;gap:12px;flex-wrap:wrap;margin-top:16px}.filter{width:220px}.workflow-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}.workflow-card{padding:18px;border-radius:22px;text-align:left;cursor:pointer;transition:transform .2s ease,border-color .2s ease,box-shadow .2s ease}.workflow-card:hover{transform:translateY(-2px);border-color:var(--ui-accent)}.workflow-title{font-size:16px;font-weight:700;color:var(--ui-text)}.workflow-desc{margin-top:8px;color:var(--ui-text-muted);line-height:1.7}.workflow-cta{margin-top:16px;color:var(--ui-accent);font-weight:700}.tab-header{display:flex;justify-content:space-between;gap:18px;align-items:center;flex-wrap:wrap}.summary-strip{display:flex;gap:10px;flex-wrap:wrap}.summary-pill{display:flex;gap:10px;align-items:center;padding:10px 14px;border-radius:999px;background:var(--ui-surface-soft);border:1px solid var(--ui-border-soft);color:var(--ui-text-muted)}.summary-pill strong{color:var(--ui-text)}
@media (max-width: 1200px){.workspace-top,.overview-grid{grid-template-columns:1fr 1fr}.workflow-grid{grid-template-columns:1fr}}
@media (max-width: 900px){.hero,.workspace-top,.overview-grid{grid-template-columns:1fr;display:grid}.hero-actions{justify-content:flex-start}.filter{width:100%}}
</style>
