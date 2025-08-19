<template>
  <div class="loginBg">
    <div id="loginView">
      <h2 class="title">Prodigal Picture - 用户登录</h2>
      <div class="desc">企业级智能协同云图库</div>
      <a-tabs v-model:activeKey="loginType" centered>
        <a-tab-pane key="account" tab="账号登录">
          <a-form :model="formState" name="accountLogin" autocomplete="off" @finish="handleSubmitAccount">
            <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
              <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
            </a-form-item>
            <a-form-item
              name="userPassword"
              :rules="[
                { required: true, message: '请输入密码' },
                { min: 6, message: '密码不能小于 6 位' },
              ]"
            >
              <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
            </a-form-item>
            <div class="tips">
              没有账号？
              <RouterLink to="/register">去注册</RouterLink>
            </div>
            <a-form-item>
              <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
            </a-form-item>
            <div class="test-account-tip">测试账号：test、密码：123456</div>
          </a-form>
        </a-tab-pane>
        <a-tab-pane key="email" tab="邮箱登录">
          <a-form :model="emailFormState" name="emailLogin" autocomplete="off" @finish="handleSubmitEmail">
            <a-form-item name="email" :rules="[{ required: true, message: '请输入邮箱' }, { type: 'email', message: '邮箱格式不正确' }]">
              <a-input v-model:value="emailFormState.email" placeholder="请输入邮箱" />
            </a-form-item>
            <a-form-item name="captcha" :rules="[{ required: true, message: '请输入验证码' }]">
              <a-input-group compact>
                <a-input v-model:value="emailFormState.captcha" style="width: 60%" placeholder="请输入验证码" />
                <a-button class="captcha-btn" style="width: 38%" :disabled="countdown > 0 || !emailFormState.email || sendingCaptcha" @click="onSendCaptcha">
                  <span v-if="countdown === 0">获取验证码</span>
                  <span v-else>{{ countdown }} 秒后重试</span>
                </a-button>
              </a-input-group>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue';
import { useLoginUserStore } from "@/stores/loginUserStore";
import { message } from "ant-design-vue";
import { getLoginUserUsingGet, loginUsingPost } from "@/api/systemController";
import { sendVerificationCodeUsingPost } from '@/api/emailController';
import { useRoute, useRouter } from "vue-router";

const loginType = ref<'account' | 'email'>('account');

const formState = reactive<API.LoginDto>({
  userAccount: '',
  userPassword: ''
});

const emailFormState = reactive({
  email: '',
  captcha: ''
});

const countdown = ref(0);
const sendingCaptcha = ref(false);
let timer: any = null;

const route = useRoute();
const router = useRouter();
const loginUserStore = useLoginUserStore();

onMounted(async () => {
  // 如果已经登录，则直接跳转到首页
  const loginUser = await getLoginUserUsingGet();
  if (loginUser.code === 0 && loginUser.data) {
    router.push({
      path: '/',
      replace: true,
    });
  }
});

const startCountdown = () => {
  countdown.value = 60;
  timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
      timer = null;
    }
  }, 1000);
};

const onSendCaptcha = async () => {
  if (!emailFormState.email) {
    message.warning('请输入邮箱');
    return;
  }
  sendingCaptcha.value = true;
  try {
    const res = await sendVerificationCodeUsingPost(emailFormState.email);
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

const handleSubmitAccount = async (values: any) => {
  const params = {
    loginType: 'ACCOUNT',
    userAccount: values.userAccount,
    userPassword: values.userPassword
  };
  const res = await loginUsingPost(params);
  if (res.code === 0 && res.data) {
    await loginUserStore.fetchLoginUser();
    loginUserStore.setLoginUser(res.data);
    router.push({
      path: '/',
      replace: true,
    });
  } else {
    message.error('登录失败，' + res.msg);
  }
};

const handleSubmitEmail = async (values: any) => {
  const params = {
    loginType: 'EMAIL',
    email: values.email,
    captcha: values.captcha
  };
  const res = await loginUsingPost(params);
  if (res.code === 0 && res.data) {
    await loginUserStore.fetchLoginUser();
    loginUserStore.setLoginUser(res.data);
    router.push({
      path: '/',
      replace: true,
    });
  } else {
    message.error('登录失败，' + res.msg);
  }
};
</script>

<style scoped >
.loginBg {
  /*background-image: url("../assets/img.png");*/
  background-size: cover; /* 背景图片覆盖整个容器 */
 /* background-position: center;*/ /* 背景居中 */
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed; /* 使用fixed定位确保不产生滚动 */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0; /* 撑满整个视口 */
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  box-sizing: border-box;
  overflow: hidden; /* 确保不会出现滚动条 */
}

#loginView {
  max-width: 360px;
  margin: 0 auto;
  width: 100%;
  /*max-width: 420px;*/
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

.test-account-tip {
  color: #888;
  font-size: 13px;
  text-align: right;
  margin-bottom: 8px;
}
</style>
