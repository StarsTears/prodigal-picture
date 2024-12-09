import axios from "axios";
import {message} from "ant-design-vue";

//创建axios 实例
const instance = axios.create({
  baseURL: 'https://localhost:9999/api/',
  timeout: 60000,
  headers: {'X-Custom-Header': 'foobar'},
  withCredentials: true,
});

// 添加请求拦截器
axios.interceptors.request.use(function (config) {
  console.log(config.path)
  // 在发送请求之前做些什么
  return config;
}, function (error) {
  // 对请求错误做些什么
  return Promise.reject(error);
});

// 添加响应拦截器
axios.interceptors.response.use(function (response) {
  const { data } = response
  //未登录
  if (data.code === 401){
    if (!response.request.responseURL.includes('/sys/get/login')&&!window.location.pathname.includes('/sys/login')){
      message.warning('请先登录')
      window.location.href = `/sys/login?redicect=${window.location.href}`
    }
  }
  if (data.code === 500){
    message.error(data.msg)
  }
  // 2xx 范围内的状态码都会触发该函数。
  // 对响应数据做点什么
  return response;
}, function (error) {
  // 超出 2xx 范围的状态码都会触发该函数。
  // 对响应错误做点什么
  return Promise.reject(error);
});

export default instance;
