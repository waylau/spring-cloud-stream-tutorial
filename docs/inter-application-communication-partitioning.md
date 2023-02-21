# 分区


Spring Cloud Stream中的分区包括两个任务：

* 配置分区的输出binding
* 配置分区的输入binding



## 配置分区的输出binding


通过设置partitionKeyExpression或partitionKeyExtractorName其中的一个属性及其partitionCount属性来配置输出binding发送分区数据。

例如，以下是有效的典型配置：


```java
spring.cloud.stream.bindings.func-out-0.producer.partitionKeyExpression=headers.id
spring.cloud.stream.bindings.func-out-0.producer.partitionCount=5
```


基于该示例配置，通过使用以下逻辑将数据发送到目标分区。

根据partitionKeyExpression为发送到分区输出绑定的每个消息计算分区键的值。partitionKeyExpression是一个SpEL表达式，它根据出站消息（在前面的示例中是消息头中的id值）进行计算，以提取分区key。

如果SpEL表达式不足以满足您的需要，可以通过提供org.springframework.cloud.stream.binder.PartitionKeyExtractorStrategy的实现并将其配置为bean来计算分区键值。如果应用程序上下文中有多个类型为org.springframework.cloud.stream.binder.PartitionKeyExtractorStrategy的bean，则可以通过使用partitionKeyExtractorName属性指定其名称来进一步过滤它，如下例所示：


```java
--spring.cloud.stream.bindings.func-out-0.producer.partitionKeyExtractorName=customPartitionKeyExtractor
--spring.cloud.stream.bindings.func-out-0.producer.partitionCount=5
. . .
@Bean
public CustomPartitionKeyExtractorClass customPartitionKeyExtractor() {
    return new CustomPartitionKeyExtractorClass();
}
```


计算消息key后，分区选择程序将目标分区确定为介于0和`partitionCount-1`之间的值。适用于大多数场景的默认计算基于以下公式：`key.hashCode() % partitionCount`。这可以在绑定上自定义，方法是设置要根据“key”计算的SpEL表达式（通过partitionSelectorExpression属性），或者将org.springframework.cloud.stream.binder.PartitionSelectorStrategy的实现配置为bean。与PartitionKeyExtractorStrategy类似，当Application Context中有多个此类型的bean可用时，可以使用spring.cloud.stream.bindings.output.producer.partitionSelectorName属性对其进行进一步过滤，如下例所示：

```java
--spring.cloud.stream.bindings.func-out-0.producer.partitionSelectorName=customPartitionSelector
. . .
@Bean
public CustomPartitionSelectorClass customPartitionSelector() {
    return new CustomPartitionSelectorClass();
}
```

## 配置分区的输入binding


输入binding（binding名称为uppercase-in-0）配置为通过设置其分区属性以及应用程序本身的instanceIndex和instanceCount属性来接收分区数据，如下例所示：


```java
spring.cloud.stream.bindings.uppercase-in-0.consumer.partitioned=true
spring.cloud.stream.instanceIndex=3
spring.cloud.stream.instanceCount=5
```

instanceCount值表示应在其中分区数据的应用程序实例总数。instanceIndex必须是多个实例中的唯一值，其值介于0和instanceCount-1之间。实例索引帮助每个应用程序实例标识其从中接收数据的唯一分区。如果原生不支持分区，则需要Binder使用特定的技术。例如，对于RabbitMQ，每个分区都有一个队列，队列名称包含实例索引。使用Kafka，如果autoRebalanceEnabled为true（默认值），则Kafka负责跨实例分布分区，并且不需要这些属性。如果autoRebalanceEnabled设置为false，绑定器将使用instanceCount和instanceIndex来确定实例订阅的分区（必须至少拥有与实例数量相同的分区）。绑定器分配分区而不是Kafka。如果希望特定分区的消息始终指向同一实例，这可能很有用。当绑定器配置需要它们时，必须正确设置这两个值，以确保使用所有数据，并且应用程序实例接收互斥数据集。

虽然使用多个实例进行分区数据处理的场景在单独的情况下可能很复杂，但Spring Cloud Data flow可以通过正确填充输入和输出值，并允许您依赖运行时基础设施来提供有关实例索引和实例计数的信息，从而大大简化流程。