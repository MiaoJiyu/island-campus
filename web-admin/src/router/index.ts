import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/Dashboard.vue'), meta: { title: '仪表盘' } },
      // 系统管理
      { path: 'system/org', name: 'SystemOrg', component: () => import('@/views/system/Organization.vue'), meta: { title: '组织管理' } },
      { path: 'system/user', name: 'SystemUser', component: () => import('@/views/system/UserManage.vue'), meta: { title: '用户管理' } },
      { path: 'system/role', name: 'SystemRole', component: () => import('@/views/system/RoleManage.vue'), meta: { title: '角色管理' } },
      // 灵动岛
      { path: 'island/config', name: 'IslandConfig', component: () => import('@/views/island/IslandConfig.vue'), meta: { title: '灵动岛配置' } },
      // 情景模式
      { path: 'mode/list', name: 'ModeList', component: () => import('@/views/mode/ModeList.vue'), meta: { title: '情景模式' } },
      // 考试管理
      { path: 'exam/list', name: 'ExamList', component: () => import('@/views/exam/ExamList.vue'), meta: { title: '考试管理' } },
      // 天气服务
      { path: 'weather/config', name: 'WeatherConfig', component: () => import('@/views/weather/WeatherConfig.vue'), meta: { title: '天气配置' } },
      { path: 'weather/alerts', name: 'WeatherAlerts', component: () => import('@/views/weather/WeatherAlerts.vue'), meta: { title: '天气提醒规则' } },
      // 公告广播
      { path: 'announcement', name: 'Announcement', component: () => import('@/views/announcement/AnnouncementList.vue'), meta: { title: '公告管理' } },
      // 远程消息
      { path: 'message/send', name: 'MessageSend', component: () => import('@/views/message/MessageSend.vue'), meta: { title: '远程消息' } },
      // 答案公布
      { path: 'answer/manage', name: 'AnswerManage', component: () => import('@/views/answer/AnswerManage.vue'), meta: { title: '答案管理' } },
      // 健康管家
      { path: 'health/dashboard', name: 'HealthDashboard', component: () => import('@/views/health/HealthDashboard.vue'), meta: { title: '健康看板' } },
      // 设备管理
      { path: 'computer/list', name: 'ComputerList', component: () => import('@/views/computer/ComputerList.vue'), meta: { title: '设备管理' } },
      // 查询令牌
      { path: 'token/manage', name: 'TokenManage', component: () => import('@/views/token/TokenManage.vue'), meta: { title: '令牌管理' } },
      // 插件管理
      { path: 'plugin/manage', name: 'PluginManage', component: () => import('@/views/plugin/PluginManage.vue'), meta: { title: '插件管理' } },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() { return { top: 0 } }
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  NProgress.start()
  document.title = `${to.meta.title || ''} - 灵岛校园`
  const token = localStorage.getItem('token')
  if (!to.meta.public && !token) next({ name: 'Login', query: { redirect: to.fullPath } })
  else next()
})
router.afterEach(() => { NProgress.done() })

export default router
