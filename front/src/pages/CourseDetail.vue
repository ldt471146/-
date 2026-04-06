<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import http from '../api/http'
import { getToken } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const detail = ref(null)
const loading = ref(false)
const progressMap = ref({})
const resumeLessonId = ref(null)
const activeChapter = ref([])
const selectedLessonId = ref(null)
const activeLessonId = ref(null)
const activeStartTime = ref(0)

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (part) => String(part).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const findChapterIdsByLesson = (course, lessonId) => {
  if (!course?.chapters) return []
  for (const chapter of course.chapters) {
    if ((chapter.lessons || []).some((lesson) => lesson.id === lessonId)) {
      return [chapter.id]
    }
  }
  return []
}

const lessonsFlat = computed(() => {
  const list = []
  detail.value?.chapters?.forEach((chapter) => {
    ;(chapter.lessons || []).forEach((lesson) => {
      list.push({
        ...lesson,
        chapterId: chapter.id,
        chapterTitle: chapter.title
      })
    })
  })
  return list
})

const findResumeLessonId = (course, recordMap) => {
  if (!course?.chapters) return null
  for (const chapter of course.chapters) {
    for (const lesson of chapter.lessons || []) {
      const record = recordMap[lesson.id]
      if (!record || record.isFinished !== 1) {
        return lesson.id
      }
    }
  }
  return null
}

const totalLessons = computed(() => lessonsFlat.value.length)
const finishedLessons = computed(
  () => lessonsFlat.value.filter((lesson) => progressMap.value[lesson.id]?.isFinished === 1).length
)
const courseProgress = computed(() => {
  if (!totalLessons.value) return 0
  return Math.round((finishedLessons.value / totalLessons.value) * 100)
})

const unlockedSet = computed(() => {
  const set = new Set()
  const orderedLessons = lessonsFlat.value
  for (let index = 0; index < orderedLessons.length; index += 1) {
    const lesson = orderedLessons[index]
    if (index === 0) {
      set.add(lesson.id)
      continue
    }
    const previousLesson = orderedLessons[index - 1]
    if (progressMap.value[previousLesson.id]?.isFinished === 1) {
      set.add(lesson.id)
    }
  }
  return set
})

const resumeLesson = computed(() => lessonsFlat.value.find((lesson) => lesson.id === resumeLessonId.value) || null)
const selectedLesson = computed(() => lessonsFlat.value.find((lesson) => lesson.id === selectedLessonId.value) || null)
const selectedLessonProgress = computed(() => {
  if (!selectedLesson.value) return 0
  return progressMap.value[selectedLesson.value.id]?.progress || 0
})

const chapterProgressList = computed(() => {
  return (detail.value?.chapters || []).map((chapter) => {
    const lessons = chapter.lessons || []
    const finished = lessons.filter((lesson) => progressMap.value[lesson.id]?.isFinished === 1).length
    const progress = lessons.length ? Math.round((finished / lessons.length) * 100) : 0
    const nextLesson = lessons.find(
      (lesson) => unlockedSet.value.has(lesson.id) && progressMap.value[lesson.id]?.isFinished !== 1
    ) || null

    return {
      id: chapter.id,
      title: chapter.title,
      total: lessons.length,
      finished,
      progress,
      nextLessonId: nextLesson?.id || null,
      nextLessonTitle: nextLesson?.title || '',
      completed: lessons.length > 0 && finished === lessons.length
    }
  })
})

const nextLesson = computed(() => {
  const availableLessons = lessonsFlat.value.filter((lesson) => unlockedSet.value.has(lesson.id))
  const currentIndex = availableLessons.findIndex((lesson) => lesson.id === selectedLessonId.value)
  if (currentIndex >= 0) {
    for (let index = currentIndex + 1; index < availableLessons.length; index += 1) {
      const candidate = availableLessons[index]
      if (progressMap.value[candidate.id]?.isFinished !== 1) {
        return candidate
      }
    }
  }
  return availableLessons.find((lesson) => progressMap.value[lesson.id]?.isFinished !== 1) || null
})

const learningRhythm = computed(() => {
  if (courseProgress.value >= 100) return '课程主线已完成，建议去题库或考试做一次综合检验。'
  if (courseProgress.value >= 70) return '你已经进入收尾阶段，优先连续完成剩余课程。'
  if (courseProgress.value > 0) return '保持当前节奏，先把最近一章学完整体体验会更顺。'
  return '从第一章开始建立节奏，系统会自动帮你记住学习断点。'
})

