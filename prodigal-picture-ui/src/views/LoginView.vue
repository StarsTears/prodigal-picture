<template>
  <div class="loginBg">
    <div id="loginView">
      <h2 class="title">Prodigal Picture - 用户登录</h2>
      <div class="desc">企业级智能协同云图库</div>
      <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
        <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号"/>
        </a-form-item>
        <a-form-item
          name="userPassword"
          :rules="[
          { required: true, message: '请输入密码' },
          { min: 6, message: '密码不能小于 6 位' },
        ]"
        >
          <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码"/>
        </a-form-item>
        <div class="tips">
          没有账号？
          <RouterLink to="/register">去注册</RouterLink>
        </div>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import {reactive,onMounted} from 'vue';
import {useLoginUserStore} from "@/stores/loginUserStore";
import {message} from "ant-design-vue";
import {getLoginUserUsingGet, loginUsingPost} from "@/api/systemController";
import {useRoute, useRouter} from "vue-router";

const formState = reactive<API.LoginDto>({
  userAccount: '',
  userPassword: ''
});

const router = useRouter()
const loginUserStore = useLoginUserStore()
/**
 * 提交表单
 * @param values
 */
// const route = useRoute();
const handleSubmit = async (values: any) => {
  const res = await loginUsingPost(values)
  // 登录成功，把登录态保存到全局状态中
  if (res.code === 0 && res.data) {
    await loginUserStore.fetchLoginUser()
    // loginUserStore.setLoginUser(res.data)
    message.success('登录成功')
    // const redirectPath = route.query.redirect || '/'; // 如果没有传递 redirect，则跳转首页
    // console.log("LoginView-redirectPath", route.query.redirect)
    // router.push(redirectPath as string);
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.msg)
  }
}
</script>

<style scoped >
.loginBg {
  background-image: url("../assets/img.png");
  height: 84vh;
  background-size: cover; /* 背景图片覆盖整个容器 */
  background-position: center; /* 背景居中 */
  display: flex;
  justify-content: center;
  align-items: center;
}

#loginView {
  max-width: 360px;
  margin: 0 auto;
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

</style>
