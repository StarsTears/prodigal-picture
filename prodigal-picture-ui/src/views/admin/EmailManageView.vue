<template>
  <div>
    <a-flex justify="space-between">
      <h2>邮件管理</h2>
      <a-space>
        <a-button type="primary" @click="doAdd">+ 新增邮件</a-button>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px"/>
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
      :scroll="{ x: 1500, y: 1000 }"
      @change="doTableChange">

      <template #bodyCell="{ column, record,index,text}">

        <template v-if="column.title === '序号'">
          {{ index + 1 }}
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
<!--        <template v-else-if="column.dataIndex === 'txt'">-->
<!--          <a-tooltip placement="topLeft">-->
<!--            <template #title>{{ record.txt }}</template>-->
<!--            {{ record.txt }}-->
<!--          </a-tooltip>-->
        <!--可编辑字段-->
        <teleplate v-if="['to','subject', 'txt', 'type'].includes(column.dataIndex)">
          <div>
            <a-input
              v-if="editableData[record.id] && column.dataIndex !== 'type'"
              v-model:value="editableData[record.id][column.dataIndex]"
              style="margin: -5px 0"
            />
            <template v-else-if="editableData[record.id] && column.dataIndex === 'type'">
              <a-select v-model:value="editableData[record.id][column.dataIndex]"
                        :options="EMAIL_TYPE_OPTIONS"
                        placeholder="输入邮件类型"
                        style="margin: -5px 0"
                        allow-clear/>
            </template>
            <template v-else>
              <template v-if="column.dataIndex === 'type'">
                {{ EMAIL_TYPE_MAP[record.type] }}
              </template>
              <template v-else>
                {{ text }}
              </template>

            </template>
          </div>
        </teleplate>
        <template v-else-if="column.dataIndex === 'receiveUserVO'">
          <a-tooltip placement="topLeft">
            <template #title>
              {{ record.receiveUserVO != null ? record.receiveUserVO.userName + ':' + record.receiveUserVO.id : "-" }}
            </template>
            {{ record.receiveUserVO != null ? record.receiveUserVO.userName : '-' }}
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'type'">
          {{ EMAIL_TYPE_MAP[record.type] }}
        </template>
        <template v-else-if="column.dataIndex === 'status'">
          {{ EMAIL_STATUS_MAP[record.status] }}
        </template>

        <template v-else-if="column.dataIndex === 'sendTime'">
          {{ record.sendTime != null ? dayjs(record?.sendTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </template>
        <template v-else-if="column.dataIndex === 'editTime'">
          {{ record.editTime != null ? dayjs(record?.editTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ record.createTime != null ? dayjs(record?.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </template>
        <template v-else-if="column.dataIndex === 'updateTime'">
          {{ record.updateTime != null ? dayjs(record?.updateTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button v-if="editableData[record.id]"
                      default
                      :icon="h(SaveOutlined)"
                      @click="doSave(record.id)">
              保存
            </a-button>
            <a-button v-if="editableData[record.id]"
                      type="dashed"
                      @click="doCancel(record.id)"
                      :icon="h(UndoOutlined)">

              取消
            </a-button>
            <a-button v-if="record.status !== 2 && !editableData[record.id]"
                      type="primary"
                      :icon="h(EditOutlined)"
                      @click="doEdit(record.id)">
              编辑
            </a-button>
            <a-button v-if="record.status === 0 && !editableData[record.id]"
                      type="primary"
                      ghost
                      :icon="h(CheckOutlined)"
                      @click="doSubmit(record)">
              提交
            </a-button>
            <a-button v-if="record.status===1 && !editableData[record.id]"
                      type="primary"
                      ghost
                      :icon="h(SendOutlined)"
                      @click="doSend(record.id)">
              发送
            </a-button>

            <a-popconfirm v-if="!editableData[record.id]"
                          okText="确定" cancelText="取消"
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
  <EmailAddModal ref="addEmailModalRef" :onReload="fetchData"/>
</template>
<script lang="ts" setup>
import {h, computed, onMounted, reactive, ref, UnwrapRef} from "vue";
import {
  UndoOutlined,
  DownOutlined,
  DeleteOutlined,
  EditOutlined,
  SaveOutlined,
  SendOutlined,
  CheckOutlined
} from '@ant-design/icons-vue';
import type {TableColumnsType} from 'ant-design-vue';
import {message} from "ant-design-vue";
import dayjs from "dayjs";
import {cloneDeep} from 'lodash-es';
import {
  deleteEmailUsingPost,
  listEmailByPageUsingPost, sendEmailByIdUsingPost,
  updateEmailUsingPost
} from "@/api/emailController";
import EmailAddModal from "@/views/email/EmailAddModal.vue";
import {
  EMAIL_STATUS_ENUM,
  EMAIL_STATUS_MAP,
  EMAIL_STATUS_OPTIONS,
  EMAIL_TYPE_MAP,
  EMAIL_TYPE_OPTIONS
} from "@/constants/email";
import EmailDto = API.EmailDto;

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
    title: '邮件类型',
    dataIndex: 'type',
    width: 150,
  },
  {
    title: '邮件状态',
    dataIndex: 'status',
    width: 150,
  },
  {
    title: '接收人',
    dataIndex: 'receiveUserVO',
    width: 150,
    ellipsis: true,
  },
  {
    title: '发送时间',
    dataIndex: 'sendTime',
    width: 180
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    width: 180
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180
  },
  // {
  //   title: '更新时间',
  //   dataIndex: 'updateTime',
  //   width: 180
  // },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 300,
  },
]

// 数据
const dataList = ref<API.EmailVO[]>([])
const loading = ref<boolean>(true)
// 搜索条件
const searchParams = reactive<API.EmailQueryDto>({})

// 获取数据
const fetchData = async () => {
  loading.value = true
  const res = await listEmailByPageUsingPost({
    ...searchParams
  })
  console.log(res)
  if (res.code === 0 && res.data) {
    dataList.value = res.data.records ?? []
  } else {
    message.error('获取数据失败，' + res.msg)
  }
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
const editableData: UnwrapRef<Record<string, dataList.value>> = reactive({});
const doEdit = (key: string) => {
  editableData[key] = cloneDeep(dataList.value.filter(item => key === item.id)[0]);
};

const doSave = async (key: string) => {
  console.log("用户管理-保存：", editableData[key])
  Object.assign(dataList.value.filter(item => key === item.id)[0], editableData[key]);

  const res = await updateEmailUsingPost({
    ...editableData[key]
  })
  if (res.code === 0 && res.data) {
    message.success('邮件修改成功')
    fetchData();
  } else {
    message.error('邮件修改失败,' + res.msg)
  }
  delete editableData[key];
};
const doCancel = (key: string) => {
  delete editableData[key];
};
//---------------------------------提交数据------------------------
const doSubmit = async (email: EmailDto) => {
  const res = await updateEmailUsingPost({
    ...email,
    status: EMAIL_STATUS_ENUM.PASS
  })
  if (res.code === 0) {
    message.success('提交成功')
    fetchData()
  } else {
    message.error('提交失败，' + res.msg)
  }
};
//---------------------------------发送邮件------------------------
const doSend = async (id: number) => {
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
