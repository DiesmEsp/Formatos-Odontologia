import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const USE_ELECTRON = process.env.VITE_ELECTRON === 'true';

const plugins: import('vite').PluginOption[] = [react()];

if (USE_ELECTRON) {
  const electron = require('vite-plugin-electron').default as typeof import('vite-plugin-electron').default;
  const renderer = require('vite-plugin-electron-renderer').default as typeof import('vite-plugin-electron-renderer').default;

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
        onstart(options: { reload: () => void }) {
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
  base: './',
  build: {
    outDir: 'dist',
  },
});
