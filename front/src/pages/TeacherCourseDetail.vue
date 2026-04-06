<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import {
  addTeacherChapter,
  addTeacherLesson,
  deleteTeacherChapter,
  deleteTeacherLesson,
  fetchTeacherCourseDetail,
  updateTeacherChapter,
  updateTeacherLesson
} from '../api/teacher'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const savingChapter = ref(false)
const savingLesson = ref(false)
const detail = ref(null)
const selectedChapterId = ref(null)
const selectedLessonId = ref(null)
const chapterDialog = ref(false)
const lessonDialog = ref(false)
const editingChapterId = ref(null)
const editingLessonId = ref(null)
const chapterForm = ref({ title: '', sortNo: 1 })
const lessonForm = ref({
  title: '',
  contentType: 'text',
  contentUrl: '',
  contentText: '',
  sortNo: 1
})

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (part) => String(part).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const normalizeUrl = (value) => {
  const raw = String(value || '').trim()
  if (!raw) return ''
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('//')) return `https:${raw}`
  return `https://${raw}`
}

const contentTypeLabel = (value) => {
  if (value === 'text') return '图文课时'
  if (value === 'video') return '视频课时'
  return '资源课时'
}

const chapters = computed(() => detail.value?.chapters || [])
const totalLessons = computed(() => chapters.value.reduce((sum, chapter) => sum + ((chapter.lessons || []).length), 0))
const selectedChapter = computed(() => chapters.value.find((chapter) => chapter.id === selectedChapterId.value) || null)
const selectedLesson = computed(() => {
  const lessons = selectedChapter.value?.lessons || []
  return lessons.find((lesson) => lesson.id === selectedLessonId.value) || null
})

const chapterStats = computed(() => {
  return chapters.value.map((chapter) => ({
    id: chapter.id,
    title: chapter.title,
    lessonCount: (chapter.lessons || []).length,
    nextLessonTitle: chapter.lessons?.[0]?.title || '还没有课时',
    sortNo: chapter.sortNo
  }))
})

const focusChapter = (chapterId) => {
  selectedChapterId.value = chapterId
  const currentChapter = chapters.value.find((chapter) => chapter.id === chapterId)
  selectedLessonId.value = currentChapter?.lessons?.[0]?.id || null
}

const focusLesson = (chapterId, lessonId) => {
  selectedChapterId.value = chapterId
  selectedLessonId.value = lessonId
}

const initializeSelection = () => {
  const firstChapter = chapters.value[0] || null
  if (!firstChapter) {
    selectedChapterId.value = null
    selectedLessonId.value = null
    return
  }
  selectedChapterId.value = firstChapter.id
  selectedLessonId.value = firstChapter.lessons?.[0]?.id || null
}

