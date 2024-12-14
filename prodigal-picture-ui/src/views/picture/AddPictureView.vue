<template>
  <div class="addPicture">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? "修改图片" : "创建图片" }}
    </h2>
    <!-- 图片上传组件 -->
    <PictureUpload :picture="picture" :onSuccess="onSuccess"/>
    <!-- 图片展示组件 -->
    <a-form v-if="picture" layout="vertical" :model="pictureForm" @finish="handleSubmit">
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
import {onMounted, reactive, ref} from "vue";
import {message} from "ant-design-vue";
import {editPictureUsingPost, getPictureVoUsingGet, listPictureTagCategoryUsingGet} from "@/api/pictureController";
import {useRoute, useRouter} from "vue-router";

const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditDto>({})
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  // 图片上传成功，信息回填
  pictureForm.name = newPicture.name
  pictureForm.category = newPicture.category
  pictureForm.introduction = newPicture.introduction
  pictureForm.tags = newPicture.tags
}

/**
 * 提交表单
 * @param values
 */
const router = useRouter();
const handleSubmit = async (values: any) => {
  const pictureID = picture.value.id;
  if (!pictureID) return;

  const res = await editPictureUsingPost({
    id: pictureID,
    ...values
  })
  if (res.code === 0 && res.data) {
    message.success('创建成功')
    //跳转到图片详情页
    router.push({path:`/picture/${pictureID}`})
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
//获取信息
const route = useRoute();
//获取老数据
const getOldPicture = async () => {
  //获取id
  let id = route.query?.id;
  if (id) {
    const res = await getPictureVoUsingGet({id})
    if (res.code === 0 && res.data) {
      const data = res.data
      picture.value = data
      pictureForm.name = data.name
      pictureForm.category = data.category
      pictureForm.introduction = data.introduction
      pictureForm.tags = data.tags
    }
  }
}
onMounted(() => {
  getOldPicture()
})
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
</style>
