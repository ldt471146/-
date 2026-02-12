<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElNotification } from 'element-plus'
import http from '../api/http'
import { fetchLearningPath, markLearningPathProgress } from '../api/learningPath'
import { useRoute } from 'vue-router'

const route = useRoute()

const loading = ref(false)
const saving = ref(false)
const courses = ref([])
const courseId = ref('')
const overview = ref(null)

const loadCourses = async () => {
  const res = await http.get('/api/courses/my')
  courses.value = res.data || []
  if (!courses.value.length) {
    const all = await http.get('/api/courses')
    courses.value = all.data || []
  }
  if (!courseId.value && courses.value.length) {
    courseId.value = courses.value[0].id
  }
}

const loadPath = async () => {
  if (!courseId.value) {
    overview.value = null
    return
  }
  loading.value = true
  try {
    const res = await fetchLearningPath(Number(courseId.value))
    overview.value = res.data || null
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '学习路径加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const markLearned = async (point) => {
  if (!point || point.status !== 'UNLOCKED' || saving.value) return
  saving.value = true
  try {
    await markLearningPathProgress({
      courseId: Number(courseId.value),
      pointId: point.pointId,
      status: 1
    })
    await loadPath()
    ElNotification({
      title: '已更新',
      message: `知识点「${point.title}」已标记掌握`,
      type: 'success',
      duration: 1500
    })
  } catch (e) {
    ElNotification({
      title: '更新失败',
      message: e?.message || '请稍后重试',
      type: 'error',
      duration: 2000
    })
  } finally {
    saving.value = false
  }
}

const progressPercent = computed(() => {
  const total = overview.value?.totalPoints || 0
  const learned = overview.value?.learnedPoints || 0
  if (!total) return 0
  return Math.round((learned * 100) / total)
})

const nextPoint = computed(() =>
  (overview.value?.points || []).find((p) => p.pointId === overview.value?.nextPointId) || null
)

watch(courseId, loadPath)

onMounted(async () => {
  const q = Number(route.query.courseId || 0)
  if (q) {
    courseId.value = q
  }
  await loadCourses()
  await loadPath()
})
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">学习路径</div>
        <div class="subtitle">按知识点依赖逐步解锁，避免跳步学习</div>
      </div>
      <div class="hero-tools">
        <el-select v-model="courseId" class="course-select" placeholder="选择课程">
          <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
        </el-select>
      </div>
    </div>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton"></div>
      </template>
      <template #default>
        <el-card class="summary" shadow="never">
          <div class="summary-grid">
            <div class="summary-kpi">
              <div class="kpi-label">总知识点</div>
              <div class="kpi-value">{{ overview?.totalPoints || 0 }}</div>
            </div>
            <div class="summary-kpi">
              <div class="kpi-label">已掌握</div>
              <div class="kpi-value">{{ overview?.learnedPoints || 0 }}</div>
            </div>
            <div class="summary-kpi">
              <div class="kpi-label">完成度</div>
              <div class="kpi-value">{{ progressPercent }}%</div>
            </div>
            <div class="summary-next">
              <div class="kpi-label">推荐下一步</div>
              <div class="next-title">{{ nextPoint?.title || overview?.nextPointTitle || '当前无可学习节点' }}</div>
            </div>
          </div>
          <el-progress :percentage="progressPercent" :stroke-width="10" />
        </el-card>

        <div class="path-grid">
          <div v-for="point in overview?.points || []" :key="point.pointId" class="point-card" :class="point.status.toLowerCase()">
            <div class="point-head">
              <div class="point-status">{{ point.status }}</div>
              <el-button
                v-if="point.status === 'UNLOCKED'"
                size="small"
                type="success"
                :loading="saving"
                @click="markLearned(point)"
              >
                标记掌握
              </el-button>
            </div>
            <div class="point-title">{{ point.title }}</div>
            <div class="point-meta">章节：{{ point.chapterTitle || '-' }}</div>
            <div class="point-desc">{{ point.description || '暂无描述' }}</div>
            <div class="point-pre">
              前置数量：{{ (point.prerequisitePointIds || []).length }}
            </div>
          </div>
          <el-empty v-if="!(overview?.points || []).length" description="当前课程还未配置知识点路径" />
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 14px;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
}

.title {
  font-size: 22px;
  font-weight: 700;
}

.subtitle {
  margin-top: 6px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.course-select {
  width: 260px;
}

.summary {
  border: 1px solid var(--ui-border);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.summary-kpi,
.summary-next {
  border: 1px solid var(--ui-border-soft);
  border-radius: 10px;
  background: var(--ui-surface-soft);
  padding: 10px;
}

.kpi-label {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.kpi-value {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 800;
  font-family: var(--font-mono);
}

.next-title {
  margin-top: 4px;
  font-size: 14px;
  font-weight: 700;
}

.path-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
}

.point-card {
  border: 1px solid var(--ui-border);
  border-radius: 12px;
  background: var(--ui-surface);
  padding: 10px;
  display: grid;
  gap: 6px;
}

.point-card.learned {
  border-color: rgba(34, 197, 94, 0.45);
  background: rgba(34, 197, 94, 0.1);
}

.point-card.unlocked {
  border-color: rgba(59, 130, 246, 0.45);
  background: rgba(59, 130, 246, 0.1);
}

.point-card.locked {
  border-color: rgba(148, 163, 184, 0.4);
  background: rgba(148, 163, 184, 0.1);
}

.point-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.point-status {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: var(--ui-text-muted);
}

.point-title {
  font-size: 15px;
  font-weight: 700;
}

.point-meta,
.point-pre {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.point-desc {
  font-size: 13px;
  line-height: 1.45;
  min-height: 36px;
}

.skeleton {
  height: 150px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
}

@media (max-width: 980px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .course-select {
    width: 100%;
  }
}
</style>
