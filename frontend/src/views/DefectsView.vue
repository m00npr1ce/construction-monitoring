<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../api'

const defects = ref<any[]>([])
const projects = ref<any[]>([])
const users = ref<any[]>([])

const title = ref('')
const description = ref('')
const projectId = ref<number | null>(null)
const assigneeId = ref<number | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const showEditModal = ref(false)
const editingDefect = ref<any | null>(null)

const PRIORITIES = ['LOW','MEDIUM','HIGH','CRITICAL']
const STATUSES = ['NEW','IN_PROGRESS','IN_REVIEW','CLOSED','CANCELLED']

async function load() {
  loading.value = true
  try {
    const [d, p, u] = await Promise.all([
      api.get('/defects'),
      api.get('/projects'),
      api.get('/users')
    ])
    defects.value = d.data
    projects.value = p.data
    users.value = u.data
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
}

async function createDefect() {
  error.value = null
  if (!title.value || !projectId.value) { error.value = 'Title and project are required'; return }
  try {
    const payload: any = { title: title.value, description: description.value, projectId: projectId.value }
    if (assigneeId.value) payload.assigneeId = assigneeId.value
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

function openEdit(d: any) {
  // shallow clone to avoid editing original until saved
  editingDefect.value = { ...d }
  // convert ISO dueDate to datetime-local value if present
  if (editingDefect.value.dueDate) {
    try {
      const dt = new Date(editingDefect.value.dueDate)
      // to input datetime-local format
      editingDefect.value.dueDateLocal = dt.toISOString().slice(0,16)
    } catch (e) {
      editingDefect.value.dueDateLocal = ''
    }
  } else editingDefect.value.dueDateLocal = ''
  showEditModal.value = true
}

async function saveEdit() {
  if (!editingDefect.value) return
  error.value = null
  try {
    const payload: any = {
      title: editingDefect.value.title,
      description: editingDefect.value.description,
      priority: editingDefect.value.priority || 'MEDIUM',
      status: editingDefect.value.status || 'NEW',
      assigneeId: editingDefect.value.assigneeId || null,
      projectId: editingDefect.value.projectId,
    }
    if (editingDefect.value.dueDateLocal) payload.dueDate = new Date(editingDefect.value.dueDateLocal).toISOString()
    const r = await api.put('/defects/' + editingDefect.value.id, payload)
    const updated = r.data
    // resolve names
    const proj = projects.value.find((p: any) => p.id === updated.projectId)
    const assignee = users.value.find((u: any) => u.id === updated.assigneeId)
    updated.projectName = proj ? proj.name : null
    updated.assigneeName = assignee ? assignee.username : null
    // replace in list
    const idx = defects.value.findIndex((x: any) => x.id === updated.id)
    if (idx >= 0) defects.value.splice(idx, 1, updated)
    showEditModal.value = false
    editingDefect.value = null
  } catch (e: any) {
    error.value = 'Update failed: ' + (e.response?.data || e.message || e)
  }
}

function closeEdit() { showEditModal.value = false; editingDefect.value = null }

onMounted(load)
</script>

<template>
  <div>
    <h2 class="text-xl font-semibold">Defects</h2>
    <div class="mt-3 mb-4 space-y-2">
      <div>
        <input v-model="title" placeholder="title" class="border p-2 rounded w-full" />
      </div>
      <div>
        <input v-model="description" placeholder="description" class="border p-2 rounded w-full" />
      </div>
      <div class="flex gap-2">
        <select v-model.number="projectId" class="border p-2 rounded flex-1">
          <option value="" disabled>Select project</option>
          <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
        </select>
        <select v-model.number="assigneeId" class="border p-2 rounded w-48">
          <option value="">No assignee</option>
          <option v-for="u in users" :key="u.id" :value="u.id">{{ u.username }} ({{ u.role }})</option>
        </select>
        <button @click="createDefect" class="btn">Create</button>
      </div>
      <div v-if="error" class="text-red-600">{{ error }}</div>
    </div>

    <ul class="space-y-2">
      <li v-for="d in defects" :key="d.id" class="p-2 border rounded bg-white">
        <div class="font-semibold">#{{ d.id }} — {{ d.title }}</div>
        <div class="small text-sm text-gray-600">Project: {{ d.projectName || d.projectId }} — Assignee: {{ d.assigneeName || d.assigneeId || '—' }}</div>
        <div class="mt-1">{{ d.description }}</div>
        <div class="mt-2 flex gap-2">
          <button @click.prevent="openEdit(d)" class="btn">Edit</button>
          <button @click.prevent="(async ()=>{ if(confirm('Delete defect?')){ await api.delete('/defects/' + d.id); defects.value = defects.value.filter(x=>x.id!==d.id) } })()" class="btn-ghost">Delete</button>
        </div>
      </li>
    </ul>
  </div>
</template>

<!-- Edit Modal -->
<template v-if="showEditModal">
  <div class="fixed inset-0 bg-black/40 flex items-center justify-center z-40">
    <div class="bg-white rounded-lg p-4 w-full max-w-2xl">
      <h3 class="text-lg font-semibold mb-2">Edit defect #{{ editingDefect?.id }}</h3>
      <div class="space-y-2">
        <input v-model="editingDefect.title" class="border p-2 rounded w-full" placeholder="Title" />
        <textarea v-model="editingDefect.description" class="border p-2 rounded w-full" placeholder="Description"></textarea>
        <div class="flex gap-2">
          <select v-model="editingDefect.priority" class="border p-2 rounded">
            <option v-for="p in PRIORITIES" :key="p" :value="p">{{ p }}</option>
          </select>
          <select v-model="editingDefect.status" class="border p-2 rounded">
            <option v-for="s in STATUSES" :key="s" :value="s">{{ s }}</option>
          </select>
          <select v-model.number="editingDefect.projectId" class="border p-2 rounded flex-1">
            <option value="" disabled>Select project</option>
            <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
          <select v-model.number="editingDefect.assigneeId" class="border p-2 rounded w-48">
            <option value="">No assignee</option>
            <option v-for="u in users" :key="u.id" :value="u.id">{{ u.username }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm text-gray-600">Due date</label>
          <input v-model="editingDefect.dueDateLocal" type="datetime-local" class="border p-2 rounded" />
        </div>
      </div>
      <div class="mt-4 flex justify-end gap-2">
        <button @click="closeEdit" class="btn-ghost">Cancel</button>
        <button @click="saveEdit" class="btn">Save</button>
      </div>
    </div>
  </div>
</template>
