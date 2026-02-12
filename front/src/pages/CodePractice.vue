<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import http from '../api/http'
import { ElNotification } from 'element-plus'

const route = useRoute()
const problems = ref([])
const activeId = ref(null)
const detail = ref(null)
const loading = ref(false)
const submitting = ref(false)
const result = ref(null)
const keyword = ref('')
const difficultyFilter = ref('all')
const showGuide = ref(true)

const languages = [
  { label: 'C (GCC 9.2.0)', id: 50 },
  { label: 'C++ (GCC 9.2.0)', id: 54 },
  { label: 'Python 3', id: 71 }
]
const languageId = ref(languages[1].id)
const sourceCode = ref('')

const starterMap = {
  50: `#include <stdio.h>

int main() {
    // TODO: write your code
    return 0;
}
`,
  54: `#include <bits/stdc++.h>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    // TODO: write your code
    return 0;
}
`,
  71: `# TODO: write your code
def solve():
    pass

if __name__ == "__main__":
    solve()
`
}

const activeProblem = computed(() =>
  problems.value.find((p) => p.id === activeId.value)
)

const filteredProblems = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  return problems.value.filter((p) => {
    const hitKeyword = !q || p.title?.toLowerCase().includes(q) || String(p.id).includes(q)
    const hitDifficulty = difficultyFilter.value === 'all' || String(p.difficulty || 1) === difficultyFilter.value
    return hitKeyword && hitDifficulty
  })
})

const difficultyText = (d) => {
  if (d === 1) return '入门'
  if (d === 2) return '进阶'
  return '挑战'
}

const resultTitleMap = {
  AC: 'Accepted',
  WA: 'Wrong Answer',
  CE: 'Compile Error',
  RE: 'Runtime Error',
  TLE: 'Time Limit Exceeded',
  IE: 'Internal Error'
}

const resultTitle = computed(() => {
  const code = result.value?.result
  return result.value?.resultLabel || resultTitleMap[code] || code || 'Unknown'
})

const resultClass = computed(() => {
  const code = result.value?.result
  if (code === 'AC') return 'ok'
  if (code === 'WA') return 'wa'
  if (code === 'CE') return 'ce'
  if (code === 'RE') return 're'
  if (code === 'TLE') return 'tle'
  if (code === 'IE') return 'ie'
  return ''
})

const resultHint = computed(() => {
  const errorType = result.value?.errorType
  if (errorType === 'COMPILE_ERROR') return '请优先检查语法、头文件和括号匹配。'
  if (errorType === 'RUNTIME_ERROR') return '请检查数组越界、空指针、除零等运行时问题。'
  if (errorType === 'TIMEOUT') return '请优化算法复杂度，避免在循环内做重复高开销操作。'
  if (errorType === 'WRONG_ANSWER') return '请核对边界条件、输入输出格式和换行。'
  if (errorType === 'SYSTEM_ERROR') return '系统繁忙或判题环境异常，请稍后重试。'
  return '通过后可以尝试更优写法，提升可读性与性能。'
})

const fillStarter = () => {
  sourceCode.value = starterMap[languageId.value] || starterMap[54]
}

