<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import http from '../api/http'

const open = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref([
  { role: 'assistant', content: '你好，我是你的二次元编程助手。想练哪一块，我来带你。' }
])

const viewport = ref({ w: 0, h: 0 })
const ball = ref({ x: 0, y: 0 })
const panelSize = ref({ w: 380, h: 500 })
const pos = ref({ x: 0, y: 0 })

const draggingPanel = ref(false)
const panelOffset = ref({ x: 0, y: 0 })
const resizing = ref(false)
const resizeStart = ref({ x: 0, y: 0, w: 0, h: 0 })
const draggingBall = ref(false)
const movedBall = ref(false)
const ballOffset = ref({ x: 0, y: 0 })

const idleFrames = ['/assistant/anime_part_1.png', '/assistant/anime_part_2.png']
const talkFrames = ['/assistant/anime_part_3.png', '/assistant/anime_part_4.png']
const thinkFrame = '/assistant/anime_part_2.png'

const avatarState = ref('idle') // idle/talk/think
const avatarMain = ref(idleFrames[0])
const avatarOverlay = ref(idleFrames[1])
const overlayVisible = ref(false)
const overlayReady = ref(false)

let idleTimer = null
let talkTimer = null
let talkResetTimer = null
let overlayTimer = null

const scrollToBottom = () => {
  const el = document.querySelector('.ai-body')
  if (el) el.scrollTop = el.scrollHeight
}

const clearTimers = () => {
  if (idleTimer) clearTimeout(idleTimer)
  if (talkTimer) clearInterval(talkTimer)
  if (talkResetTimer) clearTimeout(talkResetTimer)
  if (overlayTimer) clearTimeout(overlayTimer)
  idleTimer = null
  talkTimer = null
  talkResetTimer = null
  overlayTimer = null
}

const smoothSwap = (src) => {
  if (!src || src === avatarMain.value) return
  avatarOverlay.value = src
  overlayReady.value = false
  overlayVisible.value = false
  requestAnimationFrame(() => {
    overlayReady.value = true
    requestAnimationFrame(() => {
      overlayVisible.value = true
    })
  })
  if (overlayTimer) clearTimeout(overlayTimer)
  overlayTimer = setTimeout(() => {
    avatarMain.value = src
    overlayVisible.value = false
    overlayReady.value = false
  }, 240)
}

const runIdleAnim = () => {
  if (avatarState.value !== 'idle') return
  const next = avatarMain.value === idleFrames[0] ? idleFrames[1] : idleFrames[0]
  smoothSwap(next)
  const gap = 2400 + Math.round(Math.random() * 1600)
  idleTimer = setTimeout(runIdleAnim, gap)
}

const runTalkAnim = (duration = 1900) => {
  avatarState.value = 'talk'
  if (idleTimer) clearTimeout(idleTimer)
  let idx = 0
  if (talkTimer) clearInterval(talkTimer)
  talkTimer = setInterval(() => {
    if (avatarState.value !== 'talk') return
    smoothSwap(talkFrames[idx % talkFrames.length])
    idx++
  }, 460)
  if (talkResetTimer) clearTimeout(talkResetTimer)
  talkResetTimer = setTimeout(() => {
    avatarState.value = 'idle'
    if (talkTimer) clearInterval(talkTimer)
    smoothSwap(idleFrames[0])
    runIdleAnim()
  }, duration)
}

const setThinking = () => {
  avatarState.value = 'think'
  if (idleTimer) clearTimeout(idleTimer)
  if (talkTimer) clearInterval(talkTimer)
  smoothSwap(thinkFrame)
}

const placePanelNearBall = () => {
  const gap = 16
  const xRight = ball.value.x + 92
  const xLeft = ball.value.x - panelSize.value.w - gap
  const useRight = xRight + panelSize.value.w <= viewport.value.w - 12
  const x = useRight ? xRight : Math.max(12, xLeft)
  const y = Math.min(Math.max(12, ball.value.y - 18), Math.max(12, viewport.value.h - panelSize.value.h - 12))
  pos.value = { x, y }
}

const toggle = () => {
  if (movedBall.value) return
  open.value = !open.value
  if (open.value) {
    placePanelNearBall()
    nextTick(() => scrollToBottom())
  }
}

const onPanelMouseDown = (e) => {
  draggingPanel.value = true
  e.preventDefault()
  const panel = e.currentTarget.closest('.ai-panel')
  if (!panel) return
  const rect = panel.getBoundingClientRect()
  panelOffset.value = { x: e.clientX - rect.left, y: e.clientY - rect.top }
  document.addEventListener('pointermove', onPanelMouseMove)
  document.addEventListener('pointerup', onPanelMouseUp)
}

