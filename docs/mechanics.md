# 作法


观察以下消息处理器：

```java
public Function<Person, String> personFunction {..}
```

为了简单起见，假设这是应用程序中唯一的处理程序函数（我们假设没有内部管道）。

上述示例中显示的处理程序期望Person对象作为参数，并生成String类型作为输出。为了使框架成功地将传入的Message作为参数传递给此处理程序，它必须以某种方式将Message类型的有效负载从有线格式转换为Person类型。换句话说，框架必须找到并应用适当的MessageConverter。为了实现这一点，框架需要用户的一些指示。其中一条指令已经由处理程序方法本身的签名（Person类型）提供。因此，理论上，这应该（在某些情况下）足够了。然而，对于大多数用例，为了选择合适的MessageConverter，框架需要额外的信息，那就是contentType。



Spring Cloud Stream提供三种机制来定义contentType（按优先级顺序）：

* HEADER：contentType可以通过Message本身进行通信。通过提供contentType标头，可以声明用于查找和应用适当MessageConverter的内容类型。
* BINDING：通过设置spring.cloud.stream.bindings.input.content-type属性，可以为每个目标binding设置contentType。
* DEFAULT：如果Message头或binding中不存在contentType，则使用默认的“application/json”。



如前所述，前面的列表还显示了平局情况下的优先顺序。例如，标头提供的内容类型优先于任何其他内容类型。这同样适用于基于每个binding设置的内容类型，这本质上允许您覆盖默认内容类型。然而，它也提供了一个合理的默认值。

将application/json设置为默认值的另一个原因是分布式微服务架构驱动的互操作性需求，其中生产者和消费者不仅可以在不同的JVM中运行，还可以在不同非JVM平台上运行。

当非void处理程序方法返回时，如果返回值已经是Message，则该Message将成为有效负载。然而，当返回值不是Message时，新的Message将以返回值作为有效负载，同时从输入Message中继承标头减去SpringIntegrationProperties.messageHandlerNotPropagatedHeaders定义或过滤的标头。默认情况下，只有一个标头集：contentType。这意味着新Message没有contentType标头集，从而确保contentType可以进化。您始终可以选择不从处理程序方法返回Message，在处理程序方法中，您可以注入任何希望的头。

如果存在内部管道，则Message将通过相同的转换过程发送到下一个处理程序。但是，如果没有内部管道或您已经到达了管道的末尾，则Message将被发送回输出destination。

## 内容类型与参数类型


如前所述，对于框架选择适当的MessageConverter，它需要参数类型和可选的内容类型信息。选择适当MessageConverter的逻辑驻留在参数解析器（HandlerMethodArgumentResolvers）中，它在调用用户定义的处理程序方法之前（即框架已知实际参数类型时）触发。如果参数类型与当前有效负载的类型不匹配，则框架将委托给预先配置的MessageConverters 堆栈，以查看其中任何一个是否可以转换有效负载。如您所见，MessageConverter 的`Object fromMessage(Message<?> message, Class<?> targetClass);` 操作将targetClass作为其参数之一。框架还确保提供的Message始终包含contentType标头。当没有contentType标头时，它会注入每个绑定的contentType标头或默认contentType标头。contentType参数类型的组合是框架确定消息是否可以转换为目标类型的机制。如果找不到合适的MessageConverter，则会引发异常，可以通过添加自定义MessageConverter来处理该异常。

但是如果有效负载类型与处理程序方法声明的目标类型匹配呢？在这种情况下，没有什么要转换的，并且未修改地传递有效负载。虽然这听起来非常简单和合乎逻辑，但请记住接受`Message<?>` 或 Object作为参数。通过将目标类型声明为Object（这是Java中所有内容的一个实例），实际上就放弃了转换过程。

**注**：不要期望仅基于contentType将Message转换为其他类型。请记住，contentType是目标类型的补充。如果你愿意，你可以提供一个提示，MessageConverter可能会考虑，也可能不会考虑。


## 消息转换器



MessageConverters定义了两种方法：

```java
Object fromMessage(Message<?> message, Class<?> targetClass);

Message<?> toMessage(Object payload, @Nullable MessageHeaders headers);
```



理解这些方法的契约及其用法非常重要，特别是在Spring Cloud Stream的上下文中。

fromMessage方法将传入的Message转换为参数类型。Message的有效负载可以是任何类型，并且它取决于MessageConverter的实际实现来支持多种类型。例如，某些JSON转换器可能支持`byte[]`、`String`等有效负载类型。当应用程序包含内部管道（`input → handler1 → handler2 →. . . → output`），并且上游处理程序的输出导致可能不是初始有线格式的消息。
然而，toMessage方法有一个更严格的约定，必须始终将Message转换为有线格式：`byte[]`。

因此，出于所有意图和目的（尤其是在实现自己的转换器时），认为这两种方法具有以下签名：



```java
Object fromMessage(Message<?> message, Class<?> targetClass);

Message<byte[]> toMessage(Object payload, @Nullable MessageHeaders headers);
```