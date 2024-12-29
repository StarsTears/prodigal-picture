<template>
  <div class="space-user-analyze">
    <a-card title="用户上传行为分析">
      <v-chart :option="options" style="height: 320px" :loading="loading"/>
      <template #extra>
        <a-space>
          <a-segmented v-model:value="timeDimension" :options="timeDimensionOptions"/>
          <a-input-search placeholder="请输入用户ID" v-model:value="userId" enter-button="Search" @search="doSearch"/>
        </a-space>
      </template>
    </a-card>
  </div>
</template>


<script setup lang="ts">
import VChart from "vue-echarts";
import "echarts";
import {computed, ref, watchEffect} from "vue";
import {analyzeSpaceUserUsingPost} from "@/api/spaceAnalyzeController";

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

//用户ID选项
const userId = ref<string>()
//时间维度选项
const timeDimension = ref<'day'|'week'|'month'|'year'>('day')
const timeDimensionOptions = [
  {
    label: '日',
    value: 'day',
  },
  {
    label: '周',
    value: 'week',
  },
  {
    label: '月',
    value: 'month',
  },
  {
    label: '年',
    value: 'year',
  },
]

const doSearch = (value: string) => {
  userId.value = value
}

const loading = ref(false)

const dataList = ref<API.SpaceUserAnalyzeVO[]>([])
const fetchData = async () => {
  loading.value = true;
  const res = await analyzeSpaceUserUsingPost({
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    spaceId: props.spaceId,
    timeDimension: timeDimension.value,
    userId: userId.value,
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
  const periods = dataList.value.map((item) => item.timeRange) // 时间区间
  const counts = dataList.value.map((item) => item.count) // 上传数量

  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: periods, name: '时间区间' },
    yAxis: { type: 'value', name: '上传数量' },
    series: [
      {
        name: '上传数量',
        type: 'line',
        data: counts,
        smooth: true, // 平滑折线
        emphasis: {
          focus: 'series',
        },
      },
    ],
  }
})

</script>

<style scoped>

</style>
