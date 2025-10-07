<script setup lang="ts"></script>

<template>
  <div>
    <nav style="display:flex; gap:12px; align-items:center; margin-bottom:12px">
      <router-link to="/">Home</router-link>
      <router-link to="/projects">Projects</router-link>
      <router-link to="/defects">Defects</router-link>
      <div style="margin-left:auto">
        <template v-if="username">
          <span>{{ username }}</span>
          <button @click="logout">Logout</button>
        </template>
        <template v-else>
          <router-link to="/auth">Login/Register</router-link>
        </template>
      </div>
    </nav>
    <router-view />
  </div>
</template>

<style scoped></style>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api, { setAuthToken } from './api'

const username = ref<string | null>(null)
const router = useRouter()

async function loadMe() {
  try {
    const r = await api.get('/test/me')
    username.value = r.data.username
  } catch (e) {
    username.value = null
  }
}

function logout() {
  setAuthToken(null)
  localStorage.removeItem('jwt')
  username.value = null
  router.push('/auth')
}

onMounted(loadMe)
</script>
