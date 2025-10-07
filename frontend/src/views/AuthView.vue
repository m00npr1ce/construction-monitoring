<script setup lang="ts">
import { ref } from 'vue'
import api, { setAuthToken } from '../api'
import { useRouter } from 'vue-router'

const mode = ref<'login'|'register'>('login')
const username = ref('')
const password = ref('')
const router = useRouter()

async function submit() {
  try {
    if (mode.value === 'register') {
      await api.post('/auth/register', { username: username.value, password: password.value, role: 'ROLE_ENGINEER' })
      mode.value = 'login'
      return
    }
    const r = await api.post('/auth/login', { username: username.value, password: password.value })
    const token = r.data.token
    setAuthToken(token)
    localStorage.setItem('jwt', token)
    router.push('/projects')
  } catch (e) {
    alert('Error: ' + (e as any).response?.data || e)
  }
}
</script>

<template>
  <div>
    <h2>{{ mode === 'login' ? 'Login' : 'Register' }}</h2>
    <div>
      <label>Username<input v-model="username"/></label>
    </div>
    <div>
      <label>Password<input type="password" v-model="password"/></label>
    </div>
    <div>
      <button @click="submit">{{ mode === 'login' ? 'Login' : 'Register' }}</button>
      <button @click.prevent="mode = mode === 'login' ? 'register' : 'login'">Switch</button>
    </div>
  </div>
</template>
