<template>
  <div id="globalHeader" :style="{ position: 'fixed', zIndex: 1, width: '100%' }">
    <a-row :wrap="false" align="middle">
      <a-col flex="200px" class="logo-col">
        <RouterLink to="/">
          <div class="title-bar">
            <img src="../assets/logo.svg" alt="Prodigal Picture" class="logo">
            <div class="title">Prodigal Picture</div>
          </div>
        </RouterLink>
      </a-col>
      <a-col flex="auto" class="menu-col">
        <a-menu v-model:selectedKeys="current" mode="horizontal" :items="items" @click="doMenuClick"/>
      </a-col>
      <a-col flex="none" class="user-col">
        <div class="login-status">
          <button
            class="theme-toggle"
            type="button"
            :title="themeStore.darkMode ? '切换浅色模式' : '切换暗黑模式'"
            :aria-label="themeStore.darkMode ? '切换浅色模式' : '切换暗黑模式'"
            @click="themeStore.toggle"
          >
            <span class="theme-toggle-icon" :class="{ dark: themeStore.darkMode }">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="5" />
                <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
              </svg>
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
              </svg>
            </span>
          </button>

          <div v-if="loginUserStore.loginUser.id">
            <a-flex :gap="8" align="center">
              <a-badge :count="unreadCount" :overflow-count="99" :show-zero="false" size="small">
                <button class="notice-btn" @click="doNotice" :title="'通知' + (unreadCount ? `（${unreadCount} 条未读）` : '')">
                  <BellOutlined />
                </button>
              </a-badge>
              <a-dropdown>
                <a-space>
                  <a-avatar v-if="loginUserStore.loginUser.userAvatar" :src="loginUserStore.loginUser.userAvatar" :size="32"/>
                  <a-avatar v-if="!loginUserStore.loginUser.userAvatar" :size="32" style="color: #f56a00; background-color: #fde3cf">{{loginUserStore.loginUser.userName.charAt(0)}}</a-avatar>
                  <span class="user-name-text">{{ loginUserStore.loginUser.userName ?? "无名" }}</span>
                </a-space>
                <template #overlay>
                  <a-menu style="min-width: 130px">
                    <a-menu-item @click="doShow">
                      <UserOutlined/>
                      个人中心
                    </a-menu-item>
                    <a-menu-item>
                      <RouterLink to="/space/my_space">
                        <EyeOutlined/>
                        我的空间
                      </RouterLink>
                    </a-menu-item>
                    <a-menu-item @click=doLogout>
                      <LogoutOutlined/>
                      退出
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-flex>
          </div>
          <div v-else>
            <a-button type="primary"  href="/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
  <!--用户邮件 告警信息-->
<EmailDrawView ref="emailModalRef"/>
  <!-- 个人中心对话框-->
  <a-modal v-model:open="open"
           title="个人中心"
           :confirm-loading="confirmLoading"
           @ok="handleOk"
           footer=""
           style="max-width: 720px; width: 90%;"
  >
    <h4>{{ userDetailText }}</h4>
    <a-form :model="userDetail"
            layout="horizontal"
            :disabled="componentDisabled"
            :label-col="labelCol"
            :wrapper-col="wrapperCol"
            ref="formRef"
            style="max-width: 600px"
    >
      <a-form-item name="userProfile" label="头像">
        <a-avatar v-if="loginUserStore.loginUser.userAvatar" :src="loginUserStore.loginUser.userAvatar"/>
        <a-avatar v-if="!loginUserStore.loginUser.userAvatar" style="color: #f56a00; background-color: #fde3cf">{{loginUserStore.loginUser.userName.charAt(0)}}</a-avatar>
