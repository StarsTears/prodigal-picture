<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader/>
      </a-layout-header>

      <a-layout>
        <GlobalSider v-if="showSider" class="sider" v-model:collapsed="siderCollapsed"/>
        <a-layout-content class="content" :style="{ marginLeft: showSider && !siderCollapsed ? '200px' : '0' }">
          <RouterView :siderWidth="showSider && !siderCollapsed ? 200 : 0"/>
        </a-layout-content>
      </a-layout>
      <a-layout-footer class="footer">
        Prodigal Picture | Copyright 2024-2026 All Rights Reserved.
      </a-layout-footer>
    </a-layout>
    <div v-if="showSider" class="sider-float-toggle" @click="siderCollapsed = !siderCollapsed" :title="siderCollapsed ? '展开菜单' : '收起菜单'">
      <MenuFoldOutlined v-if="!siderCollapsed" />
      <MenuUnfoldOutlined v-else />
    </div>
  </div>
</template>

<script lang="ts">
import GlobalHeader from "@/components/GlobalHeader.vue";
import GlobalSider from "@/components/GlobalSider.vue";
import {ref, computed} from "vue";
import {useLoginUserStore} from "@/stores/loginUserStore";
import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons-vue';
export default {
  name: "BasicLayout",
  components: {GlobalHeader, GlobalSider, MenuFoldOutlined, MenuUnfoldOutlined},
  setup() {
    const loginUserStore = useLoginUserStore();
    const showSider = computed(() => loginUserStore?.loginUser?.id !== undefined);
    const siderCollapsed = ref(false);
    return {
      siderCollapsed,
      showSider,
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
  z-index: 999;
  overflow-y: auto;
}

#basicLayout :deep(.ant-layout) {
  background: var(--bg-body);
}

#basicLayout :deep(.ant-menu-root) {
  border-bottom: none !important;
  border-inline-end: none !important;
  background: transparent !important;
}

#basicLayout .content {
  padding: 28px;
  background: var(--bg-content);
  margin-top: 64px;
  min-height: calc(100vh - 64px - 52px);
  transition: margin-left 0.2s ease;
}

#basicLayout .footer {
  background: var(--bg-footer);
  padding: 16px;
  text-align: center;
}

.sider-float-toggle {
  position: fixed;
  bottom: 24px;
  left: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  font-size: 18px;
  color: var(--text-secondary);
  background: var(--bg-card);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  z-index: 1001;
  transition: all 0.2s;
  border: 1px solid var(--border-color);
}

.sider-float-toggle:hover {
  color: #4096ff;
  background: rgba(64, 150, 255, 0.08);
  box-shadow: 0 4px 16px rgba(64, 150, 255, 0.2);
}
</style>