const onPanelMouseMove = (e) => {
  if (!draggingPanel.value) return
  const maxX = viewport.value.w - panelSize.value.w - 12
  const maxY = viewport.value.h - panelSize.value.h - 12
  let x = e.clientX - panelOffset.value.x
  let y = e.clientY - panelOffset.value.y
  x = Math.max(12, Math.min(maxX, x))
  y = Math.max(12, Math.min(maxY, y))
  pos.value = { x, y }
}

const onPanelMouseUp = () => {
  draggingPanel.value = false
  document.removeEventListener('pointermove', onPanelMouseMove)
  document.removeEventListener('pointerup', onPanelMouseUp)
}

const onResizeDown = (e) => {
  resizing.value = true
  e.preventDefault()
  resizeStart.value = { x: e.clientX, y: e.clientY, w: panelSize.value.w, h: panelSize.value.h }
  document.addEventListener('pointermove', onResizing)
  document.addEventListener('pointerup', onResizeUp)
}

const onResizing = (e) => {
  if (!resizing.value) return
  const dx = e.clientX - resizeStart.value.x
  const dy = e.clientY - resizeStart.value.y
  const w = Math.max(320, Math.min(640, resizeStart.value.w + dx))
  const h = Math.max(320, Math.min(760, resizeStart.value.h + dy))
  panelSize.value = { w, h }
}

const onResizeUp = () => {
  resizing.value = false
  document.removeEventListener('pointermove', onResizing)
  document.removeEventListener('pointerup', onResizeUp)
}

const onBallDown = (e) => {
  draggingBall.value = true
  movedBall.value = false
  e.preventDefault()
  ballOffset.value = { x: e.clientX - ball.value.x, y: e.clientY - ball.value.y }
  document.addEventListener('pointermove', onBallMove)
  document.addEventListener('pointerup', onBallUp)
}

const onBallMove = (e) => {
  if (!draggingBall.value) return
  const size = 84
  const maxX = viewport.value.w - size - 12
  const maxY = viewport.value.h - size - 12
  let x = e.clientX - ballOffset.value.x
  let y = e.clientY - ballOffset.value.y
  x = Math.max(12, Math.min(maxX, x))
  y = Math.max(12, Math.min(maxY, y))
  if (Math.abs(x - ball.value.x) > 2 || Math.abs(y - ball.value.y) > 2) movedBall.value = true
  ball.value = { x, y }
  if (open.value) placePanelNearBall()
}

const onBallUp = () => {
  draggingBall.value = false
  document.removeEventListener('pointermove', onBallMove)
  document.removeEventListener('pointerup', onBallUp)
  setTimeout(() => {
    movedBall.value = false
  }, 80)
}

const onResize = () => {
  viewport.value = { w: window.innerWidth, h: window.innerHeight }
  if (ball.value.x === 0 && ball.value.y === 0) {
    ball.value = { x: viewport.value.w - 116, y: viewport.value.h - 170 }
  }
}

const send = async () => {
  const content = input.value.trim()
  if (!content || loading.value) return
  messages.value.push({ role: 'user', content })
  input.value = ''
  loading.value = true
  setThinking()
  await nextTick()
  scrollToBottom()
  try {
    const res = await http.post('/api/assistant/chat', {
      message: content,
      history: messages.value.slice(-8).map((m) => ({ role: m.role, content: m.content }))
    })
    messages.value.push({ role: 'assistant', content: res.data?.content || '暂时没有回复内容。' })
    runTalkAnim(2200)
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，当前助手连接失败，请稍后再试。' })
    avatarState.value = 'idle'
    smoothSwap(idleFrames[0])
    runIdleAnim()
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

onMounted(() => {
  onResize()
  runIdleAnim()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  clearTimers()
  document.removeEventListener('pointermove', onPanelMouseMove)
  document.removeEventListener('pointerup', onPanelMouseUp)
  document.removeEventListener('pointermove', onBallMove)
  document.removeEventListener('pointerup', onBallUp)
  document.removeEventListener('pointermove', onResizing)
  document.removeEventListener('pointerup', onResizeUp)
  window.removeEventListener('resize', onResize)
})
</script>

<template>
  <div class="ai-root">
    <button
      class="ai-avatar-btn"
      :style="{ left: `${ball.x}px`, top: `${ball.y}px` }"
      @pointerdown.prevent="onBallDown"
      @click="toggle"
      :aria-label="open ? '关闭助手' : '打开助手'"
    >
      <div class="avatar-aura"></div>
      <img class="avatar-img main" :src="avatarMain" alt="assistant-avatar" />
      <img
        v-if="overlayReady"
        class="avatar-img overlay"
        :class="{ visible: overlayVisible }"
        :src="avatarOverlay"
        alt="assistant-avatar-overlay"
      />
      <div class="avatar-chip">{{ loading ? '思考中' : 'AI' }}</div>
    </button>

    <div
      v-if="open"
      class="ai-panel"
      :style="{
        left: `${pos.x}px`,
        top: `${pos.y}px`,
        width: `${panelSize.w}px`,
        height: `${panelSize.h}px`
      }"
    >
      <div class="ai-header" @pointerdown.prevent="onPanelMouseDown">
        <img class="header-avatar" :src="avatarMain" alt="assistant-mini" />
        <div class="header-meta">
          <div class="ai-title">二次元编程助手</div>
          <div class="ai-sub">{{ loading ? '正在思考你的问题...' : '课程答疑 · 代码思路 · 题目讲解' }}</div>
        </div>
        <button class="ai-close" @click="toggle">×</button>
      </div>

      <div class="ai-body">
        <div v-for="(m, idx) in messages" :key="idx" class="ai-msg" :class="m.role">
          <div class="ai-bubble">{{ m.content }}</div>
        </div>
        <div v-if="loading" class="ai-msg assistant">
          <div class="ai-bubble">我在整理思路，请稍等...</div>
        </div>
      </div>

      <div class="ai-input">
        <input
          v-model="input"
          placeholder="比如：这题为什么选 C？"
          @keyup.enter="send"
        />
        <button class="ai-send" @click="send">发送</button>
      </div>

      <div class="ai-resize-handle" @pointerdown.prevent="onResizeDown"></div>
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

.ai-avatar-btn {
  position: fixed;
  width: 90px;
  height: 90px;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 0;
  animation: floatY 4.2s ease-in-out infinite;
  touch-action: none;
}

.avatar-aura {
  position: absolute;
  inset: -10px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(70, 195, 255, 0.35), rgba(86, 255, 213, 0.1), transparent 72%);
  filter: blur(3px);
  animation: pulseAura 3.2s ease-in-out infinite;
}

