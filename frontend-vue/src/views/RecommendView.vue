<script setup>
import { onMounted, reactive } from 'vue'
import { RouterLink } from 'vue-router'
import { BrainCircuit, ExternalLink, Save, Sparkles } from 'lucide-vue-next'
import { api } from '../services/api'

const state = reactive({
  skills: [],
  selectedSkills: [],
  localCourses: [],
  result: null,
  loading: false,
  message: '',
  profile: {
    hoursStudied: 8,
    attendance: 80,
    previousScores: 70,
    sleepHours: 7,
    tutoringSessions: 1,
    extracurricularActivities: 0,
    learningDisabilities: 0,
    familyIncome: 1,
    parentalInvolvement: 1,
    internetAccess: 1,
    socialMediaUsage: 'Medium',
    distanceFromHome: 1,
    accessToResources: 1,
    parentalEducationLevel: 2,
    physicalActivity: 2,
    motivationLevel: 1,
    peerInfluence: 1,
    gender: 0,
    topN: 5
  }
})

const fields = [
  ['hoursStudied', 'Giờ học mỗi tuần'],
  ['attendance', 'Chuyên cần (%)'],
  ['previousScores', 'Điểm trước đó'],
  ['sleepHours', 'Giờ ngủ'],
  ['tutoringSessions', 'Buổi học thêm'],
  ['physicalActivity', 'Hoạt động thể chất'],
  ['topN', 'Số khóa gợi ý']
]

function normalize(value = '') {
  return value
    .toString()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
}

function courseName(course) {
  return course.tenKhoaHoc || course.courseName || course.course_name || 'Khóa học'
}

function courseMeta(course) {
  return [
    course.toChuc || course.category,
    course.difficulty || course.capDo,
    course.matchScore || course.match_score || course.skillMatch
  ].filter(Boolean).join(' · ')
}

function externalUrl(course) {
  return course.duongDan || course.url || ''
}

function localCourseFor(course) {
  const name = normalize(courseName(course))
  return state.localCourses.find((item) => normalize(item.tenKhoaHoc) === name)
}

function recommendations() {
  return state.result?.data?.khoaHocGoiY || state.result?.recommendations || []
}

async function loadData() {
  try {
    const [skills, profile, courses] = await Promise.allSettled([
      api.getSkills(),
      api.getProfile(),
      api.getCourses({ sort: 'latest' })
    ])
    if (skills.status === 'fulfilled') state.skills = skills.value.skills || []
    if (profile.status === 'fulfilled' && profile.value.profile) Object.assign(state.profile, profile.value.profile)
    if (courses.status === 'fulfilled') state.localCourses = courses.value.data || []
  } catch {
    state.message = 'Chưa thể tải dữ liệu gợi ý.'
  }
}

async function saveProfile() {
  try {
    const result = await api.saveProfile(state.profile)
    state.message = result.message || 'Đã lưu hồ sơ học tập.'
  } catch (error) {
    state.message = error.message
  }
}

async function recommend() {
  state.loading = true
  state.message = ''
  try {
    const payload = {
      ...state.profile,
      inputSkills: state.selectedSkills,
      topN: Number(state.profile.topN || 5)
    }
    state.result = await api.recommend(payload)
  } catch (error) {
    state.message = error.message
  } finally {
    state.loading = false
  }
}

function toggleSkill(skill) {
  if (state.selectedSkills.includes(skill)) {
    state.selectedSkills = state.selectedSkills.filter((item) => item !== skill)
  } else {
    state.selectedSkills.push(skill)
  }
}

onMounted(loadData)
</script>

<template>
  <section class="container">
    <div class="page-header left">
      <h1 class="page-title">Gợi ý lộ trình AI</h1>
      <p class="page-sub">Lưu hồ sơ học tập và nhận danh sách khóa học phù hợp từ mô hình gợi ý.</p>
    </div>

    <div class="recommend-layout">
      <section class="panel">
        <h2>Hồ sơ học tập</h2>
        <form class="stack-form" @submit.prevent="recommend">
          <div class="two-cols">
            <label v-for="[key, label] in fields" :key="key" class="form-group">
              <span>{{ label }}</span>
              <input v-model.number="state.profile[key]" type="number" />
            </label>
          </div>

          <div class="two-cols">
            <label class="form-group">
              <span>Mức dùng mạng xã hội</span>
              <select v-model="state.profile.socialMediaUsage">
                <option>Low</option>
                <option>Medium</option>
                <option>High</option>
              </select>
            </label>
            <label class="form-group">
              <span>Giới tính</span>
              <select v-model.number="state.profile.gender">
                <option :value="0">Nam</option>
                <option :value="1">Nữ</option>
              </select>
            </label>
          </div>

          <div class="skill-picker">
            <button
              v-for="skill in state.skills.slice(0, 28)"
              :key="skill"
              type="button"
              class="skill-chip"
              :class="{ active: state.selectedSkills.includes(skill) }"
              @click="toggleSkill(skill)"
            >
              {{ skill }}
            </button>
          </div>

          <div class="button-row">
            <button class="btn-submit compact" type="button" @click="saveProfile"><Save :size="18" /> Lưu hồ sơ</button>
            <button class="btn-submit compact" :disabled="state.loading"><Sparkles :size="18" /> Gợi ý khóa học</button>
          </div>
          <p v-if="state.message" class="form-message">{{ state.message }}</p>
        </form>
      </section>

      <section class="panel">
        <h2>Kết quả</h2>
        <div v-if="state.loading" class="status-box loading-line">Đang phân tích dữ liệu học tập...</div>
        <div v-else-if="!state.result" class="ai-empty">
          <BrainCircuit :size="46" />
          <strong>Chưa có kết quả gợi ý</strong>
        </div>
        <template v-else>
          <div class="ai-summary">
            <span>Nhóm: {{ state.result.data?.nhomSinhVien || state.result.grade || 'Đang cập nhật' }}</span>
            <strong v-if="state.result.predictedScore">Điểm dự đoán: {{ state.result.predictedScore }}</strong>
          </div>

          <template v-for="course in recommendations()" :key="courseName(course) + externalUrl(course)">
            <RouterLink v-if="localCourseFor(course)" class="list-item recommend-item" :to="`/courses/${localCourseFor(course).id}`">
              <strong>{{ courseName(course) }}</strong>
              <span>{{ courseMeta(course) || 'Khóa học trong hệ thống' }}</span>
            </RouterLink>
            <a v-else-if="externalUrl(course)" class="list-item recommend-item" :href="externalUrl(course)" target="_blank" rel="noreferrer">
              <strong>{{ courseName(course) }} <ExternalLink :size="15" /></strong>
              <span>{{ courseMeta(course) || 'Khóa học bên ngoài' }}</span>
            </a>
            <div v-else class="list-item static recommend-item muted">
              <strong>{{ courseName(course) }}</strong>
              <span>{{ courseMeta(course) || 'Chưa có đường dẫn khóa học' }}</span>
            </div>
          </template>
        </template>
      </section>
    </div>
  </section>
</template>
