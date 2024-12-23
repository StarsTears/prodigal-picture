<template>
<div id="spaceManagerView">
  <a-flex justify="space-between">
    <h2>空间管理</h2>
    <a-space>
      <a-button type="primary" href="/space/add_space" target="_blank">+ 创建空间</a-button>
    </a-space>
  </a-flex>
  <div style="margin-bottom: 16px"/>
  <a-form layout="inline" :model="searchParams" @finish="doSearch">
    <a-form-item label="空间名称">
      <a-input v-model:value="searchParams.spaceName" placeholder="输入类型" allow-clear/>
    </a-form-item>
    <a-form-item label="空间级别">
      <a-select v-model:value="searchParams.spaceLevel"
                :options="SPACE_LEVEL_OPTIONS"
                placeholder="输入空间级别"
                style="min-width: 180px"
                allow-clear/>
    </a-form-item>
    <a-form-item label="用户ID">
      <a-input v-model:value="searchParams.userId" placeholder="输入用户ID" allow-clear/>
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
    :scroll="{x: 'max-content'}"
    @change="doTableChange">

    <template #bodyCell="{ column, record,index }">
      <template v-if="column.title === '序号'">
        {{ (pagination.current - 1) * pagination.pageSize + parseInt(index) + 1 }}
      </template>
      <template v-if="column.dataIndex ==='spaceLevel'">
        {{ SPACE_LEVEL_MAP[record.spaceLevel] }}
      </template>
      <template v-if="column.dataIndex === 'spaceUseInfo'">
        <div>大小：{{ formatSize(record.totalSize) }} / {{ formatSize(record.maxSize) }}</div>
        <div>数量：{{ formatSize(record.totalCount) }} / {{ formatSize(record.maxCount) }}</div>
      </template>

      <template v-if="column.dataIndex === 'createTime'">
        {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
      </template>
      <template v-if="column.dataIndex === 'editTime'">
        {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
      </template>
      <template v-if="column.key === 'action'">
        <a-space wrap>
          <a-button type="primary"
                    :icon="h(EditOutlined)"
                    :href="`/space/add_space?id=${record.id}`"
                    target="_blank">
            编辑
          </a-button>
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
import {h,computed, onMounted, reactive, ref} from "vue";
import {DeleteOutlined, EditOutlined} from '@ant-design/icons-vue';
import {message} from "ant-design-vue";
import dayjs from "dayjs";
import {formatSize} from "@/utils/index";
import {SPACE_LEVEL_MAP , SPACE_LEVEL_OPTIONS} from "@/constants/space"
import {deleteSpaceUsingPost, listSpaceByPageUsingPost} from "@/api/spaceController";

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
  },
  {
    title: '空间名称',
    dataIndex: 'spaceName',
  },
  {
    title: '空间级别',
    dataIndex: 'spaceLevel',
  },
  {
    title: '使用情况',
    dataIndex: 'spaceUseInfo',
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]
// 数据
const dataList = ref<API.Space>([])
const total = ref(0)
// 搜索条件
const searchParams = reactive<API.SpaceQueryDto>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend'
})

// 获取数据
const fetchData = async () => {
  const res = await listSpaceByPageUsingPost({
    ...searchParams
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
const doDelete = async (id: number) => {
  if (!id){
    return
  }
  const res = await deleteSpaceUsingPost({id})
  if (res.code === 0) {
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
