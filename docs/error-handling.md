# 错误处理



在本节中，我们将解释框架提供的错误处理机制背后的一般思想。我们将以Rabbit binder为例，因为单个binder为特定于底层代理功能的特定支持机制（例如Kafka binder）定义了不同的属性集。

当错误发生时，Spring Cloud Stream提供了几种灵活的机制来处理它们。注意，这些技术取决于binder的实现、底层消息中间件的能力以及编程模型。

每当消息处理程序（函数）抛出异常时，它就会传播回binder，此时binder将使用Spring Retry库提供的RetryTemplate多次尝试重新尝试同一消息（默认情况下为3）。如果重试不成功，则由错误处理机制决定，错误处理机制可能会丢弃消息、重新排队等待重新处理消息或将失败消息发送给DLQ。


Rabbit和Kafka都支持这些概念（尤其是DLQ）。但是，其他Binder可能不会，因此请参考您的Binder文档，了解有关支持的错误处理选项的详细信息。

但是请记住，反应式函数（reactive function）不符合消息处理程序的资格，因为它不处理单个消息，而是提供了一种将框架提供的流（即Flux）与用户提供的流连接起来的方法。为什么这很重要？这是因为您在本节后面阅读的有关Retry Template、丢弃失败消息、重试、DLQ和配置属性的所有内容都只适用于消息处理程序（即命令式函数）。

Reactive API提供了一个非常丰富的自身运算符和机制库，以帮助您处理特定于各种Reactive用例的错误，这些用例比简单的消息处理程序用例复杂得多，因此请使用它们，例如“`public final Flux<T> retryWhen(Retry retrySpec);`”可以在reactor.core.publisher.Flux中找到。




```java
@Bean
public Function<Flux<String>, Flux<String>> uppercase() {
	return flux -> flux
			.retryWhen(Retry.backoff(3, Duration.ofMillis(1000)))
			.map(v -> v.toUpperCase());
}
```

## 丢弃失败消息


默认情况下，系统提供错误处理程序。第一个错误处理程序将简单地记录错误消息。第二个错误处理程序是binder特定的错误处理程序，它负责在特定消息传递系统的上下文中处理错误消息（例如，发送到DLQ）。但是，由于没有提供额外的错误处理配置（在当前场景中），因此该处理程序不会执行任何操作。因此，本质上在被记录后，消息将被丢弃。

虽然在某些情况下是可以接受的，但在大多数情况下，它不是，我们需要一些恢复机制来避免消息丢失。



## 处理错误消息


在上一节中，我们提到，默认情况下，错误消息会被有效地记录和删除。该框架还为您提供了提供自定义错误处理程序（即发送通知或写入数据库等）的机制。您可以通过添加专门为接受ErrorMessage而设计的Consumer来实现这一点，除了有关错误的所有信息（例如，堆栈跟踪等）之外，它还包含原始消息（触发错误的消息）。注意：自定义错误处理程序与框架提供的错误处理程序（即日志记录和binder错误处理程序）是互斥的，以确保它们不会发生干扰。


```java
@Bean
public Consumer<ErrorMessage> myErrorHandler() {
	return v -> {
		// send SMS notification code
	};
}
```


要将此类使用者标识为错误处理程序，只需提供error-handler-definition属性，而后指向函数名“`spring.cloud.stream.bindings.<binding-name>.error-handler-definition=myErrorHandler`”。

例如，对于binding 名称“uppercase-in-0”属性如下所示：


```
spring.cloud.stream.bindings.uppercase-in-0.error-handler-definition=myErrorHandler
```



如果使用特殊的映射指令将 binding 映射到一个更可读的名称“spring.cloud.stream.function.bindings.uppercase-in-0=upper”，那么该属性将如下所示：

```
spring.cloud.stream.bindings.upper.error-handler-definition=myErrorHandler
```


### 默认错误处理程序

如果您想为所有函数bean使用单个错误处理程序，可以使用标准的Spring Cloud Stream机制来定义默认的属性，比如：

```
spring.cloud.stream.default.error-handler-definition=myErrorHandler
```



## DLQ


DLQ也许是最常见的机制，允许将失败的消息发送到一个特殊的destination：Dead Letter Queue（死信队列）。

配置后，失败的消息将发送到此 destination，以供后续重新处理或审核和协调。

考虑以下示例：

