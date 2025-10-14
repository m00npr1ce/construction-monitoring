<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import api from '../api'

const defects = ref<any[]>([])
const projects = ref<any[]>([])
const users = ref<any[]>([])
const expandedId = ref<number | null>(null)
const comments = ref<Record<number, any[]>>({})
const newComment = ref<Record<number, string>>({})
const attachments = ref<Record<number, any[]>>({})
const uploading = ref<Record<number, boolean>>({})

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

const query = ref('')
const filteredDefects = computed(() => {
  let filtered = defects.value
  if (selectedProject.value) filtered = filtered.filter(d => d.projectId === selectedProject.value)
  if (statusFilter.value !== 'ALL') filtered = filtered.filter(d => d.status === statusFilter.value)
  if (priorityFilter.value !== 'ALL') filtered = filtered.filter(d => d.priority === priorityFilter.value)
  const q = query.value.trim().toLowerCase()
  if (q) {
    filtered = filtered.filter(d =>
      (d.title || '').toLowerCase().includes(q) ||
      (d.description || '').toLowerCase().includes(q)
    )
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

async function toggleExpand(defectId: number) {
  expandedId.value = expandedId.value === defectId ? null : defectId
  if (expandedId.value === defectId) {
    await Promise.all([loadComments(defectId), loadAttachments(defectId)])
  }
}

async function loadComments(defectId: number) {
  try {
    const r = await api.get(`/comments/defect/${defectId}`)
    comments.value[defectId] = r.data || []
  } catch (e) {
    comments.value[defectId] = []
  }
}

async function addComment(defectId: number) {
  const content = (newComment.value[defectId] || '').trim()
  if (!content) return
  try {
    await api.post('/comments', { content, defectId })
    newComment.value[defectId] = ''
    await loadComments(defectId)
  } catch (e) { /* ignore */ }
}

async function deleteComment(defectId: number, commentId: number) {
  try {
    await api.delete(`/comments/${commentId}`)
    await loadComments(defectId)
  } catch (e) { /* ignore */ }
}

async function loadAttachments(defectId: number) {
  try {
    const r = await api.get(`/attachments/defect/${defectId}`)
    attachments.value[defectId] = r.data || []
  } catch (e) {
    attachments.value[defectId] = []
  }
}

async function uploadAttachment(defectId: number, ev: Event) {
  const input = ev.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return
  const file = input.files[0]
  const form = new FormData()
  form.append('file', file)
  try {
    uploading.value[defectId] = true
    await api.post(`/attachments/upload/${defectId}`, form, { headers: { 'Content-Type': 'multipart/form-data' } })
    await loadAttachments(defectId)
  } finally {
    uploading.value[defectId] = false
    input.value = ''
  }
}

function downloadAttachment(attId: number, name: string) {
  api.get(`/attachments/${attId}/download`, { responseType: 'blob' }).then(res => {
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const a = document.createElement('a')
    a.href = url
    a.download = name
    a.click()
    window.URL.revokeObjectURL(url)
  })
}

async function deleteAttachment(attId: number, defectId: number) {
  try {
    await api.delete(`/attachments/${attId}`)
    await loadAttachments(defectId)
  } catch (e) { /* ignore */ }
}

async function moveToNextStatus(defectId: number) {
  try {
    const response = await api.put(`/defects/${defectId}/next-status`, {
      userId: 1 // TODO: Get from current user context
    })
    
    // Update local state
    const defect = defects.value.find(d => d.id === defectId)
    if (defect) {
      defect.status = response.data.status
    }
  } catch (e: any) {
    const errorMessage = e.response?.data?.message || 'Ошибка при изменении статуса дефекта'
    error.value = errorMessage
    alert(errorMessage)
  }
}

async function cancelDefect(defectId: number) {
  try {
    const response = await api.put(`/defects/${defectId}/cancel`, {
      userId: 1 // TODO: Get from current user context
    })
    
    // Update local state
    const defect = defects.value.find(d => d.id === defectId)
    if (defect) {
      defect.status = response.data.status
    }
  } catch (e: any) {
    const errorMessage = e.response?.data?.message || 'Ошибка при отмене дефекта'
    error.value = errorMessage
    alert(errorMessage)
  }
}

// Get allowed status transitions for a defect
async function getAllowedStatuses(defectId: number) {
  try {
    const response = await api.get(`/defects/${defectId}/allowed-statuses`)
    return response.data.allowedStatuses || []
  } catch (e) {
    console.error('Failed to get allowed statuses:', e)
    return []
  }
}

// Get allowed statuses for a defect (cached)
const allowedStatusesCache = ref<Record<number, string[]>>({})

function getAllowedStatusesForDefect(defect: any) {
  if (allowedStatusesCache.value[defect.id]) {
    return allowedStatusesCache.value[defect.id]
  }
  
  // For now, return all statuses - will be updated when we load allowed statuses
  return ['NEW', 'IN_PROGRESS', 'IN_REVIEW', 'CLOSED', 'CANCELLED']
}

function getStatusLabel(status: string) {
  const labels: Record<string, string> = {
    'NEW': 'Новый',
    'IN_PROGRESS': 'В работе', 
    'IN_REVIEW': 'На проверке',
    'CLOSED': 'Закрыт',
    'CANCELLED': 'Отменен'
  }
  return labels[status] || status
}

function canMoveToNext(status: string) {
  const role = window.localStorage.getItem('role') || ''
  
  // Менеджер и админ могут переводить любой статус
  if (role.includes('ROLE_MANAGER') || role.includes('ROLE_ADMIN')) {
    return status === 'NEW' || status === 'IN_PROGRESS' || status === 'IN_REVIEW'
  }
  
  // Инженер НЕ может переводить из "На проверке" (только менеджер может закрывать)
  if (role.includes('ROLE_ENGINEER')) {
    return status === 'NEW' || status === 'IN_PROGRESS'
  }
  
  // Остальные роли не могут изменять статусы
  return false
}

function canCancel(status: string) {
  const role = window.localStorage.getItem('role') || ''
  
  // Менеджер и админ могут отменять любой статус
  if (role.includes('ROLE_MANAGER') || role.includes('ROLE_ADMIN')) {
    return status === 'NEW' || status === 'IN_PROGRESS' || status === 'IN_REVIEW'
  }
  
  // Инженер не может отменять дефекты "На проверке"
  if (role.includes('ROLE_ENGINEER')) {
    return status === 'NEW' || status === 'IN_PROGRESS'
  }
  
  // Остальные роли не могут отменять
  return false
}

function getNextStatusLabel(currentStatus: string) {
  const role = window.localStorage.getItem('role') || ''
  
  // Для инженера: не может закрывать дефекты
  if (role.includes('ROLE_ENGINEER')) {
    const engineerLabels: Record<string, string> = {
      'NEW': 'Взять в работу',
      'IN_PROGRESS': 'Отправить на проверку'
    }
    return engineerLabels[currentStatus] || 'Следующий шаг'
  }
  
  // Для менеджера и админа: полные права
  const fullLabels: Record<string, string> = {
    'NEW': 'Взять в работу',
    'IN_PROGRESS': 'Отправить на проверку',
    'IN_REVIEW': 'Закрыть'
  }
  return fullLabels[currentStatus] || 'Следующий шаг'
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
      <h2 class="text-xl font-semibold">Управление дефектами</h2>
      <button @click="exportReport" class="btn bg-green-600 text-white">Экспорт отчета</button>
    </div>

    <!-- Filters -->
    <div class="mb-4 p-4 bg-gray-50 rounded-lg">
      <h3 class="font-semibold mb-2">Фильтры</h3>
      <input v-model="query" placeholder="Поиск по заголовку/описанию" class="border p-2 rounded w-full mb-3" />
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium mb-1">Проект</label>
          <select v-model.number="selectedProject" class="border p-2 rounded w-full">
            <option :value="null">Все проекты</option>
            <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Статус</label>
          <select v-model="statusFilter" class="border p-2 rounded w-full">
            <option value="ALL">Все статусы</option>
            <option value="NEW">Новый</option>
            <option value="IN_PROGRESS">В работе</option>
            <option value="IN_REVIEW">На проверке</option>
            <option value="CLOSED">Закрыт</option>
            <option value="CANCELLED">Отменен</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Приоритет</label>
          <select v-model="priorityFilter" class="border p-2 rounded w-full">
            <option value="ALL">Все приоритеты</option>
            <option value="LOW">Низкий</option>
            <option value="MEDIUM">Средний</option>
            <option value="HIGH">Высокий</option>
            <option value="CRITICAL">Критический</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Create Defect Form -->
    <div class="mb-6 p-4 bg-blue-50 rounded-lg">
      <h3 class="font-semibold mb-3">Создать новый дефект</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium mb-1">Заголовок *</label>
          <input v-model="title" placeholder="Название дефекта" class="border p-2 rounded w-full" />
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Проект *</label>
          <select v-model.number="projectId" class="border p-2 rounded w-full">
            <option value="" disabled>Выберите проект</option>
            <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Описание</label>
          <textarea v-model="description" placeholder="Описание дефекта" class="border p-2 rounded w-full" rows="3"></textarea>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Исполнитель</label>
          <select v-model.number="assigneeId" class="border p-2 rounded w-full">
            <option value="">Без исполнителя</option>
            <option v-for="u in users" :key="u.id" :value="u.id">{{ u.username }} ({{ u.role }})</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Приоритет</label>
          <select v-model="priority" class="border p-2 rounded w-full">
            <option value="LOW">Низкий</option>
            <option value="MEDIUM">Средний</option>
            <option value="HIGH">Высокий</option>
            <option value="CRITICAL">Критический</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Статус</label>
          <select v-model="status" class="border p-2 rounded w-full">
            <option value="NEW">Новый</option>
            <option value="IN_PROGRESS">В работе</option>
            <option value="IN_REVIEW">На проверке</option>
            <option value="CLOSED">Закрыт</option>
            <option value="CANCELLED">Отменен</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Срок</label>
          <input v-model="dueDate" type="datetime-local" class="border p-2 rounded w-full" />
        </div>
        <div class="flex items-end">
          <button @click="createDefect" class="btn bg-blue-600 text-white w-full" v-if="canCreateDefects">Создать дефект</button>
        </div>
      </div>
      <div v-if="error" class="text-red-600 mt-2">{{ error }}</div>
    </div>

    <!-- Defects List -->
    <div class="space-y-3">
      <div v-if="loading" class="text-center py-4">Загрузка...</div>
      <div v-else-if="filteredDefects.length === 0" class="text-center py-8 text-gray-500">
        Дефекты не найдены
      </div>
      <div v-else>
        <div v-for="d in filteredDefects" :key="d.id" class="p-4 border rounded-lg bg-white shadow-sm">
          <div class="flex justify-between items-start mb-2">
            <div>
              <div class="font-semibold text-lg">#{{ d.id }} — {{ d.title }}</div>
              <div class="text-sm text-gray-600">
                Проект: {{ d.projectName || d.projectId }} | 
                Исполнитель: {{ d.assigneeName || d.assigneeId || '—' }} |
                Приоритет: <span :class="getPriorityClass(d.priority)">{{ d.priority }}</span>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span :class="getStatusClass(d.status)" class="px-2 py-1 rounded text-xs font-medium">
                {{ getStatusLabel(d.status) }}
              </span>
              
              <!-- Кнопка следующего шага -->
              <button v-if="canMoveToNext(d.status)" 
                      @click="moveToNextStatus(d.id)"
                      class="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-sm font-medium">
                {{ getNextStatusLabel(d.status) }}
              </button>
              
              <!-- Кнопка отмены -->
              <button v-if="canCancel(d.status)" 
                      @click="cancelDefect(d.id)"
                      class="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded text-sm font-medium">
                Отменить
              </button>
              
              <!-- Статус для финальных состояний -->
              <span v-if="d.status === 'CLOSED'" class="text-green-600 text-sm font-medium">
                ✅ Завершен
              </span>
              <span v-if="d.status === 'CANCELLED'" class="text-red-600 text-sm font-medium">
                ❌ Отменен
              </span>
              <button class="text-blue-600 text-sm underline" @click="toggleExpand(d.id)">
                {{ expandedId === d.id ? 'Свернуть' : 'Подробнее' }}
              </button>
            </div>
          </div>
          <div v-if="d.description" class="text-gray-700 mb-2">{{ d.description }}</div>
          <div class="text-xs text-gray-500">
            Создан: {{ new Date(d.createdAt).toLocaleString() }} |
            Обновлен: {{ new Date(d.updatedAt).toLocaleString() }}
            <span v-if="d.dueDate">| Срок: {{ new Date(d.dueDate).toLocaleString() }}</span>
          </div>

          <div v-if="expandedId === d.id" class="mt-4 border-t pt-3">
            <div class="grid md:grid-cols-2 gap-4">
              <!-- Comments -->
              <div>
                <h4 class="font-semibold mb-2">Комментарии</h4>
                <div class="space-y-2 max-h-56 overflow-auto bg-gray-50 p-2 rounded">
                  <div v-for="c in (comments[d.id]||[])" :key="c.id" class="flex justify-between bg-white p-2 rounded border">
                    <div>
                      <div class="text-sm">{{ c.content }}</div>
                      <div class="text-xs text-gray-500">Автор: {{ c.authorId }} | {{ new Date(c.createdAt).toLocaleString?.() || '' }}</div>
                    </div>
                    <button class="text-red-600 text-xs" @click="deleteComment(d.id, c.id)">Удалить</button>
                  </div>
                  <div v-if="!(comments[d.id]||[]).length" class="text-sm text-gray-500">Нет комментариев</div>
                </div>
                <div class="mt-2 flex gap-2">
                  <input v-model="newComment[d.id]" placeholder="Добавить комментарий" class="border p-2 rounded w-full" />
                  <button class="bg-blue-600 text-white px-3 rounded" @click="addComment(d.id)">Добавить</button>
                </div>
              </div>

              <!-- Attachments -->
              <div>
                <h4 class="font-semibold mb-2">Вложения</h4>
                <div class="space-y-2 max-h-56 overflow-auto bg-gray-50 p-2 rounded">
                  <div v-for="a in (attachments[d.id]||[])" :key="a.id" class="flex justify-between bg-white p-2 rounded border">
                    <div class="truncate">
                      <div class="text-sm truncate">{{ a.originalFileName }}</div>
                      <div class="text-xs text-gray-500">{{ a.contentType }} • {{ Math.round((a.size||0)/1024) }} KB</div>
                    </div>
                    <div class="flex gap-2 items-center text-xs">
                      <button class="text-blue-600" @click="downloadAttachment(a.id, a.originalFileName)">Скачать</button>
                      <button class="text-red-600" @click="deleteAttachment(a.id, d.id)">Удалить</button>
                    </div>
                  </div>
                  <div v-if="!(attachments[d.id]||[]).length" class="text-sm text-gray-500">Нет вложений</div>
                </div>
                <div class="mt-2">
                  <label class="inline-flex items-center gap-2 text-sm">
                    <input type="file" :disabled="uploading[d.id]" @change="(e:any)=>uploadAttachment(d.id,e)" />
                    <span v-if="uploading[d.id]">Загрузка...</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
