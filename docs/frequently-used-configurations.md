# 常用配置



## 配置原则


配置原则

* “套娃”，外层覆盖内层配置。`Spring Cloud Stream -> Spring Kafka -> Kafka`
* 能用默认就不配（遵循Spring Boot零配置）
* 优先全局配置，其次细分配置
* 优先公共配置，其次特定配置（特定Binder实现）


## 公共配置

详见官方配置文档：<https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream.html#_configuration_options>

以下是常用的配置项。

### Binding服务属性

这些属性通过`org.springframework.cloud.stream.config.BindingServiceProperties`公开。

* spring.cloud.stream.instanceCount：应用程序的已部署实例数。必须为生产者侧的分区设置。当使用RabbitMQ和Kafka时，如果`autoRebalanceEnabled=false`，则必须在消费者侧设置。默认值：1。
* spring.cloud.stream.instanceIndex：应用程序的实例索引：从0到instanceCount-1的数字。如果`autoRebalanceEnabled=false`，则用于RabbitMQ和Kafka的分区。在Cloud Foundry中自动设置以匹配应用程序的实例索引。
* spring.cloud.stream.defaultBinder：如果配置了多个Binder，则使用默认Binder。默认值：空。
* spring.cloud.stream.bindingRetryInterval：重试绑定创建之间的间隔（以秒为单位）。将其设置为0以表示情况是非常致命的，从而阻止应用程序启动。默认值：30

### Binding属性


#### 常用Binding属性



这些属性通过`org.springframework.cloud.stream.config.BindingProperties`暴露。



以下属性可用于输入和输出Binding，并且必须以“`spring.cloud.stream.bindings.<bindingName>.`”为前缀（例如，`spring.cloud.stream.bindings.uppercase-in-0.destination=ticktock`）。

可以使用“spring.cloud.stream.default”前缀设置默认值（例如“spring.cloud.stream.default.contentType=application/json”）。

* destination：绑定到中间件上的目的地，类似于RabbitMQ的exchange，在Kafka中Topic）。如果是消费者binding（输入），则可以将其绑定到多个destination，用英文“,”号隔开。如果不是，则使用实际的Binding名称，无法重写此属性的默认值。
* group：消费者组。仅适用于入站Binding。默认值：null（表示匿名消费者）。
* contentType：binding的内容类型。默认值是application/json。
* binder：binding所使用的Binder。默认值是null（如果存在Binder就使用默认的Binder）。可选kafka、rabbit、rocketmq、jms等。


#### 消费者属性


这些属性通过`org.springframework.cloud.stream.binder.ConsumerProperties`暴露。


以下属性仅适用于输入Binding，必须以“`spring.cloud.stream.bindings.<bindingName>.consumer.`”为前缀（例如，`spring.cloud.stream.bindings.input.consumer.concurrency=3)`）。

可以使用“spring.cloud.stream.default.consumer”前缀设置默认值（例如，`spring.cloud.stream.default.consumer.headerMode=none`）。

* autoStartup：消费者是否自动启动。默认值：true。
* concurrency：入站消费者的并发性。默认值：1。
* maxAttempts：如果处理失败，则表示尝试处理消息的次数（包括第一次）。设置为1可禁用重试。默认值：3。
* defaultRetryable：侦听器引发的未在retryableExceptions 中列出的异常是否可重试。默认值：true。
* instanceCount：当设置为大于等于零的值时，它允许自定义此消费者的实例计数（如果不同于`spring.cloud.stream.instanceCount`）。当设置为负值时，它默认为`spring.cloud.stream.instanceCount`的数。默认值：-1。
* instanceIndex：当设置为大于等于零的值时，它允许自定义此消费者的实例索引（如果不同于`spring.cloud.stream.instanceIndex`）。当设置为负值时，它默认为`spring.cloud.stream.instanceIndex`的值。如果提供了instanceIndexList ，则该配置会被忽略。默认值：-1。




#### 生产者属性

这些属性通过`org.springframework.cloud.stream.binder.ProducerProperties`公开暴露。


以下binding属性仅适用于输出binding，必须以“`spring.cloud.stream.bindings.<bindingName>.producer.`”为前缀（例如，`spring.cloud.stream.bindings.func-out-0.producer.partitionKeyExpression=headers.id`）。
可以使用前缀“spring.cloud.stream.default.producer”设置默认值（例如，`spring.cloud.stream.default.producer.partitionKeyExpression=headers.id`）。



