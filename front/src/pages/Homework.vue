<script setup>
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { ElNotification } from 'element-plus'
import { fetchMyHomework, fetchMyHomeworkDetail } from '../api/homework'

const loading = ref(false)
const list = ref([])
const filter = ref('all')
const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

const now = () => dayjs()
const fmt = (v) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '-')

const statusText = (deadline) => {
  if (!deadline) return '不限时'
  return dayjs(deadline).isBefore(now()) ? '已截止' : '进行中'
}

const statusType = (deadline) => {
  if (!deadline) return 'info'
  return dayjs(deadline).isBefore(now()) ? 'danger' : 'success'
}

const filtered = computed(() => {
  if (filter.value === 'active') return list.value.filter((x) => x.deadline && dayjs(x.deadline).isAfter(now()))
  if (filter.value === 'expired') return list.value.filter((x) => x.deadline && dayjs(x.deadline).isBefore(now()))
  if (filter.value === 'no-deadline') return list.value.filter((x) => !x.deadline)
  return list.value
})

const load = async () => {
  loading.value = true
  try {
    const res = await fetchMyHomework()
    list.value = res.data || []
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '作业列表加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const openDetail = async (row) => {
  detailOpen.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await fetchMyHomeworkDetail(row.id)
    detail.value = res.data || null
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '作业详情加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    detailLoading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">我的作业</div>
        <div class="subtitle">按课程查看教师布置的练习任务，按时完成更容易形成学习节奏</div>
      </div>
      <el-segmented
        v-model="filter"
        :options="[
          { label: '全部', value: 'all' },
          { label: '进行中', value: 'active' },
          { label: '已截止', value: 'expired' },
          { label: '不限时', value: 'no-deadline' }
        ]"
      />
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="filtered">
        <el-table-column prop="title" label="作业标题" min-width="220" />
        <el-table-column prop="courseTitle" label="课程" min-width="160" />
        <el-table-column label="题量" width="90">
          <template #default="{ row }">{{ row.questionCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="总分" width="90">
          <template #default="{ row }">{{ row.totalScore || 0 }}</template>
        </el-table-column>
        <el-table-column label="截止时间" min-width="170">
          <template #default="{ row }">{{ fmt(row.deadline) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.deadline)">{{ statusText(row.deadline) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !filtered.length" description="暂无作业" />
    </el-card>

    <el-drawer v-model="detailOpen" title="作业详情" size="720px" destroy-on-close>
      <el-skeleton :loading="detailLoading" animated>
        <template #template>
          <div class="skeleton"></div>
        </template>
        <template #default>
          <div v-if="detail" class="detail">
            <div class="meta-grid">
              <div class="meta-item">
                <div class="meta-k">课程</div>
                <div class="meta-v">{{ detail.courseTitle }}</div>
              </div>
              <div class="meta-item">
                <div class="meta-k">截止时间</div>
                <div class="meta-v">{{ fmt(detail.deadline) }}</div>
              </div>
              <div class="meta-item">
                <div class="meta-k">题量</div>
                <div class="meta-v">{{ detail.questionCount || 0 }}</div>
              </div>
              <div class="meta-item">
                <div class="meta-k">总分</div>
                <div class="meta-v">{{ detail.totalScore || 0 }}</div>
              </div>
            </div>

            <el-table :data="detail.problems || []">
              <el-table-column prop="title" label="题目" min-width="260" />
              <el-table-column prop="difficulty" label="难度" width="90" />
              <el-table-column prop="score" label="分值" width="90" />
            </el-table>
          </div>
        </template>
      </el-skeleton>
    </el-drawer>
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
  align-items: center;
  gap: 10px;
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

.detail {
  display: grid;
  gap: 12px;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.meta-item {
  border: 1px solid var(--ui-border-soft);
  border-radius: 10px;
  padding: 10px;
  background: var(--ui-surface-soft);
}

.meta-k {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.meta-v {
  margin-top: 4px;
  font-weight: 700;
}

.skeleton {
  height: 340px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
}

@media (max-width: 960px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }
  .meta-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>

