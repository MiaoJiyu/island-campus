import { createRouter, createWebHashHistory } from 'vue-router'
import IslandPage from '../pages/IslandBar.vue'
import ExamPage from '../pages/ExamLock.vue'
import MainPage from '../pages/MainPanel.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/island', name: 'Island', component: IslandPage },
    { path: '/exam', name: 'Exam', component: ExamPage },
    { path: '/main', name: 'Main', component: MainPage },
  ]
})
export default router
