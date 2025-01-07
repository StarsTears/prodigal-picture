<template>
  <div class="picture-list">
    <!--图片列表-->
    <a-list :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
            :data-source="dataList"
            :loading="loading"
    >
      <template #renderItem="{ item:picture,index}">
        <a-list-item style="padding: 0">
          <!-- 单张图片-->
          <a-card hoverable @click="doClickPicture(picture)">
            <template #cover>
              <div class="card-cover">
                <img :alt="picture.name" :src="picture.thumbnailUrl ?? picture.url"
                     style="height: 180px;object-fit: cover"/>
<!--                <div class="card-description" v-if="hoveringRefs[index].value">-->
<!--                  {{ picture.name }}-->
<!--                  <a-flex>-->
<!--                    <a-tag>-->
<!--                      {{ picture.category ?? '默认' }}-->
<!--                    </a-tag>-->
<!--                    <a-tag v-for="tag in picture.tags" :key="tag">-->
<!--                      {{ tag }}-->
<!--                    </a-tag>-->
<!--                  </a-flex>-->
<!--                </div>-->
              </div>
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
            <template v-if="showOp" #actions>
              <search-outlined @click="(e) => doSearch(picture, e)"/>
              <share-alt-outlined @click="(e) => doShare(picture, e)"/>
              <FullscreenOutlined @click="(e) => doAiOutPainting(picture, e)"/>
              <edit-outlined v-if="canEdit" @click="(e) => doEdit(picture, e)"/>
              <delete-outlined v-if="canDelete" @click="(e) => doDelete(picture, e)"/>
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
    <ShareModal ref="shareModalRef" :link="shareLink"/>
    <AiOutPainting ref="aiOutPaintingModalRef" :picture="AiPicture" :spaceId="AiPicture?.spaceId" :onSuccess="onAiOutPaintingSuccess"/>
  </div>

</template>

<script setup lang="ts">
import {useRouter} from "vue-router";
import {ref, reactive} from "vue";
import {DeleteOutlined, EditOutlined, SearchOutlined, ShareAltOutlined,FullscreenOutlined} from '@ant-design/icons-vue';
import {deletePictureUsingPost} from "@/api/pictureController";
import {message} from "ant-design-vue";
import ShareModal from "@/components/ShareModal.vue";
import AiOutPainting from "@/components/AiOutPainting.vue";

interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  showOp?: boolean
  onReload?: () => void
  canEdit?:boolean
  canDelete?:boolean
}
const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: () => false,
  showOp: false,
  canEdit:false,
  canDelete:false,
})

// 使用 reactive 来创建一个包含 refs 的数组
// const hoveringRefs = reactive(new Array(props.dataList.length).fill(ref(false)));
//
// const updateHovering=(index: number, value: boolean)=> {
//   hoveringRefs[index].value = value;
// }

const router = useRouter()
const doClickPicture = (picture) => {
  router.push(`/picture/${picture.spaceId}/${picture.id}/`)
}

//搜索
const doSearch = (picture, e) => {
  //阻止事件传播
  e.stopPropagation()
  window.open(`/picture/search_picture?spaceId=${picture.spaceId}&pictureId=${picture.id}`)
}

// 分享弹窗引用
const shareModalRef = ref(false)
// 分享链接
const shareLink = ref<string>()

// 分享
const doShare = (picture: API.PictureVO, e: Event) => {
  e.stopPropagation()
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.spaceId}/${picture.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}
//------------------------------AI扩图----------------------------
const aiOutPaintingModalRef = ref(false)
const AiPicture = ref<API.PictureVO>()
const doAiOutPainting = (picture: API.PictureVO, e: Event) => {
  e.stopPropagation()
  AiPicture.value = picture
  if (aiOutPaintingModalRef.value) {
    aiOutPaintingModalRef.value.openModal()
  }
}


// 编辑
const doEdit = (picture, e) => {
  //阻止事件传播
  e.stopPropagation()
  router.push({
    path: '/picture/add_picture',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

// 删除
const doDelete = async (picture, e) => {
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({
    id:id,
    spaceId: picture.spaceId,
  })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 让外层刷新
    props?.onReload()
  } else {
    message.error('删除失败')
  }
}


</script>

<style scoped>
.picture-list {
  margin-bottom: 16px;
}

.card-cover {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-description {
  position: absolute;
  bottom: 10px; /* 距离底部的距离 */
  left: 10px; /* 距离左侧的距离 */
  color: white; /* 文本颜色，确保与背景形成对比 */
  /*background-color: rgba(0, 0, 0, 0.5); !* 半透明背景 *!*/
  padding: 5px;
  border-radius: 5px;
  /*transition: opacity 0.3s;*/
}

</style>
