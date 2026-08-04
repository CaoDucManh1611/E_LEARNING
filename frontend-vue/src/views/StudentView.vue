<script setup>
import { computed, onMounted, reactive } from 'vue'
import { RouterLink } from 'vue-router'
import { Award, Bell, BookOpen, FileText, ReceiptText, RotateCcw, ShoppingBag } from 'lucide-vue-next'
import { api } from '../services/api'

const state = reactive({
  activeTab: 'courses',
  courses: [],
  enrollments: [],
  orders: [],
  notifications: [],
  invoice: null,
  certificate: null,
  unreadCount: 0,
  refundReason: '',
  loading: true,
  error: '',
  message: ''
})

const completedCount = computed(() => state.enrollments.filter((item) => item.trangThai === 'completed' || item.tienDoPercent >= 100).length)

const tabs = [
  { key: 'courses', label: 'Khóa học của tôi', icon: BookOpen },
  { key: 'orders', label: 'Lịch sử đơn hàng', icon: ReceiptText },
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

function enrollmentFor(courseId) {
  return state.enrollments.find((item) => item.course?.id === courseId)
}

function orderStatus(status) {
  const map = {
    pending: 'Chờ thanh toán',
    paid: 'Đã thanh toán',
    refund_requested: 'Chờ hoàn tiền',
    refunded: 'Đã hoàn tiền',
    rejected: 'Từ chối',
    cancelled: 'Đã hủy'
  }
  return map[status] || status || 'Đang cập nhật'
}

async function loadData() {
  state.loading = true
  state.error = ''
  try {
    const [coursesResult, ordersResult, notificationsResult] = await Promise.allSettled([
      api.getStudentCourses(),
      api.getStudentOrders(),
      api.getNotifications()
    ])

    if (coursesResult.status === 'fulfilled') {
      state.courses = coursesResult.value.myCourses || []
      state.enrollments = coursesResult.value.enrollments || []
      state.unreadCount = coursesResult.value.unreadCount || 0
      state.notifications = coursesResult.value.notifications || []
    }
    if (ordersResult.status === 'fulfilled') state.orders = ordersResult.value.data || []
    if (notificationsResult.status === 'fulfilled') {
      state.notifications = notificationsResult.value.data || state.notifications
      state.unreadCount = notificationsResult.value.unreadCount ?? state.unreadCount
    }
  } catch (error) {
    state.error = error.message
  } finally {
    state.loading = false
  }
}

async function requestRefund(orderId) {
  try {
    const result = await api.requestRefund(orderId, state.refundReason || 'Học viên yêu cầu hoàn tiền')
    state.message = result.message
    state.refundReason = ''
    await loadData()
  } catch (error) {
    state.message = error.message
  }
}

async function viewInvoice(orderId) {
  try {
    const result = await api.getInvoice(orderId)
    state.invoice = result
    state.certificate = null
    state.message = `Đã tải hóa đơn #${result.invoice?.soHoaDon || orderId}`
  } catch (error) {
    state.message = error.message
  }
}

async function getCertificate(enrollmentId) {
  try {
    const result = await api.getCertificate(enrollmentId)
    state.certificate = result
    state.invoice = null
    state.message = `Chứng chỉ: ${result.certificate?.maXacThuc}`
  } catch (error) {
    state.message = error.message
  }
}

async function readNotification(notification) {
  try {
    await api.markNotificationRead(notification.id)
    notification.daDoc = true
    state.unreadCount = Math.max(0, state.unreadCount - 1)
  } catch (error) {
    state.message = error.message
  }
}

onMounted(loadData)
</script>

<template>
  <section class="student-page">
    <div class="student-header">
      <div>
        <h1 class="page-title">Khóa học của tôi</h1>
        <p class="page-sub">Theo dõi khóa học đã sở hữu, tiến độ học tập và lịch sử mua hàng.</p>
      </div>
      <RouterLink class="btn-submit compact" to="/">Khám phá thêm khóa học</RouterLink>
    </div>

    <div v-if="state.error" class="status-box error">{{ state.error }}</div>
    <div v-else-if="state.loading" class="status-box loading-line">Đang tải dữ liệu học viên...</div>

    <template v-else>
      <div class="student-summary">
        <div class="student-summary-card">
          <BookOpen :size="20" />
          <strong>{{ state.courses.length }}</strong>
          <span>Khóa học sở hữu</span>
        </div>
        <div class="student-summary-card accent">
          <Award :size="20" />
          <strong>{{ completedCount }}</strong>
          <span>Đã hoàn thành</span>
        </div>
        <div class="student-summary-card">
          <ShoppingBag :size="20" />
          <strong>{{ state.orders.length }}</strong>
          <span>Đơn hàng</span>
        </div>
        <div class="student-summary-card notify">
          <Bell :size="20" />
          <strong>{{ state.unreadCount }}</strong>
          <span>Thông báo mới</span>
        </div>
      </div>

      <div class="student-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="student-tab"
          :class="{ active: state.activeTab === tab.key }"
          @click="state.activeTab = tab.key"
        >
          <component :is="tab.icon" :size="17" />
          {{ tab.label }}
        </button>
      </div>

      <p v-if="state.message" class="form-message panel-message">{{ state.message }}</p>

      <section v-if="state.activeTab === 'courses'" class="student-section">
        <div v-if="!state.courses.length" class="student-empty">
          <BookOpen :size="44" />
          <strong>Bạn chưa sở hữu khóa học nào.</strong>
          <RouterLink class="btn-submit compact" to="/">Xem khóa học</RouterLink>
        </div>

        <div v-else class="student-course-grid">
          <article v-for="course in state.courses" :key="course.id" class="student-course-card">
            <RouterLink class="student-course-media" :to="`/learn/${course.id}`">
              <img v-if="course.hinhAnh" :src="imageUrl(course.hinhAnh)" :alt="course.tenKhoaHoc" />
              <div v-else class="student-course-placeholder">EduRecommend</div>
            </RouterLink>
            <div class="student-course-body">
              <span class="student-course-level">{{ course.capDo || 'Beginner' }}</span>
              <h2>{{ course.tenKhoaHoc }}</h2>
              <p>{{ course.category?.tenDanhMuc || 'Khóa học' }}</p>
              <div class="student-progress">
                <span>{{ enrollmentFor(course.id)?.tienDoPercent || 0 }}%</span>
                <div><i :style="{ width: `${enrollmentFor(course.id)?.tienDoPercent || 0}%` }"></i></div>
              </div>
              <div class="student-course-actions">
                <RouterLink class="btn-learn" :to="`/learn/${course.id}`">Vào học</RouterLink>
                <button
                  v-if="(enrollmentFor(course.id)?.tienDoPercent || 0) >= 100"
                  class="btn-outline"
                  @click="getCertificate(enrollmentFor(course.id).id)"
                >
                  Chứng chỉ
                </button>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section v-else-if="state.activeTab === 'orders'" class="student-section">
        <label class="form-group student-refund-note">
          <span>Lý do hoàn tiền</span>
          <input v-model="state.refundReason" placeholder="Nhập lý do trước khi bấm hoàn tiền" />
        </label>

        <div v-if="!state.orders.length" class="student-empty">
          <ReceiptText :size="44" />
          <strong>Chưa có đơn hàng.</strong>
        </div>

        <article v-for="item in state.orders" :key="item.order.id" class="student-order-card">
          <div class="student-order-head">
            <div>
              <strong>Đơn hàng #{{ item.order.id }}</strong>
              <span>{{ item.order.createdAt || 'Gần đây' }}</span>
            </div>
            <span class="student-order-badge" :class="item.order.trangThai">{{ orderStatus(item.order.trangThai) }}</span>
          </div>

          <div class="student-order-items">
            <div v-for="orderItem in item.order.items || item.items || []" :key="orderItem.id" class="student-order-item">
              <div class="student-item-icon"><BookOpen :size="18" /></div>
              <div>
                <strong>{{ orderItem.course?.tenKhoaHoc || orderItem.tenKhoaHoc || 'Khóa học' }}</strong>
                <span>{{ formatMoney(orderItem.gia || orderItem.price) }}</span>
              </div>
            </div>
          </div>

          <div class="student-order-foot">
            <strong>{{ formatMoney(item.order.tongTien) }}</strong>
            <div class="student-order-actions">
              <button v-if="item.canRefund" class="btn-outline" @click="requestRefund(item.order.id)">
                <RotateCcw :size="16" /> Hoàn tiền
              </button>
              <button class="btn-outline" @click="viewInvoice(item.order.id)">
                <FileText :size="16" /> Hóa đơn
              </button>
              <span v-if="!item.canRefund && item.refundRequest" class="pill-soft">{{ item.refundRequest.trangThai }}</span>
            </div>
          </div>
        </article>
      </section>

      <section v-else class="student-section">
        <div class="student-notification-list">
          <button
            v-for="notification in state.notifications"
            :key="notification.id"
            class="student-notification"
            :class="{ unread: !notification.daDoc }"
            @click="readNotification(notification)"
          >
            <strong>{{ notification.tieuDe || notification.title || 'Thông báo' }}</strong>
            <span>{{ notification.noiDung || notification.content }}</span>
          </button>
          <div v-if="!state.notifications.length" class="student-empty">
            <Bell :size="44" />
            <strong>Chưa có thông báo.</strong>
          </div>
        </div>
      </section>

      <div v-if="state.invoice || state.certificate" class="student-result-card">
        <template v-if="state.invoice">
          <strong>Hóa đơn {{ state.invoice.invoice?.soHoaDon }}</strong>
          <span>Đơn #{{ state.invoice.order?.id }} · {{ formatMoney(state.invoice.order?.tongTien) }}</span>
        </template>
        <template v-else>
          <strong>Chứng chỉ {{ state.certificate.certificate?.maXacThuc }}</strong>
          <span>{{ state.certificate.enrollment?.course?.tenKhoaHoc }}</span>
        </template>
      </div>
    </template>
  </section>
</template>