<!--        <a-image :src="userDetail?.userAvatar" :width="50"/>-->
<!--        <a-upload v-if="!componentDisabled"-->
<!--                  name="file"-->
<!--                  list-type="picture"-->
<!--                  :custom-request="doUpload"-->
<!--                  :before-upload="beforeUpload"-->
<!--        >-->
<!--          <a-button>-->
<!--            <upload-outlined/>-->
<!--            Click to Upload-->
<!--          </a-button>-->
<!--        </a-upload>-->
      </a-form-item>
      <a-form-item
        name="userAccount"
        label="账号"
        :rules="[{ required: true, message: 'Please input the title of collection!' }]"
      >
        <a-input v-model:value="userDetail.userAccount" placeholder="账号"/>
      </a-form-item>
      <a-form-item name="userName" label="昵称">
        <a-input v-model:value="userDetail.userName" placeholder="昵称"/>
      </a-form-item>
      <a-form-item name="userEmail" label="邮箱"
                   :rules="[{ required: true, message: '邮箱' },
                              { type: 'email',validator: checkEmail, message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }]">
        <a-input v-model:value="userDetail.userEmail" placeholder="邮箱"/>
      </a-form-item>
      <a-form-item name="userProfile" label="简介">
        <a-textarea v-model:value="userDetail.userProfile" placeholder="简介"/>
      </a-form-item>

    </a-form>
    <a-space wrap class="detail-footer">
      <a-button v-if="componentDisabled" :icon="h(EditOutlined)" type="default" @click="doEdit">
        修改
      </a-button>
      <a-button v-if="componentDisabled" type="default" @click="doOpenChangePassword">
        <template #icon><KeyOutlined/></template>
        修改密码
      </a-button>
      <a-space wrap v-if="!componentDisabled">
        <a-button :icon="h(SaveOutlined)" type="default" @click="doSave">
          保存
        </a-button>
        <a-button type="dashed" @click="doCancel">
          取消
          <template #icon>
            <UndoOutlined/>
          </template>
        </a-button>
      </a-space>
    </a-space>
  </a-modal>

  <!-- 修改密码 -->
  <a-modal v-model:open="passwordModalOpen" title="修改密码" :footer="false" @cancel="closePasswordModal">
    <a-form :model="passwordForm" layout="vertical" @finish="doChangePassword">
      <a-form-item label="原密码" name="oldPassword"
                   :rules="[{ required: true, message: '请输入原密码' }]">
        <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入原密码"/>
      </a-form-item>
      <a-form-item label="新密码" name="newPassword"
                   :rules="[{ required: true, message: '请输入新密码' }, { min: 6, message: '密码不能小于 6 位' }]">
        <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码"/>
      </a-form-item>
      <a-form-item label="确认密码" name="checkPassword"
                   :rules="[{ required: true, message: '请确认新密码' },
                            { validator: validateChangePasswordCheck, trigger: 'blur' }]">
        <a-input-password v-model:value="passwordForm.checkPassword" placeholder="请确认新密码"/>
      </a-form-item>
      <a-form-item class="action-bar">
        <a-button type="primary" html-type="submit">确认修改</a-button>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import {computed, h, reactive, ref, watch} from 'vue';
import {
  HomeOutlined, LogoutOutlined, UserOutlined,
  EyeOutlined, EditOutlined, GlobalOutlined, SaveOutlined, UndoOutlined, SoundOutlined,
  BellOutlined,KeyOutlined,
} from '@ant-design/icons-vue';
import {type FormInstance, MenuProps, message, notification, UploadProps} from 'ant-design-vue';
import {useRouter} from "vue-router";
import {useLoginUserStore} from "@/stores/loginUserStore";
import {editUserUsingPost, updateUserUsingPost, changePasswordUsingPost} from "@/api/userController";
import {helloUsingGet, logoutUsingPost} from "@/api/systemController";
import EmailDrawView from "@/views/email/EmailDrawView.vue";
import { useThemeStore } from "@/stores/themeStore";
import { useSSE } from "@/composables/useSSE";

const loginUserStore = useLoginUserStore();
const themeStore = useThemeStore();
const { connect, disconnect, onEmailSent } = useSSE();
const unreadCount = ref(0);

// 登录后建立 SSE 连接
watch(() => loginUserStore.loginUser?.id, (newId) => {
  if (newId) {
    connect();
  } else {
    disconnect();
  }
}, { immediate: true });

