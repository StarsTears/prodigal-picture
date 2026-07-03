<template>
  <div id="pictureDetailView">
    <!--左图右信息 布局-->
    <a-row :gutter="[24, 24]" class="detail-layout">
      <!--左侧：图片展示-->
      <a-col :xs="24" :lg="15">
        <div class="image-panel" @dragstart="handleDragStart">
          <a-image :src="picture.url" class="hero-image" />
        </div>
      </a-col>

      <!--右侧：图片信息-->
      <a-col :xs="24" :lg="9">
        <div class="info-panel">
          <!--标题-->
          <h2 class="picture-title">{{ picture.name ?? '未命名' }}</h2>

          <!--上传者-->
          <div v-if="picture.user" class="author-card">
            <a-avatar :src="picture.user.userAvatar" :size="40" />
            <div class="author-info">
              <span class="author-name">{{ picture.user.userName }}</span>
<!--              <span class="author-label">上传者</span>-->
            </div>
          </div>

          <!--简介-->
          <p v-if="picture.introduction" class="picture-intro">{{ picture.introduction }}</p>

          <!--下载按钮-->
          <a-button v-if="isLogin" type="primary" block size="large" class="btn-download" @click="doDownload">
            <template #icon><DownloadOutlined /></template>
            免费下载原图
          </a-button>

          <!--元数据 + 统计 + 标签-->
          <div class="info-card">
            <!--元数据 两列-->
            <div class="meta-grid">
              <div class="meta-cell">
                <ColumnWidthOutlined class="meta-icon" />
                <span class="meta-label">分辨率</span>
                <span class="meta-val">{{ picture.picWidth }} × {{ picture.picHeight }}</span>
              </div>
              <div class="meta-cell">
                <PictureOutlined class="meta-icon" />
                <span class="meta-label">格式</span>
                <span class="meta-val">{{ picture.picFormat?.toUpperCase() ?? '-' }}</span>
              </div>
              <div class="meta-cell">
                <FileOutlined class="meta-icon" />
                <span class="meta-label">大小</span>
                <span class="meta-val">{{ formatSize(picture.picSize) }}</span>
              </div>
              <div class="meta-cell">
                <ColumnHeightOutlined class="meta-icon" />
                <span class="meta-label">比例</span>
                <span class="meta-val">{{ formatRatio(picture.picWidth, picture.picHeight) }}</span>
              </div>
              <div class="meta-cell">
                <AppstoreOutlined class="meta-icon" />
                <span class="meta-label">分类</span>
                <a-tag color="blue" size="small" class="meta-tag">{{ picture.category ?? '默认' }}</a-tag>
              </div>
              <div v-if="picture.picColor" class="meta-cell">
                <span class="meta-color-dot" :style="{ backgroundColor: toHexColor(picture.picColor) }" />
                <span class="meta-label">色系</span>
                <span class="meta-val">{{ toColorName(picture.picColor) }}</span>
              </div>
            </div>

            <!--统计-->
            <div class="stats-row">
              <span class="stats-num"><EyeOutlined /> {{ formatNumber(picture.viewQuantity ?? 0) }}</span>
              <span class="stats-num"><ShareAltOutlined /> {{ formatNumber(picture.shareQuantity ?? 0) }}</span>
              <span class="stats-num"><DownloadOutlined /> {{ formatNumber(picture.downloadQuantity ?? 0) }}</span>
            </div>

            <!--标签-->
            <div v-if="picture.tags?.length" class="tags-row">
              <a-tag v-for="tag in picture.tags" :key="tag" class="tag-pill">{{ tag }}</a-tag>
            </div>
          </div>

          <!--操作按钮-->
          <div class="action-row">
            <a-button v-if="isLogin" @click="(e) => doShare(picture, e)">
              <template #icon><ShareAltOutlined /></template>
              分享
            </a-button>
            <a-button v-if="canEditPicture" @click="doEdit">
              <template #icon><EditOutlined /></template>
              编辑
            </a-button>
            <a-dropdown v-if="isAdmin || canDeletePicture">
              <a-button><MoreOutlined /></a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item v-if="isAdmin" @click="handleReview(PIC_REVIEW_STATUS_ENUM.PASS)">
                    <CheckOutlined /> 审核通过
                  </a-menu-item>
                  <a-menu-item v-if="isAdmin" @click="handleReview(PIC_REVIEW_STATUS_ENUM.REJECT)">
                    <StopOutlined /> 拒绝
                  </a-menu-item>
                  <a-menu-item v-if="canDeletePicture" danger @click="doDelete">
                    <DeleteOutlined /> 删除
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
        </div>
      </a-col>
    </a-row>

    <ShareModal ref="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue"
