<template>
  <a-modal class="email-add-modal"
           v-model:open="open"
           :title="title"
           :footer="false"
           @cancel="closeModal">
    <a-form layout="vertical" :model="emailForm" @finish="handleSubmit">
      <a-form-item label="主题" name="name">
        <a-input v-model:value="emailForm.subject" placeholder="输入邮件主题" allow-clear/>
      </a-form-item>
      <a-form-item label="收件人" name="to">
        <a-input v-model:value="emailForm.to" placeholder="收件人" allow-clear/>
      </a-form-item>
      <a-form-item label="内容" name="txt">
        <a-textarea v-model:value="emailForm.txt" placeholder="输入邮件内容" :auto-size="false" allow-clear/>
      </a-form-item>
      <a-form-item class="action-bar">
        <a-button type="primary" html-type="submit"> {{ "创建" }}</a-button>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import {reactive, ref} from 'vue';
import {addEmailUsingPost} from "@/api/emailController";
import {message} from "ant-design-vue";

interface Props {
  link: string;
  title: string;
  onReload?: () => void
}

//指定初始值
const props = withDefaults(defineProps<Props>(), {
  title: () => '新增邮件',
});
const emailForm = reactive<API.Email>({})
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

const handleSubmit = async () => {
  console.log(emailForm)
  const res =await addEmailUsingPost({...emailForm})
  if (res.code === 0 && res.data){
    message.success('创建成功')
    //让外层刷新
    props?.onReload()
    open.value = false;
  }else{
    message.error('创建失败-'+res.msg)
  }
}
</script>

<style>
.email-add-modal {
  text-align: center;
}
</style>
