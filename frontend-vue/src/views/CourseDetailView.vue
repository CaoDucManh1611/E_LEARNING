<script setup>
import { inject, onMounted, reactive } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ArrowLeft, BookOpen, CheckCircle2, ShoppingCart, Star } from 'lucide-vue-next'
import { api } from '../services/api'

const route = useRoute()
const router = useRouter()
const auth = inject('auth')
const state = reactive({
  course: null,
  lessons: [],
  reviews: [],
  isEnrolled: false,
  canReview: false,
  review: { soSao: 5, noiDung: '' },
  loading: true,
  error: '',
  message: ''
})

function formatMoney(value) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
}

function imageUrl(path) {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${api.baseUrl}${path}`
}

async function loadCourse() {
  state.loading = true
  state.error = ''
  try {
    const result = await api.getCourse(route.params.id)
    state.course = result.course
    state.lessons = result.lessons || []
    state.reviews = result.reviews || []
    state.isEnrolled = Boolean(result.isEnrolled)
    state.canReview = Boolean(result.canReview)
  } catch (error) {
    state.error = error.message
  } finally {
    state.loading = false
  }
}

async function addToCart() {
  if (!auth.user) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  try {
    await api.addToCart(route.params.id)
    state.message = 'Đã thêm khóa học vào giỏ hàng.'
  } catch (error) {
    state.message = error.message
  }
}

async function submitReview() {
  try {
    const result = await api.submitReview(route.params.id, state.review)
    state.message = result.message || 'Đã gửi đánh giá.'
    state.review = { soSao: 5, noiDung: '' }
    await loadCourse()
  } catch (error) {
    state.message = error.message
  }
}

onMounted(loadCourse)
</script>

<template>
  <section class="container detail-wrap">
    <button class="ghost-btn" @click="router.back()"><ArrowLeft :size="18" /> Quay lại</button>

    <div v-if="state.loading" class="status-box">Đang tải chi tiết...</div>
    <div v-else-if="state.error" class="status-box error">{{ state.error }}</div>

    <template v-else>
      <div class="detail-layout">
        <div class="detail-main">
          <div class="detail-eyebrow">{{ state.course.category?.tenDanhMuc }} · {{ state.course.capDo }}</div>
          <h1 class="detail-title">{{ state.course.tenKhoaHoc }}</h1>
          <p class="detail-desc">{{ state.course.moTa }}</p>
          <div class="detail-pills">
            <span><Star :size="16" /> {{ state.course.averageStars || 0 }} sao</span>
            <span><BookOpen :size="16" /> {{ state.lessons.length }} bài học</span>
            <span><CheckCircle2 :size="16" /> {{ state.course.teacher?.hoTen || 'EduRecommend' }}</span>
          </div>
        </div>

        <aside class="purchase-panel">
          <img v-if="state.course.hinhAnh" :src="imageUrl(state.course.hinhAnh)" :alt="state.course.tenKhoaHoc" />
          <div class="price">{{ formatMoney(state.course.gia) }}</div>
          <RouterLink v-if="state.isEnrolled" class="btn-submit link-btn" :to="`/learn/${state.course.id}`">
            <CheckCircle2 :size="18" /> Vào học ngay
          </RouterLink>
          <button v-else class="btn-submit" @click="addToCart"><ShoppingCart :size="18" /> Thêm vào giỏ</button>
          <p v-if="state.message" class="form-message">{{ state.message }}</p>
        </aside>
      </div>

      <div class="split-section">
        <section>
          <h2 class="section-heading">Nội dung khóa học</h2>
          <div class="lesson-list">
            <div v-for="lesson in state.lessons" :key="lesson.id" class="lesson-row">
              <span>{{ lesson.thuTu }}</span>
              <strong>{{ lesson.tieuDe }}</strong>
              <small>{{ lesson.thoiLuongPhut || 0 }} phút</small>
            </div>
          </div>
        </section>

        <section>
          <h2 class="section-heading">Đánh giá học viên</h2>
          <div class="review-list">
            <form v-if="state.canReview" class="review-form" @submit.prevent="submitReview">
              <label class="form-group">
                <span>Số sao</span>
                <select v-model.number="state.review.soSao">
                  <option v-for="star in [5, 4, 3, 2, 1]" :key="star" :value="star">{{ star }} sao</option>
                </select>
              </label>
              <label class="form-group">
                <span>Nhận xét</span>
                <textarea v-model="state.review.noiDung" rows="3" placeholder="Cảm nhận của bạn về khóa học"></textarea>
              </label>
              <button class="btn-submit compact">Gửi đánh giá</button>
            </form>

            <div v-for="review in state.reviews" :key="review.id" class="review-row">
              <strong>{{ review.user?.hoTen || 'Học viên' }}</strong>
              <span>{{ review.soSao }} sao</span>
              <p>{{ review.noiDung }}</p>
            </div>
            <div v-if="!state.reviews.length" class="empty-line">Chưa có đánh giá.</div>
          </div>
        </section>
      </div>
    </template>
  </section>
</template>
