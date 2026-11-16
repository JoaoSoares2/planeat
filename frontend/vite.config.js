import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath } from 'node:url';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    rollupOptions: {
      input: {
        index: fileURLToPath(new URL('./index.html', import.meta.url)),
        servicos: fileURLToPath(new URL('./serviços.html', import.meta.url)),
        planejamentos: fileURLToPath(new URL('./planejamentos.html', import.meta.url)),
      },
    },
  },
});
