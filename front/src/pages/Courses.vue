<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import http from '../api/http'

const DAYS_30_MS = 30 * 24 * 60 * 60 * 1000

const router = useRouter()

const courses = ref([])
const myCourses = ref([])
const tab = ref('my')
const viewMode = ref('all')
const keyword = ref('')
const statusFilter = ref('all')
const sortBy = ref('smart')
const loading = ref(false)
const error = ref('')

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (part) => String(part).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const resolveAssetUrl = (value) => {
  const raw = String(value || '').trim()
  if (!raw) return ''
  if (/^data:/i.test(raw)) return raw
  if (/^https?:\/\//i.test(raw) || raw.startsWith('//') || raw.startsWith('/')) return raw
  return `/${raw.replace(/^\/+/, '')}`
}

const clipText = (value, max = 80) => {
  const raw = String(value || '').trim()
  if (!raw) return '暂无课程简介，进入课程详情后可以查看完整学习安排。'
  return raw.length > max ? `${raw.slice(0, max)}…` : raw
}

const safeLower = (value) => String(value || '').toLowerCase()
const getStatusLabel = (finishStatus) => (finishStatus === 1 ? '已完结' : '更新中')
const getStatusType = (finishStatus) => (finishStatus === 1 ? 'success' : 'warning')
const getProgressTone = (progress = 0) => {
  if (progress >= 100) return '已完成'
  if (progress >= 70) return '冲刺阶段'
  if (progress > 0) return '稳定推进'
  return '待开始'
}

const getRecentLearnText = (course) => {
  if (!course?.lastLessonTitle) return '还没有学习记录，适合现在开始。'
  if (!course?.lastLearnAt) return `上次停留在《${course.lastLessonTitle}》`
  return `${formatDate(course.lastLearnAt)} 学到《${course.lastLessonTitle}》`
}

const loadAll = async () => {
  const response = await http.get('/api/courses')
  courses.value = response.data || []
}

const loadMy = async () => {
  const response = await http.get('/api/courses/my')
  myCourses.value = response.data || []
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    await Promise.all([loadAll(), loadMy()])
  } catch (exception) {
    error.value = exception?.message || '课程加载失败'
    ElNotification({
      title: '课程加载失败',
      message: error.value,
      type: 'error',
      duration: 2200
    })
  } finally {
    loading.value = false
  }
}

const myCourseMap = computed(() => new Map(myCourses.value.map((course) => [course.id, course])))
const myCourseIds = computed(() => new Set(myCourses.value.map((course) => course.id)))

const joinedCount = computed(() => myCourses.value.length)
const allCount = computed(() => courses.value.length)
const finishedCount = computed(() => myCourses.value.filter((course) => (course.progress || 0) >= 100).length)
const readyCount = computed(() => myCourses.value.filter((course) => (course.progress || 0) <= 0 && !course.lastLessonId).length)
const activeLearningCount = computed(
  () => myCourses.value.filter((course) => ((course.progress || 0) > 0 && (course.progress || 0) < 100) || !!course.lastLessonId).length
)
const avgProgress = computed(() => {
  if (!myCourses.value.length) return 0
  const total = myCourses.value.reduce((sum, course) => sum + (course.progress || 0), 0)
  return Math.round(total / myCourses.value.length)
})
const latestCount = computed(
  () => courses.value.filter((course) => Date.now() - new Date(course.createdAt || 0).getTime() <= DAYS_30_MS).length
)

const resumeCourse = computed(() => {
  const candidates = myCourses.value.filter((course) => (course.progress || 0) < 100 || course.lastLessonId)
  if (!candidates.length) return myCourses.value[0] || null
  return [...candidates].sort((left, right) => {
    const leftResume = left.lastLessonId ? 1 : 0
    const rightResume = right.lastLessonId ? 1 : 0
    if (leftResume !== rightResume) return rightResume - leftResume
    const leftRecent = new Date(left.lastLearnAt || left.updatedAt || left.createdAt || 0).getTime()
    const rightRecent = new Date(right.lastLearnAt || right.updatedAt || right.createdAt || 0).getTime()
    if (leftRecent !== rightRecent) return rightRecent - leftRecent
    return (right.progress || 0) - (left.progress || 0)
  })[0]
})

