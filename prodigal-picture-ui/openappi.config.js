import {generateService} from "@umijs/openapi";

generateService({
  requestLibPath:"import request from '@/request.ts'",
  schemaPath: 'http://petstore.swagger.io/v2/swagger.json',
    serversPath: './src',
})
