<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  createTeacherQuestion,
  deleteTeacherQuestion,
  fetchTeacherCourseDetail,
  fetchTeacherCourses,
  fetchTeacherQuestions,
  importTeacherQuestions,
  updateTeacherQuestion
} from '../api/teacher'
import { ElNotification } from 'element-plus'

const courses = ref([])
const chapters = ref([])
const questions = ref([])
const courseId = ref('')
const chapterId = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const dialogOpen = ref(false)
const importOpen = ref(false)
const saving = ref(false)
const importing = ref(false)
const editingId = ref(null)
const form = ref({
  title: '',
  type: 'single',
  difficulty: 1,
  analysis: '',
  courseId: '',
  chapterId: '',
  options: [
    { label: 'A', content: '', isCorrect: 1 },
    { label: 'B', content: '', isCorrect: 0 },
    { label: 'C', content: '', isCorrect: 0 },
    { label: 'D', content: '', isCorrect: 0 }
  ]
})
const importJson = ref(`[
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
]`)

const loadCourses = async () => {
  const res = await fetchTeacherCourses()
  courses.value = res.data || []
  if (!courseId.value && courses.value.length) {
    courseId.value = courses.value[0].id
  }
}

const loadChapters = async () => {
  if (!courseId.value) {
    chapters.value = []
    return
  }
  const res = await fetchTeacherCourseDetail(courseId.value)
  chapters.value = res.data?.chapters || []
}

