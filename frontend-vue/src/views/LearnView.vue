<script setup>
import { computed, onMounted, reactive, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ArrowLeft, Award, CheckCircle2, Circle, MessageCircle, PlayCircle } from 'lucide-vue-next'
import { api } from '../services/api'

const route = useRoute()
const state = reactive({
  course: null,
  lessons: [],
  enrollment: null,
  progressList: [],
  comments: [],
  commentText: '',
  replyText: {},
  replyingTo: null,
  activeLessonId: null,
  loading: true,
  error: '',
  message: ''
})

const activeLesson = computed(() => state.lessons.find((lesson) => lesson.id === state.activeLessonId) || state.lessons[0])
const progressMap = computed(() => new Map(state.progressList.map((item) => [item.lesson?.id || item.lessonId, item])))
const progressPercent = computed(() => state.enrollment?.tienDoPercent || 0)

function lessonDone(lessonId) {
  const item = progressMap.value.get(lessonId)
  return Boolean(item?.hoanThanh)
}

function mediaUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `${api.baseUrl}${url}`
}

function isEmbedVideo(url) {
  return Boolean(url && (url.includes('youtube.com') || url.includes('youtu.be')))
}

function embedUrl(url) {
  if (!url) return ''
  if (url.includes('watch?v=')) return url.replace('watch?v=', 'embed/')
  if (url.includes('youtu.be/')) return url.replace('youtu.be/', 'www.youtube.com/embed/')
  return url
}

async function loadLearning() {
  state.loading = true
  state.error = ''
  try {
    const result = await api.getLearning(route.params.id)
    state.course = result.course
    state.lessons = result.lessons || []
    state.enrollment = result.enrollment
    state.progressList = result.progressList || []
    state.activeLessonId = state.activeLessonId || state.lessons[0]?.id || null
  } catch (error) {
    state.error = error.message
  } finally {
    state.loading = false
  }
}

async function loadComments() {
  if (!activeLesson.value?.id) return
  try {
    const result = await api.getLessonComments(activeLesson.value.id)
    state.comments = result.data || []
  } catch (error) {
    state.message = error.message
  }
}

async function sendComment(parentId = null) {
  const content = parentId ? state.replyText[parentId] : state.commentText
  if (!content?.trim() || !activeLesson.value?.id) return

  try {
    await api.postLessonComment(activeLesson.value.id, { noiDung: content, parentId })
    if (parentId) {
      state.replyText[parentId] = ''
      state.replyingTo = null
    } else {
      state.commentText = ''
    }
    await loadComments()
  } catch (error) {
    state.message = error.message
  }
}

async function toggleLesson(lesson) {
  if (!state.enrollment) return
  try {
    const nextValue = !lessonDone(lesson.id)
    const result = await api.toggleProgress({
      enrollmentId: state.enrollment.id,
      lessonId: lesson.id,
      hoanThanh: nextValue
    })
    state.enrollment.tienDoPercent = result.percent
    state.activeLessonId = lesson.id
    await loadLearning()
  } catch (error) {
    state.message = error.message
  }
}

onMounted(loadLearning)
watch(() => state.activeLessonId, loadComments)
</script>

