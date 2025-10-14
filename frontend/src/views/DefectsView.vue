<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import api from '../api'

const defects = ref<any[]>([])
const projects = ref<any[]>([])
const users = ref<any[]>([])

const title = ref('')
const description = ref('')
const projectId = ref<number | null>(null)
const assigneeId = ref<number | null>(null)
const priority = ref('MEDIUM')
const status = ref('NEW')
const dueDate = ref('')
const loading = ref(false)
const error = ref<string | null>(null)
const selectedProject = ref<number | null>(null)
const statusFilter = ref('ALL')
const priorityFilter = ref('ALL')

const canCreateDefects = computed(() => {
  const role = window.localStorage.getItem('role') || ''
  return role.includes('ROLE_MANAGER') || role.includes('ROLE_ADMIN')
})

const isAdmin = computed(() => {
  const role = window.localStorage.getItem('role') || ''
  return role.includes('ROLE_ADMIN')
})

const isManager = computed(() => {
  const role = window.localStorage.getItem('role') || ''
  return role.includes('ROLE_MANAGER')
})

const filteredDefects = computed(() => {
  let filtered = defects.value
  
  if (selectedProject.value) {
    filtered = filtered.filter(d => d.projectId === selectedProject.value)
  }
  
  if (statusFilter.value !== 'ALL') {
    filtered = filtered.filter(d => d.status === statusFilter.value)
  }
  
  if (priorityFilter.value !== 'ALL') {
    filtered = filtered.filter(d => d.priority === priorityFilter.value)
  }
  
  return filtered
})

async function load() {
  loading.value = true
  try {
    const [d, p] = await Promise.all([
      api.get('/defects'),
      api.get('/projects')
    ])
    defects.value = d.data
    projects.value = p.data
    if (isAdmin.value || isManager.value) {
      try {
        const u = await api.get('/users')
        // Показываем только инженеров как возможных исполнителей
        users.value = (u.data || []).filter((x: any) => x.role === 'ROLE_ENGINEER')
      } catch (e) {
        // ignore users load errors for non-admin contexts
        users.value = []
      }
    } else {
      users.value = []
    }
    // resolve names for existing defects
    defects.value = defects.value.map((it: any) => {
      const proj = projects.value.find((x: any) => x.id === it.projectId)
      const assignee = users.value.find((x: any) => x.id === it.assigneeId)
      return { ...it, projectName: proj ? proj.name : null, assigneeName: assignee ? assignee.username : null }
    })
  } catch (e: any) {
    error.value = 'Failed to load data: ' + (e.response?.data || e.message || e)
  } finally { loading.value = false }
}

function resetForm() {
  title.value = ''
  description.value = ''
  projectId.value = null
  assigneeId.value = null
  priority.value = 'MEDIUM'
  status.value = 'NEW'
  dueDate.value = ''
}

async function createDefect() {
  error.value = null
  if (!title.value || !projectId.value) { error.value = 'Title and project are required'; return }
  try {
    const payload: any = { 
      title: title.value, 
      description: description.value, 
      projectId: projectId.value,
      priority: priority.value,
      status: status.value
    }
    if (assigneeId.value) payload.assigneeId = assigneeId.value
    if (dueDate.value) payload.dueDate = new Date(dueDate.value).toISOString()
    
    // Только менеджер/админ — на клиенте дополнительно скроем кнопку, но сервер решает окончательно
    const r = await api.post('/defects', payload)
    // augment with resolved names for display
    const created = r.data
    const proj = projects.value.find((p: any) => p.id === created.projectId)
    const assignee = users.value.find((u: any) => u.id === created.assigneeId)
    created.projectName = proj ? proj.name : null
    created.assigneeName = assignee ? assignee.username : null
    defects.value.push(created)
    resetForm()
  } catch (e: any) {
    error.value = 'Create failed: ' + (e.response?.data || e.message || e)
  }
}

async function updateDefectStatus(defectId: number, newStatus: string) {
  try {
    const defect = defects.value.find(d => d.id === defectId)
    if (defect) {
      defect.status = newStatus
      await api.put(`/defects/${defectId}`, defect)
    }
  } catch (e: any) {
    error.value = 'Update failed: ' + (e.response?.data || e.message || e)
  }
}

async function exportReport() {
  try {
    const response = await api.get('/reports/defects/export', { 
      responseType: 'blob',
      params: selectedProject.value ? { projectId: selectedProject.value } : {}
    })
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'defects_report.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
  } catch (e: any) {
    error.value = 'Export failed: ' + (e.response?.data || e.message || e)
  }
}

