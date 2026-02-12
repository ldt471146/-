<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import http from '../api/http'
import { ElNotification } from 'element-plus'

const overview = ref(null)
const trend = ref(null)
const loading = ref(false)
const router = useRouter()

const lineRef = ref(null)
const radarRef = ref(null)
const pieRef = ref(null)
let lineChart = null
let radarChart = null
let pieChart = null

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

const resizeCharts = () => {
  lineChart?.resize()
  radarChart?.resize()
  pieChart?.resize()
}

const disposeCharts = () => {
  lineChart?.dispose()
  radarChart?.dispose()
  pieChart?.dispose()
  lineChart = null
  radarChart = null
  pieChart = null
}

const renderCharts = () => {
  if (!overview.value || !trend.value) return
  disposeCharts()

  if (lineRef.value) {
    lineChart = echarts.init(lineRef.value)
    lineChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { top: 0, textStyle: { color: '#8ea0b5' } },
      grid: { left: 36, right: 16, top: 40, bottom: 24 },
      xAxis: {
        type: 'category',
        data: trend.value.days || [],
        axisLabel: { color: '#8ea0b5' }
      },
      yAxis: { type: 'value', axisLabel: { color: '#8ea0b5' } },
      series: [
        {
          name: '学习分钟',
          type: 'line',
          smooth: true,
          data: trend.value.learnMinutes || [],
          lineStyle: { width: 3, color: '#2dd4bf' },
          itemStyle: { color: '#2dd4bf' },
          areaStyle: { color: 'rgba(45,212,191,0.15)' }
        },
        {
          name: '做题数量',
          type: 'line',
          smooth: true,
          data: trend.value.questionTotal || [],
          lineStyle: { width: 3, color: '#f59e0b' },
          itemStyle: { color: '#f59e0b' },
          areaStyle: { color: 'rgba(245,158,11,0.12)' }
        }
      ]
    })
  }

  if (radarRef.value) {
    const totalLessons = overview.value.totalLessons || 0
    const finishedLessons = overview.value.finishedLessons || 0
    const progress = totalLessons > 0 ? Math.round((finishedLessons * 100) / totalLessons) : 0
    const wrongCount = overview.value.wrongCount || 0
    const wrongRedo = overview.value.wrongRedoCount || 0
    const redoRate = wrongCount > 0 ? Math.round((wrongRedo * 100) / wrongCount) : 100
    const learnMinutes = Math.round((overview.value.learnSeconds || 0) / 60)
    const activeScore = Math.min(100, (trend.value.questionTotal || []).reduce((a, b) => a + b, 0) * 4)
    const focusScore = Math.min(100, Math.round(learnMinutes / 5))

    radarChart = echarts.init(radarRef.value)
    radarChart.setOption({
      tooltip: {},
      radar: {
        indicator: [
          { name: '课程进度', max: 100 },
          { name: '正确率', max: 100 },
          { name: '错题改正', max: 100 },
          { name: '学习活跃', max: 100 },
          { name: '专注时长', max: 100 }
        ],
        splitLine: { lineStyle: { color: 'rgba(148,163,184,0.25)' } },
        axisName: { color: '#8ea0b5' }
      },
      series: [
        {
          type: 'radar',
          data: [
            {
              value: [progress, overview.value.questionAccuracy || 0, redoRate, activeScore, focusScore],
              areaStyle: { color: 'rgba(59,130,246,0.22)' },
              lineStyle: { color: '#3b82f6' },
              itemStyle: { color: '#3b82f6' }
            }
          ]
        }
      ]
    })
  }

  if (pieRef.value) {
    const pieData = weakCourses.value.length
      ? weakCourses.value.map((w) => ({ name: w.name, value: w.value || 1 }))
      : [{ name: '暂无明显薄弱项', value: 1 }]
    pieChart = echarts.init(pieRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, textStyle: { color: '#8ea0b5' } },
      series: [
        {
          type: 'pie',
          radius: ['42%', '72%'],
          avoidLabelOverlap: false,
          data: pieData
        }
      ]
    })
  }
}

const load = async () => {
  loading.value = true
  try {
    const [ov, tr] = await Promise.all([
      http.get('/api/reports/overview'),
      http.get('/api/reports/trend')
    ])
    overview.value = ov.data
    trend.value = tr.data
    await nextTick()
    renderCharts()
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

onMounted(() => {
  load()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  disposeCharts()
})
</script>

<template>
  <div class="page">
    <div class="hero">
      <div class="title display">成长报告</div>
      <div class="subtitle">学习趋势、能力雷达、薄弱项分布</div>
    </div>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-grid"></div>
      </template>
      <template #default>
        <div v-if="overview" class="grid">
          <div class="card">
            <div class="label">课程进度</div>
            <div class="value">{{ overview.finishedLessons || 0 }} / {{ overview.totalLessons || 0 }}</div>
          </div>
          <div class="card">
            <div class="label">课程数量</div>
            <div class="value">{{ overview.myCourses || 0 }}</div>
          </div>
          <div class="card">
            <div class="label">正确率</div>
            <div class="value">{{ overview.questionAccuracy || 0 }}%</div>
          </div>
          <div class="card">
            <div class="label">错题数量</div>
            <div class="value">{{ overview.wrongCount || 0 }}</div>
          </div>
          <div class="card">
            <div class="label">学习时长</div>
            <div class="value">{{ Math.round((overview.learnSeconds || 0) / 60) }} 分钟</div>
          </div>
          <div class="card">
            <div class="label">错题重做</div>
            <div class="value">{{ overview.wrongRedoCount || 0 }}</div>
          </div>
        </div>

        <div class="charts">
          <el-card class="chart-card" shadow="never">
            <template #header>每周学习趋势</template>
            <div ref="lineRef" class="chart"></div>
          </el-card>
          <el-card class="chart-card" shadow="never">
            <template #header>能力雷达图</template>
            <div ref="radarRef" class="chart"></div>
          </el-card>
          <el-card class="chart-card" shadow="never">
            <template #header>薄弱项分布</template>
            <div ref="pieRef" class="chart"></div>
          </el-card>
        </div>

        <div v-if="overview" class="insights">
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
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.title {
  font-size: 22px;
  font-weight: 700;
}

.subtitle {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 6px;
}

.grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.card {
  padding: 14px;
  border-radius: 12px;
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
}

.label {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.value {
  font-size: 22px;
  font-weight: 700;
  margin-top: 6px;
}

.charts {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
}

.chart-card {
  border: 1px solid var(--ui-border);
}

.chart {
  height: 280px;
}

.insights {
  display: grid;
  gap: 12px;
}

.insight-card {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 16px;
  padding: 16px;
}

.insight-title {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-bottom: 10px;
}

.suggest-list {
  display: grid;
  gap: 8px;
  padding-left: 16px;
}

.suggest-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.inline-btn {
  border: 1px solid var(--ui-border);
  background: transparent;
  color: var(--ui-text);
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  cursor: pointer;
}

.skeleton-grid {
  height: 220px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
}
</style>
