<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import http from '../api/http'
import { clearAssistantSession, readAssistantSession, writeAssistantSession } from '../utils/assistantSession'
import { getStoredUser } from '../utils/session'

const MAX_MESSAGES = 40
const HISTORY_LIMIT = 8

const defaultMessages = () => ([
  {
    role: 'assistant',
    content: '你好，我是平台学习助手，可协助课程学习、作业说明、考试答疑和练习建议。你可以直接提问，也可以从左侧功能列表开始。'
  }
])

const shortcutSections = [
  {
    title: '常用功能',
    items: [
      { label: '课程答疑', prompt: '请告诉我如何在这个平台上完成一门课程的完整学习流程。' },
      { label: '作业辅导', prompt: '请告诉我如何查看、完成并提交平台中的作业。' },
      { label: '考试说明', prompt: '请说明这个平台里的模拟考试和考试任务有什么区别。' },
      { label: '学习建议', prompt: '请结合编程学习场景，给我一个适合新手的学习建议。' }
    ]
  },
  {
    title: '快捷问题',
    items: [
      { label: '为什么这题做错了？', prompt: '我做题总是出错，请告诉我应该如何分析错题并复盘。' },
      { label: '编程题怎么入手？', prompt: '面对一道编程题时，应该按什么步骤理解题意、设计思路并检查答案？' },
      { label: '如何准备答辩演示？', prompt: '请从毕业设计演示角度，告诉我这个平台应该重点展示哪些功能。' }
    ]
  }
]

const open = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref(defaultMessages())
const bodyRef = ref(null)
const sessionUser = ref(getStoredUser())

const getCurrentUser = () => getStoredUser() || sessionUser.value || null

const persistSession = () => {
  writeAssistantSession(getCurrentUser(), {
    open: open.value,
    messages: messages.value.slice(-MAX_MESSAGES)
  })
}

const restoreSession = () => {
  sessionUser.value = getStoredUser()
  const saved = readAssistantSession(getCurrentUser())
  if (!saved?.messages?.length) {
    open.value = false
    messages.value = defaultMessages()
    return
  }
  open.value = saved.open
  messages.value = saved.messages.slice(-MAX_MESSAGES)
}

