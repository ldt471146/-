<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElNotification } from 'element-plus'
import { getMe } from '../api/auth'
import { createCommunityReply, fetchCommunityPostDetail, markCommunityBest } from '../api/community'

const route = useRoute()
const loading = ref(false)
const posting = ref(false)
const marking = ref(false)
const detail = ref(null)
const roles = ref([])
const replyForm = ref({
  content: '',
  codeSnippet: ''
})

const isTeacherOrAdmin = computed(() => roles.value.includes('TEACHER') || roles.value.includes('ADMIN'))
const postId = computed(() => Number(route.params.id))

const load = async () => {
  loading.value = true
  try {
    const [detailRes, meRes] = await Promise.all([
      fetchCommunityPostDetail(postId.value),
      getMe()
    ])
    detail.value = detailRes.data || null
    roles.value = meRes?.data?.roles || []
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '帖子详情加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const submitReply = async () => {
  if (posting.value) return
  posting.value = true
  try {
    await createCommunityReply(postId.value, replyForm.value)
    replyForm.value = { content: '', codeSnippet: '' }
    ElNotification({
      title: '回复成功',
      message: '你的回复已发布',
      type: 'success',
      duration: 1600
    })
    await load()
  } catch (e) {
    ElNotification({
      title: '回复失败',
      message: e?.message || '请稍后重试',
      type: 'error',
      duration: 2000
    })
  } finally {
    posting.value = false
  }
}

const markBest = async (replyId) => {
  if (marking.value) return
  marking.value = true
  try {
    await markCommunityBest(postId.value, replyId)
    ElNotification({
      title: '已标记最佳答案',
      message: '这条回复将优先展示',
      type: 'success',
      duration: 1600
    })
    await load()
  } catch (e) {
    ElNotification({
      title: '操作失败',
      message: e?.message || '请稍后重试',
      type: 'error',
      duration: 2000
    })
  } finally {
    marking.value = false
  }
}

const formatDateTime = (value) => {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

onMounted(load)
</script>

<template>
  <div class="page" v-loading="loading">
    <el-card v-if="detail" class="post" shadow="never">
      <div class="post-title">{{ detail.title }}</div>
      <div class="post-meta">
        <span>作者：{{ detail.authorName || '-' }}</span>
        <span>发布时间：{{ formatDateTime(detail.createdAt) }}</span>
        <span>最后活跃：{{ formatDateTime(detail.lastReplyAt) }}</span>
        <span>浏览：{{ detail.viewCount || 0 }}</span>
        <span>回复：{{ detail.replies?.length || 0 }}</span>
      </div>
      <div class="post-content">{{ detail.content }}</div>
      <pre v-if="detail.codeSnippet" class="code">{{ detail.codeSnippet }}</pre>
    </el-card>

    <el-card class="reply-panel" shadow="never">
      <template #header>发布回复</template>
      <el-form label-position="top">
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="4"
            maxlength="3000"
            show-word-limit
            placeholder="描述你的解法或排查思路"
          />
        </el-form-item>
        <el-form-item label="代码片段（可选）">
          <el-input
            v-model="replyForm.codeSnippet"
            type="textarea"
            :rows="5"
            maxlength="8000"
            show-word-limit
            placeholder="贴上可运行的关键代码片段"
          />
        </el-form-item>
      </el-form>
      <div class="reply-actions">
        <el-button type="primary" :loading="posting" @click="submitReply">提交回复</el-button>
      </div>
    </el-card>

    <el-card class="reply-list" shadow="never">
      <template #header>全部回复</template>
      <div v-if="detail?.replies?.length" class="reply-items">
        <div v-for="item in detail.replies" :key="item.id" class="reply-item">
          <div class="reply-head">
            <div class="name-line">
              <span class="name">{{ item.authorName || '-' }}</span>
              <span class="time">{{ formatDateTime(item.createdAt) }}</span>
              <el-tag v-if="item.isBest === 1" type="success" size="small">最佳答案</el-tag>
            </div>
            <el-button
              v-if="isTeacherOrAdmin && item.isBest !== 1"
              size="small"
              type="success"
              plain
              :loading="marking"
              @click="markBest(item.id)"
            >
              设为最佳
            </el-button>
          </div>
          <div class="reply-content">{{ item.content }}</div>
          <pre v-if="item.codeSnippet" class="code">{{ item.codeSnippet }}</pre>
        </div>
      </div>
      <el-empty v-else description="还没有回复，来写第一条吧" />
    </el-card>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 14px;
}

.post,
.reply-panel,
.reply-list {
  border: 1px solid var(--ui-border);
}

.post-title {
  font-size: 20px;
  font-weight: 700;
}

.post-meta {
  margin-top: 8px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  color: var(--ui-text-muted);
  font-size: 12px;
}

.post-content {
  margin-top: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.code {
  margin-top: 12px;
  padding: 12px;
  border-radius: 10px;
  overflow-x: auto;
  background: var(--ui-surface-soft);
  border: 1px solid var(--ui-border-soft);
  font-family: var(--font-mono);
  font-size: 12px;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
}

.reply-items {
  display: grid;
  gap: 10px;
}

.reply-item {
  border: 1px solid var(--ui-border-soft);
  border-radius: 12px;
  padding: 12px;
  background: var(--ui-surface-soft);
}

.reply-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.name-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.name {
  font-weight: 700;
}

.reply-content {
  margin-top: 8px;
  line-height: 1.7;
  white-space: pre-wrap;
}
</style>
