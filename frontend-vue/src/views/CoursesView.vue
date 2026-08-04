<script setup>
import { computed, onMounted, reactive } from 'vue'
import { Search } from 'lucide-vue-next'
import CourseCard from '../components/CourseCard.vue'
import { api } from '../services/api'

const state = reactive({
  courses: [],
  categories: [],
  visibleCount: 6,
  loading: true,
  error: ''
})

const filters = reactive({
  search: '',
  categoryId: '',
  capDo: '',
  sort: 'latest'
})

const visibleCourses = computed(() => state.courses.slice(0, state.visibleCount))
const hasMoreCourses = computed(() => state.visibleCount < state.courses.length)

async function loadCourses() {
  state.loading = true
  state.error = ''
  try {
    const result = await api.getCourses(filters)
    state.courses = result.data || []
    state.categories = result.categories || []
    state.visibleCount = 6
  } catch (error) {
    state.error = error.message
  } finally {
    state.loading = false
  }
}

function showMore() {
  state.visibleCount += 6
}

onMounted(loadCourses)
</script>

<template>
  <section class="container courses-page">
    <div class="page-header">
      <h1 class="page-title">Khóa học</h1>
      <p class="page-sub">Tìm kiếm, lọc danh mục và chọn khóa học phù hợp với mục tiêu của bạn.</p>
    </div>

    <form class="filter-card" @submit.prevent="loadCourses">
      <label class="form-group">
        <span>Tìm kiếm</span>
        <div class="search-wrapper">
          <input v-model="filters.search" placeholder="Java, Python, MySQL..." />
          <button class="btn-search-icon" aria-label="Tìm kiếm"><Search :size="18" /></button>
        </div>
      </label>

      <label class="form-group">
        <span>Danh mục</span>
        <select v-model="filters.categoryId" @change="loadCourses">
          <option value="">Tất cả</option>
          <option v-for="cat in state.categories" :key="cat.id" :value="cat.id">{{ cat.tenDanhMuc }}</option>
        </select>
      </label>

      <label class="form-group">
        <span>Cấp độ</span>
        <select v-model="filters.capDo" @change="loadCourses">
          <option value="">Tất cả</option>
          <option value="Beginner">Beginner</option>
          <option value="Intermediate">Intermediate</option>
          <option value="Advanced">Advanced</option>
        </select>
      </label>

      <label class="form-group">
        <span>Sắp xếp</span>
        <select v-model="filters.sort" @change="loadCourses">
          <option value="latest">Mới nhất</option>
          <option value="price_asc">Giá thấp đến cao</option>
          <option value="price_desc">Giá cao đến thấp</option>
        </select>
      </label>
    </form>

    <div v-if="state.loading" class="status-box loading-line">Đang tải khóa học...</div>
    <div v-else-if="state.error" class="status-box error">{{ state.error }}</div>
    <template v-else-if="state.courses.length">
      <div class="course-grid">
        <CourseCard v-for="course in visibleCourses" :key="course.id" :course="course" />
      </div>
      <div v-if="hasMoreCourses" class="load-more-row">
        <button class="btn-outline load-more-btn" @click="showMore">Xem thêm</button>
      </div>
    </template>
    <div v-else class="status-box">Không tìm thấy khóa học phù hợp.</div>
  </section>
</template>
