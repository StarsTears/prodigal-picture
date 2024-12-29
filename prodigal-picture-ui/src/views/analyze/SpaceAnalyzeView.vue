<template>
  <div class="space-analyze">
    <h2>
      空间图库分析-
      <span v-if="queryAll">全部空间</span>
      <span v-else-if="queryPublic">公共图库</span>
      <span v-else>
        <a :href="`/space/${spaceId}`">空间ID:{{ spaceId }}}</a>
      </span>
    </h2>
    <a-row :gutter="[16,16]">
      <!-- 空间使用分析 -->
      <a-col :xs="24" :md="12">
        <SpaceUsageAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic"/>
      </a-col>
      <!-- 空间使用排行分析 -->
      <a-col :xs="24" :md="12">
        <SpaceRankAnalyze v-fi="isAdmin" :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic"/>
      </a-col>
      <!-- 用户上传行为分析 -->
      <a-col :xs="24" :md="12">
        <SpaceUserAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic"/>
      </a-col>
      <!-- 空间分类分析 -->
      <a-col :xs="24" :md="12">
        <SpaceCategoryAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic"/>
      </a-col>
      <!-- 标签分析 -->
      <a-col :xs="24" :md="12">
        <SpaceTagAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic"/>
      </a-col>
      <!-- 图片大小分段分析 -->
      <a-col :xs="24" :md="12">
        <SpaceSizeAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic"/>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import SpaceUsageAnalyze from "@/views/analyze/SpaceUsageAnalyze.vue";
import SpaceUserAnalyze from "@/views/analyze/SpaceUserAnalyze.vue";
import SpaceCategoryAnalyze from "@/views/analyze/SpaceCategoryAnalyze.vue";
import SpaceTagAnalyze from "@/views/analyze/SpaceTagAnalyze.vue";
import SpaceSizeAnalyze from "@/views/analyze/SpaceSizeAnalyze.vue";
import SpaceRankAnalyze from "@/views/analyze/SpaceRankAnalyze.vue";
import {computed} from "vue";
import {useRoute} from "vue-router";
import {useLoginUserStore} from "@/stores/loginUserStore";


const route = useRoute()
//----------------------------------插叙参数-------------------------
//空间ID
const spaceId = computed(() => {
  return route.query?.spaceId as string
})
//是否查询所有
const queryAll = computed(() => {
  return !!route.query?.queryAll
})
//是否查询公共空间
const queryPublic = computed(() => {
  return !!route.query?.queryPublic
})

const loginUserStore = useLoginUserStore()
const loginUser = loginUserStore.loginUser

const isAdmin = computed(() => {
  return loginUser.userRole === 'admin'
})

</script>

<style scoped>
.space-analyze{
  margin-bottom:16px ;
}
</style>
