# Spring Cloud Stream概念与其他中间件的映射关系

Spring Cloud Stream概念 | Kafka | RabbitMQ | RockettMQ | ActiveMQ
---|---|---|---|---
destination | topic | TopicExchange | topic | topic
consumer group | consumer group | x  | consumer group | Message Group
partition | partition | queue | queue | queue
x | Transactions | | |
x | AckMode | | | 
x | BATCH mode |  | | 

## 配置原则

配置原则

* 能用默认就不配（遵循Spring Boot零配置）
* 优先全局配置，其次细分配置
* 优先公共配置，其次特定配置（特定Binder实现）


### 公共配置

详见官方配置文档：<https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream.html#_configuration_options>

以下是常用的配置项。

#### Binding服务属性

这些属性通过`org.springframework.cloud.stream.config.BindingServiceProperties`公开。

* spring.cloud.stream.instanceCount：应用程序的已部署实例数。必须为生产者侧的分区设置。当使用RabbitMQ和Kafka时，如果`autoRebalanceEnabled=false`，则必须在消费者侧设置。默认值：1。
* spring.cloud.stream.instanceIndex：应用程序的实例索引：从0到instanceCount-1的数字。如果`autoRebalanceEnabled=false`，则用于RabbitMQ和Kafka的分区。在Cloud Foundry中自动设置以匹配应用程序的实例索引。
* spring.cloud.stream.defaultBinder：如果配置了多个Binder，则使用默认Binder。默认值：空。
* spring.cloud.stream.bindingRetryInterval：重试绑定创建之间的间隔（以秒为单位）。将其设置为0以表示情况是非常致命的，从而阻止应用程序启动。默认值：30

#### Binding属性


##### 常用Binding属性



这些属性通过`org.springframework.cloud.stream.config.BindingProperties`暴露。



以下属性可用于输入和输出Binding，并且必须以“`spring.cloud.stream.bindings.<bindingName>.`”为前缀（例如，`spring.cloud.stream.bindings.uppercase-in-0.destination=ticktock`）。

可以使用“spring.cloud.stream.default”前缀设置默认值（例如“spring.cloud.stream.default.contentType=application/json”）。

* destination：绑定到中间件上的目的地，类似于RabbitMQ的exchange，在Kafka中Topic）。如果是消费者binding（输入），则可以将其绑定到多个destination，用英文“,”号隔开。如果不是，则使用实际的Binding名称，无法重写此属性的默认值。
* group：消费者组。仅适用于入站Binding。默认值：null（表示匿名消费者）。
* contentType：binding的内容类型。默认值是application/json。
* binder：binding所使用的Binder。默认值是null（如果存在Binder就使用默认的Binder）。可选kafka、rabbit、rocketmq、jms等。


##### 消费者属性


这些属性通过`org.springframework.cloud.stream.binder.ConsumerProperties`暴露。


以下属性仅适用于输入Binding，必须以“`spring.cloud.stream.bindings.<bindingName>.consumer.`”为前缀（例如，`spring.cloud.stream.bindings.input.consumer.concurrency=3)`）。

可以使用“spring.cloud.stream.default.consumer”前缀设置默认值（例如，`spring.cloud.stream.default.consumer.headerMode=none`）。

* autoStartup：消费者是否自动启动。默认值：true。
* concurrency：入站消费者的并发性。默认值：1。
* maxAttempts：如果处理失败，则表示尝试处理消息的次数（包括第一次）。设置为1可禁用重试。默认值：3。
* defaultRetryable：侦听器引发的未在retryableExceptions 中列出的异常是否可重试。默认值：true。
* instanceCount：当设置为大于等于零的值时，它允许自定义此消费者的实例计数（如果不同于`spring.cloud.stream.instanceCount`）。当设置为负值时，它默认为`spring.cloud.stream.instanceCount`的数。默认值：-1。
* instanceIndex：当设置为大于等于零的值时，它允许自定义此消费者的实例索引（如果不同于`spring.cloud.stream.instanceIndex`）。当设置为负值时，它默认为`spring.cloud.stream.instanceIndex`的值。如果提供了instanceIndexList ，则该配置会被忽略。默认值：-1。




##### 生产者属性

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


### Binder特有配置


参阅各Binder配置说明。


