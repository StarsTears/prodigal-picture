<template>
  <div>
    <a-flex justify="space-between">
      <h2>邮件管理</h2>
      <a-space>
        <a-button type="primary" @click="doAdd">+ 新增邮件</a-button>
      </a-space>
    </a-flex>
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="接收人">
        <a-input v-model:value="searchParams.to" placeholder="输入邮箱"/>
      </a-form-item>
      <a-form-item label="邮件主题">
        <a-input v-model:value="searchParams.subject" placeholder="输入邮件主题"/>
      </a-form-item>
      <a-form-item label="邮件内容">
        <a-input v-model:value="searchParams.txt" placeholder="输入邮件内容"/>
      </a-form-item>
      <!--    <a-form-item label="邮件状态">-->
      <!--      <a-select v-model:value="searchParams.userRole"-->
      <!--                :options="USER_ROLE_OPTIONS"-->
      <!--                placeholder="输入审核状态"-->
      <!--                style="min-width: 180px"-->
      <!--                allow-clear/>-->
      <!--    </a-form-item>-->
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px"/>
    <a-table
      :columns="columns"
      :data-source="dataList"
      :loading="loading"
      :scroll="{ x: 1500, y: 1000 }"
      @change="doTableChange">

      <template #bodyCell="{ column, record,index}">

        <template v-if="column.title === '序号'">
          {{ index }}
        </template>

        <template v-else-if="column.dataIndex === 'id'">
          <a-tooltip placement="topLeft">
            <template #title>{{ record.id }}</template>
            {{ record.id }}
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'receiveUserId'">
          <a-tooltip placement="topLeft">
            <template #title>{{ record.receiveUserId }}</template>
            {{ record.receiveUserId }}
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'txt'" copyable>
          <a-tooltip placement="topLeft">
            <template #title>{{ record.txt }}</template>
            {{ record.txt }}
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'sendTime'">
          {{ dayjs(record?.sendTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'updateTime'">
          {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button type="primary"
                      :icon="h(EditOutlined)"
                      @click="doEdit(record.id)">
              编辑
            </a-button>
            <a-popconfirm okText="确定" cancelText="取消"
                          title="Sure to Confirm?"
                          @confirm="doDelete(record.id)">
              <a-button danger :icon="h(DeleteOutlined)">
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
  <EmailAddModal ref="addEmailModalRef"/>
</template>
<script lang="ts" setup>
import {h, computed, onMounted, reactive, ref, UnwrapRef} from "vue";
import {UndoOutlined, DownOutlined, DeleteOutlined, EditOutlined, SaveOutlined} from '@ant-design/icons-vue';
import type {TableColumnsType} from 'ant-design-vue';
import {message} from "ant-design-vue";
import dayjs from "dayjs";
import {deleteEmailUsingPost, listEmailUsingPost} from "@/api/emailController";
import EmailAddModal from "@/views/email/EmailAddModal.vue";

const columns: TableColumnsType = [
  {
    title: '序号',
    width: 50,
    fixed: 'left'
  },
  {
    title: 'id',
    dataIndex: 'id',
    width: 150,
    fixed: 'left',
    ellipsis: true, // 宽度不够会自动折行，但是鼠标放上去会显示"ellipsis"
  }, {
    title: '接收人邮箱',
    dataIndex: 'to',
    width: 200,
    fixed: 'left',

  },
  {
    title: '主题',
    dataIndex: 'subject',
    width: 150,

  },
  {
    title: '内容',
    dataIndex: 'txt',
    width: 250,
    ellipsis: true,
  },
  {
    title: '接收人Id',
    dataIndex: 'receiveUserId',
    width: 150,
    ellipsis: true,
  },
  {
    title: '发送时间',
    dataIndex: 'sendTime',
    width: 180
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
    width: 180
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 250,
  },
]
const emailForm = reactive<API.Email>({})
// 数据
const dataList = ref<API.Email[]>([])
const loading = ref<boolean>(true)
// 搜索条件
const searchParams = reactive<API.QueryEmailDto>({})

// 获取数据
const fetchData = async () => {
  loading.value = true
  const res = await listEmailUsingPost({
    ...searchParams
  })
  if (res.data) {
    dataList.value = res.data ?? []
  } else {
    message.error('获取数据失败，' + res.msg)
  }
  console.log(dataList.value)
  loading.value = false
}
// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 表格变化处理
const doTableChange = () => {
  fetchData()
}

// 获取数据
const doSearch = () => {
  fetchData()
}
//----------------------------------新增邮件-----------------------
// 分享弹窗引用
const addEmailModalRef = ref(false)
// 分享
const doAdd = (e: Event) => {
  e.stopPropagation()
  if (addEmailModalRef.value) {
    addEmailModalRef.value.openModal()
  }
}
//---------------------------------编辑数据------------------------
const doEdit = (id: number) => {
  console.log(id)
}
//---------------------------------删除数据------------------------
const doDelete = async (id: number) => {
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