const loadList = async () => {
  const params = {}
  if (route.query.courseId) params.courseId = Number(route.query.courseId)
  if (route.query.chapterId) params.chapterId = Number(route.query.chapterId)
  const res = await http.get('/api/code/problems', { params })
  problems.value = res.data || []
  const presetId = route.query.problemId ? Number(route.query.problemId) : null
  if (presetId && problems.value.some(p => p.id === presetId)) {
    activeId.value = presetId
  } else if (!activeId.value && problems.value.length) {
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

watch(languageId, () => {
  if (!sourceCode.value.trim()) {
    fillStarter()
  }
})

onMounted(async () => {
  fillStarter()
  await loadList()
  await loadDetail()
})
</script>

<template>
  <div class="code-page">
    <div class="page-head">
      <div class="head-main">
        <div class="title display">编程判题中心</div>
        <div class="subtitle">融合训练题单与本机判题，专注完成每一道算法题</div>
      </div>
      <div class="head-meta">
        <span class="meta-chip">题目 {{ problems.length }}</span>
        <span class="meta-chip">当前 {{ activeProblem?.id ? `P${activeProblem.id}` : '--' }}</span>
      </div>
    </div>

    <div class="layout">
      <aside class="problem-panel">
        <div class="panel-head">
          <div class="panel-title">题目目录</div>
          <div class="panel-sub">{{ filteredProblems.length }} / {{ problems.length }}</div>
        </div>
        <div class="panel-tools">
          <input v-model="keyword" class="search" placeholder="搜索题号或标题" />
          <el-select v-model="difficultyFilter" size="small">
            <el-option label="全部难度" value="all" />
            <el-option label="入门" value="1" />
            <el-option label="进阶" value="2" />
            <el-option label="挑战" value="3" />
          </el-select>
        </div>
        <div class="list">
          <button
            v-for="p in filteredProblems"
            :key="p.id"
            class="list-item"
            :class="{ active: p.id === activeId }"
            @click="onChangeProblem(p.id)"
          >
            <div class="tag">P{{ p.id }}</div>
            <div class="item-main">
              <div class="item-title">{{ p.title }}</div>
              <div class="item-meta">难度：{{ difficultyText(p.difficulty || 1) }}</div>
            </div>
          </button>
          <el-empty v-if="!filteredProblems.length" description="没有匹配题目" />
        </div>
      </aside>

      <div class="content">
        <el-skeleton :loading="loading" animated>
          <template #template>
            <div class="skeleton-block"></div>
          </template>
          <template #default>
            <div v-if="detail" class="panel">
              <div class="problem-head">
                <div class="problem-title-wrap">
                  <div class="problem-id">P{{ detail.id }}</div>
                  <div>
                    <div class="problem-title">{{ detail.title }}</div>
                    <div class="problem-meta">时间限制 {{ detail.timeLimit || 1000 }}ms · 内存 {{ detail.memoryLimit || 256 }}MB</div>
                  </div>
                </div>
                <div class="problem-badge">本机判题</div>
              </div>

              <div class="workbench">
                <section class="statement">
                  <div class="block-title">题目描述</div>
                  <div class="desc">{{ detail.content }}</div>

                  <div class="samples" v-if="detail.samples?.length">
                    <div class="block-title">样例数据</div>
                    <div class="sample-grid">
                      <div v-for="(s, i) in detail.samples" :key="i" class="sample">
                        <div class="sample-label">样例 {{ i + 1 }} 输入</div>
                        <pre>{{ s.input }}</pre>
                        <div class="sample-label">样例 {{ i + 1 }} 输出</div>
                        <pre>{{ s.output }}</pre>
                      </div>
                    </div>
                  </div>
                </section>

                <section class="editor">
                  <div class="editor-bar">
                    <el-select v-model="languageId" class="lang-select">
                      <el-option v-for="l in languages" :key="l.id" :label="l.label" :value="l.id" />
                    </el-select>
                    <div class="editor-actions">
                      <el-button plain @click="fillStarter">填入模板</el-button>
                      <el-button type="primary" :loading="submitting" @click="submit">提交判题</el-button>
                    </div>
                  </div>
                  <div class="guide-box">
                    <button class="guide-toggle" @click="showGuide = !showGuide">
                      {{ showGuide ? '收起引导' : '展开引导' }}
                    </button>
                    <div v-if="showGuide" class="guide-content">
                      <div class="guide-title">提交前引导</div>
                      <div class="guide-line">1. 先点击“填入模板”，补全 `solve/main` 逻辑。</div>
                      <div class="guide-line">2. 输入输出要严格匹配样例，注意空格和换行。</div>
                      <div class="guide-line">3. 先处理边界值：空输入、极大值、负数、重复值。</div>
                      <div v-if="detail.samples?.length" class="guide-line">
                        可参考样例输入：<code>{{ (detail.samples[0]?.input || '').trim() || '-' }}</code>
                      </div>
                    </div>
                  </div>
                  <textarea v-model="sourceCode" class="code-input"></textarea>
                </section>
              </div>

              <div v-if="result" class="result-card" :class="resultClass">
                <div class="result-line">
                  <div class="result-title">{{ resultTitle }}</div>
                  <div class="result-score">
                    通过 {{ result.passed || 0 }} / {{ result.total || 0 }}
                    <span v-if="result.failedCaseIndex"> · 失败用例 #{{ result.failedCaseIndex }}</span>
                  </div>
                </div>
                <div class="result-msg">
                  {{ (result.messages || []).join('、') || '暂无详细信息' }}
                </div>
                <div class="result-hint">{{ resultHint }}</div>
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
  gap: 14px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  padding: 2px 2px 8px;
}

.title {
  font-size: 24px;
  font-weight: 700;
}

.subtitle {
  font-size: 13px;
  color: var(--ui-text-muted);
  margin-top: 4px;
}

.head-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.meta-chip {
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid var(--ui-border);
  background: var(--ui-surface-soft);
  color: var(--ui-text-muted);
  font-size: 12px;
}

.layout {
  display: grid;
  gap: 14px;
  grid-template-columns: 310px 1fr;
}

.problem-panel {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 14px;
  padding: 12px;
  display: grid;
  gap: 12px;
  align-content: start;
  max-height: calc(100vh - 180px);
  overflow: hidden;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--ui-text);
}

