import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Ref } from 'vue'
import dayjs from 'dayjs'

interface ExamInfo {
  id: number
  name: string
  startTime: string
  endTime: string
  releasePassword?: string
  releasePasswordHash?: string
  scopeType: number
  targetOrgIds?: number[]
  targetComputerIds?: number[]
  status: number
}

export const useExamStore = defineStore('exam', () => {
  // 考试信息
  const currentExam: Ref<ExamInfo | null> = ref(null)
  
  // 状态
  const inExamMode = ref(false)
  const locked = ref(false)
  const examStartTime = ref<Date | null>(null)
  const examEndTime = ref<Date | null>(null)
  const heartbeatInterval = ref<NodeJS.Timeout | null>(null)

  // 计算属性
  const remainingTime = computed(() => {
    if (!examEndTime.value || !examStartTime.value) return null

    const now = new Date()
    const end = examEndTime.value
    const start = examStartTime.value
    
    const totalMs = end.getTime() - now.getTime()
    
    if (totalMs <= 0) {
      return {
        hours: 0,
        minutes: 0,
        seconds: 0,
        totalSeconds: 0
      }
    }

    const totalSeconds = Math.floor(totalMs / 1000)
    const hours = Math.floor(totalSeconds / 3600)
    const minutes = Math.floor((totalSeconds % 3600) / 60)
    const seconds = totalSeconds % 60

    return { hours, minutes, seconds, totalSeconds }
  })

  const remainingPercent = computed(() => {
    if (!examEndTime.value || !examStartTime.value || !remainingTime.value) return 100

    const totalExamMs = examEndTime.value.getTime() - examStartTime.value.getTime()
    const remainingMs = examEndTime.value.getTime() - new Date().getTime()
    
    return Math.round((remainingMs / totalExamMs) * 100)
  })

  const examDuration = computed(() => {
    if (!examStartTime.value || !examEndTime.value) return 0
    return (examEndTime.value.getTime() - examStartTime.value.getTime()) / 1000
  })

  // 方法
  const startExam = (examData: ExamInfo) => {
    currentExam.value = examData
    inExamMode.value = true
    locked.value = true
    examStartTime.value = new Date(examData.startTime)
    examEndTime.value = new Date(examData.endTime)

    // 开始心跳
    startHeartbeat()

    // 注册按键拦截
    registerKeyLock()
  }

  const endExam = () => {
    inExamMode.value = false
    locked.value = false
    currentExam.value = null
    examStartTime.value = null
    examEndTime.value = null

    // 停止心跳
    stopHeartbeat()

    // 取消按键拦截
    unregisterKeyLock()
  }

  const startHeartbeat = () => {
    stopHeartbeat() // 先停止现有定时器

    heartbeatInterval.value = setInterval(() => {
      sendHeartbeat()
    }, 10 * 1000) // 10秒心跳
  }

  const stopHeartbeat = () => {
    if (heartbeatInterval.value) {
      clearInterval(heartbeatInterval.value)
      heartbeatInterval.value = null
    }
  }

  const sendHeartbeat = async () => {
    if (!currentExam.value) return

    try {
      const serverUrl = await getServerUrl()
      if (!serverUrl) return

      const heartbeatData = {
        examId: currentExam.value.id,
        timestamp: Date.now(),
        status: 'active'
      }

      const response = await fetch(`${serverUrl}/api/v1/exam/heartbeat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(heartbeatData)
      })

      if (!response.ok) {
        console.error('心跳发送失败:', response.status)
      }
    } catch (error) {
      console.error('心跳发送错误:', error)
    }
  }

  // 键盘锁定相关
  const keyDownHandler = (e: KeyboardEvent) => {
    if (!inExamMode.value || !locked.value) return

    // 阻止退出全屏和开发者工具的快捷键
    const blockedKeys = [
      'F11',           // 退出全屏
      'F12',           // 开发者工具
      'Escape',
      'Ctrl+W',
      'Ctrl+Q',
      'Alt+F4',
      'Ctrl+Alt+Delete'
    ]

    const keyCombo = [
      e.ctrlKey ? 'Ctrl+' : '',
      e.altKey ? 'Alt+' : '',
      e.shiftKey ? 'Shift+' : '',
      e.key
    ].join('')

    if (blockedKeys.includes(keyCombo) || blockedKeys.includes(e.key)) {
      e.preventDefault()
      e.stopPropagation()
      
      // 可选：显示警告提示
      if (window.examAPI) {
        window.examAPI.endExam() // 通知主进程可能有违规操作
      }
      
      return false
    }
  }

  const registerKeyLock = () => {
    document.addEventListener('keydown', keyDownHandler, true)
  }

  const unregisterKeyLock = () => {
    document.removeEventListener('keydown', keyDownHandler, true)
  }

  // 辅助方法
  const getServerUrl = async (): Promise<string> => {
    return new Promise((resolve) => {
      try {
        // @ts-ignore
        window.examAPI?.getServerUrl?.().then(serverUrl => {
          resolve(serverUrl || '')
        }).catch(() => {
          resolve('')
        })
      } catch {
        resolve('')
      }
    })
  }

  const formatRemainingTime = (time: { hours: number; minutes: number; seconds: number }) => {
    const pad = (n: number) => n.toString().padStart(2, '0')
    return `${pad(time.hours)}:${pad(time.minutes)}:${pad(time.seconds)}`
  }

  const formatDateTime = (dateTime: string) => {
    return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
  }

  return {
    // 状态
    currentExam,
    inExamMode,
    locked,
    remainingTime,
    remainingPercent,
    examDuration,

    // 方法
    startExam,
    endExam,
    startHeartbeat,
    stopHeartbeat,
    sendHeartbeat,
    formatRemainingTime,
    formatDateTime
  }
})