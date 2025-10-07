<script setup lang="ts"></script>

<template>
  <div class="app-container">
    <nav class="app-nav-extra">
      <router-link to="/" class="text-sky-600 font-semibold">Home</router-link>
      <router-link to="/projects" class="text-sky-600 font-semibold">Projects</router-link>
      <router-link to="/defects" class="text-sky-600 font-semibold">Defects</router-link>
      <div class="ml-auto">
        <template v-if="username">
          <span class="mr-3">{{ username }}</span>
          <button class="btn" @click="logout">Logout</button>
        </template>
        <template v-else>
          <router-link to="/auth" class="btn-ghost">Login/Register</router-link>
        </template>
      </div>
    </nav>
    <div class="mt-4">
      <router-view />
    </div>
  </div>
</template>

<style scoped></style>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api, { setAuthToken } from './api'

const username = ref<string | null>(localStorage.getItem('username'))
const router = useRouter()

async function loadMe() {
  const token = localStorage.getItem('jwt')
  if (!token) { username.value = null; return }
  try {
    const r = await api.get('/test/me')
    username.value = r.data.username
    localStorage.setItem('username', username.value)
  } catch (e) {
    username.value = null
    localStorage.removeItem('username')
  }
}

function logout() {
  setAuthToken(null)
  localStorage.removeItem('jwt')
  username.value = null
  localStorage.removeItem('username')
  router.push('/auth')
}

onMounted(loadMe)
</script>
