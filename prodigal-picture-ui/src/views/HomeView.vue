<template>
  <div class="homeView">
    <!--搜索框-->
    <div :class="['sticky-header', { sticky: isSticky }]" >
      <div class="search-with-logo">
<!--        <img src="@/assets/logo.jpg" alt="logo" class="search-logo" />-->
        <a-input-search
          v-model:value="searchParams.searchText"
          placeholder="input search text"
          enter-button="Search"
          size="large"
          @search="doSearch"
          class="logo-search"
        />
      </div>
      <!--标签分类筛选-->
      <!--    <div label="颜色" name="picColor">-->
      <!--      <color-picker v-model:value="searchParams.picColor" format="hex" @pureColorChange="onColorChange"/>-->
      <!--    </div>-->
      <a-tabs v-model:active-key="selectCategory" @change="doSearch">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane v-for="category in categoryList" :key="category" :tab="category" />
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
    </div>
    <!-- 占位元素 -->
    <div v-if="isSticky" class="sticky-placeholder" />
    <!--图片列表-->
    <div v-if="homeLoading" class="loading-spinner">
      <a-spin size="large" tip="加载中..." />
    </div>
    <div v-else>
      <!-- 图片列表 -->
      <HomePictureList
        :dataList="dataList"
        :loading="true"
        :showView="true"
        :showShare="true"
        :showSearch="true"
      />
<!--      <PictureList-->
<!--        :dataList="dataList"-->
<!--        :loading="homeLoading"-->
<!--        :showView="true"-->
<!--        :showLike="true"-->
<!--        :showCollect="true"-->
<!--        :showShare="true"-->
<!--        :showSearch="true"-->
<!--      />-->

      <!-- 加载信息 -->
      <div class="loadingInfo">
        <a-spin v-if="homeLoading" size="large" />
        <div v-if="showBottomLine">
          <a-divider v-if="dataList.length > 0" style="color: #666666">
<!--            🦖🦖🦖 这是我的底线~-->
            🍃🍃🍃这是我的底线~
          </a-divider>
          <a-empty v-else :image="Empty.PRESENTED_IMAGE_SIMPLE" />
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageCacheUsingPost,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController'
import { Empty, message } from 'ant-design-vue'
import PictureList from '@/components/PictureList.vue'
import HomePictureList from '@/components/HomePictureList.vue'

// 接收 BasicLayout 的 siderWidth
const props = defineProps<{
  siderWidth: number
}>()

// 计算搜索框的宽度
const searchWidth = computed(() => {
  return `calc( ${props.siderWidth+28}px)` // siderWidth + logo宽度 + 间距 + padding
})

/**
 * 加载变量状态
 */
const homeLoading = ref(true)
/**
 * 加载完成状态
 */
const loadingFinish = ref(false)
/**
 * 控制底线显示
 */
const showBottomLine = ref(false)

// 新增吸顶状态
const isSticky = ref(false)
// 滚动监听
const checkSticky = () => {
  isSticky.value = window.scrollY > 100
}

// 数据
const dataList = ref<API.Picture[]>([])

// 初始化页面
onMounted(() => {
  window.addEventListener('scroll', checkSticky)
  getTagCategoryOptions()
  // 延迟执行初始图片加载，确保DOM已准备好
  setTimeout(() => {
    fetchData()
  }, 100)
  window.addEventListener('scroll', handleScrollDebounced)
})

/**
 * 卸载页面
 */
onUnmounted(() => {
  window.removeEventListener('scroll', checkSticky)
  window.removeEventListener('scroll', handleScrollDebounced)
})

const loadingLock = ref(false)

// 搜索条件
const searchParams = reactive<API.PictureQueryDto>({
  current: 1,
  pageSize: 20,
  sortField: 'viewQuantity',
  sortOrder: 'descend',
})

// 获取数据
const fetchData = async () => {
  if (loadingFinish.value || loadingLock.value) return // 如果已经加载完毕，直接返回
  loadingLock.value = true

  //转换搜索参数
  const params = {
    ...searchParams,
    tags: [],
    nullSpaceId: true,
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
    const newRecords = res.data.records || []
    dataList.value = [...dataList.value, ...newRecords]
    // total.value = res.data.total
    if (newRecords.length < searchParams.pageSize) {
      loadingFinish.value = true // 没有更多数据了
      window.removeEventListener('scroll', handleScroll) // 移除滚动监听

      // 延迟 1 秒后显示底线
      setTimeout(() => {
        showBottomLine.value = true
      }, 1000)
    } else {
      // 检查页面高度是否小于屏幕高度
      checkPageHeight()
    }
  } else {
    message.error('图片加载失败!' + res.msg)
  }
  homeLoading.value = false
  loadingLock.value = false
}

