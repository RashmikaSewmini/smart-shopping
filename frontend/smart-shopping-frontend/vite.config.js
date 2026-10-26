import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api/products': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/shopping-sessions': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/checkouts': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
    },
  },
})