<template>
  <div class="exam-lock">
    <!-- 考试信息头 -->
    <div class="exam-header">
      <div class="exam-title">{{ examInfo.name }}</div>
      <div class="exam-subtitle">正在考试中</div>
    </div>

    <!-- 数字时钟 -->
    <div class="digital-clock">
      <div class="time-display">
        <span class="time-hour">{{ formattedTime.hour }}</span>
        <span class="time-separator">:</span>
        <span class="time-minute">{{ formattedTime.minute }}</span>
        <span class="time-separator">:</span>
        <span class="time-second">{{ formattedTime.second }}</span>
      </div>
      <div class="date-display">{{ formattedDate }}</div>
    </div>

    <!-- 剩余时间 -->
    <div class="remaining-time" v-if="remainingTime">
      <div class="remaining-label">剩余时间</div>
      <div class="remaining-value">
        {{ remainingTime.hours.toString().padStart(2, '0') }}:
        {{ remainingTime.minutes.toString().padStart(2, '0') }}:
        {{ remainingTime.seconds.toString().padStart(2, '0') }}
      </div>
      <div class="time-progress-bar">
        <div class="progress-fill" :style="{ width: remainingPercent + '%' }"></div>
      </div>
    </div>

    <!-- 考试须知 -->
    <div class="exam-rules">
      <h3>考试须知</h3>
      <ul>
        <li>考试期间禁止退出全屏模式</li>
        <li>禁止使用任何其他应用程序</li>
        <li>不得查阅任何参考资料</li>
        <li>考试结束后请等待管理员解锁</li>
        <li v-if="examInfo.passwordHash">考试结束后，管理员将公布密码解锁</li>
      </ul>
    </div>

    <!-- 应急提示 -->
    <div class="emergency-hint">
      <p>如遇特殊情况需要帮助，请举手示意监考老师</p>
    </div>

    <!-- 调试信息 -->
    <div v-if="showDebugInfo" class="debug-info">
      <div>考试ID: {{ examInfo.id }}</div>
      <div>开始时间: {{ formatDateTime(examInfo.startTime) }}</div>
      <div>结束时间: {{ formatDateTime(examInfo.endTime) }}</div>
      <button @click="exitExamDebug">退出考试（调试）</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import dayjs from 'dayjs'
import { useRoute } from 'vue-router'

interface ExamInfo {
  id: number
  name: string
  startTime: string
  endTime: string
  releasePassword?: string
  releasePasswordHash?: string
}

const route = useRoute()

// 考试信息
const examInfo = reactive<ExamInfo>({
  id: 0,
  name: '未命名考试',
  startTime: '',
  endTime: ''
})

// 时间相关
const currentTime = ref(new Date())
const examEndTime = ref<Date | null>(null)
const remainingTime = ref<{
  hours: number
  minutes: number
  seconds: number
  totalSeconds: number
} | null>(null)
const remainingPercent = ref(100)

// 调试模式
const showDebugInfo = ref(false)

// 从URL参数解析考试数据
const examDataParam = computed(() => {
  try {
    const dataParam = route.query.data as string
    return dataParam ? JSON.parse(decodeURIComponent(dataParam)) : null
  } catch {
    return null
  }
})

// 格式化时间显示
const formattedTime = computed(() => {
  const hour = currentTime.value.getHours().toString().padStart(2, '0')
  const minute = currentTime.value.getMinutes().toString().padStart(2, '0')
  const second = currentTime.value.getSeconds().toString().padStart(2, '0')
  return { hour, minute, second }
})

const formattedDate = computed(() => {
  return dayjs(currentTime.value).format('YYYY年MM月DD日 dddd')
})

// 定时更新
let timeTimer: NodeJS.Timeout | null = null
let heartbeatTimer: NodeJS.Timeout | null = null

