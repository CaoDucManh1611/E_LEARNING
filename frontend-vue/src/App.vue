<script setup>
import { onMounted, provide, reactive } from 'vue'
import AppNav from './components/AppNav.vue'
import ChatWidget from './components/ChatWidget.vue'
import { api } from './services/api'

const auth = reactive({
  user: null,
  loading: true
})

async function refreshUser() {
  auth.loading = true
  try {
    const result = await api.getMe()
    auth.user = result.user
  } catch {
    auth.user = null
  } finally {
    auth.loading = false
  }
}

provide('auth', auth)
provide('refreshUser', refreshUser)

onMounted(refreshUser)
</script>

<template>
  <AppNav />
  <main>
    <RouterView />
  </main>
  <ChatWidget />
</template>
