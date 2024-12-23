<template>
  <div id="globalSider">
    <a-layout-sider v-if="loginUserStore.loginUser.id"
                    width="200"
                    style="background: #fff"
                    breakpoint="lg"
                    collapsed-width="0"
    >
      <a-menu
        v-model:selectedKeys="current"
        mode="inline"
        :items="menuItems"
        @click="handleMenuClick"
      />
    </a-layout-sider>
  </div>
</template>

<script setup lang="ts">
import {h, ref} from "vue";
import {PictureOutlined,UserOutlined} from '@ant-design/icons-vue';
import {useRouter} from "vue-router";
import {useLoginUserStore} from "@/stores/loginUserStore";
const loginUserStore = useLoginUserStore()
const menuItems = [
  {
    key:'/',
    label:'公共图库',
    icon: ()=>h(PictureOutlined),
  },{
    key:'/space/my_space',
    label: '我的空间',
    icon: ()=>h(UserOutlined),
  }
]

const router = useRouter()
//当前选中的菜单
const current = ref<string[]>([])
//监听路由变化、更新当前选中的菜单
router.afterEach((to,from,failure)=>{
  current.value = [to.path]
})
//路由跳转事件
const handleMenuClick = ({key}) => {
  router.push(key)
}
</script>

<style>
  #globalSider .ant-layout-sider{
    background: none;
  }
</style>
