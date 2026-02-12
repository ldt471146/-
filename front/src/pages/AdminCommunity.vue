<script setup>
import { onMounted, ref } from 'vue'
import { ElMessageBox, ElNotification } from 'element-plus'
import {
  fetchAdminCommunityPosts,
  fetchAdminCommunityReplies,
  reviewAdminCommunityPost,
  reviewAdminCommunityReply
} from '../api/admin'

const tab = ref('posts')
const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const status = ref('all')

const load = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (status.value !== 'all') params.status = Number(status.value)
    if (keyword.value.trim()) params.keyword = keyword.value.trim()

    const res = tab.value === 'posts'
      ? await fetchAdminCommunityPosts(params)
      : await fetchAdminCommunityReplies(params)
    const data = res.data || {}
    list.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '审核列表加载失败',
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

const doReview = async (id, action, reason = '') => {
  try {
    if (tab.value === 'posts') {
      await reviewAdminCommunityPost(id, { action, reason })
    } else {
      await reviewAdminCommunityReply(id, { action, reason })
    }
    ElNotification({
      title: '操作成功',
      message: `已执行 ${action}`,
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
  }
}

const muteUser = async (id) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入禁言原因（会记录到审计日志）', '禁言用户', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '例如：发布违规内容'
    })
    await doReview(id, 'MUTE_USER', value || '社区违规')
  } catch (e) {
    if (e !== 'cancel') {
      ElNotification({
        title: '操作失败',
        message: e?.message || '请稍后重试',
        type: 'error',
        duration: 2000
      })
    }
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">社区内容审核</div>
        <div class="subtitle">对帖子和回复进行审核、删除、禁言</div>
      </div>
    </div>

    <el-card class="toolbar" shadow="never">
      <div class="toolbar-grid">
        <el-segmented v-model="tab" :options="[
          { label: '帖子审核', value: 'posts' },
          { label: '回复审核', value: 'replies' }
        ]" @change="search" />
        <el-input v-model="keyword" placeholder="关键字搜索" clearable @keyup.enter="search" />
        <el-select v-model="status" @change="search">
          <el-option label="全部状态" value="all" />
          <el-option label="正常" value="1" />
          <el-option label="隐藏" value="2" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>
    </el-card>

    <el-table :data="list" v-loading="loading" class="table">
      <el-table-column label="ID" prop="id" width="76" />
      <el-table-column v-if="tab === 'posts'" label="标题" prop="title" min-width="180" />
      <el-table-column v-if="tab === 'posts'" label="作者" prop="authorName" min-width="120" />
      <el-table-column v-if="tab === 'posts'" label="摘要" prop="contentPreview" min-width="220" />

      <el-table-column v-if="tab === 'replies'" label="帖子ID" prop="postId" width="90" />
      <el-table-column v-if="tab === 'replies'" label="回复人" prop="authorName" min-width="120" />
      <el-table-column v-if="tab === 'replies'" label="回复内容" prop="content" min-width="220" />

      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '正常' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320">
        <template #default="{ row }">
          <el-button size="small" type="success" plain @click="doReview(row.id, 'APPROVE')">通过</el-button>
          <el-button size="small" type="warning" plain @click="doReview(row.id, 'DELETE', '管理员删除')">隐藏</el-button>
          <el-button size="small" type="danger" plain @click="muteUser(row.id)">禁言用户</el-button>
        </template>
      </el-table-column>
    </el-table>

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
  grid-template-columns: 200px 1fr 160px 100px;
  gap: 10px;
}

.table {
  border-radius: 12px;
  overflow: hidden;
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
