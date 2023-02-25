# Spring Cloud Stream概念与其他中间件的映射关系

Spring Cloud Stream概念 | Kafka | RabbitMQ | RockettMQ | ActiveMQ
---|---|---|---|---
destination | topic | TopicExchange | topic | topic
consumer group | consumer group | x  | consumer group | Message Group
partition | partition | queue | queue | queue
x | Transactions | | |
x | AckMode | | | 
x | BATCH mode |  | | 


## 入门示例


https://demo.pidb.sieiot.com/doc-preview?shareCode=1dc54ca31de491cf3343d14e8c922523b82f6e7998979c1b85892c75cae5a50

## 多Binder场景

https://demo.pidb.sieiot.com/doc-preview?shareCode=b36988e471225482c532b692181865c4b5388ff1fc249fa9ee2b66ee9f36b5d6

## 动态Destination

https://demo.pidb.sieiot.com/doc-preview?shareCode=fcee2f636a2436c115a8d0e50e69077708cc1b950681cebaac189e7823ba4a4b


## 常用配置

https://demo.pidb.sieiot.com/doc-preview?shareCode=7d5c853dc7703a33feeaa5cdf84af9ad4a6f92b845965bd4e6d13994a20d4cfe



## 延时投放

https://demo.pidb.sieiot.com/doc-preview?shareCode=ccf09be760c9b4ebc5b9cadf0c1e0834021ae636ee60ef7b2599f62ff3f87670



## 手动提交（Binder特有）


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

其他两个Binder待验证。

## 批量消费（Kafka、RabbitMQ支持）


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


RocketMQ暂不支持批量消费，fastjson解析报错。

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

该问题已经向社区反馈：<https://github.com/alibaba/spring-cloud-alibaba/issues/3172>


ActiveMQ暂不支持批量消费。接收到的批量数据没有转为POJO


```
Received personList: [123, 34, 110, 97, 109, 101, 34, 58, 34, 83, 97, 109, 32, 83, 112, 97, 100, 101, 34, 125]
Received personList: [123, 34, 110, 97, 109, 101, 34, 58, 34, 83, 97, 109, 32, 83, 112, 97, 100, 101, 34, 125]
Received personList: [123, 34, 110, 97, 109, 101, 34, 58, 34, 83, 97, 109, 32, 83, 112, 97, 100, 101, 34, 125]
```

该问题已经向社区反馈：<https://github.com/mohammedamineboutouil/spring-cloud-stream-binder-jms/issues/3>





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