const load = async () => {
  if (!courseId.value) return
  loading.value = true
  try {
    const res = await fetchTeacherQuestions({
      courseId: courseId.value,
      chapterId: chapterId.value || undefined,
      page: page.value,
      size: size.value
    })
    questions.value = res.data?.records || []
    total.value = res.data?.total || 0
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

const openCreate = () => {
  editingId.value = null
  form.value = {
    title: '',
    type: 'single',
    difficulty: 1,
    analysis: '',
    courseId: courseId.value,
    chapterId: chapterId.value || '',
    options: [
      { label: 'A', content: '', isCorrect: 1 },
      { label: 'B', content: '', isCorrect: 0 },
      { label: 'C', content: '', isCorrect: 0 },
      { label: 'D', content: '', isCorrect: 0 }
    ]
  }
  dialogOpen.value = true
}

const openImport = () => {
  importOpen.value = true
}

const closeImport = () => {
  importOpen.value = false
}

const closeDialog = () => {
  dialogOpen.value = false
}

const openEdit = (row) => {
  editingId.value = row.id
  form.value = {
    title: row.title,
    type: row.type,
    difficulty: row.difficulty || 1,
    analysis: row.analysis || '',
    courseId: row.courseId,
    chapterId: row.chapterId || '',
    options: (row.options || []).map((o) => ({
      label: o.label,
      content: o.content,
      isCorrect: o.isCorrect || 0
    }))
  }
  dialogOpen.value = true
}

const saveQuestion = async () => {
  if (saving.value) return
  if (!form.value.title || !form.value.courseId) {
    ElNotification({ title: '请完善题目', type: 'warning', duration: 1500 })
    return
  }
  try {
    saving.value = true
    if (editingId.value) {
      await updateTeacherQuestion(editingId.value, form.value)
    } else {
      await createTeacherQuestion(form.value)
    }
    closeDialog()
    await load()
  } catch (e) {
    ElNotification({ title: '保存失败', message: e?.message || '请稍后再试', type: 'error', duration: 2000 })
  } finally {
    saving.value = false
  }
}

const submitImport = async () => {
  if (!courseId.value) {
    ElNotification({ title: '请先选择课程', type: 'warning', duration: 1500 })
    return
  }
  let items = []
  try {
    const parsed = JSON.parse(importJson.value || '[]')
    if (!Array.isArray(parsed)) {
      throw new Error('导入内容必须是数组')
    }
    items = parsed
  } catch (e) {
    ElNotification({ title: 'JSON 格式错误', message: e?.message || '请检查格式', type: 'error', duration: 2200 })
    return
  }
  if (!items.length) {
    ElNotification({ title: '导入内容为空', type: 'warning', duration: 1500 })
    return
  }
  importing.value = true
  try {
    const res = await importTeacherQuestions({
      courseId: Number(courseId.value),
      chapterId: chapterId.value ? Number(chapterId.value) : null,
      items
    })
    ElNotification({
      title: '导入成功',
      message: `成功导入 ${res.data || 0} 题`,
      type: 'success',
      duration: 1800
    })
    importOpen.value = false
    await load()
  } catch (e) {
    ElNotification({ title: '导入失败', message: e?.message || '请稍后再试', type: 'error', duration: 2200 })
  } finally {
    importing.value = false
  }
}

const removeQuestion = async (row) => {
  try {
    await deleteTeacherQuestion(row.id)
    await load()
  } catch (e) {
    ElNotification({ title: '删除失败', message: e?.message || '请稍后再试', type: 'error', duration: 2000 })
  }
}

const onCourseChange = async () => {
  chapterId.value = ''
  await loadChapters()
  await load()
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

const toggleCorrect = (opt) => {
  if (form.value.type === 'single') {
    form.value.options.forEach((o) => (o.isCorrect = 0))
    opt.isCorrect = 1
  } else {
    opt.isCorrect = opt.isCorrect ? 0 : 1
  }
}

const typeLabel = computed(() => (t) => (t === 'multi' ? '多选题' : '单选题'))

onMounted(async () => {
  await loadCourses()
  await loadChapters()
  await load()
})
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">题库管理</div>
        <div class="subtitle">为课程建立题库，支持自动判题</div>
      </div>
      <div class="actions">
        <el-button native-type="button" @click="openImport">批量导入</el-button>
        <el-button native-type="button" type="primary" @click="openCreate">新建题目</el-button>
      </div>
    </div>

    <div class="filters">
      <el-select v-model="courseId" placeholder="选择课程" @change="onCourseChange" class="filter">
        <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
      </el-select>
      <el-select v-model="chapterId" placeholder="选择章节" @change="load" class="filter" clearable>
        <el-option v-for="c in chapters" :key="c.id" :label="c.title" :value="c.id" />
      </el-select>
    </div>

    <el-table :data="questions" v-loading="loading" class="table">
      <el-table-column label="题目标题" prop="title" min-width="260" />
      <el-table-column label="题型" width="100">
        <template #default="scope">{{ typeLabel(scope.row.type) }}</template>
      </el-table-column>
      <el-table-column label="难度" prop="difficulty" width="80" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button native-type="button" size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button native-type="button" size="small" type="danger" @click="removeQuestion(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

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

    <el-dialog
      v-model="dialogOpen"
      title="题目"
      width="680px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="true"
      append-to-body
      destroy-on-close
    >
      <el-form label-width="90">
        <el-form-item label="课程">
          <el-select v-model="form.courseId" class="filter">
            <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="章节">
          <el-select v-model="form.chapterId" clearable class="filter">
            <el-option v-for="c in chapters" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目标题">
          <el-input v-model="form.title" placeholder="题目标题" />
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="form.type">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multi" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="form.analysis" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="选项">
          <div class="option-grid">
            <div v-for="opt in form.options" :key="opt.label" class="option-item">
              <div class="option-label">{{ opt.label }}</div>
              <el-input v-model="opt.content" placeholder="选项内容" />
              <el-button native-type="button" size="small" @click="toggleCorrect(opt)">
                {{ opt.isCorrect ? '正确' : '设为正确' }}
              </el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button native-type="button" @click="closeDialog">取消</el-button>
        <el-button native-type="button" type="primary" :loading="saving" @click="saveQuestion">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="importOpen"
      title="题库批量导入（JSON）"
      width="760px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="true"
      append-to-body
      destroy-on-close
    >
      <div class="import-tip">导入到当前筛选的课程/章节。支持字段：title、type(single/multi)、difficulty、analysis、options[]。</div>
      <el-input
        v-model="importJson"
        type="textarea"
        :rows="18"
        placeholder="粘贴 JSON 数组"
      />
      <template #footer>
        <el-button native-type="button" @click="closeImport">取消</el-button>
        <el-button native-type="button" type="primary" :loading="importing" @click="submitImport">开始导入</el-button>
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
}

.actions {
  display: flex;
  gap: 10px;
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

.filters {
  display: flex;
  gap: 12px;
}

.filter {
  width: 200px;
}

.table {
  background: var(--ui-surface);
  border-radius: 14px;
  overflow: hidden;
}

.pager {
  display: flex;
  justify-content: flex-end;
}

.option-grid {
  display: grid;
  gap: 8px;
}

.option-item {
  display: grid;
  grid-template-columns: 36px 1fr auto;
  gap: 8px;
  align-items: center;
}

.option-label {
  font-weight: 700;
  color: var(--ui-text);
}

.import-tip {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-bottom: 8px;
}

</style>
