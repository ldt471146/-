<script setup>
import { computed, onMounted, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'
import { ElNotification } from 'element-plus'
import { getToken } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const loading = ref(false)
const progressMap = ref({})
const resumeLessonId = ref(null)
const activeChapter = ref([])
const selectedLessonId = ref(null)

const load = async () => {
  loading.value = true
  try {
    const [courseRes, recordRes] = await Promise.all([
      http.get(`/api/courses/${route.params.id}`),
      http.get('/api/learn/records')
    ])
    detail.value = courseRes.data
    progressMap.value = (recordRes.data || []).reduce((acc, r) => {
      acc[r.lessonId] = r
      return acc
    }, {})
    resumeLessonId.value = findResumeLessonId(detail.value, progressMap.value)
    const queryLessonId = Number(route.query.lessonId || 0)
    if (queryLessonId) {
      activeChapter.value = findChapterIdsByLesson(detail.value, queryLessonId)
      selectedLessonId.value = queryLessonId
    }
  } finally {
    loading.value = false
  }
}

const findResumeLessonId = (course, map) => {
  if (!course?.chapters) return null
  for (const ch of course.chapters) {
    for (const lesson of ch.lessons || []) {
      const record = map[lesson.id]
      if (!record || record.isFinished !== 1) {
        return lesson.id
      }
    }
  }
  return null
}

const activeLessonId = ref(null)
const activeStartTime = ref(0)

const startLesson = async (lessonId) => {
  activeLessonId.value = lessonId
  activeStartTime.value = Date.now()
  const current = progressMap.value[lessonId]?.progress || 0
  await markProgress(lessonId, Math.max(current, 1), false, true)
}

const markProgress = async (lessonId, progress, withDuration = false, silent = false) => {
  try {
    const payload = { lessonId, progress }
    if (withDuration && activeLessonId.value === lessonId && activeStartTime.value) {
      const seconds = Math.max(1, Math.round((Date.now() - activeStartTime.value) / 1000))
      payload.durationSeconds = seconds
      activeLessonId.value = null
      activeStartTime.value = 0
    }
    await http.post('/api/learn/progress', payload)
    const current = progressMap.value[lessonId]?.progress || 0
    const finalProgress = Math.max(current, progress)
    progressMap.value[lessonId] = { lessonId, progress: finalProgress, isFinished: finalProgress >= 100 ? 1 : 0 }
    resumeLessonId.value = findResumeLessonId(detail.value, progressMap.value)
    if (!silent) {
      ElNotification({
        title: '已更新进度',
        message: progress >= 100 ? '恭喜完成本课时' : `进度已更新为 ${progress}%`,
        type: 'success',
        duration: 1600
      })
    }
  } catch (e) {
    ElNotification({
      title: '更新失败',
      message: e?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  }
}

const findChapterIdsByLesson = (course, lessonId) => {
  if (!course?.chapters) return []
  for (const ch of course.chapters) {
    if ((ch.lessons || []).some((l) => l.id === lessonId)) {
      return [ch.id]
    }
  }
  return []
}

const lessonsFlat = computed(() => {
  const list = []
  detail.value?.chapters?.forEach((ch) => {
    (ch.lessons || []).forEach((l) => {
      list.push({ ...l, chapterId: ch.id, chapterTitle: ch.title })
    })
  })
  return list
})

const totalLessons = computed(() => lessonsFlat.value.length)
const finishedLessons = computed(
  () => lessonsFlat.value.filter((l) => progressMap.value[l.id]?.isFinished === 1).length
)
const courseProgress = computed(() => {
  if (totalLessons.value === 0) return 0
  return Math.round((finishedLessons.value / totalLessons.value) * 100)
})

const resumeLesson = computed(() =>
  lessonsFlat.value.find((l) => l.id === resumeLessonId.value)
)

const unlockedSet = computed(() => {
  const set = new Set()
  const list = lessonsFlat.value
  for (let i = 0; i < list.length; i++) {
    const prev = list[i - 1]
    if (i === 0) {
      set.add(list[i].id)
    } else if (progressMap.value[prev.id]?.isFinished === 1) {
      set.add(list[i].id)
    }
  }
  return set
})

const selectedLesson = computed(() =>
  lessonsFlat.value.find((l) => l.id === selectedLessonId.value)
)

const selectLesson = (lessonId) => {
  selectedLessonId.value = lessonId
}

const goPractice = () => {
  router.push('/practice')
}

const goLearningPath = () => {
  router.push({ path: '/learning-path', query: { courseId: route.params.id } })
}

const formatDate = (val) => {
  if (!val) return '-'
  const d = new Date(val)
  if (Number.isNaN(d.getTime())) return '-'
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const resumeLearning = async () => {
  if (!resumeLessonId.value) return
  selectedLessonId.value = resumeLessonId.value
  activeChapter.value = findChapterIdsByLesson(detail.value, resumeLessonId.value)
  await startLesson(resumeLessonId.value)
}

const finalizeLesson = async (useKeepalive = false) => {
  if (!activeLessonId.value || !activeStartTime.value) return
  const lessonId = activeLessonId.value
  const seconds = Math.max(1, Math.round((Date.now() - activeStartTime.value) / 1000))
  const current = progressMap.value[lessonId]?.progress || 1
  const payload = {
    lessonId,
    progress: current,
    durationSeconds: seconds
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
  } catch (e) {
    // ignore
  }
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
        <div class="skeleton-banner"></div>
      </template>
      <template #default>
        <div class="banner">
          <div class="banner-glow"></div>
          <div class="banner-body">
            <div class="banner-left">
              <div class="banner-title">{{ detail?.title }}</div>
              <div class="banner-intro">{{ detail?.intro || '暂无简介' }}</div>
              <div class="banner-meta">
                <span>讲师：{{ detail?.teacherName || '-' }}</span>
                <span>状态：{{ detail?.finishStatus === 1 ? '已完结' : '更新中' }}</span>
                <span>发布时间：{{ detail?.createdAt ? formatDate(detail.createdAt) : '-' }}</span>
              </div>
              <div class="banner-actions">
            <el-button
              type="primary"
              :disabled="!resumeLessonId"
              @click="resumeLearning"
            >
              继续学习
            </el-button>
                <el-button plain @click="goPractice">去题库练习</el-button>
                <el-button plain @click="goLearningPath">学习路径</el-button>
                <span v-if="resumeLessonId" class="resume-text">已定位到最近未完成课时</span>
              </div>
            </div>

            <div class="banner-right">
              <div class="stat-stack">
                <div class="stat-card">
                  <div class="stat-label">课程进度</div>
                  <div class="stat-value">{{ courseProgress }}%</div>
                  <el-progress :percentage="courseProgress" :stroke-width="8" />
                </div>
                <div class="stat-card">
                  <div class="stat-label">已完成课时</div>
                  <div class="stat-value">{{ finishedLessons }}/{{ totalLessons }}</div>
                  <div class="stat-sub">稳步推进中</div>
                </div>
              </div>

              <div class="resume-card" v-if="resumeLesson">
                <div class="resume-head">最近学习课时</div>
                <div class="resume-title">{{ resumeLesson.title }}</div>
                <div class="resume-meta">
                  {{ resumeLesson.chapterTitle }} · {{ resumeLesson.contentType }}
                </div>
                <div class="resume-actions">
                  <el-button size="small" @click="selectLesson(resumeLesson.id)">查看详情</el-button>
                  <el-button size="small" type="success" @click="markProgress(resumeLesson.id, 100, true)">
                    标记完成
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="chapters">
          <div class="lesson-list">
            <el-collapse v-model="activeChapter" accordion>
              <el-collapse-item v-for="ch in detail?.chapters || []" :key="ch.id" :name="ch.id">
                <template #title>
                  <div class="chapter-title">{{ ch.title }}</div>
                </template>
                <div class="lessons">
                  <div
                    v-for="lesson in ch.lessons"
                    :key="lesson.id"
                    class="lesson"
                    :class="{
                      active: selectedLessonId === lesson.id,
                      locked: !unlockedSet.has(lesson.id)
                    }"
                    @click="unlockedSet.has(lesson.id) && selectLesson(lesson.id)"
                  >
                    <div>
                      <div class="lesson-title">{{ lesson.title }}</div>
                      <div class="lesson-type">{{ lesson.contentType }}</div>
                      <div v-if="activeLessonId === lesson.id" class="learning-tip">正在学习中...</div>
                    </div>
                    <div class="lesson-actions">
                      <el-tag
                        v-if="progressMap[lesson.id]?.isFinished === 1"
                        type="success"
                        effect="dark"
                      >
                        已完成
                      </el-tag>
                      <el-tag v-else-if="!unlockedSet.has(lesson.id)" type="warning" effect="dark">
                        未解锁
                      </el-tag>
                      <el-progress
                        v-else
                        :percentage="progressMap[lesson.id]?.progress || 0"
                        :stroke-width="8"
                        class="lesson-progress"
                      />
                    </div>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>

          <div class="lesson-detail" v-if="selectedLesson">
            <div class="detail-title">{{ selectedLesson.title }}</div>
            <div class="detail-meta">
              {{ selectedLesson.contentType }} · {{ selectedLesson.chapterTitle }}
            </div>
            <div class="detail-body">
              <div v-if="selectedLesson.contentType === 'text'" class="detail-text">
                {{ selectedLesson.contentText || '暂无内容' }}
              </div>
              <div v-else-if="selectedLesson.contentType === 'video'" class="detail-link">
                视频地址：
                <a :href="selectedLesson.contentUrl" target="_blank">{{ selectedLesson.contentUrl }}</a>
              </div>
              <div v-else class="detail-link">
                资源地址：
                <a :href="selectedLesson.contentUrl" target="_blank">{{ selectedLesson.contentUrl }}</a>
              </div>
            </div>
            <div class="detail-actions">
              <el-button size="small" @click="startLesson(selectedLesson.id)">开始学习</el-button>
              <el-button size="small" @click="markProgress(selectedLesson.id, 60)">学到 60%</el-button>
              <el-button size="small" type="success" @click="markProgress(selectedLesson.id, 100, true)">
                完成
              </el-button>
              <el-button size="small" plain @click="goPractice">去题库练习</el-button>
              <el-button size="small" plain @click="goLearningPath">学习路径</el-button>
            </div>
          </div>
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 18px;
}

.banner {
  padding: 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgb(229 235 248 / 86%), rgba(12, 18, 30, 0.86));
  border: 1px solid var(--ui-border);
  color: var(--ui-text);
  position: relative;
  overflow: hidden;
}

.banner-glow {
  position: absolute;
  inset: -40% 40% auto -20%;
  height: 260px;
  background: radial-gradient(circle at top, rgba(86, 255, 213, 0.35), transparent 70%);
  filter: blur(10px);
  opacity: 0.8;
  pointer-events: none;
}

.banner-body {
  position: relative;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 16px;
  z-index: 1;
}

.banner-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--ui-text);
}