* [Spring Cloud Stream Kafka binder](https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream-binder-kafka.html#_configuration_options)
* [Spring Cloud Stream RabbitMQ binder](https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream-binder-rabbit.html#_configuration_options)
* [Spring Cloud Alibaba RocketMQ Binder](https://github.com/alibaba/spring-cloud-alibaba/wiki/RocketMQ-en)
* [Spring Cloud Stream JMS binder](https://github.com/mohammedamineboutouil/spring-cloud-stream-binder-jms/)


ActiveMQ目前是使用Spring Cloud Stream JMS binder。特别注意以下配置：

```
# 默认binder，可选kafka、rabbit、rocketmq、jms。如果是ActiveMQ则配置jms

####### start: ActiveMQ 特有配置 #######
# 是否使用内嵌ActiveMQ。正式项目选false
spring.activemq.in-memory=false

# 必须配置destination，值是以“queue://”或“topic://”开头
spring.cloud.stream.bindings.log-in-0.destination=queue://ticks
spring.cloud.stream.bindings.log-in-0.dlq.destination=queue://ticks
####### end:  ActiveMQ 特有配置 #######
```

## 手动提交


Kafka配置值为MANUAL或MANUAL_IMMEDIATE：

```
spring.cloud.stream.kafka.bindings.input.consumer.ackMode=MANUAL
```


Rabbit配置值为MANUAL：


```
spring.cloud.stream.rabbit.default.acknowledgeMode=MANUAL
```



示例代码：

```java
@SpringBootApplication
public class ManuallyAcknowdledgingConsumer {

 public static void main(String[] args) {
     SpringApplication.run(ManuallyAcknowdledgingConsumer.class, args);
 }

 @Bean
 public Consumer<Message<?>> process() {
    return message -> {
        Acknowledgment acknowledgment = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
        if (acknowledgment != null) {
         System.out.println("Acknowledgment provided");

         // 手动提交
         acknowledgment.acknowledge();
        }
    };
}
```


## 事务

针对特定的Binder设置事务配置。

Kafka配置

```
spring.cloud.stream.kafka.binder.transaction.transaction-id-prefix: tx-
spring.cloud.stream.kafka.binder.required-acks=all
```


RocketMQ配置

```
spring.cloud.stream.rocketmq.bindings.<channelName>.producer.transactional=true
```




## 分区

生产者

```java
@SpringBootApplication
public class KafkaPartitionProducerApplication {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private static final String[] data = new String[] {
            "foo1", "bar1", "qux1",
            "foo2", "bar2", "qux2",
            "foo3", "bar3", "qux3",
            "foo4", "bar4", "qux4",
            };

    public static void main(String[] args) {
        new SpringApplicationBuilder(KafkaPartitionProducerApplication.class)
            .web(false)
            .run(args);
    }

    @Bean
    public Supplier<Message<?>> generate() {
        return () -> {
            String value = data[RANDOM.nextInt(data.length)];
            System.out.println("Sending: " + value);
            return MessageBuilder.withPayload(value)
                    .setHeader("partitionKey", value)
                    .build();
        };
    }

}
```

application.yml

```
spring:
  cloud:
    stream:
      bindings:
        generate-out-0:
          destination: partitioned.topic
          producer:
            partition-key-expression: headers['partitionKey']
            partition-count: 12
```

消费者

```java
@SpringBootApplication
public class KafkaPartitionConsumerApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(KafkaPartitionConsumerApplication.class)
            .web(false)
            .run(args);
    }

    @Bean
    public Consumer<Message<String>> listen() {
        return message -> {
            int partition =- message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID);
            System.out.println(in + " received from partition " + partition);
        };
    }

}
```

application.yml

```
spring:
  cloud:
    stream:
      bindings:
        listen-in-0:
          destination: partitioned.topic
          group: myGroup
```


## 延时投放

Delay Message


```
spring.cloud.stream.bindings.supply-out-0.producer.poller.fixed-delay=2000
```


## 批量消费


配置：

```
spring.cloud.stream.bindings.input-in-0.consumer.batch-mode=true
```


如果还想再系统批量设置，则各个Binder来实现了。

RabbitMQ配置：

```
spring.cloud.stream.rabbit.bindings.input-in-0.consumer.enable-batching=true
spring.cloud.stream.rabbit.bindings.input-in-0.consumer.batch-size=10
spring.cloud.stream.rabbit.bindings.input-in-0.consumer.receive-timeout=200
```

Kafka配置：

```
####### start: Kafka 特有配置 #######
# 以下配置搭配批量使用
spring.kafka.consumer.max-poll-records=1
spring.kafka.consumer.fetch.max.wait.ms=5000
spring.kafka.consumer.fetch.min.bytes=5000
####### end:  Kafka 特有配置 #######
```



批量消费者代码：

```java
@Bean
public Consumer<List<Person>> logBatch() {
    return personList -> {
        // 打印出接收到的消息
        System.out.println("Received personList: " + personList);

        // ...

    };
}
```




RocketMQ支持批量发送，需要更新到最新版的fastjson：

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.24</version>
</dependency>
```


暂不支持批量消费，fastjson解析报错。

：

```
2023-02-23 11:17:42.104 ERROR 16552 --- [chDestination_1] o.s.integration.handler.LoggingHandler   : org.springframework.messaging.MessageHandlingException: error occurred in message handler [org.springframework.cloud.stream.function.FunctionConfiguration$FunctionToDestinationBinder$1@79142e08]; nested exception is com.alibaba.fastjson.JSONException: offset 1, character {, line 1, column 2, fastjson-version 2.0.24 {"name":"Sam Bo"}, failedMessage=GenericMessage [payload=byte[17], headers={ROCKET_MQ_BORN_TIMESTAMP=1677121911796, ROCKET_MQ_FLAG=0, ROCKET_MQ_MESSAGE_ID=7F00000125A078308DB173B177CD0009, ROCKET_MQ_TOPIC=logBatchDestination, ROCKET_MQ_BORN_HOST=10.10.52.63, id=76628487-9d8e-915a-777a-cd23983c551b, ROCKET_MQ_SYS_FLAG=0, contentType=application/json, ROCKET_MQ_QUEUE_ID=0, target-protocol=kafka, timestamp=1677122259081}]
	at org.springframework.integration.support.utils.IntegrationUtils.wrapInHandlingExceptionIfNecessary(IntegrationUtils.java:191)
	at org.springframework.integration.handler.AbstractMessageHandler.handleMessage(AbstractMessageHandler.java:65)
	at org.springframework.integration.dispatcher.AbstractDispatcher.tryOptimizedDispatch(AbstractDispatcher.java:115)
	at org.springframework.integration.dispatcher.UnicastingDispatcher.doDispatch(UnicastingDispatcher.java:133)
	at org.springframework.integration.dispatcher.UnicastingDispatcher.dispatch(UnicastingDispatcher.java:106)
	at org.springframework.integration.channel.AbstractSubscribableChannel.doSend(AbstractSubscribableChannel.java:72)
	at org.springframework.integration.channel.AbstractMessageChannel.send(AbstractMessageChannel.java:317)
	at org.springframework.integration.channel.AbstractMessageChannel.send(AbstractMessageChannel.java:272)
	at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:187)
	at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:166)
	at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:47)
	at org.springframework.messaging.core.AbstractMessageSendingTemplate.send(AbstractMessageSendingTemplate.java:109)
	at org.springframework.integration.endpoint.MessageProducerSupport.sendMessage(MessageProducerSupport.java:216)
	at com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQInboundChannelAdapter.lambda$consumeMessage$6(RocketMQInboundChannelAdapter.java:163)
	at org.springframework.retry.support.RetryTemplate.doExecute(RetryTemplate.java:329)
	at org.springframework.retry.support.RetryTemplate.execute(RetryTemplate.java:225)
	at com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQInboundChannelAdapter.consumeMessage(RocketMQInboundChannelAdapter.java:162)
	at com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQInboundChannelAdapter.lambda$onInit$5(RocketMQInboundChannelAdapter.java:124)
	at org.apache.rocketmq.client.impl.consumer.ConsumeMessageConcurrentlyService$ConsumeRequest.run(ConsumeMessageConcurrentlyService.java:402)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:577)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	at java.base/java.lang.Thread.run(Thread.java:1589)
Caused by: com.alibaba.fastjson.JSONException: offset 1, character {, line 1, column 2, fastjson-version 2.0.24 {"name":"Sam Bo"}
	at com.alibaba.fastjson.JSON.parseObject(JSON.java:776)
	at com.alibaba.fastjson.support.spring.messaging.MappingFastJsonMessageConverter.convertFromInternal(MappingFastJsonMessageConverter.java:68)
	at org.springframework.messaging.converter.AbstractMessageConverter.fromMessage(AbstractMessageConverter.java:185)
	at org.springframework.cloud.function.context.config.SmartCompositeMessageConverter.fromMessage(SmartCompositeMessageConverter.java:115)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.convertInputMessageIfNecessary(SimpleFunctionRegistry.java:1328)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.convertInputIfNecessary(SimpleFunctionRegistry.java:1089)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.doApply(SimpleFunctionRegistry.java:734)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.apply(SimpleFunctionRegistry.java:589)
	at org.springframework.cloud.stream.function.PartitionAwareFunctionWrapper.apply(PartitionAwareFunctionWrapper.java:84)
	at org.springframework.cloud.stream.function.FunctionConfiguration$FunctionWrapper.apply(FunctionConfiguration.java:791)
	at org.springframework.cloud.stream.function.FunctionConfiguration$FunctionToDestinationBinder$1.handleMessageInternal(FunctionConfiguration.java:623)
	at org.springframework.integration.handler.AbstractMessageHandler.handleMessage(AbstractMessageHandler.java:56)
	... 22 more
Caused by: com.alibaba.fastjson2.JSONException: offset 1, character {, line 1, column 2, fastjson-version 2.0.24 {"name":"Sam Bo"}
	at com.alibaba.fastjson2.reader.ObjectReaderImplList.readObject(ObjectReaderImplList.java:482)
	at com.alibaba.fastjson.JSON.parseObject(JSON.java:766)
	... 33 more
```

