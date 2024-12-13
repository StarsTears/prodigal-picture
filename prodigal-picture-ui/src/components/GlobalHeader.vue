<template>
  <div id="globalHeader">
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
      <a-col flex="100px">
        <div class="login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar"/>
                {{ loginUserStore.loginUser.userName ?? "无名" }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="">
                    <UserOutlined />
                    个人中心
                  </a-menu-item>
                  <a-menu-item >
                    <EditOutlined />
                    编辑
                  </a-menu-item>
                  <a-menu-item @click=doLogout>
                    <LogoutOutlined />
                    退出
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import {computed, h, ref} from 'vue';
import {HomeOutlined, GithubOutlined, LoginOutlined,LogoutOutlined,UserOutlined,EditOutlined} from '@ant-design/icons-vue';
import {MenuProps, message} from 'ant-design-vue';
import {useRouter} from "vue-router";
import {useLoginUserStore} from "@/stores/loginUserStore";
import {helloUsingGet, logoutUsingPost} from "@/api/systemController";
import checkAccess from "@/access/checkAccess";
import ACCESS_ENUM from "@/access/accessEnum";

const loginUserStore = useLoginUserStore();

const originItems = [
  {
    key: "/",
    icon: h(HomeOutlined),
    label: '首页',
    title: '首页',
  }, {
    key: '/picture/add_picture',
    label: '创建图片',
    title: '创建图片',
  },{
    key: '/admin/pictureManager',
    label: '图片管理',
    title: '图片管理',
  },  {
    key: '/admin/userManager',
    label: '用户管理',
    title: '用户管理',
  }, {
    key: '/gitHub',
    icon: h(GithubOutlined),
    label: h('a', {href: 'https://github.com/StarsTears/prodigal-picture', target: '_blank'}, 'prodigal-picture'),
  }
];

//菜单过滤
const filterMenu = (menus = [] as MenuProps[`items`]) => {
  return menus?.filter(menu => {
    if (menu.key.startsWith('/admin')) {
      let loginUser = loginUserStore.loginUser;
      if (!loginUser || !loginUser.userRole?.includes(ACCESS_ENUM.ADMIN||ACCESS_ENUM.SUPER_ADMIN)) {
        return false;
      }
    }
    return true;
  })
}
const items = computed<MenuProps['items']>(() => filterMenu(originItems));
// 过滤菜单项
// const items = menus.filter((menu) => {
//   // todo 需要自己实现 menu 到路由 item 的转化
//   const item = menuToRouteItem(menu);
//   if (item.meta?.hideInMenu) {
//     return false;
//   }
//   // 根据权限过滤菜单，有权限则返回 true，则保留该菜单
//   return checkAccess(loginUserStore.loginUser, item.meta?.access as string);
// });

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

const doLogout = async () => {
  const res = await logoutUsingPost()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: "未登录"
    })
    message.success('退出成功')
    await router.push('/login')
  } else {
    message.error('退出失败，' + res.data.msg)
  }
}
</script>

<style scoped>
#globalHeader .title-bar {
  display: flex;
  align-items: center;
}

#globalHeader .logo {
  height: 48px;
}

#globalHeader .title {
  color: black;
  font-size: 18px;
  font-weight: bold;
  margin-left: 10px;
}
</style>
