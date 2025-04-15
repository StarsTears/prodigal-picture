<template>
  <a-modal v-model:open="open" :title="title" :footer="false" @cancel="closeModal">
    <div>
      <div>
        <h4>{{name}}</h4>
        <a-typography-link copyable :content="link">
          {{ link }}
        </a-typography-link>
      </div>
      <div style="margin-top: 16px" />
      <div>
        <h4>手机扫描查看</h4>
        <a-qrcode
          :value="link"
          :size="160" color="#1890ff"
          icon="https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/1866682358702141442/2024-12-12_ABF8SLHB.JPG"
        />
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  link: string
  title: string
  name: string
}

//指定初始值
const props = withDefaults(defineProps<Props>(), {
  link: () => 'https://www.baidu.com',
  title: () => '分享图片',
  name: () => '皮卡~',
})

const open = ref<boolean>(false)
//该函数需传递给父组件，用于打开弹窗
const openModal = (e: MouseEvent) => {
  open.value = true
}
//将 onModal 函数暴露给父组件
defineExpose({
  openModal,
})
const closeModal = (e: MouseEvent) => {
  open.value = false
}
</script>

<style scoped></style>
