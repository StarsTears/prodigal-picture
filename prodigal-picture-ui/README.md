# prodigal-picture-ui

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.


## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).
### node-api
*使用 umijs/openapi 同步后端接口*
先找到 openapi.config.js 文件；修改配置
```js
generateService({
  requestLibPath:"import request from '@/request.ts'",
  schemaPath: 'http://127.0.0.1:9999/api/v2/api-docs?group=local', //此处地址为你 后端接口文档地址 + 分组；若是默认分组，则schemaPath: 'http://127.0.0.1:9999/api/v2/api-docs'
  serversPath: './src',
})

```
若你后端Controller 添加了新接口，但前端没有同步到；可在package.json 里找到 node-api;
自动同步接口文档。执行成功后会在src目录下生成api文件夹

## Project Setup
*注意 node 版本需大于 18;该前端使用的是Vue 3.12.2*
```bash
# 会有各种诡异的 bug。可以通过如下操作解决 npm 下载速度慢的问题
npm install --registry=https://registry.npmmirror.com

#启动服务
npm run dev
```

浏览器访问 http://localhost:5173

