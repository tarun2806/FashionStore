import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Vite dev server runs on http://localhost:5173
// Backend (Tomcat) runs on http://localhost:8080/FashionStore
// All /api requests are transparently proxied so the JSESSIONID cookie is shared.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: false,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        cookieDomainRewrite: 'localhost',
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // Log proxy requests for debugging
            console.log(`[Proxy] ${req.method} ${req.url} -> ${options.target}${req.url}`);
          });
        },
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
    target: 'es2020',
    rollupOptions: {
      output: {
        manualChunks: {
          react: ['react', 'react-dom', 'react-router-dom'],
          charts: ['recharts'],
        },
      },
    },
  },
});
