# Binding属性

Binding属性的格式为`spring.cloud.stream.bindings.<bindingName>.<property>=<value>`。`<bindingName>`表示正在配置的binding的名称。


比如下面的示例：

```java
@Bean
public Function<String, String> uppercase() {
	return v -> v.toUpperCase();
}
```

上述uppercase()函数会映射两个binding。其中uppercase-in-0用于输入，uppercase-out-0用于输出。



为了避免重复，Spring Cloud Stream支持为所有 Binding 设置值，用于公共Binding属性格式为`spring.cloud.stream.default.<property>=<value>` 和 `spring.cloud.stream.default.<producer|consumer>.<property>=<value>`。


在避免重复扩展Binding属性时，应使用此格式`spring.cloud.stream.<binder-type>.default.<producer|consumer>.<property>=<value>`。



## 常用Binding属性



这些属性通过`org.springframework.cloud.stream.config.BindingProperties`暴露。



以下属性可用于输入和输出Binding，并且必须以“`spring.cloud.stream.bindings.<bindingName>.`”为前缀（例如，`spring.cloud.stream.bindings.uppercase-in-0.destination=ticktock`）。

可以使用“spring.cloud.stream.default”前缀设置默认值（例如“spring.cloud.stream.default.contentType=application/json”）。

* destination：绑定到中间件上的目的地，类似于RabbitMQ的exchange，在Kafka中Topic）。如果是消费者binding（输入），则可以将其绑定到多个destination，用英文“,”号隔开。如果不是，则使用实际的Binding名称，无法重写此属性的默认值。
* group：消费者组。仅适用于入站Binding。默认值：null（表示匿名消费者）。
* contentType：binding的内容类型。默认值是application/json。
* binder：binding所使用的Binder。默认值是null（如果存在Binder就使用默认的Binder）。

## 消费者属性


这些属性通过`org.springframework.cloud.stream.binder.ConsumerProperties`暴露。


以下属性仅适用于输入Binding，必须以“`spring.cloud.stream.bindings.<bindingName>.consumer.`”为前缀（例如，`spring.cloud.stream.bindings.input.consumer.concurrency=3)`）。

可以使用“spring.cloud.stream.default.consumer”前缀设置默认值（例如，`spring.cloud.stream.default.consumer.headerMode=none`）。

* autoStartup：消费者是否自动启动。默认值：true。
* concurrency：入站消费者的并发性。默认值：1。
* partitioned：消费者是否从分区生产者接收数据。默认值：false。
* headerMode：设置为none时，将禁用输入的标头解析。仅对原生不支持消息头且需要嵌入消息头的消息中间件有效。当不支持原生标头时，且需要使用非Spring Cloud Stream应用程序的数据时，此选项非常有用。当设置为headers时，它使用中间件的原生标头机制。当设置为embeddedHeaders时，它将标头嵌入到消息负载中。默认值：取决于Binder实现。
* maxAttempts：如果处理失败，则表示尝试处理消息的次数（包括第一次）。设置为1可禁用重试。默认值：3。
* backOffInitialInterval：重试时的回退初始间隔。默认值：1000。
* backOffMaxInterval：最大回退间隔。默认值：10000。
* backOffMultiplier：回退乘数。默认值：2.0。
* defaultRetryable：侦听器引发的未在retryableExceptions 中列出的异常是否可重试。默认值：true。
* instanceCount：当设置为大于等于零的值时，它允许自定义此消费者的实例计数（如果不同于`spring.cloud.stream.instanceCount`）。当设置为负值时，它默认为`spring.cloud.stream.instanceCount`的数。默认值：-1。
* instanceIndex：当设置为大于等于零的值时，它允许自定义此消费者的实例索引（如果不同于`spring.cloud.stream.instanceIndex`）。当设置为负值时，它默认为`spring.cloud.stream.instanceIndex`的值。如果提供了instanceIndexList ，则该配置会被忽略。默认值：-1。
* instanceIndexList：与不支持原生分区的Binder（如RabbitMQ）一起使用；允许应用程序实例使用多个分区。默认值：空。
* retryableExceptions：指定将重试或不重试的异常（和子类），是一个Map结构，键为Throwable类名，值为Boolean。示例`spring.cloud.stream.bindings.input.consumer.retryable-exceptions.java.lang.IllegalStateException=false`。默认值：空。
* useNativeDecoding：当设置为true时，入站消息将由客户端库直接反序列化，客户端库必须进行相应配置（例如，设置适当的Kafka生产者值反序列化器）。使用此配置时，入站消息解组不基于绑定的contentType。当使用原生解码时，生产者负责使用适当的编码器（例如，Kafka生产者值序列化器）来序列化出站消息。此外，当使用原生编码和解码时，`headerMode=embeddedHeaders`属性将被忽略，并且标头不会嵌入消息中。默认值：false。
* multiplex：当设置为true时，底层Binder将在同一输入Binding上原生复用destination。默认值：false。


## 高级消费者配置



对于消息驱动消费者的底层消息侦听器容器的高级配置，请将单个ListenerContainerCustomizer bean添加到应用程序上下文中。它将在应用上述属性后调用，并可用于设置其他属性。同样，对于轮询消费者，添加MMessageSourceCustomizer bean。

以下是RabbitMQ binder的示例：


```java
@Bean
public ListenerContainerCustomizer<AbstractMessageListenerContainer> containerCustomizer() {
    return (container, dest, group) -> container.setAdviceChain(advice1, advice2);
}

@Bean
public MessageSourceCustomizer<AmqpMessageSource> sourceCustomizer() {
    return (source, dest, group) -> source.setPropertiesConverter(customPropertiesConverter);
}
```


## 生产者属性



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
* requiredGroups：一个逗号分隔的组列表，即使在创建消息后开始，生产者也必须确保向其传递消息（例如，通过在RabbitMQ中预先创建持久队列）。
* headerMode：设置为none时，将禁用输出上的标头嵌入。它仅对不支持原生消息头且需要嵌入消息头的消息中间件有效。当不支持原生标头时，该选项在为非Spring Cloud Stream应用程序生成数据时非常有用。当设置为headers时，它使用中间件的原生头机制。当设置为embeddedHeaders时，它将标头嵌入到消息负载中。默认值：取决于Binder现。
* useNativeEncoding：当设置为true时，出站消息直接由客户端库序列化，客户端库必须进行相应配置（例如，设置适当的Kafka生产者值序列化器）。使用此配置时，出站消息编组不基于binding的contentType。当使用原生编码时，消费者有责任使用适当的解码器（例如，Kafka消费者值反序列化器）来反序列化入站消息。此外，当使用原生编码和解码时，headerMode=embeddedHeaders属性将被忽略，并且标头不会嵌入消息中。默认值：false。
* errorChannelEnabled：设置为true时，如果Binder支持异步发送结果，则发送失败将发送到destination错误通道。默认值：false。


## 高级生产者配置


可以自定义生产者的MessageHandler，只需要实现ProducerMessageHandlerCustomizer。


```java
@FunctionalInterface
public interface ProducerMessageHandlerCustomizer<H extends MessageHandler> {

	/**
	 * Configure a {@link MessageHandler} that is being created by the binder for the
	 * provided destination name.
	 * @param handler the {@link MessageHandler} from the binder.
	 * @param destinationName the bound destination name.
	 */
	void configure(H handler, String destinationName);

}
```