.banner-intro {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 6px;
  max-width: 420px;
}

.banner-meta {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.banner-actions {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.resume-text {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.banner-right {
  display: grid;
  gap: 12px;
}

.stat-stack {
  display: grid;
  gap: 10px;
}

.stat-card {
  padding: 12px;
  border-radius: 12px;
  background: var(--ui-surface);
  border: 1px solid var(--ui-border-soft);
  display: grid;
  gap: 6px;
}

.stat-label {
  font-size: 11px;
  color: var(--ui-text-muted);
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
}

.stat-sub {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.resume-card {
  padding: 12px;
  border-radius: 12px;
  background: rgba(86, 255, 213, 0.08);
  border: 1px solid rgba(86, 255, 213, 0.25);
  display: grid;
  gap: 6px;
}

.resume-head {
  font-size: 11px;
  letter-spacing: 0.2em;
  color: var(--ui-text-muted);
  text-transform: uppercase;
}

.resume-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--ui-text);
}

.resume-meta {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.resume-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

:global(:root[data-theme='aurora']) .page .banner-intro,
:global(:root[data-theme='aurora']) .page .resume-text,
:global(:root[data-theme='aurora']) .page .stat-label,
:global(:root[data-theme='aurora']) .page .stat-sub,
:global(:root[data-theme='aurora']) .page .resume-head,
:global(:root[data-theme='aurora']) .page .resume-meta,
:global(:root[data-theme='aurora']) .page .lesson-type,
:global(:root[data-theme='aurora']) .page .detail-meta {
  color: rgba(15, 23, 42, 0.78);
}




.chapters {
  display: grid;
  gap: 12px;
  grid-template-columns: 1.1fr 1fr;
}

.chapter-title {
  font-weight: 600;
  color: var(--ui-text);
}

.lessons {
  display: grid;
  gap: 10px;
  padding: 6px 0;
}

.lesson {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--ui-surface-soft);
  border: 1px solid var(--ui-border-soft);
  color: var(--ui-text);
}

.lesson.locked {
  opacity: 0.6;
  cursor: not-allowed;
}

.lesson-title {
  font-size: 13px;
  color: var(--ui-text);
}

.lesson-type {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.learning-tip {
  margin-top: 4px;
  font-size: 12px;
  color: var(--ui-accent);
}

.lesson-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.lesson-progress {
  width: 120px;
}

.lesson.active {
  border-color: var(--ui-accent);
  box-shadow: 0 0 12px rgba(86, 255, 213, 0.25);
}

.lesson-detail {
  padding: 16px;
  border-radius: 14px;
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  display: grid;
  gap: 10px;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
}

.detail-meta {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.detail-body {
  font-size: 13px;
  color: var(--ui-text);
  line-height: 1.6;
  min-height: 120px;
  white-space: pre-wrap;
}

.detail-link a {
  color: var(--ui-accent);
}

.detail-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 960px) {
  .chapters {
    grid-template-columns: 1fr;
  }
  .banner-body {
    grid-template-columns: 1fr;
  }
}

.chapters :deep(.el-collapse-item__header) {
  background: var(--ui-surface);
  color: var(--ui-text);
  border-radius: 10px;
  border: 1px solid var(--ui-border-soft);
  padding: 0 12px;
}

.chapters :deep(.el-collapse-item__header.is-active) {
  box-shadow: 0 0 0 1px var(--ui-border);
}

.chapters :deep(.el-collapse-item__wrap) {
  background: transparent;
  border: none;
}

.chapters :deep(.el-collapse-item__content) {
  color: var(--ui-text);
}

.skeleton-banner {
  height: 120px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
}

</style>
