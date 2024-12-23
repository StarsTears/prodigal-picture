<template>
<div id="spaceDetailView">
  <!-- 空间信息 -->
  <a-flex justify="space-between">
    <h2>{{ space.spaceName }}（私有空间）</h2>
    <a-space size="middle">
<!--      <a-button type="primary" @click="doShow">-->
<!--        + 创建图片（模态框）-->
<!--      </a-button>-->
      <a-button type="primary" :href="`/picture/add_picture?spaceId=${id}`" target="_blank">
        + 创建图片
      </a-button>
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

  <UploadPictureModelView  :open="showModel" :spaceId="id" />
</div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import {DeleteOutlined, EditOutlined} from '@ant-design/icons-vue';
import {listPictureVoByPageCacheUsingPost} from "@/api/pictureController";
import {getSpaceVoByIdUsingGet} from "@/api/spaceController";
import {message} from "ant-design-vue";
import {formatSize} from '@/utils/index'
import PictureList from "@/components/PictureList.vue";
import UploadPictureModelView from "@/components/UploadPictureModelView.vue";

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
const searchParams = reactive<API.PictureQueryDto>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend'
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  //转换搜索参数
  const params = {
    ...searchParams,
    spaceId: props.id
  }

  const res = await listPictureVoByPageCacheUsingPost( params)
  if (res.data) {
    dataList.value = res.data.records ?? []
    total.value = res.data.total
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
const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

// 获取数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

const showModel = ref<boolean>(false)
const doShow = () => {
  showModel.value = true
}
</script>

<style scoped>

</style>
