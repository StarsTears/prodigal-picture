<template>
  <div class="homeView">
    开始学习前端：vue3+ ant design vue
    <!--搜索框-->
    <div class="search-bar">
      <a-input-search
        v-model:value="searchParams.searchText"
        placeholder="input search text"
        enter-button="Search"
        size="large"
        @search="doSearch"
      />
    </div>
    <!--标签分类筛选-->
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
    <a-list :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
            :data-source="dataList"
            :pagination="pagination"
            :loading="loading"
    >
      <template #renderItem="{ item:picture }">
        <a-list-item style="padding: 0">
          <!-- 单张图片-->
          <a-card hoverable @click="doClickPicture(picture)">
            <template #cover>
              <img :alt="picture.name" :src="picture.url"
                   style="height: 180px;object-fit: cover"/>
            </template>
            <a-card-meta :title="picture.name">
              <template #description>
                <a-flex>
                  <a-tag color="green">
                    {{ picture.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tags" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>
<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {
  listPictureByPageUsingPost,
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost
} from "@/api/pictureController";
import {message} from "ant-design-vue";
import {useRouter} from "vue-router";

// 数据
const dataList = ref<API.Picture[]>([])
const loading = ref<boolean>(true)
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
    tags: []
  }
  if (selectCategory.value !== 'all') {
    params.category = selectCategory.value
  }
  selectTagList.value.forEach((useTag,index)=>{
    if (useTag){
      params.tags.push(tagList.value[index])
    }
  })
  const res = await listPictureVoByPageUsingPost( params)
  if (res.data) {
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
    showSizeChanger: true,
    locale: locale,
    pageSizeOptions: ['5', '10', '20', '30'],//可选的页面显示条数
    onChange: (page: number, pageSize: number) => {
      searchParams.current = page
      searchParams.pageSize = pageSize
      fetchData()
    }
  }
})


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


const router = useRouter()
const doClickPicture=(picture)=> {router.push(`/picture/${picture.id}/`)}
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
