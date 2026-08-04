<script setup>
import { onMounted, reactive } from 'vue'
import { CreditCard, Tags, Trash2, XCircle } from 'lucide-vue-next'
import { api } from '../services/api'

const state = reactive({
  cartCourses: [],
  total: 0,
  discount: 0,
  discountedTotal: 0,
  appliedCoupon: null,
  couponCode: '',
  order: null,
  message: '',
  loading: true
})

function formatMoney(value) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
}

function syncCart(result) {
  state.cartCourses = result.cartCourses || []
  state.total = result.total || 0
  state.discount = result.discount || 0
  state.discountedTotal = result.discountedTotal || 0
  state.appliedCoupon = result.appliedCoupon
  state.message = result.message || ''
}

async function loadCart() {
  state.loading = true
  try {
    syncCart(await api.getCart())
  } catch (error) {
    state.message = error.message
  } finally {
    state.loading = false
  }
}

async function removeCourse(id) {
  syncCart(await api.removeFromCart(id))
}

async function applyCoupon() {
  try {
    syncCart(await api.applyCoupon(state.couponCode))
  } catch (error) {
    state.message = error.message
  }
}

async function removeCoupon() {
  syncCart(await api.removeCoupon())
  state.couponCode = ''
}

async function checkout() {
  try {
    const result = await api.checkout()
    state.order = result.order
    state.message = `${result.message} Mã đơn hàng #${result.order.id}`
    await loadCart()
  } catch (error) {
    state.message = error.message
  }
}

async function payOrder() {
  if (!state.order) return
  const result = await api.payOrder(state.order.id)
  state.order = result.order
  state.message = result.message
}

async function cancelOrder() {
  if (!state.order) return
  const result = await api.cancelOrder(state.order.id)
  state.order = result.order
  state.message = result.message
}

onMounted(loadCart)
</script>

<template>
  <section class="container">
    <div class="page-header left">
      <h1 class="page-title">Giỏ hàng</h1>
      <p class="page-sub">Kiểm tra khóa học, áp dụng mã giảm giá và tạo đơn thanh toán.</p>
    </div>

    <div class="cart-layout">
      <div class="cart-list">
        <div v-if="state.loading" class="status-box">Đang tải giỏ hàng...</div>
        <div v-else-if="!state.cartCourses.length" class="status-box">Giỏ hàng đang trống.</div>
        <div v-for="course in state.cartCourses" :key="course.id" class="cart-row">
          <div>
            <strong>{{ course.tenKhoaHoc }}</strong>
            <span>{{ course.category?.tenDanhMuc }} · {{ course.capDo }}</span>
          </div>
          <b>{{ formatMoney(course.gia) }}</b>
          <button class="icon-btn danger" @click="removeCourse(course.id)" aria-label="Xóa khóa học">
            <Trash2 :size="18" />
          </button>
        </div>

        <div v-if="state.order" class="panel order-result">
          <h2>Đơn hàng #{{ state.order.id }}</h2>
          <p>Trạng thái: <strong>{{ state.order.trangThai }}</strong></p>
          <div class="button-row">
            <button class="btn-submit compact" @click="payOrder"><CreditCard :size="18" /> Xác nhận thanh toán</button>
            <button class="btn-outline danger-text" @click="cancelOrder"><XCircle :size="18" /> Hủy đơn</button>
          </div>
        </div>
      </div>

      <aside class="summary-panel">
        <h2>Tổng thanh toán</h2>
        <form class="coupon-form" @submit.prevent="applyCoupon">
          <input v-model="state.couponCode" placeholder="Nhập mã giảm giá" />
          <button class="icon-btn" aria-label="Áp dụng mã"><Tags :size="18" /></button>
        </form>
        <button v-if="state.appliedCoupon" class="text-btn" @click="removeCoupon">
          Gỡ mã {{ state.appliedCoupon.maCode }}
        </button>

        <div class="summary-line"><span>Tạm tính</span><strong>{{ formatMoney(state.total) }}</strong></div>
        <div class="summary-line"><span>Giảm giá</span><strong>{{ formatMoney(state.discount) }}</strong></div>
        <div class="summary-total"><span>Cần trả</span><strong>{{ formatMoney(state.discountedTotal) }}</strong></div>
        <button class="btn-submit" :disabled="!state.cartCourses.length" @click="checkout">
          <CreditCard :size="18" /> Tạo đơn thanh toán
        </button>
        <p v-if="state.message" class="form-message">{{ state.message }}</p>
      </aside>
    </div>
  </section>
</template>