.avatar-img {
  position: absolute;
  inset: 0;
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: 50%;
  border: 2px solid rgba(135, 226, 255, 0.85);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.35);
}

.avatar-img.overlay {
  opacity: 0;
  transition: opacity 220ms ease;
}

.avatar-img.overlay.visible {
  opacity: 1;
}

.avatar-chip {
  position: absolute;
  right: -6px;
  bottom: -4px;
  font-size: 11px;
  border-radius: 999px;
  padding: 3px 8px;
  color: #06111a;
  background: linear-gradient(120deg, #89e7ff, #67ffc2);
  border: 1px solid rgba(255, 255, 255, 0.45);
}

.ai-panel {
  position: fixed;
  display: flex;
  flex-direction: column;
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid var(--ui-border);
  background:
    radial-gradient(circle at 12% -8%, rgba(87, 205, 255, 0.16), transparent 40%),
    radial-gradient(circle at 100% 0%, rgba(100, 255, 214, 0.12), transparent 42%),
    var(--ui-surface);
  box-shadow: 0 26px 58px rgba(0, 0, 0, 0.45);
}

.ai-header {
  display: grid;
  grid-template-columns: 42px 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--ui-border);
  background: linear-gradient(120deg, rgba(124, 225, 255, 0.16), rgba(88, 255, 204, 0.12));
  cursor: move;
  user-select: none;
  touch-action: none;
}

.header-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(255, 255, 255, 0.38);
}

.ai-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--ui-text);
}

.ai-sub {
  margin-top: 2px;
  font-size: 11px;
  color: var(--ui-text-muted);
}

.ai-close {
  border: none;
  background: transparent;
  color: var(--ui-text);
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  padding: 2px 4px;
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
  max-width: min(75%, 520px);
  padding: 8px 10px;
  border-radius: 12px;
  border: 1px solid var(--ui-border-soft);
  background: var(--ui-surface-soft);
  font-size: 12px;
  line-height: 1.55;
  white-space: pre-wrap;
}

.ai-msg.user .ai-bubble {
  background: rgba(118, 216, 255, 0.2);
  border-color: rgba(118, 216, 255, 0.32);
}

.ai-input {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  border-top: 1px solid var(--ui-border);
  padding: 10px;
  background: var(--ui-surface-soft);
}

.ai-input input {
  width: 100%;
  border-radius: 10px;
  border: 1px solid var(--ui-border-soft);
  background: var(--ui-surface);
  color: var(--ui-text);
  font-size: 12px;
  padding: 9px 10px;
  outline: none;
}

.ai-send {
  border: none;
  border-radius: 10px;
  padding: 0 14px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  color: #08131b;
  background: linear-gradient(120deg, #7ee2ff, #74ffcb);
}

.ai-resize-handle {
  position: absolute;
  right: 6px;
  bottom: 6px;
  width: 14px;
  height: 14px;
  border-right: 2px solid rgba(120, 224, 255, 0.7);
  border-bottom: 2px solid rgba(120, 224, 255, 0.7);
  cursor: se-resize;
  touch-action: none;
}

@keyframes floatY {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

@keyframes pulseAura {
  0%, 100% { opacity: 0.45; transform: scale(1); }
  50% { opacity: 0.9; transform: scale(1.04); }
}
</style>
