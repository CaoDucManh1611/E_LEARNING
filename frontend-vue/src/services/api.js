const API_BASE = import.meta.env.VITE_API_BASE_URL
  || (window.location.port === '5173' ? 'http://localhost:8080' : window.location.origin)

async function request(path, options = {}) {
  const isFormData = options.body instanceof FormData
  const response = await fetch(`${API_BASE}${path}`, {
    credentials: 'include',
    headers: {
      ...(!isFormData ? { 'Content-Type': 'application/json' } : {}),
      ...(options.headers || {})
    },
    ...options
  })

  const text = await response.text()
  const data = text ? JSON.parse(text) : {}

  if (!response.ok) {
    const error = new Error(data.message || data.error || 'Có lỗi xảy ra khi gọi API')
    error.status = response.status
    error.data = data
    throw error
  }

  return data
}

export const api = {
  baseUrl: API_BASE,

  getMe: () => request('/api/v1/auth/me'),
  login: (payload) => request('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  register: (payload) => request('/api/v1/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  logout: () => request('/api/v1/auth/logout', { method: 'POST' }),

  getCourses: (params = {}) => {
    const query = new URLSearchParams(Object.entries(params).filter(([, value]) => value !== '' && value != null))
    return request(`/api/v1/courses${query.toString() ? `?${query}` : ''}`)
  },
  getCourse: (id) => request(`/api/v1/courses/${id}`),
  getSkills: () => request('/api/v1/skills'),
  getProfile: () => request('/api/v1/profile/me'),
  saveProfile: (payload) => request('/api/v1/profile', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  recommend: (payload) => request('/api/v1/recommend', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  chatAI: (message) => request('/api/v1/ai/chat', {
    method: 'POST',
    body: JSON.stringify({ message })
  }),
  uploadImage: (file) => {
    const form = new FormData()
    form.append('file', file)
    return request('/api/v1/uploads/images', { method: 'POST', body: form })
  },
  uploadVideo: (file) => {
    const form = new FormData()
    form.append('file', file)
    return request('/api/v1/uploads/videos', { method: 'POST', body: form })
  },

  getCart: () => request('/api/v1/cart'),
  addToCart: (courseId) => request(`/api/v1/cart/items/${courseId}`, { method: 'POST' }),
  removeFromCart: (courseId) => request(`/api/v1/cart/items/${courseId}`, { method: 'DELETE' }),
  applyCoupon: (couponCode) => request('/api/v1/cart/coupon', {
    method: 'POST',
    body: JSON.stringify({ couponCode })
  }),
  removeCoupon: () => request('/api/v1/cart/coupon', { method: 'DELETE' }),
  clearCart: () => request('/api/v1/cart', { method: 'DELETE' }),
  checkout: () => request('/api/v1/checkout', { method: 'POST' }),
  getCheckoutOrder: (orderId) => request(`/api/v1/checkout/${orderId}`),
  payOrder: (orderId) => request(`/api/v1/checkout/${orderId}/success`, { method: 'PUT' }),
  cancelOrder: (orderId) => request(`/api/v1/checkout/${orderId}/cancel`, { method: 'PUT' }),

  getStudentCourses: () => request('/api/v1/student/my-courses'),
  getLearning: (courseId) => request(`/api/v1/student/courses/${courseId}/learn`),
  toggleProgress: (payload) => request('/api/v1/student/progress/toggle', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  getLessonComments: (lessonId) => request(`/api/v1/lessons/${lessonId}/comments`),
  postLessonComment: (lessonId, payload) => request(`/api/v1/lessons/${lessonId}/comments`, {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  submitReview: (courseId, payload) => request(`/api/v1/student/courses/${courseId}/reviews`, {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  getStudentOrders: () => request('/api/v1/student/orders'),
  getInvoice: (orderId) => request(`/api/v1/student/orders/${orderId}/invoice`),
  requestRefund: (orderId, lyDo) => request(`/api/v1/student/orders/${orderId}/refund`, {
    method: 'POST',
    body: JSON.stringify({ lyDo })
  }),
  getCertificate: (enrollmentId) => request(`/api/v1/student/certificate/${enrollmentId}`),
  getNotifications: () => request('/api/v1/student/notifications'),
  markNotificationRead: (id) => request(`/api/v1/student/notifications/${id}/read`, { method: 'PUT' }),
  getMyNotifications: () => request('/api/v1/notifications'),
  readMyNotification: (id) => request(`/api/v1/notifications/${id}/read`, { method: 'PUT' }),
  deleteMyNotification: (id) => request(`/api/v1/notifications/${id}`, { method: 'DELETE' }),

  getTeacherDashboard: () => request('/api/v1/teacher/dashboard'),
  getTeacherCourses: () => request('/api/v1/teacher/courses'),
  createTeacherCourse: (payload) => request('/api/v1/teacher/courses', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  updateTeacherCourse: (id, payload) => request(`/api/v1/teacher/courses/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  }),
  deleteTeacherCourse: (id, reason = '') => request(`/api/v1/teacher/courses/${id}${reason ? `?reason=${encodeURIComponent(reason)}` : ''}`, { method: 'DELETE' }),
  getTeacherLessons: (courseId) => request(`/api/v1/teacher/courses/${courseId}/lessons`),
  createTeacherLesson: (courseId, payload) => request(`/api/v1/teacher/courses/${courseId}/lessons`, {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  updateTeacherLesson: (courseId, lessonId, payload) => request(`/api/v1/teacher/courses/${courseId}/lessons/${lessonId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  }),
  deleteTeacherLesson: (courseId, lessonId) => request(`/api/v1/teacher/courses/${courseId}/lessons/${lessonId}`, { method: 'DELETE' }),
  getTeacherNotifications: () => request('/api/v1/teacher/notifications'),
  readTeacherNotification: (id) => request(`/api/v1/teacher/notifications/${id}/read`, { method: 'PUT' }),
  getTeacherReviews: () => request('/api/v1/teacher/reviews'),
  getTeacherReports: () => request('/api/v1/teacher/reports'),
  getTeacherOwnedAndBoughtCourses: () => request('/api/v1/teacher/my-courses'),

  getAdminRevenue: () => request('/api/v1/admin/revenue'),
  getAdminCourses: () => request('/api/v1/admin/courses'),
  createAdminCourse: (payload) => request('/api/v1/admin/courses', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  updateAdminCourse: (id, payload) => request(`/api/v1/admin/courses/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  }),
  deleteAdminCourse: (id, reason = '') => request(`/api/v1/admin/courses/${id}${reason ? `?reason=${encodeURIComponent(reason)}` : ''}`, { method: 'DELETE' }),
  getAdminLessons: (courseId) => request(`/api/v1/admin/courses/${courseId}/lessons`),
  createAdminLesson: (courseId, payload) => request(`/api/v1/admin/courses/${courseId}/lessons`, {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  updateAdminLesson: (courseId, lessonId, payload) => request(`/api/v1/admin/courses/${courseId}/lessons/${lessonId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  }),
  deleteAdminLesson: (courseId, lessonId) => request(`/api/v1/admin/courses/${courseId}/lessons/${lessonId}`, { method: 'DELETE' }),
  getAdminCategories: () => request('/api/v1/admin/categories'),
  createAdminCategory: (payload) => request('/api/v1/admin/categories', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  updateAdminCategory: (id, payload) => request(`/api/v1/admin/categories/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  }),
  deleteAdminCategory: (id) => request(`/api/v1/admin/categories/${id}`, { method: 'DELETE' }),
  getAdminCoupons: () => request('/api/v1/admin/coupons'),
  saveAdminCoupon: (payload) => request('/api/v1/admin/coupons', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  deleteAdminCoupon: (id) => request(`/api/v1/admin/coupons/${id}`, { method: 'DELETE' }),
  getAdminUsers: () => request('/api/v1/admin/users'),
  changeUserRole: (id, role) => request(`/api/v1/admin/users/${id}/role`, {
    method: 'PUT',
    body: JSON.stringify({ role })
  }),
  toggleUserLock: (id) => request(`/api/v1/admin/users/${id}/lock`, { method: 'PUT' }),
  getAdminReviews: () => request('/api/v1/admin/reviews'),
  toggleReview: (id) => request(`/api/v1/admin/reviews/${id}/toggle`, { method: 'PUT' }),
  deleteReview: (id) => request(`/api/v1/admin/reviews/${id}`, { method: 'DELETE' }),
  approveCourse: (id) => request(`/api/v1/admin/courses/${id}/approve`, { method: 'POST' }),
  rejectCourse: (id) => request(`/api/v1/admin/courses/${id}/reject`, { method: 'POST' }),
  getAdminRefunds: () => request('/api/v1/admin/refunds'),
  approveRefund: (id) => request(`/api/v1/admin/refunds/${id}/approve`, { method: 'PUT' }),
  rejectRefund: (id) => request(`/api/v1/admin/refunds/${id}/reject`, { method: 'PUT' })
}
