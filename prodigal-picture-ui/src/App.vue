<template>
  <BasicLayout />
</template>
<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import BasicLayout from "@/layouts/BasicLayout.vue";
import {useLoginUserStore} from "@/stores/loginUserStore";

const loginUserStore = useLoginUserStore();
loginUserStore.fetchLoginUser();
</script>

<style scoped>

</style>
