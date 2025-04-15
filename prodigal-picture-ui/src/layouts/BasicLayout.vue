<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header" >
        <GlobalHeader/>
      </a-layout-header>
      <!--  水印    -->
      <!--      <a-watermark content="Prodigal Picture">-->
      <!--        <div style="height: 500px"/>-->
      <!--      </a-watermark>-->
      <a-layout>
        <GlobalSider v-if="showSider" class="sider"/>
        <a-layout-content class="content" :style="{ marginLeft: showSider ? siderWidth + 'px' : '0' }">
          <RouterView :siderWidth="siderWidth"/>
        </a-layout-content>
      </a-layout>
      <a-layout-footer class="footer">
        <a href="https://github.com/StarsTears/prodigal-picture/" target="_blank">
          <GithubOutlined/>
        </a>
        Prodigal Picture | Copyright 20001-2024  All Rights Reserved.
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script lang="ts">
import GlobalHeader from "@/components/GlobalHeader.vue";
import GlobalSider from "@/components/GlobalSider.vue";
import {GithubOutlined} from '@ant-design/icons-vue';
import {ref, watchEffect,computed} from "vue";
import {useLoginUserStore} from "@/stores/loginUserStore";
export default {
  name: "BasicLayout",
  components: {GlobalHeader,GlobalSider, GithubOutlined},
  setup() {
    const siderWidth = ref(200);
    const loginUserStore = useLoginUserStore();
    const showSider = computed(() => loginUserStore?.loginUser?.id !== undefined);
    // 监听登录状态变化，更新侧边栏宽度
    watchEffect(() => {
      if (loginUserStore?.loginUser?.id != undefined && loginUserStore?.loginUser?.id) {
        siderWidth.value = 200; // 用户登录后，设置左边距为200px
      } else {
        siderWidth.value = 0; // 用户未登录，设置左边距为0
      }
    });
    return {
      siderWidth,
      showSider
    }
  }
}
</script>

<style scoped>
#basicLayout .header {
  padding-inline: 20px;
  background: white;
  color: unset;
  margin-bottom: 1px;
  position: fixed;
  z-index: 1000; /* 确保header在最上层 */
}

#basicLayout .sider {
  background: #ffffff;
  padding-top: 20px;
  border-right: 0.5px solid #eee;
  position: fixed;
  top: 64px;
  height: 100vh;
  z-index: 1000; /* 确保sider在最上层 */
}

#basicLayout :deep(.ant-menu-root){
  border-bottom: none !important;
  border-inline-end: none !important;
}

#basicLayout .content {
  padding: 28px;
  background: linear-gradient(to right, #efefef, #ffffff);
  margin-bottom: 30px;
  margin-top: 40px;
  --sider-width: 200px; /* 默认未登录时的左边距 */
  margin-left: var(--sider-width);
  transition: margin-left 0.3s; /* 平滑过渡动画 */
}

#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0;
  right: 0;
  left: 0;
  text-align: center;
  z-index: 1000; /* 确保footer在最上层 */
}
</style>