```java
@SpringBootApplication
public class SimpleStreamApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SimpleStreamApplication.class,
		  "--spring.cloud.function.definition=uppercase",
		  "--spring.cloud.stream.bindings.uppercase-in-0.destination=uppercase",
		  "--spring.cloud.stream.bindings.uppercase-in-0.group=myGroup",
		  "--spring.cloud.stream.rabbit.bindings.uppercase-in-0.consumer.auto-bind-dlq=true"
		);
	}

	@Bean
	public Function<Person, Person> uppercase() {
		return personIn -> {
		   throw new RuntimeException("intentional");
	      });
		};
	}
}
```



作为提醒，在本示例中，属性的uppercase-in-0段对应于输入destination binding。“consumer”段表明它是消费者属性。

**注**：使用DLQ时，必须至少提供group属性以正确命名DLQ destination。然而，group通常与destination属性一起使用，如我们的示例中所示。


除了一些标准的属性之外，我们还设置了auto-bind-dlq，以指示binder创建和配置DLQ destination ，这会产生一个名为uppercase.myGroup.dlq的附加Rabbit队列（有关Kafka特定的DLQ 属性，请参阅Kafka文档）。

配置后，所有失败的消息都将路由到此destination，保留原始消息以供进一步操作。

您可以看到，错误消息包含与原始错误相关的更多信息，如下所示：

```
. . . .
x-exception-stacktrace:	org.springframework.messaging.MessageHandlingException: nested exception is
      org.springframework.messaging.MessagingException: has an error, failedMessage=GenericMessage [payload=byte[15],
      headers={amqp_receivedDeliveryMode=NON_PERSISTENT, amqp_receivedRoutingKey=input.hello, amqp_deliveryTag=1,
      deliveryAttempt=3, amqp_consumerQueue=input.hello, amqp_redelivered=false, id=a15231e6-3f80-677b-5ad7-d4b1e61e486e,
      amqp_consumerTag=amq.ctag-skBFapilvtZhDsn0k3ZmQg, contentType=application/json, timestamp=1522327846136}]
      at org.spring...integ...han...MethodInvokingMessageProcessor.processMessage(MethodInvokingMessageProcessor.java:107)
      at. . . . .
Payload: blah
```

还可以通过将最大尝试次数设置为“1”，方便立即分派到DLQ（无需重新尝试）。例如

```
--spring.cloud.stream.bindings.uppercase-in-0.consumer.max-attempts=1
```

## RetryTemplate 

在本节中，我们将介绍与重试功能配置相关的配置属性。


RetryTemplate是Spring Retry库的一部分。虽然涵盖RetryTemplate的所有功能超出了本文档的范围，但我们将提及以下与RetryTemplate特别相关的消费者属性：

* maxAttempts：如果处理失败，则表示尝试处理消息的次数（包括第一次）。设置为1可禁用重试。默认值：3。
* backOffInitialInterval：重试时的回退初始间隔。默认值：1000。
* backOffMaxInterval：最大回退间隔。默认值：10000。
* backOffMultiplier：回退乘数。默认值：2.0。
* defaultRetryable：侦听器引发的未在retryableExceptions 中列出的异常是否可重试。默认值：true。
* retryableExceptions：指定将重试或不重试的异常（和子类），是一个Map结构，键为Throwable类名，值为Boolean。示例`spring.cloud.stream.bindings.input.consumer.retryable-exceptions.java.lang.IllegalStateException=false`。默认值：空。



虽然前面的设置足以满足大多数定制需求，但它们可能无法满足某些复杂的需求，此时可能需要提供自己的RetryTemplate实例。为此，在应用程序配置中将其配置为bean。应用程序提供的实例将覆盖框架提供的实例。此外，为了避免冲突，必须将 binder 要使用的RetryTemplate实例限定为`@StreamRetryTemplate`。例如


```java
@StreamRetryTemplate
public RetryTemplate myRetryTemplate() {
    return new RetryTemplate();
}
```


从上面的示例中可以看到，不需要用`@Bean`对其进行注释，因为`@StreamRetryTemplate`是一个合格的`@Bean`。

如果需要更精确地使用RetryTemplate，则可以在ConsumerProperty中按名称指定bean，以关联每个绑定的特定重试bean。


```
spring.cloud.stream.bindings.<foo>.consumer.retry-template-name=<your-retry-template-bean-name>
```