<template>
  <div class="batch-edit-picture">
    <a-modal v-model:open="open"
             title="批量编辑图片"
             :footer="false"
             @cancel="closeModal">
      <a-typography-paragraph style="color: red"> * 只对当前页面的图片生效</a-typography-paragraph>
      <!--表单项-->
      <a-form layout="vertical" :model="formData" @finish="handelSubmit">
        <a-form-item label="分类" name="category">
          <a-auto-complete
            v-model:value="formData.category"
            style="min-width: 180px"
            :options="categoryOptions"
            placeholder="请输入分类"
            allowClear
          />
        </a-form-item>
        <a-form-item label="标签" name="tags">
          <a-select
            v-model:value="formData.tags"
            style="min-width: 180px"
            :options="tagOptions"
            mode="tags"
            placeholder="请输入标签"
            allowClear
          />
        </a-form-item>
        <a-form-item label="命名规则" name="nameRule">
          <a-input v-model:value="formData.nameRule" placeholder="请输入命名规则，输入{序号} 可动态生成" allow-clear/>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">提交</a-button>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from "vue";
import {message} from "ant-design-vue";
import {editPictureByBatchUsingPost, listPictureTagCategoryUsingGet} from "@/api/pictureController";
import {reactive} from "vue";

//定义组件属性
interface Props {
  pictureList: API.PictureVO[];
  spaceId: number;
  onSuccess: () => void;
}

//给组件指定初始值
const props = withDefaults(defineProps<Props>(), {})

const open = ref<boolean>(false);
//该函数需传递给父组件，用于打开弹窗
const openModal = () => {
  open.value = true;
};
//关闭对话框
const closeModal = () => {
  // 取消所有对象的值
  Object.keys(formData).forEach((key) => {
    formData[key] = undefined
  })
  open.value = false;
};

//将 onModal 函数暴露给父组件
defineExpose({
  openModal,
})

const formData = reactive<API.PictureEditByBatchDto>({
  category: '',
  tags: [],
  nameRule: '',
})
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
  } else {
    message.error('获取标签分类列表失败' + res.msg)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

// 提交表单时处理
const handelSubmit = async (values: any) => {
  if (!props.pictureList) {
    return
  }
  const res = await editPictureByBatchUsingPost({
    pictureIdList: props.pictureList.map((picture) => picture.id),
    spaceId: props.spaceId,
    ...values,
  })
  if (res.code === 0 && res.data) {
    message.success('操作成功')
    closeModal()
    props.onSuccess?.()
  } else {
    message.error('操作失败，' + res.msg)
  }
}

</script>

<style scoped>

</style>
