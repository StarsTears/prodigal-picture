<template>
  <div id="aboutView">
    <div class="banner">
      <h1 class="title">Prodigal Picture</h1>
      <p class="subtitle">企业级智能协同云图库平台</p>
    </div>

    <a-divider/>

    <a-typography>
      <a-typography-title :level="3">平台简介</a-typography-title>
      <a-typography-paragraph>
        Prodigal Picture 是一站式云图库管理平台，涵盖<strong>公共图库、私有空间、团队协作</strong>三大核心模块。
        支持图片上传、多维检索、AI 扩图、批量编辑、空间分析等功能，满足个人到企业级团队的图片资产管理需求。
      </a-typography-paragraph>
    </a-typography>

    <a-divider/>

    <a-typography-title :level="3">技术架构</a-typography-title>
    <a-row :gutter="[24, 24]">
      <a-col :xs="24" :md="12">
        <a-card title="后端" size="small">
          <a-typography-paragraph>
            <ul class="tech-list">
              <li><strong>框架</strong>：Spring Boot 3.x</li>
              <li><strong>数据库</strong>：MySQL 8.0 + MyBatis-Plus 3.5</li>
              <li><strong>缓存</strong>：Redis + Caffeine 二级缓存</li>
              <li><strong>存储</strong>：腾讯云 COS（万象数据处理）</li>
              <li><strong>权限</strong>：Sa-Token RBAC 多账号体系</li>
              <li><strong>分表</strong>：ShardingSphere 动态分表</li>
              <li><strong>文档</strong>：Knife4j 自动接口文档</li>
            </ul>
          </a-typography-paragraph>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card title="前端" size="small">
          <a-typography-paragraph>
            <ul class="tech-list">
              <li><strong>框架</strong>：Vue 3 + TypeScript</li>
              <li><strong>UI</strong>：Ant Design Vue</li>
              <li><strong>状态</strong>：Pinia</li>
              <li><strong>构建</strong>：Vite</li>
              <li><strong>通信</strong>：Axios + WebSocket</li>
            </ul>
          </a-typography-paragraph>
        </a-card>
      </a-col>
    </a-row>

    <a-divider/>

    <a-typography-title :level="3">核心功能</a-typography-title>
    <a-collapse :bordered="false">
      <a-collapse-panel key="1" header="图片管理">
        <ul>
          <li>本地图片上传、URL 抓取、批量导入</li>
          <li>上传自动解析格式/尺寸/色调，生成缩略图及 WebP 格式</li>
          <li>管理员审核机制，分级审核自动通过</li>
          <li>多维度检索：关键词、标签、分类、时间范围、颜色</li>
          <li>百度以图搜图、主色调相似度排序</li>
        </ul>
      </a-collapse-panel>
      <a-collapse-panel key="2" header="空间与协作">
        <ul>
          <li>公共图库 / 私有空间 / 团队空间三种模式</li>
          <li>团队空间成员管理，RBAC 权限控制</li>
          <li>WebSocket 多人实时协同编辑，编辑锁防冲突</li>
          <li>空间使用量统计分析</li>
        </ul>
      </a-collapse-panel>
      <a-collapse-panel key="3" header="AI 能力">
        <ul>
          <li>阿里云百炼大模型 AI 扩图</li>
          <li>图片主色调自动提取</li>
        </ul>
      </a-collapse-panel>
      <a-collapse-panel key="4" header="空间分析">
        <ul>
          <li>空间用量 / 分类分布 / 标签统计</li>
          <li>用户上传排行 / 大小排行 / 时间趋势</li>
        </ul>
      </a-collapse-panel>
      <a-collapse-panel key="5" header="更多">
        <ul>
          <li>全局统一异常处理与响应封装</li>
          <li>ShardingSphere 团队空间动态分表</li>
          <li>Redis + Caffeine 二级缓存加速首页</li>
          <li>公告邮件系统（MongoDB + RabbitMQ 异步发送）</li>
          <li>Actuator + Prometheus 应用监控</li>
        </ul>
      </a-collapse-panel>
    </a-collapse>

    <a-divider/>

    <a-typography-title :level="3">系统架构</a-typography-title>
    <div class="arch-diagram">
      <!-- 客户端层 -->
      <div class="arch-layer client-layer">
        <span class="layer-label">客户端</span>
        <div class="layer-boxes">
          <div class="arch-box">Vue 3 + TypeScript</div>
          <div class="arch-arrow">→</div>
          <div class="arch-box">Ant Design Vue</div>
          <div class="arch-arrow">→</div>
          <div class="arch-box">Pinia / Vite</div>
        </div>
      </div>
      <div class="arch-connector">
        <span class="connector-line"></span>
        <span class="connector-text">HTTP / WebSocket</span>
        <span class="connector-line"></span>
      </div>
      <!-- 网关层 -->
      <div class="arch-layer gateway-layer">
        <span class="layer-label">网关</span>
        <div class="layer-boxes">
          <div class="arch-box accent">REST API</div>
          <div class="arch-box accent">Sa-Token 鉴权</div>
          <div class="arch-box accent">接口限流</div>
          <div class="arch-box accent">CORS 跨域</div>
        </div>
      </div>
      <div class="arch-connector">
        <span class="connector-line"></span>
        <span class="connector-text">Spring Boot 3.x</span>
        <span class="connector-line"></span>
      </div>
      <!-- 服务层 -->
      <div class="arch-layer service-layer">
        <span class="layer-label">业务服务</span>
        <div class="layer-boxes">
          <div class="arch-box">Controller</div>
          <div class="arch-arrow">→</div>
          <div class="arch-box">Service</div>
          <div class="arch-arrow">→</div>
          <div class="arch-box">Manager</div>
          <div class="arch-arrow">→</div>
          <div class="arch-box">Mapper</div>
        </div>
        <div class="layer-boxes sub-row">
          <div class="arch-box small">AOP 切面</div>
          <div class="arch-box small">异常处理</div>
          <div class="arch-box small">定时任务</div>
          <div class="arch-box small">MQ 消息</div>
        </div>
      </div>
      <div class="arch-connector">
        <span class="connector-line"></span>
        <span class="connector-text">数据持久化</span>
        <span class="connector-line"></span>
      </div>
      <!-- 数据层 -->
      <div class="arch-layer data-layer">
        <span class="layer-label">数据存储</span>
        <div class="layer-boxes">
          <div class="arch-box data-item">MySQL 8.0<br/><small>ShardingSphere 分表</small></div>
          <div class="arch-box data-item">Redis<br/><small>Caffeine 二级缓存</small></div>
          <div class="arch-box data-item">MongoDB<br/><small>邮件公告</small></div>
          <div class="arch-box data-item">腾讯云 COS<br/><small>万象图片处理</small></div>
        </div>
      </div>
      <div class="arch-connector">
        <span class="connector-line"></span>
        <span class="connector-text">外部集成</span>
        <span class="connector-line"></span>
      </div>
      <!-- 外部服务 -->
      <div class="arch-layer external-layer">
        <span class="layer-label">外部服务</span>
        <div class="layer-boxes">
          <div class="arch-box external-item">阿里云百炼<br/><small>AI 扩图</small></div>
          <div class="arch-box external-item">百度搜图<br/><small>以图搜图</small></div>
          <div class="arch-box external-item">Actuator<br/><small>Prometheus 监控</small></div>
        </div>
      </div>
    </div>

    <a-divider/>

    <a-typography-title :level="3">关于作者</a-typography-title>
    <a-typography-paragraph>
      由<a-typography-link href="https://github.com/StarsTears" target="_blank">Lang</a-typography-link>开发维护，
      项目开源在
      <a-typography-link href="https://github.com/StarsTears/prodigal-picture" target="_blank">GitHub</a-typography-link>。
    </a-typography-paragraph>
    <a-typography-paragraph type="secondary">
      如有问题或建议，请联系
      <a-typography-text code>prodigal.lang@qq.com</a-typography-text>
    </a-typography-paragraph>
  </div>