* autoStartup：生产者是否要自动启动。默认值：true。
* partitionKeyExpression：确定如何分区出站数据的SpEL表达式。如果设置，则对该binding上的出站数据进行分区。partitionCount 必须设置为大于1的值才能生效。
默认值：null。
* partitionKeyExtractorName：实现PartitionKeyExtractorStrategy的bean的名称。用于提取用于计算分区id的键（请参阅“`partitionSelector*`”）。与partitionKeyExpression互斥。默认值：null。
* partitionSelectorName：实现PartitionSelectorStrategy的bean的名称。用于基于分区键确定分区id（请参阅“`partitionKeyExtractor*`”）。与partitionSelectorExpression互斥。默认值：null。
* partitionSelectorExpression：用于自定义分区选择的SpEL表达式。如果两者都未设置，则选择“`hashCode(key) % partitionCount`”作为分区，其中key通过partitionKeyExpression计算。默认值：null。
* partitionCount：如果启用了分区，该属性用于设置分区数。如果生产者已分区，则必须设置为大于1的值。在Kafka上，它被解释为一种暗示。会使用目标Topic分区数和此值两者的较大值。默认值：1。


## Binder特有配置


参阅各Binder配置说明。


* [Spring Cloud Stream Kafka binder](https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream-binder-kafka.html#_configuration_options)
* [Spring Cloud Stream RabbitMQ binder](https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream-binder-rabbit.html#_configuration_options)
* [Spring Cloud Alibaba RocketMQ Binder](https://github.com/alibaba/spring-cloud-alibaba/wiki/RocketMQ-en)
* [Spring Cloud Stream JMS binder](https://github.com/mohammedamineboutouil/spring-cloud-stream-binder-jms/)


ActiveMQ目前是使用Spring Cloud Stream JMS binder。特别注意以下配置：

```
# 默认binder，可选kafka、rabbit、rocketmq、jms。如果是ActiveMQ则配置jms

###### start: ActiveMQ 特有配置 #######
# 是否使用内嵌ActiveMQ。正式项目选false
spring.activemq.in-memory=false

# 必须配置destination，值是以“queue://”或“topic://”开头
spring.cloud.stream.bindings.log-in-0.destination=queue://ticks
spring.cloud.stream.bindings.log-in-0.dlq.destination=queue://ticks
###### end:  ActiveMQ 特有配置 #######
```


## 配置示例

```
# 默认binder，可选kafka、rabbit、rocketmq、jms。如果是ActiveMQ则配置jms
spring.cloud.stream.defaultBinder=kafka

# 存在多个Supplier/Function/Consumer Bean时，必须配置function.definition
spring.cloud.function.definition=log;supplier

# 配置destination，如果是JMS binder值是以“queue://”或“topic://”开头
spring.cloud.stream.bindings.log-in-0.group=logGroup
spring.cloud.stream.bindings.log-in-0.destination=logDestination
spring.cloud.stream.bindings.supplier-out-0.destination=logDestination

# 消费者使用分区及并发数
#spring.cloud.stream.bindings.log-in-0.consumer.partitioned=true
# 分区索引
#spring.cloud.stream.bindings.log-in-0.consumer.instance-index=1
#spring.cloud.stream.instanceCount=5
#spring.cloud.stream.default.consumer.concurrency=10

# 分区规则及分区数
spring.cloud.stream.bindings.supplier-out-0.producer.partitionKeyExpression=headers['partitionKey']
spring.cloud.stream.bindings.supplier-out-0.producer.partitionCount=10
spring.cloud.stream.bindings.supplier-out-0.producer.required-groups=logGroup


####### start: Kafka 特有配置 #######
#spring.cloud.stream.kafka.binder.brokers=localhost
#spring.cloud.stream.kafka.binder.defaultBrokerPort=9092

# 以下配置搭配批量使用
#spring.kafka.consumer.max-poll-records=10
#spring.kafka.consumer.fetch.max.wait.ms=5000
#spring.kafka.consumer.fetch.min.bytes=5000
####### end:  Kafka 特有配置 #######

####### start: RabbitMQ 特有配置 #######
#spring.rabbitmq.host=localhost
#spring.rabbitmq.port=5672
#spring.rabbitmq.username=admin
#spring.rabbitmq.password=secret

# 以下配置分区
#spring.cloud.stream.bindings.supplier-out-0.required-groups: logGroup
#spring.cloud.stream.rabbit.bindings.supplier-out-0.producer.declare-exchange=false

# 以下配置搭配批量使用
#spring.cloud.stream.rabbit.bindings.input-in-0.consumer.enable-batching=true
#spring.cloud.stream.rabbit.bindings.input-in-0.consumer.batch-size=10
#spring.cloud.stream.rabbit.bindings.input-in-0.consumer.receive-timeout=200
####### end:  RabbitMQ 特有配置 #######


####### start: RocketMQ 特有配置 #######
#spring.cloud.stream.rocketmq.binder.name-server: 127.0.0.1:9876
####### end:  RocketMQ 特有配置 #######


####### start: ActiveMQ 特有配置 #######
# 是否使用内嵌ActiveMQ。正式项目选false
spring.activemq.in-memory=false
#spring.activemq.broker-url=tcp://192.168.1.210:9876
#spring.activemq.user=admin
#spring.activemq.password=admin
####### end:  ActiveMQ 特有配置 #######
```