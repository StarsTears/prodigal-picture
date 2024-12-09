<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="title-bar">
            <img src="../assets/logo.jpg" alt="logo" class="logo">
            <div class="title">Prodigal Picture</div>
          </div>
        </RouterLink>
      </a-col>
      <a-col flex="auto">
        <a-menu v-model:selectedKeys="current" mode="horizontal" :items="items" @click="doMenuClick"/>
      </a-col>
      <a-col flex="100px">
        <div class="login-status">
          <div v-if="loginUserStore.loginUser.id">
            {{loginUserStore.loginUser.userName ?? "无名"}}
          </div>
          <div v-else>
            <a-button type="primary" herf="/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { h, ref } from 'vue';
import {HomeOutlined, MailOutlined, AppstoreOutlined, SettingOutlined } from '@ant-design/icons-vue';
import { MenuProps } from 'ant-design-vue';
import {useRouter} from "vue-router";
import {useLoginUserStore} from "@/stores/loginUserStore";

const loginUserStore = useLoginUserStore();

const items = ref<MenuProps['items']>([
  {
    key:"/",
    icon: h(HomeOutlined),
    label: h('a', { href: '/' }, '首页'),
    title: '首页',
  },{
    key: '/about',
    label: '关于',
    title: '关于',
  }, {
    key: '/admin',
    label: h('a', { href: 'https://gitee.com/StarsTeas/ruoyi-cloud' }, 'ruoyi-cloud'),
    title: 'ruoyi-clou',
  },{
    key: '/login',
    label: h('a', { href: '/login' }, '登录'),
    title: '登录',
  }
]);

const router = useRouter();
//刷新页面时，菜单高亮
const current = ref<string[]>([]);
router.afterEach((to, from, next)=>{
  current.value=[to.path];
})
//路由跳转事件
const doMenuClick=({key})=>{
  router.push({path:key})
}

</script>

<style scoped>
#globalHeader .title-bar{
  display: flex;
  align-items: center;
}
#globalHeader .logo{
  height: 48px;
}
#globalHeader .title{
  color: black;
  font-size: 18px;
  font-weight: bold;
  margin-left: 10px;
}
</style>
