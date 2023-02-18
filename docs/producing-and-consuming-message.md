# 消息的生产与消费

可以通过编写函数并将其公开为“@Bean”来编写Spring Cloud Stream应用程序。也可以使用基于Spring Integration注解的配置或基于Spring Cloud Stream注解的配置，但建议从Spring Cloud Stream 3.x开始就使用函数实现。



## Spring Cloud Function


自Spring Cloud Stream v2.1以来，定义流处理程序和源的另一种选择是使用内置支持的Spring Cloud Function，其中它们可以表示为`java.util.function.[Supplier/Function/Consumer]`类型的bean。

要指定要绑定到binding公开的外部destination的函数bean，必须提供spring.cloud.function.definition属性。
如果只有一个`java.util.function.[Supplier/Function/Consumer]`类型的bean，则可以跳过spring.cloud.function.definition属性，因为这样的函数bean将被自动发现。然而，使用此类属性以避免任何混淆被认为是最佳做法。有时，这种自动发现可能会遇到障碍，因为`java.util.function.[Supplier/Function/Consumer]`类型的单个bean可能用于处理消息之外的其他目的，但如果是单个bean，则会自动发现并自动绑定。对于这些罕见的情况，可以通过将spring.cloud.stream.function.autodetect属性值设置为false来禁用自动发现。

下面是应用程序将消息处理程序公开为java.util.function.Function的示例，它通过充当数据的消费者和生产者来有效地支持传递语义。


```java
@SpringBootApplication
public class MyFunctionBootApp {

	public static void main(String[] args) {
		SpringApplication.run(MyFunctionBootApp.class);
	}

	@Bean
	public Function<String, String> toUpperCase() {
		return s -> s.toUpperCase();
	}
}
```


在上面的示例中，定义了一个java.util.function.Function类型的bean，称为toUpperCase，作为消息处理程序，其“input”和“output”必须绑定到所提供的目标binder公开的外部destination。默认情况下，“input”和“output”绑定名称将为“toUpperCase-in-0”和“toUpperCase-out-0”。

下面是支持其他语义的简单函数式应用程序的示例。

以下是公开为java.util.function.Supplier的source语义示例：

```java
@SpringBootApplication
public static class SourceFromSupplier {

	@Bean
	public Supplier<Date> date() {
		return () -> new Date(12345L);
	}
}
```

下面是公开为java.util.function.Consumer的sink语义示例

```java
@SpringBootApplication
public static class SinkFromConsumer {

	@Bean
	public Consumer<String> sink() {
		return System.out::println;
	}
}
```

### Supplier (Source)


Function 和 Consumer 在如何触发调用方面非常简单。它们是基于发送到绑定 destination 的数据（事件）触发的。换句话说，它们是经典的事件驱动组件。

然而，当涉及触发时，Supplier 属于自己的类别。由于根据定义，它是数据的源（source），因此它不订阅任何绑定destination，因此必须由其他机制触发。还有一个 Supplier 实施的问题，这可能是强制性的，也可能是被动的，并且直接与此类 Supplier 的触发有关。

考虑以下示例：


```java
@SpringBootApplication
public static class SupplierConfiguration {

	@Bean
	public Supplier<String> stringSupplier() {
		return () -> "Hello from Supplier";
	}
}
```

每当调用其get()方法时，前面的Supplier bean都会生成一个字符串。然而，谁会调用此方法？调用频率如何？该框架提供了一个默认的轮询机制（回答“谁？”的问题），该机制将触发 Supplier 的调用，默认情况下，它会每秒调用一次（回答“多长时间？”）。换句话说，上述配置每秒生成一条消息，每条消息都发送到binder公开的output destination 。



考虑另外一个不同的例子：


```java
@SpringBootApplication
public static class SupplierConfiguration {

    @Bean
    public Supplier<Flux<String>> stringSupplier() {
        return () -> Flux.fromStream(Stream.generate(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(1000);
                    return "Hello from Supplier";
                } catch (Exception e) {
                    // ignore
                }
            }
        })).subscribeOn(Schedulers.elastic()).share();
    }
}
```



