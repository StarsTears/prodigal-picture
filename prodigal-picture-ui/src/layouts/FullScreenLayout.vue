<template>
  <!-- FullScreenLayout.vue -->
  <div id="fullScreenLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-content class="content">
        <RouterView/>
      </a-layout-content>

      <a-layout-footer class="footer">
        <a href="https://github.com/StarsTears/prodigal-picture/" target="_blank">
          <GithubOutlined/>
        </a>
        Prodigal Picture | Copyright © prodigal 20001-2024  All Rights Reserved.
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script lang="ts">
import {GithubOutlined} from '@ant-design/icons-vue';
export default {
  name: "FullScreenLayout",
  components: {GithubOutlined}
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
