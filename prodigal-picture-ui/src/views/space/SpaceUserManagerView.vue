<template>
  <div id="spaceUserManagerView">
    <a-flex justify="space-between">
      <h2>空间成员管理</h2>
    </a-flex>
    <div style="margin-bottom: 16px"/>
    <a-flex justify="space-between">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="空间成员角色">
          <a-select v-model:value="searchParams.spaceRole"
                    :options="SPACE_ROLE_OPTIONS"
                    placeholder="输入空间成员角色"
                    style="min-width: 180px"
                    allow-clear/>
        </a-form-item>
        <a-form-item label="空间成员ID">
          <a-input v-model:value="searchParams.id" placeholder="输入空间成员ID" allow-clear/>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-form-item>
      </a-form>

      <a-form layout="inline" :model="formData" @finish="handleSubmit">
        <a-form-item label="用户 id" name="userId">
          <a-input v-model:value="formData.userId" placeholder="请输入用户 id" allow-clear/>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">添加用户</a-button>
        </a-form-item>
      </a-form>
    </a-flex>

    <div style="margin-bottom: 16px"/>
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      :scroll="{x: 'max-content'}"
      @change="doTableChange">

      <template #bodyCell="{ column, record,index }">
        <template v-if="column.title === '序号'">
          {{ (pagination.current - 1) * pagination.pageSize + parseInt(index) + 1 }}
        </template>

        <template v-if="column.dataIndex === 'userInfo'">
          <a-space>
<!--            <a-avatar :src="record.user?.userAvatar"/>-->
            <a-avatar v-if="record.user?.userAvatar" :src="record.user?.userAvatar"/>
            <a-avatar v-if="!record.user?.userAvatar" style="color: #f56a00; background-color: #fde3cf">{{record.user?.userName.charAt(0)}}</a-avatar>
            {{ record.user?.userName }}
          </a-space>
        </template>

        <template v-if="column.dataIndex === 'spaceRole'">
          <a-select v-model:value="record.spaceRole"
                    :options="SPACE_ROLE_OPTIONS"
                    @change="(value)=>editSpaceRole(value,record)"/>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space wrap>
            <a-popconfirm okText="确定"
                          cancelText="取消"
                          title="Sure to delete?"
                          @confirm="doDelete(record.id)">
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
  </div>
</template>

<script setup lang="ts">
import {h, computed, onMounted, reactive, ref} from "vue";
import {DeleteOutlined, EditOutlined, BarChartOutlined} from '@ant-design/icons-vue';
import {message} from "ant-design-vue";
import dayjs from "dayjs";
import {SPACE_ROLE_OPTIONS, SPACE_LEVEL_OPTIONS} from "@/constants/space"
import {
  addSpaceUserUsingPost,
  deleteSpaceUserUsingPost,
  editSpaceUserUsingPost,
  listSpaceUserVoByPageUsingPost
} from "@/api/spaceUserController";

const columns = [
  {
    title: '序号',
    width: 50,
    fixed: 'left'
  },
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
    fixed: 'left'
  },
  {
    title: '用户',
    dataIndex: 'userInfo',
  },
  {
    title: '角色',
    dataIndex: 'spaceRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

interface Props {
  id: string
}

const props = defineProps<Props>()

// 数据
const dataList = ref([])
const total = ref(0)
// 搜索条件
const searchParams = reactive<API.SpaceUserQueryDto>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend'
})

// 获取数据
const fetchData = async () => {
  const spaceId = props.id
  if (!spaceId) {
    return
  }
  const res = await listSpaceUserVoByPageUsingPost({
    ...searchParams,
    spaceId: spaceId
  })
  if (res.data) {
    dataList.value = res.data.records ?? []
    total.value = res.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.msg)
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
//修改角色
const editSpaceRole = async (value, record) => {
  const res = await editSpaceUserUsingPost({
    id: record.id,
    spaceRole: value,
  })
  if (res.code === 0) {
    message.success('修改成功')
  } else {
    message.error('修改失败，' + res.msg)
  }
}


//删除
const doDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deleteSpaceUserUsingPost({id})
  if (res.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 添加用户
const formData = reactive<API.SpaceUserAddDto>({})

const handleSubmit = async () => {
  const spaceId = props.id
  if (!spaceId) {
    return
  }
  const res = await addSpaceUserUsingPost({
    spaceId,
    ...formData,
  })
  if (res.code === 0) {
    message.success('添加成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('添加失败，' + res.msg)
  }
}


</script>

<style scoped>

</style>
