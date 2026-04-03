import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) { useUserStore().logout() }
      return Promise.reject(new Error(res.message))
    }
    return response.data as any
  },
  error => {
    const msg = error?.response?.data?.message || '网络错误'
    ElMessage.error(msg)
    if (error?.response?.status === 401) useUserStore().logout()
    return Promise.reject(error)
  }
)

// API 模块
const auth = {
  login: (data: { username: string; password: string }) => request.post('/auth/login', data),
  refresh: (refreshToken: string) => request.post('/auth/refresh', { refreshToken }),
  info: () => request.get('/auth/info'),
}

const org = {
  tree: () => request.get('/org/tree'),
  list: (params?: any) => request.get('/org', { params }),
  create: (data: any) => request.post('/org', data),
  update: (id: number, data: any) => request.put(`/org/${id}`, data),
  remove: (id: number) => request.delete(`/org/${id}`),
}

const user = {
  list: (params?: any) => request.get('/user', { params }),
  create: (data: any) => request.post('/user', data),
  update: (id: number, data: any) => request.put(`/user/${id}`, data),
  remove: (id: number) => request.delete(`/user/${id}`),
  resetPassword: (id: number, password: string) => request.put(`/user/${id}/reset-password`, { password }),
  batchImport: (file: File) => { const fd = new FormData(); fd.append('file', file); return request.post('/user/batch-import', fd, { headers: {'Content-Type': 'multipart/form-data'} }) },
}

const island = {
  getConfigList: () => request.get('/island/config'),
  getEffectiveConfig: (computerId?: number) => request.get('/island/config/effective', { params: { computerId } }),
  updateGlobal: (config: any) => request.put('/island/config/global', config),
  createScope: (data: any) => request.post('/island/config/scope', data),
  updateScope: (id: number, data: any) => request.put(`/island/config/scope/${id}`, data),
  deleteScope: (id: number) => request.delete(`/island/config/scope/${id}`),
  preview: () => request.get('/island/config/preview'),
}

const exam = {
  list: (params?: any) => request.get('/exam', { params }),
  create: (data: any) => request.post('/exam', data),
  update: (id: number, data: any) => request.put(`/exam/${id}`, data),
  remove: (id: number) => request.delete(`/exam/${id}`),
  calendar: (params?: any) => request.get('/exam/calendar', { params }),
  startExam: (id: number) => request.post(`/exam/${id}/start`),
  endExam: (id: number) => request.post(`/exam/${id}/end`),
  logs: (id: number) => request.get(`/exam/${id}/logs`),
}

const mode = {
  list: () => request.get('/modes'),
  scheduleList: () => request.get('/schedules'),
  createSchedule: (data: any) => request.post('/schedules', data),
  updateSchedule: (id: number, data: any) => request.put(`/schedules/${id}`, data),
  removeSchedule: (id: number) => request.delete(`/schedules/${id}`),
  switchMode: (data: { targetComputerIds: number[]; modeId: number }) => request.post('/mode/switch', data),
  currentStatus: (computerId?: number) => request.get('/mode/current-status', { params: { computerId } }),
}

const weather = {
  configList: () => request.get('/weather/configs'),
  createConfig: (data: any) => request.post('/weather/configs', data),
  testConnection: (data: any) => request.post('/weather/configs/test', data),
  setDefault: (id: number) => request.post(`/weather/configs/${id}/set-default`),
  currentWeather: (city?: string) => request.get('/weather/current', { params: { city } }),
  forecast: (city?: string) => request.get('/weather/forecast', { params: { city } }),
  alertRuleList: () => request.get('/weather/alert-rules'),
  createAlert: (data: any) => request.post('/weather/alert-rules', data),
  testAlert: (id: number) => request.post(`/weather/alert-rules/${id}/test`),
}

const announcement = {
  list: (params?: any) => request.get('/announcement', { params }),
  create: (data: any) => request.post('/announcement', data),
  publish: (id: number) => request.post(`/announcement/publish/${id}`),
  revoke: (id: number) => request.post(`/announcement/revoke/${id}`),
  emergencyBroadcast: (data: any) => request.post('/announcement/emergency', data),
  activeAnnouncements: (orgId?: number) => request.get('/announcement/active', { params: { orgId } }),
  confirms: (id: number) => request.get(`/announcement/${id}/confirms`),
}

const message = {
  send: (data: any) => request.post('/remote/message', data),
  messages: (params?: any) => request.get('/remote/messages', { params }),
  unreadCount: () => request.get('/remote/unread-count'),
  markRead: (id: number) => request.put(`/remote/${id}/read`),
  markAllRead: () => request.put('/remote/read-all'),
}

const answer = {
  list: (params?: any) => request.get('/answer/', { params }),
  create: (data: any) => request.post('/answer/', data),
  update: (id: number, data: any) => request.put(`/answer/${id}`, data),
  remove: (id: number) => request.delete(`/answer/${id}`),
  publishByTimer: (id: number) => request.post(`/answer/${id}/publish/timer`),
  publishByDecrypt: (id: number, password: string) => request.post(`/answer/${id}/publish/decrypt`, { password }),
  published: (classId?: number) => request.get('/answer/published', { params: { classId } }),
  detail: (id: number) => request.get(`/answer/${id}/detail`),
  logs: (id: number) => request.get(`/answer/${id}/logs`),
}

const health = {
  report: (computerId: number, data: any) => request.post('/health/report', null, { params: { computerId }, ...data }),
  latest: (computerId: number) => request.get('/health/latest', { params: { computerId } }),
  history: (computerId: number, days?: number) => request.get('/health/history', { params: { computerId, days } }),
  dashboard: (orgId?: number) => request.get('/health/dashboard', { params: { orgId } }),
  fix: (computerId: number, data: any) => request.post(`health/fix/${computerId}`, data),
  statusList: (orgId?: number) => request.get('/health/status-list', { params: { orgId } }),
}

const computer = {
  register: (data: any) => request.post('/computer/register', data),
  heartbeat: (id: number, data: any) => request.post(`/computer/${id}/heartbeat`, data),
  list: (params?: any) => request.get('/computer', { params }),
  detail: (id: number) => request.get(`/computer/${id}`),
  update: (id: number, data: any) => request.put(`/computer/${id}`, data),
  remove: (id: number) => request.delete(`/computer/${id}`),
  onlineCount: (orgId?: number) => request.get('/computer/online-count', { params: { orgId } }),
  updateIslandConfig: (id: number, configJson: string) => request.put(`/computer/${id}/island-config`, { islandConfigJson: configJson }),
}

const tokenApi = {
  list: (params?: any) => request.get('/token/', { params }),
  create: (data: any) => request.post('/token/', data),
  revoke: (id: number) => request.delete(`/token/${id}`),
  publicInfo: (code: string) => request.get('/token/public/info', { params: { code } }),
}

const plugin = {
  list: () => request.get('/plugin/'),
  upload: (data: FormData) => request.post('/plugin/upload', data, { headers: {'Content-Type': 'multipart/form-data'} }),
  enable: (id: number) => request.post(`/plugin/${id}/enable`),
  disable: (id: number) => request.post(`/plugin/${id}/disable`),
  configSchema: (id: number) => request.get(`/plugin/${id}/config-schema`),
  updateConfig: (id: number, values: any) => request.put(`/plugin/${id}/config`, values),
  frontendPlugin: (code: string) => request.get(`/plugin/frontend/${code}`),
}

export default {
  auth, org, user, island, exam, mode, weather, announcement,
  message, answer, health, computer, token: tokenApi, plugin
}
export { request }
