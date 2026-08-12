import { app, BrowserWindow, dialog, ipcMain, shell } from 'electron';
import { spawn, ChildProcess } from 'child_process';
import path from 'path';
import fs from 'fs';

const PORT = 7070;
const HEALTH_URL = `http://localhost:${PORT}/health`;
const SPLASH_SIZE = { width: 400, height: 300 };

const SPLASH_HTML = `<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:"Segoe UI",system-ui,-apple-system,sans-serif;background:#0f172a;color:#f1f5f9;display:flex;flex-direction:column;align-items:center;justify-content:center;height:100vh;user-select:none;-webkit-app-region:drag}
.logo{font-size:32px;font-weight:700;margin-bottom:8px;letter-spacing:-.5px}
.subtitle{font-size:13px;color:#64748b;margin-bottom:32px}
.status-row{display:flex;align-items:center;gap:10px;font-size:14px}
.spinner{width:18px;height:18px;border:2px solid #334155;border-top-color:#3b82f6;border-radius:50%;animation:spin .8s linear infinite}
.spinner.done{border-color:#22c55e;border-top-color:#22c55e;animation:none}
.spinner.error{border-color:#ef4444;border-top-color:#ef4444;animation:none}
@keyframes spin{to{transform:rotate(360deg)}}
.log{margin-top:24px;font-size:12px;color:#475569;max-width:320px;text-align:center;line-height:1.5;display:none;white-space:pre-wrap}
.log.visible{display:block}
.error-msg{color:#ef4444;margin-top:16px;font-size:13px;text-align:center;max-width:300px;display:none}
.error-msg.visible{display:block}
</style>
</head>
<body>
<div class="logo">Formatos Odontologicos</div>
<div class="subtitle">Clinica Odontologica UNMSM</div>
<div class="status-row">
<div class="spinner" id="spinner"></div>
<span id="status">Iniciando...</span>
</div>
<div class="log" id="log"></div>
<div class="error-msg" id="error"></div>
</body>
</html>`;

let mainWindow: BrowserWindow | null = null;
let splashWindow: BrowserWindow | null = null;
let serverProcess: ChildProcess | null = null;
let bootError: string | null = null;

function resolveResource(...segments: string[]): string {
  if (app.isPackaged) {
    return path.join(process.resourcesPath, ...segments);
  }
  return path.join(__dirname, '..', ...segments);
}

function createSplashWindow(): void {
  splashWindow = new BrowserWindow({
    width: SPLASH_SIZE.width,
    height: SPLASH_SIZE.height,
    resizable: false,
    frame: false,
    transparent: false,
    alwaysOnTop: true,
    center: true,
    show: true,
    webPreferences: {
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
    },
  });

  splashWindow.loadURL(`data:text/html;charset=utf-8,${encodeURIComponent(SPLASH_HTML)}`);
}

function updateSplash(status: string, error = false): void {
  if (!splashWindow || splashWindow.isDestroyed()) return;

  const spinnerClass = error ? 'error' : '';
  splashWindow.webContents.executeJavaScript(`
    document.getElementById('status').textContent = ${JSON.stringify(status)};
    document.getElementById('spinner').className = 'spinner ${spinnerClass}';
    if (${error}) {
      document.getElementById('error').textContent = ${JSON.stringify(status)};
      document.getElementById('error').classList.add('visible');
    }
  `).catch(() => {});
}

function appendSplashLog(line: string): void {
  if (!splashWindow || splashWindow.isDestroyed()) return;
  splashWindow.webContents.executeJavaScript(`
    var log = document.getElementById('log');
    log.textContent += ${JSON.stringify(line + '\\n')};
    log.classList.add('visible');
  `).catch(() => {});
}

function showFatalError(detail: string): void {
  if (splashWindow && !splashWindow.isDestroyed()) {
    splashWindow.webContents.executeJavaScript(`
      document.getElementById('spinner').className = 'spinner error';
      document.getElementById('status').textContent = 'Error al iniciar';
      document.getElementById('error').textContent = ${JSON.stringify(detail)};
      document.getElementById('error').classList.add('visible');
      document.body.style.background = '#1a0f0f';
    `).catch(() => {});
  }
}

function getJrePath(): string {
  return resolveResource('jre', 'bin', process.platform === 'win32' ? 'java.exe' : 'java');
}

function getJarPath(): string {
  return resolveResource('backend.jar');
}

function verifyResources(): string | null {
  const jreBin = getJrePath();
  if (!fs.existsSync(jreBin)) {
    return `No se encontro JRE en:\n${jreBin}\n\nLa aplicacion no fue empaquetada correctamente.`;
  }
  const jarFile = getJarPath();
  if (!fs.existsSync(jarFile)) {
    return `No se encontro backend.jar en:\n${jarFile}\n\nLa aplicacion no fue empaquetada correctamente.`;
  }
  return null;
}

