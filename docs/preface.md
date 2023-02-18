# Spring Cloud Stream是干啥子的？


Spring的数据集成之旅始于Spring Integration。通过其编程模型，它提供了一致的开发人员体验，以构建可以采用企业集成模式与外部系统（如数据库、消息代理等）连接的应用程序。
快进到云时代，微服务在企业环境中变得突出。Spring Boot改变了开发人员构建应用程序的方式。通过Spring的编程模型和Spring Boot处理的运行时职责，开发独立的、基于生产级Spring的微服务变得无缝。


为了将此扩展到数据集成工作负载，Spring Integration和Spring Boot被合并到一个新项目中——Spring Cloud Stream诞生了。


Spring Cloud Stream是一个用于构建消息驱动的微服务应用程序的框架。 Spring Cloud Stream 基于 Spring Boot 构建，以创建独立的生产级 Spring 应用程序，并使用 Spring 集成提供与消息代理的连接。它提供了来自多个供应商的中间件的个性化配置，引入了持久发布订阅语义、消费者组和分区的概念。

使用Spring Cloud Stream，开发人员可以：

* 单独构建、测试和部署以数据为中心的应用程序。
* 应用现代微服务架构模式，包括通过消息传递进行组合。
* 将应用程序责任与以事件为中心的思维分离。事件可以表示在时间上发生的事情，下游消费者应用程序可以对此做出反应，而不知道其起源或生产者的身份。
* 将业务逻辑移植到消息代理（如RabbitMQ、Apache Kafka、Amazon Kinesis）上。
* 依赖于框架对常见用例的自动内容类型支持。可以扩展到不同的数据转换类型。