前面的Supplier bean采用反应式编程风格。通常，与命令式 Supplier 不同，它应该只触发一次，因为它的get()方法的调用产生（提供）连续的消息流，而不是单个消息。

该框架认识到编程风格的差异，并保证此类 Supplier 只触发一次。

然而，想象一下想要轮询某个数据源并返回表示结果集的有限数据流的用例。反应式编程风格是此类 Supplier 的完美机制。然而，鉴于生产物流的有限性，此类 Supplier 仍需定期调用。

考虑以下示例，该示例通过生成有限的数据流来模拟此类用例：


```java
@SpringBootApplication
public static class SupplierConfiguration {

	@PollableBean
	public Supplier<Flux<String>> stringSupplier() {
		return () -> Flux.just("hello", "bye");
	}
}
```

bean本身用PollableBean注解（`@bean`的子集）进行注解，从而向框架发出信号，尽管这样的 Supplier 的实现是被动的，但它仍然需要被轮询。


**注**：
PollableBean中定义了一个 splittable 属性，该属性向该注解的后处理器发出信号，表示必须拆分注解组件生成的结果，并默认设置为true。这意味着框架将把返回的每一项作为单独的消息发送出去。如果这不是想要的行为，你可以将其设置为false，此时supplier只需返回生产的Flux，而不拆分它。


#### Supplier & 线程


正如您现在所了解到的，不同于由事件触发的Function和Consumer（它们具有输入数据），Supplier没有任何输入，因此由不同的机制触发-轮询器，轮询器可能具有不可预测的线程机制。尽管线程机制的细节在大多数时候与函数的下游执行无关，但在某些情况下，尤其是对线程亲和性有一定期望的集成框架，它可能会带来问题。例如，Spring Cloud Sleuth依赖于跟踪存储在线程本地中的数据。对于这些情况，我们通过StreamBridge有另一种机制，用户可以对线程机制进行更多的控制。您可以在“将任意数据发送到output”部分获得更多详细信息。


### Consumer (Reactive)

Reactive Consumer有点特殊，因为它有一个void返回类型，使框架没有可订阅的引用。您很可能不需要编写`Consumer<Flux<?>>`，而是将其写为函数`Function<Flux<?>, Mono<Void>>`调用then运算符作为流上的最后一个运算符。

例如：

```java
public Function<Flux<?>, Mono<Void>>consumer() {
	return flux -> flux.map(..).filter(..).then();
}
```

但如果您确实需要编写一个明确的`Consumer<Flux<?>>`，记得订阅即将到来的Flux。

此外，请记住，当混合反应式和命令式函数时，相同的规则适用于函数组合。Spring Cloud Function确实支持用命令式组合反应式函数，但是您必须意识到某些限制。例如，假设您已经用命令消费者组成了反应函数。这种组合的结果是一个反应式消费者。然而，没有办法订阅本节前面讨论的此类消费者，因此只能通过使消费者反应并手动订阅（如前所述），或者将功能更改为命令式来解决此限制。

#### 轮询配置属性


以下属性由Spring Cloud Stream公开，并以Spring.integration.poller作为前缀：


* fixedDelay：默认轮询器的固定延迟（毫秒）。默认值：1000L。
* maxMessagesPerPoll：默认轮询器的每个轮询事件的最大消息数。默认值：1L。
* cron：Cron触发器的Cron表达式值。默认值：无。
* initialDelay：周期性触发器的初始延迟。默认值：0。
* timeUnit：要应用于延迟值的TimeUnit。默认值：MILLISECONDS。


例如`--spring.integration.poller.fixed-delay=2000`将轮询器间隔设置为每两秒轮询一次。

#### 每个binding轮询配置


上一节介绍了如何配置将应用于所有binding的单个默认轮询器。虽然它很适合微服务的模型，即为每个微服务代表单个组件（例如，Supplier）而设计的Spring Cloud Stream，因此默认轮询器配置就足够了，但在某些边缘情况下，可能有几个组件需要不同的轮询配置。

