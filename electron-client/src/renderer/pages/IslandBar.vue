<template>
  <div class="island-bar" :style="barStyle" @mouseenter="hovering = true" @mouseleave="hovering = false">
    <!-- 校徽/Logo卡片 -->
    <div class="card logo-card" v-if="config.showLogo">
      <img :src="config.logoUrl || defaultLogo" alt="logo" class="logo-img">
    </div>

    <!-- 日期时间卡片 -->
    <div class="card time-card" v-if="config.showDateTime">
      <span class="time-text">{{ formattedTime }}</span>
    </div>

    <!-- 当前课程卡片 -->
    <div class="card course-card" v-if="config.showCurrentCourse && currentCourse" @mouseenter="showCourseDetail = true" @mouseleave="showCourseDetail = false">
      <el-icon><Reading /></el-icon>
      <span>{{ currentCourse.subject }} - {{ currentCourse.teacherName }}</span>
      <!-- 悬停详情 -->
      <div class="tooltip course-detail" v-show="showCourseDetail">
        <div>第{{ currentCourse.period }}节</div>
        <div>教室: {{ currentCourse.room || '-' }}</div>
        <div>{{ currentCourse.startTime }} - {{ currentCourse.endTime }}</div>
      </div>
    </div>

    <!-- 天气卡片 -->
    <div class="card weather-card" v-if="config.showWeather && weatherData"
         @mouseenter="showWeatherDetail = true" @mouseleave="showWeatherDetail = false">
      <span class="weather-icon">{{ weatherIconMap[weatherData.weather_icon] || '🌤️' }}</span>
      <span>{{ weatherData.temperature }}°C</span>
      <div class="tooltip weather-detail" v-show="showWeatherDetail">
        <div>{{ weatherData.weather }}</div>
        <div>湿度: {{ weatherData.humidity }}%</div>
        <div>风向: {{ weatherData.wind_direction }} {{ weatherData.wind_power }}</div>
        <template v-if="hourlyForecast?.length">
          <hr style="border-color:rgba(255,255,255,0.1);margin:4px 0">
          <div v-for="h in hourlyForecast.slice(0, 3)" :key="h.fx_time" style="font-size:11px;display:flex;justify-content:space-between;gap:8px">
            <span>{{ h.fx_time }}</span><span>{{ h.temp }}° {{ h.weather }}</span>
          </div>
        </template>
      </div>
    </div>

    <!-- 公告跑马灯 -->
    <div class="card marquee-card" v-if="config.showMarquee && latestAnnouncement">
      <marquee behavior="scroll" direction="left" :style="{ color: config.textColor }">{{ latestAnnouncement.title }}</marquee>
    </div>

    <!-- 情景模式标识 -->
    <div class="card mode-card" v-if="config.showModeIcon && currentMode">
      <span class="mode-dot" :style="{ background: currentMode.color }"></span>
      <span>{{ currentMode.name }}</span>
    </div>

    <!-- 健康状态 -->
    <div class="card health-card" v-if="config.showHealthDot"
         @mouseenter="showHealthDetail = true" @mouseleave="showHealthDetail = false">
      <span class="health-dot" :class="'dot-' + healthStatus"></span>
      <div class="tooltip health-detail" v-show="showHealthDetail">
        <div>CPU: {{ healthReport?.cpuTemp || '-' }}°C</div>
        <div>磁盘: {{ healthReport?.diskFreeGb || '-' }}GB</div>
        <div>内存: {{ healthReport?.memoryUsage || '-' }}%</div>
      </div>
    </div>

    <!-- 消息角标 -->
    <div class="card msg-card" v-if="config.showMessageBadge" @click="openMessageList">
      <el-icon :size="16"><Bell /></el-icon>
      <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { io } from 'socket.io-client'
import dayjs from 'dayjs'

// 类型定义
interface IslandConfig {
  position: string; height: number; backgroundColor: string; textColor: string
  borderRadius: number; showLogo: boolean; logoUrl: string
  showDateTime: boolean; dateTimeFormat: string; showCurrentCourse: boolean
  showWeather: boolean; showMarquee: boolean; showModeIcon: boolean
  showHealthDot: boolean; showMessageBadge: boolean; autoHideFullscreen: boolean
}

