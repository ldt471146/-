<script setup>
import { ref, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { login, register, sendCode, getMe } from '../api/auth'
import { setToken, getRemember } from '../utils/auth'
import { getRoleHome, setStoredUser } from '../utils/session'
import { ElNotification } from 'element-plus'

const router = useRouter()

const mode = ref('login')
const theme = ref(localStorage.getItem('theme') || 'neon')
if (!['neon', 'red', 'aurora'].includes(theme.value)) {
  theme.value = 'neon'
  localStorage.setItem('theme', theme.value)
}
const remember = ref(getRemember())
const email = ref('')
const username = ref('')
const parentPhone = ref('')
const password = ref('')
const confirmPassword = ref('')
const code = ref('')
const registerRole = ref('STUDENT')
const loading = ref(false)
const errorMsg = ref('')
const fieldErrors = ref({
  email: '',
  username: '',
  parentPhone: '',
  password: '',
  confirmPassword: '',
  code: ''
})
const touched = ref({
  email: false,
  username: false,
  parentPhone: false,
  password: false,
  confirmPassword: false,
  code: false
})
const codeSending = ref(false)
const countdown = ref(0)
let timer = null

watch(mode, () => {
  errorMsg.value = ''
  fieldErrors.value = { email: '', username: '', parentPhone: '', password: '', confirmPassword: '', code: '' }
  touched.value = {
    email: false,
    username: false,
    parentPhone: false,
    password: false,
    confirmPassword: false,
    code: false
  }
  code.value = ''
  confirmPassword.value = ''
  parentPhone.value = ''
  registerRole.value = 'STUDENT'
})

document.documentElement.setAttribute('data-theme', theme.value)

const toggleTheme = () => {
  const order = ['neon', 'red', 'aurora']
  const idx = order.indexOf(theme.value)
  theme.value = order[(idx + 1) % order.length]
  localStorage.setItem('theme', theme.value)
  document.documentElement.setAttribute('data-theme', theme.value)
}

const handleLogin = async () => {
  errorMsg.value = ''
  fieldErrors.value = { email: '', username: '', parentPhone: '', password: '', confirmPassword: '', code: '' }
  if (!email.value || !password.value) {
    errorMsg.value = '请输入邮箱和密码'
    return
  }
  loading.value = true
  try {
    const { data } = await login({ email: email.value, password: password.value, remember: remember.value })
    setToken(data.token, remember.value)
    const me = await getMe()
    setStoredUser(me.data, remember.value)
    errorMsg.value = `欢迎回来，${me.data.username}`
    ElNotification({
      title: '登录成功',
      message: remember.value ? '已启用记住我，下次无需重复登录' : '欢迎回来，继续探索',
      type: 'success',
      duration: 2000
    })
    router.push(getRoleHome(me.data))
  } catch (e) {
    errorMsg.value = e?.message || '登录失败'
  } finally {
    loading.value = false
  }
}

const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const phoneReg = /^[0-9]{6,20}$/

const validateField = (field) => {
  if (field === 'email') {
    if (!email.value) {
      fieldErrors.value.email = '邮箱不能为空'
    } else if (!emailReg.test(email.value)) {
      fieldErrors.value.email = '邮箱格式不正确'
    } else {
      fieldErrors.value.email = ''
    }
  }
  if (field === 'username') {
    fieldErrors.value.username = username.value ? '' : '用户名不能为空'
  }
  if (field === 'parentPhone') {
    if (registerRole.value !== 'STUDENT') {
      fieldErrors.value.parentPhone = ''
    } else if (!parentPhone.value) {
      fieldErrors.value.parentPhone = '家长手机号不能为空'
    } else if (!phoneReg.test(parentPhone.value)) {
      fieldErrors.value.parentPhone = '家长手机号格式不正确'
    } else {
      fieldErrors.value.parentPhone = ''
    }
  }
  if (field === 'password') {
    if (!password.value) {
      fieldErrors.value.password = '密码不能为空'
    } else if (password.value.length < 6 || password.value.length > 32) {
      fieldErrors.value.password = '密码长度为6-32位'
    } else {
      fieldErrors.value.password = ''
    }
  }
  if (field === 'confirmPassword') {
    if (!confirmPassword.value) {
      fieldErrors.value.confirmPassword = '确认密码不能为空'
    } else if (password.value !== confirmPassword.value) {
      fieldErrors.value.confirmPassword = '两次密码不一致'
    } else {
      fieldErrors.value.confirmPassword = ''
    }
  }
  if (field === 'code') {
    fieldErrors.value.code = code.value ? '' : '验证码不能为空'
  }
}

const onBlur = (field) => {
  touched.value[field] = true
  validateField(field)
}

const handleRegister = async () => {
  errorMsg.value = ''
  fieldErrors.value = { email: '', username: '', parentPhone: '', password: '', confirmPassword: '', code: '' }
  let hasError = false
  if (!email.value) {
    fieldErrors.value.email = '邮箱不能为空'
    hasError = true
  }
  if (!username.value) {
    fieldErrors.value.username = '用户名不能为空'
    hasError = true
  }
  if (registerRole.value === 'STUDENT') {
    if (!parentPhone.value) {
      fieldErrors.value.parentPhone = '家长手机号不能为空'
      hasError = true
    } else if (!phoneReg.test(parentPhone.value)) {
      fieldErrors.value.parentPhone = '家长手机号格式不正确'
      hasError = true
    }
  }
  if (!password.value) {
    fieldErrors.value.password = '密码不能为空'
    hasError = true
  } else if (password.value.length < 6 || password.value.length > 32) {
    fieldErrors.value.password = '密码长度为6-32位'
    hasError = true
  }
  if (!confirmPassword.value) {
    fieldErrors.value.confirmPassword = '确认密码不能为空'
    hasError = true
  } else if (password.value !== confirmPassword.value) {
    fieldErrors.value.confirmPassword = '两次密码不一致'
    hasError = true
  }
  if (!code.value) {
    fieldErrors.value.code = '验证码不能为空'
    hasError = true
  }
  if (hasError) {
    errorMsg.value = '请完善注册信息'
    return
  }
  loading.value = true
  try {
    const { data } = await register({
      email: email.value,
      username: username.value,
      parentPhone: registerRole.value === 'STUDENT' ? parentPhone.value : '',
      password: password.value,
      code: code.value,
      roleCode: registerRole.value
    })
    setToken(data.token, false)
    const me = await getMe()
    setStoredUser(me.data, false)
    errorMsg.value = `注册成功，欢迎 ${me.data.username}`
    ElNotification({
      title: '注册成功',
      message: '已自动登录并启用记住我',
      type: 'success',
      duration: 2000
    })
    router.push(getRoleHome(me.data))
  } catch (e) {
    errorMsg.value = e?.message || '注册失败'
  } finally {
    loading.value = false
  }
}

const handleSendCode = async () => {
  errorMsg.value = ''
  fieldErrors.value.code = ''
  if (!email.value) {
    fieldErrors.value.email = '请先输入邮箱'
    errorMsg.value = '请先输入邮箱'
    return
  }
  if (countdown.value > 0 || codeSending.value) {
    return
  }
  codeSending.value = true
  try {
    await sendCode({ email: email.value })
    ElNotification({
      title: '验证码已发送',
      message: '请查收邮箱中的验证码',
      type: 'success',
      duration: 2000
    })
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (e) {
    errorMsg.value = e?.message || '发送验证码失败'
  } finally {
    codeSending.value = false
  }
}

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<template>
  <div class="page">
    <div class="gridline"></div>
    <div class="glow-ring ring-a"></div>
    <div class="glow-ring ring-b"></div>
    <div class="scan"></div>

    <main class="shell">
      <section class="hero">
        <div class="brand">
          <div class="logo">NEON</div>
          <div class="brand-text">
            <div class="brand-title">NEON LAB</div>
            <div class="brand-sub">青少年编程中枢</div>
          </div>
        </div>
        <h1 class="display">
          数字时代的
          <span>创造力训练场</span>
        </h1>
        <p class="desc">
          用可视化学习路径 + 即时反馈，帮助青少年建立计算思维与自信。
        </p>
        <div class="metrics">
          <div class="metric">
            <div class="metric-value">99.9%</div>
            <div class="metric-label">稳定性</div>
          </div>
          <div class="metric">
            <div class="metric-value">24/7</div>
            <div class="metric-label">学习支持</div>
          </div>
          <div class="metric">
            <div class="metric-value">AI+</div>
            <div class="metric-label">智能辅导</div>
          </div>
        </div>
      </section>

      <section class="card">
        <div class="theme-toggle">
          <button class="theme-pill" @click="toggleTheme">
            {{ theme === 'neon' ? '赛博红' : theme === 'red' ? '极光' : '霓虹' }}
          </button>
        </div>
        <div class="card-top">
          <div class="chip">TECH ACCESS</div>
          <div class="status">
            <span class="status-dot"></span>
            online
          </div>
        </div>

        <div class="tabs">
          <button class="tab" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
          <button class="tab" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
        </div>

        <h2 class="display">{{ mode === 'login' ? '邮箱登录' : '创建新账号' }}</h2>
        <p class="sub">进入科技训练场，保持专注与节奏</p>

        <form class="form" @submit.prevent="mode === 'login' ? handleLogin() : handleRegister()">
          <label class="field">
            <span>邮箱</span>
            <input v-model.trim="email" type="email" placeholder="name@example.com" @blur="onBlur('email')" />
            <em v-if="touched.email && fieldErrors.email" class="field-tip">
              {{ fieldErrors.email }}
            </em>
          </label>
          <label v-if="mode === 'register'" class="field">
            <span>用户名</span>
            <input v-model.trim="username" type="text" placeholder="给自己起个昵称" @blur="onBlur('username')" />
            <em v-if="touched.username && fieldErrors.username" class="field-tip">{{ fieldErrors.username }}</em>
          </label>
          <label v-if="mode === 'register'" class="field">
            <span>家长手机号（学生必填）</span>
            <input
              v-model.trim="parentPhone"
              type="text"
              placeholder="请输入家长手机号"
              @blur="onBlur('parentPhone')"
            />
            <em v-if="touched.parentPhone && fieldErrors.parentPhone" class="field-tip">
              {{ fieldErrors.parentPhone }}
            </em>
          </label>
          <label class="field">
            <span>密码</span>
            <input v-model.trim="password" type="password" placeholder="请输入密码" @blur="onBlur('password')" />
            <em v-if="touched.password && fieldErrors.password" class="field-tip">
              {{ fieldErrors.password }}
            </em>
          </label>
          <label v-if="mode === 'login'" class="remember">
            <input v-model="remember" type="checkbox" />
            <span>记住我</span>
          </label>
          <label v-if="mode === 'register'" class="field">
            <span>确认密码</span>
            <input
              v-model.trim="confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              @blur="onBlur('confirmPassword')"
            />
            <em v-if="touched.confirmPassword && fieldErrors.confirmPassword" class="field-tip">
              {{ fieldErrors.confirmPassword }}
            </em>
          </label>
          <label v-if="mode === 'register'" class="field">
            <span>验证码</span>
            <div class="code-row">
              <input v-model.trim="code" type="text" placeholder="请输入验证码" @blur="onBlur('code')" />
              <button
                class="code-btn"
                type="button"
                :disabled="countdown > 0 || codeSending"
                @click="handleSendCode"
              >
                <span class="code-icon">⚡</span>
                {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
              </button>
            </div>
            <em v-if="touched.code && fieldErrors.code" class="field-tip">{{ fieldErrors.code }}</em>
          </label>
          <div v-if="mode === 'register'" class="role-select">
            <div class="role-title">注册身份</div>
            <label class="role-item">
              <input v-model="registerRole" type="radio" value="STUDENT" />
              <span>学生</span>
            </label>
            <label class="role-item">
              <input v-model="registerRole" type="radio" value="TEACHER" />
              <span>教师（需审核）</span>
            </label>
          </div>

          <button class="btn" type="submit" :disabled="loading">
            <span v-if="!loading">{{ mode === 'login' ? '进入控制台' : '创建我的学习账号' }}</span>
            <span v-else>验证中...</span>
          </button>
          <div class="hint" :class="{ error: errorMsg && errorMsg !== '登录成功' }">
            {{ errorMsg || (mode === 'register' ? '首次注册默认学生身份' : '欢迎回来，继续探索') }}
          </div>
        </form>

        <div class="divider"></div>

        <div class="roles">
          <div class="role">
            <div class="dot"></div>
            <div>
              <div class="role-title">学生</div>
              <div class="role-desc">项目式学习 + 题库练习</div>
            </div>
          </div>
          <div class="role">
            <div class="dot"></div>
            <div>
              <div class="role-title">家长</div>
              <div class="role-desc">成长报告与学习提醒</div>
            </div>
          </div>
          <div class="role">
            <div class="dot"></div>
            <div>
              <div class="role-title">教师</div>
              <div class="role-desc">班级管理与测评分析</div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.page {
  position: relative;
  min-height: 100vh;
  display: grid;
  place-items: center;
  overflow: hidden;
  color: var(--ui-text);
  background:
    radial-gradient(1200px 640px at 12% 8%, rgba(0, 210, 255, 0.16), transparent 70%),
    radial-gradient(900px 520px at 85% 12%, rgba(86, 255, 213, 0.12), transparent 65%),
    radial-gradient(700px 520px at 40% 90%, rgba(86, 120, 255, 0.12), transparent 70%),
    var(--ui-bg);
  animation: fadeIn 0.8s ease-out;
}

.gridline {
  position: absolute;
  inset: -20%;
  background-image:
    linear-gradient(transparent 94%, rgba(255, 255, 255, 0.03) 94%),
    linear-gradient(90deg, transparent 94%, rgba(255, 255, 255, 0.03) 94%);
  background-size: 64px 64px;
  transform: skewY(-7deg);
  opacity: 0.2;
  animation: gridMove 24s linear infinite;
}

.scan {
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, transparent 18%, rgba(86, 255, 213, 0.08), transparent 65%);
  opacity: 0.4;
  animation: scanMove 6.5s linear infinite;
  pointer-events: none;
}

.glow-ring {
  position: absolute;
  width: 460px;
  height: 460px;
  border-radius: 50%;
  filter: blur(70px);
  opacity: 0.55;
  animation: floatOrb 14s ease-in-out infinite;
}

.ring-a {
  background: #00d7ff;
  top: -140px;
  left: -100px;
  animation-delay: 0s;
}

.ring-b {
  background: #57ffcf;
  bottom: -160px;
  right: 8%;
  animation-delay: 2s;
}

.shell {
  position: relative;
  width: min(1100px, 92vw);
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 36px;
  z-index: 1;
  padding: 56px 0;
  animation: riseIn 0.9s ease-out;
}

.hero {
  padding: 24px 8px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.logo {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(86, 255, 213, 0.9), rgba(0, 210, 255, 0.9));
  color: #07101a;
  font-weight: 700;
  display: grid;
  place-items: center;
  letter-spacing: 0.08em;
}

.brand-title {
  font-size: 14px;
  letter-spacing: 0.16em;
  color: rgba(233, 241, 255, 0.75);
}

.brand-sub {
  font-size: 12px;
  color: rgba(233, 241, 255, 0.45);
  margin-top: 4px;
}

.display {
  font-family: var(--font-display);
  letter-spacing: 0.01em;
}

h1 {
  font-size: clamp(32px, 4vw, 54px);
  margin: 20px 0 8px;
  line-height: 1.1;
}

h1 span {
  display: block;
  color: var(--ui-accent);
  text-shadow: 0 0 24px rgba(86, 255, 213, 0.35);
}

.desc {
  color: var(--ui-text-muted);
  max-width: 460px;
  margin-bottom: 22px;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
  margin-top: 24px;
}

.metric {
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid var(--ui-border-soft);
  background: var(--ui-surface-soft);
  backdrop-filter: blur(6px);
}

.metric-value {
  font-family: var(--font-mono);
  color: var(--ui-accent);
  font-size: 18px;
}

.metric-label {
  font-size: 12px;
  color: var(--ui-text-muted);
  margin-top: 6px;
}

.card {
  position: relative;
  padding: 28px;
  border-radius: 22px;
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.45);
  overflow: hidden;
  backdrop-filter: blur(10px);
  animation: cardPulse 6s ease-in-out infinite;
}

.card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 22px;
  padding: 1px;
  background: linear-gradient(120deg, rgba(86, 255, 213, 0.5), rgba(0, 210, 255, 0.3), transparent);
  -webkit-mask:
    linear-gradient(#000 0 0) content-box,
    linear-gradient(#000 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.chip {
  background: rgba(86, 255, 213, 0.12);
  color: var(--ui-accent);
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--ui-accent);
  box-shadow: 0 0 12px rgba(86, 255, 213, 0.7);
  animation: pulse 2s ease-in-out infinite;
}

.tabs {
  display: inline-flex;
  gap: 8px;
  background: var(--ui-surface-soft);
  padding: 6px;
  border-radius: 999px;
  border: 1px solid var(--ui-border-soft);
  margin-bottom: 12px;
}

.tab {
  border: none;
  background: transparent;
  color: var(--ui-text);
  padding: 6px 14px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 12px;
}

.tab.active {
  background: rgba(86, 255, 213, 0.18);
  color: var(--ui-accent);
  box-shadow: 0 0 14px rgba(86, 255, 213, 0.2);
}

h2 {
  font-size: 26px;
  margin: 8px 0 4px;
}

.sub {
  color: var(--ui-text-muted);
  font-size: 14px;
  margin-bottom: 18px;
}

.form {
  display: grid;
  gap: 14px;
}

.field {
  display: grid;
  gap: 8px;
  font-size: 13px;
  color: var(--ui-text);
}

.field input {
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid var(--ui-border-soft);
  background: var(--ui-surface);
  color: var(--ui-text);
  outline: none;
}

.field-tip {
  font-size: 12px;
  color: #ff9b9b;
}

.remember {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.remember input {
  accent-color: var(--ui-accent);
}

.role-select {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 12px;
  color: var(--ui-text-muted);
}

.role-title {
  font-size: 12px;
  color: var(--ui-text);
  min-width: 60px;
}

.role-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.role-item input {
  accent-color: var(--ui-accent);
}

.code-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: center;
}

.code-btn {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--ui-border);
  background: rgba(86, 255, 213, 0.12);
  color: var(--ui-accent);
  cursor: pointer;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  position: relative;
  overflow: hidden;
}

