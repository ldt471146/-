<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { createCommunityPost, fetchCommunityPosts } from '../api/community'

const router = useRouter()
const loading = ref(false)
const posting = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')

const createOpen = ref(false)
const form = ref({
  title: '',
  content: '',
  codeSnippet: ''
})

const load = async () => {
  loading.value = true
  try {
    const res = await fetchCommunityPosts({
      page: page.value,
      size: size.value,
      keyword: keyword.value.trim() || undefined
    })
    const data = res.data || {}
    list.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '社区帖子加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const search = async () => {
  page.value = 1
  await load()
}

const resetForm = () => {
  form.value = { title: '', content: '', codeSnippet: '' }
}

const submitPost = async () => {
  if (posting.value) return
  posting.value = true
  try {
    await createCommunityPost(form.value)
    ElNotification({
      title: '发布成功',
      message: '帖子已发布，快去看看回复吧',
      type: 'success',
      duration: 1600
    })
    createOpen.value = false
    resetForm()
    await search()
  } catch (e) {
    ElNotification({
      title: '发布失败',
      message: e?.message || '请稍后重试',
      type: 'error',
      duration: 2000
    })
  } finally {
    posting.value = false
  }
}

const goDetail = (id) => {
  router.push(`/community/${id}`)
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">编程社区</div>
        <div class="subtitle">提问、解答、分享你的思路与代码片段</div>
      </div>
      <el-button type="primary" @click="createOpen = true">发布问题</el-button>
    </div>

    <el-card class="toolbar" shadow="never">
      <div class="toolbar-grid">
        <el-input
          v-model="keyword"
          placeholder="搜索标题或内容关键字"
          clearable
          @keyup.enter="search"
        />
        <el-button type="primary" @click="search">查询</el-button>
      </div>
    </el-card>

    <div class="list" v-loading="loading">
      <el-card
        v-for="item in list"
        :key="item.id"
        class="post-card"
        shadow="hover"
        @click="goDetail(item.id)"
      >
        <div class="post-head">
          <div class="post-title">{{ item.title }}</div>
          <el-tag v-if="item.bestReplyId" type="success" size="small">含最佳答案</el-tag>
        </div>
        <div class="post-preview">{{ item.contentPreview || '暂无内容摘要' }}</div>
        <div class="post-meta">
          <span>作者：{{ item.authorName || '-' }}</span>
          <span>回复：{{ item.replyCount || 0 }}</span>
          <span>浏览：{{ item.viewCount || 0 }}</span>
        </div>
      </el-card>
      <el-empty v-if="!loading && !list.length" description="暂时没有帖子，去发布第一个吧" />
    </div>

    <div class="pager">
      <el-pagination
        background
        layout="total, prev, pager, next, sizes"
        :current-page="page"
        :page-size="size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        @current-change="(v) => { page = v; load() }"
        @size-change="(v) => { size = v; page = 1; load() }"
      />
    </div>

    <el-dialog
      v-model="createOpen"
      title="发布问题帖"
      width="720px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="120" show-word-limit placeholder="例如：为什么我的循环总是超时？" />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            maxlength="5000"
            show-word-limit
            placeholder="描述你遇到的问题、期望结果和已尝试的方法"
          />
        </el-form-item>
        <el-form-item label="代码片段（可选）">
          <el-input
            v-model="form.codeSnippet"
            type="textarea"
            :rows="6"
            maxlength="8000"
            show-word-limit
            placeholder="粘贴最小可复现代码片段"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createOpen = false">取消</el-button>
        <el-button type="primary" :loading="posting" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>
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

.toolbar {
  border: 1px solid var(--ui-border);
}

.toolbar-grid {
  display: grid;
  grid-template-columns: 1fr 100px;
  gap: 10px;
}

.list {
  display: grid;
  gap: 10px;
}

.post-card {
  border: 1px solid var(--ui-border);
  cursor: pointer;
  transition: transform 0.2s ease;
}

.post-card:hover {
  transform: translateY(-1px);
}

.post-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.post-title {
  font-size: 16px;
  font-weight: 700;
}

.post-preview {
  margin-top: 8px;
  color: var(--ui-text-muted);
  font-size: 13px;
  line-height: 1.6;
}

.post-meta {
  margin-top: 10px;
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.pager {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 960px) {
  .toolbar-grid {
    grid-template-columns: 1fr;
  }
}
</style>
