# 消费者分组

尽管发布-订阅模型使通过共享 topic 连接应用程序变得容易，但通过创建给定应用程序的多个实例来扩展应用程序的能力同样重要。这样做时，应用程序的不同实例被放置在竞争消费者关系中，其中只有一个实例可以处理给定的消息。

Spring Cloud Stream通过消费者分组的概念对这种行为进行建模。（Spring Cloud Stream消费者分组类似于Kafka消费者分组，并受其启发。）每个消费者 binding 都可以使用`spring.cloud.stream.bindings.<bindingName>.group`属性来指定组名。对于下图所示的使用者，此属性将设置为`spring.cloud.stream.bindings.<bindingName>.group=hdfsWrite`或`spring.cloud.stream.bindings.<bindingName>.group=average`。


![Spring Cloud Stream消费者组](../images/SCSt-groups.png)




订阅给定 destination 的所有组都会收到已发布数据的副本，但每个组中只有一个成员从该 destination 接收给定消息。默认情况下，当未指定组时，Spring Cloud Stream会将应用程序分配给一个匿名且独立的单成员消费者组，该消费者组与所有其他消费者组都处于发布-订阅关系。


## 消费者类型

支持两种类型的消费者：

* 消息驱动（有时称为异步）
* 轮询（有时称为同步）


在2.0版之前，仅支持异步消费者。一旦消息可用，就会立即传递消息，并且有一个线程可以处理它。

当希望控制处理消息的速率时，可能需要使用同步消费者。

## 持久性

消费者组订阅是持久的。也就是说，binder 实现确保组订阅是持久的，并且一旦为组创建了至少一个订阅，即使在组中的所有应用程序停止时发送消息，组也会收到消息。

**注**：匿名订阅本质上是不持久的。对于某些 binder 实现（如RabbitMQ），可以使用非持久组订阅。

通常，在将应用程序绑定到给定目标时，最好始终指定消费者组。在扩展Spring Cloud Stream应用程序时，必须为其每个输入绑定指定一个消费者组。这样做可以防止应用程序的实例接收重复的消息（除非需要这种行为，这是不寻常的）。