// 收到邮件通知时提示, 未读数 +1
onEmailSent((data) => {
  unreadCount.value++
  notification.info({
    message: '邮件通知',
    description: data.message || '您收到一封新的通知邮件',
    placement: 'topRight',
    top: '64px',
  });
});

const originItems = [
  {
    key: "/",
    icon: h(HomeOutlined),
    label: '首页',
    title: '首页',
  },
  {
    key: "/email/notice",
    icon: h(SoundOutlined),
    label: '公告',
    title: '公告',
  },{
    key: "/about",
    icon: h(GlobalOutlined),
    label: '关于',
    title: '关于',
  },
];

const items = computed<MenuProps['items']>(() => originItems);


const router = useRouter();
//刷新页面时，菜单高亮
const current = ref<string[]>([]);
router.afterEach((to, from, next) => {
  current.value = [to.path];
})
//路由跳转事件
const doMenuClick = ({key}) => {
  router.push({path: key})
}
const doLogin = () => {
  useLoginUserStore().checkLogin()
}
/**
 * 退出登录
 */
const doLogout = async () => {
  const res = await logoutUsingPost()
  if (res.code === 0) {
    loginUserStore.setLoginUser({
      userName: "未登录"
    })
    message.success('退出成功')
    useLoginUserStore().clearRedirectPath();//清空重定向路径
    await router.push('/')
  } else {
    message.error('退出失败，' + res.msg)
  }
}

/**
 * 打开个人中心
 */
const userDetailText = ref<string>('personal information in the modal');
const open = ref<boolean>(false);
const componentDisabled = ref<boolean>(true);
const confirmLoading = ref<boolean>(false);
const labelCol = {style: {width: '150px'}};
const wrapperCol = {span: 14};
const formRef = ref<FormInstance>();
const userDetail = reactive<API.UserVO>({
  id: '',
  userName: '',
  userAvatar: '',
  useAccount: '',
  userEmail: '',
  userRole: '',
  userProfile: '',
});
//写入用户信息
const doShow = () => {
  userDetail.id = loginUserStore.loginUser.id;
  userDetail.userName = loginUserStore.loginUser.userName;
  userDetail.userAccount = loginUserStore.loginUser.userAccount;
  userDetail.userAvatar = loginUserStore.loginUser.userAvatar;
  userDetail.userEmail = loginUserStore.loginUser.userEmail;
  userDetail.userRole = loginUserStore.loginUser.userRole;
  userDetail.userProfile = loginUserStore.loginUser.userProfile;
  open.value = true;
  componentDisabled.value = true
};

const doEdit = () => {
  componentDisabled.value = false
}

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
const doSave = async () => {
  const res = await editUserUsingPost(userDetail)
  if (res.code === 0 && res.data) {
    message.success('修改成功')
    await loginUserStore.fetchLoginUser()
    userDetailText.value = 'The modal will be closed after two seconds';
    confirmLoading.value = true;
    setTimeout(() => {
      open.value = false;
      confirmLoading.value = false;
    }, 2000);
  } else {
    message.error('修改失败,' + res.msg)
  }
};
const doCancel = () => {
  componentDisabled.value = true
  formRef.value?.resetFields();
  open.value = false
}

//-------------------------------------修改密码--------------------------------------
const passwordModalOpen = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  checkPassword: '',
})

const doOpenChangePassword = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.checkPassword = ''
  passwordModalOpen.value = true
}

const closePasswordModal = () => {
  passwordModalOpen.value = false
}

const validateChangePasswordCheck = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (value && value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const doChangePassword = async () => {
  const res = await changePasswordUsingPost({
    oldPassword: passwordForm.oldPassword,
    newPassword: passwordForm.newPassword,
    checkPassword: passwordForm.checkPassword,
  })
  if (res.code === 0) {
    message.success('密码修改成功，请重新登录')
    passwordModalOpen.value = false
    // 退出登录
    await logoutUsingPost()
    loginUserStore.setLoginUser({ userName: '未登录' })
    router.push('/login')
  } else {
    message.error('密码修改失败，' + res.msg)
  }
}

/**
 * 上传前的校验
 * @param file
 */
const beforeUpload = (file: UploadProps['fileList'][number]) => {
  //校验图片格式
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    message.error('You can only upload JPG/PNG file!');
  }
  //校验图片大小
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error('Image must smaller than 2MB!');
  }
  return isJpgOrPng && isLt2M;
};


