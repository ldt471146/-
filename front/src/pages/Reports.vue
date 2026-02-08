<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import { ElNotification } from 'element-plus'

const overview = ref(null)
const trend = ref(null)
const loading = ref(false)
const router = useRouter()

const load = async () => {
  loading.value = true
  try {
    const [ov, tr] = await Promise.all([
      http.get('/api/reports/overview'),
      http.get('/api/reports/trend')
    ])
    overview.value = ov.data
    trend.value = tr.data
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '报告加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

onMounted(load)

const learnPoints = computed(() => {
  if (!trend.value) return ''
  const data = trend.value.learnMinutes || []
  const max = Math.max(1, ...data)
  return data.map((v, i) => `${i * 60},${80 - (v / max) * 60}`).join(' ')
})

const questionPoints = computed(() => {
  if (!trend.value) return ''
  const data = trend.value.questionTotal || []
  const max = Math.max(1, ...data)
  return data.map((v, i) => `${i * 60},${80 - (v / max) * 60}`).join(' ')
})

const weakCourses = computed(() => overview.value?.weakCourses || [])
const suggestions = computed(() => {
  if (!weakCourses.value.length) return ['继续保持当前节奏，错题率很低']
  return weakCourses.value.slice(0, 3).map((w) => `优先复盘《${w.name}》相关题目`)
})

const goPractice = (courseId) => {
  const query = {}
  if (courseId) {
    query.courseId = courseId
    query.mode = 'wrong'
  }
  router.push({ path: '/practice', query })
}
</script>

<template>
  <div class="page">
    <div class="hero">
      <div class="title display">成长报告</div>
      <div class="subtitle">可视化成长曲线与能力画像</div>
    </div>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-grid"></div>
      </template>
      <template #default>
        <div v-if="overview" class="grid">
          <div class="card">
            <div class="label">课程进度</div>
            <div class="value">
              {{ overview.finishedLessons || 0 }} / {{ overview.totalLessons || 0 }}
            </div>
            <div class="meta">已完成课时 / 总课时</div>
          </div>
          <div class="card">
            <div class="label">课程数量</div>
            <div class="value">{{ overview.myCourses || 0 }}</div>
            <div class="meta">已加入课程</div>
          </div>
          <div class="card">
            <div class="label">正确率</div>
            <div class="value">{{ overview.questionAccuracy || 0 }}%</div>
            <div class="meta">题目正确率</div>
          </div>
          <div class="card">
            <div class="label">错题数量</div>
            <div class="value">{{ overview.wrongCount || 0 }}</div>
            <div class="meta">待复盘错题</div>
          </div>
          <div class="card">
            <div class="label">学习时长</div>
            <div class="value">{{ Math.round((overview.learnSeconds || 0) / 60) }} 分钟</div>
            <div class="meta">累计学习时间</div>
          </div>
          <div class="card">
            <div class="label">错题重做</div>
            <div class="value">{{ overview.wrongRedoCount || 0 }}</div>
            <div class="meta">已改正错题</div>
          </div>
          <div class="card">
            <div class="label">收藏题目</div>
            <div class="value">{{ overview.favoriteCount || 0 }}</div>
            <div class="meta">收藏题数量</div>
          </div>
        </div>
        <div v-if="trend" class="charts">
          <div class="chart-card">
            <div class="chart-title">近7天学习时长（分钟）</div>
            <svg viewBox="0 0 360 100">
              <polyline :points="learnPoints" fill="none" stroke="url(#g1)" stroke-width="3" />
              <defs>
                <linearGradient id="g1" x1="0" y1="0" x2="1" y2="0">
                  <stop offset="0%" stop-color="var(--ui-accent)" />
                  <stop offset="100%" stop-color="var(--ui-accent-2)" />
                </linearGradient>
              </defs>
            </svg>
            <div class="chart-axis">
              <span v-for="d in trend.days" :key="d">{{ d }}</span>
            </div>
          </div>
          <div class="chart-card">
            <div class="chart-title">近7天做题数量</div>
            <svg viewBox="0 0 360 100">
              <polyline :points="questionPoints" fill="none" stroke="#ffb703" stroke-width="3" />
            </svg>
            <div class="chart-axis">
              <span v-for="d in trend.days" :key="d">{{ d }}</span>
            </div>
          </div>
        </div>
        <div v-if="overview" class="insights">
          <div class="insight-card">
            <div class="insight-title">弱项标签</div>
            <div v-if="weakCourses.length" class="tag-list">
              <div v-for="item in weakCourses" :key="item.id" class="tag">
                <span class="tag-name">{{ item.name }}</span>
                <span class="tag-value">{{ item.value }}</span>
              </div>
              <button class="link-btn" @click="goPractice()">去题库</button>
            </div>
            <div v-else class="empty">暂无明显薄弱课程</div>
          </div>
          <div class="insight-card">
            <div class="insight-title">建议学习路径</div>
            <ul class="suggest-list">
              <li v-for="(s, i) in suggestions" :key="i">
                <span>{{ s }}</span>
                <button v-if="weakCourses[i]" class="inline-btn" @click="goPractice(weakCourses[i].id)">
                  进入题库
                </button>
              </li>
            </ul>
          </div>
        </div>
        <el-empty v-else description="暂无报告数据" />
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
}

.subtitle {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 6px;
}

.grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.card {
  padding: 18px;
  border-radius: 14px;
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
}

.label {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.value {
  font-size: 24px;
  font-weight: 700;
  margin-top: 6px;
}

.meta {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 4px;
}

.skeleton-grid {
  height: 220px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
}

.charts {
  display: grid;
  gap: 16px;
  margin-top: 12px;
}

.chart-card {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 16px;
  padding: 16px;
}

.chart-title {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-bottom: 10px;
}

.chart-axis {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  font-size: 11px;
  color: var(--ui-text-muted);
  margin-top: 6px;
}

.insights {
  display: grid;
  gap: 16px;
  margin-top: 12px;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.insight-card {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 16px;
  padding: 16px;
  display: grid;
  gap: 12px;
}

.insight-title {
  font-size: 12px;
  color: var(--ui-text-muted);
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid var(--ui-border);
  background: var(--ui-surface-soft);
  font-size: 12px;
}

.tag-name {
  color: var(--ui-text);
  font-weight: 600;
}

.tag-value {
  background: linear-gradient(120deg, var(--ui-accent), var(--ui-accent-2));
  color: #051015;
  padding: 2px 6px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}

.suggest-list {
  display: grid;
  gap: 6px;
  padding-left: 16px;
  color: var(--ui-text);
  font-size: 13px;
}

.suggest-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.inline-btn,
.link-btn {
  border: 1px solid var(--ui-border);
  background: transparent;
  color: var(--ui-text);
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  cursor: pointer;
}

.link-btn {
  margin-left: 6px;
}

.empty {
  font-size: 12px;
  color: var(--ui-text-muted);
}
</style>
