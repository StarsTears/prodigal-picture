<template>
  <div id="globalHeader" :style="{ position: 'fixed', zIndex: 1, width: '100%' }">
    <a-row :wrap="false">
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="title-bar">
            <img src="../assets/logo.svg" alt="Prodigal Picture" class="logo">
            <div class="title">Prodigal Picture</div>
          </div>
        </RouterLink>
      </a-col>
      <a-col flex="auto">
        <a-menu v-model:selectedKeys="current" mode="horizontal" :items="items" @click="doMenuClick"/>
      </a-col>
      <a-col flex="200px">
        <div class="login-status">

          <div v-if="loginUserStore.loginUser.id">
            <a-flex >
              <BellOutlined @click="doNotice"  style="margin: 25px 10px;color: grey"/>
              <a-button shape="circle" type="text" style="margin: 18px 4px" @click="themeStore.toggle">
                <template #icon>
                  <svg v-if="themeStore.darkMode" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="5"/>
                    <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/>
                  </svg>
                  <svg v-else viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
                  </svg>
                </template>
              </a-button>
              <a-dropdown>
                <a-space>
                  <!--<a-avatar :src="loginUserStore.loginUser.userAvatar"/>-->
                  <a-avatar v-if="loginUserStore.loginUser.userAvatar" :src="loginUserStore.loginUser.userAvatar"/>
                  <a-avatar v-if="!loginUserStore.loginUser.userAvatar" style="color: #f56a00; background-color: #fde3cf">{{loginUserStore.loginUser.userName.charAt(0)}}</a-avatar>
                  {{ loginUserStore.loginUser.userName ?? "无名" }}
                </a-space>
                <template #overlay>
                  <a-menu>
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
           style="width: 720px;"
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
  HomeOutlined, LogoutOutlined, UserOutlined, PictureOutlined, FolderOutlined,
  EyeOutlined, UploadOutlined, EditOutlined, GlobalOutlined, SaveOutlined, UndoOutlined, SoundOutlined,
  BellOutlined,MailOutlined,KeyOutlined
} from '@ant-design/icons-vue';
import {type FormInstance, MenuProps, message, notification, UploadProps} from 'ant-design-vue';
import {useRouter} from "vue-router";
import {useLoginUserStore} from "@/stores/loginUserStore";
import {editUserUsingPost, updateUserUsingPost, changePasswordUsingPost} from "@/api/userController";
import {helloUsingGet, logoutUsingPost} from "@/api/systemController";
import ACCESS_ENUM from "@/access/accessEnum";
import EmailDrawView from "@/views/email/EmailDrawView.vue";
import { useThemeStore } from "@/stores/themeStore";
import { useSSE } from "@/composables/useSSE";

const loginUserStore = useLoginUserStore();
const themeStore = useThemeStore();
const { connect, disconnect, onEmailSent } = useSSE();

// 登录后建立 SSE 连接
watch(() => loginUserStore.loginUser?.id, (newId) => {
  if (newId) {
    connect();
  } else {
    disconnect();
  }
}, { immediate: true });

// 收到邮件通知时提示
onEmailSent((data) => {
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
  }, {
    key: '/picture/add_picture',
    icon: h(UploadOutlined),
    label: '创建图片',
    title: '创建图片',
  }, {
    key: '/admin/pictureManager',
    icon: h(PictureOutlined),
    label: '图片管理',
    title: '图片管理',
  }, {
    key: '/admin/spaceManager',
    icon: h(FolderOutlined),
    label: '空间管理',
    title: '空间管理',
  }, {
    key: '/admin/emailManager',
    icon: h(MailOutlined),
    label: '邮件管理',
    title: '邮件管理',
  }, {
    key: '/admin/userManager',
    icon: h(UserOutlined),
    label: '用户管理',
    title: '用户管理',
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

/**
 * 过滤菜单
 * @param menus
 */
const filterMenu = (menus = [] as MenuProps[`items`]) => {
  return menus?.filter(menu => {
    if (menu.key.startsWith('/admin')) {
      let loginUser = loginUserStore.loginUser;
      if (!loginUser || !loginUser.userRole?.includes(ACCESS_ENUM.ADMIN || ACCESS_ENUM.SUPER_ADMIN)) {
        return false;
      }
    }
    return true;
  })
}
const items = computed<MenuProps['items']>(() => filterMenu(originItems));


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
}

#globalHeader .title {
  color: purple;
  font-size: 18px;
  font-weight: bold;
  font-family: "Segoe UI Historic";
  margin-left: 10px;
}

/*#globalHeader :deep(.ant-space-item) {*/
/*  padding-right: 0 ;*/
/*}*/
.detail-footer {
  display: flex;
  justify-content: flex-end; /* 将内容推到右侧 */
}

</style>
