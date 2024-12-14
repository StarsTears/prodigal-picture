<template>
  <div class="picture-upload">
    <a-upload
      list-type="picture-card"
      :show-upload-list="false"
      :custom-request="handleUpload"
      :before-upload="beforeUpload"
      :progress="progress"
    >
      <img v-if="picture?.url" :src="picture?.url" alt="avatar"/>
      <div v-else>
        <loading-outlined v-if="loading"></loading-outlined>
        <plus-outlined v-else></plus-outlined>
        <div class="ant-upload-text">点击或拖拽上传图片</div>
      </div>
    </a-upload>
  </div>
</template>

<script lang="ts" setup>
import {ref} from 'vue';
import {PlusOutlined, LoadingOutlined} from '@ant-design/icons-vue';
import {message} from 'ant-design-vue';
import type {UploadChangeParam, UploadProps} from 'ant-design-vue';
import {updatePictureUsingPost, uploadPictureUsingPost} from "@/api/pictureController";

//图片上传进度条
const progress: UploadProps['progress'] = {
  strokeColor: {
    '0%': '#108ee9',
    '100%': '#87d068',
  },
  strokeWidth: 3,
  format: percent => `${parseFloat(percent.toFixed(2))}%`,
  class: 'test',
};

interface Props {
  picture?: API.PictureVO
  onSuccess?: (newPicture: API.PictureVO) => void;
}

//定义属性
const props = defineProps<Props>();
/**
 * 上传图片
 */
const handleUpload=async ({file}:any)=>{
  loading.value = true;
  try {
    const params = props.picture ? {id:props.picture.id} : {}
    const res = await uploadPictureUsingPost(params,{}, file);
    if (res.code === 0 && res.data) {
      message.success("图片上传成功");
      //将上传成功的图片信息返回给父组件 addPicture
      props.onSuccess?.(res.data)
    }else{
      message.error("图片上传失败，" + res.msg);
    }
  }catch (error){
    console.error('图片上传失败',error)
    message.error("图片上传失败，" + error.message);
  }
  loading.value = false;
}


function getBase64(img: Blob, callback: (base64Url: string) => void) {
  const reader = new FileReader();
  reader.addEventListener('load', () => callback(reader.result as string));
  reader.readAsDataURL(img);
}

const loading = ref<boolean>(false);

/**
 * 上传前的校验
 * @param file
 */
const beforeUpload = (file: UploadProps['fileList'][number]) => {
  //校验图片格式
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    message.error('You can only upload JPG file!');
  }
  //校验图片大小
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error('Image must smaller than 2MB!');
  }
  return isJpgOrPng && isLt2M;
};
</script>
<style scoped>
.picture-upload :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  min-height: 152px;
  min-width: 152px;
}

.picture-upload img {
  max-width: 100%;
  max-height: 480px;
}

.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}

</style>
