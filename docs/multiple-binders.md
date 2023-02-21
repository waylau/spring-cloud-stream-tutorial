# 多Binder

当类路径上存在多个Binder时，应用程序必须指明每个destination binding要使用哪个Binder。每个Binder配置都包含一个META-INF/spring.binders文件，这是一个简单的属性文件，如下例所示：


```
rabbit:\
org.springframework.cloud.stream.binder.rabbit.config.RabbitServiceAutoConfiguration
```

其他提供的Binder实现（如Kafka）也存在类似的文件，自定义Binder实现也会提供这些文件。键表示Binder实现的标识名称，而值是一个逗号分隔的配置类列表，每个配置类包含一个且仅包含一个类型为org.springframework.cloud.stream.binder.Binder的bean定义。

可以使用spring.cloud.stream.defaultBinder属性（例如，spring.cloud.stream.defaultBinder=rabbit）全局执行Binder选择，也可以通过在每个binding上配置Binder来单独执行。例如，从Kafka读取并写入RabbitMQ的处理器应用程序（分别具有名为input和output的读和写binding）可以指定以下配置：


```
spring.cloud.stream.bindings.input.binder=kafka
spring.cloud.stream.bindings.output.binder=rabbit
```