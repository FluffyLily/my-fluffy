import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': '/src'
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',  // Spring Boot 서버 주소
        changeOrigin: true,              // Origin을 맞추는 옵션
        secure: false,                   // HTTPS를 사용할 경우 true
      },
      '/uploads': {
        target: 'http://localhost:8081',  // 이미지 요청도 Spring Boot로 프록시
        changeOrigin: true,
        secure: false,
      },
    },
  },
})