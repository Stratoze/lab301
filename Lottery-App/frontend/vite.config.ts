/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    proxy: {
      '/api/v1': {
        target: process.env.VITE_DEV_PROXY_TARGET || 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test-setup.ts',
    onUnhandledError: (error: unknown) => {
      const msg = error instanceof Error ? error.message : String(error);
      if (msg.includes('inside a test was not wrapped in act(') ||
          msg.includes('An update to')) {
        return;
      }
      throw error;
    },
  },
})