<template>
  <div class="error-bg">
    <div id="errorView">
      <h1 class="error-code">500</h1>
      <p class="error-desc">服务器内部错误，请稍后重试</p>
      <a-button type="primary" @click="goHome">返回首页</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
const goHome = () => router.push({ path: '/', replace: true });
</script>

<style scoped>
.error-bg {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

#errorView {
  text-align: center;
  background: white;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  padding: 60px;
  animation: fadeIn 0.5s ease;
}

.error-code {
  font-size: 96px;
  font-weight: bold;
  color: #ff4d4f;
  margin: 0 0 16px 0;
}

.error-desc {
  font-size: 18px;
  color: #999;
  margin-bottom: 32px;
}
</style>