//-------------------------------------邮件告警信息-------------------------------------
const emailModalRef = ref(false)
// 分享
const doNotice = (e: Event) => {
  e.stopPropagation()
  unreadCount.value = 0
  if (emailModalRef.value) {
    emailModalRef.value.showDrawer()
  }
}

</script>

<style scoped>
#globalHeader {
  background: var(--bg-header);
}

#globalHeader :deep(.ant-menu) {
  background: transparent;
}

#globalHeader .title-bar {
  display: flex;
  align-items: center;
}

#globalHeader .logo {
  height: 48px;
  width: 48px;
  border-radius: 12px;
  flex-shrink: 0;
}

#globalHeader .title {
  color: purple;
  font-size: 18px;
  font-weight: bold;
  font-family: "Segoe UI Historic";
  margin-left: 10px;
  white-space: nowrap;
}

.detail-footer {
  display: flex;
  justify-content: flex-end;
}

#globalHeader .login-status {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  height: 64px;
  padding-right: 24px;
  white-space: nowrap;
}

#globalHeader .user-col {
  flex-shrink: 0;
}

/* 主题切换按钮 */
.theme-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  flex-shrink: 0;
  background: var(--bg-image-placeholder);
  border: 1px solid var(--border-color);
  border-radius: 50%;
  cursor: pointer;
  outline: none;
  transition: background .25s, border-color .25s, box-shadow .25s;
}

.theme-toggle:hover {
  background: var(--bg-switch, #e8e8e8);
  border-color: var(--primary-color, #4096ff);
  box-shadow: 0 0 0 2px rgba(64, 150, 255, .15);
}

/* 通知按钮 */
.notice-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  flex-shrink: 0;
  background: var(--bg-image-placeholder);
  border: 1px solid var(--border-color);
  border-radius: 50%;
  cursor: pointer;
  outline: none;
  color: var(--text-secondary);
  font-size: 16px;
  transition: background .25s, border-color .25s, box-shadow .25s, color .25s;
}

.notice-btn:hover {
  background: var(--bg-switch, #e8e8e8);
  border-color: var(--primary-color, #4096ff);
  box-shadow: 0 0 0 2px rgba(64, 150, 255, .15);
  color: var(--primary-color, #4096ff);
}

.theme-toggle-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  color: #f5a623;
  transition: color .35s ease, transform .4s ease;
}

.theme-toggle-icon.dark {
  color: #a78bfa;
  transform: rotate(180deg);
}

.theme-toggle-icon svg {
  position: absolute;
  transition: opacity .35s ease, transform .4s ease;
}

/* 浅色模式：显示太阳 */
.theme-toggle-icon svg:first-child {
  opacity: 1;
  transform: scale(1);
}

.theme-toggle-icon.dark svg:first-child {
  opacity: 0;
  transform: scale(0.4);
}

/* 暗黑模式：显示月亮 */
.theme-toggle-icon svg:last-child {
  opacity: 0;
  transform: scale(0.4);
}

.theme-toggle-icon.dark svg:last-child {
  opacity: 1;
  transform: scale(1);
}

/* 平板及以下：隐藏用户名文本，只显示头像 */
@media (max-width: 768px) {
  #globalHeader .user-name-text {
    display: none;
  }

  #globalHeader .title {
    display: none;
  }

  #globalHeader .logo-col {
    flex: 0 0 auto !important;
  }

  #globalHeader .login-status {
    padding-right: 8px;
    gap: 4px;
  }

  #globalHeader .menu-col {
    overflow-x: auto;
  }
}
</style>
