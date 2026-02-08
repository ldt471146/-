<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import http from '../api/http'

const open = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref([
  { role: 'assistant', content: '你好！我是你的编程小助手，有问题直接问我吧。' }
])
const pos = ref({ x: 0, y: 0 })
const ball = ref({ x: 0, y: 0 })
const viewport = ref({ w: 0, h: 0 })
const panelSize = ref({ w: 340, h: 420 })
const resizing = ref(false)
const resizeStart = ref({ x: 0, y: 0, w: 0, h: 0 })
const dragging = ref(false)
const offset = ref({ x: 0, y: 0 })
const draggingBall = ref(false)
const ballOffset = ref({ x: 0, y: 0 })

const toggle = () => {
  open.value = !open.value
  if (open.value) {
    nextTick(() => scrollToBottom())
  }
}

const scrollToBottom = () => {
  const el = document.querySelector('.ai-body')
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

const onMouseDown = (e) => {
  dragging.value = true
  const panel = e.currentTarget.closest('.ai-panel')
  if (!panel) return
  const rect = panel.getBoundingClientRect()
  offset.value = { x: e.clientX - rect.left, y: e.clientY - rect.top }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

const onMouseMove = (e) => {
  if (!dragging.value) return
  const panel = document.querySelector('.ai-panel')
  if (!panel) return
  const width = panel.offsetWidth
  const height = panel.offsetHeight
  const maxX = window.innerWidth - width - 12
  const maxY = window.innerHeight - height - 12
  let x = e.clientX - offset.value.x
  let y = e.clientY - offset.value.y
  x = Math.max(12, Math.min(maxX, x))
  y = Math.max(12, Math.min(maxY, y))
  pos.value = { x, y }
}

const onMouseUp = () => {
  dragging.value = false
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
}

const onResizeDown = (e) => {
  resizing.value = true
  resizeStart.value = {
    x: e.clientX,
    y: e.clientY,
    w: panelSize.value.w,
    h: panelSize.value.h
  }
  document.addEventListener('mousemove', onResizing)
  document.addEventListener('mouseup', onResizeUp)
}

const onResizing = (e) => {
  if (!resizing.value) return
  const dx = e.clientX - resizeStart.value.x
  const dy = e.clientY - resizeStart.value.y
  panelSize.value = {
    w: Math.max(280, resizeStart.value.w + dx),
    h: Math.max(260, resizeStart.value.h + dy)
  }
}

const onResizeUp = () => {
  resizing.value = false
  document.removeEventListener('mousemove', onResizing)
  document.removeEventListener('mouseup', onResizeUp)
}

const onBallDown = (e) => {
  draggingBall.value = true
  ballOffset.value = { x: e.clientX - ball.value.x, y: e.clientY - ball.value.y }
  document.addEventListener('mousemove', onBallMove)
  document.addEventListener('mouseup', onBallUp)
}

const onBallMove = (e) => {
  if (!draggingBall.value) return
  const size = 62
  const maxX = window.innerWidth - size - 12
  const maxY = window.innerHeight - size - 12
  let x = e.clientX - ballOffset.value.x
  let y = e.clientY - ballOffset.value.y
  x = Math.max(12, Math.min(maxX, x))
  y = Math.max(12, Math.min(maxY, y))
  ball.value = { x, y }
}

const onBallUp = () => {
  draggingBall.value = false
  document.removeEventListener('mousemove', onBallMove)
  document.removeEventListener('mouseup', onBallUp)
}

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
  document.removeEventListener('mousemove', onBallMove)
  document.removeEventListener('mouseup', onBallUp)
  document.removeEventListener('mousemove', onResizing)
  document.removeEventListener('mouseup', onResizeUp)
  window.removeEventListener('resize', onResize)
})

const onResize = () => {
  viewport.value = { w: window.innerWidth, h: window.innerHeight }
  if (ball.value.x === 0 && ball.value.y === 0) {
    ball.value = { x: viewport.value.w - 90, y: viewport.value.h - 140 }
  }
}