.panel-sub {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.panel-tools {
  display: grid;
  gap: 8px;
}

.search {
  border: 1px solid var(--ui-border-soft);
  background: var(--ui-surface-soft);
  color: var(--ui-text);
  border-radius: 10px;
  padding: 8px 10px;
  outline: none;
}

.search:focus {
  border-color: var(--ui-accent);
}

.list {
  display: grid;
  gap: 8px;
  overflow: auto;
  padding-right: 2px;
}

.list-item {
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 8px;
  align-items: center;
  padding: 10px;
  border-radius: 10px;
  background: var(--ui-surface-soft);
  border: 1px solid var(--ui-border-soft);
  color: var(--ui-text);
  cursor: pointer;
  text-align: left;
  transition: 0.2s ease;
}

.list-item:hover {
  transform: translateY(-1px);
  border-color: var(--ui-border);
}

.list-item.active {
  border-color: var(--ui-accent);
  box-shadow: 0 0 0 2px rgba(86, 255, 213, 0.14) inset;
}

.tag {
  width: 44px;
  height: 44px;
  border-radius: 10px;
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
  margin-top: 1px;
}

.content {
  display: grid;
  gap: 14px;
}

.panel {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 16px;
  padding: 14px;
  display: grid;
  gap: 12px;
}

.problem-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.problem-title-wrap {
  display: flex;
  gap: 10px;
}

.problem-id {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  background: linear-gradient(145deg, rgba(86, 255, 213, 0.25), rgba(0, 210, 255, 0.2));
  border: 1px solid var(--ui-border);
  color: var(--ui-text);
  display: grid;
  place-items: center;
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 700;
}

.problem-title {
  font-size: 19px;
  font-weight: 700;
}

.problem-meta {
  margin-top: 4px;
  font-size: 13px;
  color: var(--ui-text-muted);
}

.problem-badge {
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid var(--ui-border);
  background: var(--ui-surface-soft);
  color: var(--ui-text-muted);
  font-size: 12px;
}

.workbench {
  display: grid;
  gap: 12px;
  grid-template-columns: 0.95fr 1.05fr;
}

.statement,
.editor {
  border: 1px solid var(--ui-border-soft);
  border-radius: 12px;
  background: var(--ui-surface-soft);
  padding: 12px;
}

.block-title {
  font-size: 12px;
  color: var(--ui-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.12em;
  margin-bottom: 8px;
}

.desc {
  white-space: pre-wrap;
  font-size: 14px;
  line-height: 1.6;
}

.samples {
  display: grid;
  gap: 8px;
  margin-top: 12px;
}

.sample-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
}

.sample {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border-soft);
  border-radius: 10px;
  padding: 10px;
}

