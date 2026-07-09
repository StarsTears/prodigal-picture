<template>
  <!-- FullScreenLayout.vue -->
  <div id="fullScreenLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-content class="content">
        <RouterView/>
      </a-layout-content>

      <a-layout-footer class="footer">
        Prodigal Picture | Copyright 2024-2026 All Rights Reserved.
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
#fullScreenLayout :deep(.ant-layout) {
  background: var(--bg-body);
}

#fullScreenLayout .content {
  background: var(--bg-content);
  min-height: calc(100vh - 52px);
}

#fullScreenLayout .footer {
  background: var(--bg-footer);
  padding: 16px;
  text-align: center;
}
</style>
