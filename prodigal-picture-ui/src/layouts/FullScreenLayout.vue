<template>
  <!-- FullScreenLayout.vue -->
  <div id="fullScreenLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-content class="content">
        <RouterView/>
      </a-layout-content>

      <a-layout-footer class="footer">
        Prodigal Picture | Copyright © prodigal 20001-2026  All Rights Reserved.
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script lang="ts">
export default {
  name: "FullScreenLayout",
}
</script>

<style scoped>
#fullScreenLayout .content {
  background: linear-gradient(to right, #efefef, #ffffff);
}
#fullScreenLayout .footer {
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
