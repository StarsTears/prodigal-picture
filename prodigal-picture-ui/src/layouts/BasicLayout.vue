<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader/>
      </a-layout-header>
      <!--  水印    -->
<!--      <a-watermark content="Prodigal Picture">-->
<!--        <div style="height: 500px"/>-->
<!--      </a-watermark>-->
        <a-layout-content class="content">
          <RouterView/>
        </a-layout-content>

      <a-layout-footer class="footer">
        <a href="https://github.com/StarsTears/prodigal-picture/" target="_blank">
          <GithubOutlined/>
        </a>
        Prodigal Picture | Copyright © 20001-2024 prodigal All Rights Reserved.
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script>
import GlobalHeader from "@/components/GlobalHeader.vue";
import {GithubOutlined} from '@ant-design/icons-vue';

export default {
  name: "BasicLayout",
  components: {GlobalHeader, GithubOutlined}
}


</script>

<style scoped>
#basicLayout .header {
  padding-inline: 20px;
  background: white;
  color: unset;
  margin-bottom: 16px;
  /*z-index: 1000; !* 确保footer在最上层 *!*/
}

#basicLayout .content {
  padding: 20px;
  background: linear-gradient(to right, #efefef, #ffffff);
  margin-bottom: 30px;
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