const recommendedCourse = computed(() => {
  const candidates = courses.value.filter((course) => !myCourseIds.value.has(course.id))
  if (!candidates.length) return null
  return [...candidates].sort((left, right) => {
    const leftOpen = left.finishStatus === 1 ? 0 : 1
    const rightOpen = right.finishStatus === 1 ? 0 : 1
    if (leftOpen !== rightOpen) return rightOpen - leftOpen
    const lessonDiff = (right.totalLessons || 0) - (left.totalLessons || 0)
    if (lessonDiff !== 0) return lessonDiff
    return new Date(right.createdAt || 0).getTime() - new Date(left.createdAt || 0).getTime()
  })[0]
})

const stageCards = computed(() => {
  if (tab.value === 'my') {
    return [
      { value: 'all', label: '全部课程', desc: '查看当前已加入的所有课程', count: joinedCount.value },
      { value: 'continue', label: '继续学习', desc: '优先处理正在推进中的课程', count: activeLearningCount.value },
      { value: 'ready', label: '待开始', desc: '刚加入、还没真正开学的课程', count: readyCount.value },
      { value: 'done', label: '已完成', desc: '复盘已经学完的课程内容', count: finishedCount.value }
    ]
  }

  return [
    { value: 'all', label: '全部课程', desc: '按全局视角浏览课程广场', count: allCount.value },
    { value: 'discover', label: '值得探索', desc: '优先看你尚未加入的新课程', count: courses.value.filter((course) => !myCourseIds.value.has(course.id)).length },
    { value: 'joined', label: '已加入', desc: '快速找到你已经报名的课程', count: courses.value.filter((course) => myCourseIds.value.has(course.id)).length },
    { value: 'latest', label: '近期上新', desc: '近 30 天发布的课程', count: latestCount.value }
  ]
})

const filterByViewMode = (rows) => {
  if (tab.value === 'my') {
    if (viewMode.value === 'continue') {
      return rows.filter((course) => ((course.progress || 0) > 0 && (course.progress || 0) < 100) || !!course.lastLessonId)
    }
    if (viewMode.value === 'ready') {
      return rows.filter((course) => (course.progress || 0) <= 0 && !course.lastLessonId)
    }
    if (viewMode.value === 'done') {
      return rows.filter((course) => (course.progress || 0) >= 100)
    }
    return rows
  }

  if (viewMode.value === 'discover') {
    return rows.filter((course) => !myCourseIds.value.has(course.id))
  }
  if (viewMode.value === 'joined') {
    return rows.filter((course) => myCourseIds.value.has(course.id))
  }
  if (viewMode.value === 'latest') {
    return rows.filter((course) => Date.now() - new Date(course.createdAt || 0).getTime() <= DAYS_30_MS)
  }
  return rows
}

const listToShow = computed(() => {
  const source = tab.value === 'my' ? myCourses.value : courses.value
  let rows = filterByViewMode([...source])
  const query = keyword.value.trim().toLowerCase()

  if (query) {
    rows = rows.filter((course) =>
      safeLower(course.title).includes(query) ||
      safeLower(course.intro).includes(query) ||
      safeLower(course.teacherName).includes(query)
    )
  }

  if (statusFilter.value === 'updating') {
    rows = rows.filter((course) => course.finishStatus !== 1)
  } else if (statusFilter.value === 'finished') {
    rows = rows.filter((course) => course.finishStatus === 1)
  } else if (statusFilter.value === 'joined' && tab.value === 'all') {
    rows = rows.filter((course) => myCourseIds.value.has(course.id))
  } else if (statusFilter.value === 'not-joined' && tab.value === 'all') {
    rows = rows.filter((course) => !myCourseIds.value.has(course.id))
  }

  if (sortBy.value === 'latest' || (tab.value === 'all' && viewMode.value === 'latest' && sortBy.value === 'smart')) {
    rows.sort((left, right) => new Date(right.createdAt || 0).getTime() - new Date(left.createdAt || 0).getTime())
  } else if (sortBy.value === 'name') {
    rows.sort((left, right) => String(left.title || '').localeCompare(String(right.title || ''), 'zh-Hans-CN'))
  } else if (sortBy.value === 'progress' && tab.value === 'my') {
    rows.sort((left, right) => (right.progress || 0) - (left.progress || 0))
  } else if (tab.value === 'my') {
    rows.sort((left, right) => {
      const leftResume = left.lastLessonId ? 1 : 0
      const rightResume = right.lastLessonId ? 1 : 0
      if (leftResume !== rightResume) return rightResume - leftResume
      const progressDiff = (right.progress || 0) - (left.progress || 0)
      if (progressDiff !== 0) return progressDiff
      return new Date(right.lastLearnAt || right.updatedAt || 0).getTime() - new Date(left.lastLearnAt || left.updatedAt || 0).getTime()
    })
  } else {
    rows.sort((left, right) => {
      const joinedDiff = Number(myCourseIds.value.has(right.id)) - Number(myCourseIds.value.has(left.id))
      if (joinedDiff !== 0) return joinedDiff
      const lessonDiff = (right.totalLessons || 0) - (left.totalLessons || 0)
      if (lessonDiff !== 0) return lessonDiff
      return new Date(right.createdAt || 0).getTime() - new Date(left.createdAt || 0).getTime()
    })
  }

  return rows
})

