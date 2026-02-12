<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElNotification } from 'element-plus'
import { exportTeacherStats, fetchTeacherStatsOverview } from '../api/teacher'

const loading = ref(false)
const exporting = ref('')
const overview = ref(null)

const load = async () => {
  loading.value = true
  try {
    const res = await fetchTeacherStatsOverview()
    overview.value = res.data || null
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '统计数据加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const stats = computed(() => {
  const data = overview.value || {}
  return [
    { key: 'courses', label: '课程总数', value: data.totalCourses || 0, hint: '当前教师名下课程' },
    { key: 'students', label: '学生人数', value: data.totalStudents || 0, hint: '累计参与学习学生' },
    { key: 'subs', label: '提交总数', value: data.totalSubmissions || 0, hint: '考试作答提交量' },
    { key: 'avg', label: '平均分', value: data.avgScore || 0, hint: '任务考试平均得分' }
  ]
})

const topStudents = computed(() => (overview.value?.studentRanks || []).slice(0, 10))
const courseStats = computed(() => overview.value?.courseStats || [])
const examStats = computed(() => overview.value?.examStats || [])

const doExport = async (type) => {
  if (exporting.value) return
  exporting.value = type
  try {
    const blob = await exportTeacherStats(type)
    const filename = `teacher-stats-${type}.csv`
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(url)
    ElNotification({
      title: '导出成功',
      message: `已生成 ${filename}`,
      type: 'success',
      duration: 1600
    })
  } catch (e) {
    ElNotification({
      title: '导出失败',
      message: e?.message || '请稍后重试',
      type: 'error',
      duration: 2000
    })
  } finally {
    exporting.value = ''
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">教学数据统计</div>
        <div class="subtitle">学习时长、分数表现与任务通过率一屏掌握</div>
      </div>
      <div class="hero-actions">
        <el-button :loading="exporting === 'students'" @click="doExport('students')">导出学生榜</el-button>
        <el-button :loading="exporting === 'courses'" @click="doExport('courses')">导出课程统计</el-button>
        <el-button :loading="exporting === 'tasks'" @click="doExport('tasks')">导出任务统计</el-button>
      </div>
    </div>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton"></div>
      </template>
      <template #default>
        <div class="kpi-grid">
          <div v-for="item in stats" :key="item.key" class="kpi-card">
            <div class="kpi-label">{{ item.label }}</div>
            <div class="kpi-value">{{ item.value }}</div>
            <div class="kpi-hint">{{ item.hint }}</div>
          </div>
        </div>

        <div class="grid">
          <el-card class="panel" shadow="never">
            <template #header>学生学习与成绩排行（Top 10）</template>
            <el-table :data="topStudents" size="small">
              <el-table-column label="学生" prop="username" min-width="120" />
              <el-table-column label="学习分钟" prop="learnMinutes" width="100" />
              <el-table-column label="平均分" prop="avgScore" width="90" />
              <el-table-column label="提交次数" prop="submissionCount" width="100" />
            </el-table>
          </el-card>

          <el-card class="panel" shadow="never">
            <template #header>课程维度统计</template>
            <el-table :data="courseStats" size="small">
              <el-table-column label="课程" prop="courseTitle" min-width="140" />
              <el-table-column label="学生数" prop="studentCount" width="90" />
              <el-table-column label="平均分" prop="avgScore" width="90" />
              <el-table-column label="学习分钟" prop="totalLearnMinutes" width="100" />
            </el-table>
          </el-card>
        </div>

        <el-card class="panel" shadow="never">
          <template #header>考试任务通过情况</template>
          <el-table :data="examStats" size="small">
            <el-table-column label="任务标题" prop="taskTitle" min-width="180" />
            <el-table-column label="提交数" prop="attempts" width="90" />
            <el-table-column label="通过数" prop="passCount" width="90" />
            <el-table-column label="通过率" width="100">
              <template #default="{ row }">
                <el-tag :type="(row.passRate || 0) >= 60 ? 'success' : 'warning'">
                  {{ row.passRate || 0 }}%
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
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
  gap: 14px;
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

.hero-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.kpi-card {
  border-radius: 12px;
  border: 1px solid var(--ui-border);
  background: linear-gradient(145deg, var(--ui-surface), var(--ui-surface-soft));
  padding: 12px;
}

.kpi-label {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.kpi-value {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 800;
  color: var(--ui-text);
  font-family: var(--font-mono);
}

.kpi-hint {
  margin-top: 2px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.panel {
  border: 1px solid var(--ui-border);
}

.skeleton {
  height: 180px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
}

@media (max-width: 1100px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>

