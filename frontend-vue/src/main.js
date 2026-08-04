import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import './styles.css'

import HomeView from './views/HomeView.vue'
import CoursesView from './views/CoursesView.vue'
import CourseDetailView from './views/CourseDetailView.vue'
import LoginView from './views/LoginView.vue'
import RegisterView from './views/RegisterView.vue'
import CartView from './views/CartView.vue'
import StudentView from './views/StudentView.vue'
import TeacherView from './views/TeacherView.vue'
import AdminView from './views/AdminView.vue'
import LearnView from './views/LearnView.vue'
import RecommendView from './views/RecommendView.vue'
import { api } from './services/api'

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/home', name: 'home-alias', component: HomeView },
  { path: '/courses', name: 'courses', component: CoursesView },
  { path: '/courses/:id', name: 'course-detail', component: CourseDetailView },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/register', name: 'register', component: RegisterView },
  { path: '/cart', name: 'cart', component: CartView, meta: { auth: true } },
  { path: '/student', name: 'student', component: StudentView, meta: { auth: true } },
  { path: '/learn/:id', name: 'learn', component: LearnView, meta: { auth: true } },
  { path: '/recommend', name: 'recommend', component: RecommendView, meta: { auth: true } },
  { path: '/teacher', name: 'teacher', component: TeacherView, meta: { auth: true, role: 'teacher' } },
  { path: '/admin', name: 'admin', component: AdminView, meta: { auth: true, role: 'admin' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach(async (to) => {
  if (!to.meta.auth) return true

  try {
    const result = await api.getMe()
    const user = result.user
    if (to.meta.role && user?.role !== to.meta.role) {
      return user?.role === 'admin' ? '/admin' : user?.role === 'teacher' ? '/teacher' : '/student'
    }
    return true
  } catch {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

createApp(App).use(router).mount('#app')