const emptyDescription = computed(() => {
  if (tab.value === 'my' && viewMode.value === 'continue') return '你还没有正在推进的课程，可以先从待开始里挑一门开启。'
  if (tab.value === 'my' && viewMode.value === 'ready') return '当前没有待开始课程，去课程广场发现一门新课吧。'
  if (tab.value === 'my' && viewMode.value === 'done') return '还没有学完的课程，继续推进就能在这里复盘。'
  if (tab.value === 'my') return '你还没有加入课程，先去课程广场挑一门适合自己的课程吧。'
  if (tab.value === 'all' && viewMode.value === 'discover') return '当前没有更多待探索课程，先把已加入课程学起来吧。'
  if (tab.value === 'all' && viewMode.value === 'joined') return '这些课程还没有同步到广场列表，稍后刷新试试。'
  if (tab.value === 'all' && viewMode.value === 'latest') return '近 30 天暂无新上架课程，先看看已有优质课程。'
  return error.value || '暂时没有可展示的课程。'
})

const getJoinedCourse = (courseId) => myCourseMap.value.get(courseId) || null

const resetFiltersByTab = () => {
  viewMode.value = 'all'
  statusFilter.value = 'all'
  sortBy.value = 'smart'
}

const handleTabChange = () => {
  resetFiltersByTab()
}

const goCourse = (courseId, lessonId) => {
  if (lessonId) {
    router.push({ path: `/courses/${courseId}`, query: { lessonId } })
    return
  }
  router.push(`/courses/${courseId}`)
}

const enroll = async (courseId, options = {}) => {
  try {
    await http.post(`/api/courses/${courseId}/enroll`)
    if (!options.silent) {
      ElNotification({
        title: '加入成功',
        message: '课程已加入“我的课程”，现在可以开始学习。',
        type: 'success',
        duration: 1600
      })
    }
    await loadMy()
    if (options.switchToMy !== false) {
      tab.value = 'my'
      viewMode.value = 'continue'
    }
    return true
  } catch (exception) {
    ElNotification({
      title: '加入失败',
      message: exception?.message || '请稍后重试',
      type: 'error',
      duration: 2200
    })
    return false
  }
}

const cancelEnroll = async (courseId) => {
  try {
    await http.delete(`/api/courses/${courseId}/enroll`)
    ElNotification({
      title: '已移出课程',
      message: '课程已从“我的课程”中移除。',
      type: 'success',
      duration: 1600
    })
    await loadMy()
  } catch (exception) {
    ElNotification({
      title: '移出失败',
      message: exception?.message || '请稍后重试',
      type: 'error',
      duration: 2200
    })
  }
}

const goResume = () => {
  if (!resumeCourse.value) return
  goCourse(resumeCourse.value.id, resumeCourse.value.lastLessonId)
}

const goRecommended = async () => {
  if (!recommendedCourse.value) return
  const joined = await enroll(recommendedCourse.value.id, { silent: true })
  if (joined) {
    goCourse(recommendedCourse.value.id)
  }
}

watch(tab, resetFiltersByTab)

onMounted(load)
</script>

