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
  //     // 后端返回不包含bucket等相关信息，则使用代理
  //     // 仅代理图片文件请求到 COS，SPA 路由放行（如分享链接 /picture/0/xxx）
  //     '/picture': {
  //       target: 'https://XXXXXXX.myqcloud.com',
  //       changeOrigin: true,
  //       bypass(req) {
  //         const path = req.url || ''
  //         // 只有带图片扩展名的请求才转发到 COS，其余交给 Vite SPA 路由
  //         if (/\.(jpg|jpeg|png|gif|webp|svg|bmp|ico)$/i.test(path)) {
  //           return null // 代理到 COS
  //         }
  //         return path // 放行给 SPA
  //       },
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
