<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessageBox, ElNotification } from 'element-plus'
import {
  fetchAdminCommunityOverview,
  fetchAdminCommunityPosts,
  fetchAdminCommunityReplies,
  reviewAdminCommunityPost,
  reviewAdminCommunityReply
} from '../api/admin'

const loading = ref(false)
const overview = ref({ totalPosts: 0, normalPosts: 0, hiddenPosts: 0, totalReplies: 0, normalReplies: 0, hiddenReplies: 0, bestReplyCount: 0, mutedUsers: 0 })
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const tab = ref('posts')
const keyword = ref('')
const status = ref('all')

const summaryCards = computed(() => ([
  { label: '帖子总量', value: overview.value.totalPosts, hint: `隐藏帖子 ${overview.value.hiddenPosts}` },
  { label: '回复总量', value: overview.value.totalReplies, hint: `隐藏回复 ${overview.value.hiddenReplies}` },
  { label: '最佳回答', value: overview.value.bestReplyCount, hint: '反映社区解答质量' },
  { label: '禁言用户', value: overview.value.mutedUsers, hint: '需要持续观察恢复策略' }
]))

const laneCards = computed(() => ([
  { value: 'posts', label: '帖子巡检', count: overview.value.totalPosts, desc: '处理主题帖内容与曝光风险' },
  { value: 'replies', label: '回复巡检', count: overview.value.totalReplies, desc: '处理回复质量与违规交流' }
]))

const statusText = (value) => (value === 1 ? '正常' : '隐藏')
const statusTone = (value) => (value === 1 ? 'success' : 'warning')

const queryParams = () => {
  const params = { page: page.value, size: size.value }
  if (keyword.value.trim()) params.keyword = keyword.value.trim()
  if (status.value !== 'all') params.status = Number(status.value)
  return params
}

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (item) => String(item).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const load = async () => {
  loading.value = true
  try {
    const [listRes, overviewRes] = await Promise.all([
      tab.value === 'posts' ? fetchAdminCommunityPosts(queryParams()) : fetchAdminCommunityReplies(queryParams()),
      fetchAdminCommunityOverview()
    ])
    const pageData = listRes.data || {}
    list.value = pageData.records || []
    total.value = pageData.total || 0
    overview.value = overviewRes.data || overview.value
  } catch (e) {
    ElNotification({ title: '加载失败', message: e?.message || '社区治理数据加载失败', type: 'error', duration: 2000 })
  } finally {
    loading.value = false
  }
}

const search = async () => {
  page.value = 1
  await load()
}

const resetFilters = async () => {
  keyword.value = ''
  status.value = 'all'
  await search()
}

const switchLane = async (value) => {
  tab.value = value
  await search()
}

const doReview = async (id, action, reason = '') => {
  try {
    if (tab.value === 'posts') {
      await reviewAdminCommunityPost(id, { action, reason })
    } else {
      await reviewAdminCommunityReply(id, { action, reason })
    }
    ElNotification({
      title: '操作成功',
      message: action === 'APPROVE' ? '内容已恢复正常' : action === 'DELETE' ? '内容已隐藏' : '用户已禁言',
      type: 'success',
      duration: 1600
    })
    await load()
  } catch (e) {
    ElNotification({ title: '操作失败', message: e?.message || '请稍后重试', type: 'error', duration: 2000 })
  }
}

const hideContent = async (id) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入隐藏原因，便于后续审计与复盘', '隐藏内容', {
      confirmButtonText: '确认隐藏',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：广告、辱骂、泄题、灌水'
    })
    await doReview(id, 'DELETE', value || '管理员隐藏内容')
  } catch (e) {
    if (e !== 'cancel') {
      ElNotification({ title: '操作失败', message: e?.message || '请稍后重试', type: 'error', duration: 2000 })
    }
  }
}

