<template>
  <div class="example">
    <a-spin/>
    加载中...
  </div>
</template>

<script setup lang="ts">
import {useRouter} from "vue-router";
import {useLoginUserStore} from "@/stores/loginUserStore";
import {listSpaceVoByPageUsingPost} from "@/api/spaceController";
import {message} from "ant-design-vue";
import {onMounted} from "vue";

const loginUserStore = useLoginUserStore();
const router = useRouter();
//检查当前用户是否有个人空间
const checkMySpace = async () => {
  //获取当前用户id
  const loginUser = loginUserStore.loginUser
  if (!loginUser?.id) {
    router.replace('/login')
    return
  }
  //如果用户已登录，获取其创建的空间
  const res = await listSpaceVoByPageUsingPost({
    userId: loginUser.id,
    current: 1,
    pageSize: 1,
    spaceType:0,
  });
  console.log("MySpaceView:"+JSON.stringify(res))
  if (res.code === 0) {
    //有空间，则进入第一个空间跳转至详情页
    if (res.data?.records?.length > 0) {
      const space = res.data?.records[0]
      router.replace(`/space/${space.id}`)
    } else {
      //没有则创建
      router.replace('/space/add_space')
      message.warn('您还没有创建个人空间，请创建一个！')
    }
  } else {
    message.error('获取空间信息失败，请重试！,' + res.msg)
  }
}
onMounted(() => {
  checkMySpace()
})
</script>

<style scoped>
.example {
  height: 100vh;
  text-align: center;
  /*background: rgba(0, 0, 0, 0.05);*/
  border-radius: 4px;
  margin-bottom: 20px;
  padding: 30px 50px;
  margin: 20px 0;
}
</style>
