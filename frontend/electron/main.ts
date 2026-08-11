import { app, BrowserWindow, dialog, ipcMain, shell } from 'electron';
import { spawn, ChildProcess } from 'child_process';
import path from 'path';

const PORT = 7070;
const HEALTH_URL = `http://localhost:${PORT}/health`;

let mainWindow: BrowserWindow | null = null;
let serverProcess: ChildProcess | null = null;

function resolveResource(...segments: string[]): string {
  if (app.isPackaged) {
    return path.join(process.resourcesPath, ...segments);
  }
  return path.join(__dirname, '..', ...segments);
}

function startJavaBackend(): void {
  const javaBin = resolveResource('jre', 'bin', 'java.exe');
  const jarPath = resolveResource('backend.jar');

  console.log(`[Electron] Iniciando backend Java...`);
  console.log(`[Electron] Java: ${javaBin}`);
  console.log(`[Electron] JAR: ${jarPath}`);

  serverProcess = spawn(javaBin, [
    '-jar', jarPath,
    `--server.port=${PORT}`,
  ], {
    stdio: ['ignore', 'pipe', 'pipe'],
  });

  serverProcess.stdout?.on('data', (data: Buffer) => {
    const lines = data.toString().trim();
    if (lines) console.log(`[Java] ${lines}`);
  });

  serverProcess.stderr?.on('data', (data: Buffer) => {
    const lines = data.toString().trim();
    if (lines) console.error(`[Java] ${lines}`);
  });

  serverProcess.on('error', (err) => {
    console.error('[Electron] Error al iniciar backend:', err.message);
  });

  serverProcess.on('exit', (code, signal) => {
    console.log(`[Electron] Backend terminado: code=${code} signal=${signal}`);
    serverProcess = null;
    if (mainWindow && !mainWindow.isDestroyed()) {
      mainWindow.close();
    }
  });
}

async function waitForServer(timeoutMs = 60000): Promise<boolean> {
  console.log('[Electron] Esperando backend...');
  const startTime = Date.now();

  while (Date.now() - startTime < timeoutMs) {
    try {
      const res = await fetch(HEALTH_URL);
      if (res.ok) {
        console.log(`[Electron] Backend listo en ${PORT}`);
        return true;
      }
    } catch {}

    await new Promise((r) => setTimeout(r, 500));
  }

  console.error('[Electron] Timeout esperando backend');
  return false;
}

function createMainWindow(): void {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 720,
    minWidth: 1100,
    minHeight: 680,
    title: 'Formatos Odontologicos',
    show: false,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: false,
      webSecurity: false,
    },
  });

  mainWindow.once('ready-to-show', () => {
    mainWindow?.show();
  });

  mainWindow.on('closed', () => {
    mainWindow = null;
  });

  if (process.env.NODE_ENV === 'development' || !app.isPackaged) {
    mainWindow.loadURL(`http://localhost:5173`);
  } else {
    mainWindow.loadFile(resolveResource('public', 'index.html'));
  }
}

function stopJavaBackend(): void {
  if (!serverProcess) return;

  console.log('[Electron] Deteniendo backend Java...');

  try {
    fetch(`http://localhost:${PORT}/shutdown`, { method: 'POST' }).catch(() => {});
  } catch {}

  setTimeout(() => {
    if (serverProcess && !serverProcess.killed) {
      if (process.platform === 'win32') {
        spawn('taskkill', ['/pid', String(serverProcess.pid), '/f', '/t']);
      } else {
        serverProcess.kill('SIGTERM');
      }
    }
  }, 5000);
}

app.on('ready', async () => {
  ipcMain.handle('shell:openPath', async (_event, filePath: string) => {
    const normalized = path.normalize(filePath);
    console.log('[Electron] Abriendo ubicacion:', normalized);
    try {
      await shell.showItemInFolder(normalized);
      return { success: true };
    } catch (err) {
      console.error('[Electron] Error al abrir ubicacion:', err);
      return { success: false, error: 'No se pudo abrir la ubicacion' };
    }
  });

  startJavaBackend();

  const ready = await waitForServer();
  if (!ready) {
    dialog.showErrorBox(
      'Error de inicio',
      'No se pudo iniciar el servidor backend.\nVerifique que Java 21 este instalado.'
    );
    app.quit();
    return;
  }

  createMainWindow();
});

app.on('window-all-closed', () => {
  stopJavaBackend();
  app.quit();
});

app.on('before-quit', () => {
  stopJavaBackend();
});

app.on('activate', () => {
  if (mainWindow === null) {
    createMainWindow();
  }
});
