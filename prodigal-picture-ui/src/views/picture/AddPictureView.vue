<template>
  <div class="addPicture">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? "修改图片" : "创建图片" }}
    </h2>
    <a-typography-paragraph v-if="spaceId" type="secondary">
      保存至空间：<a :href="`/space/${spaceId}`" target="_blank">{{ spaceId }}</a>
    </a-typography-paragraph>
    <!-- 图片上传组件 -->
<!--    <PictureUpload :picture="picture" :onSuccess="onSuccess"/>-->
    <!-- 选择上传方式 -->
    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="file" tab="文件上传">
        <PictureUpload :picture="picture" :spaceId = "spaceId" :onSuccess="onSuccess" />
      </a-tab-pane>
      <a-tab-pane key="url" tab="URL 上传" force-render>
        <UrlPictureUpload :picture="picture" :spaceId = "spaceId" :onSuccess="onSuccess" />
      </a-tab-pane>
    </a-tabs>
    <!-- 图片操作 -->
    <div v-if="picture" class="edit-ber">
      <a-space>
        <a-button type="primary" ghost :icon="h(FullscreenOutlined)" @click="doAiOutPainting">AI 扩图</a-button>
      </a-space>
      <AiOutPainting ref="aiOutPaintingModalRef" :picture="picture" :spaceId="spaceId" :onSuccess="onAiOutPaintingSuccess"/>
    </div>

    <!-- 图片展示组件 -->
    <a-form v-if="picture" layout="vertical" :model="pictureForm" @finish="handleSubmit">
      <a-form-item label="图片所属用户Id" name="userId" v-if="false">
        <a-input v-model:value="pictureForm.userId" placeholder="图片所属用户ID" allow-clear/>
      </a-form-item>
      <a-form-item label="图片名称" name="name">
        <a-input v-model:value="pictureForm.name" placeholder="输入图片名称" allow-clear/>
      </a-form-item>
      <a-form-item label="简介" name="introduction">
        <a-textarea v-model:value="pictureForm.introduction" placeholder="输入简介" :auto-size="false" allow-clear/>
      </a-form-item>
      <a-form-item label="分类" name="category">
        <a-auto-complete v-model:value="pictureForm.category" placeholder="输入分类" :options="categoryOptions"/>
      </a-form-item>
      <a-form-item label="标签" name="tags">
        <a-select v-model:value="pictureForm.tags" mode="tags" placeholder="输入标签" :options="tagOptions"/>
      </a-form-item>
      <a-form-item class="action-bar">
        <a-button type="primary" html-type="submit">  {{ route.query?.id ? "修改" : "创建" }}</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from "@/views/picture/PictureUpload.vue";
import UrlPictureUpload from "@/views/picture/UrlPictureUpload.vue";
import AiOutPainting from "@/components/AiOutPainting.vue";
import {h,computed, onMounted, reactive, ref} from "vue";
import {message} from "ant-design-vue";
import {FullscreenOutlined} from '@ant-design/icons-vue';
import {
  editPictureUsingPost,
  getPictureVoByIdUsingPost,
  listPictureTagCategoryUsingGet
} from "@/api/pictureController";
import {useRoute, useRouter} from "vue-router";
const route = useRoute();
const router = useRouter();
const uploadType = ref<'file' | 'url'>('file')

const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditDto>({})
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  // 图片上传成功，信息回填
  pictureForm.name = newPicture.name
  pictureForm.category = newPicture.category
  pictureForm.introduction = newPicture.introduction
  pictureForm.tags = newPicture.tags
  pictureForm.userId = newPicture.userId
  pictureForm.spaceId = newPicture.spaceId
}

/**
 * 提交表单
 * @param values
 */
// 空间 id
const spaceId = computed(() => {
  return route.query?.spaceId
})

const handleSubmit = async (values: any) => {
  const pictureID = picture.value.id;
  console.log("图片id："+pictureID)
  if (!pictureID) return;

  const res = await editPictureUsingPost({
    id: pictureID,
    spaceId: spaceId.value,
    ...values
  })
  if (res.code === 0 && res.data) {
    message.success('创建成功')
    //跳转到图片详情页
    router.push({path:`/picture/${spaceId.value}/${pictureID}`})
  } else {
    message.error('创建失败，' + res.msg)
  }
}
/**
 * 获取分类、标签
 */
const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.code === 0 && res.data) {
    categoryOptions.value = (res.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data
      }
    })
    tagOptions.value = (res.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data
      }
    })
  }else{
    message.error('获取标签分类列表失败'+res.msg)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

//获取老数据
const getOldPicture = async () => {
  //获取id
  let id = route.query?.id;
  let spaceId = route.query?.spaceId;
  if (id) {
    const res = await getPictureVoByIdUsingPost({
      id:id,
      spaceId:spaceId
    })
    if (res.code === 0 && res.data) {
      const data = res.data
      picture.value = data
      pictureForm.name = data.name
      pictureForm.category = data.category
      pictureForm.introduction = data.introduction
      pictureForm.tags = data.tags
      pictureForm.userId = data.userId
    }
  }
}
onMounted(() => {
  getOldPicture()
})
//---------------------------AI 扩图-------------------------------------
const aiOutPaintingModalRef = ref()
const doAiOutPainting = () => {
  aiOutPaintingModalRef.value.openModal()
}
const onAiOutPaintingSuccess = (newPicture:API.PictureVO) => {
  picture.value = newPicture
}
</script>

<style scoped>
.addPicture {
  max-width: 720px;
  margin: 0 auto;
}
.addPicture .action-bar{
  display: flex;
  justify-content: flex-end;/* 将内容推到右侧 */
}
.addPicture .edit-ber{
  text-align: center;
  margin: 16px 0;
}
</style>
