<template>
  <div class="email-notice">
    <h2>公告</h2>
    <div style="margin-bottom: 24px" />

    <a-spin :spinning="loading">
      <div v-if="dataList.length === 0 && !loading" class="empty-state">
        <a-empty description="暂无公告" />
      </div>

      <a-row :gutter="[16, 16]">
        <a-col v-for="email in dataList" :key="email.id" :xs="24" :sm="12" :lg="8">
          <a-card hoverable class="notice-card" @click="showDetail(email)">
            <template #title>
              <span class="card-title">{{ email.subject || '(无主题)' }}</span>
            </template>
            <template #extra>
              <a-tag color="blue">公告</a-tag>
            </template>
            <div class="card-meta">
              <span class="card-time">
                <ClockCircleOutlined style="margin-right: 4px" />
                {{ dayjs(email.sendTime).format('YYYY-MM-DD HH:mm') }}
              </span>
            </div>
            <div class="card-content">
              {{ truncateText(email.txt, 120) }}
            </div>
            <template #actions>
              <a-button type="link" size="small" @click.stop="showDetail(email)">
                查看详情
                <DoubleRightOutlined />
              </a-button>
            </template>
          </a-card>
        </a-col>
      </a-row>

      <div v-if="total > 0" class="pagination-wrapper">
        <a-pagination
          v-model:current="searchParams.current"
          v-model:page-size="searchParams.pageSize"
          :total="total"
          :page-size-options="['6', '12', '18', '24']"
          show-size-changer
          show-quick-jumper
          @change="fetchData"
          @showSizeChange="fetchData"
        />
      </div>
    </a-spin>

    <!-- 公告详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      :title="currentNotice?.subject || '公告详情'"
      :footer="null"
      width="680px"
      destroy-on-close
    >
      <template v-if="currentNotice">
        <div class="detail-meta">
          <a-tag color="blue">公告</a-tag>
          <span class="detail-time">
            <ClockCircleOutlined style="margin-right: 4px" />
            {{ dayjs(currentNotice.sendTime).format('YYYY-MM-DD HH:mm:ss') }}
          </span>
        </div>
        <a-divider style="margin: 12px 0" />
        <div class="detail-content">{{ currentNotice.txt }}</div>
      </template>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs';
import { ref, onMounted, reactive } from 'vue';
import relativeTime from 'dayjs/plugin/relativeTime';
import { ClockCircleOutlined, DoubleRightOutlined } from '@ant-design/icons-vue';
import { listNoticeByPageUsingPost } from '@/api/emailController';
import { message } from 'ant-design-vue';

dayjs.extend(relativeTime);

const loading = ref(false);
const dataList = ref<API.EmailVO[]>([]);
const total = ref(0);

const searchParams = reactive<API.PageRequest>({
  current: 1,
  pageSize: 6,
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await listNoticeByPageUsingPost({
      current: searchParams.current,
      pageSize: searchParams.pageSize,
    });
    if (res.code === 0 && res.data) {
      dataList.value = res.data.records ?? [];
      total.value = res.data.total ?? 0;
    } else {
      message.error('获取公告失败，' + res.msg);
    }
  } finally {
    loading.value = false;
  }
};

const truncateText = (text: string | undefined, maxLen: number): string => {
  if (!text) return '(无内容)';
  return text.length > maxLen ? text.slice(0, maxLen) + '...' : text;
};

// 详情弹窗
const detailVisible = ref(false);
const currentNotice = ref<API.EmailVO | null>(null);

const showDetail = (email: API.EmailVO) => {
  currentNotice.value = email;
  detailVisible.value = true;
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.email-notice {
  max-width: 1100px;
  margin: 0 auto;
  padding: 16px;
}

.notice-card {
  transition: transform 0.2s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.notice-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.notice-card :deep(.ant-card-body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-title {
  display: inline-block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}

.card-meta {
  color: #999;
  font-size: 12px;
  margin-bottom: 8px;
}

.card-time {
  display: flex;
  align-items: center;
}

.card-content {
  flex: 1;
  color: #555;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.empty-state {
  padding: 80px 0;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #999;
  font-size: 13px;
}

.detail-time {
  display: flex;
  align-items: center;
}

.detail-content {
  font-size: 15px;
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
  color: #333;
  max-height: 500px;
  overflow-y: auto;
}
</style>
