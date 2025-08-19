# Getting Started
<p align="center">

[//]: # (	<img alt="logo" src="https://oscimg.oschina.net/oscnet/up-b99b286755aef70355a7084753f89cdb7c9.png">)
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">Prodigal Picture v1.0.0</h1>
<h4 align="center">基于 Vue + Ant Design  和 Spring Boot/Spring Cloud & Alibaba 前后端分离的云图库管理系统</h4>
<p align="center">
    <a><img alt="GitHub" src="https://img.shields.io/github/contributors/StarsTears/prodigal-picture"></a>
</p>

## 平台简介
###后端
* 基于 Spring Boot + Redis + COS+ Al+ WebSocket 的企业级智能协同云图库平台。
* 分为公共图库、私有图库和团队共享图库三大模块。用户可在平台公开上传和检索图片;管理员可以上传、审核和管理分析图片。
* 个人用户可将图片上传至私有空间进行批量管理、多维检索、编辑和分析;企业可开通团队空间并邀请成员，共享和实时协同编辑图片。
### 前端
* 基于 Vue3 + Ant Design + Pinia+ Al+ WebSocket 的企业级智能协同云图库平台。 
* 分为公共图库、私有图库和团队共享图库三大模块。用户可在平台公开上传和检索图片;管理员可以上传、审核和管理分析图片。
* 个人用户可将图片上传至私有空间进行批量管理、多维检索、编辑和分析;企业可开通团队空间并邀请成员，共享和实时协同编辑图片。

## 系统模块

~~~
├─annotation                    //元注解
├─aop      
├─api                           //调用第三方API,如阿里云百炼
│  ├─aliyunai
│  └─imagesearch
├─common                        //公共模块
├─config                        //配置
├─constant                      //常量
├─controller    
├─exception                     异常处理
├─manager                       //独立方法；如COS
│  ├─strategy
│  └─upload
├─mapper
├─model                         //实体对象
│  ├─dto
│  ├─entity
│  ├─enums
│  └─vo
├─service
│  └─impl
└─utils
~~~
## 依赖
1. Spring Boot 2.7.6
2. Spring Cloud 2021.0.3
3. MySQL 8.0+
4. MyBatis Plus 3.5.9
5. Mybatis-plus-bom  3.5.9
6. 腾讯云对象存储COS
7. Knife4j-openapi2  4.4.0
8. Hutool 工具类  5.8.26
9. jsoup  1.15.3
10. Lombok 1.18.32
11. Sa-Token 1.39.0
12. ShardingSphere 5.2.0


## 功能
1. 统一异常管理:封装了自定义异常类及统一错误码枚举类，并基于 @RestControllerAdvice 注解开发全局异常处理器，提升了系统的稳定性。
2. 统一响应封装:自定义响应封装类和返回成功/失败响应的工具类，实现了标准化的接口响应数据格式，便于前后端交亘。
3. 全局跨域配置:基于 WebMvcConfiqurer 实现了全局 CORS 配置类，配置可信源，解决了前后端跨域请求问题精度丢失解决:
    自定义 Jackson 配置类，通过 ObjectMapper 配置 Long 类型序列化为字符串，解决前端因 」 精度限制导致的长整型数据精度丢失问题，确保前后端数据一致。
4. 通过 MyBatis Plus 的 LambdaQueryWrapper 和 SqlRunner 构造动态 SQL 查询，简化了操作数据库的编码。库表设计:根据业务设计用户、图片、空间、空间成员表，其中图片标签采用 JSON 数组存储，便于维护:并通过给空间成员关联表添加联合索引来提升检索性能。
5. 使用 Knife4i 自动生成接口文档，并基于 ApiOperation 注解编写接口注释，减少了人工维护文档的工作量，提高了接口可读性。
6. 用户注册:使用 MD5+盐值加密策略对用户密码进行加密存储，防止明文密码泄露风险。用户登录:通过 Redis 整合 Spring Session 分布式保存用户登录态，实现了跨服务器的用户识别。
7. 权限管理:基于自定义权限校验注解和 Spring AOP 环绕切面，实现了统一的权限校验，区分用户和管理员。图片存管:基于 COS 对象存储 SDK封装了通用文件上传下载服务，并基于数据万象服务自动解析图片信息(包括宽高、格式、大小等)，便于检索。
8. 图片审核:设计分级审核策略，管理员上传图片自动通过审核，并自动记录审核时间与审核人id，确保可审计追溯。URL 传图:使用 Hutool 请求 URL来下载图片，并在下载前通过 Head 请求校验 URL，有效隆低带宽消耗与数据异常风险。
9. 运用 **模板方法** 设计模式统一封装本地图片和 URL 图片上传的流程，如校验、下载、上传和资源释放，复用代码并提高可维护性。
10. 图片搜索:基于 MyBatis plus 封装了将对象转换为 SQL查询的通用方法，支持按关键词、标签、分类、时间范围等多维度组合搜索。
11. 以图搜图:通过分析请求获取到百度以图搜图 API，利用 Jsoup 和 HtpClient 调用 API，并运用 门]面模式 组合 API 调用来获取图片列表，便于客户端使用。 
12. 颜色搜图:使用数据万象提取图片主色调并存储到数据库，搜索时通过欧氏距离计算颜色相似度并通过 Stream API进行排序。 
13. 批量编辑:通过 Transactional 事务管理确保批量编辑图片的原子性;并利用 ThreadPoolExecutor+ 数据库批处理进一步优化了批处理性能。 
14. A1 扩图:基于阿里云百炼大模型封装 AI 绘图服务，提供创建与査询任务的 API，采用异步任务轮询处理进度，并统错误码以提升稳定性。 
15. 空间分析:运用 group by 实现数据的聚合统计，并通过 MyBatis Plus 的 selectObis 方法按需选择字段，提高查询性能并节约内存占用。 
16. 成员管理:设计 space_user 关联表保存团队空间成员及角色信息，使用唯一联合索引防止成员重复加入，同时提升查询性能。 
17. 运用 @Lazy 注解，解决了空间服务和空间成员服务互相引用导致的循环依赖问题。
18. 权限管理:基于 Sa-Token 的 Kit 模式实现了多账号体系的 **RBAC 权限控制**，通过从请求上下文中获取参数实现了统的权限校验逻辑，并运用注解合并简化了鉴权注解的使用，轻松实现方法级别的权限校验。
19. 数据分表:使用 shardingSphere 自定义分表算法实现了团队空间图片的 动态分表，提高了查询效率。并且为了通过 
20. 框架内置的校验规则，手动维护可用分表节点。协作编辑:基于 WebSocket+ **事件驱动设计** 实现多人协作编辑图片功能，自定义握手拦截器确保权限校验通过后才能连接，并通过“编辑锁”机制，避免编辑冲突。
21. 协作优化:基于 Disruptor **无锁队列** 实现了 WebSocket消息的异步化处理，显著提升系统吞吐量;并结合 @PreDestroy 注解实现优雅停机，防止编辑操作丢失。 
22. 架构升级:随着项目代码量增大，引入领域驱动设计 DDD，将业务逻辑按子域拆分，优化了代码结构与可扩展性
## 在线体验

- test/123456

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.6/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.6/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.7.6/reference/htmlsingle/#web)