</template>

<script lang="ts" setup>
// AboutView
</script>

<style scoped>
#aboutView {
  max-width: 860px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.banner {
  text-align: center;
  padding: 40px 0 24px;
}

.title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #1677ff 0%, #69b1ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 16px;
  color: #999;
  margin: 0;
}

.tech-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tech-list li {
  padding: 4px 0;
  font-size: 14px;
}

.arch-diagram {
  margin-top: 16px;
}

.arch-layer {
  border-radius: 10px;
  padding: 16px 20px;
  position: relative;
}

.layer-label {
  position: absolute;
  top: -11px;
  left: 20px;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 10px;
  letter-spacing: 1px;
}

.client-layer {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border: 1px solid #91d5ff;
}

.client-layer .layer-label {
  background: #1890ff;
  color: #fff;
}

.gateway-layer {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f8be 100%);
  border: 1px solid #95de64;
}

.gateway-layer .layer-label {
  background: #52c41a;
  color: #fff;
}

.service-layer {
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
  border: 1px solid #ffc069;
}

.service-layer .layer-label {
  background: #fa8c16;
  color: #fff;
}

.data-layer {
  background: linear-gradient(135deg, #f9f0ff 0%, #efdbff 100%);
  border: 1px solid #b37feb;
}

.data-layer .layer-label {
  background: #722ed1;
  color: #fff;
}

.external-layer {
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
  border: 1px solid #ff7875;
}

.external-layer .layer-label {
  background: #f5222d;
  color: #fff;
}

.layer-boxes {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.layer-boxes.sub-row {
  margin-top: 8px;
}

.arch-box {
  background: var(--bg-card);
  border: 1px solid var(--border-color-strong);
  border-radius: 6px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  text-align: center;
  line-height: 1.5;
}

.arch-box small {
  font-weight: 400;
  color: #999;
  font-size: 11px;
  display: block;
}

.arch-box.accent {
  border-color: #87d068;
  background: #f6ffed;
}

.arch-box.small {
  font-size: 12px;
  padding: 4px 10px;
  color: #666;
}

.arch-box.data-item {
  min-width: 110px;
}

.arch-box.external-item {
  min-width: 100px;
}

.arch-arrow {
  color: #bbb;
  font-weight: 700;
  font-size: 14px;
}

.arch-connector {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
}

.connector-line {
  flex: 1;
  height: 0;
  border-top: 1.5px dashed #c5c5c5;
}

.connector-text {
  font-size: 11px;
  color: #aaa;
  white-space: nowrap;
  font-weight: 500;
}

ul li {
  padding: 2px 0;
}
</style>
