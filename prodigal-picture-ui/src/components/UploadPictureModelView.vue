<template>
  <!-- 图片上传对话框-->
  <div>
    <a-modal v-model:open="open"
             title=""
             :confirm-loading="confirmLoading"
             @ok="handleOk"
             footer=""
             style="width: 720px;"
    >
      <AddPictureView :spaceId="id"/>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import AddPictureView from "@/views/picture/AddPictureView.vue";
import {ref} from "vue";
const confirmLoading = ref<boolean>(false);
interface Props{
  loading ?: boolean
  open ?: boolean
  spaceId ?: number
}
const props = withDefaults(defineProps<Props>(), {
  loading: () => false,
  open: false,
  spaceId: () => 0
})

const open = ref<boolean>(props.open)
const handleOk = () => {
  confirmLoading.value = true;
  setTimeout(() => {
    open.value = false;
    confirmLoading.value = false;
  }, 2000);
};
</script>

<style scoped>

</style>
