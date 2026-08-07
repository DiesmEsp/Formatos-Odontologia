import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const USE_ELECTRON = process.env.VITE_ELECTRON === 'true';

const plugins: any[] = [react()];

if (USE_ELECTRON) {
  const electron = require('vite-plugin-electron').default;
  const renderer = require('vite-plugin-electron-renderer').default;

  plugins.push(
    electron([
      {
        entry: 'electron/main.ts',
        vite: {
          build: {
            outDir: 'dist-electron',
            rollupOptions: {
              external: ['electron'],
            },
          },
        },
      },
      {
        entry: 'electron/preload.ts',
        onstart(options: any) {
          options.reload();
        },
        vite: {
          build: {
            outDir: 'dist-electron',
            rollupOptions: {
              external: ['electron'],
            },
          },
        },
      },
    ]),
    renderer(),
  );
}

export default defineConfig({
  plugins,
  build: {
    outDir: 'dist',
  },
});
