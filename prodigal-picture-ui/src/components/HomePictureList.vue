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
          <div class="image-overlay">
            <div class="overlay-actions">
              <span class="action-item">
                <EyeOutlined />
                {{ formatNumber(item.viewQuantity) }}
              </span>
              <span v-if="showShare" class="action-item" @click.stop="(e) => doShare(item, e)">
                <ShareAltOutlined />
                {{ formatNumber(item.shareQuantity) }}
              </span>
              <span v-if="showSearch" class="action-item" @click.stop="(e) => doSearch(item, e)">
                <SearchOutlined />
              </span>
            </div>
          </div>
        </div>
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
import { useRouter } from "vue-router";
import { ref } from "vue";
import { formatNumber, handleDragStart } from "@/utils";
import {
  SearchOutlined,
  ShareAltOutlined,
  EyeOutlined,
} from '@ant-design/icons-vue';
import { Empty } from 'ant-design-vue';
import ShareModal from "@/components/ShareModal.vue";
import { useLoginUserStore } from "@/stores/loginUserStore.ts";
import { message } from "ant-design-vue";
import { incrementShareQuantityUsingPost } from "@/api/pictureController";

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
  // 分享次数+1
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
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}

.skeleton-card {
  border-radius: 8px;
  overflow: hidden;
  background: var(--bg-card);
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.picture-card {
  border-radius: 8px;
  overflow: hidden;
  background: var(--bg-card);
  transition: box-shadow 0.3s ease;
  cursor: pointer;
}

.picture-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.picture-card:hover .image-overlay {
  opacity: 1;
}

.image-wrapper {
  position: relative;
  overflow: hidden;
  background: var(--bg-image-placeholder);
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24px 12px 8px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.55));
  opacity: 0;
  transition: opacity 0.25s ease;
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
  padding: 2px 6px;
  border-radius: 4px;
  transition: background 0.2s;
}

.action-item:hover {
  background: rgba(255, 255, 255, 0.2);
}

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
