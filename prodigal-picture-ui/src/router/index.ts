import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
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
        access:ACCESS_ENUM.ADMIN
      }
    }
  ],
})

export default router