<template>
  <section class="classroom-page">
    <div v-if="state.loading" class="classroom-state loading-line">Đang tải lớp học...</div>
    <div v-else-if="state.error" class="classroom-state error">{{ state.error }}</div>

    <template v-else>
      <header class="classroom-header">
        <div class="classroom-header-left">
          <RouterLink class="classroom-back" :to="`/courses/${route.params.id}`">
            <ArrowLeft :size="17" /> Chi tiết khóa học
          </RouterLink>
          <h1>{{ state.course?.tenKhoaHoc }}</h1>
        </div>

        <button v-if="progressPercent >= 100" class="classroom-cert">
          <Award :size="17" /> Nhận chứng chỉ tốt nghiệp
        </button>
      </header>

      <div class="classroom-main">
        <main class="classroom-content">
          <section class="classroom-video-wrap">
            <iframe
              v-if="activeLesson?.videoUrl && isEmbedVideo(activeLesson.videoUrl)"
              :src="embedUrl(activeLesson.videoUrl)"
              :title="activeLesson.tieuDe"
              allowfullscreen
            />
            <video
              v-else-if="activeLesson?.videoUrl"
              :src="mediaUrl(activeLesson.videoUrl)"
              controls
              autoplay
            />
            <div v-else class="classroom-video-empty">
              <PlayCircle :size="56" />
              <span>Chưa có video cho bài này</span>
            </div>
          </section>

          <div class="classroom-info-bar">
            <h2>{{ activeLesson?.tieuDe || 'Chưa có bài học' }}</h2>
            <button v-if="activeLesson" class="classroom-complete-btn" @click="toggleLesson(activeLesson)">
              <CheckCircle2 :size="18" />
              {{ lessonDone(activeLesson.id) ? 'Bỏ hoàn thành' : 'Đánh dấu hoàn thành' }}
            </button>
          </div>

          <p v-if="state.message" class="classroom-message">{{ state.message }}</p>

          <section v-if="activeLesson" class="classroom-comments">
            <h3><MessageCircle :size="19" /> Hỏi đáp & thảo luận bài học</h3>
            <form class="classroom-comment-form" @submit.prevent="sendComment()">
              <textarea v-model="state.commentText" rows="3" placeholder="Đặt câu hỏi hoặc chia sẻ thắc mắc của bạn về bài giảng này..."></textarea>
              <button class="classroom-send-btn">Gửi bình luận</button>
            </form>

            <div class="classroom-comment-list">
              <article v-for="comment in state.comments" :key="comment.id" class="classroom-comment">
                <div class="classroom-comment-head">
                  <strong>{{ comment.user?.hoTen || 'Người học' }}</strong>
                  <span>{{ comment.createdAt }}</span>
                </div>
                <p>{{ comment.noiDung }}</p>
                <button class="text-btn" @click="state.replyingTo = state.replyingTo === comment.id ? null : comment.id">Phản hồi</button>

                <form v-if="state.replyingTo === comment.id" class="classroom-comment-form reply" @submit.prevent="sendComment(comment.id)">
                  <textarea v-model="state.replyText[comment.id]" rows="2" placeholder="Nhập phản hồi"></textarea>
                  <button class="classroom-send-btn">Gửi</button>
                </form>

                <div v-if="comment.replies?.length" class="classroom-replies">
                  <article v-for="reply in comment.replies" :key="reply.id" class="classroom-comment reply-item">
                    <div class="classroom-comment-head">
                      <strong>{{ reply.user?.hoTen || 'Người học' }}</strong>
                      <span>{{ reply.createdAt }}</span>
                    </div>
                    <p>{{ reply.noiDung }}</p>
                  </article>
                </div>
              </article>
              <div v-if="!state.comments.length" class="classroom-empty-line">Chưa có bình luận cho bài này.</div>
            </div>
          </section>
        </main>

        <aside class="classroom-sidebar">
          <div class="classroom-progress-box">
            <div class="classroom-progress-head">
              <span>Tiến độ học tập</span>
              <strong>{{ progressPercent }}%</strong>
            </div>
            <div class="classroom-progress-track">
              <i :style="{ width: `${progressPercent}%` }"></i>
            </div>
          </div>

          <h2 class="classroom-sidebar-title">Nội dung khóa học</h2>

          <div class="classroom-lesson-list">
            <button
              v-for="lesson in state.lessons"
              :key="lesson.id"
              class="classroom-lesson-item"
              :class="{ active: lesson.id === activeLesson?.id }"
              @click="state.activeLessonId = lesson.id"
            >
              <div class="classroom-lesson-head">
                <component :is="lessonDone(lesson.id) ? CheckCircle2 : Circle" :size="17" />
                <span>Bài {{ lesson.thuTu }}</span>
              </div>
              <strong>{{ lesson.tieuDe }}</strong>
              <small>{{ lesson.thoiLuongPhut || 0 }} phút</small>
            </button>
            <div v-if="!state.lessons.length" class="classroom-empty-line">Khóa học chưa có bài học.</div>
          </div>
        </aside>
      </div>
    </template>
  </section>
</template>
