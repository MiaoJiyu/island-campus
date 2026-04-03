import { app, BrowserWindow, ipcMain, screen, globalShortcut, dialog, shell } from 'electron'
import path from 'path'

let islandWindow: BrowserWindow | null = null
let examWindow: BrowserWindow | null = null
let mainWindow: BrowserWindow | null = null

// 服务器配置
const SERVER_URL = process.env.SERVER_URL || 'https://localhost:8443'
const WS_URL = SERVER_URL.replace('https', 'wss').replace('http', 'ws')

function createIslandWindow() {
  const { width: screenWidth } = screen.getPrimaryDisplay().workAreaSize
  const win = new BrowserWindow({
    width: screenWidth > 1200 ? 800 : screenWidth,
    height: 48,
    x: Math.round((screenWidth - (screenWidth > 1200 ? 800 : screenWidth)) / 2),
    y: 0,
    frame: false,
    transparent: true,
    alwaysOnTop: true,
    skipTaskbar: true,
    resizable: false,
    hasShadow: true,
    focusable: false,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, '../renderer/preload/island.cjs')
    }
  })
  win.setAlwaysOnTop(true, 'screen-saver')
  // 点击穿透（当配置focusable=false时）
  win.ignoreMouseEvents(true, { forward: true })

  if (process.env.NODE_ENV === 'development') {
    win.loadURL('http://localhost:5174/#/island')
  } else {
    win.loadFile(path.join(__dirname, '../renderer/index.html'), { hash: '/island' })
  }

  return win
}

function createMainWindow() {
  const win = new BrowserWindow({
    width: 900, height: 600, show: false,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true
    }
  })
  if (process.env.NODE_ENV === 'development') {
    win.loadURL('http://localhost:5174/#/main')
  }
  return win
}

function createExamWindow(examData: any) {
  const { width, height } = screen.getPrimaryDisplay().workAreaSize
  const win = new BrowserWindow({
    width, height,
    x: 0, y: 0,
    frame: false,
    alwaysOnTop: true,
    fullscreen: true,
    skipTaskbar: true,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, '../renderer/preload/exam.cjs')
    }
  })
  // 禁用所有快捷键和开发者工具
  win.setMenu(null)
  win.webContents.setVisualZoomLevelLimits(0, 0)

  if (process.env.NODE_ENV === 'development') {
    win.loadURL(`http://localhost:5174/#/exam?data=${encodeURIComponent(JSON.stringify(examData))}`)
  } else {
    win.loadFile(path.join(__dirname, '../renderer/index.html'), {
      hash: `/exam?data=${encodeURIComponent(JSON.stringify(examData))}`
    })
  }

  // 禁止关闭(考试模式)
  win.on('close', (e) => { e.preventDefault() })

  return win
}

app.whenReady().then(() => {
  islandWindow = createIslandWindow()
  mainWindow = createMainWindow()

  // IPC handlers
  ipcMain.handle('get-screen-size', () => screen.getPrimaryDisplay().workAreaSize)
  ipcMain.handle('get-server-url', () => SERVER_URL)
  ipcMain.handle('get-ws-url', () => WS_URL)

  ipcMain.on('start-exam', (_event, data) => {
    if (!examWindow) examWindow = createExamWindow(data)
  })

  ipcMain.on('end-exam', () => {
    if (examWindow) { examWindow.destroy(); examWindow = null }
  })

  ipcMain.on('resize-island', (_event, { width }) => {
    if (islandWindow) {
      const { width: sw } = screen.getPrimaryDisplay().workAreaSize
      islandWindow.setBounds({ x: Math.round((sw - width) / 2), width }, true)
    }
  })

  ipcMain.on('set-island-always-on-top', (_event, flag) => {
    if (islandWindow) islandWindow.setAlwaysOnTop(flag)
  })

  ipcMain.on('open-devtools', () => {
    mainWindow?.show()
    mainWindow?.webContents.openDevTools()
  })
})

app.on('window-all-closed', () => {
  // 保持后台运行，不退出应用（灵动岛需要常驻）
})

app.on('before-quit', () => {
  // 清理资源
})
