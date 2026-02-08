<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../api/http'
import { ElNotification } from 'element-plus'

const loading = ref(false)
const notices = ref([])
const page = ref(1)
const size = ref(8)
const total = ref(0)
const tab = ref('all')

const load = async () => {
  loading.value = true
  try {
    const res = await http.get('/api/notices', { params: { page: page.value, size: size.value } })
    notices.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '通知加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const unreadCount = computed(() => notices.value.filter((n) => n.isRead !== 1).length)

const filteredNotices = computed(() => {
  if (tab.value === 'unread') {
    return notices.value.filter((n) => n.isRead !== 1)
  }
  if (tab.value === 'read') {
    return notices.value.filter((n) => n.isRead === 1)
  }
  return notices.value
})

const markRead = async (id) => {
  try {
    await http.post(`/api/notices/${id}/read`)
    const item = notices.value.find((n) => n.id === id)
    if (item) item.isRead = 1
    window.dispatchEvent(new CustomEvent('notice-updated'))
  } catch (e) {
    // ignore
  }
}

const removeNotice = async (id) => {
  try {
    await http.delete(`/api/notices/${id}`)
    notices.value = notices.value.filter((n) => n.id !== id)
    window.dispatchEvent(new CustomEvent('notice-updated'))
  } catch (e) {
    ElNotification({
      title: '删除失败',
      message: e?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  }
}

const markAll = async () => {
  try {
    await http.post('/api/notices/read-all')
    notices.value = notices.value.map((n) => ({ ...n, isRead: 1 }))
    window.dispatchEvent(new CustomEvent('notice-updated'))
  } catch (e) {
    ElNotification({
      title: '操作失败',
      message: e?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  }
}

const onPageChange = async (p) => {
  page.value = p
  await load()
}

const switchTab = (name) => {
  tab.value = name
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">通知中心</div>
        <div class="subtitle">系统公告与学习提醒</div>
      </div>
      <div class="hero-actions">
        <div class="count">
          未读 {{ unreadCount }} / 共 {{ total }}
        </div>
        <el-button size="small" class="ghost" :disabled="unreadCount === 0" @click="markAll">
          全部已读
        </el-button>
      </div>
    </div>

    <div class="tab-bar">
      <el-segmented
        v-model="tab"
        :options="[
          { label: '全部', value: 'all' },
          { label: '未读', value: 'unread' },
          { label: '已读', value: 'read' }
        ]"
        @change="switchTab"
      />
    </div>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-card"></div>
      </template>
      <template #default>
        <div v-if="filteredNotices.length" class="list">
          <div
            v-for="n in filteredNotices"
            :key="n.id"
            class="item"
            :class="{ unread: n.isRead !== 1 }"
            @click="n.isRead !== 1 ? markRead(n.id) : null"
          >
            <div class="dot"></div>
            <div class="content">
              <div class="item-title">{{ n.title }}</div>
              <div class="item-text">{{ n.content }}</div>
            </div>
            <div class="ops">
              <el-button size="small" text @click.stop="markRead(n.id)">
                {{ n.isRead === 1 ? '已读' : '标记已读' }}
              </el-button>
              <el-button size="small" text type="danger" @click.stop="removeNotice(n.id)">
                删除
              </el-button>
            </div>
          </div>
        </div>
        <el-empty
          v-else
          :description="tab === 'unread' ? '暂无未读通知' : tab === 'read' ? '暂无已读通知' : '暂无通知'"
        />
      </template>
    </el-skeleton>

    <div class="pager">
      <el-pagination
        v-if="total > 0"
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.count {
  font-size: 12px;
  color: var(--ui-text-muted);
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid var(--ui-border-soft);
  background: var(--ui-surface-soft);
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

.ghost {
  border: 1px solid var(--ui-border);
  color: var(--ui-text);
  background: transparent;
}

.tab-bar {
  display: flex;
  justify-content: flex-start;
}

.list {
  display: grid;
  gap: 10px;
}

.item {
  display: grid;
  grid-template-columns: 12px 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  background: var(--ui-surface);
  border: 1px solid var(--ui-border-soft);
}

.item.unread {
  border-color: var(--ui-accent);
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--ui-accent);
  box-shadow: 0 0 10px rgba(86, 255, 213, 0.7);
}

.content {
  display: grid;
  gap: 4px;
}

.item-title {
  font-weight: 600;
}

.item-text {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.ops {
  display: grid;
  gap: 2px;
  justify-items: end;
}

.pager {
  display: flex;
  justify-content: flex-end;
}

.skeleton-card {
  height: 220px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
}
</style>
