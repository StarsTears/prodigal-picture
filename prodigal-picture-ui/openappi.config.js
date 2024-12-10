import {generateService} from "@umijs/openapi";

generateService({
  requestLibPath:"import request from '@/request.ts'",
  schemaPath: 'http://127.0.0.1:9999/api/v2/api-docs',
    serversPath: './src',
})
