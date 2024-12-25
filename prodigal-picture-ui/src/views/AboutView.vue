<template>
  <a-typography>
    <a-typography-title>Introduction</a-typography-title>
    <a-typography-paragraph>
      - Java Spring Boot 框架(2.7.6)
      - MySQL数据库+ MyBatis-Plus 框架+ MyBatis  X +Redis 分布式缓存 +Caffeine 本地缓存Jsoup 数据抓取
      -  COS 对象存储
    </a-typography-paragraph>
    <a-typography-paragraph>
      1.建立初始会话:前端与服务器建立连接后，服务器会为该客户端创建一个初始的匿名 Session，并将其状态保存下来。这个 Session 的 ID 会作为唯一标识，返回给前端。
      2.登录成功，更新会话信息:当用户在前端输入正确的账号密码并提交到后端验证成功后，后端会更新该用户的 Session，将用户的登录信息(如用户 ID、用户名等)保存到与该 Session 关联的存储中。同时，服务器会生成一个 Set-Cookie的响应头，指示前端保存该用户的 Session ID,
      3.前端保存 Cookie:前端接收到后端的响应后，浏览器会自动根据 Set-Cookie 指令，将 Session ID 存储到浏览器的 Cookie 中，与该域名绑定。
      4.带 Cookie 的后续请求:当前端再次向相同域名的服务器发送请求时，浏览器会自动在请求头中附带之前保存的 Cookie，其中包含 Session lD.
      5.后端验证会话:服务器接收到请求后，从请求头中提取 Session ID，找到对应的 Session 数据
      6.获取会话中存储的信息:后端通过该 Session 获取之前存储的用户信息(如登录名、权限等)，从而识别用户身份并执行相应的业务逻辑。
      如何对用户权限进行控制?
      可以将接口分为 4 种权限:
      1.未登录也可以使用
      2.登录用户才能使用
      3.未登录也可以使用，但是登录用户能进行更多操作(比如登录后查看全文)
      4.仅管理员才能使用
      传统的权限控制方法是，在每个接口内单独编写逻辑:先获取到当前登录用户信息，然后判断用户的权限是否符合要求。这种方法最灵活，但是会写很多重复的代码，而且其他开发者无法一眼得知接口所需要的权限。
      权限校验其实是一个比较通用的业务需求，一般会通过 Spring AOP 切面 + 自定义权限校验注解 实现统一的接口拦截和权限校验;如果有特殊的权限校验逻辑，再单独在接口中编码。
      <a-typography-text strong>
        图片管理功能具体可以拆分为:
        - 【管理员】根据 id 删除图片
        - 【管理员】更新图片
        - 【管理员】分页获取图片列表(不需要脱敏和限制条数)
        - 【管理员】根据 id 获取图片(不需要脱敏)
        - 分页获取图片列表(需要脱敏和限制条数)
        - 根据 id 获取图片(需要脱敏)
        - 修改图片
      </a-typography-text>
    </a-typography-paragraph>
    <a-typography-title :level="2">Guidelines and Resources</a-typography-title>
    <a-typography-paragraph>
      We supply a series of design principles, practical patterns and high quality design resources
      (
      <a-typography-text code>Sketch</a-typography-text>
      and
      <a-typography-text code>Axure</a-typography-text>
      ), to help people create their product prototypes beautifully and efficiently.
    </a-typography-paragraph>

    <a-typography-paragraph>
      <ul>
        <li>
          <a-typography-link href="/docs/resources">Resource Download</a-typography-link>
        </li>
      </ul>
    </a-typography-paragraph>

    <a-typography-paragraph>
      Press
      <a-typography-text keyboard>Esc</a-typography-text>
      to exit...
    </a-typography-paragraph>

    <a-divider/>

    <a-typography-title>介绍</a-typography-title>
    <a-typography-paragraph>
      蚂蚁的企业级产品是一个庞大且复杂的体系。这类产品不仅量级巨大且功能复杂，而且变动和并发频繁，常常需要设计与开发能够快速的做出响应。同时这类产品中有存在很多类似的页面以及组件，可以通过抽象得到一些稳定且高复用性的内容。
    </a-typography-paragraph>
    <a-typography-paragraph>
      随着商业化的趋势，越来越多的企业级产品对更好的用户体验有了进一步的要求。带着这样的一个终极目标，我们（蚂蚁金服体验技术部）经过大量的项目实践和总结，逐步打磨出一个服务于企业级产品的设计体系
      Ant Design。基于
      <a-typography-text mark>『确定』和『自然』</a-typography-text>
      的设计价值观，通过模块化的解决方案，降低冗余的生产成本，让设计者专注于
      <a-typography-text strong>更好的用户体验</a-typography-text>
      。
    </a-typography-paragraph>
    <a-typography-title :level="2">设计资源</a-typography-title>
    <a-typography-paragraph>
      我们提供完善的设计原则、最佳实践和设计资源文件（
      <a-typography-text code>Sketch</a-typography-text>
      和
      <a-typography-text code>Axure</a-typography-text>
      ），来帮助业务快速设计出高质量的产品原型。
    </a-typography-paragraph>

    <a-typography-paragraph>
      <ul>
        <li>
          <a-typography-link href="/docs/resources-cn">设计资源</a-typography-link>
        </li>
      </ul>
    </a-typography-paragraph>

    <a-typography-paragraph>
      <blockquote>{{ blockContent }}</blockquote>
      <pre>{{ blockContent }}</pre>
    </a-typography-paragraph>

    <a-typography-paragraph>
      按
      <a-typography-text keyboard>Esc</a-typography-text>
      键退出阅读……
    </a-typography-paragraph>
  </a-typography>
</template>
<script lang="ts" setup>
// export default {
//   name: "AboutView"
// }

const blockContent = `AntV 是蚂蚁金服全新一代数据可视化解决方案，致力于提供一套简单方便、专业可靠、不限可能的数据可视化最佳实践。得益于丰富的业务场景和用户需求挑战，AntV 经历多年积累与不断打磨，已支撑整个阿里集团内外 20000+ 业务系统，通过了日均千万级 UV 产品的严苛考验。
我们正在基础图表，图分析，图编辑，地理空间可视化，智能可视化等各个可视化的领域耕耘，欢迎同路人一起前行。`;
</script>

<style scoped>

</style>
