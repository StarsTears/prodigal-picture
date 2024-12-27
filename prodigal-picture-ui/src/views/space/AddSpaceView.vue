<template>
  <div id="addSpaceView">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? "修改空间" : "创建空间" }}
    </h2>
    <a-form layout="vertical" :model="spaceForm" @finish="handleSubmit">
      <a-form-item label="空间名称" name="spaceName">
        <a-input v-model:value="spaceForm.spaceName" placeholder="空间名称" allow-clear/>
      </a-form-item>
      <a-form-item v-if="false" label="空间所属人Id" name="userId">
        <a-input v-model:value="spaceForm.userId" placeholder="空间所属人Id" allow-clear/>
      </a-form-item>
      <a-form-item v-if="space" label="空间所属人" name="userName">
        <a-input v-model:value="spaceForm.userName" placeholder="空间所属人" disabled ="true" allow-clear/>
      </a-form-item>
      <a-form-item label="空间级别" name="spaceLevel">
        <a-select v-model:value="spaceForm.spaceLevel"
                  :options="SPACE_LEVEL_OPTIONS"
                  placeholder="输入空间级别"
                  style="min-width: 180px"
                  @change="handleSpaceLevelChange"
                  allow-clear/>
      </a-form-item>
      <a-form-item label="空间大小(MB)" name="maxSize">
        <a-input-number
          v-model:value="spaceForm.maxSize"
          placeholder="请输入数量"
          style="min-width: 180px"
          :min="spaceForm.maxSize/10"
          :max="spaceForm.maxSize*10"
          :disabled ="isEditable"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="图片数量" name="maxCount">
        <a-input-number
          v-model:value="spaceForm.maxCount"
          placeholder="输入图片数量"
          style="min-width: 180px"
          :min="spaceForm.maxCount/10"
          :max="spaceForm.maxCount*10"
          :disabled ="isEditable"
          allow-clear
        />
      </a-form-item>
      <a-form-item class="action-bar">
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading">
          {{ route.query?.id ? "修改" : "创建" }}
        </a-button>
      </a-form-item>
    </a-form>
    <a-card title="空间级别介绍">
      <a-typography-paragraph>
        * 目前仅支持开通普通版，如需升级空间，请联系
        <a href="" target="_blank">程序员Prodigal</a>。
      </a-typography-paragraph>
      <a-typography-paragraph v-for="spaceLevel in spaceLevelList">
        {{ spaceLevel.text }}： 大小 {{ formatSize(spaceLevel.maxSize) }}，
        数量 {{ spaceLevel.maxCount }}
      </a-typography-paragraph>
    </a-card>

  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, reactive, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {message} from "ant-design-vue";
import {SPACE_LEVEL_ENUM, SPACE_LEVEL_OPTIONS} from "@/constants/space";
import {formatSize} from "@/utils/index";
import {
  addSpaceUsingPost, getSpaceVoByIdUsingGet,
  listSpaceLevelUsingGet,
  updateSpaceUsingPost
} from "@/api/spaceController";
import {useLoginUserStore} from "@/stores/loginUserStore";
import ACCESS_ENUM from "@/access/accessEnum";
const loginUserStore = useLoginUserStore()

const route = useRoute();
const space = ref<API.SpaceVO>();
const spaceForm = reactive<API.SpaceAddDto | API.SpaceUpdateDto>({
  userId: '',
  userName: '',
  spaceName: '',
  spaceLevel: SPACE_LEVEL_ENUM.COMMON,
  maxSize: 100.00,
  maxCount: 100
})

const spaceLevelList = ref<API.SpaceLevel[]>([])

// 获取空间级别
const fetchSpaceLevelList = async () => {
  const res = await listSpaceLevelUsingGet()
  if (res.code === 0 && res.data) {
    spaceLevelList.value = res.data
  } else {
    message.error('加载空间级别失败，' + res.msg)
  }
}

onMounted(() => {
  fetchSpaceLevelList()
})

 const handleSpaceLevelChange=()=>{
   const loginUser = loginUserStore?.loginUser;
   if (loginUser?.userRole.includes(ACCESS_ENUM.ADMIN || ACCESS_ENUM.SUPER_ADMIN)) {
     spaceForm.maxCount = spaceLevelList.value.find(l => l.value === spaceForm.spaceLevel)?.maxCount ?? 0
     spaceForm.maxSize = formatSize(spaceLevelList.value.find(l => l.value === spaceForm.spaceLevel)?.maxSize ?? 0)
     isEditable.value = false
   }else{
     spaceForm.spaceLevel = 0;
     message.warn('请联系管理员创建高级空间···')
   }
 }

// 计算属性，用于控制字段是否可编辑
const isEditable = ref(true)
// const handleSpaceEditChange = computed(() => {
//   const loginUser = loginUserStore?.loginUser;
//   if (!loginUser?.userRole.includes(ACCESS_ENUM.ADMIN || ACCESS_ENUM.SUPER_ADMIN)) {
//     isEditable.value = false
//     message.warn('请联系管理员创建高级空间···')
//   }
// });

const loading = ref(false)
/**
 * 提交表单
 * @param values
 */
const router = useRouter();
const handleSubmit = async (values: any) => {
  //有空间ID，则为修改
  const spaceID = space.value?.id;
  loading.value = true
  let res;
  if (spaceID) {
    res = await updateSpaceUsingPost({
      id: spaceID,
      ...values
    })
  } else {
    res = await addSpaceUsingPost({
      ...spaceForm
    })
  }
  if (res.code === 0 && res.data) {
    message.success('操作成功')
    //跳转到空间详情页
    router.push({path: `/space/${spaceID ?? res.date}`})
  } else {
    message.error('操作失败，' + res.msg)
  }
  loading.value = false
}

//获取老数据
const getOldSpace = async () => {
  //获取id
  let id = route.query?.id;
  if (id) {
    const res = await getSpaceVoByIdUsingGet({id})
    console.log(res)
    if (res.code === 0 && res.data) {
      const data = res.data
      space.value = data
      spaceForm.userId = data.userId
      spaceForm.userName = data.user.userName
      spaceForm.spaceName = data.spaceName
      spaceForm.spaceLevel = data.spaceLevel
      spaceForm.maxSize = formatSize(data.maxSize)
      spaceForm.maxCount = data.maxCount
    }
  }
}
onMounted(() => {
  getOldSpace()
})
</script>

<style scoped>
#addSpaceView {
  max-width: 720px;
  margin: 0 auto;
}
/*.addSpaceView .action-bar{*/
/*  display: flex;*/
/*  justify-content: flex-end;!* 将内容推到右侧 *!*/
/*}*/
</style>