const muteUser = async (id) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入禁言原因，系统会同时记录审计日志', '禁言用户', {
      confirmButtonText: '确认禁言',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '例如：多次发布违规内容'
    })
    await doReview(id, 'MUTE_USER', value || '社区违规')
  } catch (e) {
    if (e !== 'cancel') {
      ElNotification({ title: '操作失败', message: e?.message || '请稍后重试', type: 'error', duration: 2000 })
    }
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <section class="hero">
      <div class="hero-copy">
        <div class="eyebrow">管理员 · 社区治理</div>
        <div class="title display">社区治理工作台</div>
        <div class="subtitle">聚合帖子、回复、最佳回答和禁言风险，先识别内容态势，再决定恢复、隐藏或禁言。</div>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="search">刷新工作台</el-button>
        <el-button @click="resetFilters">重置筛选</el-button>
      </div>
    </section>

    <section class="summary-grid">
      <article v-for="item in summaryCards" :key="item.label" class="summary-card">
        <div class="summary-label">{{ item.label }}</div>
        <div class="summary-value">{{ item.value }}</div>
        <div class="summary-hint">{{ item.hint }}</div>
      </article>
    </section>

    <section class="lane-grid">
      <button v-for="item in laneCards" :key="item.value" type="button" class="lane-card" :class="{ active: tab === item.value }" @click="switchLane(item.value)">
        <div>
          <div class="lane-title">{{ item.label }}</div>
          <div class="lane-desc">{{ item.desc }}</div>
        </div>
        <div class="lane-count">{{ item.count }}</div>
      </button>
    </section>

    <el-card class="toolbar" shadow="never">
      <div class="toolbar-grid">
        <el-segmented v-model="tab" :options="[{ label: '帖子审核', value: 'posts' }, { label: '回复审核', value: 'replies' }]" @change="search" />
        <el-input v-model="keyword" placeholder="关键词搜索" clearable @keyup.enter="search" />
        <el-select v-model="status" @change="search">
          <el-option label="全部状态" value="all" />
          <el-option label="正常" value="1" />
          <el-option label="隐藏" value="2" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>
    </el-card>

    <el-table :data="list" v-loading="loading" class="table">
      <el-table-column label="内容信息" min-width="320">
        <template #default="{ row }">
          <div class="content-cell" v-if="tab === 'posts'">
            <div class="content-title">{{ row.title || '未命名帖子' }}</div>
            <div class="content-meta">作者：{{ row.authorName || '-' }} · 回复 {{ row.replyCount || 0 }} · 浏览 {{ row.viewCount || 0 }}</div>
            <div class="content-preview">{{ row.contentPreview || '暂无摘要' }}</div>
          </div>
          <div class="content-cell" v-else>
            <div class="content-title">帖子 #{{ row.postId || '-' }} · {{ row.authorName || '匿名用户' }}</div>
            <div class="content-meta">{{ row.isBest === 1 ? '最佳回答' : '普通回复' }}</div>
            <div class="content-preview">{{ row.content || '暂无回复内容' }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTone(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="120">
        <template #default="{ row }">{{ formatDate(tab === 'posts' ? row.lastReplyAt || row.createdAt : row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" plain @click="doReview(row.id, 'APPROVE')">恢复正常</el-button>
          <el-button size="small" type="warning" plain @click="hideContent(row.id)">隐藏内容</el-button>
          <el-button size="small" type="danger" plain @click="muteUser(row.id)">禁言用户</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, prev, pager, next, sizes" :current-page="page" :page-size="size" :page-sizes="[10, 20, 50]" :total="total" @current-change="(v) => { page = v; load() }" @size-change="(v) => { size = v; page = 1; load() }" />
    </div>
  </div>
</template>

<style scoped>
.page,.hero,.hero-copy,.summary-grid,.lane-grid,.content-cell{display:grid;gap:14px}
.page{gap:16px}.hero{grid-template-columns:minmax(0,1fr) auto;padding:22px;border-radius:24px;border:1px solid var(--ui-border);background:radial-gradient(circle at 12% 16%,color-mix(in srgb,var(--ui-accent) 14%,transparent),transparent 24%),radial-gradient(circle at 88% 16%,color-mix(in srgb,var(--ui-accent-2) 18%,transparent),transparent 22%),var(--ui-content-bg);box-shadow:var(--ui-content-shadow)}
.eyebrow,.subtitle,.summary-label,.summary-hint,.lane-desc,.content-meta,.content-preview{color:var(--ui-text-muted)}.eyebrow{font-size:12px;letter-spacing:.16em;text-transform:uppercase}.title{font-size:clamp(30px,4vw,42px);line-height:.98;color:var(--ui-text)}.hero-actions{display:flex;gap:10px;align-items:flex-start;flex-wrap:wrap}
.summary-grid{grid-template-columns:repeat(4,minmax(0,1fr))}.lane-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.summary-card,.lane-card{padding:16px;border-radius:20px;border:1px solid var(--ui-border-soft);background:color-mix(in srgb,var(--ui-surface) 95%,transparent)}.summary-value,.lane-count,.content-title{font-weight:700;color:var(--ui-text)}.summary-value{font-size:28px}.lane-card{display:flex;justify-content:space-between;align-items:center;text-align:left;cursor:pointer}.lane-card.active{border-color:var(--ui-accent);box-shadow:0 12px 28px rgba(0,0,0,.12)}.lane-title{font-size:16px;font-weight:700;color:var(--ui-text)}.lane-count{font-size:26px}
.toolbar{border:1px solid var(--ui-border)}.toolbar-grid{display:grid;grid-template-columns:200px 1fr 160px 100px;gap:10px}.table{border-radius:16px;overflow:hidden}.pager{display:flex;justify-content:flex-end}
@media (max-width:1100px){.summary-grid,.lane-grid,.hero{grid-template-columns:1fr}} @media (max-width:760px){.toolbar-grid{grid-template-columns:1fr}.page{gap:14px}.hero{padding:16px}}
</style>