import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Ref } from 'vue'

interface IslandConfig {
  position: string
  height: number
  backgroundColor: string
  textColor: string
  borderRadius: number
  showLogo: boolean
  logoUrl: string
  showDateTime: boolean
  dateTimeFormat: string
  showCurrentCourse: boolean
  showWeather: boolean
  showMarquee: boolean
  showModeIcon: boolean
  showHealthDot: boolean
  showMessageBadge: boolean
  autoHideFullscreen: boolean
}

interface WeatherData {
  temperature: number
  weather: string
  weather_icon: string
  humidity: number
  wind_direction: string
  wind_power: string
}

interface CourseData {
  subject: string
  teacherName: string
  period: number
  room: string
  startTime: string
  endTime: string
}

interface ModeData {
  name: string
  code: string
  icon: string
  color: string
  isLockScreen: boolean
}

export const useIslandStore = defineStore('island', () => {
  // 配置
  const config: Ref<IslandConfig> = ref({
    position: 'top-center',
    height: 48,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    textColor: '#ffffff',
    borderRadius: 24,
    showLogo: true,
    logoUrl: '',
    showDateTime: true,
    dateTimeFormat: 'YYYY-MM-DD HH:mm:ss',
    showCurrentCourse: true,
    showWeather: true,
    showMarquee: true,
    showModeIcon: true,
    showHealthDot: true,
    showMessageBadge: true,
    autoHideFullscreen: false
  })

  // 数据
  const weatherData: Ref<WeatherData | null> = ref(null)
  const currentCourse: Ref<CourseData | null> = ref(null)
  const currentMode: Ref<ModeData | null> = ref(null)
  const unreadMessageCount = ref(0)
  const latestAnnouncement = ref('')
  const healthStatus = ref<'green' | 'yellow' | 'red'>('green')
  const healthReport = ref<Record<string, any> | null>(null)

  // 状态
  const connected = ref(false)
  const loading = ref(false)

  // 计算属性
  const barStyle = computed(() => {
    const style: Record<string, string> = {
      position: 'fixed',
      left: config.value.position.includes('left') ? '20px' : 'auto',
      right: config.value.position.includes('right') ? '20px' : 'auto',
      top: config.value.position.startsWith('top') ? '0' : 'auto',
      bottom: config.value.position.startsWith('bottom') ? '0' : 'auto',
      height: `${config.value.height}px`,
      backgroundColor: config.value.backgroundColor,
      borderRadius: `${config.value.borderRadius}px`,
      color: config.value.textColor,
      zIndex: '2147483647'
    }

    if (config.value.position === 'top-center') {
      style.left = '50%'
      style.transform = 'translateX(-50%)'
    }

    return style
  })

  // 方法
  const updateConfig = (newConfig: Partial<IslandConfig>) => {
    Object.assign(config.value, newConfig)
  }

  const setWeatherData = (data: WeatherData) => {
    weatherData.value = data
  }

  const setCurrentCourse = (data: CourseData) => {
    currentCourse.value = data
  }

  const setCurrentMode = (data: ModeData) => {
    currentMode.value = data
  }

  const setUnreadMessageCount = (count: number) => {
    unreadMessageCount.value = count
  }

  const setLatestAnnouncement = (announcement: string) => {
    latestAnnouncement.value = announcement
  }

  const setHealthStatus = (status: 'green' | 'yellow' | 'red') => {
    healthStatus.value = status
  }

  const setHealthReport = (report: Record<string, any>) => {
    healthReport.value = report
  }

  const setConnected = (status: boolean) => {
    connected.value = status
  }

  const setLoading = (status: boolean) => {
    loading.value = status
  }

  // 获取配置方法
  const getServerUrl = (): string => {
    // @ts-ignore
    return window.islandAPI?.getServerUrl?.() || ''
  }

  const getWsUrl = (): string => {
    // @ts-ignore
    return window.islandAPI?.getWsUrl?.() || ''
  }

  const getScreenSize = async (): Promise<{ width: number; height: number }> => {
    try {
      // @ts-ignore
      return await window.islandAPI?.getScreenSize?.() || { width: 1920, height: 1080 }
    } catch {
      return { width: 1920, height: 1080 }
    }
  }

  const startExam = (data: any) => {
    // @ts-ignore
    window.islandAPI?.startExam?.(data)
  }

  const endExam = () => {
    // @ts-ignore
    window.islandAPI?.endExam?.()
  }

  const resizeIsland = (size: { width: number }) => {
    // @ts-ignore
    window.islandAPI?.resizeIsland?.(size)
  }

  const setAlwaysOnTop = (flag: boolean) => {
    // @ts-ignore
    window.islandAPI?.setAlwaysOnTop?.(flag)
  }

  return {
    // 状态
    config,
    weatherData,
    currentCourse,
    currentMode,
    unreadMessageCount,
    latestAnnouncement,
    healthStatus,
    healthReport,
    connected,
    loading,

    // 计算属性
    barStyle,

    // 方法
    updateConfig,
    setWeatherData,
    setCurrentCourse,
    setCurrentMode,
    setUnreadMessageCount,
    setLatestAnnouncement,
    setHealthStatus,
    setHealthReport,
    setConnected,
    setLoading,
    getServerUrl,
    getWsUrl,
    getScreenSize,
    startExam,
    endExam,
    resizeIsland,
    setAlwaysOnTop
  }
})