<template>
  <div>
    <a-flex justify="space-between">
      <h2>邮件管理</h2>
      <a-space>
        <a-button type="primary" @click="doAdd">+ 新增邮件</a-button>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px"/>
    <a-form layout="inline" :model="searchParams" @finish="doSearch" autocomplete="off">
      <input type="text" style="position:absolute;left:-9999px" tabindex="-1" autocomplete="off" name="decoy"/>
      <a-form-item label="接收人">
        <a-input v-model:value="searchParams.to" placeholder="输入邮箱" autocomplete="nope"/>
      </a-form-item>
      <a-form-item label="邮件主题">
        <a-input v-model:value="searchParams.subject" placeholder="输入邮件主题" autocomplete="nope"/>
      </a-form-item>
      <a-form-item label="邮件内容">
        <a-input v-model:value="searchParams.txt" placeholder="输入邮件内容" autocomplete="nope"/>
      </a-form-item>
      <a-form-item label="邮件类型">
        <a-select v-model:value="searchParams.type"
                  :options="EMAIL_TYPE_OPTIONS"
                  placeholder="输入邮件类型"
                  style="min-width: 180px"
                  allow-clear/>
      </a-form-item>
      <a-form-item label="邮件状态">
        <a-select v-model:value="searchParams.status"
                  :options="EMAIL_STATUS_OPTIONS"
                  placeholder="输入邮件状态"
                  style="min-width: 180px"
                  allow-clear/>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px"/>
    <a-table
      :columns="columns"
      :data-source="dataList"
      :loading="loading"
      :scroll="{ x: 1200 }"
      @change="doTableChange">

      <template #bodyCell="{ column, record, index, text }">

        <template v-if="column.title === '序号'">
          {{ index + 1 }}
        </template>

        <template v-else-if="column.dataIndex === 'subject'">
          <a-tooltip placement="topLeft">
            <template #title>{{ record.subject }}</template>
            <span>{{ record.subject || '-' }}</span>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'type'">
          <a-tag :color="record.type === 0 ? 'blue' : 'orange'">{{ EMAIL_TYPE_MAP[record.type] }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'status'">
          <a-tag :color="statusColorMap[record.status]">{{ EMAIL_STATUS_MAP[record.status] }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'to'">
          <a-tooltip placement="topLeft">
            <template #title>{{ record.to }}</template>
            <span>{{ record.to || '-' }}</span>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'receiveUserVO'">
          <span v-if="record.to && record.to.includes(',')">多人</span>
          <span v-else>{{ record.receiveUserVO?.userName || '-' }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'txt'">
          <a-tooltip placement="topLeft">
            <template #title>{{ record.txt }}</template>
            <span>{{ record.txt || '-' }}</span>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'sendTime'">
          {{ record.sendTime != null ? dayjs(record.sendTime).format('YYYY-MM-DD HH:mm') : '-' }}
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ record.createTime != null ? dayjs(record.createTime).format('YYYY-MM-DD HH:mm') : '-' }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button size="small" :icon="h(EyeOutlined)" @click="doView(record)">查看</a-button>
            <a-button size="small" type="primary" :icon="h(EditOutlined)" :disabled="record.status === 2" @click="doEdit(record)">
              编辑
            </a-button>
            <a-button v-if="record.status !== 2" size="small" type="primary" ghost :icon="h(SendOutlined)" @click="doSend(record.id)">
              发送
            </a-button>
            <a-popconfirm okText="确定" cancelText="取消" title="确定删除？" @confirm="doDelete(record.id)">
              <a-button size="small" danger :icon="h(DeleteOutlined)">
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-drawer
      title="邮件详情"
      :open="drawerOpen"
      :width="520"
      @close="drawerOpen = false">
      <template v-if="viewingEmail">
        <a-descriptions :column="1" bordered size="small" style="margin-bottom: 16px">
          <a-descriptions-item label="主题">{{ viewingEmail.subject || '-' }}</a-descriptions-item>
          <a-descriptions-item label="邮件类型">
            <a-tag :color="viewingEmail.type === 0 ? 'blue' : 'orange'">{{ EMAIL_TYPE_MAP[viewingEmail.type] }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="邮件状态">
            <a-tag :color="statusColorMap[viewingEmail.status]">{{ EMAIL_STATUS_MAP[viewingEmail.status] }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="收件人">{{ viewingEmail.to || '全部用户' }}</a-descriptions-item>
          <a-descriptions-item label="发送时间">{{ viewingEmail.sendTime ? dayjs(viewingEmail.sendTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ viewingEmail.createTime ? dayjs(viewingEmail.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}</a-descriptions-item>
          <a-descriptions-item label="更新时间">{{ viewingEmail.updateTime ? dayjs(viewingEmail.updateTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}</a-descriptions-item>
        </a-descriptions>
        <div style="margin-bottom: 8px;font-weight:500;color:var(--text-secondary)">邮件内容</div>
        <div v-if="viewingEmail.html" v-html="viewingEmail.txt" style="background:var(--bg-image-placeholder);padding:16px;border-radius:6px;border:1px solid var(--border-color);max-height:400px;overflow:auto;"></div>
        <div v-else style="background:var(--bg-image-placeholder);padding:16px;border-radius:6px;border:1px solid var(--border-color);max-height:400px;overflow:auto;white-space:pre-wrap;">{{ viewingEmail.txt || '-' }}</div>
      </template>
    </a-drawer>
  </div>
  <EmailAddModal ref="emailFormModalRef" :onReload="fetchData"/>
</template>
<script lang="ts" setup>
import {h, onMounted, onUnmounted, reactive, ref} from "vue";
import {
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  SendOutlined
} from '@ant-design/icons-vue';
import type {TableColumnsType} from 'ant-design-vue';
import {message} from "ant-design-vue";
import dayjs from "dayjs";
import {
  deleteEmailUsingPost,
  listEmailByPageUsingPost,
  sendEmailByIdUsingPost
} from "@/api/emailController";
import EmailAddModal from "@/views/email/EmailAddModal.vue";
import {
  EMAIL_STATUS_MAP,
  EMAIL_STATUS_OPTIONS,
  EMAIL_TYPE_MAP,
  EMAIL_TYPE_OPTIONS
} from "@/constants/email";
import { useSSE } from "@/composables/useSSE";

const statusColorMap: Record<number, string> = {
  0: 'default',
  1: 'processing',
  2: 'success',
}

const columns: TableColumnsType = [
  {
    title: '序号',
    width: 60,
  },
  {
    title: '主题',
    dataIndex: 'subject',
    width: 180,
    ellipsis: true,
  },
  {
    title: '邮件类型',
    dataIndex: 'type',
    width: 90,
  },
  {
    title: '邮件状态',
    dataIndex: 'status',
    width: 90,
  },
  {
    title: '收件人邮箱',
    dataIndex: 'to',
    width: 150,
    ellipsis: true,
  },
  {
    title: '收件人',
    dataIndex: 'receiveUserVO',
    width: 100,
    ellipsis: true,
  },
  {
    title: '内容',
    dataIndex: 'txt',
    width: 200,
    ellipsis: true,
  },
  {
    title: '发送时间',
    dataIndex: 'sendTime',
    width: 150,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 150,
  },
  {
    title: '操作',
    key: 'action',
    width: 320,
  },
]

// 数据
const dataList = ref<API.EmailVO[]>([])
const loading = ref<boolean>(true)
// 搜索条件
const searchParams = reactive<API.EmailQueryDTO>({})

// 获取数据
const fetchData = async () => {
  loading.value = true
  const res = await listEmailByPageUsingPost({
    ...searchParams
  })
  if (res.code === 0 && res.data) {
    dataList.value = res.data.records ?? []
  } else {
    message.error('获取数据失败，' + res.msg)
  }
  loading.value = false
}

onMounted(() => {
  fetchData()
})

// SSE 监听邮件发送完成，自动刷新列表
const { onEmailSent } = useSSE()
const unsubSSE = onEmailSent(() => {
  fetchData()
})
onUnmounted(() => {
  unsubSSE()
})

const doTableChange = () => {
  fetchData()
}

const doSearch = () => {
  fetchData()
}

//----------------------------------新增/编辑邮件-----------------------
const emailFormModalRef = ref(false)

const doAdd = () => {
  if (emailFormModalRef.value) {
    emailFormModalRef.value.openModal()
  }
}

const doEdit = (record: API.EmailVO) => {
  if (emailFormModalRef.value) {
    emailFormModalRef.value.openModal(record)
  }
}

//---------------------------------查看详情------------------------
const drawerOpen = ref(false)
const viewingEmail = ref<API.EmailVO>()

const doView = (record: API.EmailVO) => {
  viewingEmail.value = record
  drawerOpen.value = true
}

//---------------------------------发送邮件------------------------
const doSend = async (id: string) => {
  const res = await sendEmailByIdUsingPost({
    emailId: id
  })
  if (res.code === 0) {
    message.success('邮件发送成功')
    fetchData()
  } else {
    message.error('邮件发送失败，' + res.msg)
  }
};

//---------------------------------删除数据------------------------
const doDelete = async (id: string) => {
  const res = await deleteEmailUsingPost({emailId: id})
  if (res.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败，' + res.msg)
  }
}
</script>

<style scoped>

</style>
