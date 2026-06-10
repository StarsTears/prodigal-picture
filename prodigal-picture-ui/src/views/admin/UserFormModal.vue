<template>
  <a-modal
    v-model:open="open"
    :title="isEdit ? '编辑用户' : '新增用户'"
    :footer="false"
    @cancel="closeModal">
    <a-form layout="vertical" :model="formState" @finish="handleSubmit">
      <a-form-item label="账号" name="userAccount"
                   :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" allow-clear/>
      </a-form-item>
      <a-form-item label="用户名" name="userName">
        <a-input v-model:value="formState.userName" placeholder="请输入用户名" allow-clear/>
      </a-form-item>
      <a-form-item label="邮箱" name="userEmail"
                   :rules="[{ required: true, message: '请输入邮箱' }, { type: 'email', message: '邮箱格式不正确' }]">
        <a-input v-model:value="formState.userEmail" placeholder="请输入邮箱" allow-clear/>
      </a-form-item>
      <a-form-item v-if="!isEdit" label="密码">
        <a-input value="123456" disabled />
        <div style="color: #999; font-size: 12px; margin-top: 4px">默认密码为 123456，用户登录后可自行修改</div>
      </a-form-item>
      <a-form-item label="头像URL" name="userAvatar">
        <a-input v-model:value="formState.userAvatar" placeholder="请输入头像URL" allow-clear/>
      </a-form-item>
      <a-form-item label="简介" name="userProfile">
        <a-textarea v-model:value="formState.userProfile" placeholder="请输入简介" :rows="3" allow-clear/>
      </a-form-item>
      <a-form-item label="角色" name="userRole">
        <a-select v-model:value="formState.userRole"
                  :options="USER_ROLE_OPTIONS"
                  placeholder="请选择角色"
                  style="width: 100%"
                  :disabled="isTargetSuperAdmin"
                  allow-clear/>
      </a-form-item>
      <a-form-item class="action-bar">
        <a-button type="primary" html-type="submit">{{ isEdit ? '保存' : '创建' }}</a-button>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { addUserUsingPost, updateUserUsingPost } from '@/api/userController';
import { message } from 'ant-design-vue';
import { USER_ROLE_OPTIONS } from '@/constants/user';

interface Props {
  onReload?: () => void
}

const props = withDefaults(defineProps<Props>(), {});

interface UserFormState {
  userAccount?: string
  userName?: string
  userEmail?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
}

const formState = reactive<UserFormState>({});
const open = ref(false);
const isEdit = ref(false);
const isTargetSuperAdmin = ref(false);
let editingId: number | undefined;

const resetForm = () => {
  formState.userAccount = undefined;
  formState.userName = undefined;
  formState.userEmail = undefined;
  formState.userAvatar = undefined;
  formState.userProfile = undefined;
  formState.userRole = undefined;
  isTargetSuperAdmin.value = false;
};

const openModal = (editData?: API.UserVO) => {
  resetForm();
  if (editData) {
    isEdit.value = true;
    editingId = editData.id;
    isTargetSuperAdmin.value = editData.userRole === 'administrator';
    formState.userAccount = editData.userAccount;
    formState.userName = editData.userName;
    formState.userEmail = editData.userEmail;
    formState.userAvatar = editData.userAvatar;
    formState.userProfile = editData.userProfile;
    formState.userRole = editData.userRole;
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
  if (isEdit.value && editingId) {
    const res = await updateUserUsingPost({
      id: editingId,
      userAccount: formState.userAccount,
      userName: formState.userName,
      userEmail: formState.userEmail,
      userAvatar: formState.userAvatar,
      userProfile: formState.userProfile,
      userRole: formState.userRole,
    });
    if (res.code === 0 && res.data) {
      message.success('修改成功');
      props?.onReload?.();
      open.value = false;
    } else {
      message.error('修改失败，' + res.msg);
    }
  } else {
    const res = await addUserUsingPost({
      userAccount: formState.userAccount,
      userName: formState.userName,
      userEmail: formState.userEmail,
      userAvatar: formState.userAvatar,
      userProfile: formState.userProfile,
      userRole: formState.userRole,
    });
    if (res.code === 0 && res.data) {
      message.success('创建成功');
      props?.onReload?.();
      open.value = false;
    } else {
      message.error('创建失败，' + res.msg);
    }
  }
}
</script>

<style scoped>
.action-bar {
  text-align: center;
}
</style>
