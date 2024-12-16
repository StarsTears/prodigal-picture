<template>
    <div id="addPictureBatchView">
      <h2 style="margin-bottom: 16px">批量创建图片</h2>
      <a-form layout="vertical" :model="formData" @finish="handleSubmit">
        <a-form-item label="关键词" name="searchText">
          <a-input v-model:value="formData.searchText" placeholder="请输入关键词" />
        </a-form-item>
        <a-form-item label="偏移量" name="offset">
          <a-input-number
            v-model:value="formData.offset"
            placeholder="请输入偏移量"
            style="min-width: 180px"
            :min="1"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="抓取数量" name="count">
          <a-input-number
            v-model:value="formData.count"
            placeholder="请输入数量"
            style="min-width: 180px"
            :min="1"
            :max="20"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="名称前缀" name="namePrefix">
          <a-input v-model:value="formData.namePrefix" placeholder="请输入名称前缀，会自动补充序号" />
        </a-form-item>
        <a-form-item label="分类" name="category">
          <a-input v-model:value="formData.category" placeholder="请输入分类" />
        </a-form-item>
        <a-form-item label="标签" name="tags">
          <a-input v-model:value="formData.tags" placeholder="请输入标签" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading">
            执行任务
          </a-button>
        </a-form-item>
      </a-form>
    </div>
</template>

<script setup lang="ts">
import {reactive, ref} from "vue";
import {message} from "ant-design-vue";
import {uploadPictureByBatchUsingPost} from "@/api/pictureController";
import {useRouter} from "vue-router";
const formData = reactive<API.PictureUploadByBatchDto>({
  count: 5,
})
const loading = ref(false)
/**
 * 提交表单
 * @param values
 */
const router = useRouter();
const handleSubmit = async (values: any) => {
  loading.value = true
  const res = await uploadPictureByBatchUsingPost({
    ...formData
  })
  if (res.code === 0 && res.data) {
    message.success(`创建成功,共${res.data}条`)
    //跳转到图片详情页
    router.push("/")
  } else {
    message.error('创建失败，' + res.msg)
  }
  loading.value = false
}

</script>

<style scoped>
#addPictureBatchView {
  max-width: 720px;
  margin: 0 auto;
}
.addPictureBatchView .action-bar{
  display: flex;
  justify-content: flex-end;/* 将内容推到右侧 */
}
</style>
