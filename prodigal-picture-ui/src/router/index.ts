import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import UserDetailView from '../views/user/UserDetailView.vue'
import PictureDetailView from '../views/picture/PictureDetailView.vue'
import ACCESS_ENUM from "@/access/accessEnum";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: '关于',
      component:()=>import('../views/AboutView.vue')
    },
    {
      path:'/login',
      name:'login',
      component: LoginView
    },
    {
      path:'/register',
      name:'register',
      component:()=>import('../views/RegisterView.vue')
    },
    {
      path:'/admin/userManager',
      name:'用户管理',
      component:()=>import('../views/admin/UserManagerView.vue'),
      meta:{
        access:ACCESS_ENUM.SUPER_ADMIN || ACCESS_ENUM.ADMIN
      }
    },
    {
      path:'/user/userdetail',
      name:'个人中心',
      component: UserDetailView
    },
    {
      path:'/admin/pictureManager',
      name:'图片管理',
      component:()=>import('../views/admin/PictureManagerView.vue'),
      meta:{
        access:ACCESS_ENUM.SUPER_ADMIN || ACCESS_ENUM.ADMIN
      }
    },
    {
      path:'/picture/add_picture',
      name:'创建图片',
      component:()=>import('../views/picture/AddPictureView.vue')
    },{
      path:'/picture/:id',
      name:'图片详情页',
      component: PictureDetailView,
      props: true
    },
  ],
})

export default router
