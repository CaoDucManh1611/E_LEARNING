<script setup>
import { computed, onMounted, reactive } from 'vue'
import { CheckCircle2, XCircle } from 'lucide-vue-next'
import { api } from '../services/api'
import StatTile from '../components/StatTile.vue'

const state = reactive({
  revenue: null,
  courses: [],
  categories: [],
  coupons: [],
  users: [],
  reviews: [],
  refunds: [],
  selectedCourseId: null,
  lessons: [],
  courseForm: { id: null, tenKhoaHoc: '', moTa: '', gia: 0, capDo: 'Beginner', hinhAnh: '', trangThai: 'active', commissionRate: 70, categoryId: '', teacherId: '' },
  lessonForm: { id: null, tieuDe: '', videoUrl: '', thuTu: 1, thoiLuongPhut: 0 },
  deleteReason: '',
  categoryForm: { tenDanhMuc: '' },
  couponForm: { maCode: '', loaiGiam: 'percent', giaTri: 10, soLuong: 20, ngayHetHan: '' },
  error: '',
  message: '',
  loading: true
})

const pendingCourses = computed(() => state.courses.filter((course) => course.trangThai === 'pending_review'))

function formatMoney(value) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
}

async function loadData() {
  state.loading = true
  state.error = ''
  try {
    const [revenue, courses, refunds, categories, coupons, users, reviews] = await Promise.all([
      api.getAdminRevenue(),
      api.getAdminCourses(),
      api.getAdminRefunds(),
      api.getAdminCategories(),
      api.getAdminCoupons(),
      api.getAdminUsers(),
      api.getAdminReviews()
    ])
    state.revenue = revenue
    state.courses = courses.data || []
    state.refunds = refunds.data || []
    state.categories = categories.data || []
    state.coupons = coupons.data || []
    state.users = users.data || []
    state.reviews = reviews.data || []
    if (!state.selectedCourseId && state.courses.length) state.selectedCourseId = state.courses[0].id
    await loadLessons()
  } catch (error) {
    state.error = error.message
  } finally {
    state.loading = false
  }
}

function resetCourseForm() {
  state.courseForm = { id: null, tenKhoaHoc: '', moTa: '', gia: 0, capDo: 'Beginner', hinhAnh: '', trangThai: 'active', commissionRate: 70, categoryId: '', teacherId: '' }
}

function editCourse(course) {
  state.courseForm = {
    id: course.id,
    tenKhoaHoc: course.tenKhoaHoc || '',
    moTa: course.moTa || '',
    gia: course.gia || 0,
    capDo: course.capDo || 'Beginner',
    hinhAnh: course.hinhAnh || '',
    trangThai: course.trangThai || 'active',
    commissionRate: course.commissionRate || 70,
    categoryId: course.category?.id || '',
    teacherId: course.teacher?.id || ''
  }
}

async function saveCourse() {
  const payload = { ...state.courseForm }
  const result = payload.id
    ? await api.updateAdminCourse(payload.id, payload)
    : await api.createAdminCourse(payload)
  state.message = result.message
  resetCourseForm()
  await loadData()
}

async function deleteCourse(course) {
  const result = await api.deleteAdminCourse(course.id, state.deleteReason)
  state.message = result.message
  state.deleteReason = ''
  if (state.selectedCourseId === course.id) state.selectedCourseId = null
  await loadData()
}

async function loadLessons() {
  state.lessons = []
  if (!state.selectedCourseId) return
  try {
    const result = await api.getAdminLessons(state.selectedCourseId)
    state.lessons = result.data || []
  } catch (error) {
    state.message = error.message
  }
}

function resetLessonForm() {
  state.lessonForm = { id: null, tieuDe: '', videoUrl: '', thuTu: 1, thoiLuongPhut: 0 }
}

