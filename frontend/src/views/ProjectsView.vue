<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../api'

const projects = ref<any[]>([])
const name = ref('')
const description = ref('')

async function load() {
  const r = await api.get('/projects')
  projects.value = r.data
}

async function createProject() {
  const r = await api.post('/projects', { name: name.value, description: description.value })
  projects.value.push(r.data)
  name.value = ''
  description.value = ''
}

onMounted(load)
</script>

<template>
  <div>
    <h2>Projects</h2>
    <div>
      <input v-model="name" placeholder="project name" />
      <input v-model="description" placeholder="description" />
      <button @click="createProject">Create</button>
    </div>
    <ul>
      <li v-for="p in projects" :key="p.id">{{ p.id }} — {{ p.name }}</li>
    </ul>
  </div>
</template>
