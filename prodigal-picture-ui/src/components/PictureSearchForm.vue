<template>
  <div class="picture-search-form">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="关键词" name="searchText">
        <a-input
          v-model:value="searchParams.searchText"
          placeholder="从名称和简介搜索"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="分类" name="category">
        <a-auto-complete
          v-model:value="searchParams.category"
          style="min-width: 180px"
          :options="categoryOptions"
          placeholder="请输入分类"
          allowClear
        />
      </a-form-item>
      <a-form-item label="标签" name="tags">
        <a-select
          v-model:value="searchParams.tags"
          style="min-width: 180px"
          :options="tagOptions"
          mode="tags"
          placeholder="请输入标签"
          allowClear
        />
      </a-form-item>
      <a-form-item label="日期" name="">
        <a-range-picker
          style="width: 400px"
          show-time
          v-model:value="dateRange"
          :placeholder="['编辑开始日期', '编辑结束时间']"
          format="YYYY/MM/DD HH:mm:ss"
          :presets="rangePresets"
          @change="onRangeChange"
        />
      </a-form-item>
      <!-- 按颜色搜索 -->
      <a-form-item label="颜色" name="picColor">
        <color-picker v-model:value="searchParams.picColor" format="hex" @pureColorChange="onColorChange"/>
      </a-form-item>
      <a-collapse v-model:active-key(v-model)="activeKey" ghost class="custom-collapse">
        <a-collapse-panel key="1">
          <a-form layout="inline" :model="searchParams">
            <a-form-item label="名称" name="name">
              <a-input v-model:value="searchParams.name" placeholder="请输入名称" allow-clear/>
            </a-form-item>
            <a-form-item label="简介" name="introduction">
              <a-input v-model:value="searchParams.introduction" placeholder="请输入简介" allow-clear/>
            </a-form-item>
            <a-form-item label="宽度" name="picWidth">
              <a-input-number v-model:value="searchParams.picWidth"/>
            </a-form-item>
            <a-form-item label="高度" name="picHeight">
              <a-input-number v-model:value="searchParams.picHeight"/>
            </a-form-item>
            <a-form-item label="格式" name="picFormat">
              <a-input v-model:value="searchParams.picFormat" placeholder="请输入格式" allow-clear/>
            </a-form-item>
          </a-form>
        </a-collapse-panel>
      </a-collapse>
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit" style="width: 96px">搜索</a-button>
          <a-button html-type="rest" style="width: 96px" @click="doClear">重置</a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>

</template>

<script setup lang="ts">
import dayjs from "dayjs";
import {onMounted, reactive, ref} from "vue";
import {listPictureTagCategoryUsingGet} from "@/api/pictureController";
import {message} from "ant-design-vue";

interface Props {
  onSearch?: (searchParams: API.PictureQueryDto) => void
}

const props = defineProps<Props>()
const activeKey = ref([]);

//搜索条件
const searchParams = reactive<API.PictureQueryDto>({})
const onColorChange = (color: string) => {
  searchParams.picColor = color
}
//获取数据 调用父组件的搜索方法 onSearch
const doSearch = () => {
  console.log('搜索条件', JSON.stringify(searchParams))
  props.onSearch?.(searchParams)
}

/**
 * 获取分类、标签
 */
const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.code === 0 && res.data) {
    categoryOptions.value = (res.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data
      }
    })
    tagOptions.value = (res.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data
      }
    })
  } else {
    message.error('获取标签分类列表失败' + res.msg)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

//日期控件
const dateRange = ref<[]>([])
/**
 * 日期范围变化时触发
 * @param value
 * @param dateString
 */
const onRangeChange = (value: any, dateString: any) => {
  if (value.length >= 2) {
    searchParams.startEditTime = value[0].toDate()
    searchParams.endEditTime = value[1].toDate()
  } else {
    searchParams.startEditTime = undefined
    searchParams.endEditTime = undefined
  }
}
const rangePresets = ref([
  {
    label: '最近一周',
    value: [dayjs().subtract(7, 'd'), dayjs()],
  }, {
    label: '过去14天',
    value: [dayjs().subtract(14, 'd'), dayjs()],
  }, {
    label: '过去30天',
    value: [dayjs().subtract(30, 'd'), dayjs()],
  },
  {
    label: '过去90天',
    value: [dayjs().subtract(90, 'd'), dayjs()],
  },
])

// 清理
const doClear = () => {
  // 取消所有对象的值
  Object.keys(searchParams).forEach((key) => {
    searchParams[key] = undefined
  })
  dateRange.value = []
  props.onSearch?.(searchParams)
}

</script>

<style scoped>
.picture-search-form .ant-form-item{
  margin-top: 16px;
}

.custom-collapse >>> .ant-collapse-item {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.custom-collapse >>> .ant-collapse-item > .ant-collapse-header {
  flex: 0 0 auto; /* 不允许头部伸缩 */
}

.custom-collapse >>> .ant-collapse-item > .ant-collapse-content {
  flex: 1; /* 内容区域占据剩余空间 */
  display: flex;
  flex-direction: row; /* 子元素一行显示 */
}

.custom-collapse >>> .ant-form-item {
  margin-right: 16px; /* 表单项之间的间距 */
  margin-top:0
}
</style>