function editLesson(lesson) {
  state.lessonForm = {
    id: lesson.id,
    tieuDe: lesson.tieuDe || '',
    videoUrl: lesson.videoUrl || '',
    thuTu: lesson.thuTu || 1,
    thoiLuongPhut: lesson.thoiLuongPhut || 0
  }
}

async function saveLesson() {
  if (!state.selectedCourseId) return
  const payload = { ...state.lessonForm }
  const result = payload.id
    ? await api.updateAdminLesson(state.selectedCourseId, payload.id, payload)
    : await api.createAdminLesson(state.selectedCourseId, payload)
  state.message = result.message
  resetLessonForm()
  await loadLessons()
}

async function deleteLesson(lesson) {
  const result = await api.deleteAdminLesson(state.selectedCourseId, lesson.id)
  state.message = result.message
  await loadLessons()
}

async function uploadAdminImage(event) {
  const file = event.target.files?.[0]
  if (!file) return
  const result = await api.uploadImage(file)
  state.courseForm.hinhAnh = result.path
  state.message = 'Đã tải ảnh khóa học.'
}

async function uploadAdminVideo(event) {
  const file = event.target.files?.[0]
  if (!file) return
  const result = await api.uploadVideo(file)
  state.lessonForm.videoUrl = result.path
  state.message = 'Đã tải video bài học.'
}

async function saveCategory() {
  const result = await api.createAdminCategory(state.categoryForm)
  state.message = result.message
  state.categoryForm.tenDanhMuc = ''
  await loadData()
}

async function deleteCategory(id) {
  const result = await api.deleteAdminCategory(id)
  state.message = result.message
  await loadData()
}

async function saveCoupon() {
  const payload = {
    ...state.couponForm,
    giaTri: Number(state.couponForm.giaTri),
    soLuong: Number(state.couponForm.soLuong),
    ngayHetHan: state.couponForm.ngayHetHan || null
  }
  const result = await api.saveAdminCoupon(payload)
  state.message = result.message
  state.couponForm = { maCode: '', loaiGiam: 'percent', giaTri: 10, soLuong: 20, ngayHetHan: '' }
  await loadData()
}

async function deleteCoupon(id) {
  const result = await api.deleteAdminCoupon(id)
  state.message = result.message
  await loadData()
}

async function changeRole(user) {
  const result = await api.changeUserRole(user.id, user.role)
  state.message = result.message
  await loadData()
}

async function toggleLock(id) {
  const result = await api.toggleUserLock(id)
  state.message = result.message
  await loadData()
}

async function toggleReview(id) {
  const result = await api.toggleReview(id)
  state.message = result.message
  await loadData()
}

async function deleteReview(id) {
  const result = await api.deleteReview(id)
  state.message = result.message
  await loadData()
}

async function approveCourse(id) {
  const result = await api.approveCourse(id)
  state.message = result.message
  await loadData()
}

async function rejectCourse(id) {
  const result = await api.rejectCourse(id)
  state.message = result.message
  await loadData()
}

async function approveRefund(id) {
  const result = await api.approveRefund(id)
  state.message = result.message
  await loadData()
}

