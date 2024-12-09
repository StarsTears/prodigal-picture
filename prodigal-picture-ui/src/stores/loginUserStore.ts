import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

/**
 * 存储登录用户信息的状态
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<any>({
    userName:"未登录"
  })
  async function fetchLoginUser() {
    //TODO: 从服务器获取登录用户信息
    // const res = await getCurrentUser();
    // if (res.code === 200 && res.data.data){
    //   loginUser.value = res.data.data
    // }
    setTimeout(()=>{
      loginUser.value = {
        id:1,
        userName:"admin"
      }
    },3000)
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
