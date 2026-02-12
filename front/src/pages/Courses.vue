<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import { ElNotification } from 'element-plus'

const courses = ref([])
const myCourses = ref([])
const tab = ref('my')
const keyword = ref('')
const statusFilter = ref('all')
const sortBy = ref('smart')
const loading = ref(false)
const error = ref('')
const router = useRouter()

const formatDate = (val) => {
  if (!val) return '-'
  const d = new Date(val)
  if (Number.isNaN(d.getTime())) return '-'
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const normalizeUrl = (val) => {
  const raw = String(val || '').trim()
  if (!raw) return ''
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('//')) return `https:${raw}`
  return `https://${raw}`
}

const loadAll = async () => {
  const res = await http.get('/api/courses')
  courses.value = res.data || []
}

const loadMy = async () => {
  const res = await http.get('/api/courses/my')
  myCourses.value = res.data || []
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    await Promise.all([loadAll(), loadMy()])
  } catch (e) {
    error.value = e?.message || '课程加载失败'
    ElNotification({
      title: '加载失败',
      message: error.value,
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const goCourse = (id, lessonId) => {
  if (lessonId) {
    router.push({ path: `/courses/${id}`, query: { lessonId } })
    return
  }
  router.push(`/courses/${id}`)
}

const enroll = async (id) => {
  try {
    await http.post(`/api/courses/${id}/enroll`)
    ElNotification({
      title: '加入成功',
      message: '课程已加入我的课程',
      type: 'success',
      duration: 1600
    })
    await loadMy()
    tab.value = 'my'
  } catch (e) {
    ElNotification({
      title: '加入失败',
      message: e?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  }
}

const cancelEnroll = async (id) => {
  try {
    await http.delete(`/api/courses/${id}/enroll`)
    ElNotification({
      title: '已移除',
      message: '课程已从我的课程移除',
      type: 'success',
      duration: 1600
    })
    await loadMy()
  } catch (e) {
    ElNotification({
      title: '移除失败',
      message: e?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  }
}

const myCourseIds = computed(() => new Set(myCourses.value.map((c) => c.id)))

const safeLower = (v) => String(v || '').toLowerCase()

const listToShow = computed(() => {
  const src = tab.value === 'my' ? myCourses.value : courses.value
  let rows = [...src]
  const q = keyword.value.trim().toLowerCase()
  if (q) {
    rows = rows.filter((x) =>
      safeLower(x.title).includes(q) ||
      safeLower(x.intro).includes(q) ||
      safeLower(x.teacherName).includes(q)
    )
  }
  if (statusFilter.value === 'updating') {
    rows = rows.filter((x) => x.finishStatus !== 1)
  } else if (statusFilter.value === 'finished') {
    rows = rows.filter((x) => x.finishStatus === 1)
  } else if (statusFilter.value === 'joined' && tab.value === 'all') {
    rows = rows.filter((x) => myCourseIds.value.has(x.id))
  } else if (statusFilter.value === 'not-joined' && tab.value === 'all') {
    rows = rows.filter((x) => !myCourseIds.value.has(x.id))
  }

  if (sortBy.value === 'latest') {
    rows.sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
  } else if (sortBy.value === 'name') {
    rows.sort((a, b) => String(a.title || '').localeCompare(String(b.title || ''), 'zh-Hans-CN'))
  } else if (sortBy.value === 'progress' && tab.value === 'my') {
    rows.sort((a, b) => (b.progress || 0) - (a.progress || 0))
  } else {
    if (tab.value === 'my') {
      rows.sort((a, b) => {
        const pa = a.progress || 0
        const pb = b.progress || 0
        if (pa !== pb) return pb - pa
        return new Date(b.updatedAt || 0).getTime() - new Date(a.updatedAt || 0).getTime()
      })
    } else {
      rows.sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
    }
  }
  return rows
})

const joinedCount = computed(() => myCourses.value.length)
const allCount = computed(() => courses.value.length)
const finishedCount = computed(() => myCourses.value.filter((x) => x.finishStatus === 1).length)
const avgProgress = computed(() => {
  if (!myCourses.value.length) return 0
  const sum = myCourses.value.reduce((acc, x) => acc + (x.progress || 0), 0)
  return Math.round(sum / myCourses.value.length)
})

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="hero">
      <div class="title display">课程学习</div>
      <div class="subtitle">选择你的学习路径，从第一行代码开始</div>
      <div class="kpis">
        <div class="kpi">我的课程 {{ joinedCount }}</div>
        <div class="kpi">课程广场 {{ allCount }}</div>
        <div class="kpi">已完结 {{ finishedCount }}</div>
        <div class="kpi">平均进度 {{ avgProgress }}%</div>
      </div>
    </div>

    <el-tabs v-model="tab" class="tabs">
      <el-tab-pane label="我的课程" name="my"></el-tab-pane>
      <el-tab-pane label="课程广场" name="all"></el-tab-pane>
    </el-tabs>

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
          <el-col v-for="i in 6" :key="i" :xs="24" :sm="12" :lg="8">
            <el-card class="card">
              <div class="skeleton-card"></div>
            </el-card>
          </el-col>
        </el-row>
      </template>
      <template #default>
        <el-row v-if="listToShow.length" :gutter="16">
          <el-col
            v-for="item in listToShow"
            :key="item.id"
            :xs="24"
            :sm="12"
            :lg="8"
          >
            <el-card class="card" shadow="hover">
              <div class="cover">
                <img v-if="normalizeUrl(item.cover)" :src="normalizeUrl(item.cover)" class="cover-img" alt="cover" />
                <div class="cover-mask"></div>
                <div class="cover-title">{{ item.title }}</div>
              </div>
              <div class="meta-line">
                <span>讲师：{{ item.teacherName || '-' }}</span>
                <el-tag size="small" :type="item.finishStatus === 1 ? 'success' : 'warning'">
                  {{ item.finishStatus === 1 ? '已完结' : '更新中' }}
                </el-tag>
                <span v-if="item.createdAt">发布：{{ formatDate(item.createdAt) }}</span>
              </div>
              <div class="intro">{{ item.intro || '暂无简介' }}</div>
              <div v-if="tab === 'my'" class="progress">
                <div class="progress-meta">
                  <span>已完成 {{ item.finishedLessons || 0 }} / {{ item.totalLessons || 0 }}</span>
                  <span>{{ item.progress || 0 }}%</span>
                </div>
                <el-progress :percentage="item.progress || 0" :stroke-width="10" />
              </div>
              <div v-if="tab === 'my'" class="last-learn">
                最近学习：
                <span class="last-title">{{ item.lastLessonTitle || '暂无学习记录' }}</span>
              </div>
              <div class="card-actions">
                <el-button type="primary" class="btn" :plain="true" @click="goCourse(item.id)">
                  进入课程
                </el-button>
                <el-button
                  v-if="tab === 'my' && item.lastLessonId"
                  class="ghost-btn"
                  @click="goCourse(item.id, item.lastLessonId)"
                >
                  继续学习
                </el-button>
                <template v-if="tab === 'all'">
                  <el-button
                    class="ghost-btn"
                    :disabled="myCourseIds.has(item.id)"
                    @click="enroll(item.id)"
                  >
                    {{ myCourseIds.has(item.id) ? '已加入' : '加入课程' }}
                  </el-button>
                </template>
                <template v-else>
                  <el-button class="ghost-btn danger" @click="cancelEnroll(item.id)">
                    移除课程
                  </el-button>
                </template>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty
          v-else
          :description="tab === 'my' ? '暂无课程，请先加入课程' : (error || '暂无课程，请先创建课程数据')"
        />
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.hero {
  padding: 6px 0 8px;
}

.title {
  font-size: 22px;
  font-weight: 600;
  color: var(--ui-text);
}

.subtitle {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 6px;
}

.kpis {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.kpi {
  font-size: 12px;
  color: var(--ui-text);
  border: 1px solid var(--ui-border-soft);
  background: var(--ui-surface-soft);
  border-radius: 999px;
  padding: 5px 10px;
}

.toolbar {
  border: 1px solid var(--ui-border-soft);
}

.toolbar-grid {
  display: grid;
  grid-template-columns: 1fr 160px 160px;
  gap: 10px;
}

.tabs {
  margin-bottom: 6px;
}

.tabs :deep(.el-tabs__header) {
  margin: 0 0 8px;
  border: none;
}

.tabs :deep(.el-tabs__nav-wrap::after) {
  height: 0;
}

.tabs :deep(.el-tabs__item) {
  color: var(--ui-text-muted);
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.tabs :deep(.el-tabs__item.is-active) {
  color: var(--ui-text);
}

.tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(120deg, var(--ui-accent), var(--ui-accent-2));
}

.card {
  overflow: hidden;
}

.cover {
  position: relative;
  height: 140px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(86, 255, 213, 0.2), rgba(0, 210, 255, 0.25));
  overflow: hidden;
}

.cover-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent, rgba(7, 12, 22, 0.7));
}

.cover-title {
  position: absolute;
  left: 12px;
  bottom: 10px;
  font-weight: 600;
  color: #f7fbff;
  text-shadow: 0 6px 16px rgba(0, 0, 0, 0.6);
}

.intro {
  margin: 12px 0;
  font-size: 12px;
  color: var(--ui-text-muted);
  min-height: 38px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta-line {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.card-actions {
  display: grid;
  gap: 8px;
}

.progress {
  margin: 8px 0 10px;
  display: grid;
  gap: 6px;
}

.progress-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.last-learn {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-bottom: 6px;
}

.last-title {
  color: var(--ui-text);
  font-weight: 600;
}

.btn {
  width: 100%;
  border: none;
  color: #0b101a;
  background: linear-gradient(120deg, var(--ui-accent), var(--ui-accent-2));
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.2);
}

.btn:hover {
  filter: brightness(1.05);
}

.btn:active {
  transform: translateY(1px);
}

.ghost-btn {
  width: 100%;
  border: 1px solid var(--ui-border);
  color: var(--ui-text);
  background: transparent;
}

.ghost-btn.danger {
  border-color: rgba(255, 77, 109, 0.35);
  color: #ff8599;
}

.card :deep(.el-card__body) {
  color: var(--ui-text);
}

.skeleton-card {
  height: 200px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
}

@media (max-width: 960px) {
  .toolbar-grid {
    grid-template-columns: 1fr;
  }
}
</style>