.code-btn::after {
  content: '';
  position: absolute;
  inset: -2px;
  background: linear-gradient(120deg, rgba(86, 255, 213, 0.2), rgba(0, 210, 255, 0.2));
  opacity: 0;
  transition: opacity 0.2s ease;
}

.code-btn:hover::after {
  opacity: 1;
}

.code-icon {
  display: inline-flex;
  font-size: 13px;
  animation: boltPulse 1.6s ease-in-out infinite;
}

.code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.field input:focus {
  border-color: var(--ui-accent);
  box-shadow: 0 0 0 3px rgba(86, 255, 213, 0.15);
  transform: translateY(-1px);
}

.field input:focus-visible {
  outline: none;
}

.field input:not(:placeholder-shown):invalid {
  border-color: rgba(255, 123, 123, 0.65);
  box-shadow: 0 0 0 2px rgba(255, 123, 123, 0.12);
}

.field input:not(:placeholder-shown):valid {
  border-color: var(--ui-accent);
}

.btn {
  margin-top: 8px;
  padding: 12px 16px;
  border-radius: 14px;
  border: none;
  color: #0a0f1f;
  background: linear-gradient(120deg, var(--ui-accent), var(--ui-accent-2));
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(0, 210, 255, 0.35);
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.hint {
  font-size: 12px;
  color: var(--ui-text-muted);
  min-height: 18px;
}

.hint.error {
  color: #ff7b7b;
}

.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  margin: 18px 0;
}

