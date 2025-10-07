<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../api'

const defects = ref<any[]>([])
const title = ref('')
const description = ref('')
const projectId = ref<number | null>(null)

async function load() {
  const r = await api.get('/defects')
  defects.value = r.data
}

async function createDefect() {
  if (!projectId.value) { alert('set projectId'); return }
  const r = await api.post('/defects', { title: title.value, description: description.value, projectId: projectId.value })
  defects.value.push(r.data)
  title.value = ''
  description.value = ''
}

onMounted(load)
</script>

<template>
  <div>
    <h2>Defects</h2>
    <div>
      <input v-model="title" placeholder="title" />
      <input v-model="description" placeholder="description" />
      <input v-model.number="projectId" placeholder="projectId" />
      <button @click="createDefect">Create</button>
    </div>
    <ul>
      <li v-for="d in defects" :key="d.id">{{ d.id }} — {{ d.title }} (project: {{ d.projectId }})</li>
    </ul>
  </div>
</template>
