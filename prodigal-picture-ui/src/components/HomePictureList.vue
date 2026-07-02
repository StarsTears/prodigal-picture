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

          <!--悬停遮罩层-->
          <div class="image-overlay">
            <!--顶部：格式 + 分辨率-->
            <div class="overlay-top">
              <span v-if="item.picFormat" class="format-badge">{{ item.picFormat.toUpperCase() }}</span>
              <span v-if="item.picWidth" class="resolution-text">{{ item.picWidth }} × {{ item.picHeight }}</span>
            </div>

            <!--中间：分类 + 标签-->
            <div class="overlay-center">
              <span v-if="item.category" class="category-pill">{{ item.category }}</span>
              <div v-if="item.tags?.length" class="tags-row">
                <span v-for="tag in item.tags.slice(0, 3)" :key="tag" class="mini-tag">{{ tag }}</span>
              </div>
            </div>

            <!--底部：名称 + 色调 + 统计 + 操作-->
            <div class="overlay-bottom">
              <div class="overlay-bottom__info">
                <span class="overlay-title">{{ item.name }}</span>
                <div class="overlay-meta">
                  <span v-if="item.picColor" class="color-dot" :style="{ backgroundColor: toHexColor(item.picColor) }" />
                  <span v-if="item.picSize" class="meta-size">{{ formatSize(item.picSize) }}</span>
                </div>
              </div>
              <div class="overlay-actions">
                <span class="action-item" title="浏览">
                  <EyeOutlined />
                  {{ formatNumber(item.viewQuantity) }}
                </span>
                <span v-if="showShare" class="action-item" @click.stop="(e) => doShare(item, e)" title="分享">
                  <ShareAltOutlined />
                  {{ formatNumber(item.shareQuantity) }}
                </span>
                <span v-if="showSearch" class="action-item" @click.stop="(e) => doSearch(item, e)" title="以图搜图">
                  <SearchOutlined />
                </span>
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
  EyeOutlined,
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
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
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
  transition: transform 0.4s ease;
}

.picture-card:hover .image-wrapper :deep(img) {
  transform: scale(1.04);
}

/* ========== 悬停遮罩层 ========== */
.image-overlay {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  pointer-events: none;
}

.picture-card:hover .image-overlay {
  pointer-events: auto;
}

/* --- 顶部：格式 + 分辨率 --- */
.overlay-top {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.5), transparent);
}

.format-badge {
  font-size: 10px;
  font-weight: 700;
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(6px);
  padding: 3px 8px;
  border-radius: 4px;
  letter-spacing: 0.5px;
}

.resolution-text {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.85);
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.4);
}

/* --- 中间：分类 + 标签 --- */
.overlay-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
}

.category-pill {
  font-size: 11px;
  color: #fff;
  background: rgba(64, 150, 255, 0.7);
  backdrop-filter: blur(6px);
  padding: 3px 12px;
  border-radius: 12px;
  font-weight: 500;
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px;
}

.mini-tag {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(6px);
  padding: 2px 8px;
  border-radius: 10px;
}

/* --- 底部：名称 + 色调 + 统计 + 操作 --- */
.overlay-bottom {
  padding: 28px 10px 10px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.65), transparent);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.overlay-bottom__info {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 8px;
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
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.6);
}

.overlay-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.color-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1.5px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 0 4px rgba(0, 0, 0, 0.3);
}

.meta-size {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
  white-space: nowrap;
}

.overlay-actions {
  display: flex;
  gap: 14px;
  justify-content: center;
  color: #fff;
  font-size: 13px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 3px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.action-item:hover {
  background: rgba(255, 255, 255, 0.2);
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
