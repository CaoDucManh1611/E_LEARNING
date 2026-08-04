<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ArrowRight, BarChart3, BrainCircuit, Search, ShieldCheck, Sparkles, TrendingUp } from 'lucide-vue-next'
import CourseCard from '../components/CourseCard.vue'
import { api } from '../services/api'

const state = reactive({
  courses: [],
  loading: true,
  error: ''
})

const featuredCourses = computed(() => state.courses.slice(0, 6))
const carouselCourses = computed(() => [...featuredCourses.value, ...featuredCourses.value])

async function loadCourses() {
  state.loading = true
  state.error = ''
  try {
    const result = await api.getCourses({ sort: 'latest' })
    state.courses = result.data || []
  } catch (error) {
    state.error = error.message
  } finally {
    state.loading = false
  }
}

onMounted(loadCourses)
</script>

<template>
  <section class="hero intro-hero hero-simple">
    <div class="hero-copy">
      <div class="hero-badge"><Sparkles :size="15" /> Hệ thống học tập thông minh</div>
      <h1 class="hero-title">EduRecommend</h1>
      <p class="hero-desc">
        Nền tảng E-Learning kết hợp quản lý khóa học, theo dõi tiến độ và gợi ý lộ trình học tập bằng AI.
      </p>
      <div class="hero-actions">
        <RouterLink class="btn-submit compact hero-btn" to="/courses"><Search :size="18" /> Xem khóa học</RouterLink>
        <RouterLink class="btn-outline hero-outline" to="/recommend"><TrendingUp :size="18" /> Gợi ý AI</RouterLink>
      </div>
    </div>
  </section>

  <section class="intro-band">
    <div class="intro-feature">
      <BrainCircuit :size="25" />
      <strong>Gợi ý học tập bằng AI</strong>
      <span>Phân tích hồ sơ học tập và kỹ năng để đề xuất khóa học phù hợp.</span>
    </div>
    <div class="intro-feature">
      <BarChart3 :size="25" />
      <strong>Theo dõi tiến độ</strong>
      <span>Học viên học theo bài, đánh dấu hoàn thành và nhận chứng chỉ.</span>
    </div>
    <div class="intro-feature">
      <ShieldCheck :size="25" />
      <strong>Quản lý đầy đủ vai trò</strong>
      <span>Admin, giảng viên và học viên dùng chung backend REST API.</span>
    </div>
  </section>

  <section class="container featured-section">
    <div class="section-head-row">
      <div>
        <p class="detail-eyebrow">Khóa học nổi bật</p>
        <h2 class="section-title-large">Lộ trình đang được quan tâm</h2>
      </div>
      <RouterLink class="btn-outline" to="/courses">Xem tất cả <ArrowRight :size="16" /></RouterLink>
    </div>

    <div v-if="state.loading" class="status-box loading-line">Đang tải khóa học nổi bật...</div>
    <div v-else-if="state.error" class="status-box error">{{ state.error }}</div>
    <div v-else-if="featuredCourses.length" class="course-carousel">
      <div class="course-track">
        <CourseCard
          v-for="(course, index) in carouselCourses"
          :key="`${course.id}-${index}`"
          :course="course"
          class="carousel-card"
        />
      </div>
    </div>
    <div v-else class="status-box">Chưa có khóa học nổi bật.</div>
  </section>
</template>
