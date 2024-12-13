import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import PictureDetailView from '../views/picture/PictureDetailView.vue'
import ACCESS_ENUM from "@/access/accessEnum.ts";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
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
      name:'userManager',
      component:()=>import('../views/admin/UserManagerView.vue'),
      meta:{
        access:ACCESS_ENUM.SUPER_ADMIN || ACCESS_ENUM.ADMIN
      }
    }, {
      path:'/admin/pictureManager',
      name:'pictureManager',
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
