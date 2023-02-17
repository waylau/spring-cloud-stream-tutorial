# 快速入门


为了快速理解Spring Cloud Stream的主要概念和抽象，接下来演示如何创建一个Spring Cloud Stream应用程序。该应用程序的业务功能非常简单，接收来自所选择的特定消息中间件的消息，比如Kafka、RabbitMQ、RocketMQ、ActiveMQ等，并将接收到的消息记录到控制台。


入门示例的创建主要有以下三个步骤：

* 使用Spring Initializer创建示例应用程序
* 将项目导入IDE
* 添加消息处理程序、构建和运行

你可以选择你钟意的任意一款消息中间件入手：

* [Spring Cloud Stream Kafka binder示例](docs/spring-initializr-cloud-stream-kafka.md)
* [Spring Cloud Stream RabbitMQ binder示例](docs/spring-initializr-cloud-stream-rabbitmq.md)
* [Spring Cloud Alibaba RocketMQ Binder示例](docs/spring-initializr-cloud-stream-rocketmq.md)
* [Spring Cloud Stream JMS binder示例](docs/spring-initializr-cloud-stream-jms.md)