onMounted(() => {
  // 初始化考试信息
  if (examDataParam.value) {
    Object.assign(examInfo, examDataParam.value)
    examEndTime.value = new Date(examDataParam.value.endTime)
    
    // 计算剩余时间
    calculateRemainingTime()
  }

  // 设置定时器
  timeTimer = setInterval(() => {
    currentTime.value = new Date()
    calculateRemainingTime()
  }, 1000)

  // WebSocket心跳
  setupWebSocketHeartbeat()

  // 键盘事件监听（防止退出）
  setupKeyboardLock()

  // 鼠标事件监听（限制右键菜单）
  setupMouseLock()

  // 5分钟后自动显示调试信息（调试用）
  setTimeout(() => {
    showDebugInfo.value = true
  }, 5 * 60 * 1000)
})

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer)
  if (heartbeatTimer) clearInterval(heartbeatTimer)
  cleanupEventListeners()
})

// 计算剩余时间
function calculateRemainingTime() {
  if (!examEndTime.value) return

  const now = new Date()
  const end = examEndTime.value
  const totalMs = end.getTime() - now.getTime()

  if (totalMs <= 0) {
    remainingTime.value = {
      hours: 0,
      minutes: 0,
      seconds: 0,
      totalSeconds: 0
    }
    remainingPercent.value = 0
    // 考试时间结束
    handleExamTimeEnd()
    return
  }

  const totalSeconds = Math.floor(totalMs / 1000)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60

  remainingTime.value = {
    hours,
    minutes,
    seconds,
    totalSeconds
  }

  // 计算进度百分比（相对于总考试时长）
  const start = new Date(examInfo.startTime)
  const totalExamMs = end.getTime() - start.getTime()
  remainingPercent.value = Math.round((totalMs / totalExamMs) * 100)
}

// 考试时间结束处理
function handleExamTimeEnd() {
  // 显示结束提示
  alert('考试时间已结束\n请等待管理员解锁')

  // 向主进程发送结束信号
  if (window.examAPI) {
    window.examAPI.endExam()
  }
}

// WebSocket心跳
function setupWebSocketHeartbeat() {
  // 每10秒发送一次心跳
  heartbeatTimer = setInterval(() => {
    sendHeartbeat()
  }, 10 * 1000)
}