对于这种情况，请使用为每个binding配置轮询器。例如，假设您有一个 binding “supply-out-0”。在这种情况下，可以使用“spring.cloud.stream.bindings.supply-out-0.producer.poller..”前缀为此类绑定配置轮询器。以下是一个示例：

```
spring.cloud.stream.bindings.supply-out-0.producer.poller.fixed-delay=2000
```



### 将任意数据发送到output

在某些情况下，实际数据源可能来自外部系统，而不是binder。例如，数据源可能是经典的REST端点。我们如何将这种来源与Spring Cloud Stream使用的函数机制联系起来？

Spring Cloud Stream提供了两种机制，因此让我们更详细地了解它们。在这里，对于这两个示例，我们将使用名为delegateToSupplier的标准MVC端点方法绑定到根web上下文，通过StreamBridge机制将传入请求委派到流。

```java
@SpringBootApplication
@Controller
public class WebSourceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebSourceApplication.class, "--spring.cloud.stream.source=toStream");
	}

	@Autowired
	private StreamBridge streamBridge;

	@RequestMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void delegateToSupplier(@RequestBody String body) {
		System.out.println("Sending " + body);
		streamBridge.send("toStream-out-0", body);
	}
}
```

在这里，我们自动连接StreamBridge bean，它允许我们将数据发送到输出 binding，有效地将非流应用程序与Spring Cloud Stream桥接起来。请注意，前面的示例没有定义任何源函数（例如，Supplier bean），从而使框架不需要预先创建源binding，这对于配置包含函数bean的情况来说是典型的。这很好，因为StreamBridge将在第一次调用其 send(..) 操作时为非现有绑定启动输出binding的创建（以及必要时的destination 自动配置），并缓存该binding以供后续重用。

但是，如果您希望在初始化（启动）时预先创建输出binding，则可以从spring.cloud.stream.source属性中受益，在该属性中可以声明source的名称。提供的名称将用作创建源binding的触发器。因此，在前面的示例中，输出绑定的名称将是toStream-out-0，这与函数使用的绑定命名约定一致。可以使用“;”表示多个source，例如，`--spring.cloud.stream.source=foo;bar`。

此外，请注意streamBridge.send(..)方法采用Object作为数据。这意味着您可以向其发送POJO或Message，并且当发送输出时，它将执行相同的例程，就像来自任何Function或Supplier一样，提供与函数相同的一致性级别。这意味着输出类型转换、分区等被视为来自函数生成的输出。


#### StreamBridge和动态Destination


StreamBridge也可用于输出Destination事先未知的情况，类似于“路由自消费者”部分中描述的用例。

让我们看一下示例

```java
@SpringBootApplication
@Controller
public class WebSourceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebSourceApplication.class, args);
	}

	@Autowired
	private StreamBridge streamBridge;

	@RequestMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void delegateToSupplier(@RequestBody String body) {
		System.out.println("Sending " + body);
		streamBridge.send("myDestination", body);
	}
}
```


这里，我们将数据发送到myDestination名称，该名称不作为binding 存在。因此，此类名称将被视为动态Destination。

一个更实际的例子，其中外来源是REST端点。

```java
@SpringBootApplication
@Controller
public class WebSourceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebSourceApplication.class);
	}

	@Autowired
	private StreamBridge streamBridge;

	@RequestMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void delegateToSupplier(@RequestBody String body) {
		streamBridge.send("myBinding", body);
	}
}
```

正如您在delegateToSupplier方法中看到的，我们使用StreamBridge将数据发送到myBinding binding。在这里，您还可以从StreamBridge的动态特性中受益，如果myBinding不存在，它将自动创建并缓存，否则将使用现有binding。


**注**：如果存在多个动态destination，缓存动态destination（binding）可能会导致内存泄漏。为了获得某种程度的控制，默认缓存大小为10。这意味着，如果动态destination大小超过该数字，则可能会驱逐现有binding，因此需要重新创建，这可能会导致性能轻微下降。可以通过`spring.cloud.stream.dynamic-destination-cache-size`属性来增加缓存大小。


