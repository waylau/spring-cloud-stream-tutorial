# 事件路由



在Spring Cloud Stream的上下文中，事件路由是

* 将事件路由到特定事件订阅者
* 将事件订阅者产生的事件路由到某个特定destination的能力


这里我们将其称为“TO”和“FROM”路由。



## TO消费者路由



路由可以依靠Spring Cloud Function 3.0中的RoutingFunction实现。只需要通过应用程序属性`--spring.cloud.stream.function.routing.enabled=true`或提供`spring.cloud.function.routing-expression`属性来启用它。一旦启用RoutingFunction，将绑定到接收所有消息的输入destination，并根据提供的指令将其路由到其他函数。

可以通过单个消息和应用程序属性提供指令。


以下是几个示例。


### 使用消息头


```java
@SpringBootApplication
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class,
                       "--spring.cloud.stream.function.routing.enabled=true");
	}

	@Bean
	public Consumer<String> even() {
		return value -> {
			System.out.println("EVEN: " + value);
		};
	}

	@Bean
	public Consumer<String> odd() {
		return value -> {
			System.out.println("ODD: " + value);
		};
    }
}
```



通过向binder （即rabbit、kafka）暴露的destination “functionRouter-in-0”发送消息，此类消息将被路由到适当的（“even”或“odd”）消费者。
默认情况下，RoutingFunction将查找spring.cloud.function.definition 或 spring.cloud.function.routing-expression（对于具有SpEL的更多动态场景）标头，如果找到，其值将被视为路由指令。

例如，将spring.cloud.function.routing-expression标头设置为值“`T(java.lang.System).currentTimeMillis() % 2 == 0 ? 'even' : 'odd'`”将结束对 odd 或 even 函数的半随机路由请求。此外，对于SpEL，求值上下文的根对象是Message，因此也可以对单个标头（或消息）通过“`…​.routing-expression=headers['type']`”进行求值。


### 使用应用配置



spring.cloud.function.routing-expression 或 spring.cloud.function.definition 可以作为应用配置，比如“`spring.cloud.function.routing-expression=headers['type']`”。


```java
@SpringBootApplication
public class RoutingStreamApplication {

  public static void main(String[] args) {
      SpringApplication.run(RoutingStreamApplication.class,
	  "--spring.cloud.function.routing-expression="
	  + "T(java.lang.System).nanoTime() % 2 == 0 ? 'even' : 'odd'");
  }
  @Bean
  public Consumer<Integer> even() {
    return value -> System.out.println("EVEN: " + value);
  }

  @Bean
  public Consumer<Integer> odd() {
    return value -> System.out.println("ODD: " + value);
  }
}
```

### RoutingFunction 和输出binding

RoutingFunction是一个Function，因此其处理方式与任何其他函数几乎相同。


当RoutingFunction路由到另一个Function时，其输出将发送到RoutingFunction的输出binding ，该binding 是预期的函数functionRouter-in-0。但如果RoutingFunction路由到Consumer呢？换句话说，调用RoutingFunction的结果可能不会产生任何要发送到输出binding的内容，因此甚至需要有一个。因此，在创建binding时，我们确实对RoutingFunction有点不同。尽管它对作为用户的你来说是透明的（你真的没有什么可做的），但了解一些机制将有助于你理解它的内部运作。

因此，规则是：我们从不为RoutingFunction创建输出binding，只创建输入。因此，当您路由到Consumer时，RoutingFunction通过没有任何输出binding而有效地成为Consumer。但是，如果RoutingFunction恰好路由到生成输出的另一个Function，则RoutingFunction的输出binding将动态创建，此时RoutingFunction将作为binding的常规函Function（同时具有输入和输出binding）。



## FROM消费者路由



除了静态目的地之外，Spring Cloud Stream还允许应用程序向动态绑定的 destination 发送消息。例如，当需要在运行时确定目标 destination 时，这很有用。应用程序可以通过以下两种方式之一实现。


### spring.cloud.stream.sendto.destination


还可以通过将spring.cloud.stream.sendto.destination标头设置为要解析的目标的名称，委托给框架来动态解析输出目标。

考虑以下示例：

```java
@SpringBootApplication
@Controller
public class SourceWithDynamicDestination {

    @Bean
	public Function<String, Message<String>> destinationAsPayload() {
		return value -> {
			return MessageBuilder.withPayload(value)
				.setHeader("spring.cloud.stream.sendto.destination", value).build();};
	}
}
```

尽管在这个示例中可以清楚地看到，我们的输出是一个Message，其spring.cloud.stream.sendto.destination标头设置为输入参数的值。框架将查阅此标头，并尝试创建或发现具有该名称的 destination 并向其发送输出。

如果预先知道 destination 名称，则可以将生产者属性配置为与任何其他destination 相同。或者，如果注册了`NewDestinationBindingCallback<>` bean，则在创建 binding 之前调用它。回调采用Binder使用的扩展生成器属性的泛型类型。它有一种方法：


```java
void configure(String destinationName, MessageChannel channel, ProducerProperties producerProperties,
        T extendedProducerProperties);
```

以下示例显示如何使用RabbitMQ binder：


```java
@Bean
public NewDestinationBindingCallback<RabbitProducerProperties> dynamicConfigurer() {
    return (name, channel, props, extended) -> {
        props.setRequiredGroups("bindThisQueue");
        extended.setQueueNameGroupOnly(true);
        extended.setAutoBindDlq(true);
        extended.setDeadLetterQueueName("myDLQ");
    };
}
```