// 发送心跳
function sendHeartbeat() {
  try {
    // @ts-ignore
    const serverUrl = window.examAPI?.getServerUrl?.() || ''
    if (!serverUrl) return

    const heartbeatData = {
      examId: examInfo.id,
      timestamp: Date.now(),
      status: 'active'
    }

    fetch(`${serverUrl}/api/v1/exam/heartbeat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(heartbeatData)
    }).catch(error => {
      console.error('心跳发送失败:', error)
    })
  } catch (error) {
    console.error('心跳错误:', error)
  }
}

// 键盘锁定
function setupKeyboardLock() {
  const keyHandler = (e: KeyboardEvent) => {
    // 阻止常用退出快捷键
    const blockedKeys = [
      'F11',           // 退出全屏
      'F12',           // 开发者工具
      'Escape',        // ESC键
      'Ctrl+W',
      'Ctrl+Q',
      'Alt+F4',
      'Ctrl+Alt+Delete',
      'Tab'
    ]

    const keyString = [
      e.ctrlKey ? 'Ctrl+' : '',
      e.altKey ? 'Alt+' : '',
      e.shiftKey ? 'Shift+' : '',
      e.key
    ].join('')

    if (blockedKeys.includes(keyString) || blockedKeys.includes(e.key)) {
      e.preventDefault()
      e.stopPropagation()
      alert('考试期间禁止使用此功能')
      return false
    }
  }

  document.addEventListener('keydown', keyHandler, true)
}

// 鼠标锁定
function setupMouseLock() {
  const contextMenuHandler = (e: MouseEvent) => {
    e.preventDefault()
    return false
  }

  document.addEventListener('contextmenu', contextMenuHandler, true)
}

// 清理事件监听器
function cleanupEventListeners() {
  // 实际应用中需要记录监听器以便清理
}

// 调试功能：退出考试
function exitExamDebug() {
  if (confirm('确定要退出考试模式吗？（仅限调试使用）')) {
    if (window.examAPI) {
      window.examAPI.endExam()
    }
  }
}

// 工具函数
function formatDateTime(dateTime: string): string {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 类型声明
declare global {
  interface Window {
    examAPI: {
      endExam: () => void
      getServerUrl: () => Promise<string>
    }
  }
}
</script>

<style scoped>
.exam-lock {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a2a6c, #3a7bd5, #00d2ff);
  color: white;
  font-family: 'Arial', 'Microsoft YaHei', sans-serif;
  user-select: none;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 9999;
}

.exam-header {
  text-align: center;
  margin-bottom: 60px;
  padding: 0 20px;
}

.exam-title {
  font-size: 3rem;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.exam-subtitle {
  font-size: 1.5rem;
  opacity: 0.9;
  letter-spacing: 0.5rem;
  text-transform: uppercase;
}

.digital-clock {
  text-align: center;
  margin: 40px 0;
  padding: 30px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  min-width: 600px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.time-display {
  font-size: 6rem;
  font-weight: bold;
  display: flex;
  justify-content: center;
  align-items: baseline;
  font-family: 'Courier New', monospace;
}

.time-hour, .time-minute, .time-second {
  display: inline-block;
  min-width: 100px;
  text-align: center;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.time-separator {
  font-size: 4rem;
  color: rgba(255, 255, 255, 0.6);
  margin: 0 5px;
}

.date-display {
  font-size: 1.5rem;
  margin-top: 20px;
  opacity: 0.9;
}

.remaining-time {
  margin: 40px 0;
  text-align: center;
}

.remaining-label {
  font-size: 1.2rem;
  opacity: 0.8;
  margin-bottom: 10px;
}

.remaining-value {
  font-size: 3rem;
  font-weight: bold;
  font-family: 'Courier New', monospace;
  margin-bottom: 15px;
  letter-spacing: 0.2rem;
}

.time-progress-bar {
  width: 400px;
  height: 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  margin: 20px auto;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(to right, #00d2ff, #3a7bd5);
  border-radius: 4px;
  transition: width 1s linear;
}

.exam-rules {
  margin: 40px 0;
  padding: 30px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 15px;
  max-width: 600px;
  text-align: left;
}

.exam-rules h3 {
  font-size: 1.5rem;
  margin-bottom: 20px;
  text-align: center;
}

.exam-rules ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.exam-rules li {
  margin-bottom: 15px;
  padding-left: 30px;
  position: relative;
  font-size: 1.1rem;
  line-height: 1.6;
}

.exam-rules li:before {
  content: '⏳';
  position: absolute;
  left: 0;
  top: 2px;
}

.emergency-hint {
  margin-top: 40px;
  padding: 20px;
  border: 2px solid rgba(255, 255, 255, 0.5);
  border-radius: 10px;
  text-align: center;
  max-width: 500px;
}

.emergency-hint p {
  margin: 0;
  font-size: 1.1rem;
  color: #ffcc00;
}

.debug-info {
  position: absolute;
  bottom: 20px;
  right: 20px;
  background: rgba(0, 0, 0, 0.7);
  padding: 15px;
  border-radius: 8px;
  font-size: 12px;
  font-family: monospace;
  text-align: left;
  max-width: 300px;
}

.debug-info div {
  margin-bottom: 5px;
}

.debug-info button {
  margin-top: 10px;
  padding: 5px 10px;
  background: #ff4444;
  border: none;
  border-radius: 4px;
  color: white;
  cursor: pointer;
  font-size: 12px;
}

.debug-info button:hover {
  background: #ff6666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .exam-title {
    font-size: 2rem;
  }

  .exam-subtitle {
    font-size: 1rem;
    letter-spacing: 0.3rem;
  }

  .digital-clock {
    min-width: 90%;
    padding: 20px;
  }

  .time-display {
    font-size: 3rem;
  }

  .time-hour, .time-minute, .time-second {
    min-width: 60px;
  }

  .time-progress-bar {
    width: 300px;
  }

  .remaining-value {
    font-size: 2rem;
  }
}

/* 动画效果 */
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

.time-second {
  animation: pulse 1s infinite;
}

/* 防止滚动 */
body {
  overflow: hidden !important;
}
</style>