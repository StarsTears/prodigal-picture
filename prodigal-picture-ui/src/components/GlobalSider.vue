<template>
  <div id="globalSider">
    <a-layout-sider v-if="loginUserStore.loginUser.id"
                    width="200"
                    style="background: var(--bg-sider)"
                    collapsed-width="0"
                    collapsible
                    :trigger="null"
                    v-model:collapsed="collapsed"
    >
      <a-menu
        v-model:selectedKeys="current"
        mode="inline"
        :items="menuItems"
        @click="handleMenuClick"
      />
    </a-layout-sider>
  </div>
</template>

<script setup lang="ts">
import {computed, h, ref, watchEffect} from "vue";
import {
  PictureOutlined, UserOutlined, TeamOutlined,
  FolderOutlined, UploadOutlined, MailOutlined, SettingOutlined,
} from '@ant-design/icons-vue';
import {useRouter} from "vue-router";
import {useLoginUserStore} from "@/stores/loginUserStore";
import {SPACE_TYPE_ENUM} from "@/constants/space";
import {listMyTeamSpaceUsingPost} from "@/api/spaceUserController";
import ACCESS_ENUM from "@/access/accessEnum";
import {message} from "ant-design-vue";

const props = defineProps<{
  collapsed: boolean
}>()

const emit = defineEmits<{
  'update:collapsed': [value: boolean]
}>()

const collapsed = computed({
  get: () => props.collapsed,
  set: (val) => emit('update:collapsed', val)
})

const loginUserStore = useLoginUserStore()
const router = useRouter()

const isAdmin = computed(() => {
  const roles = loginUserStore.loginUser?.userRole?.split(',') || [];
  return roles.some(r => r === ACCESS_ENUM.ADMIN || r === ACCESS_ENUM.SUPER_ADMIN);
})

// 固定菜单
const fixedMenuItems = [
  {
    key: '/',
    label: '公共图库',
    icon: () => h(PictureOutlined),
  },
  {
    key: '/picture/add_picture',
    label: '创建图片',
    icon: () => h(UploadOutlined),
  },
  {
    key: '/space/my_space',
    label: '我的空间',
    icon: () => h(UserOutlined),
  },
  {
    key: '/space/add_space?type=' + SPACE_TYPE_ENUM.TEAM,
    label: '创建团队',
    icon: () => h(TeamOutlined),
  },
]

// 管理员菜单组
const adminMenuItems = [
  {
    key: '/admin/pictureManager',
    label: '图片管理',
    icon: () => h(PictureOutlined),
  },
  {
    key: '/admin/spaceManager',
    label: '空间管理',
    icon: () => h(FolderOutlined),
  },
  {
    key: '/admin/userManager',
    label: '用户管理',
    icon: () => h(UserOutlined),
  },
  {
    key: '/admin/dictManager',
    label: '字典管理',
    icon: () => h(SettingOutlined),
  },
  {
    key: '/admin/emailManager',
    label: '邮件管理',
    icon: () => h(MailOutlined),
  },
]

const teamSpaceList = ref<API.SpaceUserVO[]>([])
const menuItems = computed(() => {
  const items = [...fixedMenuItems]

  // 团队空间分组
  if (teamSpaceList.value.length > 0) {
    const teamSpaceSubMenus = teamSpaceList.value.map((spaceUser) => {
      return {
        key: '/space/' + spaceUser.spaceId,
        label: spaceUser.space?.spaceName || '未命名空间',
      }
    })
    items.push({
      type: 'group',
      label: '我的团队',
      key: 'teamSpace',
      children: teamSpaceSubMenus,
    } as any)
  }

  // 管理员菜单组
  if (isAdmin.value) {
    items.push({
      type: 'group',
      label: '管理',
      key: 'adminGroup',
      children: adminMenuItems,
    } as any)
  }

  return items
})

// 加载团队空间列表
const fetchTeamSpaceList = async () => {
  const res = await listMyTeamSpaceUsingPost()
  if (res.code === 0 && res.data) {
    teamSpaceList.value = res.data
  } else {
    message.error('加载我的团队空间失败，' + res.msg)
  }
}

watchEffect(() => {
  if (loginUserStore.loginUser.id) {
    fetchTeamSpaceList()
  }
})

// 当前选中的菜单
const current = ref<string[]>([])
router.afterEach((to) => {
  current.value = [to.path]
})

const handleMenuClick = ({key}: { key: string }) => {
  router.push(key)
}
</script>

<style>
  #globalSider .ant-layout-sider {
    background: none;
  }
  #globalSider .ant-menu {
    background: transparent;
  }
</style>