<template>
  <div class="page">
    <section class="hero">
      <div class="hero-main">
        <div class="hero-eyebrow">Learning Hub</div>
        <div class="hero-title display">课程学习中枢</div>
        <div class="hero-subtitle">
          把“继续学习、发现新课、查看进度”整理成更清晰的学习工作台，减少你在不同入口之间来回切换的时间。
        </div>
        <div class="hero-actions">
          <el-button v-if="resumeCourse" type="primary" @click="goResume">继续上次学习</el-button>
          <el-button v-if="recommendedCourse" plain @click="goRecommended">一键加入推荐课程</el-button>
        </div>
      </div>

      <div class="hero-kpis">
        <div class="kpi-card accent">
          <div class="kpi-label">我的课程</div>
          <div class="kpi-value">{{ joinedCount }}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">课程广场</div>
          <div class="kpi-value">{{ allCount }}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">正在推进</div>
          <div class="kpi-value">{{ activeLearningCount }}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">平均进度</div>
          <div class="kpi-value">{{ avgProgress }}%</div>
        </div>
      </div>
    </section>

    <section class="insight-grid">
      <el-card class="insight-card resume-card" shadow="never">
        <div class="insight-head">
          <div>
            <div class="insight-label">Continue</div>
            <div class="insight-title">{{ resumeCourse?.title || '挑一门课开始吧' }}</div>
          </div>
          <el-tag v-if="resumeCourse" :type="getStatusType(resumeCourse.finishStatus)">{{ getStatusLabel(resumeCourse.finishStatus) }}</el-tag>
        </div>
        <div class="insight-copy">
          {{ resumeCourse ? getRecentLearnText(resumeCourse) : '加入课程后，这里会自动给你推荐最适合继续的那一门。' }}
        </div>
        <div v-if="resumeCourse" class="progress-block">
          <div class="progress-meta">
            <span>已完成 {{ resumeCourse.finishedLessons || 0 }} / {{ resumeCourse.totalLessons || 0 }} 节</span>
            <span>{{ resumeCourse.progress || 0 }}%</span>
          </div>
          <el-progress :percentage="resumeCourse.progress || 0" :stroke-width="10" />
        </div>
        <div class="insight-actions">
          <el-button v-if="resumeCourse" type="primary" @click="goResume">继续学习</el-button>
          <el-button v-else plain @click="tab = 'all'">去发现课程</el-button>
        </div>
      </el-card>

      <el-card class="insight-card discover-card" shadow="never">
        <div class="insight-head">
          <div>
            <div class="insight-label">Discover</div>
            <div class="insight-title">{{ recommendedCourse?.title || '课程广场已同步' }}</div>
          </div>
          <el-tag v-if="recommendedCourse" :type="getStatusType(recommendedCourse.finishStatus)">{{ getStatusLabel(recommendedCourse.finishStatus) }}</el-tag>
        </div>
        <div class="insight-copy">
          {{ recommendedCourse ? clipText(recommendedCourse.intro, 72) : '当前没有新的推荐课程，可以先回到已加入课程继续学习。' }}
        </div>
        <div class="discover-grid">
          <div>
            <div class="discover-label">待开始</div>
            <div class="discover-value">{{ readyCount }}</div>
          </div>
          <div>
            <div class="discover-label">已完成</div>
            <div class="discover-value">{{ finishedCount }}</div>
          </div>
          <div>
            <div class="discover-label">近期上新</div>
            <div class="discover-value">{{ latestCount }}</div>
          </div>
        </div>
        <div class="insight-actions">
          <el-button v-if="recommendedCourse" plain @click="goRecommended">加入并查看</el-button>
          <el-button v-else plain @click="tab = 'my'">回到我的课程</el-button>
        </div>
      </el-card>
    </section>

    <el-tabs v-model="tab" class="tabs" @tab-change="handleTabChange">
      <el-tab-pane label="我的课程" name="my" />
      <el-tab-pane label="课程广场" name="all" />
    </el-tabs>

    <section class="view-grid">
      <button
        v-for="item in stageCards"
        :key="item.value"
        type="button"
        class="view-card"
        :class="{ active: viewMode === item.value }"
        @click="viewMode = item.value"
      >
        <div>
          <div class="view-title">{{ item.label }}</div>
          <div class="view-desc">{{ item.desc }}</div>
        </div>
        <div class="view-count">{{ item.count }}</div>
      </button>
    </section>

    <el-card class="toolbar" shadow="never">
      <div class="toolbar-grid">
        <el-input v-model="keyword" clearable placeholder="搜索课程名 / 简介 / 讲师" />
        <el-select v-model="statusFilter">
          <el-option label="全部状态" value="all" />
          <el-option label="更新中" value="updating" />
          <el-option label="已完结" value="finished" />
          <el-option v-if="tab === 'all'" label="已加入" value="joined" />
          <el-option v-if="tab === 'all'" label="未加入" value="not-joined" />
        </el-select>
        <el-select v-model="sortBy">
          <el-option label="智能排序" value="smart" />
          <el-option v-if="tab === 'my'" label="进度优先" value="progress" />
          <el-option label="最新发布" value="latest" />
          <el-option label="按名称" value="name" />
        </el-select>
      </div>
    </el-card>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <el-row :gutter="16">
          <el-col v-for="index in 6" :key="index" :xs="24" :sm="12" :xl="8">
            <el-card class="course-card skeleton-card"></el-card>
          </el-col>
        </el-row>
      </template>

      <template #default>
        <el-row v-if="listToShow.length" :gutter="16">
          <el-col v-for="course in listToShow" :key="course.id" :xs="24" :sm="12" :xl="8">
            <el-card class="course-card" shadow="hover">
              <div class="cover">
                <img v-if="resolveAssetUrl(course.cover)" :src="resolveAssetUrl(course.cover)" class="cover-image" alt="course-cover" />
                <div class="cover-overlay"></div>
                <div class="cover-title">{{ course.title }}</div>
                <div class="cover-badge">
                  <el-tag effect="dark" :type="getStatusType(course.finishStatus)">{{ getStatusLabel(course.finishStatus) }}</el-tag>
                </div>
              </div>

              <div class="card-meta">
                <span>讲师：{{ course.teacherName || '待分配' }}</span>
                <span>课时：{{ course.totalLessons || 0 }}</span>
                <span>发布：{{ formatDate(course.createdAt) }}</span>
              </div>

              <div class="intro">{{ clipText(course.intro) }}</div>

              <template v-if="tab === 'my'">
                <div class="progress-block">
                  <div class="progress-meta">
                    <span>{{ getProgressTone(course.progress || 0) }}</span>
                    <span>{{ course.progress || 0 }}%</span>
                  </div>
                  <el-progress :percentage="course.progress || 0" :stroke-width="10" />
                </div>
                <div class="learning-foot">
                  <span>{{ getRecentLearnText(course) }}</span>
                </div>
              </template>

              <template v-else-if="myCourseIds.has(course.id)">
                <div class="joined-banner">
                  已加入课程，可直接进入学习。
                </div>
              </template>

              <div class="card-actions">
                <el-button type="primary" @click="goCourse(course.id)">
                  {{ tab === 'my' ? '进入课程' : '查看课程' }}
                </el-button>

                <el-button
                  v-if="tab === 'my' && course.lastLessonId"
                  plain
                  @click="goCourse(course.id, course.lastLessonId)"
                >
                  继续学习
                </el-button>

                <el-button v-if="tab === 'my'" plain @click="cancelEnroll(course.id)">移出课程</el-button>

                <template v-if="tab === 'all'">
                  <el-button
                    v-if="myCourseIds.has(course.id)"
                    plain
                    @click="goCourse(course.id, getJoinedCourse(course.id)?.lastLessonId)"
                  >
                    去学习
                  </el-button>
                  <el-button v-else plain @click="enroll(course.id)">加入课程</el-button>
                </template>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-empty v-else class="empty-card" :description="emptyDescription" />
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 18px;
}

