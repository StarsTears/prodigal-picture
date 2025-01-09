import { createApp } from 'vue'
import { createPinia } from 'pinia'

import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';

import Vue3ColorPicker from "vue3-colorpicker";
import "vue3-colorpicker/style.css";

import VueCropper from 'vue-cropper';
import 'vue-cropper/dist/index.css'

import App from './App.vue'
import router from './router'
import '@/access/access'
const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)
app.use(Vue3ColorPicker)
app.use(VueCropper)
app.mount('#app')


