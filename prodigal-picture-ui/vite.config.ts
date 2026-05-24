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
  //     },
  //     //后端返回不包含buket等相关信息，则是用代理
  //     '/picture': {
  //       target: 'https://XXXXXX.myqcloud.com',
  //       changeOrigin: true,
  //       rewrite: (path) => path,
  //     },
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
