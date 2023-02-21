# 内容类型协商


数据转换是任何消息驱动微服务架构的核心特征之一。考虑到在Spring Cloud Stream中，这样的数据被表示为Spring Message，消息在到达其目的地之前可能必须被转换为所需的形状或大小。这有两个原因：

* 转换传入消息的内容以匹配应用程序提供的处理程序的签名。
* 将传出消息的内容转换为有线格式。

有线格式通常是`byte[]`（这对于Kafka和Rabbit binder是正确的），但它由binder实现控制。

在Spring Cloud Stream中，消息转换是通过org.springframework.messaging.converter.MessageConverter完成的。
