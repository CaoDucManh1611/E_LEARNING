<script setup>
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { api } from '../services/api'

const router = useRouter()
const form = reactive({ hoTen: '', email: '', passwordHash: '', soDienThoai: '' })
const message = ref('')

async function submit() {
  message.value = ''
  try {
    await api.register(form)
    router.push('/login')
  } catch (error) {
    message.value = error.message
  }
}
</script>

<template>
  <section class="auth-page">
    <form class="auth-container" @submit.prevent="submit">
      <div class="logo auth-logo">Edu<em>Recommend</em></div>
      <h1 class="auth-title">Tạo tài khoản học viên</h1>
      <div v-if="message" class="alert">{{ message }}</div>
      <label class="form-group">
        <span>Họ tên</span>
        <input v-model="form.hoTen" required placeholder="Nguyễn Văn A" />
      </label>
      <label class="form-group">
        <span>Email</span>
        <input v-model="form.email" type="email" required placeholder="email@gmail.com" />
      </label>
      <label class="form-group">
        <span>Số điện thoại</span>
        <input v-model="form.soDienThoai" placeholder="09..." />
      </label>
      <label class="form-group">
        <span>Mật khẩu</span>
        <input v-model="form.passwordHash" type="password" required placeholder="••••••••" />
      </label>
      <button class="btn-submit">Đăng ký</button>
      <p class="auth-footer">Đã có tài khoản? <RouterLink to="/login">Đăng nhập</RouterLink></p>
    </form>
  </section>
</template>
