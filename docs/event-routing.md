# 事件路由



在Spring Cloud Stream的上下文中，事件路由是

* 将事件路由到特定事件订阅者
* 将事件订阅者产生的事件路由到某个特定destination的能力


这里我们将其称为“TO”和“FROM”路由。



## TO消费者路由



路由可以依靠Spring Cloud Function 3.0中的RoutingFunction实现。只需要通过应用程序属性`--spring.cloud.stream.function.routing.enabled=true`或提供`spring.cloud.function.routing-expression`属性来启用它。一旦启用RoutingFunction，将绑定到接收所有消息的输入destination，并根据提供的指令将其路由到其他函数。



### 使用消息头

### 使用应用配置

### RoutingFunction 和输出binding


## FROM消费者路由

### spring.cloud.stream.sendto.destination