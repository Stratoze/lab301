import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api/v1': {
        target: 'http://localhost:8080',
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
      // suppress antd internal warnings about state updates not wrapped in act()
      if (msg.includes('inside a test was not wrapped in act(') ||
          msg.includes('An update to')) {
        return;
      }
      throw error;
    },
  },
})
