<script setup>
import { computed, inject, ref, watch } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { Bell, BookOpen, GraduationCap, LogOut, ShoppingCart, ShieldCheck, UserRound, X } from 'lucide-vue-next'
import { api } from '../services/api'

const auth = inject('auth')
const refreshUser = inject('refreshUser')
const router = useRouter()
const open = ref(false)
const notifOpen = ref(false)
const notifications = ref([])
const unreadCount = ref(0)

const initials = computed(() => {
  if (!auth.user?.hoTen) return 'ED'
  return auth.user.hoTen.split(' ').slice(-2).map((part) => part[0]).join('').toUpperCase()
})

async function loadNotifications() {
  if (!auth.user) {
    notifications.value = []
    unreadCount.value = 0
    return
  }

  try {
    const result = await api.getMyNotifications()
    notifications.value = result.data || []
    unreadCount.value = result.unreadCount || 0
  } catch {
    notifications.value = []
    unreadCount.value = 0
  }
}

function normalizeNotificationUrl(url = '') {
  if (!url) return auth.user?.role === 'teacher' ? '/teacher' : auth.user?.role === 'admin' ? '/admin' : '/student'
  if (url.startsWith('/student/courses/') && url.includes('/learn')) {
    const id = url.split('/student/courses/')[1]?.split('/')[0]
    return id ? `/learn/${id}` : '/student'
  }
  if (url.startsWith('/student/courses/')) {
    const id = url.split('/student/courses/')[1]?.split('/')[0]
    return id ? `/courses/${id}` : '/courses'
  }
  if (url === '/student/orders') return '/student'
  if (url === '/teacher/dashboard' || url === '/teacher/courses' || url === '/teacher/notifications') return '/teacher'
  if (url === '/admin/courses' || url === '/admin/refunds' || url === '/admin/notifications') return '/admin'
  return url
}

async function openNotification(notification) {
  try {
    const result = await api.readMyNotification(notification.id)
    notification.daDoc = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    notifOpen.value = false
    router.push(normalizeNotificationUrl(result.url || notification.url))
  } catch {
    notifOpen.value = false
  }
}

async function deleteNotification(event, notification) {
  event.stopPropagation()
  try {
    await api.deleteMyNotification(notification.id)
    notifications.value = notifications.value.filter((item) => item.id !== notification.id)
    if (!notification.daDoc) unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch {
    // Keep the item visible if the server rejects deletion.
  }
}

async function logout() {
  await api.logout()
  await refreshUser()
  open.value = false
  notifOpen.value = false
  notifications.value = []
  unreadCount.value = 0
  router.push('/')
}

watch(() => auth.user?.id, loadNotifications, { immediate: true })
</script>

<template>
  <nav class="app-nav">
    <RouterLink class="logo" to="/">
      Edu<em>Recommend</em>
    </RouterLink>

    <div class="nav-links">
      <RouterLink class="nav-link" to="/">Trang chủ</RouterLink>
      <RouterLink class="nav-link" to="/courses">Khóa học</RouterLink>
      <RouterLink v-if="auth.user" class="nav-link" to="/student">Học tập</RouterLink>
      <RouterLink v-if="auth.user" class="nav-link" to="/recommend">Gợi ý AI</RouterLink>
      <RouterLink v-if="auth.user?.role === 'teacher'" class="nav-link" to="/teacher">Giảng viên</RouterLink>
      <RouterLink v-if="auth.user?.role === 'admin'" class="nav-link" to="/admin">Admin</RouterLink>
    </div>

    <div class="nav-actions">
      <div v-if="auth.user" class="notif-menu">
        <button class="icon-btn notif-btn" aria-label="Thông báo" @click="notifOpen = !notifOpen; open = false">
          <Bell :size="19" />
          <span v-if="unreadCount > 0" class="notif-badge">{{ unreadCount }}</span>
        </button>

        <div class="notif-dropdown" :class="{ show: notifOpen }">
          <div class="notif-dropdown-head">
            <strong>Thông báo</strong>
            <button class="text-btn" @click="loadNotifications">Làm mới</button>
          </div>
          <button
            v-for="notification in notifications.slice(0, 6)"
            :key="notification.id"
            class="notif-card"
            :class="{ unread: !notification.daDoc }"
            @click="openNotification(notification)"
          >
            <button class="notif-delete" aria-label="Xóa thông báo" @click="deleteNotification($event, notification)">
              <X :size="14" />
            </button>
            <span v-if="notification.sender" class="notif-sender">👤 {{ notification.sender.hoTen }}</span>
            <strong>{{ notification.tieuDe }}</strong>
            <p>{{ notification.noiDung }}</p>
            <small>{{ notification.createdAt || 'Gần đây' }}</small>
          </button>
          <div v-if="!notifications.length" class="notif-empty">Không có thông báo nào.</div>
        </div>
      </div>

      <RouterLink v-if="auth.user" class="icon-btn" to="/cart" aria-label="Giỏ hàng">
        <ShoppingCart :size="19" />
      </RouterLink>

      <template v-if="auth.user">
        <div class="user-menu">
          <button class="avatar-btn" @click="open = !open; notifOpen = false" aria-label="Tài khoản">
            {{ initials }}
          </button>
          <div class="dropdown-menu" :class="{ show: open }">
            <div class="dropdown-header">
              <div class="user-fullname">{{ auth.user.hoTen }}</div>
              <div class="user-email">{{ auth.user.email }}</div>
            </div>
            <RouterLink class="dropdown-item" to="/student" @click="open = false">
              <GraduationCap :size="17" /> Khóa học của tôi
            </RouterLink>
            <RouterLink class="dropdown-item" to="/recommend" @click="open = false">
              <BookOpen :size="17" /> Gợi ý lộ trình AI
            </RouterLink>
            <RouterLink v-if="auth.user.role === 'teacher'" class="dropdown-item" to="/teacher" @click="open = false">
              <BookOpen :size="17" /> Khu giảng viên
            </RouterLink>
            <RouterLink v-if="auth.user.role === 'admin'" class="dropdown-item" to="/admin" @click="open = false">
              <ShieldCheck :size="17" /> Quản trị
            </RouterLink>
            <button class="dropdown-item logout" @click="logout">
              <LogOut :size="17" /> Đăng xuất
            </button>
          </div>
        </div>
      </template>

      <template v-else>
        <RouterLink class="btn-auth btn-login" to="/login">
          <UserRound :size="16" /> Đăng nhập
        </RouterLink>
        <RouterLink class="btn-auth btn-register" to="/register">Đăng ký</RouterLink>
      </template>
    </div>
  </nav>
</template>
