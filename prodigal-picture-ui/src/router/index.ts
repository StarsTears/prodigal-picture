import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import UserDetailView from '../views/user/UserDetailView.vue'
import PictureDetailView from '../views/picture/PictureDetailView.vue'
import MySpaceView from '../views/space/MySpaceView.vue'
import AddSpaceView from  '../views/space/AddSpaceView.vue'
import SpaceDetailView from '../views/space/SpaceDetailView.vue'
import EmailNoticeView from '../views/email/EmailNoticeView.vue'
import EmailManageView from '../views/admin/EmailManageView.vue'
import ACCESS_ENUM from "@/access/accessEnum";
import FullScreenLayout from "@/layouts/FullScreenLayout.vue";
import BasicLayout from '@/layouts/BasicLayout.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: BasicLayout,
      children:[{
        path: '',
        component:HomeView
      }]
    },
    {
      path: '/about',
      name: '关于',
      component: BasicLayout,
      children:[{
        path: '',
        component:()=>import('../views/AboutView.vue')
      }]
    },
    {
      path:'/login',
      name:'login',
      component: FullScreenLayout,
      children:[{
        path: '',
        component: LoginView,
      }]
    },
    {
      path:'/register',
      name:'register',
      component: FullScreenLayout,
      children:[{
        path: '',
        component:()=>import('../views/RegisterView.vue')
      }]
    },
    {
      path:'/admin/userManager',
      name:'用户管理',
      component: BasicLayout,
      meta:{
        access:ACCESS_ENUM.SUPER_ADMIN || ACCESS_ENUM.ADMIN
      },
      children:[{
        path: '',
        component:()=>import('../views/admin/UserManagerView.vue'),
      }]
    },
    {
      path:'/user/userdetail',
      name:'个人中心',
      component: BasicLayout,
      children:[{
        path: '',
        component:UserDetailView,
      }]
    },
    {
      path:'/admin/pictureManager',
      name:'图片管理',
      component: BasicLayout,
      children:[{
        path: '',
        component:()=>import('../views/admin/PictureManagerView.vue'),
      }],
      meta:{
        access:ACCESS_ENUM.SUPER_ADMIN || ACCESS_ENUM.ADMIN
      }
    },
    {
      path:'/admin/spaceManager',
      name:'空间管理',
      component: BasicLayout,
      children:[{
        path: '',
        component:()=>import('../views/admin/SpaceManagerView.vue'),
      }],
      meta:{
        access:ACCESS_ENUM.SUPER_ADMIN || ACCESS_ENUM.ADMIN
      }
    },
    {
      path:'/picture/add_picture',
      name:'创建图片',
      component: BasicLayout,
      children:[{
        path: '',
        component:()=>import('../views/picture/AddPictureView.vue')
      }],
    },
    {
      path:'/picture/add_picture/batch',
      name:'批量创建图片',
      component: BasicLayout,
      children:[{
        path: '',
        component:()=>import('../views/picture/AddPictureBatchView.vue')
      }],
    },
    {
      path:'/picture/:id',
      name:'图片详情',
      component: BasicLayout,
      children:[{
        path: '',
        component: PictureDetailView,
        props: true,
      }],
    },
    {
      path:'/picture/search_picture',
      name:'图片搜索',
      component: BasicLayout,
      children:[{
        path: '',
        component:()=>import('../views/picture/SearchPictureView.vue')
      }],
    },
    {
      path:'/space/add_space',
      name:'创建空间',
      component: BasicLayout,
      children:[{
        path: '',
        component: AddSpaceView
      }],
    },
    {
      path:'/space/my_space',
      name:'我的空间',
      component: BasicLayout,
      children:[{
        path: '',
        component: MySpaceView
      }],
    },
    {
      path:'/space/:id',
      name:'空间详情',
      component: BasicLayout,
      children:[{
        path: '',
        component: SpaceDetailView,
        props: true,
      }],
    },
    {
      path:'/space/analyze',
      name:'空间图库分析',
      component: BasicLayout,
      children:[{
        path: '',
        component:()=>import('../views/analyze/SpaceAnalyzeView.vue'),
        props: true,
      }],
    },
    {
      path:'/email/emailManager',
      name:'邮件管理',
      component: BasicLayout,
      children:[{
        path: '',
        component: EmailManageView,
      }],
    },{
      path:'/email/notice',
      name:'公告',
      component: BasicLayout,
      children:[{
        path: '',
        component: EmailNoticeView,
      }],
    },
  ],
})

export default router
