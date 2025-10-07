import { createApp } from 'vue'
import './styles.css'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { setAuthToken } from './api'

const app = createApp(App)

// restore token from localStorage on app start
const token = localStorage.getItem('jwt')
if (token) setAuthToken(token)

app.use(createPinia())
app.use(router)

app.mount('#app')