const categoryList = ref<string[]>([])
const selectCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectTagList = ref<boolean[]>([])
// 获取数据
const doSearch = () => {
  homeLoading.value = true
  // 重置页码
  searchParams.current = 1
  //处理分类
  if (selectCategory.value !== 'all') {
    searchParams.category = selectCategory.value
  } else {
    searchParams.category = undefined
  }
  loadingFinish.value = false
  showBottomLine.value = false
  dataList.value = []
  fetchData()
}

/**
 * 获取分类、标签
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.code === 0 && res.data) {
    categoryList.value = res.data.categoryList ?? []
    tagList.value = res.data.tagList ?? []
  } else {
    message.error('获取标签分类列表失败' + res.msg)
  }
}

/**
 * 检查页面高度是否小于屏幕高度
 */
const checkPageHeight = () => {
  // 延迟检查，确保DOM更新完成
  setTimeout(() => {
    const scrollHeight = Math.max(document.documentElement.scrollHeight, document.body.scrollHeight)
    const clientHeight =
      window.innerHeight ||
      Math.min(document.documentElement.clientHeight, document.body.clientHeight)

    if (scrollHeight <= clientHeight && !loadingFinish.value) {
      searchParams.current++
      fetchData()
    }
  }, 300)
}
/**
 * 滚动加载
 */
const handleScroll = () => {
  if (loadingFinish.value || homeLoading.value) return
  const scrollHeight = Math.max(document.documentElement.scrollHeight, document.body.scrollHeight)
  const scrollTop =
    window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop
    // window.scrollY || document.documentElement.scrollTop || document.body.scrollTop
  const clientHeight =
    window.innerHeight ||
    Math.min(document.documentElement.clientHeight, document.body.clientHeight)
  // 增加更严格的判断条件
  if (scrollHeight - (clientHeight + scrollTop) < 500) {
    searchParams.current++
    fetchData()
  }
}
/**
 * 防抖函数
 */
const debounce = (fn, delay) => {
  let timeout
  return function (...args) {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}
// 在setup外部定义防抖函数
const handleScrollDebounced = debounce(handleScroll, 200)
</script>
<style scoped>
/* 吸顶容器 */
.sticky-header {
  background: white;
  transition: all 0.3s ease;
  padding: 0 20px;
  /*优化搜索框容器*/
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

/* 吸顶状态 */
.sticky-header.sticky {
  position: fixed;
  top: 60px;
  left: v-bind(searchWidth);
  right: 0;
  z-index: 1000;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(5px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  /*优化搜索栏与页面大小不一致*/
  /*max-width: 100% !important;*/
  width: 100%;
}

/* 带logo的搜索容器 */
.search-with-logo {
  display: flex;
  align-items: center;
  max-width: 800px;
  padding-top: 10px;
  margin: 10px auto 10px;
  gap: 12px;
}


/* logo样式 */
.search-logo {
  height: 50px;
  width: auto;
  object-fit: contain;
  max-width: 200px;
}

/* 调整后的搜索框 */
.logo-search {
  flex: 1;
  max-width: 700px;
}

.logo-search :deep(.ant-input) {
  border-radius: 24px 0 0 24px !important;
  height: 46px;
  font-size: 16px;
  border-color: #d9d9d9;
}

.logo-search :deep(.ant-input:hover),
.logo-search :deep(.ant-input:focus) {
  border-color: #4096ff;
}

.logo-search :deep(.ant-input-group-addon .ant-btn) {
  border-radius: 0 24px 24px 0 !important;
  height: 46px;
  width: 90px;
  background-color: #4096ff;
  color: white;
  border-color: #4096ff;
}

.logo-search :deep(.ant-input-group-addon .ant-btn:hover) {
  background-color: #1677ff;
  border-color: #1677ff;
}

/* 占位元素 */
.sticky-placeholder {
  height: calc(50px + 46px + 48px); /* logo高度 + 搜索框高度 + 标签高度 + 间距 */
}

.loadingInfo {
  text-align: center;
  padding: 30px 50px;
  margin: 20px 0;
}

.loading-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}
</style>
