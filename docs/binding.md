# Binding

如前所述，绑定提供了外部消息传递系统（例如，队列、topic等）与应用程序提供的生产者和消费者之间的桥梁。

下面的示例显示了一个完全配置和运行的Spring Cloud Stream应用程序，它以字符串类型接收消息的有效负载，将其记录到控制台，并在将其转换为大写后将其发送到下游。



```java
@SpringBootApplication
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

	@Bean
	public Function<String, String> uppercase() {
	    return value -> {
	        System.out.println("Received: " + value);
	        return value.toUpperCase();
	    };
	}
}
```



上面的示例它定义了一个Function类型的bean，遵循Spring Boot程序的约定来读取配置。在这种情况下，Supplier、Function或Consumer类型的bean被视为触发绑定到所提供的binder公开的destination的事实消息处理程序，遵循某些命名约定和规则，以避免额外配置。


## Binding 和 Binding 名字

Binding是一个抽象，它表示 binder 和用户代码所暴露的源和目标之间的桥梁。这个抽象有一个名称，尽管我们尽力限制运行Spring Cloud Stream应用程序所需的配置，但在需要额外的每个绑定配置的情况下，了解这些名称是必要的。

例如`spring.cloud.stream.bindings.input.destination=myQueue`,此属性名称中的`input`段是我们所称的绑定名称，它可以通过多种机制派生。以下小节将描述Spring Cloud Stream用于控制绑定名称的命名约定和配置元素。

### 函数式 Binding 名字

与Spring Cloud Stream早期版本中使用的基于注释的显式命名不同，函数式编程模型在涉及Binding名称时默认采用简单的约定，从而大大简化了应用程序配置。让我们来看第一个示例：




```java
@SpringBootApplication
public class SampleApplication {

	@Bean
	public Function<String, String> uppercase() {
	    return value -> value.toUpperCase();
	}
}
```


上述示例具有一个充当消息处理程序的函数uppercase。作为一个函数，它有输入和输出。用于命名输入和输出binding的命名约定如下：


* 输入： `<functionName> + -in- + <index>`
* 输出： `<functionName> + -out- + <index>`


in和out对应于Binding的类型（例如input 或output）。index是输入或输出Binding的索引。对于典型的单输入/输出函数，它总是0，因此它只与具有多个输入和输出参数的函数相关。

例如，如果希望将此函数的输入映射到名为“my-topic”的destination （例如，topic、队列等），则可以使用以下属性进行映射：

```
--spring.cloud.stream.bindings.uppercase-in-0.destination=my-topic
```

上面配置中的“uppercase-in-0”即为Binding名字。同理，作为输出Binding的命名，是“uppercase-out-0”。



#### 描述性Binding名称

有时为了提高可读性，可能希望为Binding提供一个更具描述性的名称（例如“account”、“order”等）。可以使用`spring.cloud.stream.function.bindings.<binding-name>`属性来实现。此属性还为依赖于需要显式名称的基于自定义接口的binding的现有应用程序提供迁移路径。

例如

```
--spring.cloud.stream.function.bindings.uppercase-in-0=input
```

在上面的示例中，将“uppercase-In-0”名称映射并有效重命名为“input”。现在，所有配置都可以引用输入“input”（例如，`--spring.cloud.stream.bindings.input.destination=my-topic`）。

**注**：虽然描述性Binding名称可以增强配置的可读性，但它们也会通过将隐式Binding名称映射到显式Binding名称从而产生一些误导。在大多数情况下建议避免完全使用描述性Binding名称，


### 显示 Binding 创建


在上一节中，解释了如何通过应用程序提供的Function、Supplier或Consumer bean的名称隐式地创建Binding。然而，有时可能需要在绑定未绑定到任何函数的情况下显式创建绑定。这通常是为了通过StreamBridge支持与其他框架的集成。

Spring Cloud Stream允许通过`spring.cloud.stream.input-bindings`和`spring.cloud.stream.output-bindings`属性显式定义输入和输出Binding。多个Binding可以用英文“;”作为分隔符，以下是一个示例：

```java
@Test
public void testExplicitBindings() {
	try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
		TestChannelBinderConfiguration.getCompleteConfiguration(EmptyConfiguration.class))
				.web(WebApplicationType.NONE)
				.run("--spring.jmx.enabled=false",
					"--spring.cloud.stream.input-bindings=fooin;barin",
					"--spring.cloud.stream.output-bindings=fooout;barout")) {


	. . .
	}
}

@EnableAutoConfiguration
@Configuration
public static class EmptyConfiguration {
}
```


上面例子已经声明了两个输入Binding和两个输出Binding，而配置没有定义函数，但是能够成功地创建这些Binding并访问它们对应的通道。