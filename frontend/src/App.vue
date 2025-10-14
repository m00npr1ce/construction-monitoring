<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api, { setAuthToken } from './api'

const username = ref<string | null>(localStorage.getItem('username'))
const role = ref<string | null>(localStorage.getItem('role'))
const router = useRouter()

const isAdmin = computed(() => {
  const token = localStorage.getItem('jwt') || ''
  const stored = role.value ?? localStorage.getItem('role') ?? ''
  return !!token && typeof stored === 'string' && stored.includes('ROLE_ADMIN')
})

async function loadMe() {
  const token = localStorage.getItem('jwt')
  if (!token) { 
    username.value = null
    role.value = null
    return 
  }
  try {
    // восстанавливаем auth заголовок после перезагрузки страницы
    setAuthToken(token)
    const r = await api.get('/test/me')
    username.value = r.data.username
    role.value = (r.data.roles || []).join(',')
    localStorage.setItem('username', username.value || '')
    localStorage.setItem('role', role.value || '')
  } catch (e) {
    // Не очищаем роль/имя при временной ошибке, чтобы не скрывать Admin UI
    // Только сбросим локальные реактивные значения, localStorage оставим
    username.value = localStorage.getItem('username')
    role.value = localStorage.getItem('role')
  }
}

function logout() {
  setAuthToken(null)
  localStorage.removeItem('jwt')
  username.value = null
  role.value = null
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  router.push('/auth')
}

onMounted(loadMe)
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Navigation -->
    <nav class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <div class="flex-shrink-0 flex items-center gap-2">
              <h1 class="text-xl font-bold text-gray-900">Мониторинг строительства</h1>
              <span v-if="isAdmin" class="px-2 py-0.5 text-xs rounded bg-red-100 text-red-700 border border-red-200">АДМИН</span>
            </div>
            <div class="hidden sm:ml-6 sm:flex sm:space-x-8">
              <router-link 
                to="/" 
                class="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-gray-500 hover:text-gray-700 hover:border-gray-300"
                active-class="border-blue-500 text-gray-900"
              >
                Главная
              </router-link>
              <router-link 
                to="/projects" 
                class="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-gray-500 hover:text-gray-700 hover:border-gray-300"
                active-class="border-blue-500 text-gray-900"
              >
                Проекты
              </router-link>
              <router-link 
                to="/defects" 
                class="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-gray-500 hover:text-gray-700 hover:border-gray-300"
                active-class="border-blue-500 text-gray-900"
              >
                Дефекты
              </router-link>
              <router-link
                to="/admin/users" 
                class="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-red-600 hover:text-red-700 hover:border-red-300"
                active-class="border-red-500 text-red-900"
              >
                Админ
              </router-link>
            </div>
          </div>
          
          <div class="flex items-center">
            <template v-if="username">
              <div class="flex items-center space-x-4">
                <div class="text-sm">
                  <div class="font-medium text-gray-900">{{ username }}</div>
                  <div v-if="role" class="text-gray-500">{{ role }}</div>
                </div>
                <router-link
                  to="/admin/users"
                  class="hidden sm:inline-flex bg-gray-100 text-red-700 border border-red-200 px-3 py-2 rounded-md text-sm font-medium hover:bg-gray-200"
                >
                  Админ
                </router-link>
                <router-link
                  to="/admin/users"
                  class="sm:hidden bg-red-600 hover:bg-red-700 text-white px-3 py-2 rounded-md text-sm font-medium"
                >
                  Админ
                </router-link>
                <button 
                  @click="logout"
                  class="bg-red-600 hover:bg-red-700 text-white px-3 py-2 rounded-md text-sm font-medium"
                >
                  Выйти
                </button>
              </div>
            </template>
            <template v-else>
              <router-link 
                to="/auth" 
                class="bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded-md text-sm font-medium"
              >
                Войти / Регистрация
              </router-link>
            </template>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
/* Custom styles if needed */
</style>
