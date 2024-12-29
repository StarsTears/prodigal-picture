<template>
<div id="spaceDetailView">
  <!-- 空间信息 -->
  <a-flex justify="space-between">
    <h2>{{ space.spaceName }}（私有空间）</h2>
    <a-space size="middle">
      <a-button danger :icon="h(EditOutlined)" :href="`/space/add_space?id=${space.id}`"> 编辑空间信息</a-button>
      <a-button type="primary" ghost :icon="h(BarChartOutlined)" :href="`/space/analyze?spaceId=${id}`">空间分析 </a-button>

      <a-button type="primary" :href="`/picture/add_picture?spaceId=${id}`" target="_blank">+ 创建图片</a-button>
      <a-button :icon="h(EditOutlined)" @click="doBatchEdit"> 批量编辑</a-button>
      <a-tooltip
        :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
      >
        <a-progress
          type="circle"
          :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
          :size="42"
        />
      </a-tooltip>
    </a-space>
  </a-flex>
  <!-- 搜索表单 -->
  <PictureSearchForm :onSearch="onSearch"/>
  <div style="margin-bottom: 16px"/>
  <!-- 图片列表 -->
  <PictureList :dataList="dataList" :loading="loading" showOp :onReload = "fetchData"/>
  <a-pagination
    style="text-align: right"
    v-model:current="searchParams.current"
    v-model:pageSize="searchParams.pageSize"
    :total="total"
    :show-total="() => `图片总数 ${total} / ${space.maxCount}`"
    @change="onPageChange"
  />
  <BatchEditPictureModal ref="batchEditPictureModalRef" :spaceId="id" :pictureList="dataList" :onSuccess="onBatchEditPictureSuccess"/>
</div>
</template>

<script setup lang="ts">
import {h,onMounted, reactive, ref} from "vue";
import {DeleteOutlined, EditOutlined,BarChartOutlined} from '@ant-design/icons-vue';
import {listPictureVoByPageCacheUsingPost, listPictureVoByPageUsingPost} from "@/api/pictureController";
import {getSpaceVoByIdUsingGet} from "@/api/spaceController";
import {message} from "ant-design-vue";
import {formatSize} from '@/utils/index'
import PictureList from "@/components/PictureList.vue";
import PictureSearchForm from "@/components/PictureSearchForm.vue";
import UploadPictureModelView from "@/components/UploadPictureModelView.vue";
import BatchEditPictureModal from "@/components/BatchEditPictureModal.vue";

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const space = ref<API.SpaceVO>({})
// -----------------------------------------------------获取空间详情--------------------------------
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({id: props.id})
    if (res.data) {
      space.value = res.data
    } else {
      message.error('获取图片详情数据失败，' + res.msg)
    }
  } catch (error) {
    message.error('获取图片详情数据失败，' + error.msg)
  }
}
// 页面加载时请求一次
onMounted(() => {
  fetchSpaceDetail()
})
//-------------------------------------------------获取图片列表--------------------------------
// 数据
const dataList = ref<API.Picture[]>([])
const loading = ref<boolean>(true)
const total = ref(0)
// 搜索条件
const searchParams = ref<API.PictureQueryDto>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend'
})
const onSearch = (newSearchParams: API.PictureQueryDto) => {
  searchParams.value = {
          ...searchParams.value,
          ...newSearchParams,
          current:1
  }
  fetchData()
}
// ------------------------------------------获取数据------------------------------------------
const fetchData = async () => {
  loading.value = true
  //转换搜索参数
  const params = {
    ...searchParams.value,
    spaceId: props.id
  }
  console.log("获取图片分页："+JSON.stringify(params.value))
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data) {
    dataList.value = res.data.records ?? []
    total.value = res.data.total??0
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
const onPageChange = (page, pageSize) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}


//----------------------------------批量修改当前页面数据的分类、标签弹窗------------------------
const batchEditPictureModalRef = ref<boolean>(false)
const onBatchEditPictureSuccess = () => {
  fetchData()
}
// 打开批量编辑弹窗
const doBatchEdit = () => {
  if (batchEditPictureModalRef.value) {
    batchEditPictureModalRef.value.openModal()
  }
}

</script>

<style scoped>

</style>