const scrollToBottom = async () => {
  await nextTick()
  const el = bodyRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

const openPanel = async () => {
  open.value = true
  await scrollToBottom()
}

const closePanel = () => {
  open.value = false
}

const resetConversation = async () => {
  messages.value = defaultMessages()
  clearAssistantSession(getCurrentUser())
  persistSession()
  await scrollToBottom()
}

const fillPrompt = (prompt) => {
  input.value = prompt
}

const appendAssistantMessage = (content) => {
  messages.value = [...messages.value, { role: 'assistant', content }].slice(-MAX_MESSAGES)
}

const send = async (preset = '') => {
  const content = String(preset || input.value).trim()
  if (!content || loading.value) return

  const history = messages.value.slice(-HISTORY_LIMIT).map((item) => ({
    role: item.role,
    content: item.content
  }))

  messages.value = [...messages.value, { role: 'user', content }].slice(-MAX_MESSAGES)
  input.value = ''
  loading.value = true
  await scrollToBottom()

  try {
    const response = await http.post('/api/assistant/chat', {
      message: content,
      history
    })
    appendAssistantMessage(response.data?.content || '暂时没有回复内容。')
  } catch (error) {
    appendAssistantMessage(error?.message || '抱歉，当前助手连接失败，请稍后再试。')
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

const handleShortcut = async (item) => {
  if (!item?.prompt) return
  fillPrompt(item.prompt)
  await nextTick()
}

const handleKeydown = async (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    await send()
  }
}

const handleProfileUpdated = () => {
  sessionUser.value = getStoredUser()
  restoreSession()
}

watch(open, persistSession)
watch(messages, persistSession, { deep: true })

onMounted(async () => {
  restoreSession()
  window.addEventListener('profile-updated', handleProfileUpdated)
  if (open.value) {
    await scrollToBottom()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('profile-updated', handleProfileUpdated)
})
</script>

<template>
  <div class="assistant-shell">
    <transition name="assistant-fade">
      <section v-if="open" class="assistant-panel">
        <header class="assistant-panel__header">
          <div class="assistant-panel__title-wrap">
            <div class="assistant-panel__badge">AI</div>
            <div>
              <div class="assistant-panel__title">学习助手</div>
              <div class="assistant-panel__sub">课程答疑 · 作业辅导 · 考试说明 · 学习建议</div>
            </div>
          </div>
          <button class="assistant-panel__close" type="button" @click="closePanel">收起</button>
        </header>

        <div class="assistant-panel__content">
          <aside class="assistant-sidebar">
            <div v-for="section in shortcutSections" :key="section.title" class="assistant-section">
              <div class="assistant-section__title">{{ section.title }}</div>
              <button
                v-for="item in section.items"
                :key="item.label"
                class="assistant-section__item"
                type="button"
                @click="handleShortcut(item)"
              >
                {{ item.label }}
              </button>
            </div>

            <div class="assistant-section assistant-section--actions">
              <div class="assistant-section__title">会话管理</div>
              <button class="assistant-section__item is-danger" type="button" @click="resetConversation">清空记录</button>
              <div class="assistant-section__hint">本地自动保存最近 {{ MAX_MESSAGES }} 条对话</div>
            </div>
          </aside>

          <div class="assistant-chat">
            <div ref="bodyRef" class="assistant-chat__body">
              <div class="assistant-chat__tip">提示：点击左侧功能项可快速填入问题，你也可以直接编辑后发送。</div>

              <div v-for="(message, index) in messages" :key="`${message.role}-${index}`" class="assistant-message" :class="message.role">
                <div class="assistant-message__label">{{ message.role === 'assistant' ? '学习助手' : '我' }}</div>
                <div class="assistant-message__bubble">{{ message.content }}</div>
              </div>

              <div v-if="loading" class="assistant-message assistant">
                <div class="assistant-message__label">学习助手</div>
                <div class="assistant-message__bubble is-loading">正在整理回答，请稍等…</div>
              </div>
            </div>

            <footer class="assistant-chat__footer">
              <textarea
                v-model="input"
                class="assistant-chat__input"
                placeholder="请输入课程、练习、作业或考试相关问题，按 Enter 发送，Shift + Enter 换行"
                @keydown="handleKeydown"
              />
              <div class="assistant-chat__actions">
                <button class="assistant-chat__ghost" type="button" @click="input = ''">清空输入</button>
                <button class="assistant-chat__send" type="button" :disabled="loading" @click="send()">发送</button>
              </div>
            </footer>
          </div>
        </div>
      </section>
    </transition>

    <button v-if="!open" class="assistant-trigger" type="button" @click="openPanel">
      <span class="assistant-trigger__badge">AI</span>
      <span class="assistant-trigger__copy">
        <strong>AI 学习助手</strong>
        <em>课程答疑 · 作业辅导 · 支持记录恢复</em>
      </span>
    </button>
  </div>
</template>

<style scoped>
.assistant-shell {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9600;
}

.assistant-trigger,
.assistant-panel,
.assistant-section__item,
.assistant-chat__input,
.assistant-chat__ghost,
.assistant-chat__send,
.assistant-panel__close {
  border: 1px solid var(--ui-border-soft);
  box-shadow: var(--ui-chip-shadow);
}

.assistant-trigger {
  width: 268px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 22px;
  background: color-mix(in srgb, var(--ui-surface) 94%, transparent);
  color: var(--ui-text);
  cursor: pointer;
}

.assistant-trigger__badge,
.assistant-panel__badge {
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, color-mix(in srgb, var(--ui-accent) 82%, #fff), color-mix(in srgb, var(--ui-accent-2) 74%, #fff));
  color: #fff;
  font: 700 14px/1 var(--font-display);
}

.assistant-trigger__copy,
.assistant-panel__title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.assistant-trigger__copy {
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.assistant-trigger__copy strong,
.assistant-panel__title {
  font-size: 14px;
  color: var(--ui-text);
}

.assistant-trigger__copy em,
.assistant-panel__sub,
.assistant-section__hint,
.assistant-message__label,
.assistant-chat__tip {
  font-style: normal;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.assistant-panel {
  width: min(860px, calc(100vw - 32px));
  height: min(620px, calc(100vh - 40px));
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  border-radius: 28px;
  overflow: hidden;
  background: color-mix(in srgb, var(--ui-surface) 96%, transparent);
  backdrop-filter: blur(18px);
}

.assistant-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-bottom: 1px solid var(--ui-border-soft);
}

.assistant-panel__close,
.assistant-chat__ghost,
.assistant-chat__send,
.assistant-section__item {
  border-radius: 14px;
  background: color-mix(in srgb, var(--ui-surface-soft) 94%, transparent);
  color: var(--ui-text);
  cursor: pointer;
}

.assistant-panel__close,
.assistant-chat__ghost,
.assistant-chat__send {
  padding: 10px 14px;
}

.assistant-panel__content {
  min-height: 0;
  display: grid;
  grid-template-columns: 210px minmax(0, 1fr);
}

.assistant-sidebar {
  min-height: 0;
  padding: 18px;
  border-right: 1px solid var(--ui-border-soft);
  background: color-mix(in srgb, var(--ui-surface-soft) 82%, transparent);
  overflow: auto;
}

.assistant-section + .assistant-section {
  margin-top: 18px;
}

.assistant-section__title {
  margin-bottom: 10px;
  font-size: 12px;
  font-weight: 700;
  color: var(--ui-text);
}

.assistant-section__item {
  width: 100%;
  margin-bottom: 8px;
  padding: 11px 12px;
  text-align: left;
}

.assistant-section__item.is-danger {
  color: #b42318;
}

.assistant-chat {
  min-width: 0;
  min-height: 0;
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  overflow: hidden;
}

.assistant-chat__body {
  overflow: auto;
  padding: 18px;
  display: grid;
  align-content: start;
  gap: 12px;
}

.assistant-chat__tip {
  padding: 10px 12px;
  border-radius: 14px;
  background: color-mix(in srgb, var(--ui-accent) 10%, transparent);
  border: 1px solid color-mix(in srgb, var(--ui-accent) 18%, transparent);
}

.assistant-message {
  display: grid;
  gap: 6px;
}

.assistant-message.user {
  justify-items: end;
}

.assistant-message__bubble {
  max-width: min(78%, 560px);
  padding: 12px 14px;
  border-radius: 16px;
  background: var(--ui-surface-soft);
  border: 1px solid var(--ui-border-soft);
  color: var(--ui-text);
  line-height: 1.65;
  white-space: pre-wrap;
}

.assistant-message.user .assistant-message__bubble {
  background: color-mix(in srgb, var(--ui-accent) 12%, transparent);
  border-color: color-mix(in srgb, var(--ui-accent) 22%, transparent);
}

.assistant-message__bubble.is-loading {
  color: var(--ui-text-muted);
}

.assistant-chat__footer {
  padding: 16px 18px 18px;
  border-top: 1px solid var(--ui-border-soft);
  background: color-mix(in srgb, var(--ui-surface-soft) 88%, transparent);
}

.assistant-chat__input {
  width: 100%;
  min-height: 92px;
  padding: 12px 14px;
  border-radius: 18px;
  resize: none;
  outline: none;
  background: var(--ui-surface);
  color: var(--ui-text);
}

.assistant-chat__actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.assistant-chat__send:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.assistant-fade-enter-active,
.assistant-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.assistant-fade-enter-from,
.assistant-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 900px) {
  .assistant-shell {
    right: 12px;
    left: 12px;
    bottom: 12px;
  }

  .assistant-trigger {
    width: 100%;
  }

  .assistant-panel {
    width: 100%;
    height: min(78vh, 680px);
  }

  .assistant-panel__content {
    grid-template-columns: 1fr;
  }

  .assistant-sidebar {
    border-right: none;
    border-bottom: 1px solid var(--ui-border-soft);
  }

  .assistant-message__bubble {
    max-width: 100%;
  }
}
</style>
