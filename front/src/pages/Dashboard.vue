<script setup>
const stats = [
  { title: '已学习天数', value: 12, unit: '天' },
  { title: '完成课程', value: 2, unit: '门' },
  { title: '练习题目', value: 48, unit: '题' }
]

const quickActions = [
  { title: '继续上次课程', desc: '从上次停留章节继续学习', tag: '课程' },
  { title: '开始每日练习', desc: '10分钟小挑战', tag: '练习' },
  { title: '查看成长报告', desc: '学习曲线与能力画像', tag: '成长' }
]

const courses = [
  { title: 'Python 入门', level: '初级', progress: 35 },
  { title: 'Web 前端基础', level: '初级', progress: 62 },
  { title: '算法小训练', level: '中级', progress: 18 }
]
</script>

<template>
  <div class="page">
    <div class="kpi">
      <el-card v-for="item in stats" :key="item.title" shadow="hover" class="kpi-card">
        <div class="kpi-title">{{ item.title }}</div>
        <div class="kpi-value">
          <span>{{ item.value }}</span>
          <em>{{ item.unit }}</em>
        </div>
      </el-card>
    </div>

    <div class="grid">
      <el-card shadow="hover" class="panel">
        <template #header>
          <div class="card-title">快速入口</div>
        </template>
        <div class="quick-list">
          <div v-for="item in quickActions" :key="item.title" class="quick-item">
            <div>
              <div class="quick-title">{{ item.title }}</div>
              <div class="quick-desc">{{ item.desc }}</div>
            </div>
            <el-tag type="success" effect="dark">{{ item.tag }}</el-tag>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="panel">
        <template #header>
          <div class="card-title">我的课程进度</div>
        </template>
        <div class="course-list">
          <div v-for="course in courses" :key="course.title" class="course-item">
            <div class="course-title">
              <span>{{ course.title }}</span>
              <el-tag size="small">{{ course.level }}</el-tag>
            </div>
            <el-progress :percentage="course.progress" :stroke-width="10" />
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 18px;
}

.kpi {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.kpi-card {
  position: relative;
  overflow: hidden;
}

.kpi-card::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 12px;
  background: linear-gradient(120deg, rgba(86, 255, 213, 0.08), transparent 55%);
  pointer-events: none;
}

.kpi-title {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.kpi-value {
  display: flex;
  align-items: baseline;
  gap: 6px;
  font-family: var(--font-mono);
  font-size: 24px;
  color: var(--ui-accent);
  margin-top: 8px;
}

.kpi-value em {
  font-style: normal;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.card-title {
  font-weight: 600;
}

.quick-list {
  display: grid;
  gap: 12px;
}

.quick-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--ui-surface-soft);
  border: 1px solid var(--ui-border-soft);
}

.quick-title {
  font-weight: 600;
}

.quick-desc {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.course-list {
  display: grid;
  gap: 12px;
}

.course-item {
  display: grid;
  gap: 8px;
}

.course-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
</style>
