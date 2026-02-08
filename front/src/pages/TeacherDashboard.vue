<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  createTeacherCourse,
  deleteTeacherCourse,
  fetchTeacherCourses,
  updateTeacherCourse
} from '../api/teacher'
import { ElNotification } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const courses = ref([])
const dialogOpen = ref(false)
const editingId = ref(null)
const form = ref({
  title: '',
  intro: '',
  cover: '',
  status: 1
})

const load = async () => {
  loading.value = true
  try {
    const res = await fetchTeacherCourses()
    courses.value = res.data || []
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '课程加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const normalizeUrl = (val) => {
  const raw = String(val || '').trim()
  if (!raw) return ''
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('//')) return `https:${raw}`
  return `https://${raw}`
}

const openCreate = () => {
  editingId.value = null
  form.value = { title: '', intro: '', cover: '', status: 1, finishStatus: 0 }
  dialogOpen.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  form.value = {
    title: row.title || '',
    intro: row.intro || '',
    cover: row.cover || '',
    status: row.status ?? 1,
    finishStatus: row.finishStatus ?? 0
  }
  dialogOpen.value = true
}

const saveCourse = async () => {
  if (saving.value) return
  if (!form.value.title) {
    ElNotification({
      title: '请填写标题',
      message: '课程标题不能为空',
      type: 'warning',
      duration: 1500
    })
    return
  }
  try {
    saving.value = true
    const payload = {
      ...form.value,
      cover: normalizeUrl(form.value.cover)
    }
    if (editingId.value) {
      await updateTeacherCourse(editingId.value, payload)
    } else {
      await createTeacherCourse(payload)
    }
    dialogOpen.value = false
    await load()
    ElNotification({
      title: '保存成功',
      message: '课程已更新',
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

const closeCourseDialog = () => {
  dialogOpen.value = false
}

const removeCourse = async (row) => {
  try {
    await deleteTeacherCourse(row.id)
    await load()
    ElNotification({
      title: '已删除',
      message: '课程已删除',
      type: 'success',
      duration: 1600
    })
  } catch (e) {
    ElNotification({
      title: '删除失败',
      message: e?.message || '请稍后再试',
      type: 'error',
      duration: 2000
    })
  }
}

const goDetail = (row) => {
  router.push(`/teacher/courses/${row.id}`)
}

const goQuestionBank = () => {
  router.push('/teacher/questions')
}

const goExams = () => {
  router.push('/teacher/exams')
}

const statusLabel = computed(() => (s) => (s === 1 ? '上架' : '下架'))

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="hero">
      <div class="title display">教师控制台</div>
      <div class="subtitle">课程与题库的创建、发布与管理</div>
    </div>

    <div class="toolbar">
      <el-button native-type="button" @click="goExams">考试任务</el-button>
      <el-button native-type="button" @click="goQuestionBank">题库管理</el-button>
      <el-button native-type="button" type="primary" @click="openCreate">新建课程</el-button>
    </div>

    <el-table :data="courses" v-loading="loading" class="table">
      <el-table-column label="课程标题" prop="title" min-width="220" />
      <el-table-column label="封面" min-width="220">
        <template #default="scope">
          <div class="cover-cell">
            <img
              v-if="normalizeUrl(scope.row.cover)"
              :src="normalizeUrl(scope.row.cover)"
              alt="cover"
              class="cover-cell-img"
            />
            <span v-else class="cover-empty">未设置</span>
            <a
              v-if="normalizeUrl(scope.row.cover)"
              :href="normalizeUrl(scope.row.cover)"
              target="_blank"
              rel="noreferrer"
              class="cover-link"
            >
              {{ normalizeUrl(scope.row.cover) }}
            </a>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="章节数" prop="chapterCount" width="90" />
      <el-table-column label="课时数" prop="lessonCount" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
            {{ statusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="进度" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.finishStatus === 1 ? 'success' : 'info'">
            {{ scope.row.finishStatus === 1 ? '完结' : '更新中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button native-type="button" size="small" @click="goDetail(scope.row)">管理章节</el-button>
          <el-button native-type="button" size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button native-type="button" size="small" type="danger" @click="removeCourse(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogOpen"
      title="课程信息"
      width="520px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="true"
      append-to-body
      destroy-on-close
    >
      <el-form label-width="80">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="课程标题" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" placeholder="课程简介" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="封面">
          <el-input v-model="form.cover" placeholder="封面URL（可选）" />
          <div v-if="normalizeUrl(form.cover)" class="cover-preview">
            <img :src="normalizeUrl(form.cover)" alt="cover-preview" />
          </div>
        </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="进度">
        <el-select v-model="form.finishStatus">
          <el-option label="更新中" :value="0" />
          <el-option label="已完结" :value="1" />
        </el-select>
      </el-form-item>
      </el-form>
      <template #footer>
        <el-button native-type="button" @click="closeCourseDialog">取消</el-button>
        <el-button native-type="button" type="primary" :loading="saving" @click="saveCourse">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.hero {
  padding: 6px 0 8px;
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

.toolbar {
  display: flex;
  justify-content: flex-end;
}

.table {
  background: var(--ui-surface);
  border-radius: 14px;
  overflow: hidden;
}

.cover-preview {
  margin-top: 8px;
  width: 100%;
  height: 120px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--ui-border-soft);
  background: var(--ui-surface-soft);
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-cell {
  display: grid;
  gap: 6px;
}

.cover-cell-img {
  width: 120px;
  height: 66px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--ui-border-soft);
}

.cover-empty {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.cover-link {
  font-size: 12px;
  color: var(--ui-accent);
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

</style>
