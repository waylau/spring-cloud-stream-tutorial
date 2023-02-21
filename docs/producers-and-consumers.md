# 生产者与消费者

下图显示了生产者和消费者的一般关系：




![生产者和消费者](../images/producers-consumers.pngs)

生产者是向binding destination发送消息的组件。binding destination可以通过该代理的Binder实现绑定到外部消息代理。调用 bindProducer() 方法时，第一个参数是代理中destination 的名称，第二个参数是生产者向其发送消息的本地 destination 实例，第三个参数包含要在为该binding destination创建的适配器中使用的属性（如分区键表达式）。

消费者是从binding destination接收消息的组件。与生产者一样，消费者可以绑定到外部消息代理。当调用 bindConsumer() 方法时，第一个参数是 destination 名称，第二个参数提供一组逻辑消费者的名称。由给定destination的消费binding表示的每个组接收生产者发送到该destination的每个消息的副本（即，它遵循正常的发布-订阅语义）。如果有多个消费者实例与同一组名称绑定，则消息在这些消费者实例之间进行负载平衡，以便生产者发送的每个消息仅由每个组中的一个消费者实例使用（即，它遵循正常的排队语义）。