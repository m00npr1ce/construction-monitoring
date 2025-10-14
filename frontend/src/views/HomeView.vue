<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import api from '../api'

Chart.register(...registerables)

const stats = ref({
  totalProjects: 0,
  totalDefects: 0,
  newDefects: 0,
  inProgressDefects: 0,
  closedDefects: 0
})

const loading = ref(false)
const chartInstances = ref<Chart[]>([])

async function loadStats() {
  loading.value = true
  try {
    const [projectsRes, defectsRes, analyticsRes] = await Promise.all([
      api.get('/projects'),
      api.get('/defects'),
      api.get('/reports/analytics')
    ])
    
    stats.value = {
      totalProjects: projectsRes.data.length,
      totalDefects: defectsRes.data.length,
      newDefects: analyticsRes.data.newDefects || 0,
      inProgressDefects: analyticsRes.data.inProgressDefects || 0,
      closedDefects: analyticsRes.data.closedDefects || 0
    }
    
    await nextTick()
    createCharts()
  } catch (e) {
    console.error('Failed to load stats:', e)
  } finally {
    loading.value = false
  }
}

function createCharts() {
  // Destroy existing charts
  chartInstances.value.forEach(chart => chart.destroy())
  chartInstances.value = []

  // Defects Status Pie Chart
  const statusCtx = document.getElementById('defectsStatusChart') as HTMLCanvasElement
  if (statusCtx) {
    const statusChart = new Chart(statusCtx, {
      type: 'doughnut',
      data: {
        labels: ['Новые', 'В работе', 'На проверке', 'Закрытые'],
        datasets: [{
          data: [
            stats.value.newDefects,
            stats.value.inProgressDefects,
            stats.value.closedDefects * 0.3, // Примерное распределение
            stats.value.closedDefects * 0.7
          ],
          backgroundColor: [
            '#3B82F6', // Blue
            '#F59E0B', // Yellow
            '#8B5CF6', // Purple
            '#10B981'  // Green
          ],
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 20,
              usePointStyle: true
            }
          },
          title: {
            display: true,
            text: 'Статусы дефектов',
            font: {
              size: 16,
              weight: 'bold'
            }
          }
        }
      }
    })
    chartInstances.value.push(statusChart)
  }

  // Projects Progress Bar Chart
  const progressCtx = document.getElementById('projectsProgressChart') as HTMLCanvasElement
  if (progressCtx) {
    const progressChart = new Chart(progressCtx, {
      type: 'bar',
      data: {
        labels: ['Жилой комплекс "Северный"', 'Торговый центр "Мега"', 'Офисное здание "Бизнес-Плаза"', 'Школа №15'],
        datasets: [{
          label: 'Прогресс (%)',
          data: [75, 60, 45, 90],
          backgroundColor: [
            '#3B82F6',
            '#F59E0B', 
            '#EF4444',
            '#10B981'
          ],
          borderRadius: 8,
          borderSkipped: false
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          title: {
            display: true,
            text: 'Прогресс проектов',
            font: {
              size: 16,
              weight: 'bold'
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            max: 100,
            ticks: {
              callback: function(value) {
                return value + '%'
              }
            }
          },
          x: {
            ticks: {
              maxRotation: 45,
              minRotation: 0
            }
          }
        }
      }
    })
    chartInstances.value.push(progressChart)
  }

  // Defects Timeline Line Chart
  const timelineCtx = document.getElementById('defectsTimelineChart') as HTMLCanvasElement
  if (timelineCtx) {
    const timelineChart = new Chart(timelineCtx, {
      type: 'line',
      data: {
        labels: ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн'],
        datasets: [{
          label: 'Создано дефектов',
          data: [12, 19, 15, 25, 22, 18],
          borderColor: '#EF4444',
          backgroundColor: 'rgba(239, 68, 68, 0.1)',
          tension: 0.4,
          fill: true
        }, {
          label: 'Исправлено дефектов',
          data: [8, 15, 12, 20, 18, 16],
          borderColor: '#10B981',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          tension: 0.4,
          fill: true
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top',
            labels: {
              usePointStyle: true,
              padding: 20
            }
          },
          title: {
            display: true,
            text: 'Динамика дефектов по месяцам',
            font: {
              size: 16,
              weight: 'bold'
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: {
              color: 'rgba(0, 0, 0, 0.1)'
            }
          },
          x: {
            grid: {
              color: 'rgba(0, 0, 0, 0.1)'
            }
          }
        }
      }
    })
    chartInstances.value.push(timelineChart)
  }
}

onMounted(loadStats)

const role = computed(() => (localStorage.getItem('role') || '').split(',').filter(Boolean))
const canDownloadReport = computed(() => role.value.includes('ROLE_MANAGER') || role.value.includes('ROLE_VIEWER') || role.value.includes('ROLE_ADMIN'))

async function downloadFullReport() {
  try {
    const response = await api.get('/reports/full/export', { responseType: 'blob' })
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'full_report.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    console.error('Failed to download report', e)
    alert('Failed to download report')
  }
}
</script>

<template>
  <div>
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900">Система мониторинга строительства</h1>
      <p class="mt-2 text-lg text-gray-600">
        Централизованная система управления дефектами и отслеживания проектов
      </p>
    </div>

    <!-- Stats Cards -->
    <div v-if="loading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      <p class="mt-2 text-gray-600">Загрузка статистики...</p>
    </div>
    
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6 mb-8">
      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-blue-500 rounded-md flex items-center justify-center">
                <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                </svg>
              </div>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">Всего проектов</dt>
                <dd class="text-lg font-medium text-gray-900">{{ stats.totalProjects }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-red-500 rounded-md flex items-center justify-center">
                <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
                </svg>
              </div>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">Всего дефектов</dt>
                <dd class="text-lg font-medium text-gray-900">{{ stats.totalDefects }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-blue-500 rounded-md flex items-center justify-center">
                <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
              </div>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">Новые дефекты</dt>
                <dd class="text-lg font-medium text-gray-900">{{ stats.newDefects }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-yellow-500 rounded-md flex items-center justify-center">
                <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
                </svg>
              </div>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">В работе</dt>
                <dd class="text-lg font-medium text-gray-900">{{ stats.inProgressDefects }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-green-500 rounded-md flex items-center justify-center">
                <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
              </div>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">Закрыто</dt>
                <dd class="text-lg font-medium text-gray-900">{{ stats.closedDefects }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
      <!-- Defects Status Chart -->
      <div class="bg-white shadow rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <div class="h-80">
            <canvas id="defectsStatusChart"></canvas>
          </div>
        </div>
      </div>

      <!-- Projects Progress Chart -->
      <div class="bg-white shadow rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <div class="h-80">
            <canvas id="projectsProgressChart"></canvas>
          </div>
        </div>
      </div>
    </div>

    <!-- Timeline Chart -->
    <div class="bg-white shadow rounded-lg mb-8">
      <div class="px-4 py-5 sm:p-6">
        <div class="h-96">
          <canvas id="defectsTimelineChart"></canvas>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="bg-white shadow rounded-lg">
      <div class="px-4 py-5 sm:p-6">
        <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">Быстрые действия</h3>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <router-link 
            to="/projects" 
            class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
            </svg>
            Управление проектами
          </router-link>
          
          <router-link 
            to="/defects" 
            class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
            </svg>
            Управление дефектами
          </router-link>

          <button 
            v-if="canDownloadReport"
            @click="downloadFullReport"
            class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v16h16V4H4zm4 8l4 4 4-4m-4 4V8" />
            </svg>
            Скачать общий отчет Excel
          </button>
          
          <router-link 
            to="/auth" 
            class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
            </svg>
            Войти / Регистрация
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>