import {
  deletePictureUsingPost,
  doPictureReviewUsingPost,
  getPictureVoByIdUsingPost,
  getTempDownloadUrlUsingPost,
  incrementShareQuantityUsingPost,
} from "@/api/pictureController"
import {
  DeleteOutlined,
  EditOutlined,
  DownloadOutlined,
  CheckOutlined,
  StopOutlined,
  ShareAltOutlined,
  EyeOutlined,
  MoreOutlined,
  ColumnWidthOutlined,
  PictureOutlined,
  FileOutlined,
  ColumnHeightOutlined,
  AppstoreOutlined,
} from '@ant-design/icons-vue'
import { message } from "ant-design-vue"
import { downloadImage, formatSize, formatNumber, handleDragStart, toHexColor, formatRatio, toColorName } from "@/utils/index"
import { useLoginUserStore } from "@/stores/loginUserStore"
import ACCESS_ENUM from "@/access/accessEnum"
import { useRouter } from "vue-router"
import { PIC_REVIEW_STATUS_ENUM } from "@/constants/picture"
import ShareModal from "@/components/ShareModal.vue"
import { SPACE_PERMISSION_ENUM } from "@/constants/space"

interface Props {
  id: string | number
  spaceId: string | number
}

const props = defineProps<Props>()
const picture = ref<API.PictureVO>({})

const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingPost({ isView: true }, { id: props.id, spaceId: props.spaceId })
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
  fetchPictureDetail()
})

function createPermissionChecker(permission: string) {
  return computed(() => (picture.value.permissionList ?? []).includes(permission))
}

const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

const doDownload = async () => {
  try {
    const res = await getTempDownloadUrlUsingPost({ id: picture.value.id, spaceId: picture.value.spaceId })
    if (res.code === 0 && res.data) {
      downloadImage(res.data, picture.value.name)
    } else {
      message.error('获取下载地址失败')
    }
  } catch (error) {
    message.error('获取下载地址失败')
  }
}

const loginUserStore = useLoginUserStore()
const isLogin = computed(() => {
  const loginUser = loginUserStore.loginUser
  return loginUser && loginUser.id
})

const isAdmin = computed(() => {
  const loginUser = loginUserStore.loginUser
  return loginUser && loginUser.userRole?.includes(ACCESS_ENUM.ADMIN || ACCESS_ENUM.SUPER_ADMIN)
})

const router = useRouter()
const doEdit = () => {
  router.push('/picture/add_picture?id=' + picture.value.id + '&spaceId=' + picture.value.spaceId)
}

const shareModalRef = ref(true)
const shareLink = ref<string>()
const doShare = (item: API.PictureVO, e: Event) => {
  e.stopPropagation()
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${item.spaceId}/${item.id}`
  incrementShareQuantityUsingPost({ id: item.id, spaceId: item.spaceId }).then(() => {
    item.shareQuantity = (item.shareQuantity || 0) + 1
  })
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}

const handleReview = async (reviewStatus: number) => {
  const reviewMessage = prompt("请输入审核信息")
  const res = await doPictureReviewUsingPost({
    id: picture.value.id,
    spaceId: picture.value.spaceId,
    reviewMessage,
    reviewStatus,
  })
  if (res.code === 0) {
    message.success("审核操作成功")
    fetchPictureDetail()
  } else {
    message.error("审核操作失败")
  }
}

const doDelete = async () => {
  const id = picture.value.id
  if (!id) return
  const res = await deletePictureUsingPost({ id, spaceId: picture.value.spaceId })
  if (res.code === 0) {
    message.success('删除成功')
    router.push('/')
  } else {
    message.error('删除失败，' + res.data?.msg)
  }
}
</script>

<style scoped>
#pictureDetailView {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 16px;
}

.detail-layout {
  align-items: center;
}

/* ========== 左侧图片面板 ========== */
.image-panel {
  background: #0d0d0d;
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 280px;
  padding: 20px;
}

.image-panel :deep(.ant-image) {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
}

.image-panel :deep(.ant-image-img) {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 6px;
}

/* ========== 右侧信息面板 ========== */
.info-panel {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 28px 24px;
}

.picture-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
  color: var(--text-primary);
  line-height: 1.3;
  word-break: break-word;
}

/* 上传者卡片 */
.author-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 18px;
  background: var(--bg-image-placeholder);
  border-radius: 12px;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.author-label {
  font-size: 11px;
  color: var(--text-secondary);
}

.picture-intro {
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.7;
  margin: 0 0 18px;
}

/* 下载按钮 */
.btn-download {
  margin-bottom: 18px;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
}

/* 信息卡片 */
.info-card {
  background: var(--bg-image-placeholder);
  border-radius: 12px;
  padding: 18px 16px 14px;
  margin-bottom: 18px;
}

/* 元数据 2列 */
.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px 20px;
  margin-bottom: 14px;
}

.meta-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-icon {
  color: var(--text-secondary);
  font-size: 13px;
  flex-shrink: 0;
}

.meta-label {
  font-size: 13px;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.meta-val {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  white-space: nowrap;
}

.meta-tag {
  font-size: 12px;
  line-height: 1;
}

.meta-color-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 1px solid rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

/* 统计 */
.stats-row {
  display: flex;
  gap: 18px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

.stats-num {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 标签 */
.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
  margin-top: 12px;
}

.tag-pill {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 10px;
}

/* 操作按钮 */
.action-row {
  display: flex;
  gap: 8px;
}

/* ========== 响应式 ========== */
@media (max-width: 992px) {
  .info-panel {
    margin-top: 8px;
  }

  .picture-title {
    font-size: 16px;
  }
}

@media (max-width: 576px) {
  .image-panel {
    min-height: 200px;
    max-height: 55vh;
  }
}
</style>
