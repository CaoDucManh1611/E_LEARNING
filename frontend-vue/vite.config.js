import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '127.0.0.1',
    port: 5173
  },
  build: {
    outDir: '../Edu_Recommend/doan/src/main/resources/static',
    emptyOutDir: true
  }
})
