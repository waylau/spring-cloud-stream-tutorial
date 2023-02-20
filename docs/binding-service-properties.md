# Binding服务属性

这些属性通过`org.springframework.cloud.stream.config.BindingServiceProperties`公开。

* spring.cloud.stream.instanceCount：应用程序的已部署实例数。必须为生产者侧的分区设置。当使用RabbitMQ和Kafka时，如果`autoRebalanceEnabled=false`，则必须在消费者侧设置。
默认值：1。
* spring.cloud.stream.instanceIndex：应用程序的实例索引：从0到instanceCount-1的数字。如果`autoRebalanceEnabled=false`，则用于RabbitMQ和Kafka的分区。在Cloud Foundry中自动设置以匹配应用程序的实例索引。
* spring.cloud.stream.dynamicDestinations：可以动态绑定的destination列表（例如，在动态路由方案中）。如果设置，则只能绑定列出的destination。默认值：空（允许绑定任何destination）。
* spring.cloud.stream.defaultBinder：如果配置了多个Binder，则使用默认Binder。默认值：空。
* spring.cloud.stream.overrideCloudConnectors：此属性仅在 cloud 配置文件处于激活状态且应用程序提供了 Spring Cloud Connectors 时适用。如果属性为false（默认值），Binder将检测到一个合适的绑定服务（例如，在Cloud Foundry中为RabbitMQ Binder绑定的RabbitMQ服务），并将其用于创建连接（通常通过Spring Cloud Connectors）。当设置为true时，此属性指示Binder完全忽略绑定的服务并依赖Spring Boot属性（例如，RabbitMQ Binder环境中提供的`spring.rabbitmq.*`属性）。当连接到多个系统时，此属性的典型用法是嵌套在自定义环境中。默认值：false。
* spring.cloud.stream.bindingRetryInterval：例如，当Binder不支持后期绑定并且代理（例如，Apache Kafka）关闭时，重试绑定创建之间的间隔（以秒为单位）。将其设置为0以表示情况是非常致命的，从而阻止应用程序启动。默认值：30