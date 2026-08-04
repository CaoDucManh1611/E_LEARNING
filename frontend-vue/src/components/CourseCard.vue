<script setup>
import { RouterLink } from 'vue-router'
import { ArrowRight, BookOpen, Star, UsersRound } from 'lucide-vue-next'
import { api } from '../services/api'

defineProps({
  course: {
    type: Object,
    required: true
  }
})

function formatMoney(value) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
}

function imageUrl(path) {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${api.baseUrl}${path}`
}
</script>

<template>
  <article class="course-card">
    <RouterLink class="course-media" :to="`/courses/${course.id}`">
      <img v-if="course.hinhAnh" :src="imageUrl(course.hinhAnh)" :alt="course.tenKhoaHoc" />
      <div v-else class="course-fallback">
        <BookOpen :size="34" />
      </div>
      <span class="level-badge">{{ course.capDo || 'Beginner' }}</span>
    </RouterLink>

    <div class="course-body">
      <div class="course-meta">
        <span>{{ course.category?.tenDanhMuc || 'Khóa học' }}</span>
        <span><Star :size="14" /> {{ course.averageStars || 0 }}</span>
      </div>
      <RouterLink class="course-title" :to="`/courses/${course.id}`">{{ course.tenKhoaHoc }}</RouterLink>
      <p class="course-desc">{{ course.moTa }}</p>

      <div class="course-stats">
        <span><Star :size="15" /> {{ course.reviewCount || 0 }} đánh giá</span>
        <span><UsersRound :size="15" /> {{ course.teacher?.hoTen || 'EduRecommend' }}</span>
      </div>

      <div class="course-footer">
        <strong>{{ formatMoney(course.gia) }}</strong>
        <RouterLink class="btn-small" :to="`/courses/${course.id}`">Chi tiết <ArrowRight :size="15" /></RouterLink>
      </div>
    </div>
  </article>
</template>