onMounted(() => {
  onResize()
  window.addEventListener('resize', onResize)
})
const send = async () => {
  const content = input.value.trim()
  if (!content || loading.value) return
  messages.value.push({ role: 'user', content })
  input.value = ''
  loading.value = true
  await nextTick()
  scrollToBottom()
  try {
    const res = await http.post('/api/assistant/chat', {
      message: content,
      history: messages.value.slice(-6).map((m) => ({ role: m.role, content: m.content }))
    })
    messages.value.push({ role: 'assistant', content: res.data?.content || '暂无回复' })
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，当前无法连接助手。' })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}
</script>

<template>
  <div class="ai-root">
    <button
      class="ai-bot"
      :style="{ left: `${ball.x}px`, top: `${ball.y}px` }"
      @mousedown="onBallDown"
      @click="toggle"
    >
      <span class="bot-antenna"></span>
      <span class="bot-head">
        <span class="bot-eye left"></span>
        <span class="bot-eye right"></span>
        <span class="bot-mouth"></span>
      </span>
      <span class="bot-glow"></span>
    </button>

    <div
      v-if="open"
      class="ai-panel"
      :style="{
        left: `${pos.x || 60}px`,
        top: `${pos.y || 120}px`,
        width: `${panelSize.w}px`,
        height: `${panelSize.h}px`
      }"
    >
      <div class="ai-header" @mousedown="onMouseDown">
        <div class="ai-title">编程助手</div>
        <div class="ai-sub">实时解答 · 学习引导</div>
        <button class="ai-close" @click="toggle">×</button>
      </div>
      <div class="ai-body">
        <div v-for="(m, idx) in messages" :key="idx" class="ai-msg" :class="m.role">
          <div class="ai-bubble">{{ m.content }}</div>
        </div>
        <div v-if="loading" class="ai-msg assistant">
          <div class="ai-bubble">思考中...</div>
        </div>
      </div>
      <div class="ai-input">
        <input
          v-model="input"
          placeholder="输入你的问题，比如：如何写 for 循环？"
          @keyup.enter="send"
        />
        <button class="ai-send" @click="send">发送</button>
      </div>
      <div class="ai-resize-handle" @mousedown="onResizeDown"></div>
    </div>
  </div>
</template>

<style scoped>
.ai-root {
  position: fixed;
  inset: 0;
  z-index: 9999;
  pointer-events: none;
}

.ai-root > * {
  pointer-events: auto;
}

.ai-bot {
  position: fixed;
  width: 72px;
  height: 84px;
  border: none;
  background: transparent;
  cursor: pointer;
  display: grid;
  place-items: center;
  filter: drop-shadow(0 16px 28px rgba(0, 0, 0, 0.4));
}

.bot-antenna {
  position: absolute;
  top: -2px;
  width: 8px;
  height: 18px;
  border-radius: 999px;
  background: linear-gradient(180deg, #9cffef, #00d7ff);
  box-shadow: 0 0 12px rgba(86, 255, 213, 0.9);
}

.bot-antenna::after {
  content: '';
  position: absolute;
  top: -8px;
  left: -6px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(156, 255, 239, 0.9), rgba(0, 215, 255, 0.6), transparent 70%);
  box-shadow: 0 0 16px rgba(86, 255, 213, 0.8);
}

.bot-head {
  position: relative;
  width: 68px;
  height: 58px;
  border-radius: 20px;
  background:
    radial-gradient(circle at 25% 25%, rgba(200, 255, 245, 0.9), rgba(0, 210, 255, 0.45)),
    linear-gradient(135deg, rgba(10, 20, 30, 0.35), rgba(10, 20, 30, 0.1));
  border: 1px solid rgba(120, 255, 230, 0.6);
  box-shadow:
    inset 0 0 18px rgba(0, 0, 0, 0.2),
    0 0 16px rgba(86, 255, 213, 0.35);
  display: grid;
  place-items: center;
  overflow: hidden;
}

.bot-head::before {
  content: '';
  position: absolute;
  inset: 4px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: linear-gradient(120deg, rgba(255, 255, 255, 0.08), transparent 55%);
}

.bot-head::after {
  content: '';
  position: absolute;
  inset: -10px;
  background: conic-gradient(from 180deg, transparent, rgba(86, 255, 213, 0.18), transparent);
  animation: sweep 4s linear infinite;
}

.bot-eye {
  position: absolute;
  top: 18px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: radial-gradient(circle, #03131a 35%, #00f5ff 100%);
  box-shadow: 0 0 12px rgba(0, 210, 255, 0.8);
  animation: eyeGlow 2.2s ease-in-out infinite;
}

.bot-eye.left {
  left: 16px;
}

.bot-eye.right {
  right: 16px;
}

.bot-mouth {
  position: absolute;
  bottom: 12px;
  width: 28px;
  height: 6px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(6, 16, 24, 0.3), rgba(6, 16, 24, 0.85));
  box-shadow: inset 0 0 6px rgba(0, 210, 255, 0.2);
}

.bot-glow {
  position: absolute;
  inset: -12px;
  border-radius: 24px;
  background: radial-gradient(circle, rgba(86, 255, 213, 0.3), transparent 60%);
  animation: orbPulse 2.6s ease-in-out infinite;
}

.ai-panel {
  position: fixed;
  background: radial-gradient(circle at top left, rgba(86, 255, 213, 0.12), transparent 45%),
    var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
}

.ai-header {
  padding: 12px 14px;
  background: linear-gradient(120deg, rgba(86, 255, 213, 0.18), rgba(0, 210, 255, 0.12));
  border-bottom: 1px solid var(--ui-border);
  position: relative;
  cursor: move;
  user-select: none;
}

.ai-title {
  font-weight: 700;
}

.ai-sub {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 2px;
}

.ai-close {
  position: absolute;
  right: 10px;
  top: 8px;
  border: none;
  background: transparent;
  color: var(--ui-text);
  font-size: 18px;
  cursor: pointer;
}

.ai-body {
  flex: 1;
  overflow: auto;
  padding: 12px;
  display: grid;
  gap: 8px;
}

.ai-msg {
  display: flex;
}

.ai-msg.user {
  justify-content: flex-end;
}

.ai-bubble {
  max-width: 230px;
  padding: 8px 10px;
  border-radius: 12px;
  background: var(--ui-surface-soft);
  border: 1px solid var(--ui-border-soft);
  font-size: 12px;
  line-height: 1.5;
}

.ai-msg.user .ai-bubble {
  background: rgba(86, 255, 213, 0.18);
  border-color: rgba(86, 255, 213, 0.25);
}

.ai-input {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  padding: 10px;
  border-top: 1px solid var(--ui-border);
  background: var(--ui-surface-soft);
}

.ai-input input {
  width: 100%;
}

.ai-send {
  width: 74px;
}

.ai-resize-handle {
  position: absolute;
  right: 6px;
  bottom: 6px;
  width: 14px;
  height: 14px;
  border-right: 2px solid rgba(86, 255, 213, 0.6);
  border-bottom: 2px solid rgba(86, 255, 213, 0.6);
  cursor: se-resize;
}
.ai-bot {
  animation: orbFloat 4s ease-in-out infinite;
}

.ai-input input {
  width: 100%;
  border-radius: 10px;
  border: 1px solid var(--ui-border-soft);
  padding: 8px 10px;
  background: var(--ui-surface-soft);
  color: var(--ui-text);
  outline: none;
  font-size: 12px;
}

.ai-send {
  width: 74px;
  border: none;
  background: linear-gradient(120deg, var(--ui-accent), var(--ui-accent-2));
  color: #07101a;
  border-radius: 10px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 12px;
}

@keyframes aiPulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.05);
    opacity: 1;
  }
}

@keyframes orbFloat {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
}

@keyframes orbPulse {
  0%,
  100% {
    opacity: 0.4;
  }
  50% {
    opacity: 0.9;
  }
}

@keyframes eyeGlow {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.15);
    opacity: 1;
  }
}

@keyframes sweep {
  0% {
    transform: rotate(0deg);
    opacity: 0.2;
  }
  50% {
    opacity: 0.5;
  }
  100% {
    transform: rotate(360deg);
    opacity: 0.2;
  }
}
</style>
