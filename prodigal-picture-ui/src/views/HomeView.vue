<template>
  <div class="homeView">
    <!--搜索框-->
    <div class="search-bar">
<!--      <a-form layout="inline" :model="searchParams" @finish="doSearch">-->
<!--        <a-form-item label="关键词" name="searchText">-->
<!--          <a-input-->
<!--            v-model:value="searchParams.searchText"-->
<!--            placeholder="input search text"-->
<!--            allow-clear-->
<!--          />-->
<!--        </a-form-item>-->
<!--        &lt;!&ndash; 按颜色搜索 &ndash;&gt;-->
<!--        <a-form-item label="颜色" name="picColor">-->
<!--          <color-picker v-model:value="searchParams.picColor" format="hex" @pureColorChange="onColorChange"/>-->
<!--        </a-form-item>-->
<!--      </a-form>-->
        <a-input-search
          v-model:value="searchParams.searchText"
          placeholder="input search text"
          enter-button="Search"
          size="large"
          @search="doSearch"
        />

    </div>
    <!--标签分类筛选-->
<!--    <div label="颜色" name="picColor">-->
<!--      <color-picker v-model:value="searchParams.picColor" format="hex" @pureColorChange="onColorChange"/>-->
<!--    </div>-->
    <a-tabs v-model:active-key="selectCategory" @change="doSearch">
      <a-tab-pane key="all" tab="全部"/>
      <a-tab-pane v-for="category in categoryList" :key="category" :tab="category"/>
    </a-tabs>
    <div class="tag-bar">
      <span style="margin-right: 8px">标签:</span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          v-model:checked="selectTagList[index]"
          @change="doSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>

    <!--图片列表-->
    <PictureList :dataList="dataList" :loading="loading"/>
    <a-pagination v-model:current="searchParams.current"
                  v-model:page-size="searchParams.pageSize"
                  :total="total"
                  :locale="locale"
                  @change="onPageChange"
                  style="text-align: right"
    />
  </div>
</template>
<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageCacheUsingPost,
  listPictureVoByPageUsingPost,
} from "@/api/pictureController";
import {message} from "ant-design-vue";
import PictureList from "@/components/PictureList.vue";

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
// const onColorChange=(color:string)=>{
//   searchParams.picColor = color
// }
// 获取数据
const fetchData = async () => {
  loading.value = true
  //转换搜索参数
  const params = {
    ...searchParams,
    tags: [],
    nullSpaceId: true
  }
  if (selectCategory.value !== 'all') {
    params.category = selectCategory.value
  }
  selectTagList.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags.push(tagList.value[index])
    }
  })
  // const res = await listPictureVoByPageCacheUsingPost( params)
  const res = await listPictureVoByPageUsingPost(params)
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

/**
 * 获取分类、标签
 */
const categoryList = ref<string[]>([])
const selectCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectTagList = ref<boolean[]>([])
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.code === 0 && res.data) {
    categoryList.value = res.data.categoryList ?? []
    tagList.value = res.data.tagList ?? []
  } else {
    message.error('获取标签分类列表失败' + res.msg)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})
</script>
<style scoped>
.homeView {
  margin-bottom: 16px;
}

.homeView .search-bar {
  max-width: 480px;
  margin: 0 auto 16px;
}

.homeView .tag-bar {
  margin-bottom: 20px;
}
</style>