const focusLesson = (lessonId) => {
  if (!lessonId) return
  selectedLessonId.value = lessonId
  activeChapter.value = findChapterIdsByLesson(detail.value, lessonId)
}

const initializeSelection = () => {
  const queryLessonId = Number(route.query.lessonId || 0)
  const fallbackLessonId = queryLessonId || resumeLessonId.value || lessonsFlat.value[0]?.id || null
  if (fallbackLessonId) {
    focusLesson(fallbackLessonId)
  }
}

const load = async () => {
  loading.value = true
  try {
    const [courseResponse, recordResponse] = await Promise.all([
      http.get(`/api/courses/${route.params.id}`),
      http.get('/api/learn/records')
    ])
    detail.value = courseResponse.data
    progressMap.value = (recordResponse.data || []).reduce((accumulator, record) => {
      accumulator[record.lessonId] = record
      return accumulator
    }, {})
    resumeLessonId.value = findResumeLessonId(detail.value, progressMap.value)
    initializeSelection()
  } catch (exception) {
    ElNotification({
      title: '课程详情加载失败',
      message: exception?.message || '请稍后重试',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const finalizeLesson = async (useKeepalive = false) => {
  if (!activeLessonId.value || !activeStartTime.value) return

  const lessonId = activeLessonId.value
  const durationSeconds = Math.max(1, Math.round((Date.now() - activeStartTime.value) / 1000))
  const currentProgress = progressMap.value[lessonId]?.progress || 1
  const payload = {
    lessonId,
    progress: currentProgress,
    durationSeconds
  }

  activeLessonId.value = null
  activeStartTime.value = 0

  if (!useKeepalive) {
    await http.post('/api/learn/progress', payload)
    return
  }

  const token = getToken()
  try {
    await fetch('/api/learn/progress', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: JSON.stringify(payload),
      keepalive: true
    })
  } catch {
    // ignore unload flush errors
  }
}

const markProgress = async (lessonId, progress, withDuration = false, silent = false) => {
  try {
    const payload = { lessonId, progress }
    if (withDuration && activeLessonId.value === lessonId && activeStartTime.value) {
      payload.durationSeconds = Math.max(1, Math.round((Date.now() - activeStartTime.value) / 1000))
      activeLessonId.value = null
      activeStartTime.value = 0
    }

    await http.post('/api/learn/progress', payload)

    const currentProgress = progressMap.value[lessonId]?.progress || 0
    const finalProgress = Math.max(currentProgress, progress)
    progressMap.value[lessonId] = {
      lessonId,
      progress: finalProgress,
      isFinished: finalProgress >= 100 ? 1 : 0
    }
    resumeLessonId.value = findResumeLessonId(detail.value, progressMap.value)

    if (!silent) {
      ElNotification({
        title: '学习进度已更新',
        message: finalProgress >= 100 ? '这一节已经完成，继续保持。' : `当前进度 ${finalProgress}%`,
        type: 'success',
        duration: 1600
      })
    }
  } catch (exception) {
    ElNotification({
      title: '进度更新失败',
      message: exception?.message || '请稍后重试',
      type: 'error',
      duration: 2000
    })
  }
}

const startLesson = async (lessonId) => {
  if (!lessonId) return
  if (activeLessonId.value && activeLessonId.value !== lessonId) {
    await finalizeLesson()
  }
  focusLesson(lessonId)
  activeLessonId.value = lessonId
  activeStartTime.value = Date.now()
  const currentProgress = progressMap.value[lessonId]?.progress || 0
  await markProgress(lessonId, Math.max(currentProgress, 1), false, true)
}

const resumeLearning = async () => {
  if (!resumeLessonId.value) return
  await startLesson(resumeLessonId.value)
}

const completeAndContinue = async () => {
  if (!selectedLesson.value) return
  const targetLessonId = nextLesson.value?.id || null
  await markProgress(selectedLesson.value.id, 100, true)
  if (targetLessonId && targetLessonId !== selectedLesson.value.id) {
    focusLesson(targetLessonId)
  }
}

const getLessonProgress = (lessonId) => progressMap.value[lessonId]?.progress || 0

const goPractice = () => {
  router.push('/practice')
}

const goLearningPath = () => {
  router.push({ path: '/learning-path', query: { courseId: route.params.id } })
}

const handleBeforeUnload = () => {
  finalizeLesson(true)
}

onMounted(() => {
  load()
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
  finalizeLesson(true)
})
</script>

<template>
  <div class="page">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="banner skeleton-box"></div>
      </template>

      <template #default>
        <section class="banner">
          <div class="banner-main">
            <div class="banner-eyebrow">Course Workspace</div>
            <div class="banner-title">{{ detail?.title || '课程详情' }}</div>
            <div class="banner-intro">{{ detail?.intro || '暂无简介' }}</div>
            <div class="banner-meta">
              <span>讲师：{{ detail?.teacherName || '-' }}</span>
              <span>状态：{{ detail?.finishStatus === 1 ? '已完结' : '更新中' }}</span>
              <span>发布时间：{{ detail?.createdAt ? formatDate(detail.createdAt) : '-' }}</span>
            </div>
            <div class="banner-actions">
              <el-button type="primary" :disabled="!resumeLessonId" @click="resumeLearning">继续学习</el-button>
              <el-button plain @click="goPractice">去题库练习</el-button>
              <el-button plain @click="goLearningPath">学习路径</el-button>
            </div>
            <div class="banner-rhythm">{{ learningRhythm }}</div>
          </div>

          <div class="banner-side">
            <div class="stat-grid">
              <div class="stat-card">
                <div class="stat-label">课程进度</div>
                <div class="stat-value">{{ courseProgress }}%</div>
                <el-progress :percentage="courseProgress" :stroke-width="8" />
              </div>
              <div class="stat-card">
                <div class="stat-label">已完成课时</div>
                <div class="stat-value">{{ finishedLessons }}/{{ totalLessons }}</div>
                <div class="stat-sub">按章节逐步解锁</div>
              </div>
              <div class="stat-card">
                <div class="stat-label">章节数量</div>
                <div class="stat-value">{{ detail?.chapters?.length || 0 }}</div>
                <div class="stat-sub">学习内容分段推进</div>
              </div>
            </div>

            <div class="pace-card">
              <div class="pace-label">下一步建议</div>
              <div class="pace-title">{{ nextLesson?.title || '课程主线已全部完成' }}</div>
              <div class="pace-copy">
                {{ nextLesson ? `${nextLesson.chapterTitle} · ${nextLesson.contentType}` : '可以转去题库、编程练习或考试做阶段检验。' }}
              </div>
              <div class="pace-actions">
                <el-button v-if="nextLesson" size="small" type="primary" @click="focusLesson(nextLesson.id)">
                  查看下一节
                </el-button>
                <el-button size="small" plain @click="goPractice">去练习</el-button>
              </div>
            </div>
          </div>
        </section>

        <section class="chapter-section">
          <div class="section-head">
            <div>
              <div class="section-title">章节推进</div>
              <div class="section-subtitle">一眼看清每章的完成度，以及下一节应该点哪里。</div>
            </div>
          </div>

          <div class="chapter-grid">
            <button
              v-for="chapter in chapterProgressList"
              :key="chapter.id"
              type="button"
              class="chapter-card"
              :class="{ active: activeChapter.includes(chapter.id), completed: chapter.completed }"
              @click="chapter.nextLessonId ? focusLesson(chapter.nextLessonId) : null"
            >
              <div class="chapter-card-top">
                <div class="chapter-card-title">{{ chapter.title }}</div>
                <el-tag size="small" :type="chapter.completed ? 'success' : 'info'">
                  {{ chapter.completed ? '已完成' : '进行中' }}
                </el-tag>
              </div>
              <div class="chapter-card-meta">{{ chapter.finished }}/{{ chapter.total }} 节已完成</div>
              <el-progress :percentage="chapter.progress" :stroke-width="8" />
              <div class="chapter-card-next">
                {{ chapter.nextLessonTitle ? `下一节：${chapter.nextLessonTitle}` : '这一章已全部完成' }}
              </div>
            </button>
          </div>
        </section>

        <section class="workspace">
          <el-card class="panel lesson-list-panel" shadow="never">
            <template #header>
              <div class="panel-head">
                <div>
                  <div class="panel-title">学习目录</div>
                  <div class="panel-subtitle">按顺序解锁，保持稳定节奏。</div>
                </div>
              </div>
            </template>

            <el-collapse v-model="activeChapter" accordion>
              <el-collapse-item v-for="chapter in detail?.chapters || []" :key="chapter.id" :name="chapter.id">
                <template #title>
                  <div class="collapse-title">
                    <span>{{ chapter.title }}</span>
                    <span class="collapse-meta">
                      {{ chapter.lessons?.filter((lesson) => progressMap[lesson.id]?.isFinished === 1).length || 0 }}/{{ chapter.lessons?.length || 0 }}
                    </span>
                  </div>
                </template>

                <div class="lesson-list">
                  <button
                    v-for="lesson in chapter.lessons"
                    :key="lesson.id"
                    type="button"
                    class="lesson-item"
                    :class="{
                      active: selectedLessonId === lesson.id,
                      locked: !unlockedSet.has(lesson.id)
                    }"
                    @click="unlockedSet.has(lesson.id) ? focusLesson(lesson.id) : null"
                  >
                    <div class="lesson-copy">
                      <div class="lesson-title">{{ lesson.title }}</div>
                      <div class="lesson-meta">
                        <span>{{ lesson.contentType }}</span>
                        <span v-if="activeLessonId === lesson.id">学习中...</span>
                      </div>
                    </div>
                    <div class="lesson-state">
                      <el-tag v-if="progressMap[lesson.id]?.isFinished === 1" type="success" effect="dark">已完成</el-tag>
                      <el-tag v-else-if="!unlockedSet.has(lesson.id)" type="warning" effect="dark">未解锁</el-tag>
                      <el-progress v-else :percentage="getLessonProgress(lesson.id)" :stroke-width="8" class="lesson-progress" />
                    </div>
                  </button>
                </div>
              </el-collapse-item>
            </el-collapse>
          </el-card>

          <el-card class="panel lesson-detail-panel" shadow="never">
            <template #header>
              <div class="panel-head">
                <div>
                  <div class="panel-title">学习工作区</div>
                  <div class="panel-subtitle">当前选中内容、进度与下一步建议都在这里。</div>
                </div>
              </div>
            </template>

            <template v-if="selectedLesson">
              <div class="detail-title">{{ selectedLesson.title }}</div>
              <div class="detail-meta">
                <span>{{ selectedLesson.chapterTitle }}</span>
                <span>{{ selectedLesson.contentType }}</span>
                <span>当前进度 {{ selectedLessonProgress }}%</span>
              </div>

              <div class="detail-progress">
                <el-progress :percentage="selectedLessonProgress" :stroke-width="10" />
              </div>

              <div class="detail-body">
                <div v-if="selectedLesson.contentType === 'text'" class="detail-text">
                  {{ selectedLesson.contentText || '暂无文本内容' }}
                </div>
                <div v-else-if="selectedLesson.contentType === 'video'" class="detail-link">
                  视频地址：
                  <a :href="selectedLesson.contentUrl" target="_blank" rel="noreferrer">{{ selectedLesson.contentUrl }}</a>
                </div>
                <div v-else class="detail-link">
                  资源地址：
                  <a :href="selectedLesson.contentUrl" target="_blank" rel="noreferrer">{{ selectedLesson.contentUrl }}</a>
                </div>
              </div>

              <div v-if="nextLesson" class="next-card">
                <div class="next-label">学完这节之后</div>
                <div class="next-title">{{ nextLesson.title }}</div>
                <div class="next-copy">{{ nextLesson.chapterTitle }} · {{ nextLesson.contentType }}</div>
                <el-button size="small" plain @click="focusLesson(nextLesson.id)">提前查看下一节</el-button>
              </div>

              <div class="detail-actions">
                <el-button size="small" type="primary" @click="startLesson(selectedLesson.id)">开始学习</el-button>
                <el-button size="small" @click="markProgress(selectedLesson.id, 60)">学到 60%</el-button>
                <el-button size="small" type="success" @click="completeAndContinue">完成并继续</el-button>
                <el-button size="small" plain @click="goPractice">去题库练习</el-button>
                <el-button size="small" plain @click="goLearningPath">学习路径</el-button>
              </div>
            </template>

            <el-empty v-else description="请选择左侧课程内容开始学习" />
          </el-card>
        </section>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 18px;
}

.skeleton-box {
  min-height: 280px;
}

.banner,
.panel,
.chapter-card {
  border-radius: 24px;
}

.banner {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(300px, 0.9fr);
  gap: 16px;
  padding: 22px;
  border: 1px solid var(--ui-border);
  background:
    radial-gradient(circle at 10% 10%, color-mix(in srgb, var(--ui-accent) 18%, transparent), transparent 28%),
    radial-gradient(circle at 92% 12%, color-mix(in srgb, var(--ui-accent-2) 20%, transparent), transparent 26%),
    var(--ui-content-bg);
  box-shadow: var(--ui-content-shadow);
}

.banner-main,
.banner-side,
.pace-card,
.chapter-card,
.lesson-item,
.next-card,
.detail-body {
  display: grid;
  gap: 12px;
}

.banner-eyebrow,
.section-subtitle,
.panel-subtitle,
.banner-meta,
.banner-rhythm,
.stat-label,
.stat-sub,
.chapter-card-meta,
.chapter-card-next,
.lesson-meta,
.detail-meta,
.next-label,
.next-copy {
  color: var(--ui-text-muted);
}

.banner-eyebrow,
.next-label {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.banner-title {
  font-size: clamp(28px, 4vw, 40px);
  line-height: 1.04;
  font-weight: 700;
  color: var(--ui-text);
}

.banner-intro {
  max-width: 640px;
  font-size: 14px;
  color: var(--ui-text);
}

.banner-meta,
.banner-actions,
.detail-actions,
.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.stat-card,
.pace-card,
.panel,
.chapter-card,
.detail-body,
.next-card,
.lesson-item {
  border: 1px solid var(--ui-border-soft);
  background: color-mix(in srgb, var(--ui-surface) 94%, transparent);
}

.stat-card,
.pace-card {
  padding: 14px;
  border-radius: 18px;
}

.stat-value,
.pace-title,
.detail-title,
.section-title,
.panel-title,
.chapter-card-title {
  font-weight: 700;
  color: var(--ui-text);
}

.stat-value {
  font-size: 26px;
}

.pace-title,
.detail-title,
.section-title,
.panel-title {
  font-size: 20px;
}

.pace-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.chapter-section {
  display: grid;
  gap: 14px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 12px;
}

.chapter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.chapter-card {
  padding: 14px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.chapter-card:hover,
.chapter-card.active {
  transform: translateY(-2px);
  border-color: var(--ui-border);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.14);
}

.chapter-card.completed {
  background: linear-gradient(135deg, color-mix(in srgb, var(--ui-accent) 12%, transparent), color-mix(in srgb, var(--ui-surface) 94%, transparent));
}

.chapter-card-top,
.panel-head,
.collapse-title {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.workspace {
  display: grid;
  grid-template-columns: minmax(300px, 0.9fr) minmax(0, 1.1fr);
  gap: 16px;
}

.panel {
  border: 1px solid var(--ui-border);
  background: color-mix(in srgb, var(--ui-surface) 96%, transparent);
}

:deep(.panel .el-card__header) {
  border-bottom: 1px solid var(--ui-border-soft);
}

:deep(.panel .el-card__body) {
  display: grid;
  gap: 14px;
}

.collapse-title {
  width: 100%;
  align-items: center;
}

.collapse-meta {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.lesson-list {
  display: grid;
  gap: 10px;
}

.lesson-item {
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  padding: 12px;
  border-radius: 16px;
  text-align: left;
  cursor: pointer;
}

.lesson-item.active {
  border-color: var(--ui-border);
  background: linear-gradient(135deg, color-mix(in srgb, var(--ui-accent) 10%, transparent), color-mix(in srgb, var(--ui-surface) 96%, transparent));
}

.lesson-item.locked {
  opacity: 0.66;
  cursor: not-allowed;
}

.lesson-title {
  font-weight: 600;
  color: var(--ui-text);
}

.lesson-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
}

.lesson-progress {
  width: 110px;
}

.detail-progress,
.detail-body,
.next-card {
  padding: 14px;
  border-radius: 18px;
}

.detail-body {
  min-height: 180px;
}

.detail-text {
  white-space: pre-wrap;
  color: var(--ui-text);
  line-height: 1.7;
}

.detail-link {
  color: var(--ui-text);
}

.detail-link a {
  color: var(--ui-accent-2);
  word-break: break-all;
}

.next-card {
  background: linear-gradient(135deg, color-mix(in srgb, var(--ui-accent-2) 10%, transparent), color-mix(in srgb, var(--ui-surface) 95%, transparent));
}

.next-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--ui-text);
}

@media (max-width: 1100px) {
  .banner,
  .workspace {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .page {
    gap: 14px;
  }

  .banner {
    padding: 16px;
  }

  .stat-grid {
    grid-template-columns: 1fr;
  }

  .chapter-grid {
    grid-template-columns: 1fr;
  }

  .lesson-item {
    grid-template-columns: 1fr;
  }
}
</style>
