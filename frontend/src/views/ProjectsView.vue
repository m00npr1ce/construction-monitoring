<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import api from '../api'

const projects = ref<any[]>([])
const searchQuery = ref('')
const sortKey = ref<'name'|'startDate'|'endDate'|'id'>('id')
const sortDir = ref<'asc'|'desc'>('asc')
const name = ref('')
const description = ref('')
const startDate = ref('')
const endDate = ref('')
const loading = ref(false)
const error = ref<string | null>(null)

// Details/Edit state
const showDetails = ref(false)
const showEdit = ref(false)
const selectedProject = ref<any | null>(null)
const editForm = ref({ id: 0, name: '', description: '', startDate: '', endDate: '' })

async function load() {
  loading.value = true
  try {
    const r = await api.get('/projects')
    projects.value = r.data
  } catch (e: any) {
    error.value = 'Failed to load projects: ' + (e.response?.data || e.message || e)
  } finally {
    loading.value = false
  }
}

async function createProject() {
  error.value = null
  if (!name.value.trim()) {
    error.value = 'Project name is required'
    return
  }
  
  try {
    const payload: any = { 
      name: name.value.trim(), 
      description: description.value.trim() 
    }
    if (startDate.value) payload.startDate = startDate.value
    if (endDate.value) payload.endDate = endDate.value
    
    const r = await api.post('/projects', payload)
    projects.value.push(r.data)
    resetForm()
  } catch (e: any) {
    error.value = 'Failed to create project: ' + (e.response?.data || e.message || e)
  }
}

function resetForm() {
  name.value = ''
  description.value = ''
  startDate.value = ''
  endDate.value = ''
}

onMounted(load)

const canManageProjects = computed(() => {
  const role = window.localStorage.getItem('role') || ''
  return role.includes('ROLE_MANAGER') || role.includes('ROLE_ADMIN')
})

const displayedProjects = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  let list = projects.value
  if (q) {
    list = list.filter((p: any) =>
      (p.name || '').toLowerCase().includes(q) ||
      (p.description || '').toLowerCase().includes(q)
    )
  }
  const key = sortKey.value
  const dir = sortDir.value === 'asc' ? 1 : -1
  return [...list].sort((a: any, b: any) => {
    const av = a[key] ?? ''
    const bv = b[key] ?? ''
    if (av === bv) return 0
    return (av > bv ? 1 : -1) * dir
  })
})

function viewDetails(project: any) {
  console.log('viewDetails clicked', project)
  selectedProject.value = project
  showDetails.value = true
}

function openEdit(project: any) {
  console.log('openEdit clicked', project)
  editForm.value = {
    id: project.id,
    name: project.name || '',
    description: project.description || '',
    startDate: project.startDate || '',
    endDate: project.endDate || ''
  }
  showEdit.value = true
}

async function saveEdit() {
  try {
    console.log('saveEdit submitting', editForm.value)
    const payload: any = {
      name: editForm.value.name.trim(),
      description: editForm.value.description.trim(),
      startDate: editForm.value.startDate || null,
      endDate: editForm.value.endDate || null
    }
    const r = await api.put(`/projects/${editForm.value.id}`, payload)
    const idx = projects.value.findIndex((p: any) => p.id === editForm.value.id)
    if (idx !== -1) projects.value[idx] = r.data
    showEdit.value = false
  } catch (e: any) {
    error.value = 'Failed to update project: ' + (e.response?.data || e.message || e)
    console.error('saveEdit error', e)
  }
}
</script>