#### 通过StreamBridge输出内容类型


如果需要，还可以使用以下方法签名`public boolean send(String bindingName, Object data, MimeType outputContentType)`提供特定的内容类型。或者，如果您将数据作为Message发送，则其内容类型将被接受。


#### 使用StreamBridge时制定特定binder类型 


Spring Cloud Stream支持多个Binder场景。例如，可能正在从Kafka接收数据并将其发送到RabbitMQ。

如果计划使用StreamBridge并在应用程序中配置了多个Binder，则还必须告诉StreamBridge要使用哪个Binder。为此，send方法还有两种变体：



```java
public boolean send(String bindingName, @Nullable String binderType, Object data)

public boolean send(String bindingName, @Nullable String binderType, Object data, MimeType outputContentType)
```

如您所见，还有一个额外的参数可以提供binderType，告诉BindingService在创建动态binding时要使用哪个Binder。

**注**：对于使用spring.cloud.stream.source属性或已在不同Binder下创建绑定的情况，binderType参数将无效。


#### Using channel interceptors with StreamBridge


由于StreamBridge使用MessageChannel建立输出binding，因此可以在通过StreamBridge发送数据时激活通道拦截器。由应用程序决定在StreamBridge上应用哪些信道拦截器。除非使用`@GlobalChannelInterceptor(patterns = "*")`对所有检测到的频道拦截器进行注释，否则Spring Cloud Stream不会将它们注入StreamBridge。

让我们假设应用程序中有以下两个不同的StreamBridge绑定


```
streamBridge.send("foo-out-0", message);
```

和

```
streamBridge.send("bar-out-0", message);
```

现在，如果希望在两个StreamBridge绑定上应用通道拦截器，那么可以声明以下GlobalChannelInterceptor bean。

```java
@Bean
@GlobalChannelInterceptor(patterns = "*")
public ChannelInterceptor customInterceptor() {
    return new ChannelInterceptor() {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            ...
        }
    };
}
```


但是，如果您不喜欢上面的全局方法，并且希望为每个binding都有一个专用的拦截器，那么可以执行以下操作。

```java
@Bean
@GlobalChannelInterceptor(patterns = "foo-*")
public ChannelInterceptor fooInterceptor() {
    return new ChannelInterceptor() {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            ...
        }
    };
}
```

和


```java
@Bean
@GlobalChannelInterceptor(patterns = "bar-*")
public ChannelInterceptor barInterceptor() {
    return new ChannelInterceptor() {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            ...
        }
    };
}
```


可以根据业务需要灵活地使模式更加严格或定制。

使用这种方法，应用程序可以决定在StreamBridge中注入哪些拦截器，而不是应用所有可用的拦截器。

**注**：StreamBridge通过包含StreamBridge的所有发送方法的StreamOperations接口提供合约。因此，应用程序可以选择使用StreamOperations自动装配。当涉及到通过为StreamOperations接口提供模拟或类似机制来使用StreamBridge的单元测试代码时，这非常方便。

### 支持反应式函数


由于Spring Cloud Function是在Project Reactor之上构建的，所以在实现供Supplier、Function或Consume时，不需要做太多工作来从反应式编程模型中获益。

例如：



```java
@SpringBootApplication
public static class SinkFromConsumer {

	@Bean
	public Function<Flux<String>, Flux<String>> reactiveUpperCase() {
		return flux -> flux.map(val -> val.toUpperCase());
	}
}
```


### 函数组合


Using functional programming model you can also benefit from functional composition where you can dynamically compose complex handlers from a set of simple functions. As an example let’s add the following function bean to the application defined above

@Bean
public Function<String, String> wrapInQuotes() {
	return s -> "\"" + s + "\"";
}

and modify the spring.cloud.function.definition property to reflect your intention to compose a new function from both ‘toUpperCase’ and ‘wrapInQuotes’. To do so Spring Cloud Function relies on | (pipe) symbol. So, to finish our example our property will now look like this:

