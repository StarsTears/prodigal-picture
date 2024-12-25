<template>
  <a-modal v-model:open="open"
           :title="title"
           :footer="false"
           @cancel="closeModal">
    <h4>复制分享链接</h4>
    <a-typography-link copyable>
      {{link}}
    </a-typography-link>
    <div style="margin-top: 16px;"/>
    <h4>手机扫描查看</h4>
    <a-qrcode :value="link"
              icon="https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/1866682358702141442/2024-12-12_ABF8SLHB.JPG"/>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';

interface Props {
  link: string;
  title: string;
}
//指定初始值
const props = withDefaults(defineProps<Props>(), {
  link: ()=>'https://www.baidu.com',
  title: ()=>'分享图片',
});

const open = ref<boolean>(false);
//该函数需传递给父组件，用于打开弹窗
const openModal = (e: MouseEvent) => {
  open.value = true;
};
//将 onModal 函数暴露给父组件
defineExpose({
  openModal,
})
const closeModal = (e: MouseEvent) => {
  open.value = false;
};
</script>

<style scoped>

</style>
