<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import http from '../api/http'

const router = useRouter()
const loading = ref(false)
const overview = ref(null)
const myCourses = ref([])
const unread = ref(0)

const load = async () => {
  loading.value = true
  try {
    const [ov, coursesRes, unreadRes] = await Promise.all([
      http.get('/api/reports/overview'),
      http.get('/api/courses/my'),
      http.get('/api/notices/unread-count')
    ])
    overview.value = ov.data
    myCourses.value = (coursesRes.data || []).sort((a, b) => (b.progress || 0) - (a.progress || 0))
    unread.value = unreadRes.data || 0
  } catch (e) {
    ElNotification({
      title: '总览加载失败',
      message: e?.message || '请稍后重试',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const topCourses = computed(() => myCourses.value.slice(0, 4))
const resumeCourse = computed(() => myCourses.value.find((c) => c.lastLessonId) || myCourses.value[0] || null)
const minutes = computed(() => Math.round((overview.value?.learnSeconds || 0) / 60))
const kpis = computed(() => ([
  { title: '课程完成', value: `${overview.value?.finishedLessons || 0}/${overview.value?.totalLessons || 0}`, hint: '累计课时' },
  { title: '做题正确率', value: `${overview.value?.questionAccuracy || 0}%`, hint: '当前阶段' },
  { title: '学习时长', value: `${minutes.value}`, hint: '分钟' },
  { title: '未读消息', value: `${unread.value}`, hint: '条' }
]))

const goResume = () => {
  if (!resumeCourse.value) return
  if (resumeCourse.value.lastLessonId) {
    router.push({ path: `/courses/${resumeCourse.value.id}`, query: { lessonId: resumeCourse.value.lastLessonId } })
    return
  }
  router.push(`/courses/${resumeCourse.value.id}`)
}
</script>

<template>
  <div class="page">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton"></div>
      </template>
      <template #default>
        <div class="hero">
          <div class="hero-main">
            <div class="hero-title display">学习总览</div>
            <div class="hero-sub">保持节奏，今天优先完成一节课程 + 一组题目练习</div>
            <div class="hero-actions">
              <el-button type="primary" @click="goResume" :disabled="!resumeCourse">继续学习</el-button>
              <el-button @click="router.push('/practice')">去题库练习</el-button>
              <el-button @click="router.push('/code-practice')">去编程判题</el-button>
              <el-button @click="router.push('/exams')">在线考试</el-button>
            </div>
          </div>
          <div class="hero-side">
            <div class="ring">
              <div class="ring-val">{{ overview?.questionAccuracy || 0 }}%</div>
              <div class="ring-label">答题状态</div>
            </div>
          </div>
        </div>

        <div class="kpi-grid">
          <div class="kpi-card" v-for="item in kpis" :key="item.title">
            <div class="kpi-title">{{ item.title }}</div>
            <div class="kpi-value">{{ item.value }}</div>
            <div class="kpi-hint">{{ item.hint }}</div>
          </div>
        </div>

        <div class="grid">
          <div class="panel">
            <div class="panel-title">学习优先项</div>
            <div class="focus-list">
              <button class="focus-item" @click="goResume" :disabled="!resumeCourse">
                <div class="focus-name">继续最近课程</div>
                <div class="focus-desc">{{ resumeCourse?.title || '暂无课程' }}</div>
              </button>
              <button class="focus-item" @click="router.push('/practice')">
                <div class="focus-name">错题复盘</div>
                <div class="focus-desc">当前错题 {{ overview?.wrongCount || 0 }} 题，建议先重做</div>
              </button>
              <button class="focus-item" @click="router.push('/notices')">
                <div class="focus-name">查看消息</div>
                <div class="focus-desc">当前未读 {{ unread }} 条</div>
              </button>
            </div>
          </div>

          <div class="panel">
            <div class="panel-title">课程进度</div>
            <div v-if="topCourses.length" class="course-list">
              <div class="course-item" v-for="course in topCourses" :key="course.id">
                <div class="row">
                  <span class="c-title">{{ course.title }}</span>
                  <span class="c-val">{{ course.progress || 0 }}%</span>
                </div>
                <el-progress :percentage="course.progress || 0" :stroke-width="9" />
                <div class="c-sub">最近学习：{{ course.lastLessonTitle || '暂无记录' }}</div>
              </div>
            </div>
            <el-empty v-else description="你还没有加入课程" />
          </div>
        </div>
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
  display: grid;
  grid-template-columns: 1.4fr 280px;
  gap: 14px;
  background: linear-gradient(120deg, rgba(86, 255, 213, 0.12), rgba(0, 210, 255, 0.1) 45%, transparent);
  border: 1px solid var(--ui-border);
  border-radius: 16px;
  padding: 16px;
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--ui-text);
}

.hero-sub {
  margin-top: 6px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.hero-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-side {
  display: grid;
  place-items: center;
}

.ring {
  width: 132px;
  height: 132px;
  border-radius: 50%;
  border: 2px solid var(--ui-border);
  display: grid;
  place-items: center;
  background: radial-gradient(circle at 50% 40%, rgba(86, 255, 213, 0.18), transparent 70%);
}

.ring-val {
  font-size: 28px;
  font-weight: 800;
  color: var(--ui-accent);
}

.ring-label {
  font-size: 11px;
  color: var(--ui-text-muted);
  margin-top: -6px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.kpi-card {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 12px;
  padding: 12px;
}

.kpi-title {
  font-size: 11px;
  color: var(--ui-text-muted);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.kpi-value {
  margin-top: 6px;
  font-family: var(--font-mono);
  font-size: 24px;
  font-weight: 700;
  color: var(--ui-text);
}

.kpi-hint {
  margin-top: 2px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 14px;
}

.panel {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 14px;
  padding: 14px;
}

.panel-title {
  font-size: 12px;
  color: var(--ui-text-muted);
  letter-spacing: 0.15em;
  text-transform: uppercase;
  margin-bottom: 10px;
}

.focus-list {
  display: grid;
  gap: 10px;
}

.focus-item {
  border: 1px solid var(--ui-border-soft);
  background: var(--ui-surface-soft);
  color: var(--ui-text);
  text-align: left;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
}

.focus-item:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.focus-name {
  font-weight: 700;
}

.focus-desc {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 4px;
}

.course-list {
  display: grid;
  gap: 10px;
}

.course-item {
  border: 1px solid var(--ui-border-soft);
  border-radius: 10px;
  padding: 10px;
  background: var(--ui-surface-soft);
}

.row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.c-title {
  font-weight: 600;
  color: var(--ui-text);
}

.c-val {
  font-family: var(--font-mono);
  color: var(--ui-accent);
}

.c-sub {
  margin-top: 6px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.skeleton {
  height: 220px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
}

@media (max-width: 1000px) {
  .hero {
    grid-template-columns: 1fr;
  }
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
