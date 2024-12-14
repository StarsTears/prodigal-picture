<template>
  <a-form layout="inline" :model="searchParams" @finish="doSearch">
    <a-form-item label="关键词">
      <a-input v-model:value="searchParams.searchText" placeholder="输入关键词(名称、简介)" allow-clear/>
    </a-form-item>
    <a-form-item label="类型">
      <a-input v-model:value="searchParams.category" placeholder="输入类型" allow-clear/>
    </a-form-item>
    <a-form-item label="标签">
        <a-select v-model:value="searchParams.tags" mode="tags" placeholder="输入标签"
        style="min-width: 180px" allow-clear/>
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
    :scroll="{x: 'max-content'}"
    @change="doTableChange">

    <template #bodyCell="{ column, record,index }">
      <template v-if="column.title === '序号'">
        {{ (pagination.current - 1) * pagination.pageSize + parseInt(index) + 1 }}
      </template>

      <template v-if="column.dataIndex === 'url'">
        <a-image :src="record.url" :width="120"/>
      </template>
      <template v-if="column.dataIndex === 'tags'">
        <a-space wrap>
          <a-tag v-for="tag in JSON.parse(record.tags||'[]')" :key="tag">
            {{ tag }}
          </a-tag>
        </a-space>
      </template>
      <template v-if="column.dataIndex === 'picInfo'">
        <div>格式：{{ record.picFormat }}</div>
        <div>宽度：{{ record.picWidth }}</div>
        <div>高度：{{ record.picHeight }}</div>
        <div>宽高比：{{ record.picScale }}</div>
        <div>大小：{{ (record.picSize / 1024).toFixed(2) }}KB</div>
      </template>


      <template v-if="column.dataIndex === 'createTime'">
        {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
      </template>
      <template v-if="column.dataIndex === 'editTime'">
        {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
      </template>
      <template v-if="column.key === 'action'">
        <a-space wrap>
          <a-button type="link" :href="`/picture/add_picture?id=${record.id}`" target="_blank">
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
import {
  deletePictureUsingPost,
  listPictureByPageUsingPost,
  listPictureVoByPageUsingPost
} from "@/api/pictureController";

const columns = [
  {
    title: '序号',
  },
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '图片',
    dataIndex: 'url',
  },
  {
    title: '名称',
    dataIndex: 'name',
  },
  {
    title: '简介',
    dataIndex: 'introduction',
  },
  {
    title: '类型',
    dataIndex: 'category',
  },
  {
    title: '标签',
    dataIndex: 'tags',
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
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
const dataList = ref<API.Picture>([])
const total = ref(0)
const loading = ref<boolean>(true)
// 搜索条件
const searchParams = reactive<API.PictureQueryDto>({
  current: 1,
  pageSize: 2,
  sortField: 'createTime',
  sortOrder: 'descend'
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  const res = await listPictureByPageUsingPost({
    ...searchParams
  })
  if (res.data) {
    dataList.value = res.data.records ?? []
    total.value = res.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.msg)
  }
  loading.value = false
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
  const res = await deletePictureUsingPost({id})
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