const load = async () => {
  loading.value = true
  try {
    const response = await fetchTeacherCourseDetail(route.params.id)
    detail.value = response.data
    initializeSelection()
  } catch (exception) {
    ElNotification({
      title: '加载失败',
      message: exception?.message || '课程详情加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const openAddChapter = () => {
  editingChapterId.value = null
  chapterForm.value = { title: '', sortNo: chapters.value.length + 1 }
  chapterDialog.value = true
}

const openEditChapter = (chapter) => {
  editingChapterId.value = chapter.id
  chapterForm.value = { title: chapter.title || '', sortNo: chapter.sortNo || 1 }
  chapterDialog.value = true
}

const saveChapter = async () => {
  if (savingChapter.value) return
  if (!chapterForm.value.title.trim()) {
    ElNotification({ title: '请填写章节标题', message: '章节标题不能为空', type: 'warning', duration: 1500 })
    return
  }
  try {
    savingChapter.value = true
    const payload = {
      title: chapterForm.value.title.trim(),
      sortNo: chapterForm.value.sortNo || 1
    }
    if (editingChapterId.value) {
      await updateTeacherChapter(editingChapterId.value, payload)
    } else {
      await addTeacherChapter(route.params.id, payload)
    }
    chapterDialog.value = false
    await load()
    ElNotification({ title: '保存成功', message: '章节已更新', type: 'success', duration: 1500 })
  } catch (exception) {
    ElNotification({ title: '保存失败', message: exception?.message || '请稍后再试', type: 'error', duration: 2000 })
  } finally {
    savingChapter.value = false
  }
}

const removeChapter = async (chapter) => {
  try {
    await deleteTeacherChapter(chapter.id)
    await load()
    ElNotification({ title: '已删除', message: '章节已删除', type: 'success', duration: 1500 })
  } catch (exception) {
    ElNotification({ title: '删除失败', message: exception?.message || '请稍后再试', type: 'error', duration: 2000 })
  }
}

const openAddLesson = () => {
  if (!selectedChapter.value) {
    ElNotification({ title: '请先选择章节', message: '先创建或选中章节，再添加课时。', type: 'warning', duration: 1500 })
    return
  }
  editingLessonId.value = null
  lessonForm.value = {
    title: '',
    contentType: 'text',
    contentUrl: '',
    contentText: '',
    sortNo: (selectedChapter.value.lessons || []).length + 1
  }
  lessonDialog.value = true
}

const openEditLesson = (lesson) => {
  editingLessonId.value = lesson.id
  lessonForm.value = {
    title: lesson.title || '',
    contentType: lesson.contentType || 'text',
    contentUrl: lesson.contentUrl || '',
    contentText: lesson.contentText || '',
    sortNo: lesson.sortNo || 1
  }
  lessonDialog.value = true
}

const saveLesson = async () => {
  if (savingLesson.value || !selectedChapter.value) return
  if (!lessonForm.value.title.trim()) {
    ElNotification({ title: '请填写课时标题', message: '课时标题不能为空', type: 'warning', duration: 1500 })
    return
  }
  try {
    savingLesson.value = true
    const payload = {
      title: lessonForm.value.title.trim(),
      contentType: lessonForm.value.contentType,
      contentUrl: normalizeUrl(lessonForm.value.contentUrl),
      contentText: lessonForm.value.contentText || '',
      sortNo: lessonForm.value.sortNo || 1
    }
    if (editingLessonId.value) {
      await updateTeacherLesson(editingLessonId.value, payload)
    } else {
      await addTeacherLesson(selectedChapter.value.id, payload)
    }
    lessonDialog.value = false
    await load()
    ElNotification({ title: '保存成功', message: '课时已更新', type: 'success', duration: 1500 })
  } catch (exception) {
    ElNotification({ title: '保存失败', message: exception?.message || '请稍后再试', type: 'error', duration: 2000 })
  } finally {
    savingLesson.value = false
  }
}

const removeLesson = async (lesson) => {
  try {
    await deleteTeacherLesson(lesson.id)
    await load()
    ElNotification({ title: '已删除', message: '课时已删除', type: 'success', duration: 1500 })
  } catch (exception) {
    ElNotification({ title: '删除失败', message: exception?.message || '请稍后再试', type: 'error', duration: 2000 })
  }
}

const goQuestionBank = () => {
  router.push({
    path: '/teacher/questions',
    query: {
      courseId: route.params.id,
      ...(selectedChapterId.value ? { chapterId: selectedChapterId.value } : {})
    }
  })
}

const goHomework = () => {
  router.push({
    path: '/teacher/homework',
    query: { courseId: route.params.id }
  })
}
const goExams = () => router.push('/teacher/exams')

onMounted(load)
</script>

<template>
  <div class="page">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="hero skeleton-box"></div>
      </template>

      <template #default>
        <section class="hero">
          <div class="hero-main">
            <div class="hero-eyebrow">Curriculum Editor</div>
            <div class="hero-title">{{ detail?.title || '课程编排' }}</div>
            <div class="hero-subtitle">{{ detail?.intro || '建议补充课程目标、章节节奏与产出预期，方便学生理解主线。' }}</div>
            <div class="hero-meta">
              <span>维护状态：{{ detail?.finishStatus === 1 ? '已完结' : '更新中' }}</span>
              <span>发布时间：{{ formatDate(detail?.createdAt) }}</span>
              <span>最近更新：{{ formatDate(detail?.updatedAt) }}</span>
            </div>
            <div class="hero-actions">
              <el-button type="primary" @click="openAddChapter">新建章节</el-button>
              <el-button :disabled="!selectedChapter" @click="openAddLesson">新增课时</el-button>
              <el-button plain @click="goQuestionBank">去题库工作台</el-button>
              <el-button plain @click="goHomework">去作业管理</el-button>
              <el-button plain @click="goExams">去考试任务</el-button>
            </div>
          </div>

          <div class="hero-side">
            <div class="stat-card">
              <div class="stat-label">章节数量</div>
              <div class="stat-value">{{ chapters.length }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">课时数量</div>
              <div class="stat-value">{{ totalLessons }}</div>
            </div>
            <div class="stat-card accent">
              <div class="stat-label">当前焦点</div>
              <div class="stat-value small">{{ selectedChapter?.title || '先创建章节' }}</div>
            </div>
          </div>
        </section>

        <section class="chapter-overview">
          <button
            v-for="chapter in chapterStats"
            :key="chapter.id"
            type="button"
            class="chapter-card"
            :class="{ active: chapter.id === selectedChapterId }"
            @click="focusChapter(chapter.id)"
          >
            <div class="chapter-title-row">
              <div class="chapter-title">{{ chapter.title }}</div>
              <el-tag size="small">#{{ chapter.sortNo }}</el-tag>
            </div>
            <div class="chapter-meta">{{ chapter.lessonCount }} 节课时</div>
            <div class="chapter-next">{{ chapter.nextLessonTitle }}</div>
          </button>
        </section>

        <section class="workspace">
          <el-card class="panel structure-panel" shadow="never">
            <template #header>
              <div class="panel-head">
                <div>
                  <div class="panel-title">结构编排</div>
                  <div class="panel-subtitle">先调整章节，再补课时内容。</div>
                </div>
                <el-button size="small" type="primary" @click="openAddChapter">新增章节</el-button>
              </div>
            </template>

            <div v-if="chapters.length" class="chapter-list">
              <div v-for="chapter in chapters" :key="chapter.id" class="chapter-block">
                <div class="chapter-block-head">
                  <button type="button" class="chapter-select" :class="{ active: chapter.id === selectedChapterId }" @click="focusChapter(chapter.id)">
                    <span>{{ chapter.title }}</span>
                    <span class="muted">{{ chapter.lessons?.length || 0 }} 节</span>
                  </button>
                  <div class="mini-actions">
                    <el-button size="small" link @click="openEditChapter(chapter)">编辑</el-button>
                    <el-button size="small" link type="danger" @click="removeChapter(chapter)">删除</el-button>
                  </div>
                </div>

                <div class="lesson-list">
                  <button
                    v-for="lesson in chapter.lessons || []"
                    :key="lesson.id"
                    type="button"
                    class="lesson-item"
                    :class="{ active: lesson.id === selectedLessonId }"
                    @click="focusLesson(chapter.id, lesson.id)"
                  >
                    <div>
                      <div class="lesson-title">{{ lesson.title }}</div>
                      <div class="lesson-meta">{{ contentTypeLabel(lesson.contentType) }} · 排序 {{ lesson.sortNo || 1 }}</div>
                    </div>
                    <div class="mini-actions">
                      <el-button size="small" link @click.stop="openEditLesson(lesson)">编辑</el-button>
                      <el-button size="small" link type="danger" @click.stop="removeLesson(lesson)">删除</el-button>
                    </div>
                  </button>
                </div>
              </div>
            </div>

            <el-empty v-else description="还没有章节，先创建课程结构" />
          </el-card>

          <el-card class="panel workspace-panel" shadow="never">
            <template #header>
              <div class="panel-head">
                <div>
                  <div class="panel-title">当前工作区</div>
                  <div class="panel-subtitle">在这里查看选中章节和课时的细节。</div>
                </div>
                <el-button size="small" :disabled="!selectedChapter" @click="openAddLesson">新增课时</el-button>
              </div>
            </template>

            <template v-if="selectedChapter">
              <div class="focus-card">
                <div class="focus-label">当前章节</div>
                <div class="focus-title">{{ selectedChapter.title }}</div>
                <div class="focus-meta">排序 {{ selectedChapter.sortNo || 1 }} · {{ selectedChapter.lessons?.length || 0 }} 节课时</div>
              </div>

              <div v-if="selectedLesson" class="lesson-preview">
                <div class="focus-label">当前课时</div>
                <div class="focus-title">{{ selectedLesson.title }}</div>
                <div class="focus-meta">{{ contentTypeLabel(selectedLesson.contentType) }} · 排序 {{ selectedLesson.sortNo || 1 }}</div>
                <div class="lesson-body">
                  <div v-if="selectedLesson.contentType === 'text'" class="lesson-text">
                    {{ selectedLesson.contentText || '暂无文本内容' }}
                  </div>
                  <div v-else class="lesson-link">
                    资源地址：
                    <a :href="normalizeUrl(selectedLesson.contentUrl)" target="_blank" rel="noreferrer">
                      {{ normalizeUrl(selectedLesson.contentUrl) || '未填写资源地址' }}
                    </a>
                  </div>
                </div>
                <div class="hero-actions">
                  <el-button type="primary" @click="openEditLesson(selectedLesson)">编辑当前课时</el-button>
                  <el-button plain @click="goQuestionBank">去补题目</el-button>
                </div>
              </div>

              <el-empty v-else description="当前章节还没有课时，建议先新增一节课时" />
            </template>

            <el-empty v-else description="还没有章节，先从新建章节开始" />
          </el-card>
        </section>
      </template>
    </el-skeleton>

    <el-dialog v-model="chapterDialog" :title="editingChapterId ? '编辑章节' : '新建章节'" width="520px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="章节标题">
          <el-input v-model="chapterForm.title" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="chapterForm.sortNo" :min="1" :max="99" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="chapterDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingChapter" @click="saveChapter">保存章节</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="lessonDialog" :title="editingLessonId ? '编辑课时' : '新建课时'" width="620px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="课时标题">
          <el-input v-model="lessonForm.title" maxlength="100" show-word-limit />
        </el-form-item>
        <div class="dialog-grid">
          <el-form-item label="内容类型">
            <el-select v-model="lessonForm.contentType">
              <el-option label="图文课时" value="text" />
              <el-option label="视频课时" value="video" />
              <el-option label="资源课时" value="file" />
            </el-select>
          </el-form-item>
          <el-form-item label="排序值">
            <el-input-number v-model="lessonForm.sortNo" :min="1" :max="999" />
          </el-form-item>
        </div>
        <el-form-item v-if="lessonForm.contentType === 'text'" label="图文内容">
          <el-input v-model="lessonForm.contentText" type="textarea" :rows="6" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item v-else label="资源链接">
          <el-input v-model="lessonForm.contentUrl" placeholder="https://example.com/resource" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="lessonDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingLesson" @click="saveLesson">保存课时</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 18px;
}

.skeleton-box {
  min-height: 260px;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) 320px;
  gap: 16px;
  padding: 22px;
  border-radius: 24px;
  border: 1px solid var(--ui-border);
  background:
    radial-gradient(circle at 14% 12%, color-mix(in srgb, var(--ui-accent) 18%, transparent), transparent 28%),
    radial-gradient(circle at 88% 14%, color-mix(in srgb, var(--ui-accent-2) 20%, transparent), transparent 24%),
    var(--ui-content-bg);
  box-shadow: var(--ui-content-shadow);
}

.hero-main,
.hero-side,
.chapter-overview,
.workspace,
.chapter-list,
.lesson-list,
.focus-card,
.lesson-preview,
.lesson-body,
.stat-card {
  display: grid;
  gap: 12px;
}

.hero-eyebrow,
.hero-subtitle,
.hero-meta,
.stat-label,
.chapter-meta,
.chapter-next,
.panel-subtitle,
.focus-label,
.focus-meta,
.lesson-meta,
.muted {
  color: var(--ui-text-muted);
}

.hero-eyebrow,
.focus-label {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.hero-title,
.focus-title,
.panel-title,
.chapter-title {
  font-weight: 700;
  color: var(--ui-text);
}

.hero-title {
  font-size: clamp(28px, 4vw, 38px);
}

.hero-meta,
.hero-actions,
.panel-head,
.chapter-title-row,
.chapter-block-head,
.mini-actions,
.dialog-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-side {
  align-content: start;
}

.stat-card,
.chapter-card,
.panel,
.focus-card,
.lesson-preview,
.chapter-select,
.lesson-item {
  padding: 14px;
  border-radius: 18px;
  border: 1px solid var(--ui-border-soft);
  background: color-mix(in srgb, var(--ui-surface) 95%, transparent);
}

.stat-card.accent {
  background: linear-gradient(135deg, color-mix(in srgb, var(--ui-accent) 12%, transparent), color-mix(in srgb, var(--ui-accent-2) 12%, transparent));
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--ui-text);
}

.stat-value.small {
  font-size: 16px;
}

.chapter-overview {
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.chapter-card,
.chapter-select,
.lesson-item {
  text-align: left;
  cursor: pointer;
}

.chapter-card.active,
.chapter-select.active,
.lesson-item.active {
  border-color: var(--ui-border);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.workspace {
  grid-template-columns: minmax(320px, 0.9fr) minmax(0, 1.1fr);
  gap: 16px;
}

.panel {
  border: 1px solid var(--ui-border);
  background: color-mix(in srgb, var(--ui-surface) 96%, transparent);
}

:deep(.panel .el-card__header) {
  border-bottom: 1px solid var(--ui-border-soft);
}

.chapter-block {
  display: grid;
  gap: 10px;
}

.chapter-select,
.lesson-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.lesson-title {
  font-weight: 600;
  color: var(--ui-text);
}

.lesson-body {
  min-height: 180px;
}

.lesson-text,
.lesson-link {
  color: var(--ui-text);
  line-height: 1.7;
  white-space: pre-wrap;
}

.lesson-link a {
  color: var(--ui-accent-2);
  word-break: break-all;
}

.dialog-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
}

@media (max-width: 1100px) {
  .hero,
  .workspace {
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

  .dialog-grid {
    grid-template-columns: 1fr;
  }

  .chapter-select,
  .lesson-item {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
