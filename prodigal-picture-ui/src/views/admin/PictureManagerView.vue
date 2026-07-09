<template>
  <div id="pictureManagerView">
    <a-flex justify="space-between">
      <h2>图片管理</h2>
      <a-space>
        <a-button type="primary" @click="doCreate" :icon="h(PlusOutlined)">创建图片</a-button>
        <a-button type="primary" ghost @click="doBatchCreate" :icon="h(PlusOutlined)">批量创建图片</a-button>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px"/>
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="关键词">
        <a-input v-model:value="searchParams.searchText" placeholder="输入关键词(名称、简介)" allow-clear/>
      </a-form-item>
      <a-form-item label="分类">
        <a-input v-model:value="searchParams.category" placeholder="输入分类" allow-clear/>
      </a-form-item>
      <a-form-item label="标签">
        <a-select v-model:value="searchParams.tags" mode="tags" placeholder="输入标签"
                  style="min-width: 180px" allow-clear/>
      </a-form-item>
      <a-form-item label="审核状态">
        <a-select v-model:value="searchParams.reviewStatus"
                  :options="PIC_REVIEW_STATUS_OPTIONS"
                  placeholder="输入审核状态"
                  style="min-width: 180px"
                  allow-clear/>
      </a-form-item>
      <!-- 按颜色搜索 -->
      <a-form-item label="颜色" name="picColor">
        <a-space>
          <color-picker v-model:value="searchParams.picColor" format="hex" @pureColorChange="onColorChange"/>
          <a-button v-if="searchParams.picColor" size="small" type="link" @click="onClearColor">清除颜色</a-button>
        </a-space>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px"/>
    <a-table
      :row-selection="rowSelection"
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
        <template v-else-if="column.dataIndex === 'id'">
          <a-tooltip placement="topLeft">
            <template #title>{{ record.id }}</template>
            {{ record.id }}
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'url'">
          <a-image :src="record.url" :width="120"/>
        </template>
        <template v-else-if="column.dataIndex === 'introduction'" >
          <a-tooltip placement="topLeft">
            <template #title>{{ record.introduction }}</template>
            {{ record.introduction }}
          </a-tooltip>
        </template>

        <template v-else-if="column.dataIndex === 'tags'">
          <a-space wrap>
            <a-tag v-for="tag in JSON.parse(record.tags||'[]')" :key="tag">
              {{ tag }}
            </a-tag>
          </a-space>
        </template>
        <template v-else-if="column.dataIndex === 'picInfo'">
          <div>格式：{{ record.picFormat }}</div>
          <div>宽度：{{ record.picWidth }}</div>
          <div>高度：{{ record.picHeight }}</div>
          <div>宽高比：{{ record.picScale }}</div>
          <div>大小：{{ (record.picSize / 1024).toFixed(2) }}KB</div>
        </template>
        <!-- 审核信息 -->
        <template v-else-if="column.dataIndex === 'reviewMessage'">
          <div>审核状态：{{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}</div>
          <div>审核信息：{{ record.reviewMessage }}</div>
          <div>审核人：{{ record.reviewerId }}</div>
        </template>


        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space wrap>
            <a-button size="small" type="primary" :icon="h(EditOutlined)" @click="doEdit(record)">
              编辑
            </a-button>
            <a-button v-if="record.reviewStatus!==PIC_REVIEW_STATUS_ENUM.PASS"
                      size="small"
                      type="primary"
                      ghost
                      :icon="h(CheckOutlined)"
                      @click="openReviewModal(record, PIC_REVIEW_STATUS_ENUM.PASS)">
              通过
            </a-button>
            <a-button v-if="record.reviewStatus!==PIC_REVIEW_STATUS_ENUM.REJECT"
                      size="small"
                      danger
                      :icon="h(StopOutlined)"
                      @click="openReviewModal(record, PIC_REVIEW_STATUS_ENUM.REJECT)">
              拒绝
            </a-button>
            <a-popconfirm okText="确定"
                          cancelText="取消"
                          title="确定删除？"
                          @confirm="doDelete(record)">
              <a-button size="small" danger :icon="h(DeleteOutlined)">
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 审核意见弹窗 -->
    <a-modal
      v-model:open="reviewModalVisible"
      :title="reviewModalTitle"
      ok-text="确定"
      cancel-text="取消"
      @ok="confirmReview"
    >
      <a-form layout="vertical">
        <a-form-item label="审核意见">
          <a-textarea
            v-model:value="reviewMessage"
            :placeholder="reviewModalPlaceholder"
            :rows="4"
            allow-clear
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import {h, computed, onMounted, reactive, ref, unref} from "vue";
import {useRouter} from 'vue-router';
import {StopOutlined, DeleteOutlined, EditOutlined, CheckOutlined, PlusOutlined} from '@ant-design/icons-vue';
import {Table, message} from "ant-design-vue";
import dayjs from "dayjs";
import {PIC_REVIEW_STATUS_ENUM, PIC_REVIEW_STATUS_MAP, PIC_REVIEW_STATUS_OPTIONS} from "@/constants/picture";
import {
  deletePictureUsingPost, doPictureReviewUsingPost,
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
    width: 150,
    ellipsis: true,
  },
  {
    title: '名称',
    dataIndex: 'name',
    width: 200,
  },
  {
    title: '图片',
    dataIndex: 'url',
    width: 200,
  },
  {
    title: '简介',
    dataIndex: 'introduction',
    width: 200,
    ellipsis: true,
  },
  {
    title: '类型',
    dataIndex: 'category',
    width: 100,
  },
  {
    title: '标签',
    dataIndex: 'tags',
    width: 100,
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
    width: 200,
  },
  {
    title: '审核信息',
    dataIndex: 'reviewMessage',
    width: 200,
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    width: 200,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 200,
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
    width: 200,
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
const searchParams = reactive<API.PictureQueryDTO>({
  current: 1,
  pageSize: 5,
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
const onColorChange = (color: string) => {
  searchParams.picColor = color
}
const onClearColor = () => {
  searchParams.picColor = undefined
  doSearch()
}
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

const router = useRouter()

//编辑数据
const doEdit = (record: API.PictureVO) => {
  router.push(`/picture/add_picture?id=${record.id}&spaceId=${record.spaceId}`)
}

const doCreate = () => {
  router.push('/picture/add_picture')
}

const doBatchCreate = () => {
  router.push('/picture/add_picture/batch')
}

// 审核弹窗状态
const reviewModalVisible = ref(false)
const reviewModalTitle = ref('')
const reviewModalPlaceholder = ref('')
const reviewMessage = ref('')
const currentRecord = ref<API.PictureVO | null>(null)
const currentReviewStatus = ref<number>(PIC_REVIEW_STATUS_ENUM.PASS)

const openReviewModal = (record: API.PictureVO, reviewStatus: number) => {
  currentRecord.value = record
  currentReviewStatus.value = reviewStatus
  reviewMessage.value = ''
  if (reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS) {
    reviewModalTitle.value = '审核通过'
    reviewModalPlaceholder.value = '选填：可输入审核通过意见'
  } else {
    reviewModalTitle.value = '审核拒绝'
    reviewModalPlaceholder.value = '请输入拒绝理由'
  }
  reviewModalVisible.value = true
}

const confirmReview = async () => {
  const record = currentRecord.value
  if (!record) return
  const res = await doPictureReviewUsingPost({
    id: record.id,
    spaceId: record.spaceId,
    reviewMessage: reviewMessage.value || undefined,
    reviewStatus: currentReviewStatus.value,
  })
  if (res.code === 0) {
    message.success("审核操作成功")
    reviewModalVisible.value = false
    fetchData()
  } else {
    message.error(res.msg || "审核操作失败")
  }
}


// 删除数据
const doDelete = async (record: API.PictureVO) => {
  if (!record.id) {
    return
  }
  const res = await deletePictureUsingPost({
    id:record.id,
    spaceId:record.spaceId
  })
  if (res.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}


//选中
const selectedRowKeys = ref<API.Picture['id'][]>([]); // Check here to configure the default column

const onSelectChange = (changableRowKeys: string[]) => {
  console.log('selectedRowKeys changed: ', changableRowKeys);
  selectedRowKeys.value = changableRowKeys;
};

const rowSelection = computed(() => {
  return {
    selectedRowKeys: unref(selectedRowKeys),
    onChange: onSelectChange,
    hideDefaultSelections: true,
    selections: [
      Table.SELECTION_ALL,
      Table.SELECTION_INVERT,
      Table.SELECTION_NONE,
      {
        key: 'odd',
        text: 'Select Odd Row',
        onSelect: changableRowKeys => {
          let newSelectedRowKeys = [];
          newSelectedRowKeys = changableRowKeys.filter((_key, index) => {
            if (index % 2 !== 0) {
              return false;
            }
            return true;
          });
          selectedRowKeys.value = newSelectedRowKeys;
        },
      },
      {
        key: 'even',
        text: 'Select Even Row',
        onSelect: changableRowKeys => {
          let newSelectedRowKeys = [];
          newSelectedRowKeys = changableRowKeys.filter((_key, index) => {
            if (index % 2 !== 0) {
              return true;
            }
            return false;
          });
          selectedRowKeys.value = newSelectedRowKeys;
        },
      },
    ],
  };
});
</script>

<style scoped>

</style>
