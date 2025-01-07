<template>
  <div class="search_picture">
    <h2 style="margin-bottom: 16px">以图搜图</h2>
    <h3 style="margin: 16px 0 ">原图</h3>
    <!-- 单张图片-->
    <a-card hoverable style="width: 240px">
      <template #cover>
        <img :alt="picture.name"
             :src="picture.thumbnailUrl ?? picture.url"
             style="height: 180px;object-fit: cover"/>
      </template>
    </a-card>
      <h3 style="margin: 16px 0">识图结果</h3>
      <!--图片列表-->
      <a-list :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
              :data-source="dataList"
      >
        <template #renderItem="{ item }">
          <a-list-item style="padding: 0">
            <!-- 单张图片-->
            <a href="item.fromUrl" target="_blank"/>
            <a-card hoverable>
              <template #cover>
                <img :src="item.thumbUrl "
                     style="height: 180px;object-fit: cover"/>
              </template>
            </a-card>
          </a-list-item>
        </template>
      </a-list>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from "vue";
import {getPictureVoByIdUsingGet, searchImageByBaiduUsingPost} from "@/api/pictureController.js";
import {message} from "ant-design-vue";
import {useRoute} from "vue-router";

const route = useRoute()

//从url 获取图片id
const pictureId = computed(() => {
  return route.query?.pictureId
})
//获取原图数据
const picture = ref<API.PictureVO>({})
const getOldPicture = async () => {
  const id = route.query?.pictureId
  try {
    const res = await getPictureVoByIdUsingGet({id: id})
    if (res.data) {
      picture.value = res.data
    } else {
      message.error('获取图片详情数据失败，' + res.msg)
    }
  } catch (error) {
    message.error('获取图片详情数据失败，' + error.msg)
  }
}
onMounted(() => {
  getOldPicture()
})
//搜索结果
const dataList = ref<API.ImageSearchResult[]>([])
const fetchSearchResult = async () => {
  try {
    const res = await searchImageByBaiduUsingPost({pictureId: pictureId.value})
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
  fetchSearchResult()
})
</script>

<style scoped>

</style>
