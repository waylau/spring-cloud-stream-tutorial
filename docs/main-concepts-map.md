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
* binder：binding所使用的Binder。默认值是null（如果存在Binder就使用默认的Binder）。


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

TODO

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


```
spring.cloud.stream.bindings.supply-out-0.producer.poller.fixed-delay=2000
```


## 批量消费

```
spring.cloud.stream.bindings.input-in-0.group=someGroup

spring.cloud.stream.bindings.input-in-0.consumer.batch-mode=true

spring.cloud.stream.rabbit.bindings.input-in-0.consumer.enable-batching=true
spring.cloud.stream.rabbit.bindings.input-in-0.consumer.batch-size=10
spring.cloud.stream.rabbit.bindings.input-in-0.consumer.receive-timeout=200
```