.roles {
  display: grid;
  gap: 12px;
}

.role {
  display: flex;
  gap: 10px;
  align-items: center;
  font-size: 13px;
  color: var(--ui-text);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--ui-accent);
  box-shadow: 0 0 10px rgba(86, 255, 213, 0.8);
}

.role-title {
  font-weight: 600;
}

.role-desc {
  font-size: 12px;
  color: var(--ui-text-muted);
}

@keyframes gridMove {
  0% {
    transform: translateY(0) skewY(-7deg);
  }
  100% {
    transform: translateY(60px) skewY(-7deg);
  }
}

@keyframes scanMove {
  0% {
    transform: translateX(-120%);
  }
  100% {
    transform: translateX(120%);
  }
}

@keyframes floatOrb {
  0%,
  100% {
    transform: translateY(0) translateX(0);
  }
  50% {
    transform: translateY(-18px) translateX(10px);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes riseIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes cardPulse {
  0%,
  100% {
    box-shadow: 0 30px 80px rgba(0, 0, 0, 0.45);
  }
  50% {
    box-shadow: 0 34px 92px rgba(0, 210, 255, 0.25);
  }
}

@keyframes boltPulse {
  0%,
  100% {
    transform: translateY(0);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-2px);
    opacity: 1;
  }
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
  }
}

.theme-toggle {
  position: absolute;
  top: 18px;
  right: 18px;
  z-index: 2;
}

.theme-pill {
  border: 1px solid var(--ui-border);
  background: rgba(86, 255, 213, 0.08);
  color: var(--ui-text);
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  cursor: pointer;
}

</style>
