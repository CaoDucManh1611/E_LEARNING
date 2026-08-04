<script setup>
import { inject, reactive, ref } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { api } from '../services/api'

const route = useRoute()
const router = useRouter()
const refreshUser = inject('refreshUser')
const form = reactive({ email: '', password: '' })
const message = ref('')

async function submit() {
  message.value = ''
  try {
    await api.login(form)
    await refreshUser()
    router.push(route.query.redirect || '/')
  } catch (error) {
    message.value = error.message
  }
}
</script>

<template>
  <section class="auth-page">
    <form class="auth-container" @submit.prevent="submit">
      <div class="logo auth-logo">Edu<em>Recommend</em></div>
      <h1 class="auth-title">Đăng nhập tài khoản học tập</h1>
      <div v-if="message" class="alert">{{ message }}</div>
      <label class="form-group">
        <span>Email</span>
        <input v-model="form.email" type="email" required placeholder="student@gmail.com" />
      </label>
      <label class="form-group">
        <span>Mật khẩu</span>
        <input v-model="form.password" type="password" required placeholder="••••••••" />
      </label>
      <button class="btn-submit">Đăng nhập</button>
      <p class="auth-footer">Chưa có tài khoản? <RouterLink to="/register">Đăng ký ngay</RouterLink></p>
    </form>
  </section>
</template>
