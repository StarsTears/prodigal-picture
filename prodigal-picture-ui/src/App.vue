<template>
  <a-config-provider :theme="themeConfig">
    <router-view v-if="BasicLayout" :component="BasicLayout" />
  </a-config-provider>
</template>
<script setup lang="ts">
import { onBeforeRouteLeave, onBeforeRouteUpdate, RouterLink, RouterView } from 'vue-router'
import { useRoute } from 'vue-router';
import { onMounted, computed } from "vue";
import BasicLayout from '@/layouts/BasicLayout.vue';
import FullScreenLayout from '@/layouts/FullScreenLayout.vue';
import { useThemeStore } from '@/stores/themeStore';

const themeStore = useThemeStore();
const themeConfig = computed(() => ({ algorithm: themeStore.algorithm }));

onMounted(() => {
  themeStore.init();
});

const route = useRoute();
const layoutComponent = computed(() => {
  return route.path.startsWith('/login') || route.path.startsWith('/register')
    ? FullScreenLayout
    : BasicLayout;
});

// 确保在路由离开和进入时更新布局组件
onBeforeRouteLeave(() => {
  layoutComponent.value;
});

onBeforeRouteUpdate((to, from, next) => {
  next(() => {
    layoutComponent.value;
  });
});
</script>

<style scoped>

</style>