const defaultConfig: IslandConfig = {
  position: 'top-center', height: 48, backgroundColor: 'rgba(0,0,0,0.7)',
  textColor: '#ffffff', borderRadius: 24, showLogo: true, logoUrl: '',
  showDateTime: true, dateTimeFormat: 'YYYY-MM-DD HH:mm:ss',
  showCurrentCourse: true, showWeather: true, showMarquee: true,
  showModeIcon: true, showHealthDot: true, showMessageBadge: true,
  autoHideFullscreen: false
}

const config = reactive<IslandConfig>({ ...defaultConfig })
const hovering = ref(false)
const currentTime = ref(new Date())
const unreadCount = ref(0)
const healthStatus = ref('green') // green/yellow/red
const healthReport = ref<any>(null)
const weatherData = ref<any>(null)
const hourlyForecast = ref<any[]>([])
const currentCourse = ref<any>(null)
const currentMode = ref<any>(null)
const latestAnnouncement = ref<any>(null)
const showCourseDetail = ref(false)
const showWeatherDetail = ref(false)
const showHealthDetail = ref(false)

let timeTimer: any = null
let socket: any = null

const defaultLogo = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCIgdmlld0JveD0iMCAwIDQwIDQwIj48Y2lyY2xlIGN4PSIyMCIgY3k9IjIwIiBmaWxsPSIjNDA5ZWZmIi8+PHRleHQgeD0iMjAiIHk9IjI2IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSJ3aGl0ZSIgZm9udC1zaXplPSIxNCiI+5bGxPC90ZXh0Pjwvc3ZnPg=='

const weatherIconMap: Record<string, string> = {
  '100': '☀️', '101': '⛅', '102': '☁️', '103': '🌥️', '104': '☁️',
  '300': '🌧️', '301': '🌦️', '302': '⛈️', '306': '❄️', '307': '🌨️',
  '309': '🌧️', '310': '🌧️', '312': '❄️', '314': '🌨️', '399': '🌡️',
  '500': '☀️', '502': '🌤️', '503': '⛅', '504': '☁️', '800': '☀️',
  '801': '⛅', '802': '☁️', '803': '☁️', '804': '☁️',
  '900': '🌡️', '901': '❄️', '999': '未知',
}

const barStyle = computed(() => ({
  position: 'fixed' as const,
  left: config.position.includes('left') ? '20px' : 'auto',
  right: config.position.includes('right') ? '20px' : 'auto',
  transform: config.position === 'top-center' ? 'translateX(-50%)' : undefined,
  top: config.position.startsWith('top') ? '0' : 'auto',
  bottom: config.position.startsWith('bottom') ? '0' : 'auto',
  left: config.position === 'top-center' ? '50%' : (config.position as string).includes('left') ? '20px' : 'auto',
  height: `${config.height}px`,
  backgroundColor: config.backgroundColor,
  borderRadius: `${config.borderRadius}px`,
  color: config.textColor,
  zIndex: 2147483647,
  display: 'flex',
  alignItems: 'center',
  padding: '0 16px',
  gap: '12px',
  fontFamily: '-apple-system,BlinkMacSystemFont,"PingFang SC","Microsoft YaHei",sans-serif',
  fontSize: '13px',
  transition: 'opacity 0.3s, transform 0.3s',
  opacity: hovering.value ? 1 : 0.9,
}))

const formattedTime = computed(() => dayjs(currentTime.value).format(config.dateTimeFormat.replace('YYYY','YYYY').replace('MM','MM').replace('DD','DD').replace('HH:mm:ss','HH:mm:ss')))

// 定时更新时间
onMounted(() => {
  timeTimer = setInterval(() => { currentTime.value = new Date() }, 1000)
  connectWebSocket()
  fetchIslandConfig()
  fetchWeather()
})
onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer)
  if (socket) socket.disconnect()
})

function connectWebSocket() {
  // @ts-ignore
  const wsUrl = (window as any).islandAPI?.getWsUrl?.() || ''
  if (!wsUrl) return

  socket = io(wsUrl, { path: '/ws', transports: ['websocket'], reconnection: true })

  socket.on('connect', () => console.log('[Island] WebSocket connected'))
  socket.on(`computer/${getComputerId()}/command`, handleCommand)
  socket.on(`computer/${getComputerId()}/island/config`, () => fetchIslandConfig())
  socket.on(`computer/${getComputerId()}/exam_command`, handleExamCommand)
  socket.on(`/org/*/emergency`, handleEmergencyBroadcast)
  socket.on('/queue/message', (msg: any) => {
    if (msg.type === 'remote_message') unreadCount.value++
  })
}

