<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { getMe, sendCode } from '../api/auth'
import { changePassword, updateProfile } from '../api/user'
import { ElNotification } from 'element-plus'

const profile = ref({
  username: '',
  email: '',
  avatar: '',
  phone: ''
})
const errors = ref({
  username: '',
  avatar: '',
  phone: ''
})
const touched = ref({
  username: false,
  avatar: false,
  phone: false
})

const loading = ref(false)
const saving = ref(false)
const passwordLoading = ref(false)
const codeSending = ref(false)
const countdown = ref(0)
let timer = null

const pwd = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  code: ''
})

const load = async () => {
  loading.value = true
  try {
    const res = await getMe()
    profile.value = {
      username: res.data?.username || '',
      email: res.data?.email || '',
      avatar: res.data?.avatar || '',
      phone: res.data?.phone || ''
    }
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '个人信息加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const saveProfile = async () => {
  validateField('username')
  validateField('avatar')
  validateField('phone')
  if (errors.value.username || errors.value.avatar || errors.value.phone) {
    ElNotification({
      title: '请检查输入',
      message: '资料格式不正确',
      type: 'warning',
      duration: 1600
    })
    return
  }
  saving.value = true
  try {
    const res = await updateProfile({
      username: profile.value.username,
      avatar: profile.value.avatar,
      phone: profile.value.phone
    })
    profile.value = { ...profile.value, ...res.data }
    window.dispatchEvent(new CustomEvent('profile-updated', { detail: res.data }))
    ElNotification({
      title: '保存成功',
      message: '个人资料已更新',
      type: 'success',
      duration: 1600
    })
  } catch (e) {
    ElNotification({
      title: '保存失败',
      message: e?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  } finally {
    saving.value = false
  }
}

const updatePwd = async () => {
  if (!pwd.value.oldPassword || !pwd.value.newPassword) {
    ElNotification({
      title: '请输入完整信息',
      message: '原密码和新密码不能为空',
      type: 'warning',
      duration: 1600
    })
    return
  }
  if (!pwd.value.code) {
    ElNotification({
      title: '请输入验证码',
      message: '邮箱验证码不能为空',
      type: 'warning',
      duration: 1600
    })
    return
  }
  if (pwd.value.newPassword.length < 6 || pwd.value.newPassword.length > 32) {
    ElNotification({
      title: '密码长度不合规',
      message: '新密码长度为6-32位',
      type: 'warning',
      duration: 1600
    })
    return
  }
  if (pwd.value.newPassword !== pwd.value.confirmPassword) {
    ElNotification({
      title: '两次密码不一致',
      message: '请确认新密码一致',
      type: 'warning',
      duration: 1600
    })
    return
  }
  passwordLoading.value = true
  try {
    await changePassword({
      oldPassword: pwd.value.oldPassword,
      newPassword: pwd.value.newPassword,
      code: pwd.value.code
    })
    pwd.value = { oldPassword: '', newPassword: '', confirmPassword: '', code: '' }
    ElNotification({
      title: '修改成功',
      message: '密码已更新，请妥善保管',
      type: 'success',
      duration: 1800
    })
  } catch (e) {
    ElNotification({
      title: '修改失败',
      message: e?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  } finally {
    passwordLoading.value = false
  }
}

const sendEmailCode = async () => {
  if (!profile.value.email) {
    ElNotification({
      title: '邮箱缺失',
      message: '请先登录获取邮箱',
      type: 'warning',
      duration: 1600
    })
    return
  }
  if (countdown.value > 0 || codeSending.value) return
  codeSending.value = true
  try {
    await sendCode({ email: profile.value.email })
    ElNotification({
      title: '验证码已发送',
      message: '请查收邮箱',
      type: 'success',
      duration: 1600
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
    ElNotification({
      title: '发送失败',
      message: e?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  } finally {
    codeSending.value = false
  }
}

const validateField = (field) => {
  if (field === 'username') {
    if (!profile.value.username) {
      errors.value.username = '昵称不能为空'
    } else if (profile.value.username.length < 2 || profile.value.username.length > 20) {
      errors.value.username = '昵称长度 2-20 位'
    } else {
      errors.value.username = ''
    }
  }
  if (field === 'avatar') {
    if (!profile.value.avatar) {
      errors.value.avatar = ''
      return
    }
    const ok = /^https?:\/\/.+/i.test(profile.value.avatar)
    errors.value.avatar = ok ? '' : '头像链接需以 http/https 开头'
  }
  if (field === 'phone') {
    if (!profile.value.phone) {
      errors.value.phone = ''
      return
    }
    const ok = /^1\d{10}$/.test(profile.value.phone)
    errors.value.phone = ok ? '' : '手机号格式不正确'
  }
}

const onBlur = (field) => {
  touched.value[field] = true
  validateField(field)
}

onMounted(load)

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<template>
  <div class="profile-page">
    <div class="hero">
      <div>
        <div class="title display">用户中心</div>
        <div class="subtitle">维护你的学习身份与安全设置</div>
      </div>
      <div class="avatar-card">
        <el-avatar :src="profile.avatar" :size="64">
          {{ profile.username?.slice(0, 1) || '同' }}
        </el-avatar>
        <div>
          <div class="avatar-name">{{ profile.username || '同学' }}</div>
          <div class="avatar-email">{{ profile.email }}</div>
        </div>
      </div>
    </div>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-panel"></div>
      </template>
      <template #default>
        <div class="grid">
          <div class="panel">
            <div class="panel-title">基本资料</div>
            <div class="panel-sub">更新昵称、头像、手机号</div>
            <!-- TODO: 后续支持头像上传（MinIO），替换当前“头像链接”手填方式 -->

            <div class="form">
              <label class="field">
                <span>昵称</span>
                <input v-model="profile.username" placeholder="你的昵称" @blur="onBlur('username')" />
                <em v-if="touched.username && errors.username" class="field-tip">{{ errors.username }}</em>
              </label>
              <label class="field">
                <span>头像链接</span>
                <input v-model="profile.avatar" placeholder="https://..." @blur="onBlur('avatar')" />
                <em v-if="touched.avatar && errors.avatar" class="field-tip">{{ errors.avatar }}</em>
              </label>
              <label class="field">
                <span>手机号</span>
                <input v-model="profile.phone" placeholder="选填" @blur="onBlur('phone')" />
                <em v-if="touched.phone && errors.phone" class="field-tip">{{ errors.phone }}</em>
              </label>
              <label class="field disabled">
                <span>邮箱</span>
                <input v-model="profile.email" disabled />
              </label>
            </div>
            <el-button type="primary" class="primary-btn" :loading="saving" @click="saveProfile">
              保存资料
            </el-button>
          </div>

          <div class="panel">
            <div class="panel-title">安全设置</div>
            <div class="panel-sub">修改密码保障账号安全</div>

            <div class="form">
              <label class="field">
                <span>原密码</span>
                <input v-model="pwd.oldPassword" type="password" placeholder="请输入原密码" />
              </label>
              <label class="field">
                <span>新密码</span>
                <input v-model="pwd.newPassword" type="password" placeholder="6-32位" />
              </label>
              <label class="field">
                <span>确认新密码</span>
                <input v-model="pwd.confirmPassword" type="password" placeholder="请再次输入" />
              </label>
              <label class="field">
                <span>邮箱验证码</span>
                <div class="code-row">
                  <input v-model="pwd.code" placeholder="请输入验证码" />
                  <button
                    class="code-btn"
                    type="button"
                    :disabled="countdown > 0 || codeSending"
                    @click="sendEmailCode"
                  >
                    {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
                  </button>
                </div>
              </label>
            </div>
            <el-button class="primary-btn ghost" :loading="passwordLoading" @click="updatePwd">
              更新密码
            </el-button>
          </div>
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.profile-page {
  display: grid;
  gap: 18px;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
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

.avatar-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 14px;
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
}

.avatar-name {
  font-weight: 600;
}

.avatar-email {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
}

.panel {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 16px;
  padding: 18px;
  display: grid;
  gap: 12px;
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
}

.panel-sub {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.form {
  display: grid;
  gap: 12px;
  margin-top: 8px;
}

.field {
  display: grid;
  gap: 8px;
  font-size: 12px;
  color: var(--ui-text);
}

.field input {
  border-radius: 12px;
  border: 1px solid var(--ui-border-soft);
  padding: 10px 12px;
  background: var(--ui-surface-soft);
  color: var(--ui-text);
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.field input::placeholder {
  color: var(--ui-text-muted);
}

.field-tip {
  font-size: 12px;
  color: #ff9b9b;
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
}

.code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.field input:focus {
  border-color: var(--ui-accent);
  box-shadow: 0 0 0 3px rgba(86, 255, 213, 0.12);
}

.field.disabled input {
  opacity: 0.7;
  cursor: not-allowed;
}

.primary-btn {
  margin-top: 6px;
  border-radius: 12px;
  background: linear-gradient(120deg, var(--ui-accent), var(--ui-accent-2));
  color: #0b101a;
  border: none;
}

.primary-btn.ghost {
  background: transparent;
  color: var(--ui-text);
  border: 1px solid var(--ui-border);
}

.skeleton-panel {
  height: 320px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.08);
}
</style>
