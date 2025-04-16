<template>
  <div class="search_picture">
    <h2 style="margin-bottom: 16px">以图搜图</h2>
    <h3 style="margin: 16px 0 ">原图</h3>
    <!-- 单张图片-->
    <a-card hoverable style="width: 240px">
      <div>
        <a-image :src="picture.url"/>
      </div>
    </a-card>
      <h3 style="margin: 16px 0;">识图结果</h3>
      <!--图片列表-->
      <a-list :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
              :data-source="dataList"
      >
        <template #renderItem="{ item }">
          <a-list-item style="padding: 0">
            <!-- 单张图片-->
            <a href="item.fromUrl" target="_blank"/>
            <a-card hoverable>
              <div calss="searchImg" style="height: 200px; object-fit: cover">
                <a-image :src="item.thumbUrl" />
              </div>
            </a-card>
          </a-list-item>
        </template>
      </a-list>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from "vue";
import {
  getPictureVoByIdUsingPost,
  searchImageByBaiduUsingPost
} from "@/api/pictureController.js";
import {message} from "ant-design-vue";
import {useRoute} from "vue-router";
import {handleDragStart} from "@/utils";

const route = useRoute()

//从url 获取图片id
const pictureId = computed(() => {
  return route.query?.pictureId
})
const spaceId = computed(() => {
  return route.query?.spaceId
})
//获取原图数据
const picture = ref<API.Picture>({})
const getOldPicture = async () => {
  try {
    const res = await getPictureVoByIdUsingPost({
      isView:false
    },{
        id: pictureId.value,
        spaceId: spaceId.value,
      })
    console.log(JSON.toString(res.data))
    if (res.data) {
      picture.value = res.data
    } else {
      message.error('获取图片详情数据失败，' + res.msg)
    }
  } catch (error) {
    message.error('获取图片详情数据失败，' + error.msg)
  }
}

//搜索结果
const dataList = ref<API.ImageSearchResult[]>([])
const fetchSearchResult = async () => {
  try {
    const res = await searchImageByBaiduUsingPost({
      pictureId: pictureId.value,
      spaceId: spaceId.value
    })
    if (res.code===0 && res.data) {
        dataList.value = res.data ?? []
    } else {
      message.error('搜索图片失败，' + res.msg)
    }
  } catch (error) {
    message.error('搜索图片失败，' + error.msg)
  }
}
onMounted(()=>{
  getOldPicture()
  fetchSearchResult()
})
</script>

<style scoped>
/*.search_picture .searchImg{
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-height: 100px; !* 最小高度 *!
  max-height: 80vh; !* 最大高度为视口的80% *!
  overflow: hidden; !* 确保不会溢出 *!
}
.searchImg :deep(.ant-image) {
  display: block;
  width: 100%;
  height: 100%;
}*/
</style>
