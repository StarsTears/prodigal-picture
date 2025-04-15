<template>
  <div class="registerBg">
    <div id="registerView">
      <h2 class="title">Prodigal Picture - 用户注册</h2>
      <div class="desc">企业级智能协同云图库</div>
      <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
        <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号"/>
        </a-form-item>
        <a-form-item name="userEmail"
                     :rules="[{ required: true, message: '请输入邮箱' },
                              { type: 'email',validator: checkEmail, message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }]">
          <a-input v-model:value="formState.userEmail" placeholder="请输入邮箱"/>
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
        <a-form-item
          name="checkPassword"
          :rules="[
          { required: true, message: '请输入确认密码' },
          { min: 6, message: '确认密码不能小于 6 位' },
        ]"
        >
          <a-input-password v-model:value="formState.checkPassword" placeholder="请输入确认密码"/>
        </a-form-item>
        <div class="tips">
          已有账号!
          <RouterLink to="/login">去登录</RouterLink>
        </div>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import {reactive} from 'vue';
import {useLoginUserStore} from "@/stores/loginUserStore";
import {message} from "ant-design-vue";
import {getLoginUserUsingGet, loginUsingPost, registerUsingPost} from "@/api/systemController";
import {useRouter} from "vue-router";

const formState = reactive<API.RegisterDto>({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
});

const router = useRouter()
const loginUserStore = useLoginUserStore()
//-------------------------------------邮箱校验--------------------------------------
//自定义的邮箱和手机验证规则
const checkEmail = (rule, value,callback) =>{
  // const regEmail = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
  const regEmail = /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/;
  if(value != '' && !regEmail.test(value)) {
    callback(new Error('请输入有效的邮箱'));
  }else {
    callback();
  }
};

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  //校验两次输入的密码是否一致
  if (formState.userPassword !== formState.checkPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  const res = await registerUsingPost(values)
  // 注册成功，跳转至登录页
  if (res.code === 0 && res.data) {
    message.success('注册成功')
    router.push({
      path: '/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.msg)
  }
}
</script>

<style scoped>
.registerBg {
  /*background-image: url("../assets/img.png");*/
  height: 100vh;
  background-size: cover; /* 背景图片覆盖整个容器 */
  /*background-position: center; !* 背景居中 *!*/
  display: flex;
  justify-content: center;
  align-items: center;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0; /* 撑满整个视口 */
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

#registerView {
  max-width: 360px;
  margin: 0 auto;
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

</style>
