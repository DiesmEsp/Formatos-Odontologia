export {};

declare global {
  interface Window {
    api?: {
      shell: { openPath: (filePath: string) => Promise<{ success: boolean; error?: string }> };
    };
  }
}
