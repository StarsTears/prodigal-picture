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
        Prodigal Picture | Copyright 20001-2026  All Rights Reserved.
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script lang="ts">
import GlobalHeader from "@/components/GlobalHeader.vue";
import GlobalSider from "@/components/GlobalSider.vue";
import {ref, watchEffect,computed} from "vue";
import {useLoginUserStore} from "@/stores/loginUserStore";
export default {
  name: "BasicLayout",
  components: {GlobalHeader,GlobalSider},
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
  background: var(--bg-header);
  color: unset;
  margin-bottom: 1px;
  position: fixed;
  z-index: 1000;
}

#basicLayout .sider {
  background: var(--bg-sider);
  padding-top: 20px;
  border-right: 0.5px solid var(--border-color);
  position: fixed;
  top: 64px;
  height: 100vh;
  z-index: 1000;
}

#basicLayout :deep(.ant-layout) {
  background: var(--bg-body);
}

#basicLayout :deep(.ant-menu-root){
  border-bottom: none !important;
  border-inline-end: none !important;
  background: transparent !important;
}

#basicLayout .content {
  padding: 28px;
  background: var(--bg-content);
  margin-bottom: 30px;
  margin-top: 40px;
  --sider-width: 200px;
  margin-left: var(--sider-width);
  transition: margin-left 0.3s;
}

#basicLayout .footer {
  background: var(--bg-footer);
  padding: 16px;
  position: fixed;
  bottom: 0;
  right: 0;
  left: 0;
  text-align: center;
  z-index: 1000;
}
</style>
