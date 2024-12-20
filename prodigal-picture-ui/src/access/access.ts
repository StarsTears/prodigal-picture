import router from "@/router";
import {useLoginUserStore} from "@/stores/loginUserStore";
import {message} from "ant-design-vue";
import ACCESS_ENUM from "@/access/accessEnum";

//是否为首次获取登录用户
let isFirstLogin = true;
router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore();
  let loginUser = loginUserStore.loginUser;
  //确保页面刷新时，首次加载时,能等待后端返回用户信息再校验权限
  if (isFirstLogin) {
    await loginUserStore.fetchLoginUser();
    loginUser = loginUserStore.loginUser;
    isFirstLogin = false;
  }
  // // 如果之前没登陆过，自动登录
  // if (!loginUser || !loginUser.userRole) {
  //   // 加 await 是为了等用户登录成功之后，再执行后续的代码
  //   await loginUserStore.fetchLoginUser();
  //   loginUser = loginUserStore.loginUser;
  // }
  const toUrl = to.fullPath;
  if (toUrl.startsWith("/admin")){
    if (!loginUser || !loginUser.userRole?.includes(ACCESS_ENUM.ADMIN||ACCESS_ENUM.SUPER_ADMIN)){
      message.error("没有权限");
      next(`/login?redirect=${to.fullPath}`);
      return;
    }
  }
  next();
})
