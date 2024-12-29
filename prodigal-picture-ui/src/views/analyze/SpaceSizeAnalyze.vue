<template>
  <div class="space-size-analyze">
    <a-card title="空间图库大小分析">
      <v-chart :option="options" style="height: 320px" :loading="loading"/>
    </a-card>
  </div>
</template>


<script setup lang="ts">
import VChart from "vue-echarts";
import "echarts";
import {computed, ref, watchEffect} from "vue";
import {analyzeSpaceSizeUsingPost} from "@/api/spaceAnalyzeController";
import {message} from "ant-design-vue";

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

const loading = ref(false)
const dataList = ref<API.SpaceSizeAnalyzeVO[]>([])
const fetchData = async () => {
  loading.value = true;
  const res = await analyzeSpaceSizeUsingPost({
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    spaceId: props.spaceId
  })
  if (res.code == 0 && res.data){
    dataList.value = res.data ?? []
  }else {
    message.error("获取数据失败, "+res.msg)
  }
  loading.value = false
}

/**
 * 监听变量，变量更新时，重新获取数据
 */
watchEffect(()=>{
  fetchData()
})

const options = computed(() => {
  const pieData = dataList.value.map((item) => ({
    name: item.sizeRange,
    value: item.count,
  }))

  return {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
    },
    legend: {
      top: 'bottom',
    },
    series: [
      {
        name: '图片大小',
        type: 'pie',
        radius: '50%',
        data: pieData,
      },
    ],
  }
})


</script>

<style scoped>

</style>
