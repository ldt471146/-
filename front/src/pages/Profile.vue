<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { getMe, sendCode } from '../api/auth'
import { changePassword, updateProfile, uploadAvatar } from '../api/user'
import { ElNotification } from 'element-plus'

const MAX_AVATAR_SIZE = 2 * 1024 * 1024

const profile = ref({
  username: '',
  email: '',
  avatar: '',
  phone: ''
})
const errors = ref({
  username: '',
  phone: ''
})
const touched = ref({
  username: false,
  phone: false
})

const loading = ref(false)
const saving = ref(false)
const avatarUploading = ref(false)
const passwordLoading = ref(false)
const codeSending = ref(false)
const countdown = ref(0)
const fileInputRef = ref(null)
let timer = null

const pwd = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  code: ''
})

const emitProfileUpdate = (payload) => {
  window.dispatchEvent(new CustomEvent('profile-updated', { detail: payload }))
}

const applyProfile = (payload = {}) => {
  profile.value = {
    username: payload.username ?? profile.value.username,
    email: payload.email ?? profile.value.email,
    avatar: payload.avatar ?? profile.value.avatar,
    phone: payload.phone ?? profile.value.phone
  }
}

const load = async () => {
  loading.value = true
  try {
    const res = await getMe()
    applyProfile(res.data || {})
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

const saveProfile = async () => {
  validateField('username')
  validateField('phone')
  if (errors.value.username || errors.value.phone) {
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
    applyProfile(res.data || {})
    emitProfileUpdate(res.data || {})
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

const openAvatarPicker = () => {
  if (avatarUploading.value) return
  fileInputRef.value?.click()
}

const onAvatarFileChange = async (event) => {
  const file = event?.target?.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!file.type?.startsWith('image/')) {
    ElNotification({ title: '文件格式不支持', message: '请上传图片文件', type: 'warning', duration: 1800 })
    return
  }
  if (file.size > MAX_AVATAR_SIZE) {
    ElNotification({ title: '图片过大', message: '头像大小不能超过 2MB', type: 'warning', duration: 1800 })
    return
  }
  const formData = new FormData()
  formData.append('file', file)
  avatarUploading.value = true
  try {
    const res = await uploadAvatar(formData)
    applyProfile(res.data || {})
    emitProfileUpdate(res.data || {})
    ElNotification({ title: '上传成功', message: '头像已更新', type: 'success', duration: 1600 })
  } catch (e) {
    ElNotification({ title: '上传失败', message: e?.message || '请稍后再试', type: 'error', duration: 2000 })
  } finally {
    avatarUploading.value = false
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

            <div class="avatar-uploader">
              <el-avatar :src="profile.avatar" :size="88" class="avatar-preview">
                {{ profile.username?.slice(0, 1) || '同' }}
              </el-avatar>
              <div class="avatar-uploader__body">
                <div class="avatar-uploader__title">上传头像</div>
                <div class="avatar-uploader__hint">支持 JPG / PNG / WEBP，大小不超过 2MB，上传后自动保存。</div>
                <div class="avatar-uploader__actions">
                  <el-button type="primary" :loading="avatarUploading" @click="openAvatarPicker">选择图片</el-button>
                  <span v-if="profile.avatar" class="avatar-uploader__path">当前地址：{{ profile.avatar }}</span>
                </div>
                <input ref="fileInputRef" class="file-input" type="file" accept="image/*" @change="onAvatarFileChange" />
              </div>
            </div>

            <div class="form">
              <label class="field">
                <span>昵称</span>
                <input v-model="profile.username" placeholder="你的昵称" @blur="onBlur('username')" />
                <em v-if="touched.username && errors.username" class="field-tip">{{ errors.username }}</em>
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

.avatar-uploader {
  display: grid;
  grid-template-columns: 88px 1fr;
  gap: 16px;
  align-items: center;
  margin-top: 8px;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid var(--ui-border-soft);
  background: var(--ui-surface-soft);
}

.avatar-preview {
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.14);
}

.avatar-uploader__body {
  display: grid;
  gap: 8px;
}

.avatar-uploader__title {
  font-size: 15px;
  font-weight: 700;
  color: var(--ui-text);
}

.avatar-uploader__hint,
.avatar-uploader__path {
  font-size: 12px;
  color: var(--ui-text-muted);
  line-height: 1.7;
}

.avatar-uploader__actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.file-input {
  display: none;
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

@media (max-width: 768px) {
  .hero,
  .avatar-uploader {
    grid-template-columns: 1fr;
    display: grid;
  }
}
</style>