--spring.cloud.function


使用函数式编程模型，还可以从函数组合中受益，在函数组合中，可以从一组简单函数中动态组合复杂的处理程序。作为示例，让我们向上面定义的应用程序添加以下函数bean


```java
@Bean
public Function<String, String> wrapInQuotes() {
	return s -> "\"" + s + "\"";
}
```

并修改spring.cloud.function.definition属性，以反映您从“toUpperCase”和“wrapInQuotes”组成新函数的意图。为此，Spring Cloud函数依赖于“|”符号。因此，为了完成我们的示例，我们的属性现在如下所示：


```
--spring.cloud.function.definition=toUpperCase|wrapInQuotes
```


**注**：Spring Cloud Function提供的功能组合支持的一大好处是，可以组合反应式和命令式函数。

组合的结果是一个函数，正如您可能猜到的那样，该函数可能有一个非常长且相当隐秘的名称（例如，`foo|bar|baz|xyz…`），在涉及其他配置属性时会带来很大的不便。这就是函数binding名称部分中描述的描述性binding名称功能可以帮助的地方。

例如，如果我们想给`toUpperCase|wrapInQuotes`一个更具描述性的名称，我们可以使用以下属性`spring.cloud.stream.function.bindings.toUpperCase|wrapInQuotes-in-0=quotedUpperCaseInput`，允许其他配置属性引用该binding名称（例如，`spring.cloud.stream.bindings.quotedUpperCaseInput.destination=myDestination`）。


#### 函数组合和交叉关注点

函数组合可以有效地将复杂性分解为一组简单且可单独管理/测试的组件，这些组件在运行时仍然可以表示为一个组件。但这并不是唯一的好处。
还可以使用组合来解决某些跨领域的非功能性问题，例如内容丰富。例如，假设您有一条传入消息，该消息可能缺少某些标头，或者某些标头的状态与您的业务功能预期的状态不同。现在，您可以实现一个单独的函数来解决这些问题，然后将其与主业务函数组合。

让我们看一下示例

```java
@SpringBootApplication
public class DemoStreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoStreamApplication.class,
				"--spring.cloud.function.definition=enrich|echo",
				"--spring.cloud.stream.function.bindings.enrich|echo-in-0=input",
				"--spring.cloud.stream.bindings.input.destination=myDestination",
				"--spring.cloud.stream.bindings.input.group=myGroup");

	}

	@Bean
	public Function<Message<String>, Message<String>> enrich() {
		return message -> {
			Assert.isTrue(!message.getHeaders().containsKey("foo"), "Should NOT contain 'foo' header");
			return MessageBuilder.fromMessage(message).setHeader("foo", "bar").build();
		};
	}

	@Bean
	public Function<Message<String>, Message<String>> echo() {
		return message -> {
			Assert.isTrue(message.getHeaders().containsKey("foo"), "Should contain 'foo' header");
			System.out.println("Incoming message " + message);
			return message;
		};
	}
}
```

虽然很简单，但这个示例演示了一个函数如何使用额外的头（非功能性关注点）来丰富传入的消息，因此另一个函数echo可以从中受益。echo函数保持干净，只关注业务逻辑。您还可以看到spring.cloud.stream.function.bindings 属性的用法，以简化组合binding名称。

### 具有多个输入和输出参数的函数

从3.0版开始，Spring Cloud Stream支持具有多个输入和/或多个输出（返回值）的函数。这实际上意味着什么？它针对的是什么类型的用例？

* 大数据：假设正在处理的数据源是高度未组织的，包含各种类型的数据元素（例如，订单、交易等），您需要有效地对其进行排序。
* 数据聚合：另一个用例可能要求您合并来自2+个传入的_stream的数据元素。

上面只描述了一些用例，其中您可能需要使用一个函数来接受和/或生成多个数据流。这就是我们这里针对的用例类型。