async function rejectRefund(id) {
  const result = await api.rejectRefund(id)
  state.message = result.message
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <section class="container">
    <div class="page-header left">
      <h1 class="page-title">Bảng điều khiển Admin</h1>
      <p class="page-sub">Tổng quan doanh thu, duyệt khóa học và xử lý hoàn tiền.</p>
    </div>

    <div v-if="state.error" class="status-box error">{{ state.error }}</div>
    <div v-else-if="state.loading" class="status-box">Đang tải báo cáo...</div>
    <template v-else>
      <div class="stats-grid">
        <StatTile label="Doanh thu" :value="formatMoney(state.revenue.totalRevenue)" hint="Đơn đã thanh toán" />
        <StatTile label="Đơn hàng" :value="state.revenue.totalPaidOrders" hint="Thành công" />
        <StatTile label="Hoàn tiền" :value="formatMoney(state.revenue.totalRefunded)" hint="Đã xử lý" />
        <StatTile label="Doanh thu thuần" :value="formatMoney(state.revenue.totalSystemNet)" hint="Sau hoa hồng" />
      </div>

      <p v-if="state.message" class="form-message panel-message">{{ state.message }}</p>

      <div class="dashboard-grid">
        <section class="panel">
          <h2>Khóa học chờ duyệt</h2>
          <div v-if="!pendingCourses.length" class="empty-line">Không có khóa học đang chờ duyệt.</div>
          <div v-for="course in pendingCourses" :key="course.id" class="list-item static admin-row">
            <div>
              <strong>{{ course.tenKhoaHoc }}</strong>
              <span>{{ course.teacher?.hoTen || 'Giảng viên' }} · {{ formatMoney(course.gia) }}</span>
            </div>
            <div class="button-row slim">
              <button class="btn-outline success-text" @click="approveCourse(course.id)"><CheckCircle2 :size="16" /> Duyệt</button>
              <button class="btn-outline danger-text" @click="rejectCourse(course.id)"><XCircle :size="16" /> Từ chối</button>
            </div>
          </div>
        </section>

        <section class="panel">
          <h2>Yêu cầu hoàn tiền</h2>
          <div v-if="!state.refunds.length" class="empty-line">Chưa có yêu cầu hoàn tiền.</div>
          <div v-for="refund in state.refunds.slice(0, 8)" :key="refund.id" class="list-item static admin-row">
            <div>
              <strong>Yêu cầu #{{ refund.id }}</strong>
              <span>Đơn #{{ refund.order?.id }} · {{ refund.trangThai }}</span>
            </div>
            <div v-if="refund.trangThai === 'pending'" class="button-row slim">
              <button class="btn-outline success-text" @click="approveRefund(refund.id)">Duyệt</button>
              <button class="btn-outline danger-text" @click="rejectRefund(refund.id)">Từ chối</button>
            </div>
          </div>
        </section>
      </div>

      <section class="panel wide-panel">
        <h2>Đơn hàng gần đây</h2>
        <div v-if="!state.revenue.paidOrders.length" class="empty-line">Chưa có đơn thanh toán.</div>
        <div v-for="order in state.revenue.paidOrders.slice(0, 8)" :key="order.id" class="list-item static">
          <strong>Đơn #{{ order.id }}</strong>
          <span>{{ order.user?.hoTen }} · {{ formatMoney(order.tongTien) }} · {{ order.trangThai }}</span>
        </div>
      </section>

      <div class="dashboard-grid">
        <section class="panel">
          <h2>Quản lý khóa học</h2>
          <label class="form-group delete-reason">
            <span>Lý do xóa nếu hoàn tiền</span>
            <input v-model="state.deleteReason" placeholder="Nhập lý do khi xóa khóa đã có học viên" />
          </label>
          <div v-for="course in state.courses.slice(0, 10)" :key="course.id" class="list-item static admin-row">
            <button class="plain-row" @click="state.selectedCourseId = course.id; loadLessons()">
              <strong>{{ course.tenKhoaHoc }}</strong>
              <span>{{ course.category?.tenDanhMuc }} · {{ course.trangThai }} · {{ formatMoney(course.gia) }}</span>
            </button>
            <div class="button-row slim">
              <button class="btn-outline" @click="editCourse(course)">Sửa</button>
              <button class="btn-outline danger-text" @click="deleteCourse(course)">Xóa</button>
            </div>
          </div>
        </section>

        <section class="panel">
          <h2>{{ state.courseForm.id ? 'Cập nhật khóa học' : 'Tạo khóa học' }}</h2>
          <form class="stack-form" @submit.prevent="saveCourse">
            <label class="form-group">
              <span>Tên khóa</span>
              <input v-model="state.courseForm.tenKhoaHoc" required />
            </label>
            <label class="form-group">
              <span>Mô tả</span>
              <textarea v-model="state.courseForm.moTa" rows="3"></textarea>
            </label>
            <div class="two-cols">
              <label class="form-group">
                <span>Giá</span>
                <input v-model.number="state.courseForm.gia" type="number" min="0" />
              </label>
              <label class="form-group">
                <span>Trạng thái</span>
                <select v-model="state.courseForm.trangThai">
                  <option value="active">active</option>
                  <option value="pending_review">pending_review</option>
                  <option value="draft">draft</option>
                </select>
              </label>
            </div>
            <div class="two-cols">
              <label class="form-group">
                <span>Danh mục</span>
                <select v-model="state.courseForm.categoryId">
                  <option value="">Chọn danh mục</option>
                  <option v-for="cat in state.categories" :key="cat.id" :value="cat.id">{{ cat.tenDanhMuc }}</option>
                </select>
              </label>
              <label class="form-group">
                <span>Giảng viên</span>
                <select v-model="state.courseForm.teacherId">
                  <option value="">Không chọn</option>
                  <option v-for="user in state.users.filter(u => u.role === 'teacher')" :key="user.id" :value="user.id">{{ user.hoTen }}</option>
                </select>
              </label>
            </div>
            <div class="two-cols">
              <label class="form-group">
                <span>Cấp độ</span>
                <select v-model="state.courseForm.capDo">
                  <option>Beginner</option>
                  <option>Intermediate</option>
                  <option>Advanced</option>
                </select>
              </label>
              <label class="form-group">
                <span>Hoa hồng</span>
                <input v-model.number="state.courseForm.commissionRate" type="number" min="0" max="100" />
              </label>
            </div>
            <label class="form-group">
              <span>Ảnh</span>
              <input type="file" accept="image/*" @change="uploadAdminImage" />
              <input v-model="state.courseForm.hinhAnh" placeholder="/uploads/images/..." />
            </label>
            <div class="button-row">
              <button class="btn-submit compact">Lưu khóa</button>
              <button type="button" class="btn-outline" @click="resetCourseForm">Làm mới</button>
            </div>
          </form>
        </section>
      </div>

      <div class="dashboard-grid">
        <section class="panel">
          <h2>Bài học của khóa đang chọn</h2>
          <div v-if="!state.selectedCourseId" class="empty-line">Chọn khóa học để xem bài học.</div>
          <div v-for="lesson in state.lessons" :key="lesson.id" class="list-item static admin-row">
            <div>
              <strong>{{ lesson.thuTu }}. {{ lesson.tieuDe }}</strong>
              <span>{{ lesson.thoiLuongPhut || 0 }} phút · {{ lesson.videoUrl || 'Chưa có video' }}</span>
            </div>
            <div class="button-row slim">
              <button class="btn-outline" @click="editLesson(lesson)">Sửa</button>
              <button class="btn-outline danger-text" @click="deleteLesson(lesson)">Xóa</button>
            </div>
          </div>
        </section>

        <section class="panel">
          <h2>{{ state.lessonForm.id ? 'Cập nhật bài học' : 'Thêm bài học' }}</h2>
          <form class="stack-form" @submit.prevent="saveLesson">
            <label class="form-group">
              <span>Tiêu đề</span>
              <input v-model="state.lessonForm.tieuDe" required />
            </label>
            <div class="two-cols">
              <label class="form-group">
                <span>Thứ tự</span>
                <input v-model.number="state.lessonForm.thuTu" type="number" min="1" />
              </label>
              <label class="form-group">
                <span>Thời lượng</span>
                <input v-model.number="state.lessonForm.thoiLuongPhut" type="number" min="0" />
              </label>
            </div>
            <label class="form-group">
              <span>Video</span>
              <input type="file" accept="video/*" @change="uploadAdminVideo" />
              <input v-model="state.lessonForm.videoUrl" placeholder="/uploads/videos/... hoặc URL" />
            </label>
            <div class="button-row">
              <button class="btn-submit compact" :disabled="!state.selectedCourseId">Lưu bài học</button>
              <button type="button" class="btn-outline" @click="resetLessonForm">Làm mới</button>
            </div>
          </form>
        </section>
      </div>

      <div class="dashboard-grid">
        <section class="panel">
          <h2>Danh mục</h2>
          <form class="inline-form" @submit.prevent="saveCategory">
            <input v-model="state.categoryForm.tenDanhMuc" required placeholder="Tên danh mục mới" />
            <button class="btn-submit compact">Thêm</button>
          </form>
          <div v-for="cat in state.categories" :key="cat.id" class="list-item static admin-row">
            <strong>{{ cat.tenDanhMuc }}</strong>
            <button class="btn-outline danger-text" @click="deleteCategory(cat.id)">Xóa</button>
          </div>
        </section>

        <section class="panel">
          <h2>Mã giảm giá</h2>
          <form class="stack-form" @submit.prevent="saveCoupon">
            <div class="two-cols">
              <label class="form-group">
                <span>Mã</span>
                <input v-model="state.couponForm.maCode" required placeholder="SALE10" />
              </label>
              <label class="form-group">
                <span>Loại</span>
                <select v-model="state.couponForm.loaiGiam">
                  <option value="percent">percent</option>
                  <option value="fixed">fixed</option>
                </select>
              </label>
            </div>
            <div class="two-cols">
              <label class="form-group">
                <span>Giá trị</span>
                <input v-model.number="state.couponForm.giaTri" type="number" min="0" />
              </label>
              <label class="form-group">
                <span>Số lượng</span>
                <input v-model.number="state.couponForm.soLuong" type="number" min="1" />
              </label>
            </div>
            <label class="form-group">
              <span>Ngày hết hạn</span>
              <input v-model="state.couponForm.ngayHetHan" type="date" />
            </label>
            <button class="btn-submit compact">Lưu mã</button>
          </form>
          <div v-for="coupon in state.coupons.slice(0, 6)" :key="coupon.id" class="list-item static admin-row">
            <div>
              <strong>{{ coupon.maCode }}</strong>
              <span>{{ coupon.loaiGiam }} · {{ coupon.giaTri }} · còn {{ coupon.soLuong - coupon.daDung }}</span>
            </div>
            <button class="btn-outline danger-text" @click="deleteCoupon(coupon.id)">Xóa</button>
          </div>
        </section>
      </div>

      <div class="dashboard-grid">
        <section class="panel">
          <h2>Người dùng</h2>
          <div v-for="user in state.users.slice(0, 10)" :key="user.id" class="list-item static admin-row">
            <div>
              <strong>{{ user.hoTen }}</strong>
              <span>{{ user.email }} · {{ user.isLocked ? 'Đang khóa' : 'Hoạt động' }}</span>
            </div>
            <div class="button-row slim">
              <select v-model="user.role" class="compact-select" @change="changeRole(user)">
                <option value="student">student</option>
                <option value="teacher">teacher</option>
                <option value="admin">admin</option>
              </select>
              <button class="btn-outline" @click="toggleLock(user.id)">{{ user.isLocked ? 'Mở khóa' : 'Khóa' }}</button>
            </div>
          </div>
        </section>

        <section class="panel">
          <h2>Đánh giá</h2>
          <div v-for="review in state.reviews.slice(0, 10)" :key="review.id" class="list-item static">
            <strong>{{ review.course?.tenKhoaHoc || 'Khóa học' }} · {{ review.soSao }} sao</strong>
            <span>{{ review.user?.hoTen }} · {{ review.trangThai }}</span>
            <p class="muted-line">{{ review.noiDung }}</p>
            <div class="button-row">
              <button class="btn-outline" @click="toggleReview(review.id)">Ẩn/hiện</button>
              <button class="btn-outline danger-text" @click="deleteReview(review.id)">Xóa</button>
            </div>
          </div>
        </section>
      </div>
    </template>
  </section>
</template>
