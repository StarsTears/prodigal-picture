<template>
  <div class="email-notice">
    <a-list
      class="demo-loadmore-list"
      item-layout="horizontal"
      :data-source="dataList"
    >
      <template #renderItem="{ item:email,index }">
        <a-list-item>
          <a-comment >
            <template #author>{{ email.subject }}</template>
            <template #avatar>
              <a-avatar src="https://joeschmoe.io/api/v1/random" alt="Han Solo"/>
            </template>
            <template #content>
              <p>
                {{ email.txt }}
              </p>
            </template>
            <template #datetime>
              <a-tooltip :title="dayjs(email.sendTime).format('YYYY-MM-DD HH:mm:ss')">
                <span>{{ dayjs(email.sendTime).fromNow() }}</span>
              </a-tooltip>
            </template>
          </a-comment>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>
<script lang="ts" setup>
import dayjs from 'dayjs';
import {LikeFilled, LikeOutlined, DislikeFilled, DislikeOutlined} from '@ant-design/icons-vue';
import {ref, onMounted, reactive} from 'vue';
import relativeTime from 'dayjs/plugin/relativeTime';
import {useLoginUserStore} from "@/stores/loginUserStore";
import {listEmailUsingPost} from "@/api/emailController";
import {message} from "ant-design-vue";

dayjs.extend(relativeTime);

//获取当前登录用户的邮件信息
const loginUserStore = useLoginUserStore()
const dataList = ref<API.Email[]>([])
const emilMessage = reactive<API.QueryEmailDto>({
  to: '',
  subject: '',
})
const fetchData = async () => {
  const res = await listEmailUsingPost({
    ...emilMessage
  });
  console.log(res)
  if (res.code === 0) {
    console.log(res.data)
    dataList.value = res.data ?? []
  } else {
    message.error('获取信息失败，请重试！,' + res.msg)
  }
}
onMounted(() => {
  fetchData()
})
</script>

