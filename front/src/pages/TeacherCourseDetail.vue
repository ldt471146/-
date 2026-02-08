<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  addTeacherChapter,
  addTeacherLesson,
  deleteTeacherChapter,
  deleteTeacherLesson,
  fetchTeacherCourseDetail,
  updateTeacherChapter,
  updateTeacherLesson
} from '../api/teacher'
import { ElNotification } from 'element-plus'

const route = useRoute()
const loading = ref(false)
const detail = ref(null)
const chapterDialog = ref(false)
const lessonDialog = ref(false)
const chapterSaving = ref(false)
const lessonSaving = ref(false)
const editingChapterId = ref(null)
const editingLessonId = ref(null)
const currentChapterId = ref(null)
const chapterForm = ref({ title: '', sortNo: 1 })
const lessonForm = ref({ title: '', contentType: 'text', contentText: '', contentUrl: '', sortNo: 1 })

const normalizeUrl = (val) => {
  const raw = String(val || '').trim()
  if (!raw) return ''
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('//')) return `https:${raw}`
  return `https://${raw}`
}

const load = async () => {
  loading.value = true
  try {
    const res = await fetchTeacherCourseDetail(route.params.id)
    detail.value = res.data
  } catch (e) {
    ElNotification({
      title: '加载失败',
      message: e?.message || '课程详情加载失败',
      type: 'error',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const openAddChapter = () => {
  editingChapterId.value = null
  chapterForm.value = { title: '', sortNo: 1 }
  chapterDialog.value = true
}

const openEditChapter = (ch) => {
  editingChapterId.value = ch.id
  chapterForm.value = { title: ch.title || '', sortNo: ch.sortNo || 1 }
  chapterDialog.value = true
}

const saveChapter = async () => {
  if (chapterSaving.value) return
  if (!chapterForm.value.title) {
    ElNotification({ title: '请填写章节标题', type: 'warning', duration: 1500 })
    return
  }
  try {
    chapterSaving.value = true
    if (editingChapterId.value) {
      await updateTeacherChapter(editingChapterId.value, chapterForm.value)
    } else {
      await addTeacherChapter(route.params.id, chapterForm.value)
    }
    chapterDialog.value = false
    await load()
  } catch (e) {
    ElNotification({ title: '保存失败', message: e?.message || '请稍后再试', type: 'error', duration: 2000 })
  } finally {
    chapterSaving.value = false
  }
}

const removeChapter = async (ch) => {
  try {
    await deleteTeacherChapter(ch.id)
    await load()
  } catch (e) {
    ElNotification({ title: '删除失败', message: e?.message || '请稍后再试', type: 'error', duration: 2000 })
  }
}

const openAddLesson = (ch) => {
  currentChapterId.value = ch.id
  editingLessonId.value = null
  lessonForm.value = { title: '', contentType: 'text', contentText: '', contentUrl: '', sortNo: 1 }
  lessonDialog.value = true
}

const openEditLesson = (ch, lesson) => {
  currentChapterId.value = ch.id
  editingLessonId.value = lesson.id
  lessonForm.value = {
    title: lesson.title || '',
    contentType: lesson.contentType || 'text',
    contentText: lesson.contentText || '',
    contentUrl: lesson.contentUrl || '',
    sortNo: lesson.sortNo || 1
  }
  lessonDialog.value = true
}

const saveLesson = async () => {
  if (lessonSaving.value) return
  if (!lessonForm.value.title) {
    ElNotification({ title: '请填写课时标题', type: 'warning', duration: 1500 })
    return
  }
  try {
    lessonSaving.value = true
    const payload = {
      ...lessonForm.value,
      contentUrl: normalizeUrl(lessonForm.value.contentUrl)
    }
    if (editingLessonId.value) {
      await updateTeacherLesson(editingLessonId.value, payload)
    } else {
      await addTeacherLesson(currentChapterId.value, payload)
    }
    lessonDialog.value = false
    await load()
  } catch (e) {
    ElNotification({ title: '保存失败', message: e?.message || '请稍后再试', type: 'error', duration: 2000 })
  } finally {
    lessonSaving.value = false
  }
}

const closeChapterDialog = () => {
  chapterDialog.value = false
}

const closeLessonDialog = () => {
  lessonDialog.value = false
}

const removeLesson = async (lesson) => {
  try {
    await deleteTeacherLesson(lesson.id)
    await load()
  } catch (e) {
    ElNotification({ title: '删除失败', message: e?.message || '请稍后再试', type: 'error', duration: 2000 })
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="hero">
      <div>
        <div class="title display">课程结构管理</div>
        <div class="subtitle">{{ detail?.title }}</div>
      </div>
      <el-button native-type="button" type="primary" @click.stop="openAddChapter">新增章节</el-button>
    </div>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-card"></div>
      </template>
      <template #default>
        <div v-if="detail?.chapters?.length" class="chapters">
          <div v-for="ch in detail.chapters" :key="ch.id" class="chapter-card">
            <div class="chapter-head">
              <div class="chapter-title">{{ ch.title }}</div>
              <div class="chapter-actions">
                <el-button native-type="button" size="small" @click="openAddLesson(ch)">新增课时</el-button>
                <el-button native-type="button" size="small" @click="openEditChapter(ch)">编辑</el-button>
                <el-button native-type="button" size="small" type="danger" @click="removeChapter(ch)">删除</el-button>
              </div>
            </div>
            <div class="lessons">
              <div v-for="lesson in ch.lessons" :key="lesson.id" class="lesson-item">
                <div>
                  <div class="lesson-title">{{ lesson.title }}</div>
                  <div class="lesson-meta">{{ lesson.contentType }}</div>
                  <a
                    v-if="normalizeUrl(lesson.contentUrl)"
                    class="lesson-link"
                    :href="normalizeUrl(lesson.contentUrl)"
                    target="_blank"
                    rel="noreferrer"
                  >
                    {{ normalizeUrl(lesson.contentUrl) }}
                  </a>
                </div>
                <div class="lesson-actions">
                  <el-button native-type="button" size="small" @click="openEditLesson(ch, lesson)">编辑</el-button>
                  <el-button native-type="button" size="small" type="danger" @click="removeLesson(lesson)">删除</el-button>
                </div>
              </div>
              <div v-if="!ch.lessons?.length" class="empty">暂无课时</div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无章节，请先创建章节" />
      </template>
    </el-skeleton>

    <el-dialog
      v-model="chapterDialog"
      title="章节"
      width="480px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="true"
      append-to-body
      destroy-on-close
    >
      <el-form label-width="80">
        <el-form-item label="标题">
          <el-input v-model="chapterForm.title" placeholder="章节标题" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="chapterForm.sortNo" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button native-type="button" @click="closeChapterDialog">取消</el-button>
        <el-button native-type="button" type="primary" :loading="chapterSaving" @click="saveChapter">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="lessonDialog"
      title="课时"
      width="560px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="true"
      append-to-body
      destroy-on-close
    >
      <el-form label-width="90">
        <el-form-item label="标题">
          <el-input v-model="lessonForm.title" placeholder="课时标题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="lessonForm.contentType">
            <el-option label="文本" value="text" />
            <el-option label="视频" value="video" />
            <el-option label="文件" value="file" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容文本" v-if="lessonForm.contentType === 'text'">
          <el-input v-model="lessonForm.contentText" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="资源地址" v-else>
          <el-input v-model="lessonForm.contentUrl" placeholder="资源 URL" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="lessonForm.sortNo" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button native-type="button" @click="closeLessonDialog">取消</el-button>
        <el-button native-type="button" type="primary" :loading="lessonSaving" @click="saveLesson">保存</el-button>
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
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.chapters {
  display: grid;
  gap: 14px;
}

.chapter-card {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 14px;
  padding: 14px;
  display: grid;
  gap: 12px;
}

.chapter-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.chapter-title {
  font-weight: 600;
}

.lessons {
  display: grid;
  gap: 8px;
}

.lesson-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--ui-surface-soft);
  border: 1px solid var(--ui-border-soft);
}

.lesson-title {
  font-weight: 600;
}

.lesson-meta {
  font-size: 12px;
  color: var(--ui-text-muted);
}

.lesson-link {
  margin-top: 4px;
  display: block;
  font-size: 12px;
  color: var(--ui-accent);
  max-width: 420px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty {
  font-size: 12px;
  color: var(--ui-text-muted);
  text-align: center;
  padding: 10px 0;
}
</style>