此外，请注意这里对流概念的强调略有不同。假设这样的函数只有在被允许访问实际的数据流（而不是单个元素）时才有价值。因此，我们依赖于ProjectReactor提供的抽象（即Flux和Mono），这些抽象在类路径上已经可用，作为Spring Cloud Stream Function带来的依赖项的一部分。

另一个重要方面是多个输入和输出的表示。虽然Java提供了各种不同的抽象来表示多种东西，但这些抽象是：

* 无界的
* 缺乏参数
* 缺乏类型信息


这些在这个上下文中都很重要。作为一个示例，让我们看看Collection或一个数组，它只允许我们描述单个类型的多个，或者将所有内容都向上转换为一个Object，从而影响Spring Cloud Stream的透明类型转换特性等等。

因此，为了满足所有这些需求，最初的支持依赖于签名，该签名利用了Project Reactor提供的另一个抽象——Tuple。然而，我们正在努力允许更灵活的签名。


观察以下示例：


```java
@SpringBootApplication
public class SampleApplication {

	@Bean
	public Function<Tuple2<Flux<String>, Flux<Integer>>, Flux<String>> gather() {
		return tuple -> {
			Flux<String> stringStream = tuple.getT1();
			Flux<String> intStream = tuple.getT2().map(i -> String.valueOf(i));
			return Flux.merge(stringStream, intStream);
		};
	}
}
```

上面的示例演示了一个函数，它接受两个输入（第一个是String类型，第二个是Integer类型），并生成一个String类型的输出。
因此，对于上面的示例，两个输入binding将是gather-in-0和gather-in-1，为了一致性，输出binding也遵循相同的约定，并命名为gather-out-0。
知道这将允许设置绑定特定的属性。例如，以下内容将覆盖gather-in-0绑定的内容类型：



```
--spring.cloud.stream.bindings.gather-in-0.content-type=text/plain
```


```java
@SpringBootApplication
public class SampleApplication {

	@Bean
	public static Function<Flux<Integer>, Tuple2<Flux<String>, Flux<String>>> scatter() {
		return flux -> {
			Flux<Integer> connectedFlux = flux.publish().autoConnect(2);
			UnicastProcessor even = UnicastProcessor.create();
			UnicastProcessor odd = UnicastProcessor.create();
			Flux<Integer> evenFlux = connectedFlux.filter(number -> number % 2 == 0).doOnNext(number -> even.onNext("EVEN: " + number));
			Flux<Integer> oddFlux = connectedFlux.filter(number -> number % 2 != 0).doOnNext(number -> odd.onNext("ODD: " + number));

			return Tuples.of(Flux.from(even).doOnSubscribe(x -> evenFlux.subscribe()), Flux.from(odd).doOnSubscribe(x -> oddFlux.subscribe()));
		};
	}
}
```


上面的示例与前面的示例有些相反，它演示了一个函数，该函数接受Integer类型的单个输入并生成两个输出（都是String类型）。
因此，对于上面的示例，输入binding是scatter-in-0，输出binding是scatter-out-0和scatter-out-1。


然后使用以下代码进行测试：

```Java
@Test
public void testSingleInputMultiOutput() {
	try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
			TestChannelBinderConfiguration.getCompleteConfiguration(
					SampleApplication.class))
							.run("--spring.cloud.function.definition=scatter")) {

		InputDestination inputDestination = context.getBean(InputDestination.class);
		OutputDestination outputDestination = context.getBean(OutputDestination.class);

		for (int i = 0; i < 10; i++) {
			inputDestination.send(MessageBuilder.withPayload(String.valueOf(i).getBytes()).build());
		}

		int counter = 0;
		for (int i = 0; i < 5; i++) {
			Message<byte[]> even = outputDestination.receive(0, 0);
			assertThat(even.getPayload()).isEqualTo(("EVEN: " + String.valueOf(counter++)).getBytes());
			Message<byte[]> odd = outputDestination.receive(0, 1);
			assertThat(odd.getPayload()).isEqualTo(("ODD: " + String.valueOf(counter++)).getBytes());
		}
	}
}
```




### 单个应用程序中的多个函数

