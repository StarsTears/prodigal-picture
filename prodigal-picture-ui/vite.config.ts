import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  //本地测试开启这个！服务器上使用nginx配置反向代理
  // server:{
  //   proxy:{
  //     '/api': {
  //       target: 'http://localhost:9999',
  //       changeOrigin: true,
  //       // rewrite: (path) => path.replace(/^\/api/, '')
  //     }
  //   }
  // },
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
