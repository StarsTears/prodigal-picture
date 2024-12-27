# Getting Started
<p align="center">

[//]: # (	<img alt="logo" src="https://oscimg.oschina.net/oscnet/up-b99b286755aef70355a7084753f89cdb7c9.png">)
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">Prodigal Picture v1.0.0</h1>
<h4 align="center">基于 Vue + Ant Design  和 Spring Boot/Spring Cloud & Alibaba 前后端分离的云图库管理系统</h4>

## 平台简介
* 采用前后端分离的模式。
* 后端采用Spring Boot、Spring Cloud & Alibaba。
* 注册中心、配置中心选型Nacos，权限认证使用Redis。
* 流量控制框架选型Sentinel，分布式事务选型Seata。

## 系统模块

~~~
├─annotation                    //元注解
├─aop      
├─api                           //调用第三方API,如阿里云百炼
│  ├─aliyunai
│  │  └─model
│  │      ├─dto
│  │      └─vo
│  └─imagesearch
│      ├─baidu
│      └─bing
├─common                        //公共模块
├─config                        //配置
├─constant                      //常量
├─controller    
├─exception                     异常处理
├─manager                       //
│  ├─strategy
│  └─upload
├─mapper
├─model                         //实体对象
│  ├─dto
│  │  ├─file
│  │  ├─picture
│  │  ├─space
│  │  └─user
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
5. mybatis-plus-bom  3.5.9
6. 腾讯云对象存储COS
7. knife4j-openapi2  4.4.0
8. hutool 工具类  5.8.26
9. jsoup  1.15.3
10. lombok 1.18.32


## 内置功能

1.  用户管理：用户是系统操作者，该功能主要完成系统用户配置。
2.  部门管理：配置系统组织机构（公司、部门、小组），树结构展现支持数据权限。
3.  岗位管理：配置系统用户所属担任职务。
4.  菜单管理：配置系统菜单，操作权限，按钮权限标识等。
5.  角色管理：角色菜单权限分配、设置角色按机构进行数据范围权限划分。
6.  字典管理：对系统中经常使用的一些较为固定的数据进行维护。
7.  参数管理：对系统动态配置常用参数。
8.  通知公告：系统通知公告信息发布维护。
9.  操作日志：系统正常操作日志记录和查询；系统异常信息日志记录和查询。
10. 登录日志：系统登录日志记录查询包含登录异常。
11. 在线用户：当前系统中活跃用户状态监控。
12. 定时任务：在线（添加、修改、删除)任务调度包含执行结果日志。
13. 代码生成：前后端代码的生成（java、html、xml、sql）支持CRUD下载 。
14. 系统接口：根据业务代码自动生成相关的api接口文档。
15. 服务监控：监视当前系统CPU、内存、磁盘、堆栈等相关信息。
16. 在线构建器：拖动表单元素生成相应的HTML代码。
17. 连接池监视：监视当前系统数据库连接池状态，可进行分析SQL找出系统性能瓶颈。

## 在线体验

- admin/admin123  
- 陆陆续续收到一些打赏，为了更好的体验已用于演示服务器升级。谢谢各位小伙伴。

演示地址：http://ruoyi.vip  
### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.6/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.6/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.7.6/reference/htmlsingle/#web)
