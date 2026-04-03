const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('examAPI', {
  endExam: () => ipcRenderer.send('end-exam'),
  getServerUrl: () => ipcRenderer.invoke('get-server-url'),
})
