export {};

declare global {
  interface Window {
    api: import('../electron/preload').ElectronAPI;
  }
}
