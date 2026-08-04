<script setup>
import { computed, onMounted, reactive } from 'vue'
import {
  BarChart3,
  Bell,
  BookOpen,
  ChevronLeft,
  Edit3,
  GraduationCap,
  Home,
  Plus,
  RefreshCw,
  Star,
  Trash2,
  Upload
} from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import { api } from '../services/api'

const emptyCourse = () => ({
  id: null,
  tenKhoaHoc: '',
  moTa: '',
  gia: 0,
  capDo: 'Beginner',
  hinhAnh: '',
  categoryId: ''
})

const emptyLesson = () => ({
  id: null,
  tieuDe: '',
  videoUrl: '',
  thuTu: 1,
  thoiLuongPhut: 0
})

const state = reactive({
  activeTab: 'overview',
  dashboard: null,
  report: null,
  ownedCourses: [],
  boughtCourses: [],
  reviews: [],
  notifications: [],
  courses: [],
  categories: [],
  selectedCourseId: null,
  lessons: [],
  courseForm: emptyCourse(),
  lessonForm: emptyLesson(),
  deleteReason: '',
  error: '',
  message: '',
  uploadingImage: false,
  uploadingVideo: false,
  loading: true
})

const selectedCourse = computed(() => state.courses.find((course) => course.id === state.selectedCourseId) || null)
const recentEarnings = computed(() => (state.dashboard?.earnings || []).slice(0, 6))

const tabs = [
  { key: 'overview', label: 'Tổng quan', icon: BarChart3 },
  { key: 'courses', label: 'Khóa học của tôi', icon: BookOpen },
  { key: 'lessons', label: 'Bài học', icon: GraduationCap },
  { key: 'reviews', label: 'Đánh giá học viên', icon: Star },
  { key: 'reports', label: 'Báo cáo doanh thu', icon: BarChart3 },
  { key: 'notifications', label: 'Thông báo', icon: Bell }
]

function formatMoney(value) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
}

