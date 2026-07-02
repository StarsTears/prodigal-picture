<template>
  <div id="pictureDetailView">
    <!--图片展示区-->
    <div class="image-hero" @dragstart="handleDragStart">
      <div class="image-container">
        <a-image :src="picture.url" class="hero-image" />
      </div>
    </div>

    <!--信息与操作栏-->
    <div class="info-bar">
      <div class="info-bar__left">
        <h2 class="picture-title">{{ picture.name ?? '未命名' }}</h2>
        <div class="picture-stats">
          <span class="stat-item">
            <EyeOutlined />
            {{ formatNumber(picture.viewQuantity ?? 0) }} 次浏览
          </span>
          <span class="stat-item">
            <ShareAltOutlined />
            {{ formatNumber(picture.shareQuantity ?? 0) }} 次分享
          </span>
          <span v-if="picture.picSize" class="stat-item">
            <FileOutlined />
            {{ formatSize(picture.picSize) }}
          </span>
        </div>
      </div>
      <div class="info-bar__right">
        <a-space :size="12">
          <a-button v-if="isLogin" type="primary" size="large" @click="doDownload">
            <template #icon><DownloadOutlined /></template>
            免费下载
          </a-button>
          <a-button v-if="isLogin" size="large" @click="(e) => doShare(picture, e)">
            <template #icon><ShareAltOutlined /></template>
            分享
          </a-button>
          <a-button v-if="canEditPicture" size="large" @click="doEdit">
            <template #icon><EditOutlined /></template>
            编辑
          </a-button>
          <a-dropdown v-if="isAdmin || canDeletePicture">
            <a-button size="large">
              <MoreOutlined />
            </a-button>
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
        </a-space>
      </div>
    </div>

    <!--色卡 + 详情区-->
    <a-row :gutter="[20, 20]" class="detail-section">
      <!--左侧：图片详情-->
      <a-col :xs="24" :lg="16">
        <div class="detail-card">
          <h3 class="detail-card__title">图片信息</h3>
          <p v-if="picture.introduction" class="picture-intro">{{ picture.introduction }}</p>
          <a-row :gutter="[16, 12]">
            <a-col :span="12">
              <div class="meta-item">
                <span class="meta-label">分类</span>
                <span class="meta-value">
                  <a-tag color="blue">{{ picture.category ?? '默认' }}</a-tag>
                </span>
              </div>
            </a-col>
            <a-col :span="12">
              <div class="meta-item">
                <span class="meta-label">格式</span>
                <span class="meta-value">{{ picture.picFormat?.toUpperCase() ?? '-' }}</span>
              </div>
            </a-col>
            <a-col :span="12">
              <div class="meta-item">
                <span class="meta-label">尺寸</span>
                <span class="meta-value">{{ picture.picWidth }} × {{ picture.picHeight }} px</span>
              </div>
            </a-col>
            <a-col :span="12">
              <div class="meta-item">
                <span class="meta-label">宽高比</span>
                <span class="meta-value">{{ picture.picScale }}</span>
              </div>
            </a-col>
          </a-row>
        </div>
      </a-col>

      <!--右侧：标签 + 色调-->
      <a-col :xs="24" :lg="8">
        <div class="detail-card">
          <h3 class="detail-card__title">标签</h3>
          <div class="tags-cloud" v-if="picture.tags?.length">
            <a-tag v-for="tag in picture.tags" :key="tag" class="tag-pill">{{ tag }}</a-tag>
          </div>
          <span v-else class="text-muted">暂无标签</span>
        </div>

        <div v-if="picture.picColor" class="detail-card color-card">
          <h3 class="detail-card__title">主色调</h3>
          <div class="color-preview">
            <div class="color-swatch" :style="{ backgroundColor: toHexColor(picture.picColor) }" />
            <span class="color-hex">{{ toHexColor(picture.picColor) }}</span>
          </div>
        </div>

        <div v-if="picture.user" class="detail-card user-card">
          <h3 class="detail-card__title">上传者</h3>
          <div class="user-info">
            <a-avatar :src="picture.user.userAvatar" :size="36" />
            <span class="user-name">{{ picture.user.userName }}</span>
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
  FileOutlined,
  MoreOutlined,
} from '@ant-design/icons-vue'
import { message } from "ant-design-vue"
import { downloadImage, formatSize, formatNumber, handleDragStart, toHexColor } from "@/utils/index"
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
  max-width: 1200px;
  margin: 0 auto 48px;
  padding: 0 16px;
}

/* ========== 图片展示区 ========== */
.image-hero {
  background: #0d0d0d;
  border-radius: 14px;
  overflow: hidden;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
  max-height: 75vh;
}

.image-container {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 8px;
}

.hero-image :deep(.ant-image) {
  display: block;
}

.hero-image :deep(.ant-image-img) {
  display: block;
  max-width: 100%;
  max-height: 72vh;
  object-fit: scale-down;
  border-radius: 6px;
}

/* ========== 信息与操作栏 ========== */
.info-bar {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
  padding: 0 4px;
}

.info-bar__left {
  flex: 1;
  min-width: 260px;
}

.picture-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 8px;
  color: var(--text-primary);
  line-height: 1.3;
}

.picture-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  color: var(--text-secondary);
  font-size: 13px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.info-bar__right {
  flex-shrink: 0;
}

/* ========== 详情卡片 ========== */
.detail-section {
  margin-top: 0;
}

.detail-card {
  background: var(--bg-card, #fff);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid var(--border-color, #f0f0f0);
}

.detail-card__title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 14px;
  color: var(--text-primary);
}

.picture-intro {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.7;
  margin: 0 0 16px;
}

/* 元数据项 */
.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 12px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.meta-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

/* 标签 */
.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-pill {
  font-size: 12px;
  padding: 2px 12px;
  border-radius: 12px;
}

.text-muted {
  color: var(--text-secondary);
  font-size: 13px;
}

/* 色调卡片 */
.color-card .color-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-swatch {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.color-hex {
  font-size: 16px;
  font-weight: 600;
  font-family: 'SF Mono', 'Fira Code', monospace;
  color: var(--text-primary);
}

/* 上传者 */
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .picture-title {
    font-size: 18px;
  }

  .info-bar {
    flex-direction: column;
  }

  .info-bar__right {
    width: 100%;
  }

  .info-bar__right :deep(.ant-space) {
    width: 100%;
  }

  .info-bar__right :deep(.ant-btn) {
    flex: 1;
  }
}
</style>
