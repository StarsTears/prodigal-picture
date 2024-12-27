<template>
  <a-modal class="image-ai-outpainting"
           v-model:open="open"
           title="AI 扩图"
           :footer="false"
           @cancel="closeModal">
    <a-row gutter="16">
      <a-col span="12">
        <h4>原始图片</h4>
        <img :src="picture?.url" :alt="picture?.name" style="max-width: 100%"/>
      </a-col>
      <a-col span="12">
        <h4>扩图结果</h4>
        <img
          v-if="resultImageUrl"
          :src="resultImageUrl"
          :alt="picture?.name"
          style="max-width: 100%"
        />
      </a-col>
    </a-row>
    <div style="margin-bottom: 16px"/>
    <a-flex gap="16" justify="center">
      <a-button type="primary" ghost :loading="!!taskId" @click="createTask">生成图片</a-button>
<!--      <a-button type="primary" :loading="uploadLoading" @click="handleUpload">应用结果</a-button>-->
      <a-popconfirm okText="覆盖原图"
                    cancelText="生成新图"
                    title="请选择你想执行的结果"
                    @confirm="handleUpload(true)"
                    @cancel="handleUpload(false)"
      >
        <a-button type="primary" :loading="uploadLoading">应用结果</a-button>
      </a-popconfirm>

    </a-flex>

  </a-modal>
</template>

<script setup lang="ts">
import {onUnmounted, ref} from 'vue';
import {
  createPictureOutPaintingTaskUsingPost,
  getPictureOutPaintingTaskUsingGet,
  uploadPictureByUrlUsingPost
} from "@/api/pictureController";
import {message} from "ant-design-vue";

interface Props {
  picture?: API.PictureVO;
  spaceId?: number;
  onSuccess?: (newPicture: API.PictureVO) => void;
}

//指定初始值
const props = defineProps<Props>();

const open = ref<boolean>(false);
//该函数需传递给父组件，用于打开弹窗
const openModal = (e: MouseEvent) => {
  open.value = true;
};
//将 onModal 函数暴露给父组件
defineExpose({
  openModal,
})
const closeModal = () => {
  open.value = false;
};
//存储ai 生成图片
const resultImageUrl = ref<string>()
// 任务 id
let taskId = ref<string>()
/**
 * 创建任务
 */
const createTask = async () => {
  if (!props.picture?.id) {
    return
  }
  // taskId.value='cd6db3b9-50a6-4a50-a9ef-abe1739cac64'
  startPolling()
  const res = await createPictureOutPaintingTaskUsingPost({
    pictureId: props.picture.id,
    // 可以根据需要设置扩图参数
    parameters: {
      xScale: 2,
      yScale: 2,
    },
  })
  if (res.code === 0 && res.data) {
    message.success('创建任务成功，请耐心等待，不要退出界面')
    console.log(res.data.output.taskId)
    taskId.value = res.data.output.taskId
    // 开启轮询
    startPolling()
  } else {
    message.error('创建任务失败，' + res.msg)
  }
}
// 轮询定时器
let pollingTimer: NodeJS.Timeout = null

// 清理轮询定时器
const clearPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
    taskId.value = null
  }
}

// 开始轮询
const startPolling = () => {
  if (!taskId.value) return

  pollingTimer = setInterval(async () => {
    try {
      const res = await getPictureOutPaintingTaskUsingGet({
        taskId: taskId.value,
      })
      if (res.code === 0 && res.data) {
        const taskResult = res.data.output
        if (taskResult.taskStatus === 'SUCCEEDED') {
          message.success('扩图任务成功')
          resultImageUrl.value = taskResult.outputImageUrl
          clearPolling()
        } else if (taskResult.taskStatus === 'FAILED') {
          message.error('扩图任务失败,'+taskResult.message)
          clearPolling()
          console.log(taskResult.message)
        }
      }
    } catch (error) {
      console.error('轮询任务状态失败', error)
      message.error('检测任务状态失败，请稍后重试')
      clearPolling()
    }
  }, 3000) // 每隔 3 秒轮询一次
}

// 清理定时器，避免内存泄漏
onUnmounted(() => {
  clearPolling()
})

/**
 * 上传图片
 */
const uploadLoading = ref<boolean>(false)
const handleUpload = async (edit: boolean) => {
  uploadLoading.value = true
  try {
    const params: API.PictureUploadDto = {
      fileUrl: resultImageUrl.value,
      spaceId: props.spaceId,
    }
    if (props.picture && edit) {
      params.id = props.picture.id
      message.warn("编辑-覆盖原图")
    }else{
      message.warn("生成新图")
    }
    console.log("上传图片参数，"+JSON.stringify(params))
    const res = await uploadPictureByUrlUsingPost(params)
    if (res.code === 0 && res.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data)
      // 关闭弹窗
      closeModal()
    } else {
      message.error('图片上传失败，' + res.msg)
    }
  } catch (error) {
    message.error('图片上传失败')
  } finally {
    uploadLoading.value = false
  }
}

</script>

<style>
.image-ai-outpainting {
  text-align: center;
}
</style>