function imageUrl(path) {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${api.baseUrl}${path}`
}

function statusText(status) {
  const map = {
    active: 'Đang bán',
    pending_review: 'Chờ duyệt',
    draft: 'Bản nháp',
    rejected: 'Từ chối'
  }
  return map[status] || status || 'Đang cập nhật'
}

async function loadData() {
  state.loading = true
  state.error = ''
  try {
    const [dashboard, courses, reviews, reports, notifications, myCourses] = await Promise.allSettled([
      api.getTeacherDashboard(),
      api.getTeacherCourses(),
      api.getTeacherReviews(),
      api.getTeacherReports(),
      api.getTeacherNotifications(),
      api.getTeacherOwnedAndBoughtCourses()
    ])

    if (dashboard.status === 'fulfilled') state.dashboard = dashboard.value
    if (courses.status === 'fulfilled') {
      state.courses = courses.value.data || []
      state.categories = courses.value.categories || []
      if (!state.selectedCourseId && state.courses.length) state.selectedCourseId = state.courses[0].id
    }
    if (reviews.status === 'fulfilled') state.reviews = reviews.value.data || []
    if (reports.status === 'fulfilled') state.report = reports.value
    if (notifications.status === 'fulfilled') state.notifications = notifications.value.data || []
    if (myCourses.status === 'fulfilled') {
      state.ownedCourses = myCourses.value.ownedCourses || []
      state.boughtCourses = myCourses.value.boughtCourses || []
    }

    await loadLessons()
  } catch (error) {
    state.error = error.message
  } finally {
    state.loading = false
  }
}

async function loadLessons() {
  state.lessons = []
  if (!state.selectedCourseId) return
  try {
    const result = await api.getTeacherLessons(state.selectedCourseId)
    state.lessons = result.lessons || []
  } catch (error) {
    state.message = error.message
  }
}

function setTab(tab) {
  state.activeTab = tab
}

function editCourse(course) {
  state.courseForm = {
    id: course.id,
    tenKhoaHoc: course.tenKhoaHoc || '',
    moTa: course.moTa || '',
    gia: course.gia || 0,
    capDo: course.capDo || 'Beginner',
    hinhAnh: course.hinhAnh || '',
    categoryId: course.category?.id || ''
  }
  state.activeTab = 'course-form'
}

function createCourse() {
  state.courseForm = emptyCourse()
  state.activeTab = 'course-form'
}

function resetCourseForm() {
  state.courseForm = emptyCourse()
}

async function saveCourse() {
  try {
    const payload = { ...state.courseForm }
    const result = payload.id
      ? await api.updateTeacherCourse(payload.id, payload)
      : await api.createTeacherCourse(payload)
    state.message = result.message
    resetCourseForm()
    state.activeTab = 'courses'
    await loadData()
  } catch (error) {
    state.message = error.message
  }
}

async function removeCourse(course) {
  try {
    const result = await api.deleteTeacherCourse(course.id, state.deleteReason)
    state.message = result.message
    state.deleteReason = ''
    if (state.selectedCourseId === course.id) state.selectedCourseId = null
    await loadData()
  } catch (error) {
    state.message = error.message
  }
}

function openLessons(course) {
  state.selectedCourseId = course.id
  state.activeTab = 'lessons'
  loadLessons()
}

function editLesson(lesson) {
  state.lessonForm = {
    id: lesson.id,
    tieuDe: lesson.tieuDe || '',
    videoUrl: lesson.videoUrl || '',
    thuTu: lesson.thuTu || 1,
    thoiLuongPhut: lesson.thoiLuongPhut || 0
  }
  state.activeTab = 'lesson-form'
}

function createLesson() {
  state.lessonForm = emptyLesson()
  state.activeTab = 'lesson-form'
}

function resetLessonForm() {
  state.lessonForm = emptyLesson()
}

async function saveLesson() {
  if (!state.selectedCourseId) return
  try {
    const payload = { ...state.lessonForm }
    const result = payload.id
      ? await api.updateTeacherLesson(state.selectedCourseId, payload.id, payload)
      : await api.createTeacherLesson(state.selectedCourseId, payload)
    state.message = result.message
    resetLessonForm()
    state.activeTab = 'lessons'
    await loadLessons()
  } catch (error) {
    state.message = error.message
  }
}

async function removeLesson(lesson) {
  try {
    const result = await api.deleteTeacherLesson(state.selectedCourseId, lesson.id)
    state.message = result.message
    await loadLessons()
  } catch (error) {
    state.message = error.message
  }
}

async function uploadCourseImage(event) {
  const file = event.target.files?.[0]
  if (!file) return
  state.uploadingImage = true
  try {
    const result = await api.uploadImage(file)
    state.courseForm.hinhAnh = result.path
    state.message = 'Đã tải ảnh khóa học.'
  } catch (error) {
    state.message = error.message
  } finally {
    state.uploadingImage = false
  }
}

async function uploadLessonVideo(event) {
  const file = event.target.files?.[0]
  if (!file) return
  state.uploadingVideo = true
  try {
    const result = await api.uploadVideo(file)
    state.lessonForm.videoUrl = result.path
    state.message = 'Đã tải video bài học.'
  } catch (error) {
    state.message = error.message
  } finally {
    state.uploadingVideo = false
  }
}

async function readNotification(notification) {
  try {
    await api.readTeacherNotification(notification.id)
    notification.daDoc = true
  } catch (error) {
    state.message = error.message
  }
}

onMounted(loadData)
</script>

<template>
  <section class="teacher-shell">
    <aside class="teacher-sidebar">
      <RouterLink class="teacher-logo" to="/">Edu<em>Recommend</em></RouterLink>
      <span class="teacher-menu-title">Điều hướng</span>

      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="teacher-side-link"
        :class="{ active: state.activeTab === tab.key }"
        @click="setTab(tab.key)"
      >
        <component :is="tab.icon" :size="18" />
        <span>{{ tab.label }}</span>
      </button>

      <span class="teacher-menu-title second">Học viên</span>
      <RouterLink class="teacher-side-link" to="/">
        <Home :size="18" />
        <span>Về trang chủ</span>
      </RouterLink>
    </aside>

    <main class="teacher-main">
      <div v-if="state.error" class="status-box error">{{ state.error }}</div>
      <div v-else-if="state.loading" class="status-box loading-line">Đang tải khu giảng viên...</div>
      <template v-else>
        <p v-if="state.message" class="form-message panel-message">{{ state.message }}</p>

        <section v-if="state.activeTab === 'overview'" class="teacher-view">
          <div class="teacher-page-header">
            <div>
              <h1 class="page-title">Bảng điều khiển giảng viên</h1>
              <p class="page-sub">Theo dõi doanh thu, học viên và các khóa học đang quản lý.</p>
            </div>
            <button class="btn-submit compact" @click="createCourse"><Plus :size="18" /> Tạo khóa học</button>
          </div>

          <div class="teacher-stats">
            <div class="teacher-stat-card accent">
              <strong>{{ formatMoney(state.dashboard?.totalEarning) }}</strong>
              <span>Doanh thu thực nhận</span>
            </div>
            <div class="teacher-stat-card">
              <strong>{{ state.dashboard?.studentCount || 0 }}</strong>
              <span>Học viên đã đăng ký</span>
            </div>
            <div class="teacher-stat-card">
              <strong>{{ state.dashboard?.reviewCount || 0 }}</strong>
              <span>Lượt đánh giá</span>
            </div>
            <div class="teacher-stat-card">
              <strong>{{ state.courses.length }}</strong>
              <span>Khóa học đã tạo</span>
            </div>
          </div>

          <h2 class="section-heading">Doanh thu gần đây</h2>
          <div class="teacher-table-wrap">
            <table class="teacher-table">
              <thead>
                <tr>
                  <th>Khóa học</th>
                  <th>Đơn hàng</th>
                  <th>Tiền nhận</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!recentEarnings.length">
                  <td colspan="3" class="teacher-empty">Chưa có dữ liệu doanh thu.</td>
                </tr>
                <tr v-for="earning in recentEarnings" :key="earning.id">
                  <td>{{ earning.course?.tenKhoaHoc || 'Khóa học' }}</td>
                  <td>#{{ earning.order?.id || earning.orderId || '-' }}</td>
                  <td class="money-cell">{{ formatMoney(earning.tienNhan) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-else-if="state.activeTab === 'courses'" class="teacher-view">
          <div class="teacher-page-header">
            <div>
              <h1 class="page-title">Khóa học của tôi</h1>
              <p class="page-sub">Danh sách khóa học do bạn tạo, giống khu quản lý cũ.</p>
            </div>
            <button class="btn-submit compact" @click="createCourse"><Plus :size="18" /> Thêm khóa học</button>
          </div>

          <label class="form-group teacher-delete-reason">
            <span>Lý do xóa nếu khóa có học viên</span>
            <input v-model="state.deleteReason" placeholder="Nhập lý do trước khi xóa khóa cần hoàn tiền" />
          </label>

          <div class="teacher-table-wrap">
            <table class="teacher-table">
              <thead>
                <tr>
                  <th width="92">Ảnh</th>
                  <th>Tên khóa học</th>
                  <th>Danh mục</th>
                  <th>Giá</th>
                  <th>Trạng thái</th>
                  <th width="230">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!state.courses.length">
                  <td colspan="6" class="teacher-empty">Chưa có khóa học nào được tạo.</td>
                </tr>
                <tr v-for="course in state.courses" :key="course.id">
                  <td>
                    <img v-if="course.hinhAnh" class="teacher-thumb" :src="imageUrl(course.hinhAnh)" :alt="course.tenKhoaHoc" />
                    <div v-else class="teacher-thumb empty">No image</div>
                  </td>
                  <td class="strong-cell">{{ course.tenKhoaHoc }}</td>
                  <td>{{ course.category?.tenDanhMuc || '-' }}</td>
                  <td>{{ formatMoney(course.gia) }}</td>
                  <td><span class="pill-soft">{{ statusText(course.trangThai) }}</span></td>
                  <td>
                    <div class="teacher-actions">
                      <button class="btn-outline" @click="openLessons(course)">Bài học</button>
                      <button class="btn-outline" @click="editCourse(course)"><Edit3 :size="15" /> Sửa</button>
                      <button class="btn-outline danger-text" @click="removeCourse(course)"><Trash2 :size="15" /> Xóa</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-else-if="state.activeTab === 'course-form'" class="teacher-view">
          <div class="teacher-page-header">
            <div>
              <button class="ghost-btn" @click="setTab('courses')"><ChevronLeft :size="18" /> Quay lại danh sách khóa học</button>
              <h1 class="page-title">{{ state.courseForm.id ? 'Cập nhật khóa học' : 'Tạo khóa học mới' }}</h1>
              <p class="page-sub">Sau khi tạo mới, khóa học sẽ chờ Admin duyệt như nghiệp vụ cũ.</p>
            </div>
          </div>

          <form class="teacher-form-card" @submit.prevent="saveCourse">
            <label class="form-group">
              <span>Tên khóa học</span>
              <input v-model="state.courseForm.tenKhoaHoc" required placeholder="Java Spring Boot cơ bản" />
            </label>
            <label class="form-group">
              <span>Mô tả</span>
              <textarea v-model="state.courseForm.moTa" rows="4" required placeholder="Mô tả ngắn về khóa học"></textarea>
            </label>
            <div class="two-cols">
              <label class="form-group">
                <span>Giá</span>
                <input v-model.number="state.courseForm.gia" type="number" min="0" required />
              </label>
              <label class="form-group">
                <span>Cấp độ</span>
                <select v-model="state.courseForm.capDo">
                  <option>Beginner</option>
                  <option>Intermediate</option>
                  <option>Advanced</option>
                </select>
              </label>
            </div>
            <label class="form-group">
              <span>Danh mục</span>
              <select v-model="state.courseForm.categoryId" required>
                <option value="">Chọn danh mục</option>
                <option v-for="cat in state.categories" :key="cat.id" :value="cat.id">{{ cat.tenDanhMuc }}</option>
              </select>
            </label>
            <label class="form-group">
              <span>Ảnh khóa học</span>
              <input type="file" accept="image/*" @change="uploadCourseImage" />
              <input v-model="state.courseForm.hinhAnh" placeholder="/uploads/images/course.jpg hoặc URL ảnh" />
            </label>
            <div class="button-row">
              <button class="btn-submit compact"><RefreshCw :size="18" /> {{ state.courseForm.id ? 'Cập nhật' : 'Gửi duyệt' }}</button>
              <button type="button" class="btn-outline" @click="resetCourseForm">Làm mới</button>
            </div>
          </form>
        </section>

        <section v-else-if="state.activeTab === 'lessons'" class="teacher-view">
          <div class="teacher-page-header">
            <div>
              <h1 class="page-title">Bài học{{ selectedCourse ? `: ${selectedCourse.tenKhoaHoc}` : '' }}</h1>
              <p class="page-sub">Quản lý nội dung và video bài học thuộc khóa đã chọn.</p>
            </div>
            <button class="btn-submit compact" :disabled="!selectedCourse" @click="createLesson"><Plus :size="18" /> Thêm bài học</button>
          </div>

          <label class="form-group teacher-course-select">
            <span>Chọn khóa học</span>
            <select v-model="state.selectedCourseId" @change="loadLessons">
              <option v-for="course in state.courses" :key="course.id" :value="course.id">{{ course.tenKhoaHoc }}</option>
            </select>
          </label>

          <div class="teacher-table-wrap">
            <table class="teacher-table">
              <thead>
                <tr>
                  <th width="90">Thứ tự</th>
                  <th>Tên bài học</th>
                  <th>Video</th>
                  <th width="160">Thời lượng</th>
                  <th width="150">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!selectedCourse">
                  <td colspan="5" class="teacher-empty">Chọn một khóa học để quản lý bài học.</td>
                </tr>
                <tr v-else-if="!state.lessons.length">
                  <td colspan="5" class="teacher-empty">Chưa có bài học nào được tạo.</td>
                </tr>
                <tr v-for="lesson in state.lessons" :key="lesson.id">
                  <td>{{ lesson.thuTu }}</td>
                  <td class="strong-cell">{{ lesson.tieuDe }}</td>
                  <td>
                    <a v-if="lesson.videoUrl" class="video-link" :href="imageUrl(lesson.videoUrl)" target="_blank">Xem video</a>
                    <span v-else class="muted-line">Chưa có video</span>
                  </td>
                  <td>{{ lesson.thoiLuongPhut || 0 }} phút</td>
                  <td>
                    <div class="teacher-actions">
                      <button class="btn-outline" @click="editLesson(lesson)">Sửa</button>
                      <button class="btn-outline danger-text" @click="removeLesson(lesson)">Xóa</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-else-if="state.activeTab === 'lesson-form'" class="teacher-view">
          <div class="teacher-page-header">
            <div>
              <button class="ghost-btn" @click="setTab('lessons')"><ChevronLeft :size="18" /> Quay lại danh sách bài học</button>
              <h1 class="page-title">{{ state.lessonForm.id ? 'Cập nhật bài học' : 'Thêm bài học mới' }}</h1>
              <p class="page-sub">{{ selectedCourse?.tenKhoaHoc || 'Chọn khóa học trước khi thêm bài học.' }}</p>
            </div>
          </div>

          <form class="teacher-form-card" @submit.prevent="saveLesson">
            <label class="form-group">
              <span>Tiêu đề</span>
              <input v-model="state.lessonForm.tieuDe" required placeholder="Bài 1: Giới thiệu" />
            </label>
            <div class="two-cols">
              <label class="form-group">
                <span>Thứ tự</span>
                <input v-model.number="state.lessonForm.thuTu" type="number" min="1" required />
              </label>
              <label class="form-group">
                <span>Thời lượng phút</span>
                <input v-model.number="state.lessonForm.thoiLuongPhut" type="number" min="0" />
              </label>
            </div>
            <label class="form-group">
              <span>Video bài học</span>
              <input type="file" accept="video/*" @change="uploadLessonVideo" />
              <input v-model="state.lessonForm.videoUrl" placeholder="/uploads/videos/lesson.mp4 hoặc URL YouTube" />
            </label>
            <div class="button-row">
              <button class="btn-submit compact" :disabled="!selectedCourse"><Upload :size="18" /> Lưu bài học</button>
              <button type="button" class="btn-outline" @click="resetLessonForm">Làm mới</button>
            </div>
          </form>
        </section>

        <section v-else-if="state.activeTab === 'reviews'" class="teacher-view">
          <div class="teacher-page-header">
            <div>
              <h1 class="page-title">Đánh giá học viên</h1>
              <p class="page-sub">Theo dõi phản hồi trên các khóa học của bạn.</p>
            </div>
          </div>
          <div class="teacher-table-wrap">
            <table class="teacher-table">
              <thead>
                <tr>
                  <th>Khóa học</th>
                  <th>Học viên</th>
                  <th>Số sao</th>
                  <th>Nội dung</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!state.reviews.length">
                  <td colspan="4" class="teacher-empty">Chưa có đánh giá nào.</td>
                </tr>
                <tr v-for="review in state.reviews" :key="review.id">
                  <td>{{ review.course?.tenKhoaHoc || '-' }}</td>
                  <td>{{ review.user?.hoTen || '-' }}</td>
                  <td class="money-cell">{{ review.soSao || 0 }} sao</td>
                  <td>{{ review.noiDung || '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-else-if="state.activeTab === 'reports'" class="teacher-view">
          <div class="teacher-page-header">
            <div>
              <h1 class="page-title">Báo cáo & doanh thu</h1>
              <p class="page-sub">Thống kê học viên đăng ký và thu nhập thực nhận.</p>
            </div>
          </div>
          <div class="teacher-stats compact">
            <div class="teacher-stat-card accent">
              <strong>{{ formatMoney(state.report?.totalEarning) }}</strong>
              <span>Tổng doanh thu</span>
            </div>
            <div class="teacher-stat-card">
              <strong>{{ state.report?.uniqueStudentsCount || 0 }}</strong>
              <span>Học viên duy nhất</span>
            </div>
            <div class="teacher-stat-card">
              <strong>{{ state.report?.registeredCount || 0 }}</strong>
              <span>Lượt đăng ký</span>
            </div>
          </div>
          <div class="teacher-table-wrap">
            <table class="teacher-table">
              <thead>
                <tr>
                  <th>Khóa học</th>
                  <th>Học viên</th>
                  <th>Tiến độ</th>
                  <th>Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!state.report?.enrollments?.length">
                  <td colspan="4" class="teacher-empty">Chưa có học viên đăng ký.</td>
                </tr>
                <tr v-for="enrollment in state.report?.enrollments || []" :key="enrollment.id">
                  <td>{{ enrollment.course?.tenKhoaHoc || '-' }}</td>
                  <td>{{ enrollment.user?.hoTen || '-' }}</td>
                  <td>{{ enrollment.tienDoPercent || 0 }}%</td>
                  <td>{{ enrollment.trangThai || '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-else-if="state.activeTab === 'notifications'" class="teacher-view">
          <div class="teacher-page-header">
            <div>
              <h1 class="page-title">Thông báo</h1>
              <p class="page-sub">Các cập nhật liên quan đến khóa học và hệ thống.</p>
            </div>
          </div>
          <div class="teacher-list">
            <button
              v-for="notification in state.notifications"
              :key="notification.id"
              class="teacher-notification"
              :class="{ unread: !notification.daDoc }"
              @click="readNotification(notification)"
            >
              <strong>{{ notification.tieuDe || notification.title || 'Thông báo' }}</strong>
              <span>{{ notification.noiDung || notification.content }}</span>
            </button>
            <div v-if="!state.notifications.length" class="teacher-empty block">Chưa có thông báo.</div>
          </div>
        </section>
      </template>
    </main>
  </section>
</template>