function getPriorityClass(priority: string) {
  const classes: Record<string, string> = {
    'LOW': 'text-green-600 bg-green-100',
    'MEDIUM': 'text-yellow-600 bg-yellow-100',
    'HIGH': 'text-orange-600 bg-orange-100',
    'CRITICAL': 'text-red-600 bg-red-100'
  }
  return classes[priority] || 'text-gray-600 bg-gray-100'
}

function getStatusClass(status: string) {
  const classes: Record<string, string> = {
    'NEW': 'bg-blue-100 text-blue-800',
    'IN_PROGRESS': 'bg-yellow-100 text-yellow-800',
    'IN_REVIEW': 'bg-purple-100 text-purple-800',
    'CLOSED': 'bg-green-100 text-green-800',
    'CANCELLED': 'bg-red-100 text-red-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

onMounted(load)
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-xl font-semibold">Defects Management</h2>
      <button @click="exportReport" class="btn bg-green-600 text-white">Export Report</button>
    </div>

    <!-- Filters -->
    <div class="mb-4 p-4 bg-gray-50 rounded-lg">
      <h3 class="font-semibold mb-2">Filters</h3>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium mb-1">Project</label>
          <select v-model.number="selectedProject" class="border p-2 rounded w-full">
            <option :value="null">All Projects</option>
            <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Status</label>
          <select v-model="statusFilter" class="border p-2 rounded w-full">
            <option value="ALL">All Statuses</option>
            <option value="NEW">New</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="IN_REVIEW">In Review</option>
            <option value="CLOSED">Closed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Priority</label>
          <select v-model="priorityFilter" class="border p-2 rounded w-full">
            <option value="ALL">All Priorities</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Create Defect Form -->
    <div class="mb-6 p-4 bg-blue-50 rounded-lg">
      <h3 class="font-semibold mb-3">Create New Defect</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium mb-1">Title *</label>
          <input v-model="title" placeholder="Defect title" class="border p-2 rounded w-full" />
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Project *</label>
          <select v-model.number="projectId" class="border p-2 rounded w-full">
            <option value="" disabled>Select project</option>
            <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Description</label>
          <textarea v-model="description" placeholder="Defect description" class="border p-2 rounded w-full" rows="3"></textarea>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Assignee</label>
          <select v-model.number="assigneeId" class="border p-2 rounded w-full">
            <option value="">No assignee</option>
            <option v-for="u in users" :key="u.id" :value="u.id">{{ u.username }} ({{ u.role }})</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Priority</label>
          <select v-model="priority" class="border p-2 rounded w-full">
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Status</label>
          <select v-model="status" class="border p-2 rounded w-full">
            <option value="NEW">New</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="IN_REVIEW">In Review</option>
            <option value="CLOSED">Closed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Due Date</label>
          <input v-model="dueDate" type="datetime-local" class="border p-2 rounded w-full" />
        </div>
        <div class="flex items-end">
          <button @click="createDefect" class="btn bg-blue-600 text-white w-full" v-if="canCreateDefects">Create Defect</button>
        </div>
      </div>
      <div v-if="error" class="text-red-600 mt-2">{{ error }}</div>
    </div>

    <!-- Defects List -->
    <div class="space-y-3">
      <div v-if="loading" class="text-center py-4">Loading...</div>
      <div v-else-if="filteredDefects.length === 0" class="text-center py-8 text-gray-500">
        No defects found
      </div>
      <div v-else>
        <div v-for="d in filteredDefects" :key="d.id" class="p-4 border rounded-lg bg-white shadow-sm">
          <div class="flex justify-between items-start mb-2">
            <div>
              <div class="font-semibold text-lg">#{{ d.id }} — {{ d.title }}</div>
              <div class="text-sm text-gray-600">
                Project: {{ d.projectName || d.projectId }} | 
                Assignee: {{ d.assigneeName || d.assigneeId || '—' }} |
                Priority: <span :class="getPriorityClass(d.priority)">{{ d.priority }}</span>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <select :value="d.status" @change="updateDefectStatus(d.id, ($event.target as HTMLSelectElement).value)" 
                      class="border p-1 rounded text-sm">
                <option value="NEW">New</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="IN_REVIEW">In Review</option>
                <option value="CLOSED">Closed</option>
                <option value="CANCELLED">Cancelled</option>
              </select>
              <span :class="getStatusClass(d.status)" class="px-2 py-1 rounded text-xs font-medium">
                {{ d.status }}
              </span>
            </div>
          </div>
          <div v-if="d.description" class="text-gray-700 mb-2">{{ d.description }}</div>
          <div class="text-xs text-gray-500">
            Created: {{ new Date(d.createdAt).toLocaleString() }} |
            Updated: {{ new Date(d.updatedAt).toLocaleString() }}
            <span v-if="d.dueDate">| Due: {{ new Date(d.dueDate).toLocaleString() }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
