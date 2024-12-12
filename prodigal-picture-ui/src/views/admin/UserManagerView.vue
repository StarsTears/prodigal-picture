<template>
  <a-form layout="inline" :model="searchParams" @finish="doSearch">
    <a-form-item label="账号">
      <a-input v-model:value="searchParams.userAccount" placeholder="输入账号"/>
    </a-form-item>
    <a-form-item label="用户名">
      <a-input v-model:value="searchParams.userName" placeholder="输入用户名"/>
    </a-form-item>
    <a-form-item>
      <a-button type="primary" html-type="submit">搜索</a-button>
    </a-form-item>
  </a-form>
  <div style="margin-bottom: 16px"/>
  <a-table
    :columns="columns"
    :data-source="dataList"
    :pagination="pagination"
    :loading="loading"
    @change="doTableChange">

    <template #bodyCell="{ column, record,index }">
      <template v-if="column.title === '序号'">
        {{ (pagination.current-1) * pagination.pageSize + parseInt(index)+1}}
      </template>
      <template v-if="column.dataIndex === 'userAvatar'">
        <a-image :src="record.userAvatar" :width="50"/>
      </template>
      <template v-else-if="column.dataIndex === 'userRole'">
        <div v-if="record.userRole === 'administrator'">
          <a-tag color="purple">超级管理员</a-tag>
        </div>
        <div v-else-if="record.userRole === 'admin'">
          <a-tag color="green">管理员</a-tag>
        </div>
        <div v-else>
          <a-tag color="blue">普通用户</a-tag>
        </div>
      </template>
      <template v-else-if="column.dataIndex === 'createTime'">
        {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
      </template>
      <template v-else-if="column.dataIndex === 'updateTime'">
        {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
      </template>
      <template v-else-if="column.key === 'action'">
        <a-space wrap>
          <a-button primary @click="doEdit(record.id)">
            编辑
            <template #icon>
              <EditOutlined/>
            </template>
          </a-button>
          <a-popconfirm okText="确定" cancelText="取消" title="Sure to Confirm?" @confirm="doDelete(record.id)">
            <a-button danger>
              删除
              <template #icon>
                <DeleteOutlined/>
              </template>
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </template>
  </a-table>
</template>
<script lang="ts" setup>
import {computed, onMounted, reactive, ref, UnwrapRef} from "vue";
import {SmileOutlined, DownOutlined, DeleteOutlined, EditOutlined} from '@ant-design/icons-vue';
import {message} from "ant-design-vue";
import dayjs from "dayjs";
import {cloneDeep} from 'lodash-es';
import {deleteUserUsingDelete, listUserVoByPageUsingPost} from "@/api/systemController";

const columns = [
  {
    title: '序号',
  },
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]
// 数据
const dataList = ref<API.UserVO>([])
const total = ref(0)
const loading = ref<boolean>(false)
// 搜索条件
const searchParams = reactive<API.UserQueryDto>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'ascend'
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  const res = await listUserVoByPageUsingPost({
    ...searchParams
  })
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
    loading.value = false
  } else {
    message.error('获取数据失败，' + res.data.msg)
  }
}
// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
// 设置分页的中文语言包
const locale = {
  items_per_page: '/页',
  jump_to: '跳至',
  page: '页',
};
// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    locale: locale,
    showTotal: (total) => `共 ${total} 条`,
    pageSizeOptions: ['5', '10', '20', '30'],//可选的页面显示条数
  }
})

// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 获取数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

//编辑数据
// const dataSource = ref(dataList);
// const editableData: UnwrapRef<Record<string, dataList>> = reactive({});
// const doEdit = (key: string) => {
//   editableData[key] = cloneDeep(dataSource.value.filter(item => key === item.key)[0]);
// };
// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUserUsingDelete({id})
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}
</script>

<style scoped>

</style>
