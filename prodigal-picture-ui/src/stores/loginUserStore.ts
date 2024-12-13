import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import {getLoginUserUsingGet} from "@/api/systemController";

/**
 * 存储登录用户信息的状态
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.UserVO>({
    userName:"未登录"
  })

  /**
   * 远程获取登录用户信息
   */
  async function fetchLoginUser() {
    //TODO: 从服务器获取登录用户信息
    const res = await getLoginUserUsingGet();
    if (res.code === 0 && res.data){
      loginUser.value = res.data
    }
  }

  /**
   * 设置登录用户信息
   * @param newLoginUser
   */
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
