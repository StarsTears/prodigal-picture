<template>
<div id="basicLayout">
  <a-layout style="min-height: 100vh">
    <a-layout-header class="header">
      <GlobalHeader/>
    </a-layout-header>
    <a-layout-content class="content">
      <RouterView/>
    </a-layout-content>
    <a-layout-footer class="footer">
      <a href="https://gitee.com/" target="_blank">
        Prodigal Picture @ By all Lang
      </a>

    </a-layout-footer>
  </a-layout>
</div>
</template>

<script>
import GlobalHeader from "@/components/GlobalHeader.vue";
export default {
  name: "BasicLayout",
  components: {GlobalHeader}
}
</script>

<style scoped>
#basicLayout .header{
  padding-inline: 20px;
  background: white;
  color: unset;
  margin-bottom: 16px;
}
#basicLayout .content{
  padding: 20px;
  background: linear-gradient(to right, #efefef, #ffffff);
  margin-bottom: 30px;
}

#basicLayout .footer{
  background: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0;
  right: 0;
  left: 0;
  text-align: center;
}
</style>
