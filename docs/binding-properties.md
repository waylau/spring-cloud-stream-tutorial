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



以下属性可用于输入和输出Binding，并且必须以“spring.cloud.stream.bindings.<bindingName>.”为前缀（例如，`spring.cloud.stream.bindings.uppercase-in-0.destination=ticktock`）。

可以使用“spring.cloud.stream.default”前缀设置默认值（例如“spring.cloud.stream.default.contentType=application/json”）。

* destination：绑定到中间件上的目的地，类似于RabbitMQ的exchange，在Kafka中Topic）。如果是消费者binding（输入），则可以将其绑定到多个destination，用英文“,”号隔开。如果不是，则使用实际的Binding名称，无法重写此属性的默认值。
* group：消费者组。仅适用于入站Binding。默认值：null（表示匿名消费者）。
* contentType：binding的内容类型。默认值是application/json。
* binder：binding所使用的Binder。默认值是null（如果存在Binder就使用默认的Binder）。

## 消费者属性


这些属性通过`org.springframework.cloud.stream.binder.ConsumerProperties`暴露。


以下属性仅适用于输入Binding，必须以“spring.cloud.stream.bindings.<bindingName>.consumer.”为前缀（例如，`spring.cloud.stream.bindings.input.consumer.concurrency=3)`）。

可以使用“spring.cloud.stream.default.consumer”前缀设置默认值（例如，`spring.cloud.stream.default.consumer.headerMode=none`）。

* autoStartup：消费者是否自动启动。默认值：true。
* concurrency：入站消费者的并发性。默认值：1。
* partitioned：消费者是否从分区生产者接收数据。默认值：false。
* headerMode：设置为none时，将禁用输入的标头解析。仅对本机不支持消息头且需要嵌入消息头的消息中间件有效。当不支持原生标头时，且需要使用非Spring Cloud Stream应用程序的数据时，此选项非常有用。当设置为headers时，它使用中间件的原生标头机制。当设置为embeddedHeaders时，它将标头嵌入到消息负载中。默认值：取决于Binder实现。
* maxAttempts：如果处理失败，则表示尝试处理消息的次数（包括第一次）。设置为1可禁用重试。默认值：3。
* backOffInitialInterval：重试时的回退初始间隔。默认值：1000。
* backOffMaxInterval：最大回退间隔。默认值：10000。
* backOffMultiplier：回退乘数。默认值：2.0。
* defaultRetryable：侦听器引发的未在retryableExceptions 中列出的异常是否可重试。默认值：true。
* instanceCount：当设置为大于等于零的值时，它允许自定义此消费者的实例计数（如果不同于`spring.cloud.stream.instanceCount`）。当设置为负值时，它默认为`spring.cloud.stream.instanceCount`的数。默认值：-1。
instanceIndex：当设置为大于等于零的值时，它允许自定义此消费者的实例索引（如果不同于`spring.cloud.stream.instanceIndex`）。当设置为负值时，它默认为`spring.cloud.stream.instanceIndex`的值。如果提供了instanceIndexList ，则该配置会被忽略。默认值：-1。
实例索引列表
与不支持本地分区的绑定器（如RabbitMQ）一起使用；允许应用程序实例使用多个分区。
默认值：空。
可检索异常
键中的Throwable类名映射和值中的布尔值。指定将重试或不重试的异常（和子类）。另请参见defaultRetrable。示例：spring.cloud.stream.bindings.input.consumer.retryableexceptions.java.lang.IllegalStateException=false。
默认值：空。
使用NativeDecoding
当设置为true时，入站消息将由客户端库直接反序列化，客户端库必须进行相应配置（例如，设置适当的Kafka生产者值反序列化器）。使用此配置时，入站消息解组不基于绑定的contentType。当使用本机解码时，生产者负责使用适当的编码器（例如，Kafka生产者值序列化器）来序列化出站消息。此外，当使用本机编码和解码时，headerMode=embeddedHeaders属性将被忽略，并且标头不会嵌入消息中。请参见生产者属性useNativeEncoding。
默认值：false。
多路复用
当设置为true时，底层绑定器将在同一输入绑定上本地复用目的地。
默认值：false。


## 高级消费者配置

## 生产者属性

## 高级生产者配置
