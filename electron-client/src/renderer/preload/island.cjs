const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('islandAPI', {
  getScreenSize: () => ipcRenderer.invoke('get-screen-size'),
  getServerUrl: () => ipcRenderer.invoke('get-server-url'),
  getWsUrl: () => ipcRenderer.invoke('get-ws-url'),
  startExam: (data) => ipcRenderer.send('start-exam', data),
  endExam: () => ipcRenderer.send('end-exam'),
  resizeIsland: (size) => ipcRenderer.send('resize-island', size),
  setAlwaysOnTop: (flag) => ipcRenderer.send('set-island-always-on-top', flag),
})
