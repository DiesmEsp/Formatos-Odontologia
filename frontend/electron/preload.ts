import { contextBridge, ipcRenderer } from 'electron';

contextBridge.exposeInMainWorld('api', {
  shell: {
    openPath: (filePath: string) => ipcRenderer.invoke('shell:openPath', filePath),
  },
});
