import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet } from '@/api/systemController'
import router from "@/router";

/**
 * 存储登录用户信息的状态
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.UserVO>({
    userName: '未登录',
  })
  const redirectPath = ref<string | null>(null)

  /**
   * 设置重定向路径
   * @param path
   */
  function setRedirectPath(path: string) {
    redirectPath.value = path
  }

  /**
   * 获取重定向路径
   */
  function getRedirectPath(): string | null {
    return redirectPath.value
  }

  /**
   * 清除重定向路径
   */
  function clearRedirectPath() {
    redirectPath.value = null
  }
  /**
   * 远程获取登录用户信息
   */
  async function fetchLoginUser() {
    //TODO: 从服务器获取登录用户信息
    const res = await getLoginUserUsingGet()
    if (res.code === 0 && res.data) {
      loginUser.value = res.data
      // 登录成功后如果有重定向路径，则跳转到该路径
      const path = redirectPath.value
      if (path) {
        router.push(path)
        clearRedirectPath()
      } else {
        router.push('/')
      }
    }
  }

  /**
   * 设置登录用户信息
   * @param newLoginUser
   */
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
    // 登录成功后如果有重定向路径，则跳转到该路径
    const path = redirectPath.value
    if (path) {
      router.push(path)
      clearRedirectPath()
    }
  }
  function checkLogin() {
    if (!loginUser.value?.id) {
      // 保存当前路由
      setRedirectPath(router.currentRoute.value.fullPath)
      router.push({
        path: '/login',
      })
      return
    }
  }
  return { loginUser, fetchLoginUser, setLoginUser,checkLogin, setRedirectPath,getRedirectPath, clearRedirectPath }
})
