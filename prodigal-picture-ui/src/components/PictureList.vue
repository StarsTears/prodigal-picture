<template>
  <div class="picture-list">
    <!--骨架屏加载-->
    <div v-if="loading" class="skeleton-grid">
      <div v-for="i in 8" :key="i" class="skeleton-card">
        <a-skeleton-image style="width: 100%; height: 180px" />
        <a-skeleton active :paragraph="{ rows: 1 }" :title="false" style="padding: 8px 10px" />
      </div>
    </div>

    <!--空状态-->
    <div v-else-if="dataList.length === 0" class="empty-state">
      <a-empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无图片" />
    </div>

    <!--瀑布流图片列表-->
    <Waterfall v-else
               :list="dataList"
               :width="280"
               :breakpoints="breakpoints"
               :gutter="14"
               background-color="transparent">
      <template #default="{ item }">
        <div class="picture-card" @click="doClickPicture(item)">
          <div class="image-wrapper" @dragstart="handleDragStart">
            <LazyImg :url="item.thumbnailUrl ?? item.url" />
            <!--悬停遮罩层-->
            <div class="image-overlay">
              <div class="overlay-top" v-if="showOp">
                <span class="overlay-action" @click.stop="(e) => doSearch(item, e)" title="以图搜图">
                  <SearchOutlined />
                </span>
                <span class="overlay-action" @click.stop="(e) => doShare(item, e)" title="分享">
                  <ShareAltOutlined />
                </span>
                <span class="overlay-action" @click.stop="(e) => doAiOutPainting(item, e)" title="AI 扩图">
                  <FullscreenOutlined />
                </span>
                <span v-if="canEdit" class="overlay-action" @click.stop="(e) => doEdit(item, e)" title="编辑">
                  <EditOutlined />
                </span>
                <a-popconfirm v-if="canDelete" title="确定删除该图片？" ok-text="确定" cancel-text="取消" @confirm="doDelete(item)">
                  <span class="overlay-action overlay-action--danger" @click.stop="(e) => e.stopPropagation()" title="删除">
                    <DeleteOutlined />
                  </span>
                </a-popconfirm>
              </div>
              <div class="overlay-bottom">
                <span class="overlay-title">{{ item.name }}</span>
                <span class="overlay-category">{{ item.category ?? '默认' }}</span>
              </div>
            </div>
          </div>
          <div class="card-caption">
            <span class="caption-text">{{ item.name }}</span>
          </div>
        </div>
      </template>
    </Waterfall>

    <ShareModal ref="shareModalRef" :link="shareLink" :name="picName" />
    <AiOutPainting ref="aiOutPaintingModalRef" :picture="AiPicture" :spaceId="AiPicture?.spaceId" :onSuccess="onAiOutPaintingSuccess" />
  </div>
</template>

<script setup lang="ts">
import { LazyImg, Waterfall } from 'vue-waterfall-plugin-next'
import 'vue-waterfall-plugin-next/dist/style.css'
import { useRouter } from "vue-router"
import { ref } from "vue"
import { handleDragStart } from "@/utils"
import {
  DeleteOutlined,
  EditOutlined,
  SearchOutlined,
  ShareAltOutlined,
  FullscreenOutlined,
} from '@ant-design/icons-vue'
import { Empty } from 'ant-design-vue'
import { deletePictureUsingPost } from "@/api/pictureController"
import { message } from "ant-design-vue"
import ShareModal from "@/components/ShareModal.vue"
import AiOutPainting from "@/components/AiOutPainting.vue"
import { useLoginUserStore } from "@/stores/loginUserStore.ts"

interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  showOp?: boolean
  onReload?: () => void
  canEdit?: boolean
  canDelete?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false,
  canEdit: false,
  canDelete: false,
})

const breakpoints = ref({
  3000: { rowPerView: 7 },
  1800: { rowPerView: 6 },
  1500: { rowPerView: 5 },
  1200: { rowPerView: 4 },
  1000: { rowPerView: 3 },
  800: { rowPerView: 2 },
  500: { rowPerView: 1 },
})

const router = useRouter()

const doClickPicture = (picture: API.PictureVO) => {
  router.push(`/picture/${picture.spaceId}/${picture.id}/`)
}

const doSearch = (picture: API.PictureVO, e: Event) => {
  e.stopPropagation()
  window.open(`/picture/search_picture?spaceId=${picture.spaceId}&pictureId=${picture.id}`)
}

const shareModalRef = ref(false)
const shareLink = ref<string>()
const picName = ref<string>()

const doShare = (picture: API.PictureVO, e: Event) => {
  e.stopPropagation()
  useLoginUserStore().checkLogin()
  picName.value = picture.name
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.spaceId}/${picture.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}

const aiOutPaintingModalRef = ref(false)
const AiPicture = ref<API.PictureVO>()

const doAiOutPainting = (picture: API.PictureVO, e: Event) => {
  e.stopPropagation()
  AiPicture.value = picture
  if (aiOutPaintingModalRef.value) {
    aiOutPaintingModalRef.value.openModal()
  }
}

const doEdit = (picture: API.PictureVO, e: Event) => {
  e.stopPropagation()
  router.push({
    path: '/picture/add_picture',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

const doDelete = async (picture: API.PictureVO) => {
  const id = picture.id
  if (!id) return
  const res = await deletePictureUsingPost({ id, spaceId: picture.spaceId })
  if (res.code === 0) {
    message.success('删除成功')
    props?.onReload?.()
  } else {
    message.error('删除失败')
  }
}

const onAiOutPaintingSuccess = () => {
  props?.onReload?.()
}
</script>

<style scoped>
.picture-list {
  margin-bottom: 16px;
}

/* 骨架屏 */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

.skeleton-card {
  border-radius: 10px;
  overflow: hidden;
  background: var(--bg-card);
}

/* 空状态 */
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

/* 图片卡片 */
.picture-card {
  border-radius: 10px;
  overflow: hidden;
  background: var(--bg-card);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  cursor: pointer;
}

.picture-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.13);
}

.picture-card:hover .image-overlay {
  opacity: 1;
}

/* 图片容器 */
.image-wrapper {
  position: relative;
  overflow: hidden;
  background: var(--bg-image-placeholder);
  line-height: 0;
}

.image-wrapper :deep(img) {
  width: 100%;
  display: block;
}

/* 悬停遮罩 */
.image-overlay {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

/* 顶部操作栏 */
.overlay-top {
  display: flex;
  gap: 4px;
  justify-content: flex-end;
  padding: 8px;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.45), transparent);
}

.overlay-action {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: #fff;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(4px);
  transition: background 0.2s;
  cursor: pointer;
}

.overlay-action:hover {
  background: rgba(255, 255, 255, 0.28);
}

.overlay-action--danger:hover {
  background: rgba(255, 77, 79, 0.7);
}

/* 底部信息 */
.overlay-bottom {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 8px;
  padding: 28px 10px 10px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6), transparent);
}

.overlay-title {
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.overlay-category {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.18);
  white-space: nowrap;
  flex-shrink: 0;
}

/* 底部标题 */
.card-caption {
  padding: 8px 10px 10px;
}

.caption-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
}
</style>
