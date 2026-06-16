<template>
  <div class="search-picture">
    <a-page-header
      title="以图搜图"
      subtitle="智能识别图片内容，多平台查找相似图片"
      @back="() => $router.back()"
    />

    <a-row :gutter="24">
      <!-- 原图区域 -->
      <a-col :xs="24" :md="8" :lg="6">
        <a-card title="原图" :bordered="false" class="source-card">
          <div class="source-image-wrapper">
            <a-image :src="picture.url" :preview="true" fallback="data:image/svg+xml;base64,..." />
          </div>
          <a-descriptions
            v-if="picture.name"
            :column="1"
            size="small"
            style="margin-top: 12px"
          >
            <a-descriptions-item label="名称">{{ picture.name }}</a-descriptions-item>
            <a-descriptions-item v-if="picture.picWidth && picture.picHeight" label="尺寸">
              {{ picture.picWidth }} × {{ picture.picHeight }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>

      <!-- 搜索结果区域 -->
      <a-col :xs="24" :md="16" :lg="18">
        <a-card :bordered="false">
          <template #title>
            <div class="result-header">
              <span>识图结果</span>
              <a-tag v-if="!loading && dataList.length > 0" color="blue">
                共 {{ dataList.length }} 条
              </a-tag>
            </div>
          </template>

          <!-- 加载中 -->
          <div v-if="loading" class="loading-wrapper">
            <a-spin size="large" tip="正在搜索相似图片..." />
          </div>

          <!-- 空结果 -->
          <a-empty
            v-else-if="dataList.length === 0"
            description="未找到相似图片，换个图片试试吧"
            style="padding: 60px 0"
          />

          <!-- 结果网格 -->
          <a-list
            v-else
            :grid="{ gutter: 12, xs: 2, sm: 3, md: 3, lg: 4, xl: 4, xxl: 5 }"
            :data-source="dataList"
          >
            <template #renderItem="{ item }">
              <a-list-item style="padding: 0">
                <a
                  :href="item.fromUrl"
                  target="_blank"
                  :title="item.fromUrl"
                  class="result-link"
                >
                  <a-card hoverable class="result-card" size="small">
                    <div class="result-image-wrapper">
                      <a-image
                        :src="item.thumbUrl"
                        :preview="false"
                        fallback="data:image/svg+xml;base64,..."
                        class="result-image"
                      />
                    </div>
                    <div class="result-source">
                      <LinkOutlined style="margin-right: 4px; font-size: 12px" />
                      <span class="source-text">查看来源</span>
                    </div>
                  </a-card>
                </a>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import {
  getPictureVoByIdUsingPost,
  searchImageUsingPost,
} from "@/api/pictureController.js";
import { message } from "ant-design-vue";
import { useRoute } from "vue-router";
import { LinkOutlined } from "@ant-design/icons-vue";

const route = useRoute();

const pictureId = computed(() => route.query?.pictureId);
const spaceId = computed(() => route.query?.spaceId);

const picture = ref<API.Picture>({});
const dataList = ref<API.ImageSearchResult[]>([]);
const loading = ref(false);

const getOldPicture = async () => {
  try {
    const res = await getPictureVoByIdUsingPost(
      { isView: false },
      { id: pictureId.value, spaceId: spaceId.value }
    );
    if (res.data) {
      picture.value = res.data;
    } else {
      message.error("获取原图信息失败");
    }
  } catch {
    message.error("获取原图信息失败，请稍后重试");
  }
};

const fetchSearchResult = async () => {
  loading.value = true;
  try {
    const res = await searchImageUsingPost({
      pictureId: pictureId.value,
      spaceId: spaceId.value,
    });
    if (res.code === 0 && res.data) {
      dataList.value = res.data;
    } else {
      message.warning("未找到相似图片");
    }
  } catch {
    message.error("搜索失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  getOldPicture();
  fetchSearchResult();
});
</script>

<style scoped>
.search-picture {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 16px;
}

.source-card {
  position: sticky;
  top: 16px;
}

.source-image-wrapper {
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
}

.source-image-wrapper :deep(.ant-image) {
  width: 100%;
  display: block;
}

.source-image-wrapper :deep(.ant-image-img) {
  width: 100%;
  max-height: 360px;
  object-fit: contain;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.result-link {
  display: block;
  text-decoration: none;
}

.result-card {
  transition: transform 0.2s ease;
}

.result-card:hover {
  transform: translateY(-2px);
}

.result-image-wrapper {
  height: 180px;
  overflow: hidden;
  border-radius: 6px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-image-wrapper :deep(.ant-image) {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-image-wrapper :deep(.ant-image-img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.result-source {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 0 2px;
  color: #8c8c8c;
  font-size: 12px;
}

.result-card:hover .result-source {
  color: #1677ff;
}
</style>
