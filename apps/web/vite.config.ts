import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { defineConfig } from 'vite'

// https://vite.dev/config/
// VITE_PROXY_TARGET：worktree 并行自测时指向 8081，缺省回落主线 8080（阶段4 §三端口规避）
const proxyTarget = process.env.VITE_PROXY_TARGET ?? 'http://localhost:8080'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 3000,
    strictPort: true,
    proxy: {
      '/api': proxyTarget,
      '/uploads': proxyTarget,
    },
  },
})
