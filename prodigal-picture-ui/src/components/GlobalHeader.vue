<template>
  <div id="globalHeader" :style="{ position: 'fixed', zIndex: 1, width: '100%' }">
    <a-row :wrap="false">
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="title-bar">
            <img src="../assets/logo.jpg" alt="logo" class="logo">
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

      <a-form-item name="userRole" label="角色" class="collection-create-form_last-form-item">
        <a-radio-group v-model:value="userDetail.userRole">
          <a-radio v-if="showRole || userDetail.userRole === 'administrator'" value="administrator">超级管理员</a-radio>
          <a-radio v-if="showRole || userDetail.userRole === 'admin'" value="admin">管理员</a-radio>
          <a-radio v-if="showRole || userDetail.userRole === 'user'" value="user">普通用户</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
    <a-space wrap class="detail-footer">
      <a-button v-if="componentDisabled" :icon="h(EditOutlined)" type="default" @click="doEdit">
        修改
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
</template>
<script lang="ts" setup>
import {computed, h, reactive, ref} from 'vue';
import {
  HomeOutlined, GithubOutlined, LogoutOutlined, UserOutlined, PictureOutlined, FolderOutlined,
  EyeOutlined, UploadOutlined, EditOutlined, GlobalOutlined, SaveOutlined, UndoOutlined, SoundOutlined,
  BellOutlined,MailOutlined
} from '@ant-design/icons-vue';
import {MenuProps, message, UploadProps} from 'ant-design-vue';
import {useRouter} from "vue-router";
import {useLoginUserStore} from "@/stores/loginUserStore";
import {editUserUsingPost, helloUsingGet, logoutUsingPost, updateUserUsingPost} from "@/api/systemController";
import ACCESS_ENUM from "@/access/accessEnum";
import EmailDrawView from "@/views/email/EmailDrawView.vue";

const loginUserStore = useLoginUserStore();

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
  },{
    key: "/email/notice",
    icon: h(SoundOutlined),
    label: '公告',
    title: '公告',
  }, {
    key: "/about",
    icon: h(GlobalOutlined),
    label: '关于',
    title: '关于',
  },
  // {
  //   key: '/gitHub',
  //   icon: h(GithubOutlined),
  //   label: h('a', {href: 'https://github.com/StarsTears/prodigal-picture', target: '_blank'}, 'prodigal-picture'),
  // }
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

const showRole = ref<boolean>(false)
const doEdit = () => {
  componentDisabled.value = false
  showRole.value = true
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
  showRole.value = false
};
const doCancel = () => {
  componentDisabled.value = true
  formRef.value?.resetFields();
  open.value = false
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
  background: #ffffff;
}

#globalHeader .title-bar {
  display: flex;
  align-items: center;
}

#globalHeader .logo {
  height: 48px;
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