<template>
  <div>
    <h2 class="text-xl font-semibold mb-2">Управление проектами</h2>
    <div class="mb-4 flex flex-col md:flex-row gap-2 md:items-center">
      <input v-model="searchQuery" placeholder="Поиск по названию или описанию" class="border p-2 rounded w-full md:w-1/2" />
      <div class="flex gap-2 items-center">
        <label class="text-sm text-gray-600">Сортировка:</label>
        <select v-model="sortKey" class="border p-2 rounded">
          <option value="id">ID</option>
          <option value="name">Название</option>
          <option value="startDate">Дата начала</option>
          <option value="endDate">Дата окончания</option>
        </select>
        <select v-model="sortDir" class="border p-2 rounded">
          <option value="asc">По возрастанию</option>
          <option value="desc">По убыванию</option>
        </select>
      </div>
    </div>
    
    <!-- Create Project Form -->
    <div class="mb-6 p-4 bg-blue-50 rounded-lg">
      <h3 class="font-semibold mb-3">Создать новый проект</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium mb-1">Название проекта *</label>
          <input v-model="name" placeholder="Название проекта" class="border p-2 rounded w-full" />
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Описание</label>
          <textarea v-model="description" placeholder="Описание проекта" class="border p-2 rounded w-full" rows="3"></textarea>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Дата начала</label>
          <input v-model="startDate" type="date" class="border p-2 rounded w-full" />
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Дата окончания</label>
          <input v-model="endDate" type="date" class="border p-2 rounded w-full" />
        </div>
        <div class="flex items-end">
          <button @click="createProject" class="btn bg-blue-600 text-white w-full" v-if="canManageProjects">Создать проект</button>
        </div>
      </div>
      <div v-if="error" class="text-red-600 mt-2">{{ error }}</div>
    </div>

    <!-- Projects List -->
    <div class="space-y-3">
      <div v-if="loading" class="text-center py-4">Загрузка...</div>
      <div v-else-if="projects.length === 0" class="text-center py-8 text-gray-500">
        Проекты не найдены
      </div>
      <div v-else>
        <div v-for="p in displayedProjects" :key="p.id" class="p-4 border rounded-lg bg-white shadow-sm">
          <div class="flex justify-between items-start">
            <div>
              <div class="font-semibold text-lg">#{{ p.id }} — {{ p.name }}</div>
              <div v-if="p.description" class="text-gray-700 mt-1">{{ p.description }}</div>
              <div class="text-sm text-gray-500 mt-2">
                <span v-if="p.startDate">Start: {{ new Date(p.startDate).toLocaleDateString() }}</span>
                <span v-if="p.startDate && p.endDate"> | </span>
                <span v-if="p.endDate">End: {{ new Date(p.endDate).toLocaleDateString() }}</span>
              </div>
            </div>
            <div class="flex gap-2">
              <button class="btn bg-green-600 text-white text-sm" @click="viewDetails(p)">Подробнее</button>
              <button class="btn bg-yellow-600 text-white text-sm" @click="openEdit(p)" v-if="canManageProjects">Изменить</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Details Modal -->
    <div v-if="showDetails && selectedProject" class="fixed inset-0 bg-black/40 flex items-center justify-center p-4">
      <div class="bg-white rounded-lg shadow-lg w-full max-w-lg p-5">
        <div class="flex justify-between items-center mb-3">
          <h3 class="text-lg font-semibold">Детали проекта</h3>
          <button class="btn-ghost" @click="showDetails = false">✕</button>
        </div>
        <div class="space-y-2">
          <div><span class="font-medium">ID:</span> {{ selectedProject.id }}</div>
          <div><span class="font-medium">Название:</span> {{ selectedProject.name }}</div>
          <div v-if="selectedProject.description"><span class="font-medium">Описание:</span> {{ selectedProject.description }}</div>
          <div class="text-sm text-gray-600">
            <span v-if="selectedProject.startDate">Начало: {{ new Date(selectedProject.startDate).toLocaleDateString() }}</span>
            <span v-if="selectedProject.startDate && selectedProject.endDate"> | </span>
            <span v-if="selectedProject.endDate">Окончание: {{ new Date(selectedProject.endDate).toLocaleDateString() }}</span>
          </div>
        </div>
        <div class="mt-4 text-right">
          <button class="btn btn-primary" @click="showDetails = false">Закрыть</button>
        </div>
      </div>
    </div>

    <!-- Edit Modal -->
    <div v-if="showEdit" class="fixed inset-0 bg-black/40 flex items-center justify-center p-4">
      <div class="bg-white rounded-lg shadow-lg w-full max-w-lg p-5">
        <div class="flex justify-between items-center mb-3">
          <h3 class="text-lg font-semibold">Редактирование проекта</h3>
          <button class="btn-ghost" @click="showEdit = false">✕</button>
        </div>
        <div class="grid grid-cols-1 gap-3">
          <div>
            <label class="block text-sm font-medium mb-1">Название проекта *</label>
            <input v-model="editForm.name" class="border p-2 rounded w-full" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Описание</label>
            <textarea v-model="editForm.description" class="border p-2 rounded w-full" rows="3"></textarea>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium mb-1">Дата начала</label>
              <input v-model="editForm.startDate" type="date" class="border p-2 rounded w-full" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">Дата окончания</label>
              <input v-model="editForm.endDate" type="date" class="border p-2 rounded w-full" />
            </div>
          </div>
        </div>
        <div class="mt-4 flex justify-end gap-2">
          <button class="btn btn-secondary" @click="showEdit = false">Отмена</button>
          <button class="btn btn-primary" @click="saveEdit">Сохранить</button>
        </div>
      </div>
    </div>
  </div>
</template>
