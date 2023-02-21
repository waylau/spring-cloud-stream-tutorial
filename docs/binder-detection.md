# Binder检测

Spring Cloud Stream依赖于Binder SPI的实现来执行将用户代码连接（binding）到消息代理的任务。每个Binder实现通常连接到一种类型的消息传递系统。


## 类路径检测

默认情况下，Spring Cloud Stream依赖于Spring Boot的自动配置来配置 binding 过程。如果在类路径上找到单个Binder实现，Spring Cloud Stream会自动使用它。例如，一个旨在仅绑定到RabbitMQ的Spring Cloud Stream项目可以添加以下依赖项：

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-stream-binder-rabbit</artifactId>
</dependency>
```