.sample-label {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-bottom: 4px;
}

pre {
  background: rgba(7, 16, 26, 0.78);
  padding: 8px;
  border-radius: 8px;
  color: #e9f6ff;
  font-family: var(--font-mono);
  font-size: 12px;
  white-space: pre-wrap;
  line-height: 1.5;
}

.editor {
  display: grid;
  gap: 8px;
}

.guide-box {
  border: 1px dashed var(--ui-border);
  border-radius: 10px;
  background: rgba(86, 255, 213, 0.05);
  padding: 8px 10px;
}

.guide-toggle {
  border: 1px solid var(--ui-border);
  background: transparent;
  color: var(--ui-text);
  font-size: 12px;
  border-radius: 999px;
  padding: 4px 10px;
  cursor: pointer;
}

.guide-content {
  margin-top: 8px;
  display: grid;
  gap: 4px;
}

.guide-title {
  font-size: 12px;
  font-weight: 700;
  color: var(--ui-accent);
}

.guide-line {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.editor-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.lang-select {
  width: 200px;
}

.editor-actions {
  display: flex;
  gap: 8px;
}

.code-input {
  min-height: 380px;
  background: var(--code-input-bg, #0c1220);
  color: var(--code-input-text, #eaf4ff);
  border: 1px solid rgba(86, 255, 213, 0.22);
  border-radius: 10px;
  padding: 12px;
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.5;
  outline: none;
  resize: vertical;
}

:global(html[data-theme='aurora']) .code-input {
  --code-input-bg: #f4f8ff;
  --code-input-text: #1a2433;
  border-color: #c9d7ee;
}

.code-input:focus {
  border-color: var(--ui-accent);
}

.result-card {
  border-radius: 10px;
  padding: 10px 12px;
  border: 1px solid var(--ui-border-soft);
  background: var(--ui-surface-soft);
}

.result-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.result-title {
  font-weight: 800;
}

.result-score {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.result-card.ok {
  border-color: rgba(50, 200, 120, 0.45);
  background: rgba(50, 200, 120, 0.13);
}

.result-card.wa {
  border-color: rgba(245, 158, 11, 0.45);
  background: rgba(245, 158, 11, 0.13);
}

.result-card.ce,
.result-card.re {
  border-color: rgba(239, 68, 68, 0.45);
  background: rgba(239, 68, 68, 0.13);
}

.result-card.tle {
  border-color: rgba(59, 130, 246, 0.45);
  background: rgba(59, 130, 246, 0.13);
}

.result-card.ie {
  border-color: rgba(148, 163, 184, 0.45);
  background: rgba(148, 163, 184, 0.14);
}

.result-msg {
  font-size: 12px;
  margin-top: 5px;
  color: var(--ui-text-muted);
}

.result-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--ui-text);
}

.skeleton-block {
  height: 620px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.08);
}

@media (max-width: 1180px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .problem-panel {
    max-height: none;
  }
  .workbench {
    grid-template-columns: 1fr;
  }
  .code-input {
    min-height: 280px;
  }
}

@media (max-width: 760px) {
  .page-head {
    flex-direction: column;
    align-items: flex-start;
  }
  .editor-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .lang-select {
    width: 100%;
  }
  .editor-actions {
    width: 100%;
  }
  .editor-actions :deep(.el-button) {
    flex: 1;
  }
}
</style>
