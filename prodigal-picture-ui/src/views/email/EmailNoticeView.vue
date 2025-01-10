<template>
  <div class="email-notice">
    <h2>
      公告
    </h2>
    <div style="margin-bottom: 16px"/>
    <a-list
      class="demo-loadmore-list"
      item-layout="horizontal"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #renderItem="{ item:email,index }">
        <a-list-item>
          <a-comment>
            <template #author>{{ email.subject }}</template>
            <template #avatar>
              <a-avatar src="https://zos.alipayobjects.com/rmsportal/ODTLcjxAfvqbxHnVXCYX.png" alt="Prodigal"/>
            </template>
            <template #content>
              {{ email.txt }}
            </template>
            <template #datetime>
              <a-tooltip :title="dayjs(email.sendTime).format('YYYY-MM-DD HH:mm:ss')">
                <span>{{ dayjs(email.sendTime).fromNow() }}</span>
              </a-tooltip>
            </template>
            <template #actions>
                <span key="comment-basic-like">
                  <a-tooltip title="已读">
                    <template v-if="action === 'liked'">
                      <LikeFilled @click="like(email)"/>
                    </template>
                    <template v-else>
                      <LikeOutlined @click="like(email)"/>
                    </template>
                  </a-tooltip>
                  <span style="padding-left: 8px; cursor: auto">
                    {{ likes }}
                  </span>
               </span>
              <span key="comment-basic-reply-to">
                <a-tooltip title="已读用户">
                  <UsergroupAddOutlined @click="showReadUsers(announcement)" />
                </a-tooltip>
              </span>
            </template>
            <template #extra>
              <div v-if="showingReadUsers === announcement.announcementId">
                <a-list item-layout="horizontal" :data-source="getReadUsers(announcement)">
                  <template #renderItem="{ item: user }">
                    <a-list-item>
                      <a-list-item-meta>
                        <template #avatar>
                          <a-avatar src="https://joeschmoe.io/api/v1/random" :alt="user.username" />
                        </template>
                        <template #title>{{ user.username }}</template>
                      </a-list-item-meta>
                    </a-list-item>
                  </template>
                </a-list>
              </div>
            </template>
          </a-comment>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>
<script lang="ts" setup>
import dayjs from 'dayjs';
import {LikeFilled, LikeOutlined, DislikeFilled, DislikeOutlined,UsergroupAddOutlined} from '@ant-design/icons-vue';
import {ref, onMounted, reactive,computed} from 'vue';
import relativeTime from 'dayjs/plugin/relativeTime';
import {useLoginUserStore} from "@/stores/loginUserStore";
import {listEmailByPageUsingPost} from "@/api/emailController";
import {message} from "ant-design-vue";

dayjs.extend(relativeTime);
const likes = ref<number>(0);

const action = ref<string>();
const like = () => {
  likes.value = 1;
  action.value = 'liked';
};
//获取当前登录用户的邮件信息
const loginUserStore = useLoginUserStore()
const dataList = ref<API.EmailVO[]>([])
const emilMessage = reactive<API.EmailQueryDto>({
  to: '',
  subject: '',
})
// 搜索条件
const searchParams = reactive<API.EmailQueryDto>({
  current: 1,
  pageSize: 5
})
const fetchData = async () => {
  const res = await listEmailByPageUsingPost({
    ...emilMessage,
    status: 2,
  });
  console.log(res)
  if (res.code === 0) {
    console.log(res.data)
    dataList.value = res.data.records ?? []
  } else {
    message.error('获取信息失败，请重试！,' + res.msg)
  }
}
// 设置分页的中文语言包
const locale = {
  items_per_page: '/页',
  jump_to: '跳至',
  page: '页',
};
// 分页参数
const pagination= computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    showSizeChanger: true,
    locale: locale,
    pageSizeOptions: ['5', '10', '20', '30'],//可选的页面显示条数
  }
})
// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

onMounted(() => {
  fetchData()
})

</script>

