<template>
  <div class="space-tag-analyze">
    <a-card title="空间图库标签分析">
      <v-chart :option="options" style="height: 320px" :loading="loading"/>
    </a-card>
  </div>
</template>


<script setup lang="ts">
import VChart from "vue-echarts";
import "echarts";
import 'echarts-wordcloud';
import {computed, ref, watchEffect} from "vue";
import {analyzeSpaceTagUsingPost} from "@/api/spaceAnalyzeController";
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
const dataList = ref<API.SpaceTagAnalyzeVO[]>([])
const fetchData = async () => {
  loading.value = true;
  const res = await analyzeSpaceTagUsingPost({
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
  const tagData = dataList.value.map((item) => ({
    name: item.tag,
    value: item.count,
  }))

  return {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => `${params.name}: ${params.value} 次`,
    },
    series: [
      {
        type: 'wordCloud',
        gridSize: 10,
        sizeRange: [12, 50], // 字体大小范围
        rotationRange: [-90, 90],
        shape: 'circle',
        textStyle: {
          color: () =>
            `rgb(${Math.round(Math.random() * 255)}, ${Math.round(
              Math.random() * 255,
            )}, ${Math.round(Math.random() * 255)})`, // 随机颜色
        },
        data: tagData,
      },
    ],
  }
})


</script>

<style scoped>

</style>
