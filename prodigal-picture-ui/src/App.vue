<template>
  <router-view v-if="BasicLayout" :component="BasicLayout" />
</template>
<script setup lang="ts">
import {onBeforeRouteLeave, onBeforeRouteUpdate, RouterLink, RouterView} from 'vue-router'
import { useRoute } from 'vue-router';
import BasicLayout from '@/layouts/BasicLayout.vue';
import FullScreenLayout from '@/layouts/FullScreenLayout.vue';
import {computed} from "vue";

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
