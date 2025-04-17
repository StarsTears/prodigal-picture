<template>
  <div class="timeline-view">
    <div class="timeline-header">
      <h2 style="margin-left: 50px;color: cornflowerblue">GitHub Commit History</h2>
      <a-button type="primary" @click="refreshData" style="margin-left: 700px">刷新</a-button>
    </div>
    <div :spinning="loading" style="padding-top: 100px">
      <a-timeline mode="left" :reverse="false" class="timeline-container">
        <a-timeline-item v-for="(item, index) in timelineData" :key="index" class="timeline-item">
          <div class="date-label">
            <span>{{ formatDate(item.date) }}</span>
          </div>
          <a-card hoverable class="timeline-item-content">
            <div class="commit-header">
<!--              <a-avatar :src="getGravatarUrl(item.authorEmail)">{{ item.authorName}}</a-avatar>-->
              <a-avatar>{{ item.authorName}}</a-avatar>
              <div class="commit-info">
                <div class="commit-author">
                  <span class="author-name">{{ item.authorName }}</span>
<!--                  <span class="author-email">{{ item.authorEmail }}</span>-->
                </div>
                <div class="commit-date">{{ formatDate(item.date) }}</div>
              </div>
            </div>
            <div class="commit-message">
              <div v-for="(value, key) in item.messageMap" :key="key" class="message-item">
                <div class="message-key">
                  {{ key }}:{{ value }}
                </div>
              </div>
            </div>
          </a-card>
        </a-timeline-item>
      </a-timeline>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { getCommitsUsingGet } from '@/api/gitHubController.ts'
import { message } from 'ant-design-vue'

const timelineData = ref<API.GitHubCommitInfo[]>([])
const loading = ref(false)

const fetchData = async () => {
  try {
    loading.value = true
    const res = await getCommitsUsingGet()
    if (res.code === 0) {
      timelineData.value = res.data.map((commit) => {
        const messageLines = commit.message.split('\n').map(line => line.trim())
        const messageMap = {}
        let isContentLine = false
        let contentLines = []

        // 遍历每一行
        messageLines.forEach((line, index) => {
          const trimmedLine = line.trim()

          if (trimmedLine.includes(':')) {
            const [key, value] = trimmedLine.split(':').map(s => s.trim())

            if (key.toLowerCase() === 'content') {
              // 对于content，获取冒号后的所有内容
              contentLines.push(value)
              isContentLine = true
              // 清空messageMap中的content，避免重复
              delete messageMap['content']
            } else {
              // 如果当前不是content行，处理其他key-value对
              messageMap[key] = value
              isContentLine = false
            }
          } else if (isContentLine) {
            // 如果当前行没有冒号，但之前是content行，继续添加到content
            if (trimmedLine) {
              contentLines.push(trimmedLine)
            }
          }
        })

        // 最后将所有content行的内容合并
        if (contentLines.length > 0) {
          messageMap['content'] = contentLines.join('\n')
        }

        return {
          ...commit,
          messageMap
        }
      })
    } else {
      message.error('获取提交记录失败')
    }
  } catch (error) {
    message.error('获取提交记录失败')
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  fetchData()
}

const getGravatarUrl = (email: string) => {
  return `https://www.gravatar.com/avatar/${email.toLowerCase().trim()}`
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleString()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.timeline-view {
  max-width: 800px;
  padding-top: -10px;
  padding-bottom: 20px;
  margin-left: 28%;
}

.timeline-header {
  position: fixed;
  z-index: 1000;
  width: 800px;
  background: floralwhite;
}

.timeline-item-content {
  width: 100%;
  position: relative;
}

.date-label {
/*  text-align: center;*/
  margin-bottom: 12px;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 0;
  background: #f5f5f5;
  border-radius: 4px;
}

.commit-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.commit-info {
  margin-left: 12px;
}

.commit-author {
  font-size: 14px;
  margin-bottom: 4px;
}

.author-name {
  font-weight: 500;
  margin-right: 8px;
}

.author-email {
  color: #666;
  font-size: 13px;
}

.commit-message {
  margin-top: 10px;
  color: #333;
  line-height: 1.6;
}

.message-item {
  margin-bottom: 8px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 14px;
}

.message-item.first-message {
  font-weight: 500;
  color: #1890ff;
}

:deep(.ant-timeline-item-head) {
  background: #1890ff;
}

:deep(.ant-timeline-item-tail) {
  border-left: 2px solid #1890ff;
}
</style>
