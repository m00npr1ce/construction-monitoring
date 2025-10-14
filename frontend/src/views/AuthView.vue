<script setup lang="ts">
import { ref } from 'vue'
import api, { setAuthToken } from '../api'
import { useRouter } from 'vue-router'

const mode = ref<'login'|'register'>('login')
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref<string | null>(null)
const router = useRouter()

async function submit() {
  error.value = null
  loading.value = true
  
  try {
    if (mode.value === 'register') {
      if (!username.value.trim() || !password.value) {
        error.value = 'Username and password are required'
        return
      }
      if (password.value !== confirmPassword.value) {
        error.value = 'Passwords do not match'
        return
      }
      if (password.value.length < 6) {
        error.value = 'Password must be at least 6 characters'
        return
      }
      
      await api.post('/auth/register', { 
        username: username.value.trim(), 
        password: password.value 
      })
      error.value = 'Registration successful! Please login.'
      mode.value = 'login'
      username.value = ''
      password.value = ''
      confirmPassword.value = ''
      return
    }
    
    if (!username.value.trim() || !password.value) {
      error.value = 'Username and password are required'
      return
    }
    
    const r = await api.post('/auth/login', { 
      username: username.value.trim(), 
      password: password.value 
    })
    const token = r.data.token
    setAuthToken(token)
    localStorage.setItem('jwt', token)
    
    // fetch user info and store username so navbar updates immediately
    try {
      const me = await api.get('/test/me')
      localStorage.setItem('username', me.data.username)
      localStorage.setItem('role', (me.data.roles || []).join(','))
    } catch (err) {
      // ignore
    }
    router.push('/projects')
  } catch (e: any) {
    error.value = 'Error: ' + (e.response?.data || e.message || e)
  } finally {
    loading.value = false
  }
}

function switchMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login'
  error.value = null
  username.value = ''
  password.value = ''
  confirmPassword.value = ''
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          {{ mode === 'login' ? 'Войдите в аккаунт' : 'Создайте новый аккаунт' }}
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          {{ mode === 'login' ? 'Нет аккаунта?' : 'Уже есть аккаунт?' }}
          <button @click="switchMode" class="font-medium text-blue-600 hover:text-blue-500">
            {{ mode === 'login' ? 'Зарегистрироваться' : 'Войти' }}
          </button>
        </p>
      </div>
      
      <form @submit.prevent="submit" class="mt-8 space-y-6">
        <div class="space-y-4">
          <div>
            <label for="username" class="block text-sm font-medium text-gray-700">Имя пользователя</label>
            <input 
              id="username" 
              v-model="username" 
              type="text" 
              required 
              class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              placeholder="Введите имя пользователя"
            />
          </div>
          
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700">Пароль</label>
            <input 
              id="password" 
              v-model="password" 
              type="password" 
              required 
              class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              placeholder="Введите пароль"
            />
          </div>
          
          <div v-if="mode === 'register'">
            <label for="confirmPassword" class="block text-sm font-medium text-gray-700">Подтвердите пароль</label>
            <input 
              id="confirmPassword" 
              v-model="confirmPassword" 
              type="password" 
              required 
              class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              placeholder="Подтвердите пароль"
            />
          </div>
        </div>

        <div v-if="error" class="text-red-600 text-sm text-center">
          {{ error }}
        </div>

        <div>
          <button 
            type="submit" 
            :disabled="loading"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
          >
            {{ loading ? 'Обработка...' : (mode === 'login' ? 'Войти' : 'Зарегистрироваться') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
