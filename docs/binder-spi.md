# Binder SPI

Binder SPI由许多接口、开箱即用的实用程序类和发现策略组成，这些策略为连接到外部中间件提供了可插拔机制。

SPI的关键点是Binder接口，它是一种将输入和输出连接到外部中间件的策略。以下列表显示了Binder界面的定义：


```java
public interface Binder<T, C extends ConsumerProperties, P extends ProducerProperties> {
    Binding<T> bindConsumer(String bindingName, String group, T inboundBindTarget, C consumerProperties);

    Binding<T> bindProducer(String bindingName, T outboundBindTarget, P producerProperties);
}
```


接口是参数化的，提供了许多扩展点：

* 输入和输出绑定目标。
* 扩展了使用者和生产者属性，允许特定的Binder实现添加可以以类型安全方式支持的补充属性。

典型的Binder实现包括以下内容：

* 实现Binder接口的类；
* 一个Spring的`@Configuration`类，它创建Binder类型的bean以及中间件连接基础结构。
* 一个或多个Binder定义，定义在类路径中的META-INF/spring.binders文件中，如下例所示：

```
kafka:\
org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration
```



