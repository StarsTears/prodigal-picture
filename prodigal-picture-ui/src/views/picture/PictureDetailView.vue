<template>
  <!--图片详情-->
  <div id="pictureDetailView">
    <!-- 栅格布局 -->
    <a-row :gutter="[16,16]">
      <!-- 图片预览 -->
      <a-col :sm="24" :md="16" :xl="18">
        <a-card title="图片预览">
          <div class="imgView" @dragstart="handleDragStart">
            <a-image :src="picture.url"/>
          </div>

        </a-card>
      </a-col>
      <!-- 图片区域 -->
      <a-col :sm="24" :md="8" :xl="6">
        <a-card title="图片信息">
          <a-descriptions :column="1">
            <a-descriptions-item label="作者">
              <a-space>
                <a-avatar :size="24" :src="picture.user?.userAvatar"/>
                <div>{{ picture.user?.userName }}</div>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="图片名称">
              {{ picture.name ?? '未命名' }}
            </a-descriptions-item>
            <a-descriptions-item label="简介">
              {{ picture.introduction ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item v-if="picture.spaceId" label="所属空间">
              {{ picture.spaceId==0 ? '公共图库' :picture.spaceId }}
            </a-descriptions-item>
            <a-descriptions-item label="分类">
              {{ picture.category ?? '默认' }}
            </a-descriptions-item>
            <a-descriptions-item label="标签">
              <a-tag v-for="tag in picture.tags" :key="tag">
                {{ tag }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="格式">
              {{ picture.picFormat ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="宽度">
              {{ picture.picWidth ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="高度">
              {{ picture.picHeight ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="宽高比">
              {{ picture.picScale ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="大小">
              {{ formatSize(picture.picSize) }}
            </a-descriptions-item>
            <a-descriptions-item label="主色调">
              <a-space>
                {{ picture.picColor ?? '-' }}
                <div v-if="picture.picColor"
                  :style="{
                          backgroundColor: toHexColor(picture.picColor),
                          width: '16px',
                          height: '16px',
                          }"
                />
              </a-space>
            </a-descriptions-item>

          </a-descriptions>
          <a-space wrap>
            <a-button v-if="isLogin" :icon="h(DownloadOutlined)" type="primary" @click="doDownload">
              免费下载
            </a-button>
            <a-button v-if="canEditPicture" :icon="h(EditOutlined)" type="default" @click="doEdit">
              编辑
            </a-button>
            <a-button v-if="isLogin" type="primary" ghost @click="(e)=>doShare(picture, e)">
              分享
              <template #icon>
                <share-alt-outlined />
              </template>
            </a-button>

            <a-button v-if="isAdmin" :icon="h(CheckOutlined)" type="primary" @click="handleReview(PIC_REVIEW_STATUS_ENUM.PASS)">
              审核
            </a-button>
            <a-button v-if="isAdmin" :icon="h(SmileOutlined)" type="default" @confirm="handleReview(PIC_REVIEW_STATUS_ENUM.REJECT)">
              拒绝
            </a-button>
            <a-popconfirm v-if="canDeletePicture"
                          :icon="h(DeleteOutlined)"
                          okText="确定"
                          cancelText="取消"
                          title="Sure to delete?" @confirm="doDelete">
              <a-button danger>
                删除
                <template #icon>
                  <DeleteOutlined/>
                </template>
              </a-button>
            </a-popconfirm>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
    <ShareModal ref="shareModalRef" :link="shareLink"/>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref, h} from "vue";
import {deletePictureUsingPost, doPictureReviewUsingPost, getPictureVoByIdUsingPost} from "@/api/pictureController";
import {DeleteOutlined, EditOutlined, DownloadOutlined, CheckOutlined, SmileOutlined,ShareAltOutlined} from '@ant-design/icons-vue';
import {message} from "ant-design-vue";
import {downloadImage, formatSize, handleDragStart} from "@/utils/index";
import {useLoginUserStore} from "@/stores/loginUserStore";
import ACCESS_ENUM from "@/access/accessEnum";
import {useRouter} from "vue-router";
import {PIC_REVIEW_STATUS_ENUM} from "@/constants/picture";
import {toHexColor} from '@/utils/index'
import ShareModal from "@/components/ShareModal.vue";
import {SPACE_PERMISSION_ENUM} from "@/constants/space";

interface Props {
  id: string | number,
  spaceId: string | number
}

const props = defineProps<Props>()
const picture = ref<API.PictureVO>({})
// 获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingPost({
      isView:true
    },{
      id: props.id,
      spaceId: props.spaceId
    })
    if (res.data) {
      picture.value = res.data
    } else {
      message.error('获取图片详情数据失败，' + res.msg)
    }
  } catch (error) {
    message.error('获取图片详情数据失败，' + error.msg)
  }
}
// 页面加载时请求一次
onMounted(() => {
  fetchPictureDetail()
})

//----------------------------------团队空间的权限校验------------------------
// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (picture.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

/**
 * 下载图片
 */
const doDownload = () => {
  downloadImage(picture.value.url, picture.value.name)
}
/**
 * 判断当前用户是否具有编辑与删除权限
 */
const loginUserStore = useLoginUserStore()
//判断是否登录
const isLogin = computed(() => {
  let loginUser = loginUserStore.loginUser;
  return loginUser && loginUser.id
})

const isAdmin = computed(() => {
  let loginUser = loginUserStore.loginUser;
  return !loginUser && loginUser.userRole.includes(ACCESS_ENUM.ADMIN || ACCESS_ENUM.SUPER_ADMIN)
})

// const canEdit = computed(() => {
//   let loginUser = loginUserStore.loginUser;
//   //未登录不可编辑
//   if (!loginUser.id) {
//     return false;
//   }
//   //仅限本人与管理员可编辑
//   const user = picture.value.user || {}
//   return loginUser.id === user.id || loginUser.userRole.includes(ACCESS_ENUM.ADMIN || ACCESS_ENUM.SUPER_ADMIN);
// })

const router = useRouter();
const doEdit = () => {
  // 跳转编辑页面
  router.push('/picture/add_picture?id=' + picture.value.id+ '&spaceId=' + picture.value.spaceId)
}


//------------------------------- 分享 ---------------------------
// 分享弹窗引用
const shareModalRef = ref(true)
// 分享链接
const shareLink = ref<string>()
const doShare = (picture: API.PictureVO, e: Event) => {
  e.stopPropagation()
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.spaceId}/${picture.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}
//-----------------------------------审核图片----------------------
const handleReview = async (reviewStatus: number) => {
  const reviewMessage = prompt("请输入审核信息")
  const res = await doPictureReviewUsingPost({
    id: picture.value.id,
    spaceId:picture.value.spaceId,
    reviewMessage,
    reviewStatus
  })
  if (res.code === 0) {
    message.success("审核操作成功")
    // 刷新数据
    fetchPictureDetail()
  } else {
    message.error("审核操作失败")
  }
  console.log("审核图片")
}
//-----------------------------删除----------------------------
const doDelete = async () => {
  let id = picture.value.id;
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({
    id: id,
    spaceId: picture.value.spaceId
  })
  if (res.code === 0) {
    message.success('删除成功')
    //跳转到图片列表页
    router.push('/')
  } else {
    message.error('删除失败，' + res.data.msg)
  }
}
</script>

<style scoped>
#pictureDetailView {
  width: 80%;
  margin: 0 auto 40px;
}

#pictureDetailView .imgView{
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-height: 100px; /* 最小高度 */
  max-height: 80vh; /* 最大高度为视口的80% */
  overflow: hidden; /* 确保不会溢出 */
}

/* 图片样式：完全自适应 */
.imgView :deep(.ant-image) {
  display: block;
  width: 100%;
  height: 100%;
}

.imgView :deep(.ant-image .ant-image-img) {
  display: block;
  width: auto;
  height: auto;
  max-width: 100%;
  max-height: 70vh; /* 与容器一致 */
  object-fit: scale-down; /* 关键修改：使用scale-down */
  margin: 0 auto;
  transition: transform 0.3s ease;
}
</style>