还可能需要在单个应用程序中对多个消息处理程序进行分组。可以通过定义几个函数来实现。


```java
@SpringBootApplication
public class SampleApplication {

	@Bean
	public Function<String, String> uppercase() {
		return value -> value.toUpperCase();
	}

	@Bean
	public Function<String, String> reverse() {
		return value -> new StringBuilder(value).reverse().toString();
	}
}
```



在上面的例子中，我们有一个配置，它定义了两个大写和反大写的函数。因此，首先，如前所述，我们需要注意到存在冲突（不止一个函数），因此我们需要通过提供指向我们要绑定的实际函数的spring.cloud.function.definition属性来解决冲突。除了这里，我们将使用“;”分隔符指向两个函数（请参见下面的测试用例）。


```java
@Test
public void testMultipleFunctions() {
	try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
			TestChannelBinderConfiguration.getCompleteConfiguration(
					ReactiveFunctionConfiguration.class))
							.run("--spring.cloud.function.definition=uppercase;reverse")) {

		InputDestination inputDestination = context.getBean(InputDestination.class);
		OutputDestination outputDestination = context.getBean(OutputDestination.class);

		Message<byte[]> inputMessage = MessageBuilder.withPayload("Hello".getBytes()).build();
		inputDestination.send(inputMessage, "uppercase-in-0");
		inputDestination.send(inputMessage, "reverse-in-0");

		Message<byte[]> outputMessage = outputDestination.receive(0, "uppercase-out-0");
		assertThat(outputMessage.getPayload()).isEqualTo("HELLO".getBytes());

		outputMessage = outputDestination.receive(0, "reverse-out-1");
		assertThat(outputMessage.getPayload()).isEqualTo("olleH".getBytes());
	}
}
```


### 批量消费者

当使用支持批处理侦听器的MessageChannelBinder，并且为使用者绑定启用了该功能时，可以将`spring.cloud.stream.bindings.<binding-name>.consumer.batch-mode`设置为true，以允许将整个批处理消息传递给列表中的函数。



```java
@Bean
public Function<List<Person>, Person> findFirstPerson() {
    return persons -> persons.get(0);
}
```


### 批量生产商


还可以在生产者端使用批处理的概念，返回一个消息集合，这有效地提供了一种相反的效果，即集合中的每个消息都将由binder单独发送。
考虑以下函数：


```java
@Bean
public Function<String, List<Message<String>>> batch() {
	return p -> {
		List<Message<String>> list = new ArrayList<>();
		list.add(MessageBuilder.withPayload(p + ":1").build());
		list.add(MessageBuilder.withPayload(p + ":2").build());
		list.add(MessageBuilder.withPayload(p + ":3").build());
		list.add(MessageBuilder.withPayload(p + ":4").build());
		return list;
	};
}
```

返回列表中的每条消息将被单独发送，从而导致四条消息被发送到输出destination。


### Spring Integration流作为函数



当实现一个功能时，可能会有符合企业集成模式（EIP）类别的复杂需求。最好使用Spring Integration（SI）这样的框架来处理这些问题，这是EIP的一个参考实现。

值得庆幸的是，SI已经为通过集成流作为网关将集成流作为函数公开提供了支持。请考虑以下示例：


```java
@SpringBootApplication
public class FunctionSampleSpringIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(FunctionSampleSpringIntegrationApplication.class, args);
	}

	@Bean
	public IntegrationFlow uppercaseFlow() {
		return IntegrationFlows.from(MessageFunction.class, "uppercase")
				.<String, String>transform(String::toUpperCase)
				.logAndReply(LoggingHandler.Level.WARN);
	}

	public interface MessageFunction extends Function<Message<String>, Message<String>> {

	}
}
```


对于熟悉SI的人，您可以看到我们定义了一个IntegrationFlow类型的bean，其中我们声明了一个集成流，我们希望将其公开为一个名为“uppercase”的`Function<String, String>`（使用SI DSL）。MessageFunction接口允许我们显式声明输入和输出的类型，以便进行正确的类型转换。

