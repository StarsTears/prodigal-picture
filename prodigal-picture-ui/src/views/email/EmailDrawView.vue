<template>
  <a-drawer
    v-model:open="open"
    title="通知"
    placement="right"
    :width="420"
  >
    <div v-if="dataList.length === 0" style="text-align: center; padding: 80px 0">
      <a-empty description="暂无通知" />
    </div>

    <a-list
      v-else
      :data-source="dataList"
      item-layout="horizontal"
    >
      <template #renderItem="{ item: email }">
        <a-list-item class="notice-item" @click="showDetail(email)">
          <a-list-item-meta>
            <template #avatar>
              <a-avatar :src="logo" style="background: #fff" />
            </template>
            <template #title>
              <a-space :size="8">
                <span class="notice-subject">{{ email.subject || '(无主题)' }}</span>
                <a-tag :color="email.type === 0 ? 'blue' : 'orange'" style="font-size: 11px; line-height: 18px">
                  {{ EMAIL_TYPE_MAP[email.type!] }}
                </a-tag>
              </a-space>
            </template>
            <template #description>
              <div class="notice-desc">
                <span class="notice-time">{{ dayjs(email.sendTime).format('MM-DD HH:mm') }}</span>
                <span class="notice-txt">{{ truncateText(email.txt, 50) }}</span>
              </div>
            </template>
          </a-list-item-meta>
        </a-list-item>
      </template>
    </a-list>

    <a-modal
      v-model:open="detailOpen"
      :title="currentNotice?.subject || '通知详情'"
      :footer="null"
      width="560px"
      destroy-on-close
    >
      <template v-if="currentNotice">
        <a-descriptions :column="1" bordered size="small" style="margin-bottom: 12px">
          <a-descriptions-item label="类型">
            <a-tag :color="currentNotice.type === 0 ? 'blue' : 'orange'">
              {{ EMAIL_TYPE_MAP[currentNotice.type!] }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="发送时间">
            {{ dayjs(currentNotice.sendTime).format('YYYY-MM-DD HH:mm:ss') }}
          </a-descriptions-item>
        </a-descriptions>
        <a-divider style="margin: 12px 0" />
        <div class="detail-content">{{ currentNotice.txt }}</div>
      </template>
    </a-modal>
  </a-drawer>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue';
import { useLoginUserStore } from '@/stores/loginUserStore';
import { listEmailByPageUsingPost } from '@/api/emailController';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { EMAIL_TYPE_MAP } from '@/constants/email';
import logo from '@/assets/logo.svg';

const open = ref(false);

const showDrawer = () => {
  open.value = true;
  fetchData();
};

defineExpose({ showDrawer });

const loginUserStore = useLoginUserStore();
const dataList = ref<API.EmailVO[]>([]);

const fetchData = async () => {
  const res = await listEmailByPageUsingPost({
    to: loginUserStore?.loginUser.userEmail,
    status: 2,
    current: 1,
    pageSize: 50,
  });
  if (res.code === 0) {
    dataList.value = res.data?.records ?? [];
  } else {
    message.error('获取通知失败，' + res.msg);
  }
};

const truncateText = (text: string | undefined, maxLen: number): string => {
  if (!text) return '';
  return text.length > maxLen ? text.slice(0, maxLen) + '...' : text;
};

const detailOpen = ref(false);
const currentNotice = ref<API.EmailVO | null>(null);

const showDetail = (email: API.EmailVO) => {
  currentNotice.value = email;
  detailOpen.value = true;
};
</script>

<style scoped>
.notice-item {
  cursor: pointer;
  padding: 12px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}
.notice-item:hover {
  background: #fafafa;
}
.notice-subject {
  font-size: 14px;
  font-weight: 500;
}
.notice-desc {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.notice-time {
  font-size: 12px;
  color: #999;
}
.notice-txt {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.detail-content {
  font-size: 15px;
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
  color: #333;
  max-height: 400px;
  overflow-y: auto;
}
</style>
