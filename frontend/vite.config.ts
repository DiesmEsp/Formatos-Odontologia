import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const USE_ELECTRON = process.env.VITE_ELECTRON === 'true';

function removeCrossoriginPlugin(): import('vite').Plugin {
  return {
    name: 'remove-crossorigin',
    transformIndexHtml(html) {
      return html.replace(/\s+crossorigin/g, '');
    },
  };
}

const plugins: import('vite').PluginOption[] = [react(), removeCrossoriginPlugin()];

if (USE_ELECTRON) {
  const electron = (await import('vite-plugin-electron')).default;
  const renderer = (await import('vite-plugin-electron-renderer')).default;

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
