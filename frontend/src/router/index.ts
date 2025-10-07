import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import AuthView from '../views/AuthView.vue'
import ProjectsView from '../views/ProjectsView.vue'
import DefectsView from '../views/DefectsView.vue'

const routes = [
  { path: '/', component: HomeView },
  { path: '/auth', component: AuthView },
  { path: '/projects', component: ProjectsView },
  { path: '/defects', component: DefectsView },
]

const router = createRouter({ history: createWebHistory(import.meta.env.BASE_URL), routes })

export default router
