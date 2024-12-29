<template>
  <div class="space-usage-analyze">
    <a-card title="空间资源使用分析">
      <a-flex gap="middle">
        <a-card title="存储空间" style="width: 50%">
          <div style="height: 220px; text-align: center">
            <h3>{{ formatSize(data.usedSize) }} / {{ data.maxSize ? formatSize(data.maxSize) : '无限制' }}</h3>
            <a-progress type="dashboard" :percent="data.sizeUsageRatio ?? 0" />
          </div>
        </a-card>
        <a-card title="图片数量" style="width: 50%">
          <div style="height: 220px; text-align: center">
            <h3>{{ data.usedCount }} / {{ data.maxCount ?? '无限制' }} </h3>
            <a-progress type="dashboard" :percent="data.countUsageRatio ?? 0" />
          </div>
        </a-card>
      </a-flex>

    </a-card>
  </div>
</template>


<script setup lang="ts">
import {onMounted, ref, watchEffect} from "vue";
import {message} from "ant-design-vue";
import {analyzeSpaceUsageUsingPost} from "@/api/spaceAnalyzeController";
import {formatSize} from "@/utils";

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

const loading = ref(true)
//图表数据
const data = ref<API.SpaceUsageAnalyzeVO[]>([]);

const fetchData = async () => {
  loading.value = true;
  const res = await analyzeSpaceUsageUsingPost({
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    spaceId: props.spaceId,
  });
  if (res.code === 0 && res.data) {
    data.value = res.data;
  }else {
    message.error("获取数据失败, "+res.msg);
  }
  loading.value = false;
};
/**
 * 监听变量，变量更新时，重新获取数据
 */
watchEffect(()=>{
  fetchData()
})
const options = {};
</script>

<style scoped>

</style>
