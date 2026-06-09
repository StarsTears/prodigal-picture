<template>
  <div class="resetBg">
    <div id="resetView">
      <h2 class="title">Prodigal Picture - 重置密码</h2>
      <div class="desc">企业级智能协同云图库</div>
      <a-form ref="formRef" :model="formState" name="resetPassword" autocomplete="off" @finish="handleSubmit">
        <input type="text" style="position:absolute;left:-9999px" tabindex="-1" autocomplete="off" name="decoy"/>
        <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" autocomplete="nope" />
        </a-form-item>
        <a-form-item name="userEmail"
                     :rules="[{ required: true, message: '请输入邮箱' },
                              { type: 'email', validator: checkEmail, message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }]">
          <a-input v-model:value="formState.userEmail" placeholder="请输入邮箱" autocomplete="nope" />
        </a-form-item>
        <a-form-item name="captcha" :rules="[{ required: true, message: '请输入验证码' }]">
          <a-input-group compact>
            <a-input v-model:value="formState.captcha" style="width: 60%" placeholder="请输入验证码" autocomplete="nope" />
            <a-button class="captcha-btn" style="width: 38%" :disabled="countdown > 0 || !formState.userEmail || sendingCaptcha" @click="onSendCaptcha">
              <span v-if="countdown === 0">获取验证码</span>
              <span v-else>{{ countdown }} 秒后重试</span>
            </a-button>
          </a-input-group>
        </a-form-item>
        <a-form-item
          name="newPassword"
          :rules="[
            { required: true, message: '请输入新密码' },
            { min: 6, message: '密码不能小于 6 位' },
          ]"
        >
          <a-input-password v-model:value="formState.newPassword" placeholder="请输入新密码" autocomplete="new-password" @change="onNewPasswordChange" />
        </a-form-item>
        <a-form-item
          name="checkPassword"
          :rules="[
            { required: true, message: '请输入确认密码' },
            { min: 6, message: '确认密码不能小于 6 位' },
            { validator: validateCheckPassword, message: '两次输入的密码不一致', trigger: ['blur', 'change'] },
          ]"
        >
          <a-input-password v-model:value="formState.checkPassword" placeholder="请输入确认密码" autocomplete="new-password" />
        </a-form-item>
        <div class="tips">
          想起密码了？
          <RouterLink to="/login">去登录</RouterLink>
        </div>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">重置密码</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onUnmounted } from 'vue';
import { message } from "ant-design-vue";
import { resetPasswordUsingPost } from "@/api/systemController";
import { sendVerificationCodeUsingPost } from '@/api/emailController';
import { useRouter } from "vue-router";

const formState = reactive<API.ResetPasswordDTO>({
  userAccount: '',
  userEmail: '',
  captcha: '',
  newPassword: '',
  checkPassword: ''
});

const countdown = ref(0);
const sendingCaptcha = ref(false);
const formRef = ref();
let timer: ReturnType<typeof setInterval> | null = null;
const router = useRouter();

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
});

const checkEmail = (_rule: any, value: string, callback: (error?: Error) => void) => {
  const regEmail = /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/;
  if (value && !regEmail.test(value)) {
    callback(new Error('请输入有效的邮箱'));
  } else {
    callback();
  }
};

const validateCheckPassword = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const onNewPasswordChange = () => {
  if (formState.checkPassword) {
    formRef.value?.validateFields('checkPassword');
  }
};

const startCountdown = () => {
  countdown.value = 60;
  timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer!);
      timer = null;
    }
  }, 1000);
};

const onSendCaptcha = async () => {
  if (!formState.userEmail) {
    message.warning('请输入邮箱');
    return;
  }
  sendingCaptcha.value = true;
  try {
    const res = await sendVerificationCodeUsingPost({ email: formState.userEmail });
    if (res.code === 0) {
      message.success('验证码已发送，请查收邮箱');
      startCountdown();
    } else {
      message.error(res.msg || '验证码发送失败');
    }
  } catch (e) {
    message.error('验证码发送失败');
  } finally {
    sendingCaptcha.value = false;
  }
};

const handleSubmit = async (values: any) => {
  const res = await resetPasswordUsingPost(values);
  if (res.code === 0 && res.data) {
    message.success('密码重置成功，请重新登录');
    router.push({
      path: '/login',
      replace: true,
    });
  } else {
    message.error('重置失败，' + res.msg);
  }
};
</script>

<style scoped>
.resetBg {
  background-size: cover;
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  box-sizing: border-box;
  overflow: hidden;
}

#resetView {
  max-width: 360px;
  margin: 0 auto;
  width: 100%;
  background: white;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  padding: 40px;
  animation: fadeIn 0.5s ease;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}

.captcha-btn {
  background-color: #1677ff !important;
  color: #fff !important;
  border-color: #1677ff !important;
}
</style>
