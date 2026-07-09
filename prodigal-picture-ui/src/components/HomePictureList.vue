<template>
  <div v-if="loading" class="skeleton-grid">
    <div v-for="i in 8" :key="i" class="skeleton-card">
      <a-skeleton-image style="width: 100%; height: 180px" />
      <a-skeleton active :paragraph="{ rows: 1 }" :title="false" style="padding: 8px 10px" />
    </div>
  </div>
  <div v-else-if="dataList.length === 0" class="empty-state">
    <a-empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无图片" />
  </div>
  <Waterfall v-else
             :list="dataList"
             :width="280"
             :breakpoints="breakpoints"
             :gutter="12"
             background-color="var(--bg-body)">
    <template #default="{ item }">
      <div class="picture-card" @click="doClickPicture(item)">
        <div class="image-wrapper" @dragstart="handleDragStart">
          <LazyImg :url="item.thumbnailUrl ?? item.url" />

          <!--悬停信息卡片-->
          <div class="hover-card">
            <!--操作按钮-->
            <div class="hover-card__actions">
              <span class="hover-action" @click.stop="(e) => doSearch(item, e)" title="以图搜图">
                <SearchOutlined />
              </span>
              <span v-if="showShare" class="hover-action" @click.stop="(e) => doShare(item, e)" title="分享">
                <ShareAltOutlined />
              </span>
            </div>

            <!--分类 + 标签-->
            <div class="hover-top">
              <span v-if="item.category" class="hover-category">{{ item.category }}</span>
              <div v-if="item.tags?.length" class="hover-tags">
                <span v-for="tag in item.tags.slice(0, 4)" :key="tag" class="hover-tag">{{ tag }}</span>
              </div>
            </div>

            <!--信息行-->
            <div class="hover-rows">
              <div class="hover-row">
                <ColumnWidthOutlined class="hover-row__icon" />
                <span class="hover-row__text">{{ item.picWidth }} × {{ item.picHeight }}</span>
              </div>
              <div class="hover-row">
                <PictureOutlined class="hover-row__icon" />
                <span class="hover-row__text">{{ item.picFormat?.toUpperCase() ?? '-' }}</span>
              </div>
              <div class="hover-row">
                <FileOutlined class="hover-row__icon" />
                <span class="hover-row__text">{{ formatSize(item.picSize) }}</span>
              </div>
              <div class="hover-row">
                <DownloadOutlined class="hover-row__icon" />
                <span class="hover-row__text">{{ formatNumber(item.downloadQuantity) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!--卡片底部标题-->
        <div class="card-caption">
          <span class="caption-text">{{ item.name }}</span>
          <span class="caption-author">{{ item.user?.userName }}</span>
        </div>
      </div>
    </template>
  </Waterfall>

  <ShareModal ref="shareModalRef" :link="shareLink" :name="picName"/>
</template>

<script setup lang="ts">
import { LazyImg, Waterfall } from 'vue-waterfall-plugin-next'
import 'vue-waterfall-plugin-next/dist/style.css'
import { useRouter } from "vue-router"
import { ref } from "vue"
import { formatNumber, formatSize, handleDragStart, toHexColor } from "@/utils"
import {
  SearchOutlined,
  ShareAltOutlined,
  ColumnWidthOutlined,
  PictureOutlined,
  FileOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { Empty } from 'ant-design-vue'
import ShareModal from "@/components/ShareModal.vue"
import { useLoginUserStore } from "@/stores/loginUserStore.ts"
import { message } from "ant-design-vue"
import { incrementShareQuantityUsingPost } from "@/api/pictureController"

interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  showView?: boolean
  showShare?: boolean
  showSearch?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showView: true,
  showShare: true,
  showSearch: true,
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

const shareModalRef = ref(false)
const shareLink = ref<string>()
const picName = ref<string>()

const doShare = (picture: API.PictureVO, e: Event) => {
  e.stopPropagation()
  useLoginUserStore().checkLogin()
  picName.value = picture.name
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.spaceId}/${picture.id}`
  incrementShareQuantityUsingPost({ id: picture.id, spaceId: picture.spaceId }).then(() => {
    picture.shareQuantity = (picture.shareQuantity || 0) + 1
  })
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}

const doSearch = (picture: API.PictureVO, e: Event) => {
  e.stopPropagation()
  router.push(`/picture/search_picture?spaceId=${picture.spaceId}&pictureId=${picture.id}`)
}
</script>

<style scoped>
/* 骨架屏 */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
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
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
}

.picture-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.14);
}

.picture-card:hover .hover-card {
  opacity: 1;
  transform: translateY(0);
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
  transition: transform 0.45s ease;
}

.picture-card:hover .image-wrapper :deep(img) {
  transform: scale(1.05);
}

/* ========== 悬停信息卡片 ========== */
.hover-card {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) translateY(6px);
  width: calc(100% - 48px);
  max-width: 240px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(2px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  opacity: 0;
  transition: opacity 0.3s ease, transform 0.3s ease;
  pointer-events: none;
}

.picture-card:hover .hover-card {
  pointer-events: auto;
  opacity: 1;
  transform: translate(-50%, -50%) translateY(0);
}

/* --- 操作按钮 --- */
.hover-card__actions {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
}

.hover-action {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  background: rgba(255, 255, 255, 0.08);
  transition: background 0.2s;
  cursor: pointer;
}

.hover-action:hover {
  background: rgba(255, 255, 255, 0.25);
}

/* --- 信息行 --- */
.hover-rows {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 2px 12px;
}

.hover-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 3px 0;
}

.hover-row__icon {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  flex-shrink: 0;
  width: 16px;
  text-align: center;
}

.hover-row__text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
  white-space: nowrap;
}

/* --- 分类 + 标签 --- */
.hover-top {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.hover-category {
  font-size: 10px;
  font-weight: 600;
  color: #fff;
  background: rgba(64, 150, 255, 0.4);
  padding: 2px 10px;
  border-radius: 8px;
  flex-shrink: 0;
}

.hover-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.hover-tag {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.08);
  padding: 1px 7px;
  border-radius: 6px;
}

/* ========== 底部标题 ========== */
.card-caption {
  padding: 8px 10px 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.caption-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.caption-author {
  font-size: 11px;
  color: var(--text-secondary);
  white-space: nowrap;
  flex-shrink: 0;
}
</style>
