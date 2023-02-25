# Spring Cloud Stream Tutorial. 《跟老卫学Spring Cloud Stream开发》

![](images/SCSt-with-binder.png)

*Spring Cloud Stream Tutorial*, is a book about how to develop Spring Cloud Stream applications.



《跟老卫学Spring Cloud Stream开发》是一本 [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) 应用开发的开源学习教程，主要介绍如何从0开始开发 Spring Cloud Stream 应用。图文并茂，并通过大量实例带你走近 Spring Cloud Stream 的世界！



本书业余时间所著，水平有限，难免疏漏，欢迎指正。



## Summary 目录

* [Spring Cloud Stream是干啥子的？](docs/preface.md)
* [快速入门](docs/quick-start.md)
  * [Apache Kafka安装与使用](https://waylau.com/apache-kafka-quickstart/)
  * [RabbitMQ安装与使用](https://waylau.com/rabbitmq-quickstart/)
  * [Apache RocketMQ安装与使用](https://waylau.com/apache-rocketmq-quickstart/)
  * [Apache ActiveMQ Artemis安装与使用](https://waylau.com/apache-activemq-artemis-quickstart/)
  * [Apache ActiveMQ安装与使用](https://waylau.com/apache-activemq-quickstart/)
  * [Spring Cloud Stream Kafka binder示例](docs/spring-initializr-cloud-stream-kafka.md)
  * [Spring Cloud Stream RabbitMQ binder示例](docs/spring-initializr-cloud-stream-rabbitmq.md)
  * [Spring Cloud Alibaba RocketMQ Binder示例](docs/spring-initializr-cloud-stream-rocketmq.md)
  * [Spring Cloud Stream JMS binder示例](docs/spring-initializr-cloud-stream-jms.md)
* [核心概念](docs/main-concepts.md)
  * [应用模型](docs/application-model.md)
  * [Binder抽象](docs/binder-abstraction.md)
  * [持久发布订阅](docs/persistent-publish-subscribe.md)
  * [消费者组](docs/consumer-group.md)
  * [分区](docs/partitioning.md)
  * [插件化Binder SPI](docs/binder-spi.md)
* [编程模型](docs/programming-model.md)
  * [Destination Binder](docs/destination-binder.md)
  * [Binding](docs/binding.md)
  * [消息的生产与消费](docs/producing-and-consuming-message.md)
  * [事件路由](docs/event-routing.md)
  * [错误处理](docs/error-handling.md)
* [Binder](docs/binder.md)
  * [生产者与消费者](docs/producers-and-consumers.md)
  * [Binder SPI](docs/binder-spi.md)
  * [Binder检测](docs/binder-detection.md)
  * [多Binder](docs/multiple-binders.md)
  * [连接到多个系统](docs/multiple-systems.md)
  * [多Binder应用里面自定义Binder](docs/binder-customizer.md)
  * [Binding可视化与控制](docs/binding-visualization-control.md)
  * [Binder配置属性](docs/binder-configuration-properties.md)
  * [实现自定义Binder](docs/custom-binder.md)
* [配置](docs/configuration-option.md)
  * [Binding服务属性](docs/binding-service-properties.md)
  * [Binding属性](docs/binding-properties.md)
* [内容类型协商](docs/content-type-management.md)
  * [作法](docs/mechanics.md)
  * [提供的消息转换器](docs/provided-messageconverters.md)
  * [用户定义的消息转换器](docs/user-defined-message-converters.md)
* [应用程序间通信](docs/inter-application-communication.md)
  * [连接多个应用程序实例](docs/connecting-multiple-application-instances.md)
  * [实例索引和实例计数](docs/instance-index-instance-count.md)
  * [分区](docs/inter-application-communication-partitioning.md)
* [多Binder（Kafka、RabbitMQ、RocketMQ、ActiveMQ）示例](docs/spring-cloud-stream-muti-binder-kafka-rabbitmq-rocketmq-activemq-demo.md)
* [动态Destination示例](docs/spring-cloud-stream-dynamic-destination-kafka-demo.md)
* [Spring Cloud Stream概念与其他中间件的映射关系](docs/main-concepts-map.md)
* [批量处理示例](docs/spring-cloud-stream-batch-demo.md)
* [延迟消息示例](docs/spring-cloud-stream-fixed-delay-demo.md)
* [常用配置](docs/frequently-used-configurations.md)
* [分区示例](docs/spring-cloud-stream-partitioning-demo.md)
* [问题](docs/issue.md)
* 未完待续...

## Samples 示例

* [spring-cloud-stream-binder-kafka-demo](samples/spring-cloud-stream-binder-kafka-demo)
* [spring-cloud-stream-binder-rabbitmq-demo](samples/spring-cloud-stream-binder-rabbitmq-demo)
* [spring-cloud-stream-binder-rocketmq-demo](samples/spring-cloud-stream-binder-rocketmq-demo)
* [spring-cloud-stream-binder-jms-demo](samples/spring-cloud-stream-binder-jms-demo)
* [spring-cloud-stream-binder-artemis-demo](samples/spring-cloud-stream-binder-artemis-demo) (test)
* [spring-cloud-stream-muti-binder-kafka-rabbitmq-rocketmq-activemq-demo](samples/spring-cloud-stream-muti-binder-kafka-rabbitmq-rocketmq-activemq-demo)
* [spring-cloud-stream-dynamic-destination-kafka-demo](samples/spring-cloud-stream-dynamic-destination-kafka-demo)
* [spring-cloud-stream-batch-demo](samples/spring-cloud-stream-batch-demo)
* [spring-cloud-stream-fixed-delay-demo](samples/spring-cloud-stream-fixed-delay-demo)
* [spring-cloud-stream-partitioning-demo](samples/spring-cloud-stream-partitioning-demo)
* 未完待续...


## Get start 如何开始阅读

选择下面入口之一：

* <https://github.com/waylau/spring-cloud-stream-tutorial> 的 [README.md](https://github.com/waylau/spring-cloud-stream-tutorial/blob/master/README.md)



## Issue 意见、建议

如有勘误、意见或建议欢迎拍砖 <https://github.com/waylau/spring-cloud-stream-tutorial/issues>

## Contact 联系作者

* Blog: [waylau.com](http://waylau.com)
* Gmail: [waylau521(at)gmail.com](mailto:waylau521@gmail.com)
* Weibo: [waylau521](http://weibo.com/waylau521)
* Twitter: [waylau521](https://twitter.com/waylau521)
* Github : [waylau](https://github.com/waylau)


## Support Me 请老卫喝一杯

![开源捐赠](https://waylau.com/images/showmethemoney-sm.jpg)
