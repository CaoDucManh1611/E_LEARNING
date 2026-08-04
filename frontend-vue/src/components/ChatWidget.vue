<script setup>
import { nextTick, reactive, ref } from 'vue'
import { Bot, MessageCircle, Send, X } from 'lucide-vue-next'
import { api } from '../services/api'

const open = ref(false)
const listRef = ref(null)
const state = reactive({
  input: '',
  loading: false,
  messages: [
    {
      role: 'ai',
      text: 'Xin chào! Tôi là trợ lý EduRecommend. Bạn có thể hỏi về khóa học, gợi ý AI, hoàn tiền, giảng viên hoặc đánh giá.'
    }
  ]
})

const suggestions = [
  'Chính sách hoàn tiền là gì?',
  'AI gợi ý lộ trình như thế nào?',
  'Giảng viên có chức năng gì?',
  'Quy định đánh giá khóa học?'
]

async function scrollToBottom() {
  await nextTick()
  if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
}

async function sendMessage(text = state.input) {
  const message = text.trim()
  if (!message || state.loading) return

  state.messages.push({ role: 'user', text: message })
  state.input = ''
  state.loading = true
  await scrollToBottom()

  try {
    const result = await api.chatAI(message)
    state.messages.push({ role: 'ai', text: result.reply || 'Tôi chưa có câu trả lời phù hợp.' })
  } catch {
    state.messages.push({ role: 'ai', text: 'Hệ thống chat đang bận, bạn thử lại sau nhé.' })
  } finally {
    state.loading = false
    await scrollToBottom()
  }
}
</script>

<template>
  <div class="chat-widget">
    <section class="chat-window" :class="{ show: open }">
      <header class="chat-head">
        <div>
          <Bot :size="22" />
          <div>
            <strong>Trợ lý AI EduRecommend</strong>
            <span>Hỏi đáp thông minh tự động</span>
          </div>
        </div>
        <button aria-label="Đóng chat" @click="open = false"><X :size="18" /></button>
      </header>

      <div ref="listRef" class="chat-messages">
        <div v-for="(message, index) in state.messages" :key="index" class="chat-message" :class="message.role">
          {{ message.text }}
        </div>
        <div v-if="state.loading" class="chat-message ai typing">Đang trả lời...</div>
      </div>

      <div class="chat-suggestions">
        <button v-for="item in suggestions" :key="item" @click="sendMessage(item)">{{ item }}</button>
      </div>

      <form class="chat-form" @submit.prevent="sendMessage()">
        <input v-model="state.input" placeholder="Nhập câu hỏi của bạn..." />
        <button aria-label="Gửi tin nhắn" :disabled="state.loading || !state.input.trim()">
          <Send :size="17" />
        </button>
      </form>
    </section>

    <button class="chat-bubble" aria-label="Mở chat" @click="open = !open">
      <MessageCircle v-if="!open" :size="27" />
      <X v-else :size="27" />
    </button>
  </div>
</template>
