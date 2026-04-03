import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import api from '@/api'

export interface IslandConfig {
  position: string; height: number; backgroundColor: string; textColor: string
  borderRadius: number; showLogo: boolean; logoUrl: string
  showDateTime: boolean; dateTimeFormat: string
  showCurrentCourse: boolean; showWeather: boolean; showMarquee: boolean
  showModeIcon: boolean; showHealthDot: boolean; showMessageBadge: boolean
  autoHideFullscreen: boolean
}

const DEFAULT_CONFIG: IslandConfig = {
  position: 'top-center', height: 48, backgroundColor: 'rgba(0,0,0,0.7)',
  textColor: '#ffffff', borderRadius: 24, showLogo: true, logoUrl: '',
  showDateTime: true, dateTimeFormat: 'YYYY-MM-DD HH:mm:ss',
  showCurrentCourse: true, showWeather: true, showMarquee: true,
  showModeIcon: true, showHealthDot: true, showMessageBadge: true,
  autoHideFullscreen: false
}

export const useIslandStore = defineStore('island', () => {
  const globalConfig = reactive<IslandConfig>({ ...DEFAULT_CONFIG })
  const previewMode = ref(false)

  function updateConfig(partial: Partial<IslandConfig>) {
    Object.assign(globalConfig, partial)
  }

  function resetToDefault() { Object.assign(globalConfig, { ...DEFAULT_CONFIG }) }
  
  async function saveGlobalConfig() {
    await api.island.updateGlobal(globalConfig)
    return true
  }

  return { globalConfig, previewMode, updateConfig, resetToDefault, saveGlobalConfig }
})