要接收原始输入，可以使用`from(Function.class, …​).`。

生成的函数binder到目标绑定器暴露的输入和输出destination。




## 使用轮询消费者


When using polled consumers, you poll the PollableMessageSource on demand. To define binding for polled consumer you need to provide spring.cloud.stream.pollable-source property.

Consider the following example of a polled consumer binding:

--spring.cloud.stream.pollable-source=myDestination

The pollable-source name myDestination in the preceding example will result in myDestination-in-0 binding name to stay consistent with functional programming model.

Given the polled consumer in the preceding example, you might use it as follows:

使用轮询消费者时，可以按需轮询PolableMessageSource。要为轮询消费者定义binding，需要提供`spring.cloud.stream.pollable-source`属性。

考虑以下轮询消费者binding示例：

```
--spring.cloud.stream.pollable-source=myDestination
```

前面示例中的可轮询源名称myDestination将导致myDestination-in-0绑定名称与函数式编程模型保持一致。

给定上一示例中的轮询消费者，您可以按如下方式使用它：

```java
@Bean
public ApplicationRunner poller(PollableMessageSource destIn, MessageChannel destOut) {
    return args -> {
        while (someCondition()) {
            try {
                if (!destIn.poll(m -> {
                    String newPayload = ((String) m.getPayload()).toUpperCase();
                    destOut.send(new GenericMessage<>(newPayload));
                })) {
                    Thread.sleep(1000);
                }
            }
            catch (Exception e) {
                // handle failure
            }
        }
    };
}
```


一个不那么手动、更像Spring的替代方法是配置一个调度任务bean。例如

```java
@Scheduled(fixedDelay = 5_000)
public void poll() {
	System.out.println("Polling...");
	this.source.poll(m -> {
		System.out.println(m.getPayload());

	}, new ParameterizedTypeReference<Foo>() { });
}
```



PollableMessageSource.poll()方法采用MessageHandler参数（通常是lambda表达式，如下所示）。如果消息被接收并成功处理，则返回true。

与消息驱动的使用者一样，如果MessageHandler抛出异常，消息将发布到错误通道，如错误处理中所述。

通常，poll()方法在MessageHandler退出时确认消息。如果方法异常退出，消息将被拒绝（不重新排队）。您可以通过对确认负责来覆盖该行为，如下例所示：


```java
@Bean
public ApplicationRunner poller(PollableMessageSource dest1In, MessageChannel dest2Out) {
    return args -> {
        while (someCondition()) {
            if (!dest1In.poll(m -> {
                StaticMessageHeaderAccessor.getAcknowledgmentCallback(m).noAutoAck();
                // e.g. hand off to another thread which can perform the ack
                // or acknowledge(Status.REQUEUE)

            })) {
                Thread.sleep(1000);
            }
        }
    };
}
```


这里还有一个重载轮询方法，其定义如下：

```
poll(MessageHandler handler, ParameterizedTypeReference<?> type)
```


该类型是一个转换提示，允许转换传入的消息负载，如以下示例所示：

```java
boolean result = pollableSource.poll(received -> {
			Map<String, Foo> payload = (Map<String, Foo>) received.getPayload();
            ...

		}, new ParameterizedTypeReference<Map<String, Foo>>() {});

```

### 错误处理

默认情况下，为可轮询源配置错误通道；如果回调抛出异常，则向错误通道（`<destination>.<group>.errors`）发送ErrorMessage；该错误通道也桥接到全局Spring Integration errorChannel。


可以使用`@ServiceActivator`订阅任一错误通道来处理错误；如果没有订阅，错误将被记录下来，消息将被确认为成功。如果错误通道服务激活器抛出异常，消息将被拒绝（默认情况下），并且不会重新传递。如果服务激活器抛出RequeCurrentMessageException，则消息将在代理处重新排队，并将在后续轮询中再次检索。

如果侦听器直接抛出RequeCurrentMessageException，则消息将被重新排队，如上所述，并且不会发送到错误通道。