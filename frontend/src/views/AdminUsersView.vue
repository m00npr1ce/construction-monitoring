<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import api from '../api'

type User = { id: number; username: string; role: string | null }
const users = ref<User[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const roles = ['ROLE_ADMIN','ROLE_MANAGER','ROLE_ENGINEER','ROLE_VIEWER']

async function loadUsers() {
  loading.value = true
  try {
    const r = await api.get('/users')
    users.value = r.data
  } catch (e: any) {
    error.value = 'Failed to load users: ' + (e.response?.data || e.message || e)
  } finally {
    loading.value = false
  }
}

async function setRole(user: User, role: string) {
  try {
    await api.put(`/users/${user.id}/role`, { role })
    user.role = role
  } catch (e: any) {
    error.value = 'Failed to set role: ' + (e.response?.data || e.message || e)
  }
}

async function removeUser(user: User) {
  if (!confirm(`Delete user ${user.username}?`)) return
  try {
    await api.delete(`/users/${user.id}`)
    users.value = users.value.filter(u => u.id !== user.id)
  } catch (e: any) {
    error.value = 'Failed to delete user: ' + (e.response?.data || e.message || e)
  }
}

onMounted(loadUsers)

const notAdmin = computed(() => {
  const token = window.localStorage.getItem('jwt') || ''
  const role = window.localStorage.getItem('role') || ''
  return !token || !role.includes('ROLE_ADMIN')
})
</script>

<template>
  <div>
    <h2 class="text-xl font-semibold mb-4">Admin • Users</h2>
    <div v-if="error" class="text-red-600 mb-2">{{ error }}</div>
    <div v-if="notAdmin" class="p-4 bg-red-50 border border-red-200 rounded mb-3">
      Доступ запрещён. Требуются права администратора.
    </div>
    <div v-if="loading">Loading...</div>
    <table v-else class="w-full border">
      <thead class="bg-gray-50">
        <tr>
          <th class="text-left p-2 border">ID</th>
          <th class="text-left p-2 border">Username</th>
          <th class="text-left p-2 border">Role</th>
          <th class="text-left p-2 border">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in users" :key="u.id" class="border-b">
          <td class="p-2 border">{{ u.id }}</td>
          <td class="p-2 border">{{ u.username }}</td>
          <td class="p-2 border">
            <select :value="u.role || ''" @change="setRole(u, ($event.target as HTMLSelectElement).value)" class="border p-1 rounded">
              <option value="">— no role —</option>
              <option v-for="r in roles" :key="r" :value="r">{{ r }}</option>
            </select>
          </td>
          <td class="p-2 border">
            <button class="btn btn-danger" @click="removeUser(u)">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>