.hero,
.insight-card,
.toolbar,
.course-card,
.view-card,
.empty-card {
  border-radius: 24px;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.9fr);
  gap: 16px;
  padding: 20px;
  border: 1px solid var(--ui-border);
  background:
    radial-gradient(circle at top left, color-mix(in srgb, var(--ui-accent) 28%, transparent), transparent 32%),
    radial-gradient(circle at 92% 12%, color-mix(in srgb, var(--ui-accent-2) 24%, transparent), transparent 26%),
    var(--ui-content-bg);
  box-shadow: var(--ui-content-shadow);
}

.hero-main,
.hero-kpis,
.insight-grid {
  display: grid;
  gap: 12px;
}

.hero-eyebrow,
.insight-label,
.view-desc,
.kpi-label {
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--ui-text-muted);
}

.hero-title {
  font-size: clamp(40px, 5vw, 56px);
  line-height: 0.95;
  color: var(--ui-text);
}

.hero-subtitle,
.insight-copy,
.intro,
.card-meta,
.learning-foot,
.joined-banner,
.view-desc {
  color: var(--ui-text-muted);
}

.hero-actions,
.progress-meta,
.card-meta,
.card-actions,
.insight-actions,
.discover-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-kpis {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.kpi-card {
  padding: 16px;
  border-radius: 18px;
  border: 1px solid var(--ui-border-soft);
  background: color-mix(in srgb, var(--ui-surface) 92%, transparent);
}

.kpi-card.accent {
  background: linear-gradient(135deg, color-mix(in srgb, var(--ui-accent) 18%, transparent), color-mix(in srgb, var(--ui-accent-2) 14%, transparent));
}

.kpi-value,
.insight-title,
.view-count {
  font-weight: 700;
  color: var(--ui-text);
}

.kpi-value {
  font-size: 28px;
}

.insight-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.insight-card {
  border: 1px solid var(--ui-border);
  background: color-mix(in srgb, var(--ui-surface) 92%, transparent);
}

:deep(.insight-card .el-card__body),
:deep(.course-card .el-card__body) {
  display: grid;
  gap: 14px;
}

.resume-card {
  background:
    radial-gradient(circle at 12% 10%, color-mix(in srgb, var(--ui-accent) 24%, transparent), transparent 28%),
    color-mix(in srgb, var(--ui-surface) 94%, transparent);
}

.discover-card {
  background:
    radial-gradient(circle at 90% 14%, color-mix(in srgb, var(--ui-accent-2) 24%, transparent), transparent 28%),
    color-mix(in srgb, var(--ui-surface) 94%, transparent);
}

.insight-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.insight-title {
  font-size: 24px;
}

.discover-grid {
  justify-content: space-between;
}

.discover-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--ui-text);
}

