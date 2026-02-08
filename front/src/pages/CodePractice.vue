<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../api/http'
import { ElNotification } from 'element-plus'

const problems = ref([])
const activeId = ref(null)
const detail = ref(null)
const loading = ref(false)
const submitting = ref(false)
const result = ref(null)

const languages = [
  { label: 'C (GCC 9.2.0)', id: 50 },
  { label: 'C++ (GCC 9.2.0)', id: 54 },
  { label: 'Python 3', id: 71 }
]
const languageId = ref(languages[1].id)
const sourceCode = ref(`// 在这里编写你的代码\n#include <bits/stdc++.h>\nusing namespace std;\n\nint main(){\n    ios::sync_with_stdio(false);\n    cin.tie(nullptr);\n\n    long long a,b;\n    if(!(cin>>a>>b)) return 0;\n    cout<<a+b;\n    return 0;\n}\n`)

const activeProblem = computed(() =>
  problems.value.find((p) => p.id === activeId.value)
)

const loadList = async () => {
  const res = await http.get('/api/code/problems')
  problems.value = res.data || []
  if (!activeId.value && problems.value.length) {
    activeId.value = problems.value[0].id
  }
}

const loadDetail = async () => {
  if (!activeId.value) return
  loading.value = true
  try {
    const res = await http.get(`/api/code/problems/${activeId.value}`)
    detail.value = res.data
    result.value = null
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '题目加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!detail.value) return
  if (!sourceCode.value.trim()) {
    ElNotification({
      title: '请输入代码',
      message: '代码不能为空',
      type: 'warning',
      duration: 1500
    })
    return
  }
  submitting.value = true
  try {
    const res = await http.post('/api/code/submit', {
      problemId: detail.value.id,
      languageId: languageId.value,
      sourceCode: sourceCode.value
    })
    result.value = res.data
  } catch (e) {
    ElNotification({
      title: '提交失败',
      message: e?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  } finally {
    submitting.value = false
  }
}

const onChangeProblem = async (id) => {
  activeId.value = id
  await loadDetail()
}

onMounted(async () => {
  await loadList()
  await loadDetail()
})
</script>

<template>
  <div class="code-page">
    <div class="header">
      <div>
        <div class="title display">算法竞技场</div>
        <div class="subtitle">本机 Judge0 判题，极速反馈</div>
      </div>
      <div class="legend">
        <span class="dot"></span>
        online judge
      </div>
    </div>

    <div class="layout">
      <div class="sidebar">
        <div class="side-title">题目列表</div>
        <div class="list">
          <button
            v-for="p in problems"
            :key="p.id"
            class="list-item"
            :class="{ active: p.id === activeId }"
            @click="onChangeProblem(p.id)"
          >
            <div class="tag">P{{ p.id }}</div>
            <div>
              <div class="item-title">{{ p.title }}</div>
              <div class="item-meta">难度 {{ p.difficulty || 1 }}</div>
            </div>
          </button>
        </div>
      </div>

      <div class="content">
        <el-skeleton :loading="loading" animated>
          <template #template>
            <div class="skeleton-block"></div>
          </template>
          <template #default>
            <div v-if="detail" class="panel">
              <div class="panel-head">
                <div class="panel-title">{{ detail.title }}</div>
                <div class="panel-meta">
                  时间限制 {{ detail.timeLimit || 1000 }}ms · 内存 {{ detail.memoryLimit || 256 }}MB
                </div>
              </div>

              <div class="panel-body">
                <div class="desc">{{ detail.content }}</div>

                <div class="samples" v-if="detail.samples?.length">
                  <div class="sample-title">样例</div>
                  <div class="sample-grid">
                    <div v-for="(s, i) in detail.samples" :key="i" class="sample">
                      <div class="sample-label">输入</div>
                      <pre>{{ s.input }}</pre>
                      <div class="sample-label">输出</div>
                      <pre>{{ s.output }}</pre>
                    </div>
                  </div>
                </div>
              </div>

              <div class="editor">
                <div class="editor-bar">
                  <el-select v-model="languageId" class="lang-select">
                    <el-option v-for="l in languages" :key="l.id" :label="l.label" :value="l.id" />
                  </el-select>
                  <el-button type="primary" :loading="submitting" @click="submit">提交判题</el-button>
                </div>
                <textarea v-model="sourceCode" class="code-input"></textarea>
              </div>

              <div v-if="result" class="result-card" :class="{ ok: result.result === 'AC' }">
                <div class="result-title">{{ result.result === 'AC' ? 'Accepted' : 'Wrong Answer' }}</div>
                <div class="result-meta">
                  通过 {{ result.passed || 0 }} / {{ result.total || 0 }}
                </div>
                <div class="result-msg">
                  {{ (result.messages || []).join('、') }}
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无编程题" />
          </template>
        </el-skeleton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.code-page {
  display: grid;
  gap: 16px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.legend {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.legend .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--ui-accent);
  box-shadow: 0 0 10px rgba(86, 255, 213, 0.8);
}

.layout {
  display: grid;
  gap: 16px;
  grid-template-columns: 280px 1fr;
}

.sidebar {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 14px;
  padding: 16px;
  display: grid;
  gap: 10px;
}

.side-title {
  font-size: 12px;
  color: var(--ui-text-muted);
  letter-spacing: 0.12em;
}

.list {
  display: grid;
  gap: 10px;
}

.list-item {
  display: grid;
  grid-template-columns: 46px 1fr;
  gap: 10px;
  align-items: center;
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--ui-surface-soft);
  border: 1px solid transparent;
  color: var(--ui-text);
  cursor: pointer;
  text-align: left;
}

.list-item.active {
  border-color: var(--ui-accent);
  box-shadow: 0 0 16px rgba(86, 255, 213, 0.25);
}

.tag {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--ui-accent), var(--ui-accent-2));
  color: #07101a;
  display: grid;
  place-items: center;
  font-weight: 700;
}

