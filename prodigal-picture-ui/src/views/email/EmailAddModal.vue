<template>
  <a-modal class="email-form-modal"
           v-model:open="open"
           :title="isEdit ? '编辑邮件' : '新增邮件'"
           :footer="false"
           @cancel="closeModal">
    <a-form layout="vertical" :model="emailForm" @finish="handleSubmit">
      <a-form-item label="主题" name="subject">
        <a-input v-model:value="emailForm.subject" placeholder="输入邮件主题" allow-clear/>
      </a-form-item>
      <a-form-item label="邮件类型" name="type" :rules="[{ required: true, message: '请选择邮件类型' }]">
        <a-select v-model:value="emailForm.type"
                  :options="EMAIL_TYPE_OPTIONS"
                  placeholder="选择邮件类型"
                  style="min-width: 100%;text-align: left"
                  allow-clear/>
      </a-form-item>
      <a-form-item label="收件人" name="to">
        <a-select v-model:value="emailForm.to"
                  mode="multiple"
                  :options="userOptions"
                  placeholder="选择收件人（留空则发送给全部用户）"
                  :filter-option="filterOption"
                  style="min-width: 100%;text-align: left"
                  allow-clear/>
      </a-form-item>
      <a-form-item label="内容" name="txt">
        <a-textarea v-model:value="emailForm.txt" placeholder="输入邮件内容" :rows="6" allow-clear/>
      </a-form-item>
      <a-form-item class="action-bar">
        <a-button type="primary" html-type="submit">{{ isEdit ? '保存' : '创建' }}</a-button>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import {reactive, ref} from 'vue';
import {addEmailUsingPost, updateEmailUsingPost} from "@/api/emailController";
import {listUserVoByPageUsingPost} from "@/api/userController";
import {message} from "ant-design-vue";
import {EMAIL_TYPE_OPTIONS} from '@/constants/email'

interface Props {
  onReload?: () => void
}

const props = withDefaults(defineProps<Props>(), {});

interface EmailFormState {
  subject?: string
  type?: number
  to?: string[]
  txt?: string
}

const emailForm = reactive<EmailFormState>({})
const open = ref<boolean>(false);
const isEdit = ref(false);
let editingId: string | undefined;
const userOptions = ref<{ label: string; value: string }[]>([]);

const fetchUsers = async () => {
  try {
    const res = await listUserVoByPageUsingPost({ current: 1, pageSize: 9999 });
    if (res.code === 0 && res.data?.records) {
      userOptions.value = res.data.records
        .filter(u => u.userEmail)
        .map(u => ({
          label: `${u.userName || u.userAccount} (${u.userEmail})`,
          value: u.userEmail!,
        }));
    }
  } catch {
    // ignore
  }
};

const filterOption = (input: string, option: any) =>
  option.label.toLowerCase().includes(input.toLowerCase());

const resetForm = () => {
  emailForm.subject = undefined;
  emailForm.type = undefined;
  emailForm.to = undefined;
  emailForm.txt = undefined;
};

const openModal = (editData?: API.EmailVO) => {
  resetForm();
  fetchUsers();
  if (editData) {
    isEdit.value = true;
    editingId = editData.id;
    emailForm.subject = editData.subject;
    emailForm.type = editData.type;
    emailForm.txt = editData.txt;
    emailForm.to = editData.to ? editData.to.split(',') : [];
  } else {
    isEdit.value = false;
    editingId = undefined;
  }
  open.value = true;
};

defineExpose({ openModal });

const closeModal = () => {
  open.value = false;
};

const handleSubmit = async () => {
  const toStr = Array.isArray(emailForm.to) ? emailForm.to.join(',') : (emailForm.to || '');

  if (isEdit.value && editingId) {
    const res = await updateEmailUsingPost({
      id: editingId,
      type: emailForm.type,
      to: toStr,
      subject: emailForm.subject,
      txt: emailForm.txt,
    });
    if (res.code === 0 && res.data) {
      message.success('修改成功');
      props?.onReload?.();
      open.value = false;
    } else {
      message.error('修改失败-' + res.msg);
    }
  } else {
    const res = await addEmailUsingPost({
      type: emailForm.type,
      to: toStr,
      subject: emailForm.subject,
      txt: emailForm.txt,
    });
    if (res.code === 0 && res.data) {
      message.success('创建成功');
      props?.onReload?.();
      open.value = false;
    } else {
      message.error('创建失败-' + res.msg);
    }
  }
}
</script>

<style>
.email-form-modal {
  text-align: center;
}
</style>
