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


## 功能


## 在线体验

- test/test123456

[//]: # (演示地址：http://ruoyi.vip  )
### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.6/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.6/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.7.6/reference/htmlsingle/#web)