.item-title {
  font-weight: 600;
}

.item-meta {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 2px;
}

.content {
  display: grid;
  gap: 16px;
}

.panel {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 16px;
  padding: 18px;
  display: grid;
  gap: 16px;
}

.panel-title {
  font-size: 18px;
  font-weight: 700;
}

.panel-meta {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 4px;
}

.desc {
  white-space: pre-wrap;
  font-size: 13px;
  line-height: 1.6;
}

.samples {
  display: grid;
  gap: 10px;
}

.sample-title {
  font-size: 12px;
  color: var(--ui-text-muted);
  letter-spacing: 0.12em;
}

.sample-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
}

.sample {
  background: var(--ui-surface-soft);
  border: 1px solid var(--ui-border-soft);
  border-radius: 12px;
  padding: 10px;
}

.sample-label {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-bottom: 4px;
}

pre {
  background: rgba(8, 12, 20, 0.5);
  padding: 8px;
  border-radius: 10px;
  color: var(--ui-text);
  font-family: var(--font-mono);
  font-size: 12px;
  white-space: pre-wrap;
}

.editor {
  display: grid;
  gap: 10px;
}

.editor-bar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.lang-select {
  width: 240px;
}

.code-input {
  min-height: 260px;
  background: #0c1220;
  color: #eaf4ff;
  border: 1px solid rgba(86, 255, 213, 0.2);
  border-radius: 14px;
  padding: 12px;
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.5;
  outline: none;
}

.result-card {
  border-radius: 12px;
  padding: 12px;
  background: rgba(255, 77, 109, 0.15);
  border: 1px solid rgba(255, 77, 109, 0.3);
}

.result-card.ok {
  background: rgba(34, 197, 94, 0.15);
  border-color: rgba(34, 197, 94, 0.4);
}

.result-title {
  font-weight: 700;
}

.result-meta {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 4px;
}

.result-msg {
  font-size: 12px;
  margin-top: 6px;
}

.skeleton-block {
  height: 520px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.08);
}

@media (max-width: 960px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
</style>