function startJavaBackend(): void {
  const jreBin = getJrePath();
  const jarFile = getJarPath();

  updateSplash('Iniciando servidor...');

  serverProcess = spawn(jreBin, [
    '-jar', jarFile,
    `--server.port=${PORT}`,
  ], {
    stdio: ['ignore', 'pipe', 'pipe'],
  });

  serverProcess.stdout?.on('data', (data: Buffer) => {
    const lines = data.toString().trim();
    if (lines) {
      console.log(`[Java] ${lines}`);
      appendSplashLog(lines);
    }
  });

  serverProcess.stderr?.on('data', (data: Buffer) => {
    const lines = data.toString().trim();
    if (lines) {
      console.error(`[Java] ${lines}`);
      appendSplashLog(`[err] ${lines}`);
    }
  });

  serverProcess.on('error', (err) => {
    const msg = `Error al ejecutar Java: ${err.message}`;
    console.error(`[Electron] ${msg}`);
    bootError = msg;
    showFatalError(msg);
  });

  serverProcess.on('exit', (code, signal) => {
    const reason = code !== null ? `code=${code}` : `signal=${signal}`;
    console.log(`[Electron] Backend termino: ${reason}`);

    if (code !== 0 && !bootError) {
      bootError = `El servidor backend termino inesperadamente (${reason}).\n\nRevise el log en la pantalla de inicio para mas detalles.`;
    }

    serverProcess = null;

    if (mainWindow && !mainWindow.isDestroyed()) {
      mainWindow.close();
    }
  });
}

async function waitForServer(timeoutMs = 60000): Promise<boolean> {
  updateSplash('Conectando al servidor...');

  const startTime = Date.now();

  while (Date.now() - startTime < timeoutMs) {
    if (bootError) {
      return false;
    }

    if (!serverProcess) {
      bootError = 'El servidor backend no pudo iniciar.';
      return false;
    }

    try {
      const res = await fetch(HEALTH_URL);
      if (res.ok) {
        updateSplash('Servidor listo. Cargando aplicacion...');
        return true;
      }
    } catch {}

    await new Promise((r) => setTimeout(r, 500));
  }

  bootError = `Timeout: el servidor no respondio en ${timeoutMs / 1000}s.\nVerifique que el puerto ${PORT} no este bloqueado.`;
  return false;
}

function closeSplash(): void {
  if (splashWindow && !splashWindow.isDestroyed()) {
    splashWindow.close();
    splashWindow = null;
  }
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
      sandbox: true,
      webSecurity: true,
    },
  });

  let shown = false;
  const revealWindow = () => {
    if (shown) return;
    shown = true;
    closeSplash();
    mainWindow?.show();
  };

  mainWindow.once('ready-to-show', revealWindow);

  const fallbackTimer = setTimeout(() => {
    if (!shown) {
      console.warn('[Electron] ready-to-show no disparo; mostrando ventana por timeout');
      revealWindow();
    }
  }, 5000);

  mainWindow.on('closed', () => {
    clearTimeout(fallbackTimer);
    mainWindow = null;
  });

  mainWindow.webContents.on('did-finish-load', () => {
    console.log('[Electron] Frontend cargado correctamente');
  });

  mainWindow.webContents.on(
    'did-fail-load',
    (_event, errorCode, errorDescription, validatedURL) => {
      console.error(
        `[Electron] Error al cargar frontend: ${errorCode} ${errorDescription} (${validatedURL})`
      );
      if (errorCode !== -3) {
        dialog.showErrorBox(
          'Error al cargar la aplicacion',
          `No se pudo cargar la interfaz.\n\n${errorDescription} (codigo ${errorCode})`
        );
      }
    }
  );

  mainWindow.webContents.on('render-process-gone', (_event, details) => {
    console.error('[Electron] El proceso de renderizado termino:', details.reason);
    dialog.showErrorBox(
      'Error del renderizador',
      `El proceso de renderizado termino inesperadamente.\n\nRazon: ${details.reason}`
    );
  });

  if (!app.isPackaged) {
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
      killProcess();
    }
  }, 5000);
}

function killProcess(): void {
  if (!serverProcess) return;
  if (process.platform === 'win32') {
    spawn('taskkill', ['/pid', String(serverProcess.pid), '/f', '/t']);
  } else {
    serverProcess.kill('SIGTERM');
  }
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

  console.log('[Electron] Iniciando Formatos Odontologicos...');
  createSplashWindow();

  const resourceError = verifyResources();
  if (resourceError) {
    showFatalError(resourceError);
    return;
  }

  startJavaBackend();

  const ready = await waitForServer();
  if (!ready) {
    showFatalError(bootError || 'Error desconocido al iniciar el servidor.');
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
    const ready = waitForServer();
    ready.then((ok) => {
      if (ok) createMainWindow();
      else showFatalError(bootError || 'Error al reconectar.');
    });
  }
});