.tabs,
.toolbar,
.course-card {
  border-radius: 22px;
}

.view-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.view-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px 18px;
  border: 1px solid var(--ui-border);
  background: color-mix(in srgb, var(--ui-surface) 94%, transparent);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.view-card:hover {
  transform: translateY(-1px);
}

.view-card.active {
  border-color: color-mix(in srgb, var(--ui-accent) 60%, transparent);
  background: linear-gradient(135deg, color-mix(in srgb, var(--ui-accent) 16%, transparent), color-mix(in srgb, var(--ui-accent-2) 12%, transparent));
  box-shadow: 0 12px 30px color-mix(in srgb, var(--ui-accent) 12%, transparent);
}

.view-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--ui-text);
}

.view-count {
  font-size: 30px;
}

.toolbar {
  border: 1px solid var(--ui-border);
  background: color-mix(in srgb, var(--ui-surface) 94%, transparent);
}

:deep(.toolbar .el-card__body) {
  padding: 16px 18px;
}

.toolbar-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) 180px 180px;
  gap: 12px;
}

.course-card {
  border: 1px solid var(--ui-border);
  overflow: hidden;
  background: color-mix(in srgb, var(--ui-surface) 96%, transparent);
}

.skeleton-card {
  min-height: 320px;
}

.cover {
  position: relative;
  min-height: 168px;
  border-radius: 18px;
  overflow: hidden;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--ui-accent) 26%, transparent), color-mix(in srgb, var(--ui-accent-2) 26%, transparent)),
    color-mix(in srgb, var(--ui-surface-soft) 88%, transparent);
}

.cover-image,
.cover-overlay {
  position: absolute;
  inset: 0;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-overlay {
  background: linear-gradient(180deg, transparent, rgba(5, 10, 18, 0.72));
}

.cover-title,
.cover-badge {
  position: absolute;
  z-index: 1;
}

.cover-title {
  left: 14px;
  right: 14px;
  bottom: 14px;
  font-size: 24px;
  line-height: 1;
  color: #fff;
  text-shadow: 0 6px 18px rgba(0, 0, 0, 0.35);
}

.cover-badge {
  top: 14px;
  right: 14px;
}

.progress-block,
.learning-foot,
.joined-banner {
  padding: 12px 14px;
  border-radius: 16px;
  border: 1px solid var(--ui-border-soft);
  background: color-mix(in srgb, var(--ui-surface-soft) 92%, transparent);
}

.progress-block {
  display: grid;
  gap: 10px;
}

.card-actions {
  justify-content: flex-start;
}

@media (max-width: 1200px) {
  .hero,
  .insight-grid,
  .view-grid,
  .toolbar-grid {
    grid-template-columns: 1fr;
  }
}
</style>
