<template>
  <Waterfall  v-if="dataList.length > 0"
              :list="dataList"
              :width="300"
              :breakpoints="breakpoints">
    <!-- v2.6.0之前版本插槽数据获取 -->
    <!-- <template #item="{ item, url, index }"> -->
    <!-- 新版插槽数据获取 -->
    <template #default="{ item, url, index }">
      <a-card >
        <template #cover>
          <!-- @dragstart="handleDragStart" 禁止拖拽 -->
          <div  @dragstart="handleDragStart" @click="doClickPicture(item)">
            <LazyImg :url="item.thumbnailUrl ?? item.url" />
          </div>
        </template>
        <a-card-meta
          :title="item.name"
          :description="`作者: ${item.user.userName}`"
          :bodyStyle="{ color: '#fff' }"
        >
          <template #avatar>
            <a-avatar size="large" v-if="item.user.userAvatar" :src="item.user.userAvatar" />
            <a-avatar size="large" v-else>
              <template #icon>
                <UserOutlined />
              </template>
            </a-avatar>
          </template>
        </a-card-meta>
        <template  #actions>
          <div v-if="showView">
            <EyeOutlined />
            {{ formatNumber(item.viewQuantity) }}
          </div>
          <div v-if="showShare">
            <share-alt-outlined @click="(e) => doShare(item, e)"/>
            {{ formatNumber(item.shareQuantity) }}
          </div>
          <div v-if="showSearch">
            <search-outlined @click="(e) => doSearch(item, e)"/>
          </div>
          <!--            <FullscreenOutlined @click="(e) => doAiOutPainting(item, e)"/>-->
          <!--            <edit-outlined v-if="canEdit" @click="(e) => doEdit(item, e)"/>-->
          <!--            <delete-outlined v-if="canDelete" @click="(e) => doDelete(item, e)"/>-->
        </template>
      </a-card>
    </template>
  </Waterfall>
  <!-- 分享弹框组件 -->
  <ShareModal ref="shareModalRef" :link="shareLink" :name="picName"/>
</template>
<script setup lang="ts">
import { LazyImg, Waterfall } from 'vue-waterfall-plugin-next'
import 'vue-waterfall-plugin-next/dist/style.css'
import {useRouter} from "vue-router";
import {ref} from "vue";
import {formatNumber, handleDragStart} from "@/utils";
import {EyeOutlined,UserOutlined,DeleteOutlined, EditOutlined, SearchOutlined, ShareAltOutlined,FullscreenOutlined} from '@ant-design/icons-vue';
import ShareModal from "@/components/ShareModal.vue";
import {useLoginUserStore} from "@/stores/loginUserStore.ts";
import {message} from "ant-design-vue";

interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  showView?: boolean
  showShare?: boolean
  showSearch?:boolean
  showOp?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false
})
/**
 * 瀑布流布局
 */
const breakpoints = ref({
  3000: {
    //当屏幕宽度小于等于3000
    rowPerView: 7, // 一行8图
  },
  1800: {
    rowPerView: 6,
  },
  1500: {
    rowPerView: 5,
  },
  1200: {
    rowPerView: 4,
  },
  1000: {
    rowPerView: 3,
  },
  800: {
    rowPerView: 2,
  },
  700: {
    rowPerView: 2,
  },
  500: {
    rowPerView: 1,
  },
  300: {
    rowPerView: 1,
  },
})


const router = useRouter()
const doClickPicture = (picture) => {
  router.push(`/picture/${picture.spaceId}/${picture.id}/`)
}

/**
 * 分享状态
 */
const isShare = ref<boolean>(true)
// 分享弹窗引用
const shareModalRef = ref(false)
// 分享链接
const shareLink = ref<string>()
/**
 * 图片名称
 */
const picName = ref<string>()
// 分享
const doShare = (picture: API.PictureVO, e: Event) => {
  e.stopPropagation()
  useLoginUserStore().checkLogin()
  if (!isShare.value) {
    message.warn('已分享！')
    return
  }
  picName.value=picture.name
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.spaceId}/${picture.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}

//搜索
const doSearch = (picture: API.PictureVO, e: Event) => {
  //阻止事件传播
  e.stopPropagation()
  router.push(`/picture/search_picture?spaceId=${picture.spaceId}&pictureId=${picture.id}`)
}
</script>
