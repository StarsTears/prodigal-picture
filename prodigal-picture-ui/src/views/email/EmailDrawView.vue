<template>
  <a-drawer
    v-model:open="open"
    class="custom-class"
    root-class-name="root-class-name"
    :root-style="{ color: 'blue' }"
    style="color: red"
    title="通知"
    placement="right"
    @after-open-change="afterOpenChange"
  >
    <a-list item-layout="horizontal" :data-source="dataList">
      <template #renderItem="{ item:email }">
        <a-list-item>
          <a-list-item-meta>
            <template #description>
              {{email.txt}}
            </template>
            <template #title>
<!--              <a href="https://www.antdv.com/">{{ item.title }}</a>-->
              {{email.subject}}
            </template>
            <template #avatar>
              <a-avatar src="https://www.antdv.com/assets/logo.1ef800a8.svg" />
            </template>
          </a-list-item-meta>
        </a-list-item>
      </template>
    </a-list>
  </a-drawer>
</template>
<script lang="ts" setup>
import {onMounted, ref,reactive} from 'vue';
import {useLoginUserStore} from "@/stores/loginUserStore";
import {listEmailByPageUsingPost} from "@/api/emailController";
import {message} from "ant-design-vue";

const open = ref<boolean>(false);
const afterOpenChange = (bool: boolean) => {
  console.log('open', bool);
};
//该函数需传递给父组件，用于打开弹窗
const showDrawer = () => {
  open.value = true;
  fetchData()
};
//将 onModal 函数暴露给父组件
defineExpose({
  showDrawer,
})

//获取当前登录用户的邮件信息
const loginUserStore = useLoginUserStore()
const dataList = ref<API.EmailVO[]>([])
const emilMessage = reactive<API.EmailQueryDto>({
  to: loginUserStore?.loginUser.userEmail,
  subject: '',
})
const fetchData = async () => {
  const res = await listEmailByPageUsingPost({
    ...emilMessage,
    status: 2,
  });
  console.log(res)
  if (res.code === 0) {
    console.log(res.data)
    dataList.value = res.data.records ??[]
  } else {
    message.error('获取信息失败，请重试！,' + res.msg)
  }
}
// onMounted(()=>{
//   fetchData()
// })
</script>