function handleCommand(data: any) {
  console.log('[Island] Command:', data)
  switch (data.type) {
    case 'mode_switch':
      currentMode.value = data.data.mode
      break
    case 'health_fix':
      executeHealthFix(data.data.action)
      break
    case 'config_update':
      fetchIslandConfig()
      break
  }
}

function handleExamCommand(data: any) {
  if (data.type === 'exam_command' && data.data.command === 'start') {
    // @ts-ignore
    window.islandAPI?.startExam?.(data.data)
  }
}

function handleEmergencyBroadcast(data: any) {
  alert(`【紧急广播】\n${data.data.content}`)
  // 发送确认回执
  socket?.emit('broadcast_confirm', { announcementId: data.data.id })
}

function getComputerId(): string {
  return localStorage.getItem('computerId') || 'unknown'
}

async function fetchIslandConfig() {
  try {
    // @ts-ignore
    const serverUrl = (window as any).islandAPI?.getServerUrl?.() || ''
    const cid = getComputerId()
    const res = await fetch(`${serverUrl}/api/v1/island/config/effective?computerId=${cid}`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    if (res.ok) { const d = await res.json(); Object.assign(config, d.data) }
  } catch (e) { /* 使用默认配置 */ }
}

async function fetchWeather() {
  try {
    // @ts-ignore
    const serverUrl = (window as any).islandAPI?.getServerUrl?.() || ''
    const res = await fetch(`${serverUrl}/api/v1/weather/current?city=北京`)
    if (res.ok) { const d = await res.json(); weatherData.value = d.data }
    // 获取逐小时预报
    const fRes = await fetch(`${serverUrl}/api/v1/weather/forecast?city=北京`)
    if (fRes.ok) { const fd = await fRes.json(); hourlyForecast.value = fd.data?.hourly_forecast || [] }
  } catch (e) { /* */ }
  setInterval(fetchWeather, 30 * 60 * 1000) // 30分钟刷新
}

function openMessageList() { /* 打开消息列表窗口或弹窗 */ }

// 全屏应用自动隐藏监听
if (typeof window !== 'undefined') {
  document.addEventListener('fullscreenchange', () => {
    if (config.autoHideFullscreen && document.fullscreenElement) {
      ;(document.querySelector('.island-bar') as HTMLElement)?.style.setProperty('display', 'none')
    } else {
      ;(document.querySelector('.island-bar') as HTMLElement)?.style.setProperty('display', 'flex')
    }
  })
}
</script>

<style scoped lang="scss">
.island-bar {
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  user-select: none;
  overflow: hidden;
  white-space: nowrap;
}
.card {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: default;
  position: relative;
  flex-shrink: 0;
}
.logo-card .logo-img { width: 28px; height: 28px; border-radius: 6px; object-fit: cover; }
.time-card .time-text { font-size: 13px; letter-spacing: 0.5px; }
.weather-icon { font-size: 18px; }
.marquee-card { max-width: 200px; overflow: hidden; marquee { max-width: 100%; }}
.mode-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.health-dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block;
  &.dot-green { background: #67c23a; box-shadow: 0 0 4px #67c23a; }
  &.dot-yellow { background: #e6a23c; box-shadow: 0 0 4px #e6a23c; }
  &.dot-red { background: #f56c6c; box-shadow: 0 0 4px #f56c6c; animation: pulse 1s infinite; }
}
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
.msg-card { cursor: pointer; position: relative; }

.tooltip {
  position: absolute; top: 100%; left: 0; margin-top: 8px;
  background: rgba(0,0,0,0.85); color: #fff; padding: 8px 12px;
  border-radius: 8px; font-size: 12px; white-space: normal; min-width: 150px;
  z-index: 9999; pointer-events: none; line-height: 1.6;
  &::before { content: ''; position: absolute; top: -4px; left: 20px;
    border-left: 5px solid transparent; border-right: 5px solid transparent; border-bottom: 5px solid rgba(0,0,0,0.85); }
}